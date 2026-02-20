package com.jumbotail.shipping.repository;

import com.jumbotail.shipping.model.Warehouse;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface WarehouseRepository extends JpaRepository<Warehouse, Long> {
    Optional<Warehouse> findByWarehouseId(String warehouseId);
    List<Warehouse> findByActiveTrue();
    Optional<Warehouse> findByWarehouseIdAndActiveTrue(String warehouseId);
}
