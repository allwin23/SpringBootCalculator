package com.jumbotail.shipping.config;

import com.jumbotail.shipping.model.*;
import com.jumbotail.shipping.repository.CustomerRepository;
import com.jumbotail.shipping.repository.ProductRepository;
import com.jumbotail.shipping.repository.SellerRepository;
import com.jumbotail.shipping.repository.WarehouseRepository;
import com.jumbotail.shipping.repository.OrderRepository;
import com.jumbotail.shipping.repository.InventoryRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import java.util.List;
import java.time.LocalDateTime;

/**
 * Data initializer to populate sample data for testing
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class DataInitializer implements CommandLineRunner {
    
    private final CustomerRepository customerRepository;
    private final SellerRepository sellerRepository;
    private final ProductRepository productRepository;
    private final WarehouseRepository warehouseRepository;
    private final OrderRepository orderRepository;
    private final InventoryRepository inventoryRepository;
    
    @Override
    public void run(String... args) {
        log.info("Initializing sample data...");
        
        // Initialize Customers
        initializeCustomers();
        
        // Initialize Warehouses
        initializeWarehouses();
        
        // Initialize Sellers and Products
        initializeSellersAndProducts();
        
        // Initialize Orders
        initializeOrders();
        
        // Initialize Inventory
        initializeInventory();
        
        log.info("Sample data initialization completed");
    }
    
    private void initializeCustomers() {
        if (customerRepository.count() == 0) {
            Customer customer1 = Customer.builder()
                    .customerId("Cust-123")
                    .name("Shree Kirana Store")
                    .phoneNumber("9847123456")
                    .location(new Location(11.232, 23.445495))
                    .active(true)
                    .build();
            
            Customer customer2 = Customer.builder()
                    .customerId("Cust-124")
                    .name("Andheri Mini Mart")
                    .phoneNumber("9101234567")
                    .location(new Location(17.232, 33.445495))
                    .active(true)
                    .build();
            
            customerRepository.save(customer1);
            customerRepository.save(customer2);
            log.info("Initialized {} customers", customerRepository.count());
        }
    }
    
    private void initializeWarehouses() {
        if (warehouseRepository.count() == 0) {
            Warehouse warehouse1 = Warehouse.builder()
                    .warehouseId("789")
                    .name("BLR_Warehouse")
                    .location(new Location(12.99999, 37.923273))
                    .active(true)
                    .build();
            
            Warehouse warehouse2 = Warehouse.builder()
                    .warehouseId("790")
                    .name("MUMB_Warehouse")
                    .location(new Location(11.99999, 27.923273))
                    .active(true)
                    .build();
            
            warehouseRepository.save(warehouse1);
            warehouseRepository.save(warehouse2);
            log.info("Initialized {} warehouses", warehouseRepository.count());
        }
    }
    
    private void initializeSellersAndProducts() {
        if (sellerRepository.count() == 0) {
            // Nestle Seller
            Seller nestleSeller = Seller.builder()
                    .sellerId("123")
                    .name("Nestle Seller")
                    .location(new Location(12.5, 37.5)) // Near BLR warehouse
                    .active(true)
                    .build();
            nestleSeller = sellerRepository.save(nestleSeller);
            
            Product maggie = Product.builder()
                    .productId("456")
                    .name("Maggie 500g Packet")
                    .sellingPrice(10.0)
                    .seller(nestleSeller)
                    .attributes(new ProductAttributes(0.5, 10.0, 10.0, 10.0))
                    .active(true)
                    .build();
            productRepository.save(maggie);
            
            // Rice Seller
            Seller riceSeller = Seller.builder()
                    .sellerId("124")
                    .name("Rice Seller")
                    .location(new Location(11.5, 27.5)) // Near MUMB warehouse
                    .active(true)
                    .build();
            riceSeller = sellerRepository.save(riceSeller);
            
            Product riceBag = Product.builder()
                    .productId("457")
                    .name("Rice Bag 10Kg")
                    .sellingPrice(500.0)
                    .seller(riceSeller)
                    .attributes(new ProductAttributes(10.0, 1000.0, 800.0, 500.0))
                    .active(true)
                    .build();
            productRepository.save(riceBag);
            
            // Sugar Seller
            Seller sugarSeller = Seller.builder()
                    .sellerId("125")
                    .name("Sugar Seller")
                    .location(new Location(13.0, 38.0)) // Near BLR warehouse
                    .active(true)
                    .build();
            sugarSeller = sellerRepository.save(sugarSeller);
            
            Product sugarBag = Product.builder()
                    .productId("458")
                    .name("Sugar Bag 25kg")
                    .sellingPrice(700.0)
                    .seller(sugarSeller)
                    .attributes(new ProductAttributes(25.0, 1000.0, 900.0, 600.0))
                    .active(true)
                    .build();
            productRepository.save(sugarBag);
            
            log.info("Initialized {} sellers and {} products", 
                     sellerRepository.count(), productRepository.count());
        }
    }
    
    private void initializeOrders() {
        if (orderRepository.count() == 0) {
            Customer c1 = customerRepository.findByCustomerIdAndActiveTrue("Cust-123").orElse(null);
            Seller s1 = sellerRepository.findBySellerIdAndActiveTrue("123").orElse(null); // Nestle
            Product p1 = productRepository.findByProductIdAndActiveTrue("456").orElse(null); // Maggie
            
            if (c1 != null && s1 != null && p1 != null) {
                Order order1 = Order.builder()
                    .orderId("ORD-001")
                    .customer(c1)
                    .seller(s1)
                    .status(OrderStatus.CREATED)
                    .orderDate(LocalDateTime.now())
                    .totalAmount(100.0)
                    .totalWeight(5.0)
                    .build();
                    
                OrderItem item1 = OrderItem.builder()
                    .order(order1)
                    .product(p1)
                    .quantity(10)
                    .price(10.0)
                    .weight(0.5)
                    .build();
                    
                order1.setItems(List.of(item1));
                orderRepository.save(order1);
                
                Product p2 = productRepository.findByProductIdAndActiveTrue("457").orElse(null); // Rice
                if (p2 != null) {
                    Order order2 = Order.builder()
                        .orderId("ORD-002")
                        .customer(c1)
                        .seller(s1)
                        .status(OrderStatus.CREATED)
                        .orderDate(LocalDateTime.now())
                        .totalAmount(500.0)
                        .totalWeight(10.0)
                        .build();
                        
                    OrderItem item2 = OrderItem.builder()
                        .order(order2)
                        .product(p2)
                        .quantity(1)
                        .price(500.0)
                        .weight(10.0)
                        .build();
                        
                    order2.setItems(List.of(item2));
                    orderRepository.save(order2);
                }
                
                log.info("Initialized {} orders", orderRepository.count());
            }
        }
    }

    private void initializeInventory() {
        if (inventoryRepository.count() == 0) {
            Warehouse w1 = warehouseRepository.findByWarehouseIdAndActiveTrue("789").orElse(null);
            Warehouse w2 = warehouseRepository.findByWarehouseIdAndActiveTrue("790").orElse(null);
            
            Product p1 = productRepository.findByProductIdAndActiveTrue("456").orElse(null); // Maggie
            Product p2 = productRepository.findByProductIdAndActiveTrue("457").orElse(null); // Rice
            Product p3 = productRepository.findByProductIdAndActiveTrue("458").orElse(null); // Sugar
            
            if (w1 != null && w2 != null && p1 != null && p2 != null && p3 != null) {
                // BLR Warehouse Inventory (has everything)
                inventoryRepository.save(WarehouseInventory.builder()
                        .warehouseId(w1.getId())
                        .productId(p1.getId())
                        .quantity(500)
                        .build());
                        
                inventoryRepository.save(WarehouseInventory.builder()
                        .warehouseId(w1.getId())
                        .productId(p2.getId())
                        .quantity(200)
                        .build());
                        
                inventoryRepository.save(WarehouseInventory.builder()
                        .warehouseId(w1.getId())
                        .productId(p3.getId())
                        .quantity(20)
                        .build());
                        
                // MUMB Warehouse Inventory (only has Rice and Sugar, explicitly out of Maggie)
                inventoryRepository.save(WarehouseInventory.builder()
                        .warehouseId(w2.getId())
                        .productId(p1.getId())
                        .quantity(0) // Out of stock on purpose
                        .build());
                        
                inventoryRepository.save(WarehouseInventory.builder()
                        .warehouseId(w2.getId())
                        .productId(p2.getId())
                        .quantity(50)
                        .build());
                        
                inventoryRepository.save(WarehouseInventory.builder()
                        .warehouseId(w2.getId())
                        .productId(p3.getId())
                        .quantity(50)
                        .build());
                        
                log.info("Initialized {} warehouse inventory records", inventoryRepository.count());
            }
        }
    }
}
