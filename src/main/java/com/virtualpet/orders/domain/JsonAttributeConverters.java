package com.virtualpet.orders.domain;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.json.JsonMapper;

/**
 * String-backed JSON converters used by orders entities. We keep the columns as plain text so the
 * mapping works identically on H2 (tests) and Postgres (prod); switching to JSONB later only
 * affects the column type, not the Java side.
 */
public final class JsonAttributeConverters {

  private static final ObjectMapper MAPPER = JsonMapper.builder().build();

  private JsonAttributeConverters() {}

  @Converter
  public static class AddressJsonConverter implements AttributeConverter<Address, String> {
    @Override
    public String convertToDatabaseColumn(Address attribute) {
      return attribute == null ? null : MAPPER.writeValueAsString(attribute);
    }

    @Override
    public Address convertToEntityAttribute(String dbData) {
      return dbData == null ? null : MAPPER.readValue(dbData, Address.class);
    }
  }
}
