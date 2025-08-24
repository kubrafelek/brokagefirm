# 📈 Brokage Firm Management System

<div align="center">

![Java](https://img.shields.io/badge/Java-ED8B00?style=for-the-badge&logo=java&logoColor=white)
![Spring Boot](https://img.shields.io/badge/Spring_Boot-F2F4F9?style=for-the-badge&logo=spring-boot)
![React](https://img.shields.io/badge/React-20232A?style=for-the-badge&logo=react&logoColor=61DAFB)
![Bootstrap](https://img.shields.io/badge/Bootstrap-563D7C?style=for-the-badge&logo=bootstrap&logoColor=white)
![H2](https://img.shields.io/badge/H2-Database-blue?style=for-the-badge)

A comprehensive full-stack application for managing stock orders in a Brokage firm with modern web interface and robust REST APIs.

</div>

---

## 🌟 Features

### 🖥️ Web User Interface
- **🔐 Authentication**: Secure login for customers and administrators
- **📊 Dashboard**: Real-time overview of assets and portfolio performance
- **📈 Order Management**: Intuitive interface for creating, viewing, and managing orders
- **💰 Asset Tracking**: Visual representation of asset holdings and balances
- **👑 Admin Panel**: Comprehensive administrative tools for order matching and system management
- **📱 Responsive Design**: Mobile-friendly interface using Bootstrap

### ⚙️ Backend API
- **🔒 Secure Authentication**: JWT-based authentication with role-based access control
- **📋 Order Management**: Create, list, cancel, and match stock orders
- **💼 Asset Management**: Track customer assets including TRY (Turkish Lira) and stocks
- **🔐 Customer Authentication**: Secure login system with admin and customer roles
- **⚖️ Order Matching**: Admin capability to match pending orders
- **✅ Balance Validation**: Automatic balance checks for order creation and cancellation

---

## 🛠️ Technology Stack

### Frontend 🎨
- **Framework**: React 18.2.0
- **UI Library**: React Bootstrap 2.5.0 + Bootstrap 5.2.0
- **Routing**: React Router DOM 6.3.0
- **HTTP Client**: Axios 0.27.2
- **Charts**: Chart.js 3.9.1 + React Chart.js 2
- **Notifications**: React Toastify 9.0.8
- **Date Handling**: Moment.js 2.29.4
- **Testing**: Jest + React Testing Library

### Backend ⚡
- **Framework**: Spring Boot 3.5.5
- **Database**: H2 (In-memory for development)
- **Security**: Spring Security with BCrypt password encoding
- **Persistence**: Spring Data JPA with Hibernate
- **Testing**: JUnit 5 + Mockito
- **Build Tool**: Maven

---

## 🚀 Installation and Setup

### Prerequisites 📋
- **Java**: 22 or later
- **Maven**: 3.6+
- **Node.js**: 16+ 
- **npm**: 8+

### Backend Setup 🖥️

1. **📥 Clone the repository:**
   ```bash
   git clone https://github.com/your-username/brokagefirm.git
   cd brokagefirm
   ```

2. **🔧 Build the application:**
   ```bash
   mvn clean compile
   ```

3. **🧪 Run tests:**
   ```bash
   mvn test
   ```

4. **🚀 Start the backend server:**
   ```bash
   mvn spring-boot:run
   ```
   The backend API will be running at `http://localhost:8080` 🌐

5. **🗄️ Access H2 Console** (for database inspection):
   - URL: `http://localhost:8080/h2-console`
   - JDBC URL: `jdbc:h2:mem:brokagedb`
   - Username: `sa`
   - Password: `password`

### Frontend Setup 🎨

1. **📂 Navigate to the web directory:**
   ```bash
   cd web
   ```

2. **📦 Install dependencies:**
   ```bash
   npm install
   ```

3. **🚀 Start the development server:**
   ```bash
   npm start
   ```
   The React application will open in your browser at `http://localhost:3000` 🌐

4. **🏗️ Build for production:**
   ```bash
   npm run build
   ```

---

## 👥 Default Users & Test Data

The application automatically creates initial test data:

### 👑 Admin User
- **Username**: `admin`
- **Password**: `admin123`
- **Capabilities**: Full system access, order matching, customer management

### 👤 Test Customers

**Customer 1:**
- **Username**: `customer1`
- **Password**: `pass123`
- **Assets**: 
  - 💵 TRY: 10,000.00
  - 📈 AAPL: 10.00 shares

**Customer 2:**
- **Username**: `customer2` 
- **Password**: `pass123`
- **Assets**:
  - 💵 TRY: 15,000.00
  - 📈 GOOGL: 5.00 shares

---

## 🌐 API Endpoints

### 🔐 Authentication
| Method | Endpoint | Description | Access |
|--------|----------|-------------|---------|
| `POST` | `/api/auth/login` | Customer/Admin login | Public |

### 📋 Order Management
| Method | Endpoint | Description | Access |
|--------|----------|-------------|---------|
| `POST` | `/api/orders` | Create a new order | Authenticated |
| `GET` | `/api/orders` | List orders (with filters) | Authenticated |
| `DELETE` | `/api/orders/{orderId}` | Cancel a pending order | Owner/Admin |
| `POST` | `/api/orders/match` | Match a pending order | Admin Only |
| `GET` | `/api/orders/pending` | List all pending orders | Admin Only |

### 💼 Asset Management
| Method | Endpoint | Description | Access |
|--------|----------|-------------|---------|
| `GET` | `/api/assets` | List customer assets | Authenticated |

---

## 📱 Web UI Features

### 🏠 Dashboard
- **Portfolio Overview**: Visual charts showing asset distribution
- **Recent Orders**: Quick view of latest trading activity
- **Account Balance**: Real-time balance information
- **Performance Metrics**: Portfolio performance indicators

### 📊 Order Management
- **Create Orders**: User-friendly form for placing BUY/SELL orders
- **Order History**: Comprehensive list with filtering and sorting
- **Order Status**: Real-time status updates (PENDING, MATCHED, CANCELED)
- **Quick Actions**: Cancel pending orders with one click

### 💰 Asset Portfolio
- **Asset Overview**: Detailed view of all holdings
- **Balance Tracking**: Total and usable balance for each asset
- **Transaction History**: Complete audit trail of all transactions

### 👑 Admin Panel
- **System Overview**: Global system statistics and metrics
- **Order Matching**: Interface for matching pending orders
- **Customer Management**: View and manage all customer accounts
- **System Monitoring**: Real-time system health and performance

---

## 📖 API Usage Examples

### 1. 🔑 Login
```bash
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username": "customer1", "password": "pass123"}'
```

### 2. 🛒 Create Buy Order
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

### 3. 📋 List Orders
```bash
curl -X GET "http://localhost:8080/api/orders" \
  -H "Username: customer1" \
  -H "Password: pass123"
```

### 4. 💼 List Assets
```bash
curl -X GET "http://localhost:8080/api/assets" \
  -H "Username: customer1" \
  -H "Password: pass123"
```

### 5. ❌ Cancel Order
```bash
curl -X DELETE http://localhost:8080/api/orders/1 \
  -H "Username: admin" \
  -H "Password: admin123"
```

### 6. ⚖️ Match Order (Admin Only)
```bash
curl -X POST http://localhost:8080/api/orders/match \
  -H "Content-Type: application/json" \
  -H "Username: admin" \
  -H "Password: admin123" \
  -d '{"orderId": 1}'
```

---

## 🏗️ Architecture

### 🎯 System Architecture
```
┌─────────────────┐    ┌─────────────────┐    ┌─────────────────┐
│   React Web UI │───▶│  Spring Boot    │───▶│   H2 Database   │
│                 │    │   REST API      │    │                 │
│  - Dashboard    │    │  - Controllers  │    │  - Customers    │
│  - Order Mgmt   │    │  - Services     │    │  - Orders       │
│  - Asset View   │    │  - Repositories │    │  - Assets       │
│  - Admin Panel  │    │  - Security     │    │                 │
└─────────────────┘    └─────────────────┘    └─────────────────┘
```

### 🏛️ Backend Architecture
The application follows a layered architecture:

- **🌐 Controller Layer**: REST endpoints and request/response handling
- **⚙️ Service Layer**: Business logic and transaction management
- **🗄️ Repository Layer**: Data access using Spring Data JPA
- **📊 Entity Layer**: JPA entities representing database tables
- **📦 DTO Layer**: Data transfer objects for API communication
- **🔧 Configuration Layer**: Security and application configuration

---

## 📝 Database Schema

### 📊 Entities

#### 👤 Customer
```sql
- id (Primary Key)
- username (Unique)
- password (BCrypt encoded)
- isAdmin (Boolean)
```

#### 💼 Asset
```sql
- id (Primary Key)
- customerId
- assetName
- size (Total amount)
- usableSize (Available for trading)
- Unique constraint: (customerId, assetName)
```

#### 📋 Order
```sql
- id (Primary Key)
- customerId
- assetName
- orderSide (BUY/SELL)
- size
- price
- status (PENDING/MATCHED/CANCELED)
- createDate
```

---

## 📊 Code Coverage

This project maintains high code quality with comprehensive test coverage:

### 🎯 Coverage Metrics
- **Line Coverage**: 80%+ target
- **Branch Coverage**: Tracked and reported
- **Method Coverage**: Complete service layer coverage

### 📈 Coverage Reports
- **Local Reports**: Available in `target/site/jacoco/` after running tests

### 🔧 Running Coverage Locally
```bash
# Generate coverage report
mvn clean test jacoco:report

# View report in browser
open target/site/jacoco/index.html
```

### 🚀 Automated Coverage
- **CI/CD Pipeline**: Automatically generates coverage on every push
- **PR Comments**: Coverage diff displayed on pull requests
- **Quality Gates**: Minimum coverage thresholds enforced
- **GitHub Pages**: Coverage reports published automatically

---

## 🧪 Testing

The application includes comprehensive testing:

### Backend Tests 🖥️
- **AssetServiceTest**: Asset management operations
- **CustomerServiceTest**: Authentication and customer operations  
- **OrderServiceTest**: Order creation, cancellation, and matching
- **ApplicationTests**: Integration tests

Run backend tests:
```bash
mvn test
```

### Frontend Tests 🎨
- **Component Tests**: Individual component functionality
- **Integration Tests**: Component interaction testing
- **User Journey Tests**: End-to-end user workflows

Run frontend tests:
```bash
cd web
npm test
```

---

## 🚨 Error Handling

The application provides comprehensive error responses:

| Status Code | Description | Example |
|-------------|-------------|---------|
| 🔓 401 | Unauthorized | Invalid credentials |
| 🚫 403 | Forbidden | Insufficient permissions |
| ❌ 400 | Bad Request | Invalid input data |
| 🔍 404 | Not Found | Resource not found |
| ⚠️ 409 | Conflict | Business rule violations |

---

## 🔒 Security Features

- **🔐 Authentication**: Secure login with BCrypt password hashing
- **🛡️ Authorization**: Role-based access control (Admin/Customer)
- **🔍 Input Validation**: Comprehensive input validation and sanitization
- **🏦 Transaction Safety**: Database transactions for data consistency
- **🚫 CORS Protection**: Cross-origin request security
- **📝 Audit Trail**: Complete logging of all transactions

---

## 🚀 Production Considerations

For production deployment, consider:

1. **🗄️ Database**: Replace H2 with PostgreSQL/MySQL
2. **🔐 Security**: Implement JWT tokens and OAuth2
3. **⚙️ Configuration**: Environment-based configuration
4. **📊 Monitoring**: Add logging, metrics, and health checks
5. **📚 Documentation**: Swagger/OpenAPI documentation
6. **🛡️ Validation**: Enhanced validation and error handling
7. **⏱️ Rate Limiting**: API rate limiting implementation
8. **🔄 Migration**: Database migration scripts (Flyway/Liquibase)
9. **🏗️ CI/CD**: Automated deployment pipeline
10. **📈 Scaling**: Load balancing and horizontal scaling

---

## 📸 Screenshots

<div align="center">

### 🏠 Dashboard
![Screenshot 2025-08-23 at 6.43.00 PM.png](screen-shots/web/Screenshot%202025-08-23%20at%206.43.00%E2%80%AFPM.png)

### 📋 Order Management
![Screenshot 2025-08-23 at 6.43.15 PM.png](screen-shots/web/Screenshot%202025-08-23%20at%206.43.15%E2%80%AFPM.png)
![Screenshot 2025-08-23 at 6.43.53 PM.png](screen-shots/web/Screenshot%202025-08-23%20at%206.43.53%E2%80%AFPM.png)
![Screenshot 2025-08-23 at 6.43.29 PM.png](screen-shots/web/Screenshot%202025-08-23%20at%206.43.29%E2%80%AFPM.png)

### 👑 Admin Panel
![Screenshot 2025-08-23 at 6.44.15 PM.png](screen-shots/web/Screenshot%202025-08-23%20at%206.44.15%E2%80%AFPM.png)
![Screenshot 2025-08-23 at 6.44.26 PM.png](screen-shots/web/Screenshot%202025-08-23%20at%206.44.26%E2%80%AFPM.png)
![Screenshot 2025-08-23 at 6.44.47 PM.png](screen-shots/web/Screenshot%202025-08-23%20at%206.44.47%E2%80%AFPM.png)

</div>


## 📄 License

This project is developed for educational and demonstration purposes.

---

<div align="center">

**Made with ❤️ by [Kubra Felek](https://github.com/kubrafelek)**

</div>
