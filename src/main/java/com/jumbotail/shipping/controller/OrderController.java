package com.jumbotail.shipping.controller;

import com.jumbotail.shipping.dto.OrderRequest;
import com.jumbotail.shipping.dto.OrderResponse;
import com.jumbotail.shipping.dto.OrderShippingEstimateResponse;
import com.jumbotail.shipping.service.OrderService;
import com.jumbotail.shipping.service.OrderShippingService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import io.swagger.v3.oas.annotations.tags.Tag;

/**
 * REST Controller for order-related operations
 */
@RestController
@RequestMapping("/api/v1/orders")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "1. Order Management", description = "Endpoints for creating and retrieving orders and shipping estimates")
public class OrderController {
    
    private final OrderService orderService;
    private final OrderShippingService orderShippingService;
    
    /**
     * Create a new order
     * 
     * POST /api/v1/orders
     * 
     * @param request Order creation request
     * @return Created order response
     */
    @PostMapping
    public ResponseEntity<OrderResponse> createOrder(@Valid @RequestBody OrderRequest request) {
        log.info("Received request to create order for sellerId: {}, customerId: {}", 
                 request.getSellerId(), request.getCustomerId());
        
        OrderResponse response = orderService.createOrder(request);
        
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
    
    /**
     * Get order details by orderId
     * 
     * GET /api/v1/orders/{orderId}
     * 
     * @param orderId Order ID
     * @return Order response
     */
    @GetMapping("/{orderId}")
    public ResponseEntity<OrderResponse> getOrder(@PathVariable String orderId) {
        log.info("Received request to fetch order: {}", orderId);
        
        OrderResponse response = orderService.getOrderByOrderId(orderId);
        
        return ResponseEntity.ok(response);
    }

    /**
     * Get shipping estimate for an order
     * 
     * GET /api/v1/orders/{orderId}/shipping?deliverySpeed=express
     * 
     * @param orderId External Order ID
     * @param deliverySpeed Optional delivery speed (standard/express)
     * @return Rich shipping estimate
     */
    @GetMapping("/{orderId}/shipping")
    public ResponseEntity<OrderShippingEstimateResponse> getShippingEstimate(
            @PathVariable String orderId,
            @RequestParam(required = false) String deliverySpeed) {
        
        log.info("Received request for shipping estimate - orderId: {}, deliverySpeed: {}", 
                 orderId, deliverySpeed);
        
        OrderShippingEstimateResponse response = orderShippingService.getShippingEstimate(orderId, deliverySpeed);
        
        return ResponseEntity.ok(response);
    }
}
