package com.virtualpet.chatbot.config;

import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ChatConfig {

  /**
   * Virtual threads (Java 21+) para el chatbot: cada request de chat bloquea en I/O mientras
   * espera la respuesta de Gemini. Con virtual threads esto es barato y no consume threads del
   * pool de Tomcat.
   */
  @Bean
  public Executor chatExecutor() {
    return Executors.newVirtualThreadPerTaskExecutor();
  }
}
