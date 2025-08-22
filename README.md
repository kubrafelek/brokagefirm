# Brokerage Firm Backend API

A comprehensive Spring Boot application for managing stock orders in a brokerage firm. This application provides REST APIs for order management, asset tracking, and customer authentication with proper authorization controls.

## Features

### Core Functionality
- **Order Management**: Create, list, and cancel stock orders
- **Asset Management**: Track customer assets including TRY (Turkish Lira) and stocks
- **Customer Authentication**: Secure login system with admin and customer roles
- **Order Matching**: Admin capability to match pending orders
- **Balance Validation**: Automatic balance checks for order creation and cancellation

### API Endpoints

#### Authentication
- `POST /api/auth/login` - Customer/Admin login

#### Order Management
- `POST /api/orders` - Create a new order
- `GET /api/orders` - List orders (with optional filters: customerId, startDate, endDate)
- `DELETE /api/orders/{orderId}` - Cancel a pending order
- `POST /api/orders/match` - Match a pending order (Admin only)
- `GET /api/orders/pending` - List all pending orders (Admin only)

#### Asset Management
- `GET /api/assets` - List customer assets (with optional customerId filter for admin)

### Business Rules

1. **Order Creation**:
   - BUY orders require sufficient TRY balance
   - SELL orders require sufficient asset balance
   - All orders start with PENDING status
   - Assets are reserved when orders are created

2. **Order Cancellation**:
   - Only PENDING orders can be cancelled
   - Customers can only cancel their own orders
   - Admin can cancel any order
   - Reserved assets are released upon cancellation

3. **Order Matching**:
   - Only admin users can match orders
   - Only PENDING orders can be matched
   - BUY orders: TRY is transferred, asset is added to customer portfolio
   - SELL orders: Asset is transferred, TRY is added to customer portfolio

4. **Authorization**:
   - Admin users can access all data and perform all operations
   - Regular customers can only access their own data
   - Basic authentication using username/password headers

## Technology Stack

- **Framework**: Spring Boot 3.5.5
- **Database**: H2 (In-memory for development)
- **Security**: Spring Security with BCrypt password encoding
- **Persistence**: Spring Data JPA with Hibernate
- **Testing**: JUnit 5 + Mockito
- **Build Tool**: Maven

## Database Schema

### Entities

1. **Customer**
   - id (Primary Key)
   - username (Unique)
   - password (BCrypt encoded)
   - isAdmin (Boolean)

2. **Asset**
   - id (Primary Key)
   - customerId
   - assetName
   - size (Total amount)
   - usableSize (Available for trading)
   - Unique constraint: (customerId, assetName)

3. **Order**
   - id (Primary Key)
   - customerId
   - assetName
   - orderSide (BUY/SELL)
   - size
   - price
   - status (PENDING/MATCHED/CANCELED)
   - createDate

## Getting Started

### Prerequisites
- Java 22 or later
- Maven 3.6+

### Build and Run

1. **Clone the repository**
   ```bash
   git clone <repository-url>
   cd brokagefirm
   ```

2. **Build the application**
   ```bash
   mvn clean compile
   ```

3. **Run tests**
   ```bash
   mvn test
   ```

4. **Start the application**
   ```bash
   mvn spring-boot:run
   ```

   The application will start on `http://localhost:8080`

5. **Access H2 Console** (for database inspection)
   - URL: `http://localhost:8080/h2-console`
   - JDBC URL: `jdbc:h2:mem:brokagedb`
   - Username: `sa`
   - Password: `password`

### Initial Data

The application automatically creates initial test data:

**Admin User:**
- Username: `admin`
- Password: `admin123`

**Test Customers:**
- Username: `customer1`, Password: `pass123` (ID: 2)
  - TRY: 10,000.00
  - AAPL: 10.00 shares
- Username: `customer2`, Password: `pass123` (ID: 3)
  - TRY: 15,000.00
  - GOOGL: 5.00 shares

## API Usage Examples

### 1. Login
```bash
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username": "customer1", "password": "pass123"}'
```

### 2. Create Buy Order
```bash
curl -X POST http://localhost:8080/api/orders \
  -H "Content-Type: application/json" \
  -H "Username: customer1" \
  -H "Password: pass123" \
  -d '{
    "customerId": 2,
    "assetName": "AAPL",
    "side": "BUY",
    "size": 5.00,
    "price": 150.00
  }'
```

### 3. List Orders
```bash
curl -X GET "http://localhost:8080/api/orders" \
  -H "Username: customer1" \
  -H "Password: pass123"
```

### 4. List Assets
```bash
curl -X GET "http://localhost:8080/api/assets" \
  -H "Username: customer1" \
  -H "Password: pass123"
```

### 5. Cancel Order (Admin)
```bash
curl -X DELETE http://localhost:8080/api/orders/1 \
  -H "Username: admin" \
  -H "Password: admin123"
```

### 6. Match Order (Admin Only)
```bash
curl -X POST http://localhost:8080/api/orders/match \
  -H "Content-Type: application/json" \
  -H "Username: admin" \
  -H "Password: admin123" \
  -d '{"orderId": 1}'
```

## Testing

The application includes comprehensive unit tests for all service layers:

- **AssetServiceTest**: Tests asset management operations
- **CustomerServiceTest**: Tests authentication and customer operations
- **OrderServiceTest**: Tests order creation, cancellation, and matching
- **ApplicationTests**: Integration test for Spring Boot context

Run tests with: `mvn test`

## Production Considerations

For production deployment, consider:

1. **Database**: Replace H2 with a persistent database (PostgreSQL, MySQL)
2. **Security**: Implement JWT tokens instead of basic authentication
3. **Configuration**: Externalize configuration using environment variables
4. **Monitoring**: Add logging, metrics, and health checks
5. **API Documentation**: Add Swagger/OpenAPI documentation
6. **Input Validation**: Enhanced validation and error handling
7. **Rate Limiting**: Implement API rate limiting
8. **Data Migration**: Add database migration scripts (Flyway/Liquibase)

## Architecture

The application follows a layered architecture:

- **Controller Layer**: REST endpoints and request/response handling
- **Service Layer**: Business logic and transaction management
- **Repository Layer**: Data access using Spring Data JPA
- **Entity Layer**: JPA entities representing database tables
- **DTO Layer**: Data transfer objects for API communication
- **Configuration Layer**: Security and application configuration

## Error Handling

The application provides appropriate error responses for:
- Invalid credentials (401 Unauthorized)
- Insufficient permissions (403 Forbidden)
- Invalid input data (400 Bad Request)
- Resource not found (404 Not Found)
- Business rule violations (400 Bad Request with descriptive messages)

## License

This project is developed for educational and demonstration purposes.
