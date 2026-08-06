package com.example.project.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.cloud.config.server.EnableConfigServer;

/**
 * Enable Spring Cloud Config Server when running with profile 'config-server'.
 */
@Configuration
@Profile("config-server")
@EnableConfigServer
public class ConfigServerConfiguration {
    // Configuration is handled entirely by properties
}

