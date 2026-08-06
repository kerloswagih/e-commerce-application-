package com.example.project.shop.service;

import com.example.project.shop.dto.*;
import com.example.project.shop.entity.*;
import com.example.project.shop.repository.*;
import com.example.project.repository.UserRepository;
import com.example.project.entity.User;
import com.example.project.feign.UserServiceClient;
import com.example.project.feign.InventoryServiceClient;
import com.example.project.feign.WalletServiceClient;
import com.example.project.inventory.dto.ReserveRequestDTO;
import com.example.project.wallet.dto.TransactionResponseDTO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class ShopService {

    private final ShopProductRepository shopProductRepository;
    private final CartItemRepository cartItemRepository;
    private final OrderRepository orderRepository;
    private final com.example.project.shop.repository.OrderItemRepository orderItemRepository;
    private final UserRepository userRepository;
    private final UserServiceClient userServiceClient;
    private final InventoryServiceClient inventoryServiceClient;
    private final WalletServiceClient walletServiceClient;

    public ShopProductResponseDTO createProduct(ShopProductRequestDTO dto) {
        ShopProduct p = new ShopProduct();
        p.setInventoryProductId(dto.getInventoryProductId());
        p.setTitle(dto.getTitle());
        p.setPrice(dto.getPrice());
        p.setIsActive(true);
        ShopProduct saved = shopProductRepository.save(p);
        return new ShopProductResponseDTO(saved.getId(), saved.getInventoryProductId(), saved.getTitle(), saved.getPrice(), saved.getIsActive());
    }

    public ShopProductResponseDTO getProduct(Long id) {
        ShopProduct p = shopProductRepository.findById(id).orElseThrow(() -> new RuntimeException("Product not found: " + id));
        return new ShopProductResponseDTO(p.getId(), p.getInventoryProductId(), p.getTitle(), p.getPrice(), p.getIsActive());
    }

    public List<ShopProductResponseDTO> listActiveProducts() {
        return shopProductRepository.findByIsActiveTrue().stream()
                .map(p -> new ShopProductResponseDTO(p.getId(), p.getInventoryProductId(), p.getTitle(), p.getPrice(), p.getIsActive()))
                .collect(Collectors.toList());
    }

    @Transactional
    public CartItemResponseDTO addToCart(CartItemRequestDTO dto) {
        CartItem item = new CartItem();
        item.setUserId(dto.getUserId());
        item.setProductId(dto.getShopProductId());
        item.setQuantity(dto.getQuantity() != null ? dto.getQuantity() : 1);
        CartItem saved = cartItemRepository.save(item);
        return new CartItemResponseDTO(saved.getId(), saved.getProductId(), saved.getQuantity());
    }

    public List<CartItemResponseDTO> listCartByUser(Long userId) {
        return cartItemRepository.findByUserId(userId).stream()
                .map(ci -> new CartItemResponseDTO(ci.getId(), ci.getProductId(), ci.getQuantity()))
                .collect(Collectors.toList());
    }

    @Transactional
    public void removeFromCart(CartItemRequestDTO dto) {
        cartItemRepository.deleteById(dto.getId());
    }

    public OrderResponseDTO getOrder(Long orderId) {
        OrderEntity order = orderRepository.findById(orderId).orElseThrow(() -> new RuntimeException("Order not found: " + orderId));
        return new OrderResponseDTO(order.getId(), order.getUserId(), order.getTotalAmount(), order.getStatus(), order.getCreatedAt());
    }

    public List<OrderItemDTO> getOrderItems(Long orderId) {
        return orderItemRepository.findByOrderId(orderId).stream()
                .map(oi -> new OrderItemDTO(oi.getId(), oi.getOrder().getId(), oi.getProductId(), oi.getQuantity(), oi.getUnitPrice()))
                .collect(Collectors.toList());
    }

    public List<OrderResponseDTO> getUserOrders(Long userId) {
        return orderRepository.findByUserId(userId).stream()
                .map(o -> new OrderResponseDTO(o.getId(), o.getUserId(), o.getTotalAmount(), o.getStatus(), o.getCreatedAt()))
                .collect(Collectors.toList());
    }

    @Transactional
    public CheckoutResponseDTO checkout(CheckoutRequestDTO dto) {
        Long userId = dto.getUserId();

        log.info("Checkout initiated for userId: {}", userId);

        // STEP 1: Verify user exists via Auth Service Feign
        try {
            ResponseEntity<?> userResponse = userServiceClient.getUserById(userId);
            if (!userResponse.getStatusCode().is2xxSuccessful()) {
                log.error("User verification failed for userId: {}", userId);
                throw new RuntimeException("Unable to verify user with id: " + userId);
            }
            log.info("User verified successfully: {}", userId);
        } catch (Exception ex) {
            log.error("Error verifying user: {}", userId, ex);
            throw new RuntimeException("Unable to verify user with id: " + userId);
        }

        // STEP 2: Get cart items
        List<CartItem> items = cartItemRepository.findByUserId(userId);
        if (items.isEmpty()) {
            log.warn("Cart is empty for userId: {}", userId);
            throw new RuntimeException("Cart is empty");
        }
        log.info("Cart has {} items for userId: {}", items.size(), userId);

        // STEP 3: Create order and prepare for inventory reservation
        OrderEntity order = new OrderEntity();
        order.setUserId(userId);
        BigDecimal total = BigDecimal.ZERO;
        List<OrderItem> orderItems = new java.util.ArrayList<>();
        List<ReservationInfo> reservations = new java.util.ArrayList<>();

        for (CartItem ci : items) {
            ShopProduct sp = shopProductRepository.findById(ci.getProductId())
                    .orElseThrow(() -> new RuntimeException("Product not found: " + ci.getProductId()));

            // STEP 4: Check inventory and reserve stock via Inventory Service Feign
            try {
                ReserveRequestDTO reserveRequest = new ReserveRequestDTO();
                reserveRequest.setQuantity(ci.getQuantity());

                ResponseEntity<?> reserveResponse = inventoryServiceClient.reserve(
                        sp.getInventoryProductId(),
                        reserveRequest
                );

                if (!reserveResponse.getStatusCode().is2xxSuccessful()) {
                    log.error("Inventory reservation failed for productId: {}, quantity: {}",
                            sp.getInventoryProductId(), ci.getQuantity());
                    throw new RuntimeException("Unable to reserve inventory for product: " + sp.getInventoryProductId());
                }

                log.info("Inventory reserved: productId={}, quantity={}",
                        sp.getInventoryProductId(), ci.getQuantity());

                // Track reservation for potential rollback
                reservations.add(new ReservationInfo(sp.getInventoryProductId(), ci.getQuantity()));
            } catch (Exception ex) {
                log.error("Error reserving inventory: {}", sp.getInventoryProductId(), ex);
                // Release already-reserved items
                releaseReservations(reservations);
                throw new RuntimeException("Unable to reserve inventory for product: " + sp.getInventoryProductId());
            }

            // Create order item
            OrderItem oi = new OrderItem();
            oi.setProductId(sp.getId());
            oi.setUnitPrice(sp.getPrice());
            oi.setQuantity(ci.getQuantity());
            orderItems.add(oi);
            total = total.add(sp.getPrice().multiply(BigDecimal.valueOf(ci.getQuantity())));
        }

        order.setTotalAmount(total);
        for (OrderItem oi : orderItems) {
            oi.setOrder(order);
        }
        order.setItems(orderItems);
        order.setStatus("PENDING");

        // STEP 5: Create payment transaction in Wallet Service via Feign
        try {
            // FIX: Look up wallet ID by user ID
            ResponseEntity<com.example.project.wallet.dto.WalletResponseDTO> walletResponse = walletServiceClient.getWalletByUserId(userId);
            if (!walletResponse.getStatusCode().is2xxSuccessful() || walletResponse.getBody() == null) {
                log.error("Wallet not found for userId: {}", userId);
                releaseReservations(reservations);
                throw new RuntimeException("Wallet not found for user: " + userId);
            }
            Long walletId = walletResponse.getBody().getId();

            ResponseEntity<TransactionResponseDTO> paymentResponse = walletServiceClient.createPayment(
                    walletId,
                    total,
                    "ORDER-" + System.currentTimeMillis()
            );

            if (!paymentResponse.getStatusCode().is2xxSuccessful()) {
                log.error("Payment creation failed for walletId: {}, amount: {}", walletId, total);
                releaseReservations(reservations);
                throw new RuntimeException("Unable to create payment transaction");
            }

            log.info("Payment transaction created successfully for walletId: {}, amount: {}", walletId, total);
        } catch (Exception ex) {
            log.error("Error creating payment: {}", userId, ex);
            releaseReservations(reservations);
            throw new RuntimeException("Unable to process payment: " + ex.getMessage());
        }

        // STEP 6: Save order
        OrderEntity saved = orderRepository.save(order);

        for (OrderItem oi : orderItems) {
            oi.setOrder(saved);
            orderItemRepository.save(oi);
        }

        // STEP 7: Clear cart
        cartItemRepository.deleteByUserId(userId);

        log.info("Checkout completed successfully. OrderId: {}, UserId: {}, Total: {}",
                saved.getId(), userId, saved.getTotalAmount());

        return new CheckoutResponseDTO(saved.getId(), saved.getStatus(), saved.getTotalAmount());
    }

    /**
     * Helper method to release inventory reservations in case of checkout failure
     */
    private void releaseReservations(List<ReservationInfo> reservations) {
        for (ReservationInfo reservation : reservations) {
            try {
                ReserveRequestDTO releaseRequest = new ReserveRequestDTO();
                releaseRequest.setQuantity(reservation.quantity);

                inventoryServiceClient.release(reservation.productId, releaseRequest);
                log.info("Released inventory reservation: productId={}, quantity={}",
                        reservation.productId, reservation.quantity);
            } catch (Exception ex) {
                log.error("Failed to release inventory reservation: {}", reservation.productId, ex);
                // Log but don't throw - best effort cleanup
            }
        }
    }

    /**
     * Helper class to track inventory reservations
     */
    private static class ReservationInfo {
        Long productId;
        Integer quantity;

        ReservationInfo(Long productId, Integer quantity) {
            this.productId = productId;
            this.quantity = quantity;
        }
    }
}




