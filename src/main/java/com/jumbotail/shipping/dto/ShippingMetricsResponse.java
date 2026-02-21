package com.jumbotail.shipping.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ShippingMetricsResponse {
    private long totalRequests;
    private long avgLatencyMs;
    private long cacheHits;
    private String mostUsedTransport;
    private long failedRequests;
}
