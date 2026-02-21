package com.jumbotail.shipping.service;

import com.github.benmanes.caffeine.cache.stats.CacheStats;
import com.jumbotail.shipping.dto.ShippingMetricsResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.cache.caffeine.CaffeineCache;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

@Service
@RequiredArgsConstructor
public class ShippingMetricsService {

    private final AtomicLong totalRequests = new AtomicLong(0);
    private final AtomicLong totalLatencyMs = new AtomicLong(0);
    private final AtomicLong failedRequests = new AtomicLong(0);
    private final ConcurrentHashMap<String, AtomicLong> transportModeUsage = new ConcurrentHashMap<>();

    private final CacheManager cacheManager;

    public void recordMetrics(long latencyMs, String transportMode, boolean success) {
        totalRequests.incrementAndGet();
        totalLatencyMs.addAndGet(latencyMs);

        if (!success) {
            failedRequests.incrementAndGet();
        }

        if (transportMode != null && !transportMode.isEmpty() && success) {
            transportModeUsage.computeIfAbsent(transportMode, k -> new AtomicLong(0)).incrementAndGet();
        }
    }

    public ShippingMetricsResponse getMetrics() {
        long requests = totalRequests.get();
        long avgLatency = requests > 0 ? totalLatencyMs.get() / requests : 0;
        long failures = failedRequests.get();

        String mostUsed = "None";
        long maxUsage = 0;
        for (Map.Entry<String, AtomicLong> entry : transportModeUsage.entrySet()) {
            if (entry.getValue().get() > maxUsage) {
                maxUsage = entry.getValue().get();
                mostUsed = entry.getKey();
            }
        }

        long cacheHits = getCacheHits();

        return ShippingMetricsResponse.builder()
                .totalRequests(requests)
                .avgLatencyMs(avgLatency)
                .cacheHits(cacheHits)
                .mostUsedTransport(mostUsed)
                .failedRequests(failures)
                .build();
    }

    private long getCacheHits() {
        long hits = 0;
        if (cacheManager == null) return hits;

        String[] relevantCaches = {"shippingEstimate", "shippingCharge"};
        for (String cacheName : relevantCaches) {
            Cache springCache = cacheManager.getCache(cacheName);
            if (springCache instanceof CaffeineCache) {
                CacheStats stats = ((CaffeineCache) springCache).getNativeCache().stats();
                hits += stats.hitCount();
            }
        }
        return hits;
    }
}
