package com.jumbotail.shipping.service;

import com.jumbotail.shipping.dto.NearestWarehouseResponse;
import com.jumbotail.shipping.model.Location;
import com.jumbotail.shipping.model.Product;
import com.jumbotail.shipping.model.ProductAttributes;
import com.jumbotail.shipping.model.Seller;
import com.jumbotail.shipping.model.Warehouse;
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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
public class CacheVerificationTest {

    @Autowired
    private WarehouseService warehouseService;

    @Autowired
    private CacheManager cacheManager;

    @Autowired
    private SellerRepository sellerRepository;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private WarehouseRepository warehouseRepository;

    private Seller testSeller;
    private Product testProduct;
    private Warehouse testWarehouse;

    @BeforeEach
    void setUp() {
        // Clear caches
        cacheManager.getCacheNames().forEach(name -> cacheManager.getCache(name).clear());

        // Setup test data
        testSeller = Seller.builder()
                .sellerId("CACHE-SELLER-001")
                .name("Cache Test Seller")
                .location(new Location(12.5, 37.5))
                .active(true)
                .build();
        testSeller = sellerRepository.save(testSeller);

        testProduct = Product.builder()
                .productId("CACHE-PRODUCT-001")
                .name("Cache Test Product")
                .sellingPrice(100.0)
                .seller(testSeller)
                .attributes(new ProductAttributes(1.0, 10.0, 10.0, 10.0))
                .active(true)
                .build();
        testProduct = productRepository.save(testProduct);

        testWarehouse = Warehouse.builder()
                .warehouseId("CACHE-WH-001")
                .name("Cache Test Warehouse")
                .location(new Location(12.6, 37.6))
                .active(true)
                .build();
        testWarehouse = warehouseRepository.save(testWarehouse);
    }

    @Test
    void testNearestWarehouseCaching() {
        String sellerId = testSeller.getSellerId();
        String productId = testProduct.getProductId();

        // 1. Initial call (should populate cache)
        NearestWarehouseResponse firstResponse = warehouseService.findNearestWarehouse(sellerId, productId);
        assertNotNull(firstResponse);
        
        // Check if cache is populated
        assertNotNull(cacheManager.getCache("nearestWarehouse").get(sellerId + "_" + productId));

        // 2. Modify database (this shouldn't affect the cached result)
        testWarehouse.setName("Updated Warehouse Name");
        warehouseRepository.save(testWarehouse);

        // 3. Second call (should hit cache)
        NearestWarehouseResponse secondResponse = warehouseService.findNearestWarehouse(sellerId, productId);
        
        // In a real scenario, we might check logs or use Mockito to verify the repository was only called once
        // But for this simple check, we verify the response is identical and cache is still there
        assertEquals(firstResponse.getWarehouseId(), secondResponse.getWarehouseId());
        
        // 4. Clear cache manually and verify it hits DB again (if we had a way to track hits)
        cacheManager.getCache("nearestWarehouse").clear();
        NearestWarehouseResponse thirdResponse = warehouseService.findNearestWarehouse(sellerId, productId);
        assertNotNull(thirdResponse);
    }
}
