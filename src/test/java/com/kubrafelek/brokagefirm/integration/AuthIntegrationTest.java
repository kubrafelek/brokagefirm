package com.kubrafelek.brokagefirm.integration;

import com.kubrafelek.brokagefirm.dto.LoginRequest;
import com.kubrafelek.brokagefirm.dto.LoginResponse;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MvcResult;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Integration tests for authentication endpoints
 */
@DisplayName("Authentication Integration Tests")
class AuthIntegrationTest extends BaseIntegrationTest {

    private static final String AUTH_LOGIN_URL = "/api/auth/login";

    @Test
    @DisplayName("Should authenticate admin user successfully")
    void testAdminLogin_Success() throws Exception {
        LoginRequest loginRequest = new LoginRequest(ADMIN_USERNAME, ADMIN_PASSWORD);

        MvcResult result = mockMvc.perform(post(AUTH_LOGIN_URL)
                .contentType(MediaType.APPLICATION_JSON)
                .content(toJson(loginRequest)))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.message").value("Login successful"))
                .andExpect(jsonPath("$.userId").value(ADMIN_ID))
                .andExpect(jsonPath("$.isAdmin").value(true))
                .andReturn();

        String responseBody = result.getResponse().getContentAsString();
        LoginResponse response = fromJson(responseBody, LoginResponse.class);

        assertEquals("Login successful", response.getMessage());
        assertEquals(ADMIN_ID, response.getUserId());
        assertTrue(response.getIsAdmin());
    }

    @Test
    @DisplayName("Should authenticate customer user successfully")
    void testCustomerLogin_Success() throws Exception {
        LoginRequest loginRequest = new LoginRequest(CUSTOMER1_USERNAME, CUSTOMER1_PASSWORD);

        MvcResult result = mockMvc.perform(post(AUTH_LOGIN_URL)
                .contentType(MediaType.APPLICATION_JSON)
                .content(toJson(loginRequest)))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.message").value("Login successful"))
                .andExpect(jsonPath("$.userId").value(CUSTOMER1_ID))
                .andExpect(jsonPath("$.isAdmin").value(false))
                .andReturn();

        String responseBody = result.getResponse().getContentAsString();
        LoginResponse response = fromJson(responseBody, LoginResponse.class);

        assertEquals("Login successful", response.getMessage());
        assertEquals(CUSTOMER1_ID, response.getUserId());
        assertFalse(response.getIsAdmin());
    }

    @Test
    @DisplayName("Should reject login with invalid credentials")
    void testLogin_InvalidCredentials() throws Exception {
        LoginRequest loginRequest = new LoginRequest("invaliduser", "wrongpassword");

        mockMvc.perform(post(AUTH_LOGIN_URL)
                .contentType(MediaType.APPLICATION_JSON)
                .content(toJson(loginRequest)))
                .andExpect(status().isUnauthorized())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.error").value("Invalid credentials"));
    }

    @Test
    @DisplayName("Should reject login with wrong password")
    void testLogin_WrongPassword() throws Exception {
        LoginRequest loginRequest = new LoginRequest(CUSTOMER1_USERNAME, "wrongpassword");

        mockMvc.perform(post(AUTH_LOGIN_URL)
                .contentType(MediaType.APPLICATION_JSON)
                .content(toJson(loginRequest)))
                .andExpect(status().isUnauthorized())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.error").value("Invalid credentials"));
    }

    @Test
    @DisplayName("Should reject login with empty username")
    void testLogin_EmptyUsername() throws Exception {
        LoginRequest loginRequest = new LoginRequest("", CUSTOMER1_PASSWORD);

        mockMvc.perform(post(AUTH_LOGIN_URL)
                .contentType(MediaType.APPLICATION_JSON)
                .content(toJson(loginRequest)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Should reject login with empty password")
    void testLogin_EmptyPassword() throws Exception {
        LoginRequest loginRequest = new LoginRequest(CUSTOMER1_USERNAME, "");

        mockMvc.perform(post(AUTH_LOGIN_URL)
                .contentType(MediaType.APPLICATION_JSON)
                .content(toJson(loginRequest)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Should reject login with null request body")
    void testLogin_NullRequestBody() throws Exception {
        mockMvc.perform(post(AUTH_LOGIN_URL)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Should reject login with malformed JSON")
    void testLogin_MalformedJson() throws Exception {
        mockMvc.perform(post(AUTH_LOGIN_URL)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{invalid json}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Should authenticate multiple different users")
    void testLogin_MultipleUsers() throws Exception {
        // Test customer1
        LoginRequest customer1Login = new LoginRequest(CUSTOMER1_USERNAME, CUSTOMER1_PASSWORD);
        mockMvc.perform(post(AUTH_LOGIN_URL)
                .contentType(MediaType.APPLICATION_JSON)
                .content(toJson(customer1Login)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.userId").value(CUSTOMER1_ID))
                .andExpect(jsonPath("$.isAdmin").value(false));

        // Test customer2
        LoginRequest customer2Login = new LoginRequest(CUSTOMER2_USERNAME, CUSTOMER2_PASSWORD);
        mockMvc.perform(post(AUTH_LOGIN_URL)
                .contentType(MediaType.APPLICATION_JSON)
                .content(toJson(customer2Login)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.userId").value(CUSTOMER2_ID))
                .andExpect(jsonPath("$.isAdmin").value(false));

        // Test admin
        LoginRequest adminLogin = new LoginRequest(ADMIN_USERNAME, ADMIN_PASSWORD);
        mockMvc.perform(post(AUTH_LOGIN_URL)
                .contentType(MediaType.APPLICATION_JSON)
                .content(toJson(adminLogin)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.userId").value(ADMIN_ID))
                .andExpect(jsonPath("$.isAdmin").value(true));
    }
}
