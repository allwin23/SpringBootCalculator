package com.jumbotail.shipping.service;

import com.jumbotail.shipping.dto.LogisticsSimulationRequest;
import com.jumbotail.shipping.dto.LogisticsSimulationResponse;
import com.jumbotail.shipping.dto.NearestWarehouseResponse;
import com.jumbotail.shipping.dto.TransportModeOption;
import com.jumbotail.shipping.exception.InvalidRequestException;
import com.jumbotail.shipping.exception.ResourceNotFoundException;
import com.jumbotail.shipping.model.Order;
import com.jumbotail.shipping.model.Warehouse;
import com.jumbotail.shipping.repository.OrderRepository;
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
public class LogisticsSimulationService {

    private final OrderRepository orderRepository;
    private final WarehouseService warehouseService;

    public LogisticsSimulationResponse simulateLogistics(LogisticsSimulationRequest request) {
        log.info("Simulating logistics for orderId: {} with priority: {}", request.getOrderId(), request.getPriority());

        // 1. Validate priority
        if (!"cost".equalsIgnoreCase(request.getPriority()) && !"speed".equalsIgnoreCase(request.getPriority())) {
            throw new InvalidRequestException("Priority must be 'cost' or 'speed'");
        }

        // 2. Fetch Order
        Order order = orderRepository.findByOrderId(request.getOrderId())
                .orElseThrow(() -> new ResourceNotFoundException("Order not found with ID: " + request.getOrderId()));

        if (order.getItems().isEmpty()) {
            throw new InvalidRequestException("Order " + request.getOrderId() + " has no items");
        }

        // 3. Find Nearest Warehouse
        NearestWarehouseResponse nearestWarehouse = warehouseService.findNearestWarehouse(
                order.getSeller().getSellerId(),
                order.getItems().get(0).getProduct().getProductId());

        Warehouse warehouse = warehouseService.getWarehouseByWarehouseId(nearestWarehouse.getWarehouseId());

        // 4. Calculate Distance
        double distance = DistanceCalculator.calculateDistance(warehouse.getLocation(), order.getCustomer().getLocation());
        double totalWeight = order.getTotalWeight();

        // 5. Generate Options for all Transport Modes
        List<TransportModeOption> options = new ArrayList<>();
        for (TransportMode mode : TransportMode.values()) {
            double baseCharge = mode.calculateCharge(distance, totalWeight);
            double estimatedTime = distance / mode.getAverageSpeed();

            options.add(TransportModeOption.builder()
                    .transportMode(mode.getName())
                    .baseChargeRs(Math.round(baseCharge * 100.0) / 100.0)
                    .estimatedTimeHours(Math.round(estimatedTime * 10.0) / 10.0)
                    .build());
        }

        // 6. Determine Recommended Option based on Priority
        TransportModeOption recommended = null;
        if ("cost".equalsIgnoreCase(request.getPriority())) {
            recommended = options.stream()
                    .min(Comparator.comparingDouble(TransportModeOption::getBaseChargeRs))
                    .orElseThrow();
        } else {
            recommended = options.stream()
                    .min(Comparator.comparingDouble(TransportModeOption::getEstimatedTimeHours))
                    .orElseThrow();
        }

        return LogisticsSimulationResponse.builder()
                .orderId(request.getOrderId())
                .priority(request.getPriority())
                .distanceKm(Math.round(distance * 100.0) / 100.0)
                .options(options)
                .recommendedOption(recommended)
                .build();
    }
}
