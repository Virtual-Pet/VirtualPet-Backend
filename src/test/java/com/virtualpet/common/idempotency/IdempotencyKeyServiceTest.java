package com.virtualpet.common.idempotency;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.virtualpet.common.exception.ApiException;
import java.time.Duration;
import java.util.concurrent.atomic.AtomicInteger;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

@ExtendWith(MockitoExtension.class)
class IdempotencyKeyServiceTest {

  @Mock private StringRedisTemplate redis;
  @Mock private ValueOperations<String, String> valueOps;

  private IdempotencyKeyService service;

  record Response(String paymentId, String status) {}

  @BeforeEach
  void setup() {
    when(redis.opsForValue()).thenReturn(valueOps);
    service = new IdempotencyKeyService(redis, new ObjectMapper());
  }

  @Test
  void firstCallExecutesAndCachesTheResult() {
    when(valueOps.get(anyString())).thenReturn(null);
    when(valueOps.setIfAbsent(anyString(), anyString(), any(Duration.class))).thenReturn(true);

    AtomicInteger executions = new AtomicInteger();
    Response result =
        service.executeOnce(
            "payment-intent",
            "key-1",
            Duration.ofHours(1),
            Response.class,
            () -> {
              executions.incrementAndGet();
              return new Response("p1", "PENDING");
            });

    assertThat(executions.get()).isEqualTo(1);
    assertThat(result).isEqualTo(new Response("p1", "PENDING"));
    verify(valueOps).set(anyString(), anyString(), eq(Duration.ofHours(1)));
  }

  @Test
  void replayReturnsCachedResponseWithoutExecuting() {
    when(valueOps.get(anyString())).thenReturn("{\"paymentId\":\"p1\",\"status\":\"PAID\"}");

    AtomicInteger executions = new AtomicInteger();
    Response result =
        service.executeOnce(
            "payment-intent",
            "key-1",
            Duration.ofHours(1),
            Response.class,
            () -> {
              executions.incrementAndGet();
              return new Response("should-not-execute", "FAIL");
            });

    assertThat(executions.get()).isZero();
    assertThat(result).isEqualTo(new Response("p1", "PAID"));
    verify(valueOps, times(0)).set(anyString(), anyString(), any(Duration.class));
  }

  @Test
  void concurrentFirstCallCollidesWithInFlightPlaceholder() {
    when(valueOps.get(anyString())).thenReturn(null, "__in_flight__");
    when(valueOps.setIfAbsent(anyString(), anyString(), any(Duration.class))).thenReturn(false);

    assertThatThrownBy(
            () ->
                service.executeOnce(
                    "payment-intent",
                    "key-1",
                    Duration.ofHours(1),
                    Response.class,
                    () -> new Response("p1", "PAID")))
        .isInstanceOf(ApiException.class);
  }

  @Test
  void exceptionInActionClearsLockSoCallerCanRetry() {
    when(valueOps.get(anyString())).thenReturn(null);
    when(valueOps.setIfAbsent(anyString(), anyString(), any(Duration.class))).thenReturn(true);

    assertThatThrownBy(
            () ->
                service.executeOnce(
                    "payment-intent",
                    "key-1",
                    Duration.ofHours(1),
                    Response.class,
                    () -> {
                      throw new RuntimeException("downstream blew up");
                    }))
        .isInstanceOf(RuntimeException.class);

    verify(redis).delete(anyString());
  }
}
