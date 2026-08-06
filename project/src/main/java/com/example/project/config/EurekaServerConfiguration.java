package com.example.project.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.cloud.netflix.eureka.server.EnableEurekaServer;

/**
 * Enable Spring Cloud Eureka Server when running with profile 'eureka-server'.
 *
 * This configuration is only activated when the application is started with:
 * SPRING_PROFILES_ACTIVE=eureka-server
 *
 * This prevents Eureka Server from being enabled on microservices, which would
 * cause registry conflicts and prevent proper service discovery.
 */
@Configuration
@Profile("eureka-server")
@EnableEurekaServer
public class EurekaServerConfiguration {
    // Configuration is handled entirely by properties (application-eureka-server.properties)
}

