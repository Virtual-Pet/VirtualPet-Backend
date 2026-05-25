package com.virtualpet.orders.domain;

import java.io.Serializable;
import java.math.BigDecimal;

/** Totals snapshot stored on checkout_sessions.totals (JSONB). */
public record SessionTotals(BigDecimal items, BigDecimal shipping, BigDecimal grandTotal)
    implements Serializable {}
