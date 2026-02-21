package com.jumbotail.shipping.controller;

import com.jumbotail.shipping.dto.distance.CalculationMode;
import com.jumbotail.shipping.dto.distance.DistanceResponse;
import com.jumbotail.shipping.service.DistanceService;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/logistics")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "3. Logistics Optimization Engine")
public class DistanceController {

    private final DistanceService distanceService;

    /**
     * Calculate route distance and Estimated Time of Arrival (ETA)
     * 
     * GET /api/v1/logistics/distance
     */
    @GetMapping("/distance")
    public ResponseEntity<DistanceResponse> getDistance(
            @RequestParam double sourceLat,
            @RequestParam double sourceLng,
            @RequestParam double destLat,
            @RequestParam double destLng,
            @RequestParam(defaultValue = "HAVERSINE") CalculationMode mode) {

        log.info("Received request for Distance Calculation. Mode: {}", mode);

        DistanceResponse response = distanceService.calculateDistance(sourceLat, sourceLng, destLat, destLng, mode);
        return ResponseEntity.ok(response);
    }
}
