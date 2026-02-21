package com.jumbotail.shipping.service.recommendation;

import com.jumbotail.shipping.dto.recommendation.OptimizationPriority;
import com.jumbotail.shipping.dto.recommendation.RecommendationOption;
import com.jumbotail.shipping.dto.recommendation.RecommendationResponse;
import com.jumbotail.shipping.exception.InvalidRequestException;
import com.jumbotail.shipping.exception.ResourceNotFoundException;
import com.jumbotail.shipping.model.Order;
import com.jumbotail.shipping.model.Warehouse;
import com.jumbotail.shipping.repository.OrderRepository;
import com.jumbotail.shipping.repository.WarehouseRepository;
import com.jumbotail.shipping.service.InventoryService;
import com.jumbotail.shipping.strategy.TransportMode;
import com.jumbotail.shipping.util.DistanceCalculator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class LogisticsDecisionService {

    private final OrderRepository orderRepository;
    private final WarehouseRepository warehouseRepository;
    private final InventoryService inventoryService;
    private final DeliveryTimeEstimator deliveryTimeEstimator;
    private final OptionScorer optionScorer;

    public RecommendationResponse recommendLogistics(Long orderEntityId, OptimizationPriority priority) {
        log.info("Generating logistics recommendation for Order ID: {}, Priority: {}", orderEntityId, priority);

        // 1. Fetch Order and Basic Validate
        Order order = orderRepository.findById(orderEntityId)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found with internal ID: " + orderEntityId));

        if (order.getItems() == null || order.getItems().isEmpty()) {
            throw new InvalidRequestException("Order " + orderEntityId + " has no items");
        }

        double totalWeight = order.getTotalWeight();

        // 2. Fetch all active warehouses
        List<Warehouse> activeWarehouses = warehouseRepository.findByActiveTrue();
        if (activeWarehouses.isEmpty()) {
            throw new InvalidRequestException("No active warehouses available for fulfillment");
        }

        List<RecommendationOption> generatedOptions = new ArrayList<>();

        // 3. The Core Simulation Loop
        for (Warehouse warehouse : activeWarehouses) {
            
            // Step 3a: Filter by Inventory 
            boolean hasStock = inventoryService.hasSufficientStockForOrder(warehouse.getId(), order);
            if (!hasStock) {
                log.debug("Warehouse {} excluded: Insufficient stock for order {}", warehouse.getWarehouseId(), orderEntityId);
                continue; 
            }

            // Step 3b: Calculate Distance
            double distance = DistanceCalculator.calculateDistance(warehouse.getLocation(), order.getCustomer().getLocation());

            // Step 3c: Simulate all transport modes for this warehouse
            for (TransportMode mode : TransportMode.values()) {
                
                // Real-world logic: Transport mode has min/max distances
                if (distance < mode.getMinDistance() || distance > mode.getMaxDistance()) {
                    continue; 
                }

                // Simulate Cost
                double baseCharge = mode.calculateCharge(distance, totalWeight);
                double estimatedCost = Math.round(baseCharge * 100.0) / 100.0;

                // Simulate Time
                double estimatedHours = Math.round(deliveryTimeEstimator.estimateDeliveryHours(distance, mode) * 10.0) / 10.0;

                RecommendationOption option = RecommendationOption.builder()
                        .warehouseId(warehouse.getId())
                        .transportMode(mode)
                        .estimatedCost(estimatedCost)
                        .estimatedDeliveryHours(estimatedHours)
                        .distanceKm(Math.round(distance * 100.0) / 100.0)
                        .build();

                generatedOptions.add(option);
            }
        }

        if (generatedOptions.isEmpty()) {
            throw new InvalidRequestException("No valid logistics options found for Order " + orderEntityId + ". Check inventory or distances blockages.");
        }

        // 4. Score the options
        optionScorer.scoreOptions(generatedOptions, priority);

        // 5. Rank the options (lowest score is best)
        generatedOptions.sort(Comparator.comparingDouble(RecommendationOption::getScore));

        // 6. Build the Response
        RecommendationOption bestMatch = generatedOptions.get(0);
        List<RecommendationOption> alternatives = generatedOptions.subList(1, generatedOptions.size());

        return RecommendationResponse.builder()
                .recommendedOption(bestMatch)
                .alternatives(alternatives)
                .build();
    }
}
