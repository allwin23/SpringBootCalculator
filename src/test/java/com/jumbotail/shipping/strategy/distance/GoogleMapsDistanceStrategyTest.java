package com.jumbotail.shipping.strategy.distance;

import com.jumbotail.shipping.dto.distance.CalculationMode;
import com.jumbotail.shipping.dto.distance.DistanceResponse;
import com.jumbotail.shipping.dto.distance.GoogleDistanceMatrixResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.client.RestTemplate;

import java.net.URI;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class GoogleMapsDistanceStrategyTest {

    @Mock
    private RestTemplate restTemplate;

    @Mock
    private HaversineDistanceStrategy fallbackStrategy;

    @InjectMocks
    private GoogleMapsDistanceStrategy strategy;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(strategy, "apiKey", "test-api-key");
    }

    @Test
    void calculate_success_returnsGoogleDistance() {
        // Arrange
        GoogleDistanceMatrixResponse mockResponse = new GoogleDistanceMatrixResponse();
        mockResponse.setStatus("OK");
        
        GoogleDistanceMatrixResponse.Distance distance = new GoogleDistanceMatrixResponse.Distance();
        distance.setValue(142340); // 142.34 km
        
        GoogleDistanceMatrixResponse.Duration duration = new GoogleDistanceMatrixResponse.Duration();
        duration.setValue(10080); // 168 minutes
        
        GoogleDistanceMatrixResponse.Element element = new GoogleDistanceMatrixResponse.Element();
        element.setStatus("OK");
        element.setDistance(distance);
        element.setDuration(duration);
        
        GoogleDistanceMatrixResponse.Row row = new GoogleDistanceMatrixResponse.Row();
        row.setElements(List.of(element));
        
        mockResponse.setRows(List.of(row));

        when(restTemplate.getForObject(any(URI.class), eq(GoogleDistanceMatrixResponse.class)))
                .thenReturn(mockResponse);

        // Act
        DistanceResponse response = strategy.calculate(12.9, 77.5, 13.1, 78.1);

        // Assert
        assertEquals(142.34, response.getDistanceKm());
        assertEquals(168, response.getDurationMinutes());
        assertEquals(CalculationMode.GOOGLE.name(), response.getCalculationMode());
        
        verify(fallbackStrategy, never()).calculate(anyDouble(), anyDouble(), anyDouble(), anyDouble());
    }

    @Test
    void calculate_apiError_usesFallback() {
        // Arrange
        when(restTemplate.getForObject(any(URI.class), eq(GoogleDistanceMatrixResponse.class)))
                .thenThrow(new RuntimeException("API Connection Refused"));

        DistanceResponse fallbackResponse = DistanceResponse.builder()
                .distanceKm(50.0)
                .durationMinutes(75)
                .calculationMode(CalculationMode.HAVERSINE.name())
                .build();
                
        when(fallbackStrategy.calculate(12.9, 77.5, 13.1, 78.1))
                .thenReturn(fallbackResponse);

        // Act
        DistanceResponse response = strategy.calculate(12.9, 77.5, 13.1, 78.1);

        // Assert
        assertEquals(50.0, response.getDistanceKm());
        assertEquals(CalculationMode.HAVERSINE.name(), response.getCalculationMode());
        
        verify(fallbackStrategy, times(1)).calculate(12.9, 77.5, 13.1, 78.1);
    }
    
    @Test
    void calculate_missingApiKey_usesFallbackImmediately() {
        // Arrange
        ReflectionTestUtils.setField(strategy, "apiKey", "UNSET");
        
        DistanceResponse fallbackResponse = DistanceResponse.builder()
                .distanceKm(50.0)
                .durationMinutes(75)
                .calculationMode(CalculationMode.HAVERSINE.name())
                .build();
                
        when(fallbackStrategy.calculate(12.9, 77.5, 13.1, 78.1))
                .thenReturn(fallbackResponse);

        // Act
        DistanceResponse response = strategy.calculate(12.9, 77.5, 13.1, 78.1);

        // Assert
        assertEquals(CalculationMode.HAVERSINE.name(), response.getCalculationMode());
        
        // Ensure no external call is made if API key is not configured
        verify(restTemplate, never()).getForObject(any(URI.class), eq(GoogleDistanceMatrixResponse.class));
        verify(fallbackStrategy, times(1)).calculate(12.9, 77.5, 13.1, 78.1);
    }
}
