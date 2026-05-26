package com.virtualpet.orders.domain;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.UUID;

/** Immutable line snapshot inside a {@link CheckoutSession}. */
public record SessionLineItem(UUID skuId, int quantity, BigDecimal unitPrice, BigDecimal subtotal)
    implements Serializable {}
