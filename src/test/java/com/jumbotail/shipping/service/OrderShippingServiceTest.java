package com.jumbotail.shipping.service;

import com.jumbotail.shipping.dto.LocationDTO;
import com.jumbotail.shipping.dto.NearestWarehouseResponse;
import com.jumbotail.shipping.dto.OrderShippingEstimateResponse;
import com.jumbotail.shipping.model.*;
import com.jumbotail.shipping.repository.OrderRepository;
import com.jumbotail.shipping.strategy.DeliverySpeed;
import com.jumbotail.shipping.strategy.TransportMode;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collections;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OrderShippingServiceTest {

    @Mock
    private OrderRepository orderRepository;
    @Mock
    private WarehouseService warehouseService;
    @Mock
    private ShippingMetricsService shippingMetricsService;

    @InjectMocks
    private OrderShippingService orderShippingService;

    private Order testOrder;
    private Warehouse testWarehouse;

    @BeforeEach
    void setUp() {
        Seller seller = Seller.builder().sellerId("S1").location(new Location(10.0, 10.0)).build();
        Customer customer = Customer.builder().customerId("C1").location(new Location(10.5, 10.5)).build();
        Product product = Product.builder().productId("P1").seller(seller).build();
        
        OrderItem item = OrderItem.builder().product(product).quantity(1).build();
        
        testOrder = Order.builder()
                .orderId("ORD1")
                .seller(seller)
                .customer(customer)
                .items(Collections.singletonList(item))
                .totalWeight(10.0)
                .totalAmount(100.0)
                .status(OrderStatus.CREATED)
                .build();
        
        testWarehouse = Warehouse.builder()
                .warehouseId("WH1")
                .location(new Location(10.1, 10.1))
                .active(true)
                .build();
    }

    @Test
    void testGetShippingEstimate_Success() {
        // Arrange
        when(orderRepository.findByOrderId("ORD1")).thenReturn(Optional.of(testOrder));
        when(warehouseService.findNearestWarehouse("S1", "P1"))
                .thenReturn(new NearestWarehouseResponse("WH1", new LocationDTO(10.1, 10.1)));
        when(warehouseService.getWarehouseByWarehouseId("WH1")).thenReturn(testWarehouse);

        /**
         * Logic check:
         * Distance WH(10.1, 10.1) to C(10.5, 10.5) is approx 62.9 km
         * Mode: MINI_VAN (0-100km, speed 40km/h, rate 3.0)
         * Standard Speed: handling 24h, factor 1.2
         * 
         * Base Charge: 62.9 * 10 * 3.0 = 1887.0
         * Speed Charge: 10.0 + (0.0 * 10) = 10.0
         * Total: 1897.0
         * 
         * Time: 24 + (62.9 / 40 * 1.2) = 24 + 1.887 = 25.887 -> 25.9
         */

        // Act
        OrderShippingEstimateResponse response = orderShippingService.getShippingEstimate("ORD1", "standard");

        // Assert
        assertNotNull(response);
        assertEquals("ORD1", response.getOrderId());
        assertEquals(10.0, response.getTotalWeight());
        assertEquals("Mini Van", response.getTransportMode());
        assertEquals("WH1", response.getWarehouseId());
        assertTrue(response.getDistanceKm() > 0);
        assertTrue(response.getShippingCharge() > 0);
        assertEquals("standard", response.getDeliverySpeed());
        assertTrue(response.getEstimatedDeliveryHours() > 24);
    }
}
