package com.jumbotail.shipping.controller;

import com.jumbotail.shipping.dto.NearestWarehouseResponse;
import com.jumbotail.shipping.service.WarehouseService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import io.swagger.v3.oas.annotations.tags.Tag;

/**
 * REST Controller for warehouse-related operations
 */
@RestController
@RequestMapping("/api/v1/warehouse")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "2. Master Data Lookups", description = "Helper endpoints for looking up basic data")
public class WarehouseController {
    
    private final WarehouseService warehouseService;
    
    /**
     * Get the nearest warehouse for a seller based on a product
     * 
     * GET /api/v1/warehouse/nearest?sellerId=123&productId=456
     * 
     * @param sellerId Seller ID
     * @param productId Product ID
     * @return Nearest warehouse response
     */
    @GetMapping("/nearest")
    public ResponseEntity<NearestWarehouseResponse> getNearestWarehouse(
            @RequestParam String sellerId,
            @RequestParam String productId) {
        
        log.info("Received request for nearest warehouse - sellerId: {}, productId: {}", sellerId, productId);
        
        NearestWarehouseResponse response = warehouseService.findNearestWarehouse(sellerId, productId);
        
        return ResponseEntity.ok(response);
    }
}
