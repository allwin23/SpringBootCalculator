package com.jumbotail.shipping.controller;

import com.jumbotail.shipping.exception.ResourceNotFoundException;
import com.jumbotail.shipping.service.WarehouseService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.containsString;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(WarehouseController.class)
class WarehouseControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private WarehouseService warehouseService;

    @Test
    void testGetNearestWarehouse_NotFound() throws Exception {
        when(warehouseService.findNearestWarehouse(anyString(), anyString()))
                .thenThrow(new ResourceNotFoundException("Seller not found with ID: 123"));

        mockMvc.perform(get("/api/v1/warehouse/nearest")
                        .param("sellerId", "123")
                        .param("productId", "456")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.error").value("Resource Not Found"))
                .andExpect(jsonPath("$.message").value("Seller not found with ID: 123"))
                .andExpect(jsonPath("$.path").value("/api/v1/warehouse/nearest"))
                .andExpect(jsonPath("$.timestamp").exists());
    }

    @Test
    void testGetNearestWarehouse_MissingParam() throws Exception {
        mockMvc.perform(get("/api/v1/warehouse/nearest")
                        .param("sellerId", "123")
                        // missing productId
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.error").value("Missing Parameter"))
                .andExpect(jsonPath("$.message").value(containsString("productId")))
                .andExpect(jsonPath("$.timestamp").exists());
    }
}
