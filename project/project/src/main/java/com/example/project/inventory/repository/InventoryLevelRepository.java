package com.example.project.inventory.repository;

import com.example.project.inventory.entity.InventoryLevel;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface InventoryLevelRepository extends JpaRepository<InventoryLevel, Long> {
    Optional<InventoryLevel> findByProductId(Long productId);
}

