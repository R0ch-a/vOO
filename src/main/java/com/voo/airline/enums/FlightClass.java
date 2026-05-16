package com.voo.airline.enums;

import java.math.BigDecimal;

public enum FlightClass {

    ECONOMY("Economy", new BigDecimal("350.00")),
    PREMIUM_ECONOMY("Premium Economy", new BigDecimal("750.00")),
    EXECUTIVE("Executiva", new BigDecimal("1800.00"));

    private final String label;
    private final BigDecimal basePrice;

    FlightClass(String label, BigDecimal basePrice) {
        this.label = label;
        this.basePrice = basePrice;
    }

    public String getLabel()         { return label; }
    public BigDecimal getBasePrice() { return basePrice; }
}
