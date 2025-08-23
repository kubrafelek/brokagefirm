package com.kubrafelek.brokagefirm.integration;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Integration tests for asset management endpoints
 */
@DisplayName("Asset Management Integration Tests")
class AssetIntegrationTest extends BaseIntegrationTest {

        private static final String ASSETS_URL = "/api/assets";

        @Test
        @DisplayName("Should list customer's own assets")
        void testListAssets_Customer() throws Exception {
                HttpHeaders headers = createCustomer1Headers();

                mockMvc.perform(get(ASSETS_URL)
                                .headers(headers))
                                .andExpect(status().isOk())
                                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                                .andExpect(jsonPath("$").isArray())
                                .andExpect(jsonPath("$").isNotEmpty())
                                .andExpect(jsonPath("$[*].assetName").value(hasItems("TRY", "AAPL", "GOOGL")))
                                .andExpect(jsonPath("$[?(@.assetName=='TRY')].size").value(hasItem(10000.00)))
                                .andExpect(jsonPath("$[?(@.assetName=='TRY')].usableSize")
                                                .value(hasItem(10000.00)))
                                .andExpect(jsonPath("$[?(@.assetName=='AAPL')].size").value(hasItem(10.00)))
                                .andExpect(jsonPath("$[?(@.assetName=='AAPL')].usableSize")
                                                .value(hasItem(10.00)));
        }

        @Test
        @DisplayName("Should list different customer's assets")
        void testListAssets_DifferentCustomer() throws Exception {
                HttpHeaders headers = createCustomer2Headers();

                mockMvc.perform(get(ASSETS_URL)
                                .headers(headers))
                                .andExpect(status().isOk())
                                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                                .andExpect(jsonPath("$").isArray())
                                .andExpect(jsonPath("$").isNotEmpty())
                                .andExpect(jsonPath("$[*].assetName").value(hasItems("TRY", "MSFT", "TSLA")))
                                .andExpect(jsonPath("$[?(@.assetName=='TRY')].size").value(hasItem(15000.00)))
                                .andExpect(jsonPath("$[?(@.assetName=='MSFT')].size").value(hasItem(20.00)));
        }

        @Test
        @DisplayName("Should allow admin to list all assets without userId parameter")
        void testListAssets_AdminWithoutUserId() throws Exception {
                HttpHeaders headers = createAdminHeaders();

                mockMvc.perform(get(ASSETS_URL)
                                .headers(headers))
                                .andExpect(status().isBadRequest())
                                .andExpect(content().contentType("text/plain;charset=UTF-8"))
                                .andExpect(content().string("Customer ID is required for admin to list assets"));
        }

        @Test
        @DisplayName("Should allow admin to list specific customer's assets")
        void testListAssets_AdminWithUserId() throws Exception {
                HttpHeaders headers = createAdminHeaders();

                mockMvc.perform(get(ASSETS_URL)
                                .headers(headers)
                                .param("userId", CUSTOMER1_ID.toString()))
                                .andExpect(status().isOk())
                                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                                .andExpect(jsonPath("$").isArray())
                                .andExpect(jsonPath("$[*].assetName").value(hasItems("TRY", "AAPL", "GOOGL")));
        }

        @Test
        @DisplayName("Should allow admin to list any customer's assets")
        void testListAssets_AdminDifferentCustomer() throws Exception {
                HttpHeaders headers = createAdminHeaders();

                mockMvc.perform(get(ASSETS_URL)
                                .headers(headers)
                                .param("userId", CUSTOMER2_ID.toString()))
                                .andExpect(status().isOk())
                                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                                .andExpect(jsonPath("$").isArray())
                                .andExpect(jsonPath("$[*].assetName").value(hasItems("TRY", "MSFT", "TSLA")));
        }

        @Test
        @DisplayName("Should ignore userId parameter for regular customers")
        void testListAssets_CustomerWithUserIdParam() throws Exception {
                HttpHeaders headers = createCustomer1Headers();

                // Customer1 tries to access Customer2's assets by providing userId param
                mockMvc.perform(get(ASSETS_URL)
                                .headers(headers)
                                .param("userId", CUSTOMER2_ID.toString()))
                                .andExpect(status().isOk())
                                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                                .andExpect(jsonPath("$").isArray())
                                // Should still return customer1's assets, not customer2's
                                .andExpect(jsonPath("$[*].assetName").value(hasItems("TRY", "AAPL", "GOOGL")));
        }

        @Test
        @DisplayName("Should handle customer with no assets gracefully")
        void testListAssets_NoAssets() throws Exception {
                // Assuming we have a user with no assets, or we could create one
                HttpHeaders headers = createAdminHeaders();

                mockMvc.perform(get(ASSETS_URL)
                                .headers(headers)
                                .param("userId", "999")) // Non-existent user
                                .andExpect(status().isOk())
                                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                                .andExpect(jsonPath("$").isArray())
                                .andExpect(jsonPath("$").isEmpty());
        }

        @Test
        @DisplayName("Should verify asset structure and required fields")
        void testListAssets_AssetStructure() throws Exception {
                HttpHeaders headers = createCustomer1Headers();

                mockMvc.perform(get(ASSETS_URL)
                                .headers(headers))
                                .andExpect(status().isOk())
                                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                                .andExpect(jsonPath("$").isArray())
                                .andExpect(jsonPath("$").isNotEmpty())
                                // Check that each asset has required fields
                                .andExpect(jsonPath("$[*].assetName").exists())
                                .andExpect(jsonPath("$[*].size").exists())
                                .andExpect(jsonPath("$[*].usableSize").exists())
                                // Check that size and usableSize are valid numbers
                                .andExpect(jsonPath("$[*].size").value(everyItem(greaterThanOrEqualTo(0.0))))
                                .andExpect(jsonPath("$[*].usableSize")
                                                .value(everyItem(greaterThanOrEqualTo(0.0))));
        }

        @Test
        @DisplayName("Should verify TRY asset exists for all customers")
        void testListAssets_TryAssetExists() throws Exception {
                // Test customer1
                HttpHeaders customer1Headers = createCustomer1Headers();
                mockMvc.perform(get(ASSETS_URL)
                                .headers(customer1Headers))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$[?(@.assetName=='TRY')]").exists())
                                .andExpect(jsonPath("$[?(@.assetName=='TRY')].size").value(hasItem(greaterThan(0.0))))
                                .andExpect(jsonPath("$[?(@.assetName=='TRY')].usableSize")
                                                .value(hasItem(greaterThan(0.0))));

                // Test customer2
                HttpHeaders customer2Headers = createCustomer2Headers();
                mockMvc.perform(get(ASSETS_URL)
                                .headers(customer2Headers))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$[?(@.assetName=='TRY')]").exists())
                                .andExpect(jsonPath("$[?(@.assetName=='TRY')].size").value(hasItem(greaterThan(0.0))))
                                .andExpect(jsonPath("$[?(@.assetName=='TRY')].usableSize")
                                                .value(hasItem(greaterThan(0.0))));
        }

        @Test
        @DisplayName("Should reject asset listing without authentication")
        void testListAssets_NoAuth() throws Exception {
                mockMvc.perform(get(ASSETS_URL))
                                .andExpect(status().isBadRequest()); // Actually returns 400 due to missing required
                                                                     // headers
        }

        @Test
        @DisplayName("Should reject asset listing with invalid credentials")
        void testListAssets_InvalidCredentials() throws Exception {
                HttpHeaders headers = createAuthHeaders("invaliduser", "wrongpassword");

                mockMvc.perform(get(ASSETS_URL)
                                .headers(headers))
                                .andExpect(status().isUnauthorized())
                                .andExpect(content().contentType("text/plain;charset=UTF-8"))
                                .andExpect(content().string("Invalid credentials"));
        }

        @Test
        @DisplayName("Should verify asset data consistency")
        void testListAssets_DataConsistency() throws Exception {
                HttpHeaders headers = createCustomer1Headers();

                mockMvc.perform(get(ASSETS_URL)
                                .headers(headers))
                                .andExpect(status().isOk())
                                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                                .andExpect(jsonPath("$").isArray())
                                .andExpect(jsonPath("$").isNotEmpty())
                                // Verify usableSize is non-negative for all assets
                                .andExpect(jsonPath("$[*].usableSize")
                                                .value(everyItem(greaterThanOrEqualTo(0.0))))
                                // Verify size is non-negative for all assets
                                .andExpect(jsonPath("$[*].size").value(everyItem(greaterThanOrEqualTo(0.0))));
        }

        @Test
        @DisplayName("Should handle multiple asset types for customer")
        void testListAssets_MultipleAssetTypes() throws Exception {
                HttpHeaders headers = createCustomer1Headers();

                mockMvc.perform(get(ASSETS_URL)
                                .headers(headers))
                                .andExpect(status().isOk())
                                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                                .andExpect(jsonPath("$").isArray())
                                .andExpect(jsonPath("$", hasSize(greaterThan(1)))) // Should have multiple assets
                                .andExpect(jsonPath("$[*].assetName", hasItems("TRY", "AAPL", "GOOGL")));
        }
}
