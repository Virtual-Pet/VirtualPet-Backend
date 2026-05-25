package com.virtualpet.common.config;

import com.virtualpet.common.money.MoneyModule;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import tools.jackson.databind.JacksonModule;

@Configuration
public class JacksonConfig {

  @Bean
  public JacksonModule moneyModule() {
    return new MoneyModule();
  }
}
