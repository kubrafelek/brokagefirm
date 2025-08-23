package com.kubrafelek.brokagefirm.integration;

import com.kubrafelek.brokagefirm.dto.CreateOrderRequest;
import com.kubrafelek.brokagefirm.dto.LoginRequest;
import com.kubrafelek.brokagefirm.dto.LoginResponse;
import com.kubrafelek.brokagefirm.dto.MatchOrderRequest;
import com.kubrafelek.brokagefirm.enums.OrderSide;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MvcResult;

import java.math.BigDecimal;

import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * End-to-end integration tests that simulate complete business workflows
 */
@DisplayName("End-to-End Workflow Integration Tests")
class EndToEndWorkflowIT extends BaseIT {

    private static final String AUTH_LOGIN_URL = "/api/auth/login";
    private static final String ORDERS_URL = "/api/orders";
    private static final String ORDERS_MATCH_URL = "/api/orders/match";
    private static final String ORDERS_PENDING_URL = "/api/orders/pending";
    private static final String ASSETS_URL = "/api/assets";

    @Test
    @DisplayName("Complete trading workflow: Login -> Check Assets -> Create Order -> Match -> Verify")
    void testCompleteTradeWorkflow() throws Exception {
        // 1. Customer login
        LoginRequest loginRequest = new LoginRequest(CUSTOMER1_USERNAME, CUSTOMER1_PASSWORD);
        MvcResult loginResult = mockMvc.perform(post(AUTH_LOGIN_URL)
                .contentType(MediaType.APPLICATION_JSON)
                .content(toJson(loginRequest)))
                .andExpect(status().isOk())
                .andReturn();

        LoginResponse loginResponse = fromJson(loginResult.getResponse().getContentAsString(), LoginResponse.class);
        assertEquals(CUSTOMER1_ID, loginResponse.getUserId());
        assertFalse(loginResponse.getIsAdmin());

        // 2. Check customer's assets before trading
        HttpHeaders customerHeaders = createCustomer1Headers();
        MvcResult assetsBeforeResult = mockMvc.perform(get(ASSETS_URL)
                .headers(customerHeaders))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[?(@.assetName=='TRY')].usableSize").value(hasItem(10000.00)))
                .andExpect(jsonPath("$[?(@.assetName=='AAPL')].usableSize").value(hasItem(10.00)))
                .andReturn();

        // 3. Customer creates a BUY order
        CreateOrderRequest buyOrder = new CreateOrderRequest(
                CUSTOMER1_ID, "AAPL", OrderSide.BUY, BigDecimal.valueOf(2.0), BigDecimal.valueOf(150.00));

        MvcResult orderResult = mockMvc.perform(post(ORDERS_URL)
                .headers(customerHeaders)
                .contentType(MediaType.APPLICATION_JSON)
                .content(toJson(buyOrder)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("PENDING"))
                .andReturn();

        Long orderId = objectMapper.readTree(orderResult.getResponse().getContentAsString())
                .get("id").asLong();

        // 4. Verify TRY balance is reserved after order creation
        mockMvc.perform(get(ASSETS_URL)
                .headers(customerHeaders))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[?(@.assetName=='TRY')].usableSize").value(hasItem(9700.00))); // 10000 - (2 * 150)

        // 5. Admin login and match the order
        HttpHeaders adminHeaders = createAdminHeaders();
        MatchOrderRequest matchRequest = new MatchOrderRequest(orderId);

        mockMvc.perform(post(ORDERS_MATCH_URL)
                .headers(adminHeaders)
                .contentType(MediaType.APPLICATION_JSON)
                .content(toJson(matchRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("MATCHED"));

        // 6. Verify final asset balances after matching
        mockMvc.perform(get(ASSETS_URL)
                .headers(customerHeaders))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[?(@.assetName=='TRY')].size").value(hasItem(9700.00))) // TRY reduced
                .andExpect(jsonPath("$[?(@.assetName=='TRY')].usableSize").value(hasItem(9700.00))) // Usable updated
                .andExpect(jsonPath("$[?(@.assetName=='AAPL')].size").value(hasItem(12.00))) // AAPL increased
                .andExpect(jsonPath("$[?(@.assetName=='AAPL')].usableSize").value(hasItem(12.00))); // Usable updated

        // 7. Verify order history shows the matched order
        mockMvc.perform(get(ORDERS_URL)
                .headers(customerHeaders))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[?(@.id==" + orderId + ")].status").value("MATCHED"));
    }

    @Test
    @DisplayName("Complete SELL order workflow with asset transfer")
    void testCompleteSellOrderWorkflow() throws Exception {
        HttpHeaders customerHeaders = createCustomer1Headers();
        HttpHeaders adminHeaders = createAdminHeaders();

        // 1. Check initial AAPL balance
        mockMvc.perform(get(ASSETS_URL)
                .headers(customerHeaders))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[?(@.assetName=='AAPL')].usableSize").value(hasItem(10.00)));

        // 2. Create SELL order
        CreateOrderRequest sellOrder = new CreateOrderRequest(
                CUSTOMER1_ID, "AAPL", OrderSide.SELL, BigDecimal.valueOf(3.0), BigDecimal.valueOf(155.00));

        MvcResult orderResult = mockMvc.perform(post(ORDERS_URL)
                .headers(customerHeaders)
                .contentType(MediaType.APPLICATION_JSON)
                .content(toJson(sellOrder)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("PENDING"))
                .andReturn();

        Long orderId = objectMapper.readTree(orderResult.getResponse().getContentAsString())
                .get("id").asLong();

        // 3. Verify AAPL is reserved after order creation
        mockMvc.perform(get(ASSETS_URL)
                .headers(customerHeaders))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[?(@.assetName=='AAPL')].usableSize").value(hasItem(7.00))); // 10 - 3

        // 4. Admin matches the order
        MatchOrderRequest matchRequest = new MatchOrderRequest(orderId);
        mockMvc.perform(post(ORDERS_MATCH_URL)
                .headers(adminHeaders)
                .contentType(MediaType.APPLICATION_JSON)
                .content(toJson(matchRequest)))
                .andExpect(status().isOk());

        // 5. Verify final balances
        mockMvc.perform(get(ASSETS_URL)
                .headers(customerHeaders))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[?(@.assetName=='AAPL')].size").value(hasItem(7.00))) // AAPL reduced
                .andExpect(jsonPath("$[?(@.assetName=='AAPL')].usableSize").value(hasItem(7.00)))
                .andExpect(jsonPath("$[?(@.assetName=='TRY')].size").value(hasItem(10465.00))) // TRY increased (10000 + 3*155)
                .andExpect(jsonPath("$[?(@.assetName=='TRY')].usableSize").value(hasItem(10465.00)));
    }

    @Test
    @DisplayName("Order cancellation workflow with asset release")
    void testOrderCancellationWorkflow() throws Exception {
        HttpHeaders customerHeaders = createCustomer1Headers();

        // 1. Check initial TRY balance
        MvcResult initialAssetsResult = mockMvc.perform(get(ASSETS_URL)
                .headers(customerHeaders))
                .andExpect(status().isOk())
                .andReturn();

        // 2. Create BUY order that reserves TRY
        CreateOrderRequest buyOrder = new CreateOrderRequest(
                CUSTOMER1_ID, "AAPL", OrderSide.BUY, BigDecimal.valueOf(4.0), BigDecimal.valueOf(100.00));

        MvcResult orderResult = mockMvc.perform(post(ORDERS_URL)
                .headers(customerHeaders)
                .contentType(MediaType.APPLICATION_JSON)
                .content(toJson(buyOrder)))
                .andExpect(status().isOk())
                .andReturn();

        Long orderId = objectMapper.readTree(orderResult.getResponse().getContentAsString())
                .get("id").asLong();

        // 3. Verify TRY is reserved
        mockMvc.perform(get(ASSETS_URL)
                .headers(customerHeaders))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[?(@.assetName=='TRY')].usableSize").value(hasItem(9600.00))); // 10000 - 400

        // 4. Cancel the order
        mockMvc.perform(delete(ORDERS_URL + "/" + orderId)
                .headers(customerHeaders))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("CANCELLED"));

        // 5. Verify TRY is released back
        mockMvc.perform(get(ASSETS_URL)
                .headers(customerHeaders))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[?(@.assetName=='TRY')].usableSize").value(hasItem(10000.00))); // Back to original
    }

    @Test
    @DisplayName("Admin comprehensive management workflow")
    void testAdminManagementWorkflow() throws Exception {
        HttpHeaders adminHeaders = createAdminHeaders();
        HttpHeaders customer1Headers = createCustomer1Headers();
        HttpHeaders customer2Headers = createCustomer2Headers();

        // 1. Admin login verification
        LoginRequest adminLogin = new LoginRequest(ADMIN_USERNAME, ADMIN_PASSWORD);
        MvcResult adminLoginResult = mockMvc.perform(post(AUTH_LOGIN_URL)
                .contentType(MediaType.APPLICATION_JSON)
                .content(toJson(adminLogin)))
                .andExpect(status().isOk())
                .andReturn();

        LoginResponse adminLoginResponse = fromJson(adminLoginResult.getResponse().getContentAsString(),
                LoginResponse.class);
        assertTrue(adminLoginResponse.getIsAdmin());

        // 2. Admin views all customer assets
        mockMvc.perform(get(ASSETS_URL)
                .headers(adminHeaders)
                .param("userId", CUSTOMER1_ID.toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[*].userId").value(everyItem(is(CUSTOMER1_ID.intValue()))));

        mockMvc.perform(get(ASSETS_URL)
                .headers(adminHeaders)
                .param("userId", CUSTOMER2_ID.toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[*].userId").value(everyItem(is(CUSTOMER2_ID.intValue()))));

        // 3. Admin creates orders for different customers
        CreateOrderRequest orderForCustomer1 = new CreateOrderRequest(
                CUSTOMER1_ID, "AAPL", OrderSide.BUY, BigDecimal.valueOf(1.0), BigDecimal.valueOf(150.00));

        CreateOrderRequest orderForCustomer2 = new CreateOrderRequest(
                CUSTOMER2_ID, "MSFT", OrderSide.BUY, BigDecimal.valueOf(2.0), BigDecimal.valueOf(400.00));

        MvcResult order1Result = mockMvc.perform(post(ORDERS_URL)
                .headers(adminHeaders)
                .contentType(MediaType.APPLICATION_JSON)
                .content(toJson(orderForCustomer1)))
                .andExpect(status().isOk())
                .andReturn();

        MvcResult order2Result = mockMvc.perform(post(ORDERS_URL)
                .headers(adminHeaders)
                .contentType(MediaType.APPLICATION_JSON)
                .content(toJson(orderForCustomer2)))
                .andExpect(status().isOk())
                .andReturn();

        Long order1Id = objectMapper.readTree(order1Result.getResponse().getContentAsString())
                .get("id").asLong();
        Long order2Id = objectMapper.readTree(order2Result.getResponse().getContentAsString())
                .get("id").asLong();

        // 4. Admin views all pending orders
        mockMvc.perform(get(ORDERS_PENDING_URL)
                .headers(adminHeaders))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[?(@.id==" + order1Id + ")].status").value("PENDING"))
                .andExpect(jsonPath("$[?(@.id==" + order2Id + ")].status").value("PENDING"));

        // 5. Admin matches one order and cancels another
        MatchOrderRequest matchRequest = new MatchOrderRequest(order1Id);
        mockMvc.perform(post(ORDERS_MATCH_URL)
                .headers(adminHeaders)
                .contentType(MediaType.APPLICATION_JSON)
                .content(toJson(matchRequest)))
                .andExpect(status().isOk());

        mockMvc.perform(delete(ORDERS_URL + "/" + order2Id)
                .headers(adminHeaders))
                .andExpect(status().isOk());

        // 6. Verify final order states
        mockMvc.perform(get(ORDERS_URL)
                .headers(adminHeaders))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[?(@.id==" + order1Id + ")].status").value("MATCHED"))
                .andExpect(jsonPath("$[?(@.id==" + order2Id + ")].status").value("CANCELLED"));
    }

    @Test
    @DisplayName("Multi-customer concurrent trading workflow")
    void testMultiCustomerTradingWorkflow() throws Exception {
        HttpHeaders customer1Headers = createCustomer1Headers();
        HttpHeaders customer2Headers = createCustomer2Headers();
        HttpHeaders adminHeaders = createAdminHeaders();

        // 1. Both customers check their initial assets
        mockMvc.perform(get(ASSETS_URL)
                .headers(customer1Headers))
                .andExpect(status().isOk());

        mockMvc.perform(get(ASSETS_URL)
                .headers(customer2Headers))
                .andExpect(status().isOk());

        // 2. Customer1 creates a buy order
        CreateOrderRequest customer1BuyOrder = new CreateOrderRequest(
                CUSTOMER1_ID, "GOOGL", OrderSide.BUY, BigDecimal.valueOf(1.0), BigDecimal.valueOf(2800.00));

        MvcResult customer1OrderResult = mockMvc.perform(post(ORDERS_URL)
                .headers(customer1Headers)
                .contentType(MediaType.APPLICATION_JSON)
                .content(toJson(customer1BuyOrder)))
                .andExpect(status().isOk())
                .andReturn();

        Long customer1OrderId = objectMapper.readTree(customer1OrderResult.getResponse().getContentAsString())
                .get("id").asLong();

        // 3. Customer2 creates a sell order
        CreateOrderRequest customer2SellOrder = new CreateOrderRequest(
                CUSTOMER2_ID, "MSFT", OrderSide.SELL, BigDecimal.valueOf(5.0), BigDecimal.valueOf(420.00));

        MvcResult customer2OrderResult = mockMvc.perform(post(ORDERS_URL)
                .headers(customer2Headers)
                .contentType(MediaType.APPLICATION_JSON)
                .content(toJson(customer2SellOrder)))
                .andExpect(status().isOk())
                .andReturn();

        Long customer2OrderId = objectMapper.readTree(customer2OrderResult.getResponse().getContentAsString())
                .get("id").asLong();

        // 4. Admin views all pending orders from both customers
        mockMvc.perform(get(ORDERS_PENDING_URL)
                .headers(adminHeaders))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[?(@.id==" + customer1OrderId + ")]").exists())
                .andExpect(jsonPath("$[?(@.id==" + customer2OrderId + ")]").exists());

        // 5. Admin matches both orders
        MatchOrderRequest matchRequest1 = new MatchOrderRequest(customer1OrderId);
        MatchOrderRequest matchRequest2 = new MatchOrderRequest(customer2OrderId);

        mockMvc.perform(post(ORDERS_MATCH_URL)
                .headers(adminHeaders)
                .contentType(MediaType.APPLICATION_JSON)
                .content(toJson(matchRequest1)))
                .andExpect(status().isOk());

        mockMvc.perform(post(ORDERS_MATCH_URL)
                .headers(adminHeaders)
                .contentType(MediaType.APPLICATION_JSON)
                .content(toJson(matchRequest2)))
                .andExpect(status().isOk());

        // 6. Both customers verify their updated assets
        mockMvc.perform(get(ASSETS_URL)
                .headers(customer1Headers))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[?(@.assetName=='GOOGL')].size").value(hasItem(6.00))); // 5 + 1

        mockMvc.perform(get(ASSETS_URL)
                .headers(customer2Headers))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[?(@.assetName=='MSFT')].size").value(hasItem(15.00))) // 20 - 5
                .andExpect(jsonPath("$[?(@.assetName=='TRY')].size").value(hasItem(17100.00))); // 15000 + (5 * 420)
    }

    @Test
    @DisplayName("Complex error recovery workflow")
    void testErrorRecoveryWorkflow() throws Exception {
        HttpHeaders customerHeaders = createCustomer1Headers();
        HttpHeaders adminHeaders = createAdminHeaders();

        // 1. Customer attempts to create order with insufficient balance
        CreateOrderRequest insufficientBalanceOrder = new CreateOrderRequest(
                CUSTOMER1_ID, "AAPL", OrderSide.BUY, BigDecimal.valueOf(100.0), BigDecimal.valueOf(200.00));

        mockMvc.perform(post(ORDERS_URL)
                .headers(customerHeaders)
                .contentType(MediaType.APPLICATION_JSON)
                .content(toJson(insufficientBalanceOrder)))
                .andExpect(status().isBadRequest())
                .andExpect(content().string(containsString("Insufficient usable balance")));

        // 2. Customer creates valid order
        CreateOrderRequest validOrder = new CreateOrderRequest(
                CUSTOMER1_ID, "AAPL", OrderSide.BUY, BigDecimal.valueOf(2.0), BigDecimal.valueOf(150.00));

        MvcResult orderResult = mockMvc.perform(post(ORDERS_URL)
                .headers(customerHeaders)
                .contentType(MediaType.APPLICATION_JSON)
                .content(toJson(validOrder)))
                .andExpect(status().isOk())
                .andReturn();

        Long orderId = objectMapper.readTree(orderResult.getResponse().getContentAsString())
                .get("id").asLong();

        // 3. Admin attempts to match non-existent order (should fail)
        MatchOrderRequest invalidMatchRequest = new MatchOrderRequest(99999L);
        mockMvc.perform(post(ORDERS_MATCH_URL)
                .headers(adminHeaders)
                .contentType(MediaType.APPLICATION_JSON)
                .content(toJson(invalidMatchRequest)))
                .andExpect(status().isBadRequest());

        // 4. Admin successfully matches the real order
        MatchOrderRequest validMatchRequest = new MatchOrderRequest(orderId);
        mockMvc.perform(post(ORDERS_MATCH_URL)
                .headers(adminHeaders)
                .contentType(MediaType.APPLICATION_JSON)
                .content(toJson(validMatchRequest)))
                .andExpect(status().isOk());

        // 5. Customer attempts to cancel already matched order (should fail)
        mockMvc.perform(delete(ORDERS_URL + "/" + orderId)
                .headers(customerHeaders))
                .andExpect(status().isBadRequest())
                .andExpect(content().string(containsString("cancelled")));

        // 6. Verify final state is consistent
        mockMvc.perform(get(ORDERS_URL)
                .headers(customerHeaders))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[?(@.id==" + orderId + ")].status").value("MATCHED"));
    }
}
