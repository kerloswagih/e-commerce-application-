package com.example.project.gateway;

import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.cloud.client.discovery.DiscoveryClient;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.util.List;

/**
 * Gateway Service Discovery Configuration
 *
 * This configuration class logs all discovered services at startup and provides
 * monitoring for the API Gateway's service discovery mechanism.
 *
 * Only activated when running with 'gateway' profile to avoid conflicts with
 * Eureka server mode.
 */
@Configuration
@Profile("gateway")
public class GatewayServiceDiscoveryConfig {

    private static final Logger logger = LoggerFactory.getLogger(GatewayServiceDiscoveryConfig.class);

    private final DiscoveryClient discoveryClient;

    public GatewayServiceDiscoveryConfig(DiscoveryClient discoveryClient) {
        this.discoveryClient = discoveryClient;
    }

    /**
     * Log all discovered services at application startup
     *
     * Added a delay to allow Eureka client to fully initialize before checking services.
     */
    @PostConstruct
    public void logDiscoveredServices() {
        // Add a small delay to allow Eureka client to initialize and register
        try {
            Thread.sleep(500);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        logger.info("========== API GATEWAY SERVICE DISCOVERY ==========");

        try {
            List<String> services = discoveryClient.getServices();

            if (services != null && !services.isEmpty()) {
                logger.info("Total Services Discovered: {}", services.size());

                for (String service : services) {
                    List<?> instances = discoveryClient.getInstances(service);
                    logger.info("Service: {} | Instances: {}", service, instances != null ? instances.size() : 0);

                    if (instances != null && !instances.isEmpty()) {
                        instances.forEach(instance ->
                            logger.debug("  Instance: {}", instance)
                        );
                    }
                }
                logger.info("========== END SERVICE DISCOVERY ==========");
            } else {
                logger.warn("No services discovered in Eureka registry yet. Services may still be registering.");
                logger.info("========== END SERVICE DISCOVERY ==========");
            }
        } catch (Exception e) {
            logger.error("Error during service discovery: {}", e.getMessage(), e);
            // Print full stack trace for debugging
            e.printStackTrace();
        }
    }
}




