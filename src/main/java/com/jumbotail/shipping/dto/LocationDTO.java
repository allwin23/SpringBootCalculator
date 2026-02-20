package com.jumbotail.shipping.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO for Location representation in API responses
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class LocationDTO {
    private Double lat;
    private Double lng;
}
