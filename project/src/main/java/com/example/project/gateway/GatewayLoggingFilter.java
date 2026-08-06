package com.example.project.gateway;

import org.springframework.stereotype.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.UUID;

/**
 * Gateway Logging Filter
 *
 * Logs all incoming requests and outgoing responses through the API Gateway.
 * Tracks request duration and identifies slow requests.
 */
@Component
public class GatewayLoggingFilter implements Filter {

    private static final Logger logger = LoggerFactory.getLogger(GatewayLoggingFilter.class);
    private static final long SLOW_REQUEST_THRESHOLD_MS = 5000; // 5 seconds

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, jakarta.servlet.ServletException {

        if (request instanceof HttpServletRequest && response instanceof HttpServletResponse) {
            HttpServletRequest httpRequest = (HttpServletRequest) request;
            HttpServletResponse httpResponse = (HttpServletResponse) response;

            String requestId = UUID.randomUUID().toString();
            String method = httpRequest.getMethod();
            String path = httpRequest.getRequestURI();
            String remoteAddr = httpRequest.getRemoteAddr();

            long startTime = System.currentTimeMillis();

            // Log incoming request
            logger.info("[GATEWAY REQUEST] ID: {} | Method: {} | Path: {} | RemoteAddr: {}",
                requestId, method, path, remoteAddr);

            try {
                chain.doFilter(request, response);
            } finally {
                long duration = System.currentTimeMillis() - startTime;
                int status = httpResponse.getStatus();

                if (duration > SLOW_REQUEST_THRESHOLD_MS) {
                    logger.warn("[GATEWAY SLOW REQUEST] ID: {} | Status: {} | Duration: {}ms | Path: {}",
                        requestId, status, duration, path);
                } else {
                    logger.info("[GATEWAY RESPONSE] ID: {} | Status: {} | Duration: {}ms | Path: {}",
                        requestId, status, duration, path);
                }
            }
        } else {
            chain.doFilter(request, response);
        }
    }
}



