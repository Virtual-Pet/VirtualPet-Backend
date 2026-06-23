package com.virtualpet.shipments.sse;

import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ShipmentSseConfig {

  @Bean
  public Executor shipmentSseExecutor() {
    return Executors.newVirtualThreadPerTaskExecutor();
  }
}
