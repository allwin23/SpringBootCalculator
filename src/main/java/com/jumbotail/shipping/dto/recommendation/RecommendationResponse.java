package com.jumbotail.shipping.dto.recommendation;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RecommendationResponse {
    private RecommendationOption recommendedOption;
    private List<RecommendationOption> alternatives;
}
