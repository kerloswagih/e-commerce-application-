package com.example.project.gateway;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpStatus;
import org.springframework.web.client.RestTemplate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Integration tests for the gateway routes.
 *
 * These tests start the application on a random port with the 'gateway' profile
 * and use a MockRestServiceServer to stub the backend services the gateway
 * proxies to via the application's @LoadBalanced RestTemplate.
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
    properties = {"spring.profiles.active=gateway"})
public class GatewayRoutesIntegrationTest {

    @Autowired
    private TestRestTemplate testRestTemplate;

    @MockBean
    private RestTemplate restTemplate; // mock the RestTemplate used by the gateway so we can verify/exchange

    @Test
    public void authHealth_isProxiedToAuthService() {
        when(restTemplate.exchange(eq("http://auth-service/api/actuator/health"), eq(HttpMethod.GET), any(HttpEntity.class), eq(byte[].class)))
            .thenReturn(new org.springframework.http.ResponseEntity<>("{\"status\":\"UP\"}".getBytes(), HttpStatus.OK));

        ResponseEntity<String> resp = testRestTemplate.getForEntity("/api/auth/health", String.class);

        assertThat(resp.getStatusCode().is2xxSuccessful()).isTrue();
        assertThat(resp.getBody()).contains("UP");

        verify(restTemplate).exchange(eq("http://auth-service/api/actuator/health"), eq(HttpMethod.GET), any(HttpEntity.class), eq(byte[].class));
    }

    @Test
    public void shopPath_isRewrittenAndProxiedToShopService() {
        when(restTemplate.exchange(eq("http://shop-service/api/v1/shop/items/123"), eq(HttpMethod.GET), any(HttpEntity.class), eq(byte[].class)))
            .thenReturn(new org.springframework.http.ResponseEntity<> ("{\"id\":123,\"name\":\"item\"}".getBytes(), HttpStatus.OK));

        ResponseEntity<String> resp = testRestTemplate.getForEntity("/api/shop/items/123", String.class);

        assertThat(resp.getStatusCode().is2xxSuccessful()).isTrue();
        assertThat(resp.getBody()).contains("item");

        verify(restTemplate).exchange(eq("http://shop-service/api/v1/shop/items/123"), eq(HttpMethod.GET), any(HttpEntity.class), eq(byte[].class));
    }

    @Test
    public void walletPost_isProxiedToWalletService() {
        when(restTemplate.exchange(eq("http://wallet-service/api/v1/wallet/charge"), eq(HttpMethod.POST), any(HttpEntity.class), eq(byte[].class)))
            .thenReturn(new org.springframework.http.ResponseEntity<> (new byte[0], HttpStatus.CREATED));

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<String> request = new HttpEntity<>("{\"amount\":10}", headers);

        ResponseEntity<String> resp = testRestTemplate.postForEntity("/api/wallet/charge", request, String.class);

        assertThat(resp.getStatusCodeValue()).isEqualTo(201);

        verify(restTemplate).exchange(eq("http://wallet-service/api/v1/wallet/charge"), eq(HttpMethod.POST), any(HttpEntity.class), eq(byte[].class));
    }
}


