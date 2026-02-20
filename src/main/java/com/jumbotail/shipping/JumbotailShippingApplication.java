package com.jumbotail.shipping;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;

/**
 * Main Spring Boot Application for Jumbotail Shipping Charge Calculator
 * 
 * This application provides APIs to calculate shipping charges for a B2B e-commerce marketplace
 * that helps Kirana stores discover and order products.
 */
@SpringBootApplication
@EnableCaching
public class JumbotailShippingApplication {

    public static void main(String[] args) {
        SpringApplication.run(JumbotailShippingApplication.class, args);
    }
}
