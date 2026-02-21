package com.jumbotail.shipping.dto.distance;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DistanceResponse {
    private Double distanceKm;
    private String calculationMode;
    private Integer durationMinutes;
}
