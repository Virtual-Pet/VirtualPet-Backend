package com.virtualpet.orders.domain;

import java.io.Serializable;

/**
 * Shipping address. Persisted as JSON on {@code orders.shipping_address}; also embedded inside the
 * ephemeral CheckoutSession in Redis. Mirrors the OpenAPI Address schema: addressLine + city +
 * postalCode required, state + country optional.
 */
public record Address(
    String addressLine, String city, String state, String country, String postalCode)
    implements Serializable {}
