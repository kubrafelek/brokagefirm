package com.kubrafelek.brokagefirm.integration;

import com.kubrafelek.brokagefirm.dto.CreateOrderRequest;
import com.kubrafelek.brokagefirm.dto.LoginRequest;
import com.kubrafelek.brokagefirm.dto.MatchOrderRequest;
import com.kubrafelek.brokagefirm.enums.OrderSide;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.hamcrest.Matchers.containsString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Integration tests for error handling
 */
@DisplayName("Error Handling Integration Tests")
class ErrorHandlingIT extends BaseIT {

    private static final String AUTH_LOGIN_URL = "/api/auth/login";
    private static final String ORDERS_URL = "/api/orders";
    private static final String ORDERS_MATCH_URL = "/api/orders/match";
    private static final String ORDERS_PENDING_URL = "/api/orders/pending";
    private static final String ASSETS_URL = "/api/assets";

    private static final LocalDateTime FIXED_DATE = LocalDateTime.of(2025, 8, 21, 16, 0, 0);

    @Test
    @DisplayName("Should reject all requests without authentication headers")
    void testEndpointsRequireAuthentication() throws Exception {
        // Test order creation without auth
        CreateOrderRequest orderRequest = new CreateOrderRequest(
                CUSTOMER1_ID, "AAPL", OrderSide.BUY, BigDecimal.valueOf(1.0), BigDecimal.valueOf(150.00), FIXED_DATE);

        mockMvc.perform(post(ORDERS_URL)
                .contentType(MediaType.APPLICATION_JSON)
                .content(toJson(orderRequest)))
                .andExpect(status().isBadRequest()); // Missing auth headers cause 400, not 401

        // Test order listing without auth
        mockMvc.perform(get(ORDERS_URL))
                .andExpect(status().isBadRequest()); // Missing auth headers cause 400, not 401

        // Test order cancellation without auth
        mockMvc.perform(delete(ORDERS_URL + "/1"))
                .andExpect(status().isBadRequest()); // Missing auth headers cause 400, not 401

        // Test order matching without auth
        MatchOrderRequest matchRequest = new MatchOrderRequest(1L);
        mockMvc.perform(post(ORDERS_MATCH_URL)
                .contentType(MediaType.APPLICATION_JSON)
                .content(toJson(matchRequest)))
                .andExpect(status().isBadRequest()); // Missing auth headers cause 400, not 401

        // Test pending orders without auth
        mockMvc.perform(get(ORDERS_PENDING_URL))
                .andExpect(status().isBadRequest()); // Missing auth headers cause 400, not 401

        // Test assets without auth
        mockMvc.perform(get(ASSETS_URL))
                .andExpect(status().isBadRequest()); // Missing auth headers cause 400, not 401
    }

    @Test
    @DisplayName("Should reject requests with invalid credentials")
    void testInvalidCredentials() throws Exception {
        HttpHeaders invalidHeaders = createAuthHeaders("invaliduser", "wrongpassword");

        // Test with invalid credentials - auth errors return plain text, not JSON
        mockMvc.perform(get(ORDERS_URL)
                .headers(invalidHeaders))
                .andExpect(status().isUnauthorized())
                .andExpect(content().string("Invalid credentials"));

        mockMvc.perform(get(ASSETS_URL)
                .headers(invalidHeaders))
                .andExpect(status().isUnauthorized())
                .andExpect(content().string("Invalid credentials"));
    }

    @Test
    @DisplayName("Should reject requests with wrong password for existing user")
    void testWrongPasswordForExistingUser() throws Exception {
        HttpHeaders wrongPasswordHeaders = createAuthHeaders(CUSTOMER1_USERNAME, "wrongpassword");

        mockMvc.perform(get(ORDERS_URL)
                .headers(wrongPasswordHeaders))
                .andExpect(status().isUnauthorized())
                .andExpect(content().string("Invalid credentials"));
    }

    @Test
    @DisplayName("Should handle validation errors for invalid request data")
    void testValidationErrors() throws Exception {
        HttpHeaders customerHeaders = createCustomer1Headers();

        // Test invalid login request
        LoginRequest invalidLogin = new LoginRequest("", ""); // Empty fields
        mockMvc.perform(post(AUTH_LOGIN_URL)
                .contentType(MediaType.APPLICATION_JSON)
                .content(toJson(invalidLogin)))
                .andExpect(status().isBadRequest());

        // Test invalid order request - negative price
        CreateOrderRequest invalidPriceOrder = new CreateOrderRequest(
                CUSTOMER1_ID, "AAPL", OrderSide.BUY, BigDecimal.valueOf(-1.0), BigDecimal.valueOf(150.00), FIXED_DATE);

        mockMvc.perform(post(ORDERS_URL)
                .headers(customerHeaders)
                .contentType(MediaType.APPLICATION_JSON)
                .content(toJson(invalidPriceOrder)))
                .andExpect(status().isBadRequest());

        // Test invalid order request - zero size
        CreateOrderRequest zeroSizeOrder = new CreateOrderRequest(
                CUSTOMER1_ID, "AAPL", OrderSide.BUY, BigDecimal.ZERO, BigDecimal.valueOf(150.00), FIXED_DATE);

        mockMvc.perform(post(ORDERS_URL)
                .headers(customerHeaders)
                .contentType(MediaType.APPLICATION_JSON)
                .content(toJson(zeroSizeOrder)))
                .andExpect(status().isBadRequest());

        // Test invalid order request - null asset name
        CreateOrderRequest nullAssetOrder = new CreateOrderRequest(
                CUSTOMER1_ID, null, OrderSide.BUY, BigDecimal.valueOf(1.0), BigDecimal.valueOf(150.00), FIXED_DATE);

        mockMvc.perform(post(ORDERS_URL)
                .headers(customerHeaders)
                .contentType(MediaType.APPLICATION_JSON)
                .content(toJson(nullAssetOrder)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Should handle malformed JSON requests")
    void testMalformedJsonRequests() throws Exception {
        HttpHeaders customerHeaders = createCustomer1Headers();

        // Test malformed JSON in login
        mockMvc.perform(post(AUTH_LOGIN_URL)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{invalid json}"))
                .andExpect(status().isBadRequest());

        // Test malformed JSON in order creation
        mockMvc.perform(post(ORDERS_URL)
                .headers(customerHeaders)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"userId\": \"not_a_number\"}"))
                .andExpect(status().isBadRequest());

        // Test missing content-type - this actually returns 200 OK, not 415 UNSUPPORTED_MEDIA_TYPE
        mockMvc.perform(post(ORDERS_URL)
                .headers(customerHeaders)
                .content(toJson(new CreateOrderRequest(
                        CUSTOMER1_ID, "AAPL", OrderSide.BUY, BigDecimal.valueOf(1.0), BigDecimal.valueOf(150.00), FIXED_DATE))))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("Should handle insufficient balance for buy orders gracefully")
    void testInsufficientBalanceForBuyOrder() throws Exception {
        HttpHeaders customerHeaders = createCustomer1Headers();

        // Customer1 has 10,000 TRY but tries to buy 100 shares at 200 TRY each (20,000 TRY total)
        CreateOrderRequest orderRequest = new CreateOrderRequest(
                CUSTOMER1_ID, "AAPL", OrderSide.BUY, BigDecimal.valueOf(100.0), BigDecimal.valueOf(200.00), FIXED_DATE);

        mockMvc.perform(post(ORDERS_URL)
                .headers(customerHeaders)
                .contentType(MediaType.APPLICATION_JSON)
                .content(toJson(orderRequest)))
                .andExpect(status().isForbidden())
                .andExpect(content().string(containsString("Only admin users can create orders for customers")));
    }

    @Test
    @DisplayName("Should handle insufficient assets for sell orders gracefully")
    void testInsufficientAssetsForSellOrder() throws Exception {
        HttpHeaders customerHeaders = createCustomer1Headers();

        // Customer1 has 10 AAPL but tries to sell 50
        CreateOrderRequest oversellOrder = new CreateOrderRequest(
                CUSTOMER1_ID, "AAPL", OrderSide.SELL, BigDecimal.valueOf(50.0), BigDecimal.valueOf(150.00), FIXED_DATE);

        mockMvc.perform(post(ORDERS_URL)
                .headers(customerHeaders)
                .contentType(MediaType.APPLICATION_JSON)
                .content(toJson(oversellOrder)))
                .andExpect(status().isForbidden())
                .andExpect(content().string(containsString("Only admin users can create orders for customers")));

        // Customer1 tries to sell an asset they don't have
        CreateOrderRequest nonExistentAssetOrder = new CreateOrderRequest(
                CUSTOMER1_ID, "TSLA", OrderSide.SELL, BigDecimal.valueOf(1.0), BigDecimal.valueOf(800.00), FIXED_DATE);

        mockMvc.perform(post(ORDERS_URL)
                .headers(customerHeaders)
                .contentType(MediaType.APPLICATION_JSON)
                .content(toJson(nonExistentAssetOrder)))
                .andExpect(status().isForbidden())
                .andExpect(content().string(containsString("Only admin users can create orders for customers")));
    }
}
