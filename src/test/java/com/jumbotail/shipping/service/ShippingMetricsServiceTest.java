package com.jumbotail.shipping.service;

import com.github.benmanes.caffeine.cache.stats.CacheStats;
import com.jumbotail.shipping.dto.ShippingMetricsResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.cache.CacheManager;
import org.springframework.cache.caffeine.CaffeineCache;
import com.github.benmanes.caffeine.cache.Cache;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ShippingMetricsServiceTest {

    @Mock
    private CacheManager cacheManager;

    @InjectMocks
    private ShippingMetricsService shippingMetricsService;

    @Test
    void testRecordMetricsAndGetMetrics() {
        // Record successes
        shippingMetricsService.recordMetrics(100, "Truck", true);
        shippingMetricsService.recordMetrics(50, "Truck", true);
        shippingMetricsService.recordMetrics(200, "Aeroplane", true);
        
        // Record failure
        shippingMetricsService.recordMetrics(150, null, false);

        ShippingMetricsResponse metrics = shippingMetricsService.getMetrics();
        
        assertEquals(4, metrics.getTotalRequests());
        assertEquals(1, metrics.getFailedRequests());
        assertEquals(125, metrics.getAvgLatencyMs()); // (100+50+200+150) / 4 = 125
        assertEquals("Truck", metrics.getMostUsedTransport());
        assertEquals(0, metrics.getCacheHits());
    }
}
