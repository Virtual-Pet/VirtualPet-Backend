package com.virtualpet;

import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.mock;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.virtualpet.common.money.MoneyModule;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Profile;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.StringRedisTemplate;

/**
 * Provides stub infrastructure beans so the application context loads when no Redis is reachable.
 * The StringRedisTemplate uses deep stubs so chained calls (opsForValue().increment(...)) return
 * mocks instead of NPEs; rate limit and idempotency code paths gracefully treat null returns as "no
 * prior state".
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
  public StringRedisTemplate stringRedisTemplate() {
    return mock(StringRedisTemplate.class, RETURNS_DEEP_STUBS);
  }

  @Bean
  @Primary
  public ObjectMapper objectMapper() {
    return new ObjectMapper().registerModule(new MoneyModule());
  }
}
