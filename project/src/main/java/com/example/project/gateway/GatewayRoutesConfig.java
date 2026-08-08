package com.example.project.gateway;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.servlet.function.HandlerFunction;
import org.springframework.web.servlet.function.RequestPredicates;
import org.springframework.web.servlet.function.RouterFunction;
import org.springframework.web.servlet.function.RouterFunctions;
import org.springframework.web.servlet.function.ServerRequest;
import org.springframework.web.servlet.function.ServerResponse;

import java.util.List;

/**
 * Gateway Routes Configuration for servlet-based gateway profile
 *
 * This configuration registers a set of RouterFunctions that proxy incoming
 * requests to backend services using the application's @LoadBalanced
 * RestTemplate. Using a RestTemplate-based proxy avoids compile-time
 * dependencies on the reactive RouteLocator types which are not present
 * when the servlet gateway starter is used in some setups.
 */
@Configuration
@Profile("gateway")
public class GatewayRoutesConfig {

    private static final Logger logger = LoggerFactory.getLogger(GatewayRoutesConfig.class);

    @Bean
    public RouterFunction<ServerResponse> gatewayRoutes(RestTemplate restTemplate) {
        // Create handler factory for proxied routes
        HandlerFunction<ServerResponse> authHealthHandler = proxyHandler(restTemplate,
            "/api/auth", "/api/actuator/health", "auth-service", true);

        HandlerFunction<ServerResponse> shopHandler = proxyHandler(restTemplate,
            "/api/shop", "/api/v1/shop", "shop-service", false);

        HandlerFunction<ServerResponse> authHandler = proxyHandler(restTemplate,
            "/api/auth", "/api/v1/auth", "auth-service", false);

        HandlerFunction<ServerResponse> walletHandler = proxyHandler(restTemplate,
            "/api/wallet", "/api/v1/wallet", "wallet-service", false);

        HandlerFunction<ServerResponse> inventoryHandler = proxyHandler(restTemplate,
            "/api/inventory", "/api/v1/inventory", "inventory-service", false);

        HandlerFunction<ServerResponse> usersHandler = proxyHandler(restTemplate,
            "/api/users", "/api/v1/users", "auth-service", false);

        HandlerFunction<ServerResponse> transactionsHandler = proxyHandler(restTemplate,
            "/api/transactions", "/api/v1/transactions", "wallet-service", false);

        HandlerFunction<ServerResponse> eurekaHandler = proxyHandler(restTemplate,
            "/eureka", "/eureka", "eureka-server", false);

        return RouterFunctions
            .route(RequestPredicates.path("/api/auth/health"), authHealthHandler)
            .andRoute(RequestPredicates.path("/api/shop/**"), shopHandler)
            .andRoute(RequestPredicates.path("/api/auth/**"), authHandler)
            .andRoute(RequestPredicates.path("/api/wallet/**"), walletHandler)
            .andRoute(RequestPredicates.path("/api/inventory/**"), inventoryHandler)
            .andRoute(RequestPredicates.path("/api/users/**"), usersHandler)
            .andRoute(RequestPredicates.path("/api/transactions/**"), transactionsHandler)
            .andRoute(RequestPredicates.path("/eureka/**"), eurekaHandler);
    }

    /**
     * Factory method that returns a HandlerFunction which proxies the incoming request
     * to the configured backend service using RestTemplate. If `fixedTargetPath` is true
     * the backend path will be used literally (useful for health mapping), otherwise the
     * original request suffix is appended to the target base path.
     */
    private HandlerFunction<ServerResponse> proxyHandler(RestTemplate restTemplate,
                                                         String incomingPrefix,
                                                         String targetBasePath,
                                                         String serviceId,
                                                         boolean fixedTargetPath) {
        return (ServerRequest request) -> {
            try {
                String incomingPath = request.path(); // e.g. /api/shop/items/1

                String suffix = "";
                if (!fixedTargetPath) {
                    if (incomingPath.length() > incomingPrefix.length()) {
                        suffix = incomingPath.substring(incomingPrefix.length());
                    }
                }

                String query = request.uri().getQuery();
                String target = "http://" + serviceId + (targetBasePath.endsWith("/") ? targetBasePath.substring(0, targetBasePath.length()-1) : targetBasePath) + (suffix.isEmpty() ? "" : suffix) + (query != null ? "?" + query : "");

                HttpHeaders headers = new HttpHeaders();
                request.headers().asHttpHeaders().forEach((k, v) -> headers.put(k, List.copyOf(v)));

                // Read body as byte[] if present
                byte[] body;
                try {
                    body = request.body(byte[].class);
                    if (body == null) body = new byte[0];
                } catch (Exception ex) {
                    // no body or cannot read as bytes
                    body = new byte[0];
                }

                HttpEntity<byte[]> entity = new HttpEntity<>(body, headers);
                HttpMethod method = request.method();
                ResponseEntity<byte[]> resp = restTemplate.exchange(target, method, entity, byte[].class);

                ServerResponse.BodyBuilder builder = ServerResponse.status(resp.getStatusCode().value());
                resp.getHeaders().forEach((k, v) -> builder.header(k, v.toArray(new String[0])));
                byte[] responseBody = resp.getBody() == null ? new byte[0] : resp.getBody();
                return builder.body(responseBody);

            } catch (Exception e) {
                logger.error("Error proxying request to backend service", e);
                return ServerResponse.status(502).body(("Error proxying request: " + e.getMessage()).getBytes());
            }
        };
    }
}








