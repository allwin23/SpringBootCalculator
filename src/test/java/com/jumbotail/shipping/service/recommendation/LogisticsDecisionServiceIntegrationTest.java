package com.jumbotail.shipping.service.recommendation;

import com.jumbotail.shipping.dto.recommendation.OptimizationPriority;
import com.jumbotail.shipping.dto.recommendation.RecommendationResponse;
import com.jumbotail.shipping.model.Order;
import com.jumbotail.shipping.repository.OrderRepository;
import com.jumbotail.shipping.strategy.TransportMode;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;
import lombok.extern.slf4j.Slf4j;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
@Slf4j
public class LogisticsDecisionServiceIntegrationTest {

    @Autowired
    private LogisticsDecisionService logisticsDecisionService;

    @Autowired
    private OrderRepository orderRepository;

    @Test
    public void testRecommendationFlow() {
        // DataInitializer seeds ORD-001 (internal id likely 1)
        // Let's find an order from the DB
        Order order = orderRepository.findAll().stream().findFirst().orElseThrow();
        Long internalId = order.getId();

        // 1. Test COST priority
        RecommendationResponse costResponse = logisticsDecisionService.recommendLogistics(internalId, OptimizationPriority.COST);
        assertNotNull(costResponse.getRecommendedOption());
        
        // 2. Test SPEED priority
        RecommendationResponse speedResponse = logisticsDecisionService.recommendLogistics(internalId, OptimizationPriority.SPEED);
        assertNotNull(speedResponse.getRecommendedOption());

        // In a properly seeded environment, SPEED priority often favors AEROPLANE if distance allows, 
        // while COST priority favors TRUCK or MINI_VAN.
        log.info("Recommended for COST: {} via {}", 
            costResponse.getRecommendedOption().getWarehouseId(), 
            costResponse.getRecommendedOption().getTransportMode());
            
        log.info("Recommended for SPEED: {} via {}", 
            speedResponse.getRecommendedOption().getWarehouseId(), 
            speedResponse.getRecommendedOption().getTransportMode());
    }
}
