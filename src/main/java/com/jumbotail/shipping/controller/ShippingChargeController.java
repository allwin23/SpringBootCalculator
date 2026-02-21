package com.jumbotail.shipping.controller;

import com.jumbotail.shipping.dto.ShippingChargeRequest;
import com.jumbotail.shipping.dto.ShippingChargeResponse;
import com.jumbotail.shipping.service.ShippingChargeService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import io.swagger.v3.oas.annotations.tags.Tag;

import java.util.HashMap;
import java.util.Map;

/**
 * REST Controller for shipping charge calculations
 */
@RestController
@RequestMapping("/api/v1/shipping-charge")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "4. Shipping Charge Engine", description = "Endpoints for calculating direct manual shipping quotes")
public class ShippingChargeController {
    
    private final ShippingChargeService shippingChargeService;
    
    /**
     * Get the shipping charge for a customer from a warehouse
     * 
     * GET /api/v1/shipping-charge?warehouseId=789&customerId=456&deliverySpeed=standard
     * 
     * @param warehouseId Warehouse ID
     * @param customerId Customer ID
     * @param deliverySpeed Delivery speed ("standard" or "express")
     * @param productId Optional product ID for accurate weight calculation
     * @return Shipping charge response
     */
    @GetMapping
    public ResponseEntity<Map<String, Double>> getShippingCharge(
            @RequestParam String warehouseId,
            @RequestParam String customerId,
            @RequestParam String deliverySpeed,
            @RequestParam(required = false) String productId) {
        
        log.info("Received request for shipping charge - warehouseId: {}, customerId: {}, deliverySpeed: {}, productId: {}", 
                 warehouseId, customerId, deliverySpeed, productId);
        
        Double shippingCharge = shippingChargeService.calculateShippingCharge(
                warehouseId, customerId, deliverySpeed, productId);
        
        Map<String, Double> response = new HashMap<>();
        response.put("shippingCharge", shippingCharge);
        
        return ResponseEntity.ok(response);
    }
    
    /**
     * Calculate shipping charges for a seller and customer
     * This combines nearest warehouse retrieval and shipping charge calculation
     * 
     * POST /api/v1/shipping-charge/calculate
     * 
     * Request Body:
     * {
     *   "sellerId": "123",
     *   "customerId": "456",
     *   "deliverySpeed": "express"
     * }
     * 
     * @param request Shipping charge request
     * @return Shipping charge response with charge and nearest warehouse
     */
    @PostMapping("/calculate")
    public ResponseEntity<ShippingChargeResponse> calculateShippingCharge(
            @Valid @RequestBody ShippingChargeRequest request) {
        
        log.info("Received request to calculate shipping charge - sellerId: {}, customerId: {}, deliverySpeed: {}", 
                 request.getSellerId(), request.getCustomerId(), request.getDeliverySpeed());
        
        ShippingChargeResponse response = shippingChargeService.calculateShippingChargeForSellerAndCustomer(request);
        
        return ResponseEntity.ok(response);
    }
}
