package com.jumbotail.shipping.service.recommendation;

import com.jumbotail.shipping.strategy.TransportMode;
import org.springframework.stereotype.Component;

@Component
public class DeliveryTimeEstimator {

    // Speeds in km/h
    private static final double MINIVAN_SPEED = 40.0;
    private static final double TRUCK_SPEED = 60.0;
    private static final double AIRPLANE_SPEED = 500.0;

    // Processing time in hours
    private static final double WAREHOUSE_PROCESSING_TIME = 2.0;

    /**
     * Estimates delivery time in hours based on distance and transport mode.
     * Includes a constant warehouse processing time.
     *
     * @param distanceKm    the distance to cover
     * @param transportMode the mode of transport
     * @return estimated hours
     */
    public double estimateDeliveryHours(double distanceKm, TransportMode transportMode) {
        double speed = switch (transportMode) {
            case MINI_VAN -> MINIVAN_SPEED;
            case TRUCK -> TRUCK_SPEED;
            case AEROPLANE -> AIRPLANE_SPEED;
            default -> TRUCK_SPEED; // Default fallback
        };

        return (distanceKm / speed) + WAREHOUSE_PROCESSING_TIME;
    }
}
