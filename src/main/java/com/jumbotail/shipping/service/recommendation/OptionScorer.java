package com.jumbotail.shipping.service.recommendation;

import com.jumbotail.shipping.dto.recommendation.OptimizationPriority;
import com.jumbotail.shipping.dto.recommendation.RecommendationOption;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class OptionScorer {

    /**
     * Normalizes and scores given options based on priority.
     * Updates the `score` field inline for every option.
     * Lower score is better.
     *
     * @param options  the simulated options to score
     * @param priority the user's priority (COST, SPEED, BALANCED)
     */
    public void scoreOptions(List<RecommendationOption> options, OptimizationPriority priority) {
        if (options == null || options.isEmpty()) {
            return;
        }

        // 1. Find max values for normalization
        double maxCost = options.stream().mapToDouble(RecommendationOption::getEstimatedCost).max().orElse(1.0);
        double maxTime = options.stream().mapToDouble(RecommendationOption::getEstimatedDeliveryHours).max().orElse(1.0);
        double maxDistance = options.stream().mapToDouble(RecommendationOption::getDistanceKm).max().orElse(1.0);

        // Prevent division by zero
        maxCost = maxCost == 0 ? 1.0 : maxCost;
        maxTime = maxTime == 0 ? 1.0 : maxTime;
        maxDistance = maxDistance == 0 ? 1.0 : maxDistance;

        // 2. Determine Weights based on Priority
        double costWeight;
        double timeWeight;
        double distanceWeight;

        switch (priority) {
            case COST:
                costWeight = 0.6;
                timeWeight = 0.2;
                distanceWeight = 0.2;
                break;
            case SPEED:
                costWeight = 0.2;
                timeWeight = 0.6;
                distanceWeight = 0.2;
                break;
            case BALANCED:
            default:
                costWeight = 0.334;
                timeWeight = 0.333;
                distanceWeight = 0.333;
                break;
        }

        // 3. Normalize and Score
        for (RecommendationOption option : options) {
            double normalizedCost = option.getEstimatedCost() / maxCost;
            double normalizedTime = option.getEstimatedDeliveryHours() / maxTime;
            double normalizedDistance = option.getDistanceKm() / maxDistance;

            double score = (costWeight * normalizedCost) +
                           (timeWeight * normalizedTime) +
                           (distanceWeight * normalizedDistance);

            option.setScore(score);
        }
    }
}
