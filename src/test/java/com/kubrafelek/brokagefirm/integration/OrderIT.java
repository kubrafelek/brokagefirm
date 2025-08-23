package com.kubrafelek.brokagefirm.integration;

import com.kubrafelek.brokagefirm.dto.CreateOrderRequest;
import com.kubrafelek.brokagefirm.dto.MatchOrderRequest;
import com.kubrafelek.brokagefirm.enums.OrderSide;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Integration tests for order management functionality
 */
@DisplayName("Order Management Integration Tests")
class OrderIT extends BaseIT {

    private static final String ORDERS_URL = "/api/orders";
    private static final String ORDERS_MATCH_URL = "/api/orders/match";
    private static final String ORDERS_PENDING_URL = "/api/orders/pending";

    private static final LocalDateTime FIXED_DATE = LocalDateTime.of(2025, 8, 21, 16, 0, 0);

    @Test
    @DisplayName("Should successfully create BUY order")
    void testCreateBuyOrder_Success() throws Exception {
        CreateOrderRequest request = new CreateOrderRequest(
                CUSTOMER1_ID, "AAPL", OrderSide.BUY, BigDecimal.valueOf(2.0), BigDecimal.valueOf(150.00), FIXED_DATE);

        HttpHeaders adminHeaders = createAdminHeaders();

        mockMvc.perform(post(ORDERS_URL)
                .headers(adminHeaders)
                .contentType(MediaType.APPLICATION_JSON)
                .content(toJson(request)))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.userId").value(CUSTOMER1_ID))
                .andExpect(jsonPath("$.assetName").value("AAPL"))
                .andExpect(jsonPath("$.orderSide").value("BUY"))
                .andExpect(jsonPath("$.size").value(2.0))
                .andExpect(jsonPath("$.price").value(150.00))
                .andExpect(jsonPath("$.status").value("PENDING"));
    }

    @Test
    @DisplayName("Should successfully create SELL order")
    void testCreateSellOrder_Success() throws Exception {
        CreateOrderRequest request = new CreateOrderRequest(
                CUSTOMER1_ID, "AAPL", OrderSide.SELL, BigDecimal.valueOf(3.0), BigDecimal.valueOf(155.00), FIXED_DATE);

        HttpHeaders adminHeaders = createAdminHeaders();

        mockMvc.perform(post(ORDERS_URL)
                .headers(adminHeaders)
                .contentType(MediaType.APPLICATION_JSON)
                .content(toJson(request)))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.userId").value(CUSTOMER1_ID))
                .andExpect(jsonPath("$.assetName").value("AAPL"))
                .andExpect(jsonPath("$.orderSide").value("SELL"))
                .andExpect(jsonPath("$.size").value(3.0))
                .andExpect(jsonPath("$.price").value(155.00))
                .andExpect(jsonPath("$.status").value("PENDING"));
    }

    @Test
    @DisplayName("Should reject BUY order with insufficient TRY balance")
    void testCreateBuyOrder_InsufficientBalance() throws Exception {
        CreateOrderRequest request = new CreateOrderRequest(
                CUSTOMER1_ID, "AAPL", OrderSide.BUY, BigDecimal.valueOf(100.0), BigDecimal.valueOf(200.00), FIXED_DATE);

        HttpHeaders adminHeaders = createAdminHeaders();

        mockMvc.perform(post(ORDERS_URL)
                .headers(adminHeaders)
                .contentType(MediaType.APPLICATION_JSON)
                .content(toJson(request)))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentType("text/plain;charset=UTF-8"))
                .andExpect(content().string(containsString("Insufficient usable balance")));
    }

    @Test
    @DisplayName("Should reject SELL order with insufficient asset balance")
    void testCreateSellOrder_InsufficientAssets() throws Exception {
        CreateOrderRequest request = new CreateOrderRequest(
                CUSTOMER1_ID, "AAPL", OrderSide.SELL, BigDecimal.valueOf(100.0), BigDecimal.valueOf(150.00), FIXED_DATE);

        HttpHeaders adminHeaders = createAdminHeaders();

        mockMvc.perform(post(ORDERS_URL)
                .headers(adminHeaders)
                .contentType(MediaType.APPLICATION_JSON)
                .content(toJson(request)))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentType("text/plain;charset=UTF-8"))
                .andExpect(content().string(containsString("Insufficient usable balance")));
    }

    @Test
    @DisplayName("Should reject order creation by non-admin users")
    void testCreateOrder_CustomerForbidden() throws Exception {
        CreateOrderRequest request = new CreateOrderRequest(
                CUSTOMER1_ID, "AAPL", OrderSide.BUY, BigDecimal.valueOf(2.0), BigDecimal.valueOf(150.00), FIXED_DATE);

        HttpHeaders customerHeaders = createCustomer1Headers();

        mockMvc.perform(post(ORDERS_URL)
                .headers(customerHeaders)
                .contentType(MediaType.APPLICATION_JSON)
                .content(toJson(request)))
                .andExpect(status().isForbidden())
                .andExpect(content().string(containsString("Only admin users can create orders for customers")));
    }

    @Test
    @DisplayName("Should allow admin to create order for any customer")
    void testCreateOrder_AdminForCustomer() throws Exception {
        CreateOrderRequest request = new CreateOrderRequest(
                CUSTOMER1_ID, "AAPL", OrderSide.BUY, BigDecimal.valueOf(1.0), BigDecimal.valueOf(150.00), FIXED_DATE);

        HttpHeaders headers = createAdminHeaders();

        mockMvc.perform(post(ORDERS_URL)
                .headers(headers)
                .contentType(MediaType.APPLICATION_JSON)
                .content(toJson(request)))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.userId").value(CUSTOMER1_ID));
    }

    @Test
    @DisplayName("Should list customer's own orders")
    void testListOrders_Customer() throws Exception {
        HttpHeaders headers = createCustomer1Headers();

        mockMvc.perform(get(ORDERS_URL)
                .headers(headers))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[*].userId").value(everyItem(is(CUSTOMER1_ID.intValue()))));
    }

    @Test
    @DisplayName("Should allow admin to list all orders")
    void testListOrders_Admin() throws Exception {
        HttpHeaders headers = createAdminHeaders();

        mockMvc.perform(get(ORDERS_URL)
                .headers(headers))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$").isArray());
    }

    @Test
    @DisplayName("Should allow admin to list orders for specific customer")
    void testListOrders_AdminWithCustomerFilter() throws Exception {
        HttpHeaders headers = createAdminHeaders();

        mockMvc.perform(get(ORDERS_URL)
                .headers(headers)
                .param("userId", CUSTOMER1_ID.toString()))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[*].userId").value(everyItem(is(CUSTOMER1_ID.intValue()))));
    }

    @Test
    @DisplayName("Should filter orders by date range")
    void testListOrders_WithDateFilter() throws Exception {
        HttpHeaders headers = createCustomer1Headers();

        mockMvc.perform(get(ORDERS_URL)
                .headers(headers)
                .param("startDate", "2025-08-22T00:00:00")
                .param("endDate", "2025-08-24T23:59:59"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$").isArray());
    }

    @Test
    @DisplayName("Should reject order matching by non-admin user")
    void testMatchOrder_NonAdmin() throws Exception {
        MatchOrderRequest matchRequest = new MatchOrderRequest(1L);
        HttpHeaders customerHeaders = createCustomer1Headers();

        mockMvc.perform(post(ORDERS_MATCH_URL)
                .headers(customerHeaders)
                .contentType(MediaType.APPLICATION_JSON)
                .content(toJson(matchRequest)))
                .andExpect(status().isForbidden())
                .andExpect(content().contentType("text/plain;charset=UTF-8"))
                .andExpect(content().string(containsString("Only admin users can match orders")));
    }

    @Test
    @DisplayName("Should allow admin to list pending orders")
    void testListPendingOrders_Admin() throws Exception {
        HttpHeaders adminHeaders = createAdminHeaders();

        mockMvc.perform(get(ORDERS_PENDING_URL)
                .headers(adminHeaders))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[*].status").value(everyItem(is("PENDING"))));
    }

    @Test
    @DisplayName("Should reject pending orders listing by non-admin user")
    void testListPendingOrders_NonAdmin() throws Exception {
        HttpHeaders customerHeaders = createCustomer1Headers();

        mockMvc.perform(get(ORDERS_PENDING_URL)
                .headers(customerHeaders))
                .andExpect(status().isForbidden())
                .andExpect(content().contentType("text/plain;charset=UTF-8"))
                .andExpect(content().string(containsString("Only admin users can view all pending orders")));
    }

    @Test
    @DisplayName("Should reject order operations without authentication")
    void testOrderOperations_NoAuth() throws Exception {
        CreateOrderRequest createRequest = new CreateOrderRequest(
                CUSTOMER1_ID, "AAPL", OrderSide.BUY, BigDecimal.valueOf(1.0), BigDecimal.valueOf(150.00), FIXED_DATE);

        // Test create order without auth
        mockMvc.perform(post(ORDERS_URL)
                .contentType(MediaType.APPLICATION_JSON)
                .content(toJson(createRequest)))
                .andExpect(status().isBadRequest());

        // Test list orders without auth
        mockMvc.perform(get(ORDERS_URL))
                .andExpect(status().isBadRequest());

        // Test cancel order without auth
        mockMvc.perform(delete(ORDERS_URL + "/1"))
                .andExpect(status().isBadRequest());

        // Test match order without auth
        MatchOrderRequest matchRequest = new MatchOrderRequest(1L);
        mockMvc.perform(post(ORDERS_MATCH_URL)
                .contentType(MediaType.APPLICATION_JSON)
                .content(toJson(matchRequest)))
                .andExpect(status().isBadRequest());

        // Test list pending orders without auth
        mockMvc.perform(get(ORDERS_PENDING_URL))
                .andExpect(status().isBadRequest());
    }
}
