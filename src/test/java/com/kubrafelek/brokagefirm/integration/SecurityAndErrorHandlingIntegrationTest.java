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

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Integration tests for security and error handling
 */
@DisplayName("Security and Error Handling Integration Tests")
class SecurityAndErrorHandlingIntegrationTest extends BaseIntegrationTest {

    private static final String AUTH_LOGIN_URL = "/api/auth/login";
    private static final String ORDERS_URL = "/api/orders";
    private static final String ORDERS_MATCH_URL = "/api/orders/match";
    private static final String ORDERS_PENDING_URL = "/api/orders/pending";
    private static final String ASSETS_URL = "/api/assets";

    @Test
    @DisplayName("Should reject all requests without authentication headers")
    void testEndpointsRequireAuthentication() throws Exception {
        // Test order creation without auth
        CreateOrderRequest orderRequest = new CreateOrderRequest(
                CUSTOMER1_ID, "AAPL", OrderSide.BUY, BigDecimal.valueOf(1.0), BigDecimal.valueOf(150.00));

        mockMvc.perform(post(ORDERS_URL)
                .contentType(MediaType.APPLICATION_JSON)
                .content(toJson(orderRequest)))
                .andExpect(status().isUnauthorized());

        // Test order listing without auth
        mockMvc.perform(get(ORDERS_URL))
                .andExpect(status().isUnauthorized());

        // Test order cancellation without auth
        mockMvc.perform(delete(ORDERS_URL + "/1"))
                .andExpect(status().isUnauthorized());

        // Test order matching without auth
        MatchOrderRequest matchRequest = new MatchOrderRequest(1L);
        mockMvc.perform(post(ORDERS_MATCH_URL)
                .contentType(MediaType.APPLICATION_JSON)
                .content(toJson(matchRequest)))
                .andExpect(status().isUnauthorized());

        // Test pending orders without auth
        mockMvc.perform(get(ORDERS_PENDING_URL))
                .andExpect(status().isUnauthorized());

        // Test assets without auth
        mockMvc.perform(get(ASSETS_URL))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("Should reject requests with invalid credentials")
    void testInvalidCredentials() throws Exception {
        HttpHeaders invalidHeaders = createAuthHeaders("invaliduser", "wrongpassword");

        // Test with invalid credentials
        mockMvc.perform(get(ORDERS_URL)
                .headers(invalidHeaders))
                .andExpect(status().isUnauthorized())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.error").value("Invalid credentials"));

        mockMvc.perform(get(ASSETS_URL)
                .headers(invalidHeaders))
                .andExpect(status().isUnauthorized())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.error").value("Invalid credentials"));
    }

    @Test
    @DisplayName("Should reject requests with wrong password for existing user")
    void testWrongPasswordForExistingUser() throws Exception {
        HttpHeaders wrongPasswordHeaders = createAuthHeaders(CUSTOMER1_USERNAME, "wrongpassword");

        mockMvc.perform(get(ORDERS_URL)
                .headers(wrongPasswordHeaders))
                .andExpect(status().isUnauthorized())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.error").value("Invalid credentials"));
    }

    @Test
    @DisplayName("Should enforce customer access restrictions")
    void testCustomerAccessRestrictions() throws Exception {
        HttpHeaders customer1Headers = createCustomer1Headers();
        HttpHeaders customer2Headers = createCustomer2Headers();

        // Customer1 trying to create order for customer2
        CreateOrderRequest orderForOtherCustomer = new CreateOrderRequest(
                CUSTOMER2_ID, "AAPL", OrderSide.BUY, BigDecimal.valueOf(1.0), BigDecimal.valueOf(150.00));

        mockMvc.perform(post(ORDERS_URL)
                .headers(customer1Headers)
                .contentType(MediaType.APPLICATION_JSON)
                .content(toJson(orderForOtherCustomer)))
                .andExpect(status().isForbidden())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.error").value("You can only create orders for yourself"));

        // Customer trying to match orders
        MatchOrderRequest matchRequest = new MatchOrderRequest(1L);
        mockMvc.perform(post(ORDERS_MATCH_URL)
                .headers(customer1Headers)
                .contentType(MediaType.APPLICATION_JSON)
                .content(toJson(matchRequest)))
                .andExpect(status().isForbidden())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.error").value("Only admin users can match orders"));

        // Customer trying to view pending orders
        mockMvc.perform(get(ORDERS_PENDING_URL)
                .headers(customer1Headers))
                .andExpect(status().isForbidden())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.error").value("Only admin users can view all pending orders"));
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
                CUSTOMER1_ID, "AAPL", OrderSide.BUY, BigDecimal.valueOf(-1.0), BigDecimal.valueOf(150.00));

        mockMvc.perform(post(ORDERS_URL)
                .headers(customerHeaders)
                .contentType(MediaType.APPLICATION_JSON)
                .content(toJson(invalidPriceOrder)))
                .andExpect(status().isBadRequest());

        // Test invalid order request - zero size
        CreateOrderRequest zeroSizeOrder = new CreateOrderRequest(
                CUSTOMER1_ID, "AAPL", OrderSide.BUY, BigDecimal.ZERO, BigDecimal.valueOf(150.00));

        mockMvc.perform(post(ORDERS_URL)
                .headers(customerHeaders)
                .contentType(MediaType.APPLICATION_JSON)
                .content(toJson(zeroSizeOrder)))
                .andExpect(status().isBadRequest());

        // Test invalid order request - null asset name
        CreateOrderRequest nullAssetOrder = new CreateOrderRequest(
                CUSTOMER1_ID, null, OrderSide.BUY, BigDecimal.valueOf(1.0), BigDecimal.valueOf(150.00));

        mockMvc.perform(post(ORDERS_URL)
                .headers(customerHeaders)
                .contentType(MediaType.APPLICATION_JSON)
                .content(toJson(nullAssetOrder)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Should handle business logic errors gracefully")
    void testBusinessLogicErrors() throws Exception {
        HttpHeaders customerHeaders = createCustomer1Headers();

        // Test insufficient balance for BUY order
        CreateOrderRequest insufficientBalanceOrder = new CreateOrderRequest(
                CUSTOMER1_ID, "AAPL", OrderSide.BUY, BigDecimal.valueOf(1000.0), BigDecimal.valueOf(200.00));

        mockMvc.perform(post(ORDERS_URL)
                .headers(customerHeaders)
                .contentType(MediaType.APPLICATION_JSON)
                .content(toJson(insufficientBalanceOrder)))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.error").value("Insufficient usable balance"));

        // Test insufficient asset for SELL order
        CreateOrderRequest insufficientAssetOrder = new CreateOrderRequest(
                CUSTOMER1_ID, "AAPL", OrderSide.SELL, BigDecimal.valueOf(100.0), BigDecimal.valueOf(150.00));

        mockMvc.perform(post(ORDERS_URL)
                .headers(customerHeaders)
                .contentType(MediaType.APPLICATION_JSON)
                .content(toJson(insufficientAssetOrder)))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.error").value("Insufficient usable balance"));
    }

    @Test
    @DisplayName("Should handle not found errors")
    void testNotFoundErrors() throws Exception {
        HttpHeaders customerHeaders = createCustomer1Headers();
        HttpHeaders adminHeaders = createAdminHeaders();

        // Test cancelling non-existent order
        mockMvc.perform(delete(ORDERS_URL + "/99999")
                .headers(customerHeaders))
                .andExpect(status().isNotFound())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.error").value("Order not found"));

        // Test matching non-existent order
        MatchOrderRequest matchNonExistentOrder = new MatchOrderRequest(99999L);
        mockMvc.perform(post(ORDERS_MATCH_URL)
                .headers(adminHeaders)
                .contentType(MediaType.APPLICATION_JSON)
                .content(toJson(matchNonExistentOrder)))
                .andExpect(status().isNotFound())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.error").value("Order not found"));
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

        // Test missing content-type
        mockMvc.perform(post(ORDERS_URL)
                .headers(customerHeaders)
                .content(toJson(new CreateOrderRequest(
                        CUSTOMER1_ID, "AAPL", OrderSide.BUY, BigDecimal.valueOf(1.0), BigDecimal.valueOf(150.00)))))
                .andExpect(status().isUnsupportedMediaType());
    }

    @Test
    @DisplayName("Should handle cross-customer order cancellation security")
    void testCrossCustomerOrderCancellationSecurity() throws Exception {
        HttpHeaders customer1Headers = createCustomer1Headers();
        HttpHeaders customer2Headers = createCustomer2Headers();

        // Customer1 creates an order
        CreateOrderRequest createRequest = new CreateOrderRequest(
                CUSTOMER1_ID, "AAPL", OrderSide.BUY, BigDecimal.valueOf(1.0), BigDecimal.valueOf(150.00));

        var createResult = mockMvc.perform(post(ORDERS_URL)
                .headers(customer1Headers)
                .contentType(MediaType.APPLICATION_JSON)
                .content(toJson(createRequest)))
                .andExpect(status().isOk())
                .andReturn();

        String createResponse = createResult.getResponse().getContentAsString();
        Long orderId = objectMapper.readTree(createResponse).get("order").get("id").asLong();

        // Customer2 tries to cancel customer1's order
        mockMvc.perform(delete(ORDERS_URL + "/" + orderId)
                .headers(customer2Headers))
                .andExpect(status().isForbidden())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.error").value("You can only cancel your own orders"));
    }

    @Test
    @DisplayName("Should handle invalid order state transitions")
    void testInvalidOrderStateTransitions() throws Exception {
        HttpHeaders customerHeaders = createCustomer1Headers();
        HttpHeaders adminHeaders = createAdminHeaders();

        // Create and match an order
        CreateOrderRequest createRequest = new CreateOrderRequest(
                CUSTOMER1_ID, "AAPL", OrderSide.BUY, BigDecimal.valueOf(1.0), BigDecimal.valueOf(150.00));

        var createResult = mockMvc.perform(post(ORDERS_URL)
                .headers(customerHeaders)
                .contentType(MediaType.APPLICATION_JSON)
                .content(toJson(createRequest)))
                .andExpect(status().isOk())
                .andReturn();

        String createResponse = createResult.getResponse().getContentAsString();
        Long orderId = objectMapper.readTree(createResponse).get("order").get("id").asLong();

        // Match the order
        MatchOrderRequest matchRequest = new MatchOrderRequest(orderId);
        mockMvc.perform(post(ORDERS_MATCH_URL)
                .headers(adminHeaders)
                .contentType(MediaType.APPLICATION_JSON)
                .content(toJson(matchRequest)))
                .andExpect(status().isOk());

        // Try to cancel already matched order
        mockMvc.perform(delete(ORDERS_URL + "/" + orderId)
                .headers(customerHeaders))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.error").value("Only pending orders can be cancelled"));

        // Try to match already matched order
        mockMvc.perform(post(ORDERS_MATCH_URL)
                .headers(adminHeaders)
                .contentType(MediaType.APPLICATION_JSON)
                .content(toJson(matchRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.error").value("Only pending orders can be matched"));
    }

    @Test
    @DisplayName("Should handle admin user ID requirement for assets")
    void testAdminAssetUserIdRequirement() throws Exception {
        HttpHeaders adminHeaders = createAdminHeaders();

        // Admin trying to list assets without userId parameter
        mockMvc.perform(get(ASSETS_URL)
                .headers(adminHeaders))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.error").value("Customer ID is required for admin to list assets"));
    }

    @Test
    @DisplayName("Should properly handle concurrent access scenarios")
    void testConcurrentAccessScenarios() throws Exception {
        HttpHeaders customer1Headers = createCustomer1Headers();
        HttpHeaders adminHeaders = createAdminHeaders();

        // Create an order
        CreateOrderRequest createRequest = new CreateOrderRequest(
                CUSTOMER1_ID, "AAPL", OrderSide.BUY, BigDecimal.valueOf(1.0), BigDecimal.valueOf(150.00));

        var createResult = mockMvc.perform(post(ORDERS_URL)
                .headers(customer1Headers)
                .contentType(MediaType.APPLICATION_JSON)
                .content(toJson(createRequest)))
                .andExpect(status().isOk())
                .andReturn();

        String createResponse = createResult.getResponse().getContentAsString();
        Long orderId = objectMapper.readTree(createResponse).get("order").get("id").asLong();

        // Simulate concurrent cancellation attempts
        // First cancellation should succeed
        mockMvc.perform(delete(ORDERS_URL + "/" + orderId)
                .headers(customer1Headers))
                .andExpect(status().isOk());

        // Second cancellation should fail (order already cancelled)
        mockMvc.perform(delete(ORDERS_URL + "/" + orderId)
                .headers(adminHeaders))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Only pending orders can be cancelled"));
    }

    @Test
    @DisplayName("Should handle edge cases in date filtering")
    void testDateFilteringEdgeCases() throws Exception {
        HttpHeaders customerHeaders = createCustomer1Headers();

        // Test with invalid date format
        mockMvc.perform(get(ORDERS_URL)
                .headers(customerHeaders)
                .param("startDate", "invalid-date"))
                .andExpect(status().isBadRequest());

        // Test with end date before start date
        mockMvc.perform(get(ORDERS_URL)
                .headers(customerHeaders)
                .param("startDate", "2025-08-25T00:00:00")
                .param("endDate", "2025-08-24T00:00:00"))
                .andExpect(status().isOk()) // Should still work but return empty results
                .andExpect(jsonPath("$.orders").isArray());
    }

    @Test
    @DisplayName("Should handle missing authentication headers gracefully")
    void testMissingAuthHeaders() throws Exception {
        HttpHeaders incompleteHeaders = new HttpHeaders();
        incompleteHeaders.set("Username", CUSTOMER1_USERNAME);
        // Missing password header

        mockMvc.perform(get(ORDERS_URL)
                .headers(incompleteHeaders))
                .andExpect(status().isUnauthorized());

        // Only password header
        HttpHeaders passwordOnlyHeaders = new HttpHeaders();
        passwordOnlyHeaders.set("Password", CUSTOMER1_PASSWORD);

        mockMvc.perform(get(ORDERS_URL)
                .headers(passwordOnlyHeaders))
                .andExpect(status().isUnauthorized());
    }
}
