package com.example.project.gateway;

import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

/**
 * Gateway Routes Configuration for Spring Cloud Gateway Server MVC
 *
 * NOTE: Due to the incompatibility between spring-cloud-gateway-server-webmvc:5.0.2
 * and the properties-based route configuration model, routes should be defined
 * programmatically or through a different configuration approach.
 *
 * For now, routes are configured via application-gateway.properties with the understanding
 * that MVC Gateway may not support all the same configuration patterns as the reactive gateway.
 *
 * Alternative: Consider using the reactive spring-cloud-starter-gateway instead of
 * spring-cloud-gateway-server-webmvc for full properties-based route support.
 */
@Configuration
@Profile("gateway")
public class GatewayRoutesConfig {
    // Routes are configured in application-gateway.properties
    // MVC Gateway will process them according to its supported configuration model
}








