package com.jumbotail.shipping.strategy;

/**
 * Enum representing different delivery speeds
 */
public enum DeliverySpeed {
    STANDARD("standard", 10.0, 0.0), // Rs 10 standard charge, no extra per kg
    EXPRESS("express", 10.0, 1.2);   // Rs 10 standard charge + Rs 1.2 per kg extra
    
    private final String code;
    private final double standardCharge; // Standard courier charge in Rs
    private final double extraPerKg;    // Extra charge per kg in Rs
    
    DeliverySpeed(String code, double standardCharge, double extraPerKg) {
        this.code = code;
        this.standardCharge = standardCharge;
        this.extraPerKg = extraPerKg;
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
}
