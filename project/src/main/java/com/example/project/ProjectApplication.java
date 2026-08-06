package com.example.project;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import com.netflix.discovery.shared.transport.jersey3.Jersey3TransportClientFactories;
import org.springframework.cloud.openfeign.EnableFeignClients;

/**
 * Main Spring Boot Application
 *
 * Annotations:
 * - @SpringBootApplication: Enables auto-configuration, component scanning
 * - @EnableDiscoveryClient: Enables service registration/discovery with Eureka
 * - @EnableFeignClients: Enables Feign declarative HTTP client for inter-service communication
 *
 * Profile-Specific Configurations:
 * - Eureka Server: Use profile "eureka-server" for the Eureka registry server
 * - Gateway: Use profile "gateway" for the API Gateway
 * - Auth Service: Use profile "auth" for the authentication service
 * - Inventory Service: Use profile "inventory" for the inventory service
 * - Wallet Service: Use profile "wallet" for the wallet service
 * - Shop Service: Use profile "shop" for the shop service
 *
 * Example startup:
 * $env:SPRING_PROFILES_ACTIVE = "eureka-server"; java -jar project-0.0.1-SNAPSHOT.jar
 */
@SpringBootApplication
@EnableDiscoveryClient
@EnableFeignClients
public class ProjectApplication {

	public static void main(String[] args) {
		SpringApplication.run(ProjectApplication.class, args);
	}

	@Configuration
	static class EurekaConfiguration {
		@Bean
		public Jersey3TransportClientFactories jersey3TransportClientFactories() {
			return new Jersey3TransportClientFactories();
		}
	}
}

