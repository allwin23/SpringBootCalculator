package com.jumbotail.shipping.controller;

import com.jumbotail.shipping.dto.recommendation.RecommendationRequest;
import com.jumbotail.shipping.dto.recommendation.RecommendationResponse;
import com.jumbotail.shipping.service.recommendation.LogisticsDecisionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/logistics")
@RequiredArgsConstructor
@Tag(name = "3. Logistics Optimization Engine", description = "Elite Logistics Optimization Engine endpoints")
public class LogisticsRecommendationController {

    private final LogisticsDecisionService logisticsDecisionService;

    @PostMapping("/recommendation")
    @Operation(summary = "Get Optimal Logistics Recommendation", description = "Simulates options and recommends the best warehouse/mode based on priority")
    public ResponseEntity<RecommendationResponse> getRecommendation(@Valid @RequestBody RecommendationRequest request) {
        RecommendationResponse response = logisticsDecisionService.recommendLogistics(request.getOrderId(), request.getPriority());
        return ResponseEntity.ok(response);
    }
}
