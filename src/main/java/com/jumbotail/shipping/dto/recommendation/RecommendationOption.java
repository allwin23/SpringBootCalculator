package com.jumbotail.shipping.dto.recommendation;

import com.jumbotail.shipping.strategy.TransportMode;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RecommendationOption {
    private Long warehouseId;
    private TransportMode transportMode;
    private Double estimatedCost;
    private Double estimatedDeliveryHours;
    private Double distanceKm;
    
    // Internal usage for ranking, might not be exposed to client depending on DTO mapping
    private Double score;
}
