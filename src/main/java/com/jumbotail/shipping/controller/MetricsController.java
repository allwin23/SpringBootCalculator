package com.jumbotail.shipping.controller;

import com.jumbotail.shipping.dto.ShippingMetricsResponse;
import com.jumbotail.shipping.service.ShippingMetricsService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/metrics")
@RequiredArgsConstructor
public class MetricsController {

    private final ShippingMetricsService shippingMetricsService;

    @GetMapping("/shipping")
    public ResponseEntity<ShippingMetricsResponse> getShippingMetrics() {
        return ResponseEntity.ok(shippingMetricsService.getMetrics());
    }
}
