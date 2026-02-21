package com.jumbotail.shipping.controller;

import com.jumbotail.shipping.dto.ProductWeightResponse;
import com.jumbotail.shipping.service.WeightConversionService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/products")
@RequiredArgsConstructor
public class ProductController {

    private final WeightConversionService weightConversionService;

    @GetMapping("/{id}/weight")
    public ResponseEntity<ProductWeightResponse> getProductWeight(
            @PathVariable("id") String productId,
            @RequestParam(name = "unit", defaultValue = "kg") String unit) {
        
        ProductWeightResponse response = weightConversionService.getProductWeight(productId, unit);
        return ResponseEntity.ok(response);
    }
}
