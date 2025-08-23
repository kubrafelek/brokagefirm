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

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Integration tests for admin-specific functionality
 */
@DisplayName("Admin Functionality Integration Tests")
class AdminFunctionalityIntegrationTest extends BaseIntegrationTest {

    private static final String ORDERS_URL = "/api/orders";
    private static final String ORDERS_MATCH_URL = "/api/orders/match";
    private static final String ORDERS_PENDING_URL = "/api/orders/pending";
    private static final String ASSETS_URL = "/api/assets";

    @Test
    @DisplayName("Admin should be able to create orders for any customer")
    void testAdminCreateOrderForAnyCustomer() throws Exception {
        HttpHeaders adminHeaders = createAdminHeaders();

        // Create order for customer1
        CreateOrderRequest orderForCustomer1 = new CreateOrderRequest(
                CUSTOMER1_ID, "AAPL", OrderSide.BUY, BigDecimal.valueOf(2.0), BigDecimal.valueOf(150.00));

        mockMvc.perform(post(ORDERS_URL)
                .headers(adminHeaders)
                .contentType(MediaType.APPLICATION_JSON)
                .content(toJson(orderForCustomer1)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Order created successfully"))
                .andExpect(jsonPath("$.order.userId").value(CUSTOMER1_ID));

        // Create order for customer2
        CreateOrderRequest orderForCustomer2 = new CreateOrderRequest(
                CUSTOMER2_ID, "MSFT", OrderSide.BUY, BigDecimal.valueOf(3.0), BigDecimal.valueOf(400.00));

        mockMvc.perform(post(ORDERS_URL)
                .headers(adminHeaders)
                .contentType(MediaType.APPLICATION_JSON)
                .content(toJson(orderForCustomer2)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Order created successfully"))
                .andExpect(jsonPath("$.order.userId").value(CUSTOMER2_ID));
    }

    @Test
    @DisplayName("Admin should be able to view all orders")
    void testAdminViewAllOrders() throws Exception {
        HttpHeaders adminHeaders = createAdminHeaders();

        mockMvc.perform(get(ORDERS_URL)
                .headers(adminHeaders))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.message").value("Orders retrieved successfully"))
                .andExpect(jsonPath("$.orders").isArray())
                // Should contain orders from multiple customers
                .andExpect(jsonPath("$.orders[*].userId").value(hasItems(
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
                .andExpect(jsonPath("$.message").value("Orders retrieved successfully"))
                .andExpect(jsonPath("$.orders").isArray())
                // Should only contain orders from customer1
                .andExpect(jsonPath("$.orders[*].userId").value(everyItem(is(CUSTOMER1_ID.intValue()))));
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
                .andExpect(jsonPath("$.message").value("Assets retrieved successfully"))
                .andExpect(jsonPath("$.assets[*].userId").value(everyItem(is(CUSTOMER1_ID.intValue()))));

        // View customer2 assets
        mockMvc.perform(get(ASSETS_URL)
                .headers(adminHeaders)
                .param("userId", CUSTOMER2_ID.toString()))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.message").value("Assets retrieved successfully"))
                .andExpect(jsonPath("$.assets[*].userId").value(everyItem(is(CUSTOMER2_ID.intValue()))));
    }

    @Test
    @DisplayName("Admin should be able to cancel any customer's order")
    void testAdminCancelAnyOrder() throws Exception {
        HttpHeaders customerHeaders = createCustomer1Headers();
        HttpHeaders adminHeaders = createAdminHeaders();

        // Customer creates an order
        CreateOrderRequest createRequest = new CreateOrderRequest(
                CUSTOMER1_ID, "AAPL", OrderSide.BUY, BigDecimal.valueOf(1.0), BigDecimal.valueOf(150.00));

        MvcResult createResult = mockMvc.perform(post(ORDERS_URL)
                .headers(customerHeaders)
                .contentType(MediaType.APPLICATION_JSON)
                .content(toJson(createRequest)))
                .andExpect(status().isOk())
                .andReturn();

        String createResponse = createResult.getResponse().getContentAsString();
        Long orderId = objectMapper.readTree(createResponse).get("order").get("id").asLong();

        // Admin cancels the order
        mockMvc.perform(delete(ORDERS_URL + "/" + orderId)
                .headers(adminHeaders))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.message").value("Order cancelled successfully"))
                .andExpect(jsonPath("$.order.status").value("CANCELLED"));
    }

    @Test
    @DisplayName("Admin should be able to match pending orders")
    void testAdminMatchPendingOrders() throws Exception {
        HttpHeaders customerHeaders = createCustomer1Headers();
        HttpHeaders adminHeaders = createAdminHeaders();

        // Customer creates a buy order
        CreateOrderRequest buyOrder = new CreateOrderRequest(
                CUSTOMER1_ID, "AAPL", OrderSide.BUY, BigDecimal.valueOf(1.0), BigDecimal.valueOf(150.00));

        MvcResult createResult = mockMvc.perform(post(ORDERS_URL)
                .headers(customerHeaders)
                .contentType(MediaType.APPLICATION_JSON)
                .content(toJson(buyOrder)))
                .andExpect(status().isOk())
                .andReturn();

        String createResponse = createResult.getResponse().getContentAsString();
        Long orderId = objectMapper.readTree(createResponse).get("order").get("id").asLong();

        // Admin matches the order
        MatchOrderRequest matchRequest = new MatchOrderRequest(orderId);

        mockMvc.perform(post(ORDERS_MATCH_URL)
                .headers(adminHeaders)
                .contentType(MediaType.APPLICATION_JSON)
                .content(toJson(matchRequest)))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.message").value("Order matched successfully"))
                .andExpect(jsonPath("$.order.status").value("MATCHED"));
    }

    @Test
    @DisplayName("Admin should be able to list all pending orders")
    void testAdminListAllPendingOrders() throws Exception {
        HttpHeaders adminHeaders = createAdminHeaders();

        mockMvc.perform(get(ORDERS_PENDING_URL)
                .headers(adminHeaders))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.message").value("Pending orders retrieved successfully"))
                .andExpect(jsonPath("$.orders").isArray())
                .andExpect(jsonPath("$.orders[*].status").value(everyItem(is("PENDING"))));
    }

    @Test
    @DisplayName("Admin should have comprehensive order management capabilities")
    void testAdminComprehensiveOrderManagement() throws Exception {
        HttpHeaders customerHeaders = createCustomer1Headers();
        HttpHeaders adminHeaders = createAdminHeaders();

        // 1. Admin creates order for customer
        CreateOrderRequest adminCreateOrder = new CreateOrderRequest(
                CUSTOMER1_ID, "AAPL", OrderSide.BUY, BigDecimal.valueOf(1.0), BigDecimal.valueOf(150.00));

        MvcResult adminCreateResult = mockMvc.perform(post(ORDERS_URL)
                .headers(adminHeaders)
                .contentType(MediaType.APPLICATION_JSON)
                .content(toJson(adminCreateOrder)))
                .andExpect(status().isOk())
                .andReturn();

        Long adminOrderId = objectMapper.readTree(adminCreateResult.getResponse().getContentAsString())
                .get("order").get("id").asLong();

        // 2. Customer creates their own order
        CreateOrderRequest customerCreateOrder = new CreateOrderRequest(
                CUSTOMER1_ID, "GOOGL", OrderSide.SELL, BigDecimal.valueOf(2.0), BigDecimal.valueOf(2800.00));

        MvcResult customerCreateResult = mockMvc.perform(post(ORDERS_URL)
                .headers(customerHeaders)
                .contentType(MediaType.APPLICATION_JSON)
                .content(toJson(customerCreateOrder)))
                .andExpect(status().isOk())
                .andReturn();

        Long customerOrderId = objectMapper.readTree(customerCreateResult.getResponse().getContentAsString())
                .get("order").get("id").asLong();

        // 3. Admin views all orders and should see both
        mockMvc.perform(get(ORDERS_URL)
                .headers(adminHeaders))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.orders[?(@.id==" + adminOrderId + ")]").exists())
                .andExpect(jsonPath("$.orders[?(@.id==" + customerOrderId + ")]").exists());

        // 4. Admin matches one order
        MatchOrderRequest matchRequest = new MatchOrderRequest(adminOrderId);
        mockMvc.perform(post(ORDERS_MATCH_URL)
                .headers(adminHeaders)
                .contentType(MediaType.APPLICATION_JSON)
                .content(toJson(matchRequest)))
                .andExpect(status().isOk());

        // 5. Admin cancels the other order
        mockMvc.perform(delete(ORDERS_URL + "/" + customerOrderId)
                .headers(adminHeaders))
                .andExpect(status().isOk());

        // 6. Verify order statuses changed
        mockMvc.perform(get(ORDERS_URL)
                .headers(adminHeaders))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.orders[?(@.id==" + adminOrderId + ")].status").value("MATCHED"))
                .andExpect(jsonPath("$.orders[?(@.id==" + customerOrderId + ")].status").value("CANCELLED"));
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
                .andExpect(jsonPath("$.message").value("Orders retrieved successfully"))
                .andExpect(jsonPath("$.orders").isArray());
    }

    @Test
    @DisplayName("Admin should be able to create sell orders when customer has sufficient assets")
    void testAdminCreateSellOrderForCustomer() throws Exception {
        HttpHeaders adminHeaders = createAdminHeaders();

        // Create sell order for customer1 who has AAPL assets
        CreateOrderRequest sellOrder = new CreateOrderRequest(
                CUSTOMER1_ID, "AAPL", OrderSide.SELL, BigDecimal.valueOf(5.0), BigDecimal.valueOf(155.00));

        mockMvc.perform(post(ORDERS_URL)
                .headers(adminHeaders)
                .contentType(MediaType.APPLICATION_JSON)
                .content(toJson(sellOrder)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Order created successfully"))
                .andExpect(jsonPath("$.order.orderSide").value("SELL"))
                .andExpect(jsonPath("$.order.userId").value(CUSTOMER1_ID));
    }

    @Test
    @DisplayName("Admin should respect business rules even with elevated permissions")
    void testAdminRespectsBusinessRules() throws Exception {
        HttpHeaders adminHeaders = createAdminHeaders();

        // Try to create order that would exceed customer's balance
        CreateOrderRequest invalidOrder = new CreateOrderRequest(
                CUSTOMER1_ID, "AAPL", OrderSide.BUY, BigDecimal.valueOf(1000.0), BigDecimal.valueOf(200.00));

        mockMvc.perform(post(ORDERS_URL)
                .headers(adminHeaders)
                .contentType(MediaType.APPLICATION_JSON)
                .content(toJson(invalidOrder)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Insufficient usable balance"));
    }

    @Test
    @DisplayName("Admin should be able to perform bulk operations efficiently")
    void testAdminBulkOperations() throws Exception {
        HttpHeaders adminHeaders = createAdminHeaders();

        // Create multiple orders for different customers
        CreateOrderRequest[] orders = {
                new CreateOrderRequest(CUSTOMER1_ID, "AAPL", OrderSide.BUY, BigDecimal.valueOf(1.0),
                        BigDecimal.valueOf(150.00)),
                new CreateOrderRequest(CUSTOMER2_ID, "MSFT", OrderSide.BUY, BigDecimal.valueOf(2.0),
                        BigDecimal.valueOf(400.00)),
                new CreateOrderRequest(TEST_USER_ID, "NVDA", OrderSide.SELL, BigDecimal.valueOf(3.0),
                        BigDecimal.valueOf(800.00))
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
                .andExpect(jsonPath("$.orders", hasSize(greaterThanOrEqualTo(orders.length))));
    }
}
