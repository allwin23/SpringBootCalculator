package com.jumbotail.shipping.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TransportModeOption {
    private String transportMode;
    private double baseChargeRs;
    private double estimatedTimeHours;
}
