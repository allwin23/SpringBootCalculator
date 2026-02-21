package com.jumbotail.shipping.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProductWeightResponse {
    private String productId;
    private String originalWeight;
    private String convertedWeight;
}
