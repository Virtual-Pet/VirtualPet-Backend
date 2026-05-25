package com.virtualpet.common.money;

import static org.assertj.core.api.Assertions.assertThat;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.math.BigDecimal;
import org.junit.jupiter.api.Test;

class MoneyModuleTest {

  private final ObjectMapper mapper = new ObjectMapper().registerModule(new MoneyModule());

  record Amount(BigDecimal value) {}

  @Test
  void serializesBigDecimalAsFixedScaleString() throws Exception {
    String json = mapper.writeValueAsString(new Amount(new BigDecimal("1500")));
    assertThat(json).isEqualTo("{\"value\":\"1500.00\"}");
  }

  @Test
  void roundsToTwoDecimalsHalfUp() throws Exception {
    String json = mapper.writeValueAsString(new Amount(new BigDecimal("1500.125")));
    assertThat(json).isEqualTo("{\"value\":\"1500.13\"}");
  }

  @Test
  void deserializesStringAsBigDecimal() throws Exception {
    Amount amount = mapper.readValue("{\"value\":\"99.99\"}", Amount.class);
    assertThat(amount.value()).isEqualByComparingTo("99.99");
  }

  @Test
  void deserializesNumericAsBigDecimal() throws Exception {
    Amount amount = mapper.readValue("{\"value\":42.5}", Amount.class);
    assertThat(amount.value()).isEqualByComparingTo("42.5");
  }
}
