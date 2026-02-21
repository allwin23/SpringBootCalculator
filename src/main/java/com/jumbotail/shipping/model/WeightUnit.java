package com.jumbotail.shipping.model;

/**
 * Enum representing supported weight units.
 */
public enum WeightUnit {
    G("g"),
    KG("kg"),
    TON("ton");

    private final String symbol;

    WeightUnit(String symbol) {
        this.symbol = symbol;
    }

    public String getSymbol() {
        return symbol;
    }

    public static WeightUnit fromString(String unitStr) {
        if (unitStr == null) {
            return KG; // default
        }
        for (WeightUnit b : WeightUnit.values()) {
            if (b.symbol.equalsIgnoreCase(unitStr) || b.name().equalsIgnoreCase(unitStr)) {
                return b;
            }
        }
        throw new IllegalArgumentException("Invalid unit: " + unitStr);
    }
}
