package com.virtualpet.orders.domain;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;
import java.util.List;
import tools.jackson.core.type.TypeReference;
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

  @Converter
  public static class SessionTotalsJsonConverter
      implements AttributeConverter<SessionTotals, String> {
    @Override
    public String convertToDatabaseColumn(SessionTotals attribute) {
      return attribute == null ? null : MAPPER.writeValueAsString(attribute);
    }

    @Override
    public SessionTotals convertToEntityAttribute(String dbData) {
      return dbData == null ? null : MAPPER.readValue(dbData, SessionTotals.class);
    }
  }

  @Converter
  public static class SessionLineItemsJsonConverter
      implements AttributeConverter<List<SessionLineItem>, String> {

    private static final TypeReference<List<SessionLineItem>> TYPE = new TypeReference<>() {};

    @Override
    public String convertToDatabaseColumn(List<SessionLineItem> attribute) {
      return attribute == null ? null : MAPPER.writeValueAsString(attribute);
    }

    @Override
    public List<SessionLineItem> convertToEntityAttribute(String dbData) {
      return dbData == null ? null : MAPPER.readValue(dbData, TYPE);
    }
  }
}
