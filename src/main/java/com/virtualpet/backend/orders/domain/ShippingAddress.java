package com.virtualpet.backend.orders.domain;

import java.io.Serializable;

// Esta clase no es un @Entity, es solo un POJO para mapear el JSONB
public record ShippingAddress(
        String street,
        String number,
        String city,
        String zipCode,
        String additionalInfo
) implements Serializable {}
