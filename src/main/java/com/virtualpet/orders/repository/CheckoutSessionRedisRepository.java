package com.virtualpet.orders.repository;

import com.virtualpet.common.exception.ApiException;
import com.virtualpet.orders.domain.CheckoutSession;
import java.time.Duration;
import java.util.Optional;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Repository;
import tools.jackson.core.JacksonException;
import tools.jackson.databind.ObjectMapper;

/**
 * Redis-backed storage for {@link CheckoutSession}. Two keys per session:
 *
 * <ul>
 *   <li>{@code checkout:session:{sessionId}} — the JSON-serialized session
 *   <li>{@code checkout:user:{userId}:active} — secondary pointer to the user's active sessionId,
 *       used to resolve "reuse active session" without scanning keys
 * </ul>
 *
 * <p>Both share the same TTL (30 min from creation). The active pointer is cleared when the session
 * leaves PENDING/AWAITING_PAYMENT (PAID/CONFIRMED/FAILED).
 */
@Slf4j
@Repository
@RequiredArgsConstructor
public class CheckoutSessionRedisRepository {

  static final Duration SESSION_TTL = Duration.ofMinutes(30);

  private static final String SESSION_KEY_PREFIX = "checkout:session:";
  private static final String USER_ACTIVE_KEY_PREFIX = "checkout:user:";
  private static final String USER_ACTIVE_KEY_SUFFIX = ":active";

  private final StringRedisTemplate redis;
  private final ObjectMapper objectMapper;

  public CheckoutSession save(CheckoutSession session) {
    String json = serialize(session);
    Duration ttl = remainingTtl(session);
    redis.opsForValue().set(sessionKey(session.getId()), json, ttl);
    return session;
  }

  /**
   * Atomically claim the active-session slot for a user. Returns true if claimed, false if the user
   * already had one.
   */
  public boolean tryClaimActive(UUID userId, UUID sessionId, Duration ttl) {
    Boolean claimed =
        redis.opsForValue().setIfAbsent(userActiveKey(userId), sessionId.toString(), ttl);
    return Boolean.TRUE.equals(claimed);
  }

  public void clearActive(UUID userId) {
    redis.delete(userActiveKey(userId));
  }

  public Optional<CheckoutSession> findById(UUID id) {
    String json = redis.opsForValue().get(sessionKey(id));
    if (json == null) {
      return Optional.empty();
    }
    return Optional.of(deserialize(json));
  }

  public Optional<CheckoutSession> findByIdAndUserId(UUID id, UUID userId) {
    return findById(id).filter(s -> userId.equals(s.getUserId()));
  }

  public Optional<CheckoutSession> findActiveByUser(UUID userId) {
    String sessionIdStr = redis.opsForValue().get(userActiveKey(userId));
    if (sessionIdStr == null) {
      return Optional.empty();
    }
    UUID sessionId;
    try {
      sessionId = UUID.fromString(sessionIdStr);
    } catch (IllegalArgumentException ex) {
      log.warn("Active pointer for user {} holds invalid UUID '{}'", userId, sessionIdStr);
      return Optional.empty();
    }
    return findById(sessionId);
  }

  private String sessionKey(UUID id) {
    return SESSION_KEY_PREFIX + id;
  }

  private String userActiveKey(UUID userId) {
    return USER_ACTIVE_KEY_PREFIX + userId + USER_ACTIVE_KEY_SUFFIX;
  }

  private Duration remainingTtl(CheckoutSession session) {
    if (session.getExpiresAt() == null) {
      return SESSION_TTL;
    }
    Duration remaining = Duration.between(java.time.Instant.now(), session.getExpiresAt());
    return remaining.isPositive() ? remaining : Duration.ofSeconds(1);
  }

  private String serialize(CheckoutSession session) {
    try {
      return objectMapper.writeValueAsString(session);
    } catch (JacksonException e) {
      throw new ApiException(
          HttpStatus.INTERNAL_SERVER_ERROR, "Failed to serialize checkout session");
    }
  }

  private CheckoutSession deserialize(String json) {
    try {
      return objectMapper.readValue(json, CheckoutSession.class);
    } catch (JacksonException e) {
      throw new ApiException(HttpStatus.INTERNAL_SERVER_ERROR, "Failed to read checkout session");
    }
  }
}
