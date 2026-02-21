package com.jumbotail.shipping.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LogisticsSimulationRequest {
    
    @NotBlank(message = "Order ID is required")
    private String orderId;

    @NotBlank(message = "Priority is required")
    @Pattern(regexp = "^(cost|speed)$", message = "Priority must be either 'cost' or 'speed'")
    private String priority;
}
