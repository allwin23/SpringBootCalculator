package com.jumbotail.shipping.service.recommendation;

import com.jumbotail.shipping.strategy.TransportMode;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class DeliveryTimeEstimatorTest {

    private final DeliveryTimeEstimator estimator = new DeliveryTimeEstimator();

    @Test
    public void testEstimatedDeliveryHours() {
        double distance = 100.0;
        
        // MiniVan @ 40 km/h + 2h processing = 2.5 + 2 = 4.5
        double minivanTime = estimator.estimateDeliveryHours(distance, TransportMode.MINI_VAN);
        assertEquals(4.5, minivanTime, 0.01);
        
        // Truck @ 60 km/h + 2h processing = 1.666 + 2 = 3.666
        double truckTime = estimator.estimateDeliveryHours(distance, TransportMode.TRUCK);
        assertEquals(3.66, truckTime, 0.1);
        
        // Aeroplane @ 500 km/h + 2h processing = 0.2 + 2 = 2.2
        double aeroTime = estimator.estimateDeliveryHours(distance, TransportMode.AEROPLANE);
        assertEquals(2.2, aeroTime, 0.01);
    }
}
