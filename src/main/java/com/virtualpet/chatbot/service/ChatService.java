package com.virtualpet.chatbot.service;

import tools.jackson.databind.ObjectMapper;
import com.virtualpet.chatbot.client.GeminiClient;
import com.virtualpet.chatbot.client.GeminiDTO;
import com.virtualpet.chatbot.memory.RedisChatMemory;
import com.virtualpet.chatbot.tools.ChatToolRegistry;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class ChatService {

  private static final int MAX_TOOL_ROUNDS = 5;

  // Patterns para parsear tool calls en formato ReAct emitidos como texto plano
  private static final Pattern ACTION_PATTERN =
      Pattern.compile("\"action\"\\s*:\\s*\"([^\"]+)\"");
  private static final Pattern STRING_FIELD_PATTERN =
      Pattern.compile("\"([^\"]+)\"\\s*:\\s*\"([^\"]+)\"");

  private final GeminiClient geminiClient;
  private final RedisChatMemory chatMemory;
  private final ChatToolRegistry toolRegistry;
  private final ObjectMapper objectMapper;

  @Value("classpath:chatbot/system-prompt.txt")
  private Resource systemPromptResource;

  public String chat(String sessionId, String message, UUID userId)
      throws IOException, InterruptedException {

    log.warn("### CHAT REQUEST sessionId={} userId={}", sessionId, userId);
    String systemPrompt = systemPromptResource.getContentAsString(StandardCharsets.UTF_8);
    List<GeminiDTO.Tool> tools = userId != null ? toolRegistry.getToolDefinitions() : null;

    List<GeminiDTO.Content> contents = new ArrayList<>(chatMemory.getHistory(sessionId));
    contents.add(GeminiDTO.Content.user(message));

    String finalText = null;

    for (int round = 0; round < MAX_TOOL_ROUNDS; round++) {
      var request = new GeminiDTO.ChatRequest(
          GeminiDTO.SystemInstruction.of(systemPrompt), contents, tools);
      var response = geminiClient.chat(request);

      if (response.candidates() == null || response.candidates().isEmpty()) {
        break;
      }
      var candidate = response.candidates().getFirst();
      if (candidate.content() == null || candidate.content().parts() == null) {
        log.warn("Gemini returned null content, finishReason={}", candidate.finishReason());
        break;
      }
      var parts = candidate.content().parts();

      var functionCallPart = parts.stream().filter(p -> p.functionCall() != null).findFirst();

      if (functionCallPart.isEmpty()) {
        String text = parts.stream()
            .filter(p -> p.text() != null)
            .map(GeminiDTO.Part::text)
            .findFirst()
            .orElse("");

        // Thinking models sometimes emit tool calls as plain text (ReAct JSON format)
        // instead of using the structured function calling API. Try to parse and execute.
        if (userId != null) {
          var textToolCall = tryParseTextToolCall(text);
          if (textToolCall != null) {
            log.warn("Model leaked text-based tool call for '{}', executing as fallback", textToolCall.name());
            String toolResult = toolRegistry.execute(textToolCall.name(), textToolCall.args(), userId);
            contents.add(GeminiDTO.Content.modelFunctionCall(textToolCall.name(), textToolCall.args(), null));
            contents.add(GeminiDTO.Content.userFunctionResponse(textToolCall.name(), Map.of("result", toolResult)));
            continue;
          }
        }

        finalText = text;
        break;
      }

      // Structured function call — ejecutar la tool y continuar el loop
      var fcPart = functionCallPart.get();
      var fc = fcPart.functionCall();
      Map<String, Object> args = fc.args() != null ? fc.args() : Map.of();
      String toolResult = toolRegistry.execute(fc.name(), args, userId);

      contents.add(GeminiDTO.Content.modelFunctionCall(fc.name(), args, fcPart.thoughtSignature()));
      contents.add(GeminiDTO.Content.userFunctionResponse(fc.name(), Map.of("result", toolResult)));
    }

    if (finalText == null) finalText = "No pude procesar tu consulta. Por favor, intentá de nuevo.";

    chatMemory.save(sessionId, message, finalText);
    return finalText;
  }

  private record TextToolCall(String name, Map<String, Object> args) {}

  // Formato ReAct:  {"action": "funcName", "action_input": {...}}
  // Formato directo: {"funcName": {...args}}
  private static final Pattern DIRECT_TOOL_PATTERN =
      Pattern.compile("\"(getMyOrders|requestInvoice)\"\\s*:\\s*\\{");

  /**
   * Parsea tool calls textuales en dos formatos que emite gemini-3.1-flash-lite:
   *   1. ReAct:   {"action": "funcName", "action_input": {...}}
   *   2. Directo: {"funcName": {...args}}
   */
  private TextToolCall tryParseTextToolCall(String text) {
    if (text == null || text.isBlank()) return null;
    int start = text.indexOf('{');
    int end = text.lastIndexOf('}');
    if (start == -1 || end <= start) return null;
    String json = text.substring(start, end + 1);

    log.warn("tryParseTextToolCall input: [{}]", json);

    // Formato 1: ReAct {"action": "funcName", "action_input": {...}}
    Matcher actionMatcher = ACTION_PATTERN.matcher(json);
    if (actionMatcher.find()) {
      String name = actionMatcher.group(1);
      log.warn("tryParseTextToolCall: formato ReAct, tool={}", name);
      return new TextToolCall(name, extractStringArgs(json, "action_input"));
    }

    // Formato 2: directo {"funcName": {...args}}
    Matcher directMatcher = DIRECT_TOOL_PATTERN.matcher(json);
    if (directMatcher.find()) {
      String name = directMatcher.group(1);
      log.warn("tryParseTextToolCall: formato directo, tool={}", name);
      int argsOpen = json.indexOf('{', directMatcher.end() - 1);
      int argsClose = json.lastIndexOf('}');
      Map<String, Object> args = new HashMap<>();
      if (argsOpen != -1 && argsClose > argsOpen) {
        String argsJson = json.substring(argsOpen, argsClose + 1);
        Matcher fieldMatcher = STRING_FIELD_PATTERN.matcher(argsJson);
        while (fieldMatcher.find()) {
          String key = fieldMatcher.group(1).equals("order_id") ? "orderId" : fieldMatcher.group(1);
          args.put(key, fieldMatcher.group(2));
        }
      }
      return new TextToolCall(name, args);
    }

    log.warn("tryParseTextToolCall: ningún formato reconocido");
    return null;
  }

  private Map<String, Object> extractStringArgs(String json, String containerKey) {
    Map<String, Object> args = new HashMap<>();
    int keyIdx = json.indexOf(containerKey);
    if (keyIdx == -1) return args;
    int objStart = json.indexOf('{', keyIdx);
    int objEnd = json.indexOf('}', objStart + 1);
    if (objStart == -1 || objEnd == -1) return args;
    Matcher m = STRING_FIELD_PATTERN.matcher(json.substring(objStart, objEnd + 1));
    while (m.find()) {
      String key = m.group(1).equals("order_id") ? "orderId" : m.group(1);
      args.put(key, m.group(2));
    }
    return args;
  }
}
