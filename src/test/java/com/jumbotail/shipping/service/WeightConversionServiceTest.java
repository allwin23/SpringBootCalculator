package com.jumbotail.shipping.service;

import com.jumbotail.shipping.dto.ProductWeightResponse;
import com.jumbotail.shipping.exception.InvalidRequestException;
import com.jumbotail.shipping.exception.ResourceNotFoundException;
import com.jumbotail.shipping.model.Product;
import com.jumbotail.shipping.model.ProductAttributes;
import com.jumbotail.shipping.model.WeightUnit;
import com.jumbotail.shipping.repository.ProductRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class WeightConversionServiceTest {

    @Mock
    private ProductRepository productRepository;

    @InjectMocks
    private WeightConversionService weightConversionService;

    private Product mockProduct;

    @BeforeEach
    void setUp() {
        ProductAttributes attributes = new ProductAttributes(2.5, 10.0, 10.0, 10.0);
        mockProduct = Product.builder()
                .productId("123")
                .attributes(attributes)
                .build();
    }

    @Test
    void testConvertWeight_Grams() {
        double result = weightConversionService.convertWeight(2.5, WeightUnit.G);
        assertEquals(2500.0, result);
    }

    @Test
    void testConvertWeight_Kilograms() {
        double result = weightConversionService.convertWeight(2.5, WeightUnit.KG);
        assertEquals(2.5, result);
    }

    @Test
    void testConvertWeight_Tons() {
        double result = weightConversionService.convertWeight(2500.0, WeightUnit.TON);
        assertEquals(2.5, result);
    }

    @Test
    void testGetProductWeight_Success() {
        when(productRepository.findByProductId("123")).thenReturn(Optional.of(mockProduct));

        ProductWeightResponse response = weightConversionService.getProductWeight("123", "g");

        assertEquals("123", response.getProductId());
        assertEquals("2.5 kg", response.getOriginalWeight());
        assertEquals("2500 g", response.getConvertedWeight());
    }

    @Test
    void testGetProductWeight_InvalidUnit() {
        assertThrows(InvalidRequestException.class, () -> {
            weightConversionService.getProductWeight("123", "invalid");
        });
    }

    @Test
    void testGetProductWeight_ProductNotFound() {
        when(productRepository.findByProductId("999")).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> {
            weightConversionService.getProductWeight("999", "g");
        });
    }
}
