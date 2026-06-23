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
      if (dbData == null || dbData.trim().isEmpty()) {
        return null;
      }
      String trimmed = dbData.trim();
      if (trimmed.startsWith("{") && trimmed.endsWith("}")) {
        try {
          return MAPPER.readValue(trimmed, Address.class);
        } catch (Exception e) {
          // Fall back to parse as flat string
        }
      }

      // Parse as flat string legacy data: e.g. "Av. Independencia 2345, Mar del Plata, BA, 7600"
      String[] parts = trimmed.split(",");
      String addressLine = parts.length > 0 ? parts[0].trim() : trimmed;
      String city = parts.length > 1 ? parts[1].trim() : "";
      String state = parts.length > 2 ? parts[2].trim() : "";
      String postalCode = parts.length > 3 ? parts[3].trim() : "";
      return new Address(addressLine, city, state, "Argentina", postalCode);
    }
  }
}
