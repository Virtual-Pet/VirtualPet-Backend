package com.virtualpet.common.money;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import java.io.IOException;
import java.math.BigDecimal;

/** Accepts BigDecimal as either a string or a JSON number. */
public class MoneyDeserializer extends StdDeserializer<BigDecimal> {

  public MoneyDeserializer() {
    super(BigDecimal.class);
  }

  @Override
  public BigDecimal deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {
    JsonToken token = p.currentToken();
    if (token == JsonToken.VALUE_STRING) {
      String text = p.getText().trim();
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
