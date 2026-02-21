package com.jumbotail.shipping.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LogisticsSimulationResponse {
    private String orderId;
    private String priority;
    private double distanceKm;
    private List<TransportModeOption> options;
    private TransportModeOption recommendedOption;
}
