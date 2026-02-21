package com.jumbotail.shipping.service;

import com.jumbotail.shipping.model.Order;
import com.jumbotail.shipping.model.OrderItem;
import com.jumbotail.shipping.repository.InventoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class InventoryService {

    private final InventoryRepository inventoryRepository;

    /**
     * Checks if a warehouse has sufficient stock for ALL items in an order.
     * Caches the result to avoid redundant DB queries during the recommendation simulation loop.
     *
     * @param warehouseId the warehouse to check
     * @param order the order containing items to check
     * @return true if all items are in stock with sufficient quantity, false otherwise
     */
    public boolean hasSufficientStockForOrder(Long warehouseId, Order order) {
        for (OrderItem item : order.getItems()) {
            boolean hasStock = hasSufficientStock(warehouseId, item.getProduct().getId(), item.getQuantity());
            if (!hasStock) {
                return false;
            }
        }
        return true;
    }

    private boolean hasSufficientStock(Long warehouseId, Long productId, int requiredQuantity) {
        return inventoryRepository.findByWarehouseIdAndProductId(warehouseId, productId)
                .map(inventory -> inventory.getQuantity() >= requiredQuantity)
                .orElse(false);
    }
}
