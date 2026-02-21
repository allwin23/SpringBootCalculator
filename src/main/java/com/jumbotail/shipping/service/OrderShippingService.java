package com.jumbotail.shipping.service;

import com.jumbotail.shipping.dto.NearestWarehouseResponse;
import com.jumbotail.shipping.dto.OrderShippingEstimateResponse;
import com.jumbotail.shipping.exception.InvalidRequestException;
import com.jumbotail.shipping.exception.ResourceNotFoundException;
import com.jumbotail.shipping.model.Order;
import com.jumbotail.shipping.model.Warehouse;
import com.jumbotail.shipping.repository.OrderRepository;
import com.jumbotail.shipping.strategy.DeliverySpeed;
import com.jumbotail.shipping.strategy.TransportMode;
import com.jumbotail.shipping.util.DistanceCalculator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Service to bridge Order domain and Shipping calculation logic
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class OrderShippingService {
    
    private final OrderRepository orderRepository;
    private final WarehouseService warehouseService;
    private final ShippingMetricsService shippingMetricsService;
    
    /**
     * Calculate a rich shipping estimate for an existing order
     * 
     * @param orderId External order ID
     * @param speedCode Optional delivery speed code (defaults to STANDARD if null)
     * @return Shipping estimate with logistics intelligence
     */
    @Cacheable(value = "shippingEstimate", key = "#orderId + '_' + (#speedCode != null ? #speedCode : 'standard')")
    @Transactional(readOnly = true)
    public OrderShippingEstimateResponse getShippingEstimate(String orderId, String speedCode) {
        log.info("Calculating shipping estimate for order: {}, speed: {}", orderId, speedCode);
        long startTime = System.currentTimeMillis();
        boolean success = false;
        String finalTransportMode = null;
        
        try {
            // 1. Fetch Order
            Order order = orderRepository.findByOrderId(orderId)
                    .orElseThrow(() -> new ResourceNotFoundException("Order not found with ID: " + orderId));
            
            if (order.getItems().isEmpty()) {
                throw new InvalidRequestException("Order " + orderId + " has no items");
            }
            
            // 2. Validate Delivery Speed
            DeliverySpeed speed = (speedCode != null) ? DeliverySpeed.fromCode(speedCode) : DeliverySpeed.STANDARD;
            
            // 3. Find Nearest Warehouse to Seller
            // In this system, we pick the warehouse closest to the seller to fulfill the order.
            // We use one of the products to find the nearest warehouse (as implemented in WarehouseService).
            NearestWarehouseResponse nearestWarehouse = warehouseService.findNearestWarehouse(
                    order.getSeller().getSellerId(), 
                    order.getItems().get(0).getProduct().getProductId());
            
            Warehouse warehouse = warehouseService.getWarehouseByWarehouseId(nearestWarehouse.getWarehouseId());
            
            // 4. Calculate Distance (Warehouse to Customer)
            double distance = DistanceCalculator.calculateDistance(warehouse.getLocation(), order.getCustomer().getLocation());
            
            // 5. Determine Transport Mode and Shipping Charge
            TransportMode mode = TransportMode.getTransportMode(distance);
            finalTransportMode = mode.getName();
            double totalWeight = order.getTotalWeight();
            
            double baseCharge = mode.calculateCharge(distance, totalWeight);
            double speedCharge = speed.calculateAdditionalCharge(totalWeight);
            double totalCharge = Math.round((baseCharge + speedCharge) * 100.0) / 100.0;
            
            // 6. Estimate Delivery Time
            // Formula: handlingTime + (distance / speed * speedFactor)
            double transportHours = (distance / mode.getAverageSpeed()) * speed.getTimeFactor();
            double estimatedHours = Math.round((speed.getHandlingHours() + transportHours) * 10.0) / 10.0;
            
            log.info("Estimate for order {}: {} Rs, {} hours via {}", 
                     orderId, totalCharge, estimatedHours, mode.getName());
            
            success = true;
            return OrderShippingEstimateResponse.builder()
                    .orderId(orderId)
                    .totalWeight(totalWeight)
                    .transportMode(mode.getName())
                    .warehouseId(warehouse.getWarehouseId())
                    .distanceKm(Math.round(distance * 100.0) / 100.0)
                    .shippingCharge(totalCharge)
                    .deliverySpeed(speed.getCode())
                    .estimatedDeliveryHours(estimatedHours)
                    .build();
        } finally {
            shippingMetricsService.recordMetrics(System.currentTimeMillis() - startTime, finalTransportMode, success);
        }
    }
}
