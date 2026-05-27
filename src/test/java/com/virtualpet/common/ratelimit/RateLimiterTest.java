package com.virtualpet.common.ratelimit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

import java.time.Duration;
import java.util.concurrent.TimeUnit;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

@ExtendWith(MockitoExtension.class)
class RateLimiterTest {

  @Mock private StringRedisTemplate redis;

  @Mock private ValueOperations<String, String> valueOps;

  private RateLimiter rateLimiter;

  @BeforeEach
  void setup() {
    when(redis.opsForValue()).thenReturn(valueOps);
    rateLimiter = new RateLimiter(redis);
  }

  @Test
  void allowsRequestsUnderTheLimit() {
    when(valueOps.increment(anyString())).thenReturn(1L, 2L, 3L);

    for (int i = 0; i < 3; i++) {
      RateLimitDecision decision =
          rateLimiter.tryAcquire("login", "1.2.3.4", 5, Duration.ofMinutes(1));
      assertThat(decision.allowed()).isTrue();
    }
  }

  @Test
  void deniesWithRetryAfterWhenLimitExceeded() {
    when(valueOps.increment(anyString())).thenReturn(6L);
    when(redis.getExpire(anyString(), eq(TimeUnit.SECONDS))).thenReturn(42L);

    RateLimitDecision decision =
        rateLimiter.tryAcquire("login", "1.2.3.4", 5, Duration.ofMinutes(1));

    assertThat(decision.allowed()).isFalse();
    assertThat(decision.retryAfterSeconds()).isEqualTo(42L);
  }

  @Test
  void setsExpiryOnTheFirstHitOfAWindow() {
    when(valueOps.increment(anyString())).thenReturn(1L);

    rateLimiter.tryAcquire("login", "1.2.3.4", 5, Duration.ofSeconds(60));

    org.mockito.Mockito.verify(redis).expire(anyString(), any(Duration.class));
  }

  @Test
  void fallsBackToWindowWhenTtlIsUnknown() {
    when(valueOps.increment(anyString())).thenReturn(99L);
    when(redis.getExpire(anyString(), eq(TimeUnit.SECONDS))).thenReturn(-1L);

    RateLimitDecision decision =
        rateLimiter.tryAcquire("login", "1.2.3.4", 5, Duration.ofSeconds(60));

    assertThat(decision.allowed()).isFalse();
    assertThat(decision.retryAfterSeconds()).isEqualTo(60L);
  }
}
