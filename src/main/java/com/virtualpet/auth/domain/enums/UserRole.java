package com.virtualpet.auth.domain.enums;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

public enum UserRole {
  ROLE_CUSTOMER,
  ROLE_EMPLOYEE,
  ROLE_ADMIN,
  ROLE_RIDER;

  /** Spec-facing wire name without the Spring Security ROLE_ prefix. */
  @JsonValue
  public String wireName() {
    return name().substring("ROLE_".length());
  }

  @JsonCreator
  public static UserRole fromWire(String value) {
    String upper = value == null ? "" : value.trim().toUpperCase();
    return UserRole.valueOf(upper.startsWith("ROLE_") ? upper : "ROLE_" + upper);
  }
}
