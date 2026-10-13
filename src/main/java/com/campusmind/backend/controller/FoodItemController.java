
package com.campusmind.backend.controller;

import java.util.List;
import java.util.Optional;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.campusmind.backend.entity.FoodItem;
import com.campusmind.backend.repository.FoodItemRepository;

@RestController
@RequestMapping("/api/canteen/items")
@CrossOrigin(origins = "http://localhost:5173")
public class FoodItemController {

    private final FoodItemRepository foodItemRepository;

    public FoodItemController(FoodItemRepository foodItemRepository) {
        this.foodItemRepository = foodItemRepository;
    }

    // Get all food items
    @GetMapping
    public List<FoodItem> getAllItems() {
        return foodItemRepository.findAll();
    }

    // Get one food item by ID
    @GetMapping("/{id}")
    public ResponseEntity<FoodItem> getItemById(@PathVariable Long id) {
        return foodItemRepository.findById(id)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    // Add a food item
    @PostMapping
    public ResponseEntity<?> addItem(@RequestBody FoodItem foodItem) {
        if (foodItem.getName() == null || foodItem.getName().isBlank()
                || foodItem.getCategory() == null
                || foodItem.getCategory().isBlank()
                || foodItem.getPrice() == null
                || foodItem.getPrice().signum() <= 0
                || foodItem.getAvailable() == null) {
            return ResponseEntity.badRequest()
                    .body("Enter a valid name, category, positive price, and availability.");
        }

        FoodItem savedItem = foodItemRepository.save(foodItem);

        return ResponseEntity.status(HttpStatus.CREATED).body(savedItem);
    }

    // Update a food item
    @PutMapping("/{id}")
    public ResponseEntity<?> updateItem(
            @PathVariable Long id,
            @RequestBody FoodItem updatedItem) {

        Optional<FoodItem> existingItem =
                foodItemRepository.findById(id);

        if (existingItem.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        if (updatedItem.getName() == null
                || updatedItem.getName().isBlank()
                || updatedItem.getCategory() == null
                || updatedItem.getCategory().isBlank()
                || updatedItem.getPrice() == null
                || updatedItem.getPrice().signum() <= 0
                || updatedItem.getAvailable() == null) {
            return ResponseEntity.badRequest()
                    .body("Enter valid food item details.");
        }

        FoodItem item = existingItem.get();
        item.setName(updatedItem.getName());
        item.setCategory(updatedItem.getCategory());
        item.setPrice(updatedItem.getPrice());
        item.setAvailable(updatedItem.getAvailable());

        return ResponseEntity.ok(foodItemRepository.save(item));
    }

    // Delete a food item
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteItem(@PathVariable Long id) {
        if (!foodItemRepository.existsById(id)) {
            return ResponseEntity.notFound().build();
        }

        foodItemRepository.deleteById(id);
        return ResponseEntity.noContent().build();
    }

    // Filter food items by category
    @GetMapping("/category/{category}")
    public List<FoodItem> getItemsByCategory(
            @PathVariable String category) {
        return foodItemRepository.findByCategoryIgnoreCase(category);
    }

    // Get available or unavailable items
    @GetMapping("/availability/{available}")
    public List<FoodItem> getItemsByAvailability(
            @PathVariable Boolean available) {
        return foodItemRepository.findByAvailable(available);
    }
}
