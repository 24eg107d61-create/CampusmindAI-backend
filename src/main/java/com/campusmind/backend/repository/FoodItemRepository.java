
package com.campusmind.backend.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.campusmind.backend.entity.FoodItem;

public interface FoodItemRepository extends JpaRepository<FoodItem, Long> {

    List<FoodItem> findByCategoryIgnoreCase(String category);

    List<FoodItem> findByAvailable(Boolean available);

    boolean existsByNameIgnoreCase(String name);
}
