package com.jumbotail.shipping.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.jumbotail.shipping.dto.LogisticsSimulationRequest;
import com.jumbotail.shipping.dto.LogisticsSimulationResponse;
import com.jumbotail.shipping.dto.TransportModeOption;
import com.jumbotail.shipping.service.LogisticsSimulationService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(LogisticsController.class)
class LogisticsControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private LogisticsSimulationService logisticsSimulationService;

    @Test
    void testSimulateLogistics_Success() throws Exception {
        LogisticsSimulationRequest request = new LogisticsSimulationRequest("ORD-123", "speed");
        
        TransportModeOption aero = new TransportModeOption("Aeroplane", 1500.0, 5.0);
        LogisticsSimulationResponse response = LogisticsSimulationResponse.builder()
                .orderId("ORD-123")
                .priority("speed")
                .distanceKm(1000.0)
                .options(List.of(aero))
                .recommendedOption(aero)
                .build();

        when(logisticsSimulationService.simulateLogistics(any(LogisticsSimulationRequest.class))).thenReturn(response);

        mockMvc.perform(post("/api/v1/logistics/simulate")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.orderId").value("ORD-123"))
                .andExpect(jsonPath("$.priority").value("speed"))
                .andExpect(jsonPath("$.recommendedOption.transportMode").value("Aeroplane"));
    }
}
