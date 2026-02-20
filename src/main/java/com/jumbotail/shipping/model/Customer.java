package com.jumbotail.shipping.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Customer entity representing Kirana stores
 */
@Entity
@Table(name = "customers")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Customer {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false, unique = true)
    private String customerId; // e.g., "Cust-123"
    
    @Column(nullable = false)
    private String name;
    
    @Column(nullable = false)
    private String phoneNumber;
    
    @Embedded
    private Location location;
    
    @Column(nullable = false)
    private Boolean active = true;
}
