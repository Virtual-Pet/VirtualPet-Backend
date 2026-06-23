package com.virtualpet;

import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.time.Duration;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import org.mockito.ArgumentMatchers;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Profile;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

/**
 * In-memory stand-in for Redis. Provides just enough behavior for cart, idempotency and
 * rate-limiting paths: ValueOperations#get/set with TTL ignored, setIfAbsent honored, and
 * StringRedisTemplate#delete.
 */
@TestConfiguration
@Profile("mock")
public class TestRedisConfiguration {

  @Bean
  @Primary
  public RedisConnectionFactory redisConnectionFactory() {
    return mock(RedisConnectionFactory.class);
  }

  @Bean
  @Primary
  @SuppressWarnings("unchecked")
  public StringRedisTemplate stringRedisTemplate() {
    ConcurrentHashMap<String, String> store = new ConcurrentHashMap<>();

    StringRedisTemplate template = mock(StringRedisTemplate.class, RETURNS_DEEP_STUBS);
    ValueOperations<String, String> ops = mock(ValueOperations.class);
    when(template.opsForValue()).thenReturn(ops);

    when(ops.get(ArgumentMatchers.anyString()))
        .thenAnswer(inv -> store.get(inv.<String>getArgument(0)));

    when(ops.increment(ArgumentMatchers.anyString()))
        .thenAnswer(
            inv -> {
              String key = inv.getArgument(0);
              long next = Long.parseLong(store.getOrDefault(key, "0")) + 1;
              store.put(key, Long.toString(next));
              return next;
            });

    when(ops.setIfAbsent(
            ArgumentMatchers.anyString(),
            ArgumentMatchers.anyString(),
            ArgumentMatchers.any(Duration.class)))
        .thenAnswer(
            inv -> {
              String key = inv.getArgument(0);
              String value = inv.getArgument(1);
              return store.putIfAbsent(key, value) == null;
            });

    doAnswer(
            inv -> {
              store.put(inv.getArgument(0), inv.getArgument(1));
              return null;
            })
        .when(ops)
        .set(
            ArgumentMatchers.anyString(),
            ArgumentMatchers.anyString(),
            ArgumentMatchers.any(Duration.class));

    when(template.delete(ArgumentMatchers.anyString()))
        .thenAnswer(inv -> store.remove(inv.<String>getArgument(0)) != null);

    when(template.expire(ArgumentMatchers.anyString(), ArgumentMatchers.any(Duration.class)))
        .thenReturn(true);
    when(template.getExpire(ArgumentMatchers.anyString(), ArgumentMatchers.any(TimeUnit.class)))
        .thenReturn(-1L);

    return template;
  }
}
