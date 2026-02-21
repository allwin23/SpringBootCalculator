package com.jumbotail.shipping.service;

import com.jumbotail.shipping.dto.ShippingChargeRequest;
import com.jumbotail.shipping.dto.ShippingChargeResponse;
import com.jumbotail.shipping.exception.InvalidRequestException;
import com.jumbotail.shipping.exception.ResourceNotFoundException;
import com.jumbotail.shipping.model.*;
import com.jumbotail.shipping.repository.CustomerRepository;
import com.jumbotail.shipping.repository.ProductRepository;
import com.jumbotail.shipping.repository.SellerRepository;
import com.jumbotail.shipping.repository.WarehouseRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cache.CacheManager;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class ShippingChargeServiceTest {
    
    @Autowired
    private ShippingChargeService shippingChargeService;
    
    @Autowired
    private CacheManager cacheManager;
    
    @Autowired
    private WarehouseService warehouseService;
    
    @Autowired
    private CustomerRepository customerRepository;
    
    @Autowired
    private SellerRepository sellerRepository;
    
    @Autowired
    private ProductRepository productRepository;
    
    @Autowired
    private WarehouseRepository warehouseRepository;
    
    private Customer testCustomer;
    private Seller testSeller;
    private Product testProduct;
    private Warehouse testWarehouse;
    
    @BeforeEach
    void setUp() {
        // Clear caches to prevent pollution
        cacheManager.getCacheNames().forEach(name -> cacheManager.getCache(name).clear());
        
        // Create test customer
        testCustomer = Customer.builder()
                .customerId("TEST-CUST-001")
                .name("Test Customer")
                .phoneNumber("9876543210")
                .location(new Location(13.0, 38.0))
                .active(true)
                .build();
        testCustomer = customerRepository.save(testCustomer);
        
        // Create test seller
        testSeller = Seller.builder()
                .sellerId("TEST-SELLER-001")
                .name("Test Seller")
                .location(new Location(12.5, 37.5))
                .active(true)
                .build();
        testSeller = sellerRepository.save(testSeller);
        
        // Create test product
        testProduct = Product.builder()
                .productId("TEST-PRODUCT-001")
                .name("Test Product")
                .sellingPrice(100.0)
                .seller(testSeller)
                .attributes(new ProductAttributes(5.0, 20.0, 20.0, 20.0)) // 5kg product
                .active(true)
                .build();
        testProduct = productRepository.save(testProduct);
        
        // Create test warehouse
        testWarehouse = Warehouse.builder()
                .warehouseId("WH-001")
                .name("Test Warehouse")
                .location(new Location(12.6, 37.6))
                .active(true)
                .build();
        testWarehouse = warehouseRepository.save(testWarehouse);
    }
    
    @Test
    void testCalculateShippingCharge_Standard_Success() {
        Double charge = shippingChargeService.calculateShippingCharge(
                testWarehouse.getWarehouseId(), testCustomer.getCustomerId(), "standard", testProduct.getProductId());
        
        assertNotNull(charge);
        assertTrue(charge > 0);
    }
    
    @Test
    void testCalculateShippingCharge_Express_Success() {
        Double charge = shippingChargeService.calculateShippingCharge(
                testWarehouse.getWarehouseId(), testCustomer.getCustomerId(), "express", testProduct.getProductId());
        
        assertNotNull(charge);
        assertTrue(charge > 0);
    }
    
    @Test
    void testCalculateShippingCharge_InvalidDeliverySpeed() {
        assertThrows(InvalidRequestException.class, () -> {
            shippingChargeService.calculateShippingCharge(
                    testWarehouse.getWarehouseId(), testCustomer.getCustomerId(), "invalid", testProduct.getProductId());
        });
    }
    
    @Test
    void testCalculateShippingCharge_CustomerNotFound() {
        assertThrows(ResourceNotFoundException.class, () -> {
            shippingChargeService.calculateShippingCharge(
                    testWarehouse.getWarehouseId(), "NON-EXISTENT", "standard", testProduct.getProductId());
        });
    }
    
    @Test
    void testCalculateShippingChargeForSellerAndCustomer_Success() {
        ShippingChargeRequest request = new ShippingChargeRequest();
        request.setSellerId(testSeller.getSellerId());
        request.setCustomerId(testCustomer.getCustomerId());
        request.setDeliverySpeed("express");
        
        ShippingChargeResponse response = shippingChargeService.calculateShippingChargeForSellerAndCustomer(request);
        
        assertNotNull(response);
        assertNotNull(response.getShippingCharge());
        assertNotNull(response.getNearestWarehouse());
        assertTrue(response.getShippingCharge() > 0);
    }
    
    @Test
    void testCalculateShippingChargeForSellerAndCustomer_InvalidDeliverySpeed() {
        ShippingChargeRequest request = new ShippingChargeRequest();
        request.setSellerId(testSeller.getSellerId());
        request.setCustomerId(testCustomer.getCustomerId());
        request.setDeliverySpeed("invalid");
        
        assertThrows(InvalidRequestException.class, () -> {
            shippingChargeService.calculateShippingChargeForSellerAndCustomer(request);
        });
    }
}
