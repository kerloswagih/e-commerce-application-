package com.example.project.config;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.cloud.client.loadbalancer.LoadBalanced;
import org.springframework.cloud.loadbalancer.annotation.LoadBalancerClient;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.web.client.RestTemplate;

/**
 * API Gateway Configuration
 *
 * This configuration is activated when running with profile 'gateway'.
 * It provides load-balanced REST template for the gateway to communicate with services.
 */
@Configuration
@Profile("gateway")
public class GatewayConfiguration {

    /**
     * Load-balanced RestTemplate for inter-service communication
     * Uses Spring Cloud LoadBalancer with Eureka service discovery
     */
    @Bean
    @LoadBalanced
    public RestTemplate restTemplate() {
        return new RestTemplate();
    }
}

