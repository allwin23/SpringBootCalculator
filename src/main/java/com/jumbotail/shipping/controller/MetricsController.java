package com.jumbotail.shipping.controller;

import com.jumbotail.shipping.dto.ShippingMetricsResponse;
import com.jumbotail.shipping.service.ShippingMetricsService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@RequestMapping("/api/v1/metrics")
@RequiredArgsConstructor
@Tag(name = "5. System Health & Metrics", description = "Endpoints for checking system health and real-time transit stats")
public class MetricsController {

    private final ShippingMetricsService shippingMetricsService;

    @GetMapping("/shipping")
    public ResponseEntity<ShippingMetricsResponse> getShippingMetrics() {
        return ResponseEntity.ok(shippingMetricsService.getMetrics());
    }
}
