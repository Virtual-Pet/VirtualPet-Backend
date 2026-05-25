package com.virtualpet.common.config;

import com.fasterxml.jackson.databind.Module;
import com.virtualpet.common.money.MoneyModule;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class JacksonConfig {

  @Bean
  public Module moneyModule() {
    return new MoneyModule();
  }
}
