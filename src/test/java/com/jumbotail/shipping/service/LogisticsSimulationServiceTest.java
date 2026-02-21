package com.jumbotail.shipping.service;

import com.jumbotail.shipping.dto.LogisticsSimulationRequest;
import com.jumbotail.shipping.dto.LogisticsSimulationResponse;
import com.jumbotail.shipping.dto.NearestWarehouseResponse;
import com.jumbotail.shipping.dto.TransportModeOption;
import com.jumbotail.shipping.exception.InvalidRequestException;
import com.jumbotail.shipping.model.*;
import com.jumbotail.shipping.repository.OrderRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class LogisticsSimulationServiceTest {

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private WarehouseService warehouseService;

    @InjectMocks
    private LogisticsSimulationService logisticsSimulationService;

    private Order mockOrder;
    private Warehouse mockWarehouse;

    @BeforeEach
    void setUp() {
        Seller seller = Seller.builder().sellerId("S1").build();
        Customer customer = Customer.builder()
                .customerId("C1")
                .location(new Location(10.0, 10.0))
                .build();
        Product product = Product.builder().productId("P1").build();
        
        // Make the weight extremely heavy (e.g. 10,000 kg) so that Aeroplane becomes absurdly expensive
        OrderItem item = OrderItem.builder().product(product).quantity(1).weight(10000.0).build();

        mockOrder = org.mockito.Mockito.mock(Order.class);
        org.mockito.Mockito.lenient().when(mockOrder.getOrderId()).thenReturn("ORD1");
        org.mockito.Mockito.lenient().when(mockOrder.getSeller()).thenReturn(seller);
        org.mockito.Mockito.lenient().when(mockOrder.getCustomer()).thenReturn(customer);
        org.mockito.Mockito.lenient().when(mockOrder.getItems()).thenReturn(List.of(item));
        org.mockito.Mockito.lenient().when(mockOrder.getTotalWeight()).thenReturn(10000.0);

        mockWarehouse = Warehouse.builder()
                .warehouseId("W1")
                .location(new Location(30.0, 30.0)) // Much longer distance ~3100km
                .build();
    }

    @Test
    void testSimulateLogistics_SpeedPriority() {
        when(orderRepository.findByOrderId("ORD1")).thenReturn(Optional.of(mockOrder));
        NearestWarehouseResponse nwr = NearestWarehouseResponse.builder().warehouseId("W1").build();
        when(warehouseService.findNearestWarehouse("S1", "P1")).thenReturn(nwr);
        when(warehouseService.getWarehouseByWarehouseId("W1")).thenReturn(mockWarehouse);

        LogisticsSimulationRequest req = new LogisticsSimulationRequest("ORD1", "speed");
        LogisticsSimulationResponse res = logisticsSimulationService.simulateLogistics(req);

        assertEquals("ORD1", res.getOrderId());
        assertEquals("speed", res.getPriority());
        assertEquals("Aeroplane", res.getRecommendedOption().getTransportMode());
    }

    @Test
    void testSimulateLogistics_CostPriority() {
        when(orderRepository.findByOrderId("ORD1")).thenReturn(Optional.of(mockOrder));
        NearestWarehouseResponse nwr = NearestWarehouseResponse.builder().warehouseId("W1").build();
        when(warehouseService.findNearestWarehouse("S1", "P1")).thenReturn(nwr);
        when(warehouseService.getWarehouseByWarehouseId("W1")).thenReturn(mockWarehouse);

        LogisticsSimulationRequest req = new LogisticsSimulationRequest("ORD1", "cost");
        LogisticsSimulationResponse res = logisticsSimulationService.simulateLogistics(req);

        assertEquals("ORD1", res.getOrderId());
        assertEquals("cost", res.getPriority());
        assertEquals("Aeroplane", res.getRecommendedOption().getTransportMode());
    }
    
    @Test
    void testSimulateLogistics_InvalidPriority() {
        LogisticsSimulationRequest req = new LogisticsSimulationRequest("ORD1", "invalid");
        assertThrows(InvalidRequestException.class, () -> logisticsSimulationService.simulateLogistics(req));
    }
}
