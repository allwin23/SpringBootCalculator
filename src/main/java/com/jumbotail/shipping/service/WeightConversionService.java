package com.jumbotail.shipping.service;

import com.jumbotail.shipping.dto.ProductWeightResponse;
import com.jumbotail.shipping.exception.InvalidRequestException;
import com.jumbotail.shipping.exception.ResourceNotFoundException;
import com.jumbotail.shipping.model.Product;
import com.jumbotail.shipping.model.WeightUnit;
import com.jumbotail.shipping.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.text.DecimalFormat;

@Service
@RequiredArgsConstructor
@Slf4j
public class WeightConversionService {

    private final ProductRepository productRepository;
    private static final DecimalFormat df = new DecimalFormat("#.####");

    /**
     * Get product weight in requested unit. Features caching for performance.
     *
     * @param productId The product ID
     * @param unitStr   The target unit string (e.g., "g", "kg", "ton")
     * @return ProductWeightResponse containing original and converted weight
     */
    @Cacheable(value = "productWeight", key = "#productId + '_' + #unitStr")
    public ProductWeightResponse getProductWeight(String productId, String unitStr) {
        log.info("Calculating product weight for product: {} in unit: {}", productId, unitStr);

        // 1. Validate requested unit
        WeightUnit targetUnit;
        try {
            targetUnit = WeightUnit.fromString(unitStr);
        } catch (IllegalArgumentException e) {
            throw new InvalidRequestException("Invalid unit: " + unitStr + ". Supported units are: g, kg, ton");
        }

        // 2. Fetch product
        Product product = productRepository.findByProductId(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found with id: " + productId));

        // 3. Get base weight (always stored in KG)
        Double baseWeightKg = product.getAttributes().getWeight();
        if (baseWeightKg == null) {
            throw new InvalidRequestException("Product " + productId + " does not have a weight defined.");
        }

        // 4. Convert weight
        double convertedWeight = convertWeight(baseWeightKg, targetUnit);

        // 5. Format response
        String originalWeightStr = df.format(baseWeightKg) + " kg";
        String convertedWeightStr = formatConvertedWeight(convertedWeight, targetUnit);

        return ProductWeightResponse.builder()
                .productId(productId)
                .originalWeight(originalWeightStr)
                .convertedWeight(convertedWeightStr)
                .build();
    }

    /**
     * Converts weight from KG to the target unit.
     */
    public double convertWeight(double weightInKg, WeightUnit targetUnit) {
        return switch (targetUnit) {
            case G -> weightInKg * 1000.0;
            case KG -> weightInKg;
            case TON -> weightInKg / 1000.0;
        };
    }

    private String formatConvertedWeight(double weight, WeightUnit unit) {
        if (unit == WeightUnit.G) {
            // Drop decimals for grams usually, but we'll stick to df for consistency
            return df.format(weight) + " " + unit.getSymbol();
        }
        return df.format(weight) + " " + unit.getSymbol();
    }
}
