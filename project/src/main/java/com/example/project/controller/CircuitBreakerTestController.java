package com.example.project.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry;
import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import org.springframework.beans.factory.annotation.Autowired;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.Optional;

/**
 * Test controller for simulating circuit breaker failures and monitoring.
 * This controller simulates Feign client calls to demonstrate circuit breaker behavior.
 */
@Slf4j
@RestController
@RequestMapping("/v1/circuit-breaker-test")
public class CircuitBreakerTestController {

    @Autowired(required = false)
    private Optional<CircuitBreakerRegistry> circuitBreakerRegistry = Optional.empty();

    private static volatile boolean simulateFailure = false;
    private static final AtomicInteger failureCount = new AtomicInteger(0);

    /**
     * Simulate a call to User Service (Auth Service)
     * Returns success if simulateFailure is false, throws exception if true
     * This call is wrapped through the circuit breaker to track metrics
     */
    @GetMapping("/user-service/{userId}")
    public Map<String, Object> callUserService(@PathVariable Long userId) {
        log.info("Calling user service for userId: {}", userId);

        // Execute through circuit breaker to record metrics
        if (circuitBreakerRegistry.isPresent()) {
            Optional<CircuitBreaker> cbOptional = circuitBreakerRegistry.get().find("userServiceClient");
            if (cbOptional.isPresent()) {
                return cbOptional.get().executeSupplier(() -> {
                    if (simulateFailure) {
                        failureCount.incrementAndGet();
                        log.error("Simulating failure #{} for user service", failureCount.get());
                        throw new RuntimeException("Simulated user service failure");
                    }

                    Map<String, Object> response = new HashMap<>();
                    response.put("userId", userId);
                    response.put("email", "user" + userId + "@example.com");
                    response.put("firstName", "User");
                    response.put("lastName", "Test");
                    response.put("active", true);

                    log.info("User service call successful for userId: {}", userId);
                    return response;
                });
            }
        }

        // Fallback if circuit breaker registry not available
        throw new RuntimeException("Circuit breaker registry not available");
    }

    /**
     * Simulate a call to Inventory Service
     * Returns success if simulateFailure is false, throws exception if true
     * This call is wrapped through the circuit breaker to track metrics
     */
    @PostMapping("/inventory-service/reserve")
    public Map<String, Object> callInventoryService(@RequestParam Long productId, @RequestParam Integer quantity) {
        log.info("Calling inventory service to reserve productId: {}, quantity: {}", productId, quantity);

        // Execute through circuit breaker to record metrics
        if (circuitBreakerRegistry.isPresent()) {
            Optional<CircuitBreaker> cbOptional = circuitBreakerRegistry.get().find("inventoryServiceClient");
            if (cbOptional.isPresent()) {
                return cbOptional.get().executeSupplier(() -> {
                    if (simulateFailure) {
                        failureCount.incrementAndGet();
                        log.error("Simulating failure #{} for inventory service", failureCount.get());
                        throw new RuntimeException("Simulated inventory service failure");
                    }

                    Map<String, Object> response = new HashMap<>();
                    response.put("productId", productId);
                    response.put("quantity", quantity);
                    response.put("reserved", true);
                    response.put("timestamp", System.currentTimeMillis());

                    log.info("Inventory service call successful");
                    return response;
                });
            }
        }

        // Fallback if circuit breaker registry not available
        throw new RuntimeException("Circuit breaker registry not available");
    }

    /**
     * Simulate a call to Wallet Service
     * Returns success if simulateFailure is false, throws exception if true
     * This call is wrapped through the circuit breaker to track metrics
     */
    @PostMapping("/wallet-service/payment")
    public Map<String, Object> callWalletService(@RequestParam Long userId, @RequestParam Double amount) {
        log.info("Calling wallet service for userId: {}, amount: {}", userId, amount);

        // Execute through circuit breaker to record metrics
        if (circuitBreakerRegistry.isPresent()) {
            Optional<CircuitBreaker> cbOptional = circuitBreakerRegistry.get().find("walletServiceClient");
            if (cbOptional.isPresent()) {
                return cbOptional.get().executeSupplier(() -> {
                    if (simulateFailure) {
                        failureCount.incrementAndGet();
                        log.error("Simulating failure #{} for wallet service", failureCount.get());
                        throw new RuntimeException("Simulated wallet service failure");
                    }

                    Map<String, Object> response = new HashMap<>();
                    response.put("userId", userId);
                    response.put("amount", amount);
                    response.put("transactionId", "TXN-" + System.currentTimeMillis());
                    response.put("status", "PENDING");

                    log.info("Wallet service call successful");
                    return response;
                });
            }
        }

        // Fallback if circuit breaker registry not available
        throw new RuntimeException("Circuit breaker registry not available");
    }

    /**
     * Enable fault simulation
     */
    @PostMapping("/simulate-failure/enable")
    public Map<String, String> enableFailureSimulation() {
        simulateFailure = true;
        failureCount.set(0);
        log.warn("FAULT SIMULATION ENABLED - All service calls will fail");

        Map<String, String> response = new HashMap<>();
        response.put("status", "Failure simulation enabled");
        response.put("message", "All subsequent calls will fail. This will trigger circuit breaker.");
        return response;
    }

    /**
     * Disable fault simulation
     */
    @PostMapping("/simulate-failure/disable")
    public Map<String, String> disableFailureSimulation() {
        simulateFailure = false;
        log.info("FAULT SIMULATION DISABLED - Service calls will succeed");

        Map<String, String> response = new HashMap<>();
        response.put("status", "Failure simulation disabled");
        response.put("message", "Service calls will now succeed. Circuit breaker will recover.");
        return response;
    }

    /**
     * Get current circuit breaker statuses
     */
    @GetMapping("/status")
    public Map<String, Object> getCircuitBreakerStatus() {
        log.info("Fetching circuit breaker status");

        Map<String, Object> status = new HashMap<>();
        status.put("simulateFailure", simulateFailure);
        status.put("failureCount", failureCount.get());

        if (circuitBreakerRegistry.isPresent()) {
            Map<String, Object> circuitBreakers = new HashMap<>();
            CircuitBreakerRegistry registry = circuitBreakerRegistry.get();

            // Get status of each circuit breaker
            registry.find("userServiceClient").ifPresent(userServiceCB -> {
                var metrics = userServiceCB.getMetrics();
                circuitBreakers.put("userServiceClient", Map.of(
                    "state", userServiceCB.getState().toString(),
                    "failureRate", String.format("%.2f%%", metrics.getFailureRate())
                ));
            });

            registry.find("inventoryServiceClient").ifPresent(inventoryCB -> {
                var metrics = inventoryCB.getMetrics();
                circuitBreakers.put("inventoryServiceClient", Map.of(
                    "state", inventoryCB.getState().toString(),
                    "failureRate", String.format("%.2f%%", metrics.getFailureRate())
                ));
            });

            registry.find("walletServiceClient").ifPresent(walletCB -> {
                var metrics = walletCB.getMetrics();
                circuitBreakers.put("walletServiceClient", Map.of(
                    "state", walletCB.getState().toString(),
                    "failureRate", String.format("%.2f%%", metrics.getFailureRate())
                ));
            });

            status.put("circuitBreakers", circuitBreakers);
        } else {
            status.put("circuitBreakers", "No circuit breakers registered");
        }
        return status;
    }

    /**
     * Reset all circuit breaker metrics
     */
    @PostMapping("/reset")
    public Map<String, String> resetCircuitBreakers() {
        log.info("Resetting all circuit breakers");
        simulateFailure = false;
        failureCount.set(0);

        if (circuitBreakerRegistry.isPresent()) {
            circuitBreakerRegistry.get().getAllCircuitBreakers().forEach(cb -> {
                cb.reset();
                log.info("Reset circuit breaker: {}", cb.getName());
            });
        }

        Map<String, String> response = new HashMap<>();
        response.put("status", "All circuit breakers reset");
        return response;
    }

    /**
     * Get detailed metrics endpoint
     */
    @GetMapping("/metrics")
    public Map<String, Object> getMetrics() {
        log.info("Fetching detailed metrics");

        Map<String, Object> metrics = new HashMap<>();
        if (circuitBreakerRegistry.isPresent()) {
            Map<String, Object> circuitBreakerMetrics = new HashMap<>();

            circuitBreakerRegistry.get().getAllCircuitBreakers().forEach(cb -> {
                var cbMetrics = cb.getMetrics();
                circuitBreakerMetrics.put(cb.getName(), Map.of(
                    "state", cb.getState().toString(),
                    "failureRate", String.format("%.2f%%", cbMetrics.getFailureRate())
                ));
            });

            metrics.put("circuitBreakers", circuitBreakerMetrics);
        } else {
            metrics.put("circuitBreakers", "No circuit breakers registered");
        }
        return metrics;
    }
}




















