package com.virtualpet.common.money;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;
import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;

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
  public void serialize(BigDecimal value, JsonGenerator gen, SerializerProvider provider)
      throws IOException {
    BigDecimal normalized = value.setScale(SCALE, RoundingMode.HALF_UP);
    gen.writeString(normalized.toPlainString());
  }
}
