package com.jumbotail.shipping.model;

import jakarta.persistence.Embeddable;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Location entity representing geographic coordinates
 */
@Embeddable
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Location {
    
    private Double lat; // Latitude
    private Double lng; // Longitude
}
