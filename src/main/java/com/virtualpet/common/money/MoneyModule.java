package com.virtualpet.common.money;

import com.fasterxml.jackson.databind.module.SimpleModule;
import java.math.BigDecimal;

public class MoneyModule extends SimpleModule {

  public MoneyModule() {
    super("MoneyModule");
    addSerializer(BigDecimal.class, new MoneySerializer());
    addDeserializer(BigDecimal.class, new MoneyDeserializer());
  }
}
