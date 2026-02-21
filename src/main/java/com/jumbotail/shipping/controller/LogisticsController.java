package com.jumbotail.shipping.controller;

import com.jumbotail.shipping.dto.LogisticsSimulationRequest;
import com.jumbotail.shipping.dto.LogisticsSimulationResponse;
import com.jumbotail.shipping.service.LogisticsSimulationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@RequestMapping("/api/v1/logistics")
@RequiredArgsConstructor
@Tag(name = "3. Logistics Optimization Engine")
public class LogisticsController {

    private final LogisticsSimulationService logisticsSimulationService;

    @PostMapping("/simulate")
    public ResponseEntity<LogisticsSimulationResponse> simulateLogistics(
            @Valid @RequestBody LogisticsSimulationRequest request) {
        
        LogisticsSimulationResponse response = logisticsSimulationService.simulateLogistics(request);
        return ResponseEntity.ok(response);
    }
}
