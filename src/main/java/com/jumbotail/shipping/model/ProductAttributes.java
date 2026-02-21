package com.jumbotail.shipping.model;

import jakarta.persistence.Column;
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
    @Column(name="product_weight")
    private Double weight; // in kg
    
    /**
     * Dimensions in centimeters
     * Format: length x width x height
     */
    @Column(name="product_length")
    private Double length; // in cm
    @Column(name="product_width")
    private Double width;  // in cm
    @Column(name="product_height")
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
