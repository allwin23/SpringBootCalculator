package com.jumbotail.shipping.strategy.distance;

import com.jumbotail.shipping.dto.distance.CalculationMode;
import com.jumbotail.shipping.dto.distance.DistanceResponse;
import com.jumbotail.shipping.dto.distance.GoogleDistanceMatrixResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;
import java.util.Optional;

@Component("googleDistanceStrategy")
@RequiredArgsConstructor
@Slf4j
public class GoogleMapsDistanceStrategy implements DistanceStrategy {

    private final RestTemplate restTemplate;
    // Injecting the Haversine strategy so we can fallback to it seamlessly if Google fails
    private final HaversineDistanceStrategy fallbackStrategy;

    @Value("${google.maps.api-key:UNSET}")
    private String apiKey;

    private static final String GOOGLE_MAPS_API_URL = "https://maps.googleapis.com/maps/api/distancematrix/json";

    @Override
    public DistanceResponse calculate(double srcLat, double srcLng, double destLat, double destLng) {
        if ("UNSET".equals(apiKey) || apiKey.isBlank()) {
            log.warn("Google Maps API key is not configured. Falling back to Haversine calculation.");
            return fallbackStrategy.calculate(srcLat, srcLng, destLat, destLng);
        }

        try {
            URI uri = UriComponentsBuilder.fromHttpUrl(GOOGLE_MAPS_API_URL)
                    .queryParam("origins", srcLat + "," + srcLng)
                    .queryParam("destinations", destLat + "," + destLng)
                    .queryParam("key", apiKey)
                    .build()
                    .toUri();

            log.debug("Calling Google Distance Matrix API: {}", uri.toString().replaceAll("key=[^&]+", "key=***"));

            GoogleDistanceMatrixResponse response = restTemplate.getForObject(uri, GoogleDistanceMatrixResponse.class);

            if (response != null && "OK".equals(response.getStatus())) {
                return parseGoogleResponse(response, srcLat, srcLng, destLat, destLng);
            } else {
                log.warn("Google API returned non-OK status: {}. Falling back to Haversine.", 
                        response != null ? response.getStatus() : "NULL");
                return fallbackStrategy.calculate(srcLat, srcLng, destLat, destLng);
            }
            
        } catch (Exception e) {
            log.error("Failed to calculate distance using Google Maps API. Falling back to Haversine. Error: {}", e.getMessage());
            return fallbackStrategy.calculate(srcLat, srcLng, destLat, destLng);
        }
    }

    private DistanceResponse parseGoogleResponse(GoogleDistanceMatrixResponse response, double srcLat, double srcLng, double destLat, double destLng) {
        try {
            GoogleDistanceMatrixResponse.Element element = response.getRows().get(0).getElements().get(0);

            if (!"OK".equals(element.getStatus())) {
                log.warn("Google Element status is not OK: {}. Falling back.", element.getStatus());
                return fallbackStrategy.calculate(srcLat, srcLng, destLat, destLng);
            }

            // Convert meters to kilometers
            double distanceKm = element.getDistance().getValue() / 1000.0;
            // Convert seconds to minutes
            int durationMinutes = element.getDuration().getValue() / 60;

            return DistanceResponse.builder()
                    .distanceKm(Math.round(distanceKm * 100.0) / 100.0)
                    .durationMinutes(durationMinutes)
                    .calculationMode(CalculationMode.GOOGLE.name())
                    .build();
                    
        } catch (Exception e) {
            log.error("Failed parsing Google JSON response structure. Falling back. Error: {}", e.getMessage());
            return fallbackStrategy.calculate(srcLat, srcLng, destLat, destLng);
        }
    }
}
