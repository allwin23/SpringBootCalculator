# Jumbotail Shipping Charge Calculator

A Spring Boot application for calculating shipping charges in a B2B e-commerce marketplace that helps Kirana stores discover and order products.

## Features

- **Google Maps Distance Engine**: Real-time matrix distance calculations with seamless Haversine fallback.
- **Logistics Simulator & Shipping Architect**: Calculate multi-tier shipping charges based on distance, weight, transport mode, and delivery speed.
- **Nearest Warehouse Finder**: Core spatial routing system routing to closest viable inventory.
- **Flexible Weight Conversions**: Intelligent dynamic unit mapping to localized units (KG, LBS, GRAMS).
- **Enterprise Rate Limiting**: Bulletproof endpoint protection powered by Bucket4j throttling algorithms.
- **Telemetry Monitoring**: Full-scale metrics aggregation tracking active shipments, averages, and volume.
- **Strict Exception Handling**: Standardized, secure `@ControllerAdvice` API error responses filtering raw stack traces into safe, front-end consumable HTTP codes (e.g. `InvalidRequestException` for 400).
- **Fast Performance Caching**: In-memory response caching via Caffeine preventing redundant API calls.
- **Production Ready**: Fully Dockerized, multi-cloud compatible (Render/Railway), and database-agnostic (H2 vs PostgreSQL).
- **Interactive Documentation**: Swagger UI automatically deployed to intercept the base URL (No need to hunt for docs!).
- **Comprehensive Testing**: Validated via exhaustive Unit Tests and System Integration validations.

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

## 📚 Architectural Design & API Docs

For a deep dive into the System Design, Strategy Patterns, Database hot-swapping, and the complete internal structure of every API endpoint, please explore the [ARCHITECTURE.md](ARCHITECTURE.md) record.

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

## 🌐 API Definitions & Usage

The Jumbotail Shipping Engine exposes powerful REST APIs for end-to-end commerce handling.

### 1. Order Management & Checkout (`OrderController`)

**A. Create New Order (Multi-Item Support)**
- **Endpoint**: `POST /api/v1/orders`
- **Use Case**: B2B Kirana store ordering products from a specific supplier matrix.
- **Request Body (Database Compatible)**:
```json
{
  "sellerId": "123",
  "customerId": "Cust-123",
  "items": [
    {
      "productId": "456",
      "quantity": 50
    },
    {
      "productId": "459",
      "quantity": 10
    }
  ]
}
```
- **Response**: Returns full `OrderResponse` containing unique `id`, mapped `seller`, `customer`, `status`, and total `items`.

**B. Get Order Details**
- **Endpoint**: `GET /api/v1/orders/{orderId}`
- **Use Case**: Viewing an active order receipt.

**C. Calculate Cart Shipping Architecture**
- **Endpoint**: `GET /api/v1/orders/{orderId}/shipping`
- **Query Params**: `deliverySpeed` (STANDARD or EXPRESS)
- **Use Case**: Taking a full customer cart, locating nearest dispatch warehouse with necessary inventory, summing the total kg cart weight, analyzing geographic distance against the destination, choosing the cheapest transport vector, and finalizing the bill.
- **Response**:
```json
{
  "orderId": 11,
  "deliverySpeed": "STANDARD",
  "nearestWarehouse": {
    "warehouseId": 789,
    "distanceKm": 84.76
  },
  "cartWeightKg": 2.50,
  "transportMode": "MINI_VAN",
  "shippingCost": 635.70,
  "estimatedDeliveryDays": 3
}
```

### 2. Logistics Distance Engine (`DistanceController`)

**Get Route Telemetry**
- **Endpoint**: `GET /api/v1/logistics/distance`
- **Use Case**: Used by backend routers to identify live road distance dynamically failing over if external nodes crash.
- **Query Parameters**:
  - `sourceLat`, `sourceLng` (required): Starting coordinate
  - `destLat`, `destLng` (required): End coordinate
  - `mode` (optional; `GOOGLE` or `HAVERSINE`)
- **Example Request** (BLR Warehouse to 'Cust-123' store):
```bash
GET /api/v1/logistics/distance?sourceLat=12.99999&sourceLng=37.923273&destLat=11.232&destLng=23.445495&mode=GOOGLE
```
- **Response**:
```json
{
  "distanceKm": 1642.3,
  "calculationMode": "GOOGLE",
  "durationMinutes": 1845
}
```

### 3. Supply Chain Simulator (`LogisticsController`)

**Test Theoretical Routes**
- **Endpoint**: `POST /api/v1/logistics/simulate`
- **Use Case**: Business logic simulation for profitability validation. Feeds arbitrary metrics and calculates hypothetical margins.
- **Request Body**:
```json
{
  "distanceKm": 500.5,
  "weightKg": 100.0,
  "transportMode": "AEROPLANE"
}
```
- **Response**: Outputs theoretical `totalCost`, `profitMargin`, etc.

### 4. Warehouse Inventory Lookups (`WarehouseController`)

**Get Nearest Dispatch Center**
- **Endpoint**: `GET /api/v1/warehouse/nearest`
- **Use Case**: Geographically search the database for the nearest physical building that stocks the necessary item.

### 5. Product Catalog Conversions (`ProductController`)

**Convert Product Weights**
- **Endpoint**: `GET /api/v1/products/{productId}/weight`
- **Query Params**: `targetUnit` (KG, LBS, GRAMS)
- **Use Case**: Frontend localization. Converts product mass metrics efficiently.
- **Response**:
```json
{
  "productId": 789,
  "productName": "Maggie 500g",
  "originalWeightKg": 0.5,
  "convertedWeight": 1.1023,
  "targetUnit": "LBS"
}
```

### 6. Operational Telemetry (`MetricsController`)

**Fetch Analytics Data**
- **Endpoint**: `GET /api/v1/metrics/shipping`
- **Use Case**: Get global operational statistics.
- **Response**:
```json
{
  "totalOrdersProcessed": 45,
  "averageShippingCost": 234.50,
  "totalRevenue": 10552.50,
  "mostCommonTransportMode": "TRUCK"
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

The application will start on `http://localhost:9090`

### Access H2 Console

Once the application is running, you can access the H2 database console at:
```
http://localhost:9090/h2-console
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

## Exception Handling Architecture

The application implements a robust, fault-tolerant `GlobalExceptionHandler` mapping core errors into safe JSON structures.

- `ResourceNotFoundException`: Maps cleanly to `404 Not Found` when requesting invalid Order IDs or inactive Warehouses.
- `InvalidRequestException`: Traps bad payloads into `400 Bad Request` avoiding 500 crashes.
- `MissingServletRequestParameterException`: Gracefully intercepts missing required parameters like `customerId`.
- `Validation Exceptions`: All DTO `@Valid` failures safely output their customized constraint string directly to the client.

## Project Expansion Milestones (Successfully Completed)

Every initial target of this project's roadmap has successfully been engineered:

- ✅ **External Mapping Integration**: Google Maps Distance Matrix implementation.
- ✅ **Swagger/OpenAPI UI**: Fully auto-generated interactive documentation at `/`.
- ✅ **Cloud Platform Containerization**: 100% platform-agnostic `Dockerfile`.
- ✅ **Multiple Products in Single Order**: Full Order itemization loop matrix applied.
- ✅ **Variable Weight Units**: Dynamic localization built-in for multi-national weight mapping.
- ✅ **Rate Limiting & Threat Protection**: Endpoints shielded by Bucket4j algorithms.
- ✅ **Testing Matrix Full Coverage**: Isolated component unit tests and system validations.
- ✅ **Performance Telemetry**: Full statistical `/metrics` controllers for Grafana consumption.

## Author

Built as part of Jumbotail assignment

## License

This project is for educational/demonstration purposes.
