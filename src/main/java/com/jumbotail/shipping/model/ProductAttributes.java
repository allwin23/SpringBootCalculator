package com.jumbotail.shipping.model;

import jakarta.persistence.Embeddable;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Product attributes including weight and dimensions
 */
@Embeddable
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ProductAttributes {
    
    /**
     * Weight in kilograms
     */
    private Double weight; // in kg
    
    /**
     * Dimensions in centimeters
     * Format: length x width x height
     */
    private Double length; // in cm
    private Double width;  // in cm
    private Double height; // in cm
    
    /**
     * Calculate volume in cubic centimeters
     */
    public Double getVolume() {
        if (length == null || width == null || height == null) {
            return 0.0;
        }
        return length * width * height;
    }
}
