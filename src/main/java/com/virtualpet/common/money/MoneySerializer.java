package com.virtualpet.common.money;

import java.math.BigDecimal;
import java.math.RoundingMode;
import tools.jackson.core.JacksonException;
import tools.jackson.core.JsonGenerator;
import tools.jackson.databind.SerializationContext;
import tools.jackson.databind.ser.std.StdSerializer;

/**
 * Serializes BigDecimal as a fixed-scale decimal string so JavaScript clients never lose precision.
 * The OpenAPI Money type is documented as "string decimal with fixed scale".
 */
public class MoneySerializer extends StdSerializer<BigDecimal> {

  public static final int SCALE = 2;

  public MoneySerializer() {
    super(BigDecimal.class);
  }

  @Override
  public void serialize(BigDecimal value, JsonGenerator gen, SerializationContext ctxt)
      throws JacksonException {
    BigDecimal normalized = value.setScale(SCALE, RoundingMode.HALF_UP);
    gen.writeString(normalized.toPlainString());
  }
}
