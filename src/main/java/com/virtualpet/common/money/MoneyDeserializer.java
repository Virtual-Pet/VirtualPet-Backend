package com.virtualpet.common.money;

import java.math.BigDecimal;
import tools.jackson.core.JacksonException;
import tools.jackson.core.JsonParser;
import tools.jackson.core.JsonToken;
import tools.jackson.databind.DeserializationContext;
import tools.jackson.databind.deser.std.StdDeserializer;

/** Accepts BigDecimal as either a string or a JSON number. */
public class MoneyDeserializer extends StdDeserializer<BigDecimal> {

  public MoneyDeserializer() {
    super(BigDecimal.class);
  }

  @Override
  public BigDecimal deserialize(JsonParser p, DeserializationContext ctxt) throws JacksonException {
    JsonToken token = p.currentToken();
    if (token == JsonToken.VALUE_STRING) {
      String text = p.getString().trim();
      if (text.isEmpty()) {
        return null;
      }
      return new BigDecimal(text);
    }
    if (token == JsonToken.VALUE_NUMBER_INT || token == JsonToken.VALUE_NUMBER_FLOAT) {
      return p.getDecimalValue();
    }
    return (BigDecimal) ctxt.handleUnexpectedToken(BigDecimal.class, p);
  }
}
