package com.jumbotail.shipping.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Warehouse entity representing marketplace warehouses across the country
 */
@Entity
@Table(name = "warehouses")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Warehouse {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false, unique = true)
    private String warehouseId; // e.g., "789"
    
    @Column(nullable = false, unique = true)
    private String name; // e.g., "BLR_Warehouse"
    
    @Embedded
    private Location location;
    
    @Column(nullable = false)
    private Boolean active = true;
}
