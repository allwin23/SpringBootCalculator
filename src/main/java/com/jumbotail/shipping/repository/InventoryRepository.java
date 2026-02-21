package com.jumbotail.shipping.repository;

import com.jumbotail.shipping.model.WarehouseInventory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface InventoryRepository extends JpaRepository<WarehouseInventory, Long> {
    
    Optional<WarehouseInventory> findByWarehouseIdAndProductId(Long warehouseId, Long productId);
    
}
