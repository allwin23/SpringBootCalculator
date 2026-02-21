package com.jumbotail.shipping.service;

import com.jumbotail.shipping.dto.OrderItemDTO;
import com.jumbotail.shipping.dto.OrderRequest;
import com.jumbotail.shipping.dto.OrderResponse;
import com.jumbotail.shipping.exception.InvalidRequestException;
import com.jumbotail.shipping.exception.ResourceNotFoundException;
import com.jumbotail.shipping.model.*;
import com.jumbotail.shipping.repository.CustomerRepository;
import com.jumbotail.shipping.repository.OrderRepository;
import com.jumbotail.shipping.repository.ProductRepository;
import com.jumbotail.shipping.repository.SellerRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Service for handling order-related operations
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class OrderService {
    
    private final OrderRepository orderRepository;
    private final CustomerRepository customerRepository;
    private final SellerRepository sellerRepository;
    private final ProductRepository productRepository;
    
    /**
     * Create a new order with validations and weight calculation
     * 
     * @param request Order request DTO
     * @return Order response DTO
     */
    @Transactional
    public OrderResponse createOrder(OrderRequest request) {
        log.info("Creating order for sellerId: {}, customerId: {}", request.getSellerId(), request.getCustomerId());
        
        // 1. Validate Seller
        Seller seller = sellerRepository.findBySellerIdAndActiveTrue(request.getSellerId())
                .orElseThrow(() -> new ResourceNotFoundException("Seller not found with ID: " + request.getSellerId()));
        
        // 2. Validate Customer
        Customer customer = customerRepository.findByCustomerIdAndActiveTrue(request.getCustomerId())
                .orElseThrow(() -> new ResourceNotFoundException("Customer not found with ID: " + request.getCustomerId()));
        
        // 3. Initialize Order
        Order order = Order.builder()
                .orderId("ORD-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase())
                .customer(customer)
                .seller(seller)
                .status(OrderStatus.CREATED)
                .build();
        
        List<OrderItem> orderItems = new ArrayList<>();
        double totalAmount = 0.0;
        double totalWeight = 0.0;
        
        // 4. Validate and Process Products
        for (OrderItemDTO itemDto : request.getItems()) {
            Product product = productRepository.findByProductIdAndActiveTrue(itemDto.getProductId())
                    .orElseThrow(() -> new ResourceNotFoundException("Product not found with ID: " + itemDto.getProductId()));
            
            // Ensure product belongs to the seller
            if (!product.getSeller().getSellerId().equals(request.getSellerId())) {
                throw new InvalidRequestException("Product " + itemDto.getProductId() + " does not belong to seller " + request.getSellerId());
            }
            
            double unitPrice = product.getSellingPrice();
            double unitWeight = (product.getAttributes() != null && product.getAttributes().getWeight() != null) 
                    ? product.getAttributes().getWeight() : 1.0;
            
            OrderItem orderItem = OrderItem.builder()
                    .order(order)
                    .product(product)
                    .quantity(itemDto.getQuantity())
                    .price(unitPrice)
                    .weight(unitWeight)
                    .build();
            
            orderItems.add(orderItem);
            totalAmount += unitPrice * itemDto.getQuantity();
            totalWeight += unitWeight * itemDto.getQuantity();
        }
        
        order.setItems(orderItems);
        order.setTotalAmount(Math.round(totalAmount * 100.0) / 100.0);
        order.setTotalWeight(Math.round(totalWeight * 100.0) / 100.0);
        
        // 5. Persist Order
        Order savedOrder = orderRepository.save(order);
        log.info("Order created successfully with ID: {}, Total Amount: {}, Total Weight: {}", 
                 savedOrder.getOrderId(), savedOrder.getTotalAmount(), savedOrder.getTotalWeight());
        
        return mapToResponse(savedOrder);
    }
    
    /**
     * Get order by orderId
     * 
     * @param orderId External order ID
     * @return Order response DTO
     */
    @Transactional(readOnly = true)
    public OrderResponse getOrderByOrderId(String orderId) {
        Order order = orderRepository.findByOrderId(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found with ID: " + orderId));
        return mapToResponse(order);
    }
    
    private OrderResponse mapToResponse(Order order) {
        return OrderResponse.builder()
                .orderId(order.getOrderId())
                .sellerId(order.getSeller().getSellerId())
                .customerId(order.getCustomer().getCustomerId())
                .totalAmount(order.getTotalAmount())
                .totalWeight(order.getTotalWeight())
                .status(order.getStatus())
                .orderDate(order.getOrderDate())
                .items(order.getItems().stream().map(item -> OrderItemDTO.builder()
                        .productId(item.getProduct().getProductId())
                        .quantity(item.getQuantity())
                        .price(item.getPrice())
                        .weight(item.getWeight())
                        .build()).collect(Collectors.toList()))
                .build();
    }
}
