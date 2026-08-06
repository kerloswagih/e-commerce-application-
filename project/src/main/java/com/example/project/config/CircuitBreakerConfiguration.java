package com.example.project.config;

import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import io.github.resilience4j.circuitbreaker.CircuitBreakerConfig;
import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Duration;

/**
 * Configuration for Resilience4j Circuit Breaker.
 * Creates and registers circuit breakers for inter-service communication.
 */
@Slf4j
@Configuration
public class CircuitBreakerConfiguration {

    /**
     * Configure circuit breaker registry with default settings
     */
    @Bean
    public CircuitBreakerRegistry circuitBreakerRegistry() {
        return CircuitBreakerRegistry.ofDefaults();
    }

    /**
     * Create circuit breaker for User Service (Auth Service)
     */
    @Bean
    public CircuitBreaker userServiceClientCircuitBreaker(CircuitBreakerRegistry registry) {
        CircuitBreakerConfig config = CircuitBreakerConfig.custom()
                .slidingWindowSize(5)
                .failureRateThreshold(50)
                .slowCallRateThreshold(50)
                .slowCallDurationThreshold(Duration.ofSeconds(1))
                .waitDurationInOpenState(Duration.ofSeconds(10))
                .permittedNumberOfCallsInHalfOpenState(3)
                .automaticTransitionFromOpenToHalfOpenEnabled(true)
                .recordExceptions(Exception.class)
                .build();

        CircuitBreaker circuitBreaker = registry.circuitBreaker("userServiceClient", config);
        log.info("Initialized circuit breaker: userServiceClient");
        return circuitBreaker;
    }

    /**
     * Create circuit breaker for Inventory Service
     */
    @Bean
    public CircuitBreaker inventoryServiceClientCircuitBreaker(CircuitBreakerRegistry registry) {
        CircuitBreakerConfig config = CircuitBreakerConfig.custom()
                .slidingWindowSize(5)
                .failureRateThreshold(50)
                .slowCallRateThreshold(50)
                .slowCallDurationThreshold(Duration.ofSeconds(1))
                .waitDurationInOpenState(Duration.ofSeconds(10))
                .permittedNumberOfCallsInHalfOpenState(3)
                .automaticTransitionFromOpenToHalfOpenEnabled(true)
                .recordExceptions(Exception.class)
                .build();

        CircuitBreaker circuitBreaker = registry.circuitBreaker("inventoryServiceClient", config);
        log.info("Initialized circuit breaker: inventoryServiceClient");
        return circuitBreaker;
    }

    /**
     * Create circuit breaker for Wallet Service
     */
    @Bean
    public CircuitBreaker walletServiceClientCircuitBreaker(CircuitBreakerRegistry registry) {
        CircuitBreakerConfig config = CircuitBreakerConfig.custom()
                .slidingWindowSize(5)
                .failureRateThreshold(50)
                .slowCallRateThreshold(50)
                .slowCallDurationThreshold(Duration.ofSeconds(1))
                .waitDurationInOpenState(Duration.ofSeconds(10))
                .permittedNumberOfCallsInHalfOpenState(3)
                .automaticTransitionFromOpenToHalfOpenEnabled(true)
                .recordExceptions(Exception.class)
                .build();

        CircuitBreaker circuitBreaker = registry.circuitBreaker("walletServiceClient", config);
        log.info("Initialized circuit breaker: walletServiceClient");
        return circuitBreaker;
    }
}




