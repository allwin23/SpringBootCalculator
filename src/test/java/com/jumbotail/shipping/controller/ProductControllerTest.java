package com.jumbotail.shipping.controller;

import com.jumbotail.shipping.dto.ProductWeightResponse;
import com.jumbotail.shipping.exception.InvalidRequestException;
import com.jumbotail.shipping.exception.ResourceNotFoundException;
import com.jumbotail.shipping.service.WeightConversionService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(ProductController.class)
class ProductControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private WeightConversionService weightConversionService;

    @Test
    void testGetProductWeight_Success() throws Exception {
        ProductWeightResponse response = ProductWeightResponse.builder()
                .productId("123")
                .originalWeight("2.5 kg")
                .convertedWeight("2500 g")
                .build();

        when(weightConversionService.getProductWeight("123", "g")).thenReturn(response);

        mockMvc.perform(get("/api/v1/products/123/weight")
                .param("unit", "g"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.productId").value("123"))
                .andExpect(jsonPath("$.originalWeight").value("2.5 kg"))
                .andExpect(jsonPath("$.convertedWeight").value("2500 g"));
    }

    @Test
    void testGetProductWeight_InvalidUnit() throws Exception {
        when(weightConversionService.getProductWeight("123", "invalid"))
                .thenThrow(new InvalidRequestException("Invalid unit: invalid"));

        mockMvc.perform(get("/api/v1/products/123/weight")
                .param("unit", "invalid"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Invalid Request"))
                .andExpect(jsonPath("$.message").value("Invalid unit: invalid"));
    }

    @Test
    void testGetProductWeight_ProductNotFound() throws Exception {
        when(weightConversionService.getProductWeight("999", "kg"))
                .thenThrow(new ResourceNotFoundException("Product not found"));

        mockMvc.perform(get("/api/v1/products/999/weight")
                .param("unit", "kg"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value("Resource Not Found"))
                .andExpect(jsonPath("$.message").value("Product not found"));
    }
}
