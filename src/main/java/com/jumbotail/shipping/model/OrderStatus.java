package com.jumbotail.shipping.model;

/**
 * Status of an order in the system
 */
public enum OrderStatus {
    CREATED,
    PENDING,
    PROCESSING,
    SHIPPED,
    DELIVERED,
    CANCELLED
}
