package com.example.project.config;

import feign.Logger;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Configuration for Feign Clients
 * Enables detailed logging and resilience patterns for inter-service communication
 */
@Configuration
public class FeignClientConfiguration {

    /**
     * Set Feign logging level to FULL for debugging
     * Logs request/response headers, body, and metadata
     */
    @Bean
    Logger.Level feignLoggerLevel() {
        return Logger.Level.FULL;
    }
}

