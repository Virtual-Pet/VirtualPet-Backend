package com.virtualpet.common.ratelimit;

import java.time.Duration;
import java.util.concurrent.TimeUnit;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

/**
 * Fixed-window counter in Redis. Each (scope, clientKey) gets a counter incremented per request; on
 * overflow the caller is denied until the window expires.
 */
@Service
@RequiredArgsConstructor
public class RateLimiter {

  private final StringRedisTemplate redis;

  public RateLimitDecision tryAcquire(String scope, String clientKey, int limit, Duration window) {
    String key = "ratelimit:" + scope + ":" + clientKey;
    Long count = redis.opsForValue().increment(key);
    if (count == null) {
      return RateLimitDecision.allow();
    }
    if (count == 1L) {
      redis.expire(key, window);
    }
    if (count > limit) {
      Long ttlSeconds = redis.getExpire(key, TimeUnit.SECONDS);
      long retryAfter = ttlSeconds == null || ttlSeconds < 0 ? window.toSeconds() : ttlSeconds;
      return RateLimitDecision.deny(retryAfter);
    }
    return RateLimitDecision.allow();
  }
}
