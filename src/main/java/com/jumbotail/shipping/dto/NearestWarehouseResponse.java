package com.jumbotail.shipping.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Response DTO for nearest warehouse API
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class NearestWarehouseResponse {
    private String warehouseId;
    private LocationDTO warehouseLocation;
}
