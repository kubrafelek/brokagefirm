package com.kubrafelek.brokagefirm.integration;

import com.kubrafelek.brokagefirm.dto.CreateOrderRequest;
import com.kubrafelek.brokagefirm.dto.MatchOrderRequest;
import com.kubrafelek.brokagefirm.enums.OrderSide;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MvcResult;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Integration tests for admin-specific functionality
 */
@DisplayName("Admin Functionality Integration Tests")
class AdminFunctionalityIT extends BaseIT {

    private static final String ORDERS_URL = "/api/orders";
    private static final String ORDERS_MATCH_URL = "/api/orders/match";
    private static final String ORDERS_PENDING_URL = "/api/orders/pending";
    private static final String ASSETS_URL = "/api/assets";

    private static final LocalDateTime FIXED_DATE = LocalDateTime.of(2025, 8, 21, 16, 0, 0);

    @Test
    @DisplayName("Admin should be able to create orders for any customer")
    void testAdminCreateOrderForAnyCustomer() throws Exception {
        HttpHeaders adminHeaders = createAdminHeaders();

        // Create order for customer1
        CreateOrderRequest orderForCustomer1 = new CreateOrderRequest(
                CUSTOMER1_ID, "AAPL", OrderSide.BUY, BigDecimal.valueOf(2.0), BigDecimal.valueOf(150.00), FIXED_DATE);

        mockMvc.perform(post(ORDERS_URL)
                .headers(adminHeaders)
                .contentType(MediaType.APPLICATION_JSON)
                .content(toJson(orderForCustomer1)))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.userId").value(CUSTOMER1_ID))
                .andExpect(jsonPath("$.assetName").value("AAPL"))
                .andExpect(jsonPath("$.orderSide").value("BUY"))
                .andExpect(jsonPath("$.status").value("PENDING"));

        // Create order for customer2
        CreateOrderRequest orderForCustomer2 = new CreateOrderRequest(
                CUSTOMER2_ID, "MSFT", OrderSide.BUY, BigDecimal.valueOf(3.0), BigDecimal.valueOf(400.00), FIXED_DATE);

        mockMvc.perform(post(ORDERS_URL)
                .headers(adminHeaders)
                .contentType(MediaType.APPLICATION_JSON)
                .content(toJson(orderForCustomer2)))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.userId").value(CUSTOMER2_ID))
                .andExpect(jsonPath("$.assetName").value("MSFT"))
                .andExpect(jsonPath("$.orderSide").value("BUY"))
                .andExpect(jsonPath("$.status").value("PENDING"));
    }

    @Test
    @DisplayName("Admin should be able to view all orders")
    void testAdminViewAllOrders() throws Exception {
        HttpHeaders adminHeaders = createAdminHeaders();

        mockMvc.perform(get(ORDERS_URL)
                .headers(adminHeaders))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$").isArray())
                // Should contain orders from multiple customers
                .andExpect(jsonPath("$[*].userId").value(hasItems(
                        CUSTOMER1_ID.intValue(),
                        CUSTOMER2_ID.intValue(),
                        TEST_USER_ID.intValue())));
    }

    @Test
    @DisplayName("Admin should be able to filter orders by specific customer")
    void testAdminFilterOrdersByCustomer() throws Exception {
        HttpHeaders adminHeaders = createAdminHeaders();

        mockMvc.perform(get(ORDERS_URL)
                .headers(adminHeaders)
                .param("userId", CUSTOMER1_ID.toString()))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$").isArray())
                // Should only contain orders from customer1
                .andExpect(jsonPath("$[*].userId").value(everyItem(is(CUSTOMER1_ID.intValue()))));
    }

    @Test
    @DisplayName("Admin should be able to view assets for any customer")
    void testAdminViewAnyCustomerAssets() throws Exception {
        HttpHeaders adminHeaders = createAdminHeaders();

        // View customer1 assets
        mockMvc.perform(get(ASSETS_URL)
                .headers(adminHeaders)
                .param("userId", CUSTOMER1_ID.toString()))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[*].userId").value(everyItem(is(CUSTOMER1_ID.intValue()))));

        // View customer2 assets
        mockMvc.perform(get(ASSETS_URL)
                .headers(adminHeaders)
                .param("userId", CUSTOMER2_ID.toString()))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[*].userId").value(everyItem(is(CUSTOMER2_ID.intValue()))));
    }

    @Test
    @DisplayName("Admin should be able to cancel any customer's order")
    void testAdminCancelAnyOrder() throws Exception {
        HttpHeaders adminHeaders = createAdminHeaders();

        // Admin creates an order for customer
        CreateOrderRequest createRequest = new CreateOrderRequest(
                CUSTOMER1_ID, "AAPL", OrderSide.BUY, BigDecimal.valueOf(1.0), BigDecimal.valueOf(150.00), FIXED_DATE);

        MvcResult createResult = mockMvc.perform(post(ORDERS_URL)
                .headers(adminHeaders)
                .contentType(MediaType.APPLICATION_JSON)
                .content(toJson(createRequest)))
                .andExpect(status().isOk())
                .andReturn();

        String createResponse = createResult.getResponse().getContentAsString();
        Long orderId = objectMapper.readTree(createResponse).get("id").asLong();

        // Admin cancels the order
        mockMvc.perform(delete(ORDERS_URL + "/" + orderId)
                .headers(adminHeaders))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.status").value("CANCELLED"))
                .andExpect(jsonPath("$.id").value(orderId));
    }

    @Test
    @DisplayName("Admin should be able to match pending orders")
    void testAdminMatchPendingOrders() throws Exception {
        HttpHeaders adminHeaders = createAdminHeaders();

        // Admin creates a buy order for customer
        CreateOrderRequest buyOrder = new CreateOrderRequest(
                CUSTOMER1_ID, "AAPL", OrderSide.BUY, BigDecimal.valueOf(1.0), BigDecimal.valueOf(150.00), FIXED_DATE);

        MvcResult createResult = mockMvc.perform(post(ORDERS_URL)
                .headers(adminHeaders)
                .contentType(MediaType.APPLICATION_JSON)
                .content(toJson(buyOrder)))
                .andExpect(status().isOk())
                .andReturn();

        String createResponse = createResult.getResponse().getContentAsString();
        Long orderId = objectMapper.readTree(createResponse).get("id").asLong();

        // Admin matches the order
        MatchOrderRequest matchRequest = new MatchOrderRequest(orderId);

        mockMvc.perform(post(ORDERS_MATCH_URL)
                .headers(adminHeaders)
                .contentType(MediaType.APPLICATION_JSON)
                .content(toJson(matchRequest)))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.status").value("MATCHED"))
                .andExpect(jsonPath("$.id").value(orderId));
    }

    @Test
    @DisplayName("Admin should be able to list all pending orders")
    void testAdminListAllPendingOrders() throws Exception {
        HttpHeaders adminHeaders = createAdminHeaders();

        mockMvc.perform(get(ORDERS_PENDING_URL)
                .headers(adminHeaders))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[*].status").value(everyItem(is("PENDING"))));
    }

    @Test
    @DisplayName("Admin should be able to filter orders by date range")
    void testAdminFilterOrdersByDateRange() throws Exception {
        HttpHeaders adminHeaders = createAdminHeaders();

        mockMvc.perform(get(ORDERS_URL)
                .headers(adminHeaders)
                .param("startDate", "2025-08-22T00:00:00")
                .param("endDate", "2025-08-24T23:59:59"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$").isArray());
    }

    @Test
    @DisplayName("Admin should be able to create sell orders when customer has sufficient assets")
    void testAdminCreateSellOrderForCustomer() throws Exception {
        HttpHeaders adminHeaders = createAdminHeaders();

        // Create sell order for customer1 who has AAPL assets
        CreateOrderRequest sellOrder = new CreateOrderRequest(
                CUSTOMER1_ID, "AAPL", OrderSide.SELL, BigDecimal.valueOf(5.0), BigDecimal.valueOf(155.00), FIXED_DATE);

        mockMvc.perform(post(ORDERS_URL)
                .headers(adminHeaders)
                .contentType(MediaType.APPLICATION_JSON)
                .content(toJson(sellOrder)))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.orderSide").value("SELL"))
                .andExpect(jsonPath("$.userId").value(CUSTOMER1_ID))
                .andExpect(jsonPath("$.status").value("PENDING"));
    }

    @Test
    @DisplayName("Admin should respect business rules even with elevated permissions")
    void testAdminRespectsBusinessRules() throws Exception {
        HttpHeaders adminHeaders = createAdminHeaders();

        // Try to create order that would exceed customer's balance
        CreateOrderRequest invalidOrder = new CreateOrderRequest(
                CUSTOMER1_ID, "AAPL", OrderSide.BUY, BigDecimal.valueOf(1000.0), BigDecimal.valueOf(200.00), FIXED_DATE);

        mockMvc.perform(post(ORDERS_URL)
                .headers(adminHeaders)
                .contentType(MediaType.APPLICATION_JSON)
                .content(toJson(invalidOrder)))
                .andExpect(status().isBadRequest())
                .andExpect(content().string(containsString("Insufficient usable balance")));
    }

    @Test
    @DisplayName("Admin should be able to perform bulk operations efficiently")
    void testAdminBulkOperations() throws Exception {
        HttpHeaders adminHeaders = createAdminHeaders();

        // Create multiple orders for different customers
        CreateOrderRequest[] orders = {
                new CreateOrderRequest(CUSTOMER1_ID, "AAPL", OrderSide.BUY, BigDecimal.valueOf(1.0),
                        BigDecimal.valueOf(150.00), FIXED_DATE),
                new CreateOrderRequest(CUSTOMER2_ID, "MSFT", OrderSide.BUY, BigDecimal.valueOf(2.0),
                        BigDecimal.valueOf(400.00), FIXED_DATE),
                new CreateOrderRequest(TEST_USER_ID, "NVDA", OrderSide.SELL, BigDecimal.valueOf(3.0),
                        BigDecimal.valueOf(800.00), FIXED_DATE)
        };

        // Create all orders
        for (CreateOrderRequest order : orders) {
            mockMvc.perform(post(ORDERS_URL)
                    .headers(adminHeaders)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(toJson(order)))
                    .andExpect(status().isOk());
        }

        // Verify admin can see all created orders
        mockMvc.perform(get(ORDERS_URL)
                .headers(adminHeaders))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(greaterThanOrEqualTo(orders.length))));
    }
}
