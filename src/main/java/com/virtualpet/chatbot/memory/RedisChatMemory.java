package com.virtualpet.chatbot.memory;

import com.virtualpet.chatbot.client.GeminiDTO;
import tools.jackson.core.type.TypeReference;
import tools.jackson.databind.ObjectMapper;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

/**
 * Historial de conversación persistido en Redis con TTL de 30 minutos. Almacena pares
 * (user, model) como texto plano; el system prompt se agrega siempre en runtime.
 */
@Component
public class RedisChatMemory {

  private static final String KEY_PREFIX = "chat:memory:";
  private static final long TTL_SECONDS = 30 * 60;
  private static final int MAX_TURNS = 10; // 10 pares user+model = 20 mensajes

  private final StringRedisTemplate redis;
  private final ObjectMapper objectMapper;

  public RedisChatMemory(StringRedisTemplate redis, ObjectMapper objectMapper) {
    this.redis = redis;
    this.objectMapper = objectMapper;
  }

  public List<GeminiDTO.Content> getHistory(String sessionId) {
    List<StoredTurn> turns = loadTurns(KEY_PREFIX + sessionId);
    return turns.stream()
        .flatMap(
            t ->
                List.of(GeminiDTO.Content.user(t.userText()), GeminiDTO.Content.model(t.modelText()))
                    .stream())
        .toList();
  }

  public void save(String sessionId, String userText, String modelText) {
    String key = KEY_PREFIX + sessionId;
    List<StoredTurn> turns = loadTurns(key);
    turns.add(new StoredTurn(userText, modelText));
    if (turns.size() > MAX_TURNS) {
      turns = turns.subList(turns.size() - MAX_TURNS, turns.size());
    }
    try {
      redis.opsForValue().set(key, objectMapper.writeValueAsString(turns), TTL_SECONDS, TimeUnit.SECONDS);
    } catch (Exception ignored) {
    }
  }

  private List<StoredTurn> loadTurns(String key) {
    String json = redis.opsForValue().get(key);
    if (json == null || json.isBlank()) return new ArrayList<>();
    try {
      return new ArrayList<>(objectMapper.readValue(json, new TypeReference<List<StoredTurn>>() {}));
    } catch (Exception e) {
      return new ArrayList<>();
    }
  }

  record StoredTurn(String userText, String modelText) {}
}
