package com.jumbotail.shipping.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Seller entity representing product sellers in the marketplace
 */
@Entity
@Table(name = "sellers")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Seller {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false, unique = true)
    private String sellerId; // e.g., "123"
    
    @Column(nullable = false)
    private String name;
    
    @Embedded
    private Location location;
    
    @Column(nullable = false)
    private Boolean active = true;
}
