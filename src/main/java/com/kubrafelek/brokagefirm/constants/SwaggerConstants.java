package com.kubrafelek.brokagefirm.constants;

/**
 * Constants class containing all Swagger API documentation strings
 * for centralized management and consistency across the application.
 */
public final class SwaggerConstants {

    private SwaggerConstants() {
    }

    public static final class ResponseCodes {
        public static final String OK = "200";
        public static final String BAD_REQUEST = "400";
        public static final String UNAUTHORIZED = "401";
        public static final String FORBIDDEN = "403";
        public static final String NOT_FOUND = "404";
        public static final String INTERNAL_SERVER_ERROR = "500";
    }

    public static final class ResponseDescriptions {
        public static final String LOGIN_SUCCESSFUL = "Login successful";
        public static final String ORDER_CREATED_SUCCESSFULLY = "Order created successfully";
        public static final String ORDERS_RETRIEVED_SUCCESSFULLY = "Orders retrieved successfully";
        public static final String ORDER_CANCELLED_SUCCESSFULLY = "Order cancelled successfully";
        public static final String ORDER_MATCHED_SUCCESSFULLY = "Order matched successfully";
        public static final String PENDING_ORDERS_RETRIEVED_SUCCESSFULLY = "Pending orders retrieved successfully";
        public static final String ASSETS_RETRIEVED_SUCCESSFULLY = "Assets retrieved successfully";
        public static final String API_INFO_RETRIEVED_SUCCESSFULLY = "API information retrieved successfully";
        public static final String SERVICE_HEALTHY = "Service is healthy";

        public static final String INVALID_CREDENTIALS = "Invalid credentials";
        public static final String INTERNAL_SERVER_ERROR = "Internal server error";
        public static final String INVALID_ORDER_DATA = "Invalid order data";
        public static final String NOT_AUTHORIZED_CREATE_ORDER = "Not authorized to create order for this customer";
        public static final String ORDER_CANNOT_BE_CANCELLED = "Order cannot be cancelled";
        public static final String ORDER_NOT_FOUND = "Order not found";
        public static final String ORDER_CANNOT_BE_MATCHED = "Order cannot be matched";
        public static final String ONLY_ADMIN_MATCH_ORDERS = "Only admin users can match orders";
        public static final String ONLY_ADMIN_VIEW_PENDING_ORDERS = "Only admin users can view pending orders";
        public static final String CUSTOMER_ID_REQUIRED_ADMIN = "Customer ID is required for admin users";
    }

    public static final class OperationSummaries {
        public static final String USER_LOGIN = "User login";
        public static final String CREATE_NEW_ORDER = "Create a new order";
        public static final String LIST_ORDERS = "List orders";
        public static final String CANCEL_ORDER = "Cancel an order";
        public static final String MATCH_ORDER = "Match an order";
        public static final String LIST_PENDING_ORDERS = "List pending orders";
        public static final String LIST_CUSTOMER_ASSETS = "List customer assets";
        public static final String API_INFORMATION = "API information";
        public static final String HEALTH_CHECK = "Health check";
    }

    public static final class OperationDescriptions {
        public static final String USER_LOGIN_DESC = "Authenticate a user with username and password. Returns user information and authentication status.";
        public static final String CREATE_ORDER_DESC = "Create a new trading order. Admins can create orders for any customer, while regular customers can only create orders for themselves.";
        public static final String LIST_ORDERS_DESC = "Retrieve orders with optional filtering by customer ID and date range. Admins can view all orders, while regular customers can only see their own.";
        public static final String CANCEL_ORDER_DESC = "Cancel a pending order. Customers can only cancel their own orders, while admins can cancel any order.";
        public static final String MATCH_ORDER_DESC = "Match a pending order with available assets. Only admin users can perform this operation.";
        public static final String LIST_PENDING_ORDERS_DESC = "Retrieve all pending orders in the system. Only admin users can access this endpoint.";
        public static final String LIST_ASSETS_DESC = "Retrieve assets for a customer. Admins can specify any customer ID, while regular customers can only see their own assets.";
        public static final String API_INFO_DESC = "Get API information, available endpoints, test accounts, and usage examples";
        public static final String HEALTH_CHECK_DESC = "Check the health status of the API";
    }

    public static final class ParameterDescriptions {
        public static final String LOGIN_CREDENTIALS = "Login credentials";
        public static final String ORDER_CREATION_REQUEST = "Order creation request";
        public static final String MATCH_ORDER_REQUEST = "Match order request";
        public static final String USERNAME_AUTH = "Username for authentication";
        public static final String PASSWORD_AUTH = "Password for authentication";
        public static final String CUSTOMER_ID_FILTER = "Customer ID to filter orders (admin only)";
        public static final String CUSTOMER_ID_ADMIN_REQUIRED = "Customer ID (required for admin users, ignored for regular customers)";
        public static final String START_DATE_FILTER = "Start date for filtering orders (ISO datetime format)";
        public static final String END_DATE_FILTER = "End date for filtering orders (ISO datetime format)";
        public static final String ORDER_ID_TO_CANCEL = "ID of the order to cancel";
        public static final String ORDER_ID_TO_MATCH = "ID of the order to match";
    }

    public static final class Tags {
        public static final String AUTHENTICATION_NAME = "Authentication";
        public static final String AUTHENTICATION_DESC = "User authentication operations";

        public static final String ORDERS_NAME = "Orders";
        public static final String ORDERS_DESC = "Order management operations for trading";

        public static final String ASSETS_NAME = "Assets";
        public static final String ASSETS_DESC = "Asset management operations";

        public static final String API_INFO_NAME = "API Info";
        public static final String API_INFO_DESC = "API information and health check endpoints";
    }

    public static final class ErrorMessages {
        public static final String INVALID_CREDENTIALS = "Invalid credentials";
        public static final String LOGIN_FAILED = "Login failed: ";
        public static final String FAILED_TO_CREATE_ORDER = "Failed to create order: ";
        public static final String FAILED_TO_LIST_ORDERS = "Failed to list orders: ";
        public static final String FAILED_TO_CANCEL_ORDER = "Failed to cancel order: ";
        public static final String FAILED_TO_MATCH_ORDER = "Failed to match order: ";
        public static final String FAILED_TO_LIST_PENDING_ORDERS = "Failed to list pending orders: ";
        public static final String FAILED_TO_LIST_ASSETS = "Failed to list assets: ";
        public static final String YOU_CAN_ONLY_CREATE_ORDERS_FOR_YOURSELF = "You can only create orders for yourself";
        public static final String ONLY_ADMIN_USERS_CAN_MATCH_ORDERS = "Only admin users can match orders";
        public static final String ONLY_ADMIN_USERS_CAN_VIEW_PENDING_ORDERS = "Only admin users can view all pending orders";
        public static final String CUSTOMER_ID_REQUIRED_FOR_ADMIN = "Customer ID is required for admin to list assets";
    }

    public static final class SuccessMessages {
        public static final String LOGIN_SUCCESSFUL = "Login successful";
    }
}
