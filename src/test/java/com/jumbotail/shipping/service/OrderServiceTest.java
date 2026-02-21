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
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OrderServiceTest {

    @Mock
    private OrderRepository orderRepository;
    @Mock
    private CustomerRepository customerRepository;
    @Mock
    private SellerRepository sellerRepository;
    @Mock
    private ProductRepository productRepository;

    @InjectMocks
    private OrderService orderService;

    private Seller testSeller;
    private Customer testCustomer;
    private Product testProduct;

    @BeforeEach
    void setUp() {
        testSeller = Seller.builder().sellerId("123").name("Test Seller").build();
        testCustomer = Customer.builder().customerId("Cust-123").name("Test Customer").build();
        testProduct = Product.builder()
                .productId("456")
                .name("Test Product")
                .sellingPrice(100.0)
                .seller(testSeller)
                .attributes(new ProductAttributes(1.0, 10.0, 10.0, 10.0))
                .active(true)
                .build();
    }

    @Test
    void testCreateOrder_Success() {
        // Arrange
        OrderRequest request = new OrderRequest("123", "Cust-123", 
                Arrays.asList(new OrderItemDTO("456", 2, null, null)));

        when(sellerRepository.findBySellerIdAndActiveTrue("123")).thenReturn(Optional.of(testSeller));
        when(customerRepository.findByCustomerIdAndActiveTrue("Cust-123")).thenReturn(Optional.of(testCustomer));
        when(productRepository.findByProductIdAndActiveTrue("456")).thenReturn(Optional.of(testProduct));
        when(orderRepository.save(any(Order.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        OrderResponse response = orderService.createOrder(request);

        // Assert
        assertNotNull(response);
        assertEquals("123", response.getSellerId());
        assertEquals("Cust-123", response.getCustomerId());
        assertEquals(200.0, response.getTotalAmount());
        assertEquals(2.0, response.getTotalWeight());
        assertEquals(1, response.getItems().size());
        verify(orderRepository).save(any(Order.class));
    }

    @Test
    void testCreateOrder_SellerNotFound() {
        OrderRequest request = new OrderRequest("999", "Cust-123", Arrays.asList(new OrderItemDTO("456", 1, null, null)));
        when(sellerRepository.findBySellerIdAndActiveTrue("999")).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> orderService.createOrder(request));
    }

    @Test
    void testCreateOrder_ProductMismatchSeller() {
        // Product belongs to seller 123, but request is for seller 125
        OrderRequest request = new OrderRequest("125", "Cust-123", 
                Arrays.asList(new OrderItemDTO("456", 1, null, null)));
        
        Seller differentSeller = Seller.builder().sellerId("125").name("Seller 125").build();

        when(sellerRepository.findBySellerIdAndActiveTrue("125")).thenReturn(Optional.of(differentSeller));
        when(customerRepository.findByCustomerIdAndActiveTrue("Cust-123")).thenReturn(Optional.of(testCustomer));
        when(productRepository.findByProductIdAndActiveTrue("456")).thenReturn(Optional.of(testProduct));

        assertThrows(InvalidRequestException.class, () -> orderService.createOrder(request));
    }
}
