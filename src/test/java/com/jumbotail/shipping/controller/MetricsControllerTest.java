package com.jumbotail.shipping.controller;

import com.jumbotail.shipping.dto.ShippingMetricsResponse;
import com.jumbotail.shipping.service.ShippingMetricsService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(MetricsController.class)
class MetricsControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ShippingMetricsService shippingMetricsService;

    @Test
    void testGetShippingMetrics() throws Exception {
        ShippingMetricsResponse response = ShippingMetricsResponse.builder()
                .totalRequests(10)
                .avgLatencyMs(45)
                .cacheHits(5)
                .mostUsedTransport("Truck")
                .failedRequests(1)
                .build();

        when(shippingMetricsService.getMetrics()).thenReturn(response);

        mockMvc.perform(get("/api/v1/metrics/shipping"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalRequests").value(10))
                .andExpect(jsonPath("$.avgLatencyMs").value(45))
                .andExpect(jsonPath("$.cacheHits").value(5))
                .andExpect(jsonPath("$.mostUsedTransport").value("Truck"))
                .andExpect(jsonPath("$.failedRequests").value(1));
    }
}
