package com.virtualpet.common.idempotency;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.virtualpet.common.exception.ApiException;
import java.time.Duration;
import java.util.function.Supplier;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

/**
 * Replay-safe execution gated by an Idempotency-Key. Stores the JSON-serialized result in Redis so
 * retries return the original response without re-executing the action.
 *
 * <p>Concurrent first-time calls are deduplicated via SETNX of an in-flight placeholder; the second
 * concurrent call gets a 409 with a Retry-After-style hint.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class IdempotencyKeyService {

  private static final String PLACEHOLDER = "__in_flight__";
  private static final Duration IN_FLIGHT_TTL = Duration.ofMinutes(2);

  private final StringRedisTemplate redis;
  private final ObjectMapper objectMapper;

  public <T> T executeOnce(
      String scope, String key, Duration ttl, Class<T> type, Supplier<T> action) {
    return execute(scope, key, ttl, action, json -> readValue(json, type));
  }

  public <T> T executeOnce(
      String scope, String key, Duration ttl, TypeReference<T> type, Supplier<T> action) {
    return execute(scope, key, ttl, action, json -> readValue(json, type));
  }

  private <T> T execute(
      String scope,
      String key,
      Duration ttl,
      Supplier<T> action,
      java.util.function.Function<String, T> reader) {
    String redisKey = buildKey(scope, key);

    String existing = redis.opsForValue().get(redisKey);
    if (existing != null && !PLACEHOLDER.equals(existing)) {
      return reader.apply(existing);
    }

    Boolean acquired = redis.opsForValue().setIfAbsent(redisKey, PLACEHOLDER, IN_FLIGHT_TTL);
    if (Boolean.FALSE.equals(acquired)) {
      String afterRace = redis.opsForValue().get(redisKey);
      if (afterRace != null && !PLACEHOLDER.equals(afterRace)) {
        return reader.apply(afterRace);
      }
      throw new ApiException(
          HttpStatus.CONFLICT, "Operation with the same Idempotency-Key is in progress");
    }

    try {
      T result = action.get();
      redis.opsForValue().set(redisKey, writeValue(result), ttl);
      return result;
    } catch (RuntimeException ex) {
      redis.delete(redisKey);
      throw ex;
    }
  }

  private String buildKey(String scope, String key) {
    return "idempotency:" + scope + ":" + key;
  }

  private String writeValue(Object value) {
    try {
      return objectMapper.writeValueAsString(value);
    } catch (JsonProcessingException e) {
      throw new IllegalStateException("Failed to serialize idempotent response", e);
    }
  }

  private <T> T readValue(String json, Class<T> type) {
    try {
      return objectMapper.readValue(json, type);
    } catch (JsonProcessingException e) {
      throw new IllegalStateException("Corrupted idempotent response in cache", e);
    }
  }

  private <T> T readValue(String json, TypeReference<T> type) {
    try {
      return objectMapper.readValue(json, type);
    } catch (JsonProcessingException e) {
      throw new IllegalStateException("Corrupted idempotent response in cache", e);
    }
  }
}
