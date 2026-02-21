package com.jumbotail.shipping.strategy;

/**
 * Enum representing different delivery speeds
 */
public enum DeliverySpeed {
    STANDARD("standard", 10.0, 0.0, 24.0, 1.2), // 24h handling, 1.2x time factor
    EXPRESS("express", 10.0, 1.2, 4.0, 0.8);    // 4h handling, 0.8x time factor
    
    private final String code;
    private final double standardCharge; // Standard courier charge in Rs
    private final double extraPerKg;    // Extra charge per kg in Rs
    private final double handlingHours;  // Fixed handling time in hours
    private final double timeFactor;    // Multiplier for transport time
    
    DeliverySpeed(String code, double standardCharge, double extraPerKg, double handlingHours, double timeFactor) {
        this.code = code;
        this.standardCharge = standardCharge;
        this.extraPerKg = extraPerKg;
        this.handlingHours = handlingHours;
        this.timeFactor = timeFactor;
    }
    
    /**
     * Get DeliverySpeed from string code
     * 
     * @param code Delivery speed code ("standard" or "express")
     * @return DeliverySpeed enum
     * @throws IllegalArgumentException if code is invalid
     */
    public static DeliverySpeed fromCode(String code) {
        if (code == null) {
            throw new IllegalArgumentException("Delivery speed code cannot be null");
        }
        
        String normalizedCode = code.toLowerCase().trim();
        for (DeliverySpeed speed : values()) {
            if (speed.code.equals(normalizedCode)) {
                return speed;
            }
        }
        
        throw new IllegalArgumentException("Invalid delivery speed: " + code + ". Must be 'standard' or 'express'");
    }
    
    /**
     * Calculate additional charge based on weight
     * 
     * @param weight Weight in kilograms
     * @return Additional charge in rupees
     */
    public double calculateAdditionalCharge(double weight) {
        return standardCharge + (extraPerKg * weight);
    }
    
    public String getCode() {
        return code;
    }
    
    public double getStandardCharge() {
        return standardCharge;
    }
    
    public double getExtraPerKg() {
        return extraPerKg;
    }

    public double getHandlingHours() {
        return handlingHours;
    }

    public double getTimeFactor() {
        return timeFactor;
    }
}
