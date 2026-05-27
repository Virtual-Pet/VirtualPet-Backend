package com.virtualpet.common.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import com.virtualpet.common.money.MoneyModule;
import tools.jackson.databind.JacksonModule;

@Configuration
public class JacksonConfig {

  @Bean
  public ObjectMapper objectMapper() {
      return new ObjectMapper();
  }
  @Bean
  public JacksonModule moneyModule() {
    return new MoneyModule();
  }
}
