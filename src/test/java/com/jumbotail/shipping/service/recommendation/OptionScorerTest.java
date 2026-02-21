package com.jumbotail.shipping.service.recommendation;

import com.jumbotail.shipping.dto.recommendation.OptimizationPriority;
import com.jumbotail.shipping.dto.recommendation.RecommendationOption;
import org.junit.jupiter.api.Test;
import java.util.Arrays;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;

public class OptionScorerTest {

    private final OptionScorer scorer = new OptionScorer();

    @Test
    public void testScoringCostPriority() {
        RecommendationOption opt1 = RecommendationOption.builder()
                .estimatedCost(100.0)
                .estimatedDeliveryHours(10.0)
                .distanceKm(50.0)
                .build();
                
        RecommendationOption opt2 = RecommendationOption.builder()
                .estimatedCost(200.0)
                .estimatedDeliveryHours(5.0)
                .distanceKm(100.0)
                .build();

        List<RecommendationOption> options = Arrays.asList(opt1, opt2);
        scorer.scoreOptions(options, OptimizationPriority.COST);

        // Opt1 is cheaper (100 vs 200), shorter distance, but slower (if time mattered less).
        // COST priority: 0.6*cost + 0.2*dist + 0.2*time
        // Maxes: Cost=200, Time=10, Dist=100
        // Opt1 normalized: Cost=0.5, Time=1.0, Dist=0.5. Score = 0.6*0.5 + 0.2*0.5 + 0.2*1.0 = 0.3 + 0.1 + 0.2 = 0.6
        // Opt2 normalized: Cost=1.0, Time=0.5, Dist=1.0. Score = 0.6*1.0 + 0.2*1.0 + 0.2*0.5 = 0.6 + 0.2 + 0.1 = 0.9
        
        assertTrue(opt1.getScore() < opt2.getScore(), "Cheaper option should have a lower score in COST priority");
    }

    @Test
    public void testScoringSpeedPriority() {
        RecommendationOption opt1 = RecommendationOption.builder()
                .estimatedCost(100.0)
                .estimatedDeliveryHours(10.0)
                .distanceKm(50.0)
                .build();
                
        RecommendationOption opt2 = RecommendationOption.builder()
                .estimatedCost(500.0)
                .estimatedDeliveryHours(2.0)
                .distanceKm(50.0)
                .build();

        List<RecommendationOption> options = Arrays.asList(opt1, opt2);
        scorer.scoreOptions(options, OptimizationPriority.SPEED);

        // Opt2 is much faster (2h vs 10h) but much more expensive.
        // SPEED priority should favor opt2.
        assertTrue(opt2.getScore() < opt1.getScore(), "Faster option should have a lower score in SPEED priority");
    }
}
