package com.virtualpet.orders.domain;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.UUID;

/** Immutable line snapshot stored on checkout_sessions.line_items (JSONB). */
public record SessionLineItem(UUID skuId, int quantity, BigDecimal unitPrice, BigDecimal subtotal)
    implements Serializable {}
