# Jumbotail Shipping Charge Calculator

A Spring Boot application for calculating shipping charges in a B2B e-commerce marketplace that helps Kirana stores discover and order products.

## Features

- **Nearest Warehouse Finder**: Find the nearest warehouse for a seller based on product location
- **Shipping Charge Calculator**: Calculate shipping charges based on distance, weight, transport mode, and delivery speed
- **Combined API**: Single endpoint to find nearest warehouse and calculate shipping charge
- **Caching**: Response caching for improved performance
- **Exception Handling**: Comprehensive error handling with meaningful error messages
- **Unit Tests**: Complete test coverage for services and utilities

## Technology Stack

- **Java 17**
- **Spring Boot 3.2.0**
- **Spring Data JPA**
- **H2 Database** (In-memory for development)
- **Maven**
- **Lombok**
- **Caffeine Cache**

## Project Structure

```
jumbotail-shipping/
├── src/
│   ├── main/
│   │   ├── java/com/jumbotail/shipping/
│   │   │   ├── config/          # Configuration classes
│   │   │   ├── controller/       # REST controllers
│   │   │   ├── dto/              # Data Transfer Objects
│   │   │   ├── exception/        # Custom exceptions and handlers
│   │   │   ├── model/            # Entity models
│   │   │   ├── repository/       # JPA repositories
│   │   │   ├── service/          # Business logic services
│   │   │   ├── strategy/         # Strategy pattern implementations
│   │   │   └── util/             # Utility classes
│   │   └── resources/
│   │       └── application.yml   # Application configuration
│   └── test/                     # Unit tests
└── pom.xml                       # Maven dependencies
```

## Business Logic

### Transport Modes

The application determines transport mode based on distance:

- **Mini Van**: 0-100 km at **3 Rs per km per kg**
- **Truck**: 100-500 km at **2 Rs per km per kg**
- **Aeroplane**: 500+ km at **1 Rs per km per kg**

### Delivery Speeds

- **Standard**: Rs 10 standard courier charge + calculated shipping charge
- **Express**: Rs 10 standard courier charge + Rs 1.2 per kg extra + calculated shipping charge

### Distance Calculation

Uses the Haversine formula to calculate great-circle distance between two geographic coordinates.

## API Endpoints

### 1. Get Nearest Warehouse

**Endpoint**: `GET /api/v1/warehouse/nearest`

**Query Parameters**:
- `sellerId` (required): Seller ID
- `productId` (required): Product ID

**Example Request**:
```bash
GET /api/v1/warehouse/nearest?sellerId=123&productId=456
```

**Example Response**:
```json
{
  "warehouseId": 789,
  "warehouseLocation": {
    "lat": 12.99999,
    "lng": 37.923273
  }
}
```

### 2. Get Shipping Charge

**Endpoint**: `GET /api/v1/shipping-charge`

**Query Parameters**:
- `warehouseId` (required): Warehouse ID
- `customerId` (required): Customer ID
- `deliverySpeed` (required): "standard" or "express"
- `productId` (optional): Product ID for accurate weight calculation

**Example Request**:
```bash
GET /api/v1/shipping-charge?warehouseId=789&customerId=456&deliverySpeed=standard&productId=456
```

**Example Response**:
```json
{
  "shippingCharge": 150.00
}
```

### 3. Calculate Shipping Charge for Seller and Customer

**Endpoint**: `POST /api/v1/shipping-charge/calculate`

**Request Body**:
```json
{
  "sellerId": "123",
  "customerId": "456",
  "deliverySpeed": "express"
}
```

**Example Response**:
```json
{
  "shippingCharge": 180.00,
  "nearestWarehouse": {
    "warehouseId": 789,
    "warehouseLocation": {
      "lat": 12.99999,
      "lng": 37.923273
    }
  }
}
```

## Running the Application

### Prerequisites

- Java 17 or higher
- Maven 3.6+

### Build and Run

```bash
# Navigate to project directory
cd jumbotail-shipping

# Build the project
mvn clean install

# Run the application
mvn spring-boot:run
```

The application will start on `http://localhost:8080`

### Access H2 Console

Once the application is running, you can access the H2 database console at:
```
http://localhost:8080/h2-console
```

**JDBC URL**: `jdbc:h2:mem:shippingdb`
**Username**: `sa`
**Password**: (leave empty)

## Running Tests

```bash
# Run all tests
mvn test

# Run tests with coverage
mvn test jacoco:report
```

## Sample Data

The application automatically initializes sample data on startup:

- **Customers**: Cust-123, Cust-124
- **Sellers**: Nestle Seller (123), Rice Seller (124), Sugar Seller (125)
- **Products**: Maggie 500g, Rice Bag 10Kg, Sugar Bag 25kg
- **Warehouses**: BLR_Warehouse, MUMB_Warehouse

## Design Patterns Used

1. **Strategy Pattern**: Used for transport mode selection and delivery speed calculation
2. **Repository Pattern**: Data access abstraction
3. **DTO Pattern**: Data transfer objects for API requests/responses
4. **Service Layer Pattern**: Business logic separation

## Caching

The application uses Caffeine cache for:
- Nearest warehouse lookups
- Shipping charge calculations

Cache configuration:
- Maximum size: 500 entries
- Expiration: 10 minutes after write

## Exception Handling

The application includes comprehensive exception handling:

- `ResourceNotFoundException`: When requested resource is not found
- `InvalidRequestException`: When request parameters are invalid
- `IllegalArgumentException`: For invalid arguments
- Global exception handler returns standardized error responses

## Future Enhancements

- [ ] Support for multiple products in a single order
- [ ] Integration with external mapping services for accurate distance calculation
- [ ] Support for different weight units
- [ ] Rate limiting and API versioning
- [ ] Swagger/OpenAPI documentation
- [ ] Docker containerization
- [ ] Integration tests
- [ ] Performance monitoring and metrics

## Author

Built as part of Jumbotail assignment

## License

This project is for educational/demonstration purposes.
