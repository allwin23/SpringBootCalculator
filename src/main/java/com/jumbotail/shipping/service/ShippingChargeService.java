package com.jumbotail.shipping.service;

import com.jumbotail.shipping.dto.LocationDTO;
import com.jumbotail.shipping.dto.NearestWarehouseResponse;
import com.jumbotail.shipping.dto.ShippingChargeRequest;
import com.jumbotail.shipping.dto.ShippingChargeResponse;
import com.jumbotail.shipping.exception.InvalidRequestException;
import com.jumbotail.shipping.exception.ResourceNotFoundException;
import com.jumbotail.shipping.model.Customer;
import com.jumbotail.shipping.model.Location;
import com.jumbotail.shipping.model.Product;
import com.jumbotail.shipping.model.Seller;
import com.jumbotail.shipping.model.Warehouse;
import com.jumbotail.shipping.repository.CustomerRepository;
import com.jumbotail.shipping.repository.ProductRepository;
import com.jumbotail.shipping.repository.SellerRepository;
import com.jumbotail.shipping.strategy.DeliverySpeed;
import com.jumbotail.shipping.strategy.TransportMode;
import com.jumbotail.shipping.util.DistanceCalculator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Service for calculating shipping charges
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class ShippingChargeService {
    
    private final WarehouseService warehouseService;
    private final CustomerRepository customerRepository;
    private final SellerRepository sellerRepository;
    private final ProductRepository productRepository;
    
    /**
     * Calculate shipping charge from warehouse to customer
     * Uses caching to improve performance
     * Note: This method requires productId to calculate accurate shipping charge based on weight.
     * If productId is not provided, a default weight of 1kg is used.
     * 
     * @param warehouseId Warehouse ID
     * @param customerId Customer ID
     * @param deliverySpeed Delivery speed ("standard" or "express")
     * @param productId Optional product ID for accurate weight calculation
     * @return Shipping charge
     */
    @Cacheable(value = "shippingCharge", key = "#warehouseId + '_' + #customerId + '_' + #deliverySpeed + '_' + (#productId != null ? #productId : 'default')")
    @Transactional(readOnly = true)
    public Double calculateShippingCharge(String warehouseId, String customerId, String deliverySpeed, String productId) {
        log.info("Calculating shipping charge for warehouseId: {}, customerId: {}, deliverySpeed: {}, productId: {}", 
                 warehouseId, customerId, deliverySpeed, productId);
        
        // Validate delivery speed
        DeliverySpeed speed;
        try {
            speed = DeliverySpeed.fromCode(deliverySpeed);
        } catch (IllegalArgumentException e) {
            throw new InvalidRequestException("Invalid delivery speed: " + deliverySpeed + ". Must be 'standard' or 'express'");
        }
        
        // Get warehouse
        Warehouse warehouse = warehouseService.getWarehouseByWarehouseId(warehouseId);
        
        if (warehouse.getLocation() == null || warehouse.getLocation().getLat() == null ||
            warehouse.getLocation().getLng() == null) {
            throw new ResourceNotFoundException("Warehouse location not available for warehouseId: " + warehouseId);
        }
        
        // Get customer
        Customer customer = customerRepository.findByCustomerIdAndActiveTrue(customerId)
                .orElseThrow(() -> new ResourceNotFoundException("Customer not found with ID: " + customerId));
        
        if (customer.getLocation() == null || customer.getLocation().getLat() == null ||
            customer.getLocation().getLng() == null) {
            throw new ResourceNotFoundException("Customer location not available for customerId: " + customerId);
        }
        
        // Calculate distance
        double distance = DistanceCalculator.calculateDistance(warehouse.getLocation(), customer.getLocation());
        
        // Get product weight if productId is provided
        double weight = 1.0; // Default weight in kg
        if (productId != null && !productId.trim().isEmpty()) {
            Product product = productRepository.findByProductIdAndActiveTrue(productId)
                    .orElseThrow(() -> new ResourceNotFoundException("Product not found with ID: " + productId));
            
            if (product.getAttributes() != null && product.getAttributes().getWeight() != null) {
                weight = product.getAttributes().getWeight();
            } else {
                log.warn("Product {} has no weight information, using default weight", productId);
            }
        } else {
            log.warn("ProductId not provided, using default weight {} kg for shipping charge calculation", weight);
        }
        
        return calculateShippingChargeInternal(distance, weight, speed);
    }
    
    /**
     * Calculate shipping charge for seller and customer (combined API)
     * 
     * @param request Shipping charge request containing sellerId, customerId, and deliverySpeed
     * @return Shipping charge response with charge and nearest warehouse
     */
    @Cacheable(value = "shippingCharge", key = "#request.sellerId + '_' + #request.customerId + '_' + #request.deliverySpeed")
    @Transactional(readOnly = true)
    public ShippingChargeResponse calculateShippingChargeForSellerAndCustomer(ShippingChargeRequest request) {
        log.info("Calculating shipping charge for sellerId: {}, customerId: {}, deliverySpeed: {}", 
                 request.getSellerId(), request.getCustomerId(), request.getDeliverySpeed());
        
        // Validate delivery speed
        DeliverySpeed speed;
        try {
            speed = DeliverySpeed.fromCode(request.getDeliverySpeed());
        } catch (IllegalArgumentException e) {
            throw new InvalidRequestException("Invalid delivery speed: " + request.getDeliverySpeed() + 
                                             ". Must be 'standard' or 'express'");
        }
        
        // Get seller
        Seller seller = sellerRepository.findBySellerIdAndActiveTrue(request.getSellerId())
                .orElseThrow(() -> new ResourceNotFoundException("Seller not found with ID: " + request.getSellerId()));
        
        if (seller.getLocation() == null || seller.getLocation().getLat() == null ||
            seller.getLocation().getLng() == null) {
            throw new ResourceNotFoundException("Seller location not available for sellerId: " + request.getSellerId());
        }
        
        // Get customer
        Customer customer = customerRepository.findByCustomerIdAndActiveTrue(request.getCustomerId())
                .orElseThrow(() -> new ResourceNotFoundException("Customer not found with ID: " + request.getCustomerId()));
        
        if (customer.getLocation() == null || customer.getLocation().getLat() == null ||
            customer.getLocation().getLng() == null) {
            throw new ResourceNotFoundException("Customer location not available for customerId: " + request.getCustomerId());
        }
        
        // Find nearest warehouse to seller
        // We need productId for this, but it's not in the request. Let's get seller's first product
        List<Product> sellerProducts = productRepository.findBySellerAndActiveTrue(seller);
        
        if (sellerProducts.isEmpty()) {
            throw new ResourceNotFoundException("No products found for sellerId: " + request.getSellerId());
        }
        
        // Use first product to find nearest warehouse
        Product product = sellerProducts.get(0);
        NearestWarehouseResponse nearestWarehouse = warehouseService.findNearestWarehouse(
                request.getSellerId(), product.getProductId());
        
        // Get warehouse entity
        Warehouse warehouse = warehouseService.getWarehouseByWarehouseId(nearestWarehouse.getWarehouseId());
        
        // Calculate distance from warehouse to customer
        double distance = DistanceCalculator.calculateDistance(warehouse.getLocation(), customer.getLocation());
        
        // Get product weight
        double weight = product.getAttributes() != null && product.getAttributes().getWeight() != null
                ? product.getAttributes().getWeight()
                : 1.0; // Default weight if not available
        
        // Calculate shipping charge
        Double shippingCharge = calculateShippingChargeInternal(distance, weight, speed);
        
        log.info("Shipping charge calculated: {} Rs for distance: {} km, weight: {} kg", 
                 shippingCharge, distance, weight);
        
        return ShippingChargeResponse.builder()
                .shippingCharge(shippingCharge)
                .nearestWarehouse(nearestWarehouse)
                .build();
    }
    
    /**
     * Internal method to calculate shipping charge based on distance, weight, and delivery speed
     * 
     * @param distance Distance in kilometers
     * @param weight Weight in kilograms
     * @param speed Delivery speed
     * @return Total shipping charge in rupees
     */
    private Double calculateShippingChargeInternal(double distance, double weight, DeliverySpeed speed) {
        // Determine transport mode based on distance
        TransportMode transportMode = TransportMode.getTransportMode(distance);
        
        log.debug("Transport mode: {} for distance: {} km", transportMode.getName(), distance);
        
        // Calculate base shipping charge based on transport mode
        double baseCharge = transportMode.calculateCharge(distance, weight);
        
        // Add delivery speed charges
        double speedCharge = speed.calculateAdditionalCharge(weight);
        
        double totalCharge = baseCharge + speedCharge;
        
        log.debug("Base charge: {} Rs, Speed charge: {} Rs, Total: {} Rs", 
                  baseCharge, speedCharge, totalCharge);
        
        // Round to 2 decimal places
        return Math.round(totalCharge * 100.0) / 100.0;
    }
}
