package com.virtualpet.common.money;

import java.math.BigDecimal;
import tools.jackson.databind.module.SimpleModule;

public class MoneyModule extends SimpleModule {

  public MoneyModule() {
    super("MoneyModule");
    addSerializer(BigDecimal.class, new MoneySerializer());
    addDeserializer(BigDecimal.class, new MoneyDeserializer());
  }
}
