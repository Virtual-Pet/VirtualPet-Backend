package com.virtualpet.orders.domain;

import java.io.Serializable;
import java.math.BigDecimal;

/** Totals snapshot inside a {@link CheckoutSession}. */
public record SessionTotals(BigDecimal items, BigDecimal shipping, BigDecimal grandTotal)
    implements Serializable {}
