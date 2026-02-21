package com.jumbotail.shipping.dto;

import com.jumbotail.shipping.model.OrderStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Response DTO for created order
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrderResponse {
    
    private String orderId;
    private String sellerId;
    private String customerId;
    private List<OrderItemDTO> items;
    private Double totalAmount;
    private Double totalWeight;
    private OrderStatus status;
    private LocalDateTime orderDate;
}
