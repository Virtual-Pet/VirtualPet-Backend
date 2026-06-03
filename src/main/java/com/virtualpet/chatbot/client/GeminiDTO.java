package com.virtualpet.chatbot.client;

import com.fasterxml.jackson.annotation.JsonInclude;
import java.util.List;
import java.util.Map;

/** DTOs que mapean el formato JSON de la API de Gemini (generateContent). */
public final class GeminiDTO {

  private GeminiDTO() {}

  // ── Request ──────────────────────────────────────────────────────────────

  @JsonInclude(JsonInclude.Include.NON_NULL)
  public record ChatRequest(
      SystemInstruction systemInstruction, List<Content> contents, List<Tool> tools) {}

  public record SystemInstruction(List<Part> parts) {
    public static SystemInstruction of(String text) {
      return new SystemInstruction(List.of(Part.text(text)));
    }
  }

  @JsonInclude(JsonInclude.Include.NON_NULL)
  public record Content(String role, List<Part> parts) {

    public static Content user(String text) {
      return new Content("user", List.of(Part.text(text)));
    }

    public static Content model(String text) {
      return new Content("model", List.of(Part.text(text)));
    }

    public static Content modelFunctionCall(String name, Map<String, Object> args, String thoughtSignature) {
      return new Content("model", List.of(Part.functionCall(name, args, thoughtSignature)));
    }

    public static Content userFunctionResponse(String name, Object response) {
      return new Content("user", List.of(Part.functionResponse(name, response)));
    }
  }

  @JsonInclude(JsonInclude.Include.NON_NULL)
  public record Part(
      String text,
      FunctionCall functionCall,
      FunctionResponse functionResponse,
      String thoughtSignature) {

    public static Part text(String text) {
      return new Part(text, null, null, null);
    }

    public static Part functionCall(String name, Map<String, Object> args, String thoughtSignature) {
      return new Part(null, new FunctionCall(name, args), null, thoughtSignature);
    }

    public static Part functionResponse(String name, Object response) {
      return new Part(null, null, new FunctionResponse(name, response), null);
    }
  }

  @JsonInclude(JsonInclude.Include.NON_NULL)
  public record FunctionCall(String name, Map<String, Object> args) {}

  public record FunctionResponse(String name, Object response) {}

  public record Tool(List<FunctionDeclaration> functionDeclarations) {}

  public record FunctionDeclaration(
      String name, String description, Map<String, Object> parameters) {}

  // ── Response ─────────────────────────────────────────────────────────────

  public record ChatResponse(List<Candidate> candidates) {}

  public record Candidate(Content content, String finishReason) {}
}
