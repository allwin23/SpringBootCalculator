package com.jumbotail.shipping.service;

import com.jumbotail.shipping.dto.distance.CalculationMode;
import com.jumbotail.shipping.dto.distance.DistanceResponse;
import com.jumbotail.shipping.strategy.distance.DistanceStrategy;
import com.jumbotail.shipping.strategy.distance.DistanceStrategyFactory;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class DistanceService {

    private final DistanceStrategyFactory strategyFactory;

    /**
     * Calculates the distance and duration using the specified mode strategy.
     * Caches the result to avoid redundant expensive API calls.
     */
    @Cacheable(value = "distanceCache")
    public DistanceResponse calculateDistance(double srcLat, double srcLng, double destLat, double destLng, CalculationMode mode) {
        
        log.info("Computing distance for [{},{}] to [{},{}], Mode: {}", srcLat, srcLng, destLat, destLng, mode);

        DistanceStrategy strategy = strategyFactory.getStrategy(mode);
        
        return strategy.calculate(srcLat, srcLng, destLat, destLng);
    }
}
