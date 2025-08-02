# Order API

A Spring Boot microservice for managing orders in an e-commerce platform.

**Author**: Esraa Refaat

## Contributors

- **Esraa Refaat** - Project lead and main development
- **Basma & Shahd** - Notification configuration and messaging
- **Taghreed** - Bank integration and payment processing

## Overview

The Order API is part of a microservices architecture that handles order creation, management, and payment processing. It integrates with catalog, coupon, and user services to provide a comprehensive order management solution.

## Features

- Order creation and management
- Payment processing
- Coupon validation and application
- Real-time stock management via RabbitMQ
- Microservices integration with Feign clients
- PostgreSQL database with Liquibase migrations
- Comprehensive REST API

## Tech Stack

- **Framework**: Spring Boot 3.5.3
- **Language**: Java 21
- **Database**: PostgreSQL
- **Message Queue**: RabbitMQ (CloudAMQP)
- **Migration**: Liquibase
- **Build Tool**: Maven
- **Service Communication**: OpenFeign
- **Data Processing**: Lombok

## Prerequisites

- Java 21
- Maven 3.6+
- PostgreSQL database
- RabbitMQ instance

## Getting Started

### 1. Clone the repository
```bash
git clone <repository-url>
cd order-api
```

### 2. Configure the application
Update `src/main/resources/application.properties` with your configuration:

```properties
# Database
spring.datasource.url=jdbc:postgresql://your-host:port/database
spring.datasource.username=your-username
spring.datasource.password=your-password

# RabbitMQ
spring.rabbitmq.host=your-rabbitmq-host
spring.rabbitmq.username=your-username
spring.rabbitmq.password=your-password

# Microservices URLs
coupon.service.url=http://localhost:8080
catalog.service.url=http://localhost:8081
user.service.url=http://localhost:8083
```

### 3. Run the application
```bash
mvn spring-boot:run
```

The application will start on port 8082.

## API Endpoints

### Orders

| Method | Endpoint | Description |
|--------|----------|-------------|
| POST | `/orders` | Create a new order |
| GET | `/orders/{orderId}` | Get order by ID |
| GET | `/orders` | Get all orders |
| GET | `/orders/customer/{customerEmail}` | Get orders by customer |
| POST | `/orders/{orderId}/payment` | Process payment for order |

### Example Request

```json
POST /orders
Authorization: Bearer <token>

{
  "customerEmail": "customer@example.com",
  "items": [
    {
      "productId": 1,
      "quantity": 2
    }
  ],
  "couponCode": "DISCOUNT10"
}
```

## Database Schema

The application uses PostgreSQL with two main tables:
- `orders`: Stores order information
- `order_items`: Stores individual products in each order

See [DATABASE_SCHEMA.md](DATABASE_SCHEMA.md) for complete schema documentation.

## Microservices Integration

The Order API integrates with several services:

### Catalog Service
- Fetches product information and pricing
- Validates product availability

### Coupon Service  
- Validates coupon codes
- Applies discounts to orders

### User Service
- Validates customer information
- Retrieves customer details

### Stock Management
- Uses RabbitMQ for real-time stock updates
- Handles stock consumption and restoration

## Message Queue Configuration

### RabbitMQ Exchanges and Queues

- **Exchange**: `my-exchange`
- **Stock Routing Key**: `store.consume.stock`
- **Response Queue**: `order.stock.response.queue`
- **Response Routing Key**: `order.stock.response.key`

## Development

### Building the project
```bash
mvn clean compile
```

### Running tests
```bash
mvn test
```

### Creating a production build
```bash
mvn clean package
```

## Configuration

### Environment Variables

Key configuration properties can be overridden via environment variables:

- `SERVER_PORT`: Application port (default: 8082)
- `SPRING_DATASOURCE_URL`: Database URL
- `SPRING_DATASOURCE_USERNAME`: Database username
- `SPRING_DATASOURCE_PASSWORD`: Database password
- `COUPON_SERVICE_URL`: Coupon service URL
- `CATALOG_SERVICE_URL`: Catalog service URL
- `USER_SERVICE_URL`: User service URL

### Feign Client Configuration

- Connect timeout: 2000ms
- Read timeout: 5000ms
- Log level: Basic

## Security

- Authorization header required for order creation
- Cross-origin requests allowed from `http://localhost:4201`
- Input validation using Jakarta Bean Validation

## Monitoring and Logging

- SLF4J logging with Logback
- Request/response logging in controllers
- Feign client logging for external service calls

## Database Migrations

Liquibase manages database schema changes. Migration files are located in:
```
src/main/resources/db/changelog/
```

Key migrations:
- `01-initial-schema.xml`: Initial table creation
- `02-replace-coupon-relation.xml`: Coupon service integration
- `03-add-product-id-to-order-items.xml`: Catalog service integration
- `04-catalog-service-integration.xml`: Enhanced catalog integration
- `05-add-transaction-ids.xml`: Payment tracking
