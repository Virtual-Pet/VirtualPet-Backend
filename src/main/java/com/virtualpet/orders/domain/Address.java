package com.virtualpet.orders.domain;

import java.io.Serializable;

/**
 * Shipping address, persisted as JSONB on orders + checkout_sessions. Mirrors the OpenAPI Address
 * schema: addressLine + city + postalCode required, state + country optional.
 */
public record Address(
    String addressLine, String city, String state, String country, String postalCode)
    implements Serializable {}
