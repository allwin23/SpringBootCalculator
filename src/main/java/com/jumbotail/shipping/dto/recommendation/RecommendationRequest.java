package com.jumbotail.shipping.dto.recommendation;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class RecommendationRequest {
    @NotNull(message = "Order ID is mandatory")
    private Long orderId;

    @NotNull(message = "Priority is mandatory")
    private OptimizationPriority priority;
}
