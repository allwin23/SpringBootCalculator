package com.jumbotail.shipping.strategy.distance;

import com.jumbotail.shipping.dto.distance.CalculationMode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
@RequiredArgsConstructor
public class DistanceStrategyFactory {

    private final Map<String, DistanceStrategy> strategies;

    public DistanceStrategy getStrategy(CalculationMode mode) {
        switch (mode) {
            case GOOGLE:
                return strategies.get("googleDistanceStrategy");
            case HAVERSINE:
            default:
                return strategies.get("haversineDistanceStrategy");
        }
    }
}
