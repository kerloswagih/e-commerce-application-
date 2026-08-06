package com.example.project.shop.controller;

import com.example.project.shop.dto.*;
import com.example.project.shop.service.ShopService;
import com.example.project.exception.ApiResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/v1/shop")
@RequiredArgsConstructor
@Slf4j
public class ShopController {

    private final ShopService shopService;

    @PostMapping("/products")
    public ResponseEntity<?> createProduct(@RequestBody ShopProductRequestDTO dto) {
        try {
            ShopProductResponseDTO created = shopService.createProduct(dto);
            return ResponseEntity.ok(created);
        } catch (RuntimeException ex) {
            log.error("Error creating product: {}", ex.getMessage(), ex);
            ApiResponse errorResponse = new ApiResponse(null, ex.getMessage(), HttpStatus.BAD_REQUEST.value());
            return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
        }
    }

    @GetMapping("/products")
    public ResponseEntity<?> listProducts() {
        try {
            return ResponseEntity.ok(shopService.listActiveProducts());
        } catch (RuntimeException ex) {
            log.error("Error listing products: {}", ex.getMessage(), ex);
            ApiResponse errorResponse = new ApiResponse(null, ex.getMessage(), HttpStatus.BAD_REQUEST.value());
            return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
        }
    }

    @GetMapping("/products/{id}")
    public ResponseEntity<?> getProduct(@PathVariable Long id) {
        try {
            return ResponseEntity.ok(shopService.getProduct(id));
        } catch (RuntimeException ex) {
            log.error("Error getting product: {}", ex.getMessage(), ex);
            ApiResponse errorResponse = new ApiResponse(null, ex.getMessage(), HttpStatus.BAD_REQUEST.value());
            return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
        }
    }

    @PostMapping("/cart/add")
    public ResponseEntity<?> addToCart(@RequestBody CartItemRequestDTO dto) {
        try {
            return ResponseEntity.ok(shopService.addToCart(dto));
        } catch (RuntimeException ex) {
            log.error("Error adding to cart: {}", ex.getMessage(), ex);
            ApiResponse errorResponse = new ApiResponse(null, ex.getMessage(), HttpStatus.BAD_REQUEST.value());
            return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
        }
    }

    @GetMapping("/cart/user/{userId}")
    public ResponseEntity<?> getCartByUser(@PathVariable Long userId) {
        try {
            return ResponseEntity.ok(shopService.listCartByUser(userId));
        } catch (RuntimeException ex) {
            log.error("Error getting cart: {}", ex.getMessage(), ex);
            ApiResponse errorResponse = new ApiResponse(null, ex.getMessage(), HttpStatus.BAD_REQUEST.value());
            return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
        }
    }

    @PostMapping("/cart/remove")
    public ResponseEntity<?> removeFromCart(@RequestBody CartItemRequestDTO dto) {
        try {
            shopService.removeFromCart(dto);
            return ResponseEntity.ok().build();
        } catch (RuntimeException ex) {
            log.error("Error removing from cart: {}", ex.getMessage(), ex);
            ApiResponse errorResponse = new ApiResponse(null, ex.getMessage(), HttpStatus.BAD_REQUEST.value());
            return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
        }
    }

    @PostMapping("/checkout")
    public ResponseEntity<?> checkout(@RequestBody CheckoutRequestDTO dto) {
        try {
            log.info("Checkout API: Processing checkout for userId: {}", dto.getUserId());
            CheckoutResponseDTO response = shopService.checkout(dto);
            return ResponseEntity.ok(response);
        } catch (RuntimeException ex) {
            log.error("Checkout error: {}", ex.getMessage(), ex);
            ApiResponse errorResponse = new ApiResponse(
                    null,
                    ex.getMessage(),
                    HttpStatus.BAD_REQUEST.value()
            );
            return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
        }
    }

    @GetMapping("/orders/{id}")
    public ResponseEntity<OrderResponseDTO> getOrder(@PathVariable Long id) {
        return ResponseEntity.ok(shopService.getOrder(id));
    }

    @GetMapping("/orders/{id}/items")
    public ResponseEntity<List<OrderItemDTO>> getOrderItems(@PathVariable Long id) {
        return ResponseEntity.ok(shopService.getOrderItems(id));
    }

    @GetMapping("/orders/user/{userId}")
    public ResponseEntity<List<OrderResponseDTO>> getUserOrders(@PathVariable Long userId) {
        return ResponseEntity.ok(shopService.getUserOrders(userId));
    }
}

