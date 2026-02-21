package com.jumbotail.shipping.strategy.distance;

import com.jumbotail.shipping.dto.distance.CalculationMode;
import com.jumbotail.shipping.dto.distance.DistanceResponse;
import com.jumbotail.shipping.model.Location;
import com.jumbotail.shipping.util.DistanceCalculator;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Component("haversineDistanceStrategy")
@Slf4j
public class HaversineDistanceStrategy implements DistanceStrategy {

    // Assume average speed of 40 km/h for the crow-flies duration estimation
    private static final double AVERAGE_SPEED_KMH = 40.0;

    @Override
    public DistanceResponse calculate(double srcLat, double srcLng, double destLat, double destLng) {
        log.debug("Calculating distance using Haversine formula for [{},{}] to [{},{}]", srcLat, srcLng, destLat, destLng);
        
        Location source = new Location(srcLat, srcLng);
        Location dest = new Location(destLat, destLng);
        
        double distanceKm = DistanceCalculator.calculateDistance(source, dest);
        
        // Estimate duration based on distance and average speed
        int durationMinutes = (int) Math.round((distanceKm / AVERAGE_SPEED_KMH) * 60);

        return DistanceResponse.builder()
                .distanceKm(Math.round(distanceKm * 100.0) / 100.0)
                .durationMinutes(durationMinutes)
                .calculationMode(CalculationMode.HAVERSINE.name())
                .build();
    }
}
