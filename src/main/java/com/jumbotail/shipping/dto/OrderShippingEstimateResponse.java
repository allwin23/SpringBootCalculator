package com.jumbotail.shipping.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Rich response DTO for order shipping estimate
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrderShippingEstimateResponse {
    
    private String orderId;
    private Double totalWeight;
    private String transportMode;
    private String warehouseId;
    private Double distanceKm;
    private Double shippingCharge;
    private String deliverySpeed;
    private Double estimatedDeliveryHours;
}
