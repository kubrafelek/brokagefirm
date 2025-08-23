package com.kubrafelek.brokagefirm.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpHeaders;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

/**
 * Base class for integration tests providing common configuration and utilities
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
@ActiveProfiles("test")
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
@Transactional
public abstract class BaseIT {

    @Autowired
    protected MockMvc mockMvc;

    @Autowired
    protected ObjectMapper objectMapper;

    // Test user credentials
    protected static final String ADMIN_USERNAME = "admin";
    protected static final String ADMIN_PASSWORD = "admin123";
    protected static final String CUSTOMER1_USERNAME = "customer1";
    protected static final String CUSTOMER1_PASSWORD = "pass123";
    protected static final String CUSTOMER2_USERNAME = "customer2";
    protected static final String CUSTOMER2_PASSWORD = "pass123";
    protected static final String TEST_USERNAME = "testuser";
    protected static final String TEST_PASSWORD = "pass123";

    // Test user IDs (based on data.sql)
    protected static final Long ADMIN_ID = 1L;
    protected static final Long CUSTOMER1_ID = 2L;
    protected static final Long CUSTOMER2_ID = 3L;
    protected static final Long TEST_USER_ID = 4L;

    @BeforeEach
    void setUp() {
        // MockMvc is automatically configured with @AutoConfigureMockMvc
    }

    /**
     * Create HTTP headers with basic authentication
     */
    protected HttpHeaders createAuthHeaders(String username, String password) {
        HttpHeaders headers = new HttpHeaders();
        headers.set("Username", username);
        headers.set("Password", password);
        headers.set("Content-Type", "application/json");
        return headers;
    }

    /**
     * Create admin authentication headers
     */
    protected HttpHeaders createAdminHeaders() {
        return createAuthHeaders(ADMIN_USERNAME, ADMIN_PASSWORD);
    }

    /**
     * Create customer1 authentication headers
     */
    protected HttpHeaders createCustomer1Headers() {
        return createAuthHeaders(CUSTOMER1_USERNAME, CUSTOMER1_PASSWORD);
    }

    /**
     * Create customer2 authentication headers
     */
    protected HttpHeaders createCustomer2Headers() {
        return createAuthHeaders(CUSTOMER2_USERNAME, CUSTOMER2_PASSWORD);
    }

    /**
     * Create test user authentication headers
     */
    protected HttpHeaders createTestUserHeaders() {
        return createAuthHeaders(TEST_USERNAME, TEST_PASSWORD);
    }

    /**
     * Convert object to JSON string
     */
    protected String toJson(Object object) throws Exception {
        return objectMapper.writeValueAsString(object);
    }

    /**
     * Convert JSON string to object
     */
    protected <T> T fromJson(String json, Class<T> clazz) throws Exception {
        return objectMapper.readValue(json, clazz);
    }
}
