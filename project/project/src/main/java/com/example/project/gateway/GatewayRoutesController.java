package com.example.project.gateway;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Gateway Routes Controller
 *
 * Provides endpoints to expose gateway configuration and routes.
 * This serves as a REST endpoint for viewing gateway route configuration
 * that was loaded from application-gateway.properties.
 */
@RestController
@RequestMapping("/api/gateway")
public class GatewayRoutesController {

    private static final Logger logger = LoggerFactory.getLogger(GatewayRoutesController.class);

    /**
     * Get all configured gateway routes (from properties file configuration)
     */
    @GetMapping("/routes")
    public ResponseEntity<?> getRoutes() {
        try {
            List<Map<String, Object>> routes = Arrays.asList(
                createRoute("auth-health-route", "lb://auth-service", "/api/auth/health", "/api/actuator/health"),
                createRoute("shop-route", "lb://shop-service", "/api/shop/**", "/api/v1/shop/"),
                createRoute("auth-route", "lb://auth-service", "/api/auth/**", "/api/v1/auth/"),
                createRoute("wallet-route", "lb://wallet-service", "/api/wallet/**", "/api/v1/wallet/"),
                createRoute("inventory-route", "lb://inventory-service", "/api/inventory/**", "/api/v1/inventory/"),
                createRoute("eureka-route", "lb://eureka-server", "/eureka/**", "")
            );

            Map<String, Object> response = new HashMap<>();
            response.put("message", "Successfully retrieved gateway routes");
            response.put("routeCount", routes.size());
            response.put("routes", routes);

            logger.debug("Retrieved {} routes", routes.size());
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            logger.error("Error retrieving gateway routes", e);
            return ResponseEntity.status(500).body(Map.of(
                "error", "Error retrieving gateway routes: " + e.getMessage()
            ));
        }
    }

    /**
     * Helper method to create route info
     */
    private Map<String, Object> createRoute(String id, String uri, String predicate, String rewritePath) {
        Map<String, Object> route = new HashMap<>();
        route.put("id", id);
        route.put("uri", uri);
        route.put("predicates", Arrays.asList(Map.of("name", "Path", "args", predicate)));
        if (!rewritePath.isEmpty()) {
            route.put("filters", Arrays.asList(Map.of("name", "RewritePath", "args", rewritePath)));
        }
        return route;
    }

    /**
     * Get gateway configuration summary
     */
    @GetMapping("/config")
    public ResponseEntity<?> getConfig() {
        Map<String, Object> config = new HashMap<>();
        config.put("port", 8084);
        config.put("serviceId", "gateway");
        config.put("eurekaMappings", Map.of(
            "auth-service", "8080",
            "wallet-service", "8081",
            "inventory-service", "8082",
            "shop-service", "8083"
        ));
        config.put("status", "UP");
        return ResponseEntity.ok(config);
    }
}



