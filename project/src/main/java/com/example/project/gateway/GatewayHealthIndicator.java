package com.example.project.gateway;

import org.springframework.cloud.client.discovery.DiscoveryClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.http.ResponseEntity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Gateway Health Controller
 *
 * Monitors the health of the API Gateway and its upstream services.
 * Reports service registration status and availability.
 */
@RestController
@RequestMapping("/api/gateway/health")
public class GatewayHealthIndicator {

    private static final Logger logger = LoggerFactory.getLogger(GatewayHealthIndicator.class);

    private final DiscoveryClient discoveryClient;

    public GatewayHealthIndicator(DiscoveryClient discoveryClient) {
        this.discoveryClient = discoveryClient;
    }

    /**
     * Get gateway health status including all registered services
     */
    @GetMapping
    public ResponseEntity<?> health() {
        try {
            List<String> services = discoveryClient.getServices();

            if (services == null || services.isEmpty()) {
                logger.warn("No services registered with Eureka");
                return ResponseEntity.ok(buildHealthResponse("DOWN", "No services discovered", services, null));
            }

            Map<String, Object> serviceHealth = new HashMap<>();

            for (String service : services) {
                List<?> instances = discoveryClient.getInstances(service);
                Map<String, Object> serviceInfo = new HashMap<>();
                serviceInfo.put("instances", instances != null ? instances.size() : 0);
                serviceInfo.put("status", instances != null && !instances.isEmpty() ? "UP" : "DOWN");
                serviceHealth.put(service, serviceInfo);
            }

            logger.debug("Gateway health check complete. Services: {}", services.size());

            return ResponseEntity.ok(buildHealthResponse("UP", "Gateway is healthy", services, serviceHealth));

        } catch (Exception e) {
            logger.error("Error checking gateway health", e);
            return ResponseEntity.status(500).body(Map.of(
                "status", "DOWN",
                "error", e.getMessage()
            ));
        }
    }

    private Map<String, Object> buildHealthResponse(String status, String message, List<String> services, Map<String, Object> serviceHealth) {
        Map<String, Object> response = new HashMap<>();
        response.put("status", status);
        response.put("message", message);
        response.put("registeredServices", services != null ? services.size() : 0);
        if (serviceHealth != null) {
            response.put("services", serviceHealth);
        }
        return response;
    }
}


