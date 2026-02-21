# Jumbotail Shipping Engine - Architecture & API Design

## 🏗️ System Architecture Overview

The **Jumbotail Shipping Engine** is a production-grade, highly resilient Spring Boot 3.2 microservice designed to handle real-world logistics, shipping charge estimations, and order routing for a B2B E-Commerce platform. 

It uses a modern Java 17+ technology stack and is fully optimized for multi-cloud deployments (like Render.com and Railway.app).

### Core Architectural Patterns
1. **Strategy Design Pattern:** Used heavily in the **Logistics Distance Engine**. The application dynamically switches between a `GoogleMapsDistanceStrategy` (hitting live Google APIs) and a `HaversineDistanceStrategy` (a complex trigonometric geographic fallback) if external network conditions drop or API quotas are exceeded. 
2. **Resilience & Fault Tolerance:** The outbound REST clients (`RestTemplate`) are explicitly wrapped in 5-second `Connect` and `Read` timeouts. The system is designed to never freeze worker threads on external API drops.
3. **In-Memory Caching:** High-throughput endpoints (like `distanceCache`, `nearestWarehouse`, `shippingEstimate`) are guarded by **Caffeine** cache. This drastically reduces external API billing costs and local database lookups for redundant coordinate matrices.
4. **Environment-Agnostic Database Layer:** Uses an **H2 in-memory database** automatically during local development (`application.yml`) and scales up instantly to an enterprise **PostgreSQL** database in production (`application-prod.yml`) using dynamic Spring Profiles.

---

## 🔌 API Definitions & Use Cases

The engine is grouped into several modular domains.

### 1. Order Management (`OrderController`)
Manages the lifecycle of customer orders and generating immediate shipping estimations natively within the cart.

*   `POST /api/v1/orders`
    *   **Use Case:** A B2B customer checks out a cart of products. This API persists the `Order`, maps the `OrderItems` to the `Seller`, calculates quantities, and returns an `orderId`.
*   `GET /api/v1/orders/{orderId}`
    *   **Use Case:** Displays the order receipt, standard status, and the complete itemized breakdown for the customer dashboard.
*   `GET /api/v1/orders/{orderId}/shipping`
    *   **Use Case:** The most critical endpoint. Given an `orderId` and an optional `deliverySpeed` (e.g., STANDARD, EXPRESS, NEXT_DAY), it orchestrates a massive background calculation:
        1. Identifies all products in the order.
        2. Locates the nearest physical warehouse stocking those products.
        3. Invokes the Distance Engine to find the distance between the Warehouse and the Customer.
        4. Calculates total weight and converts transport modes (Truck, Van, Air).
        5. Returns the **Exact Shipping Cost ($)** and **Estimated Time of Arrival (ETA)**.

### 2. Logistics Optimization Engine (`LogisticsController` / `DistanceController`)
The calculation powerhouse providing routing logic to the broader platform.

*   `GET /api/v1/logistics/distance`
    *   **Use Case:** Standalone calculator used by drivers or analytics. It takes `sourceLat`, `sourceLng`, `destLat`, `destLng` and a calculation `mode` (`GOOGLE` or `HAVERSINE`). It queries the live Google Distance Matrix API. If Google fails, it seamlessly returns the Haversine trigonometric distance.
*   `POST /api/v1/logistics/simulate`
    *   **Use Case:** Fleet management scenario testing. You can send a payload of arbitrary payloads (weights, distances, transport tiers) and it will project potential profit margins and shipping costs for theoretical delivery runs.

### 3. Warehouse Inventory (`WarehouseController`)
Handles the geographic spatial queries of physical supply chain nodes.

*   `GET /api/v1/warehouse/nearest`
    *   **Use Case:** Takes a `sellerId` and a `productId`. It iterates through all available warehouses assigned to that seller, checks if they currently hold inventory of the given product, runs distance checks against the requesting customer, and returns the mathematically closest dispatch node. 

### 4. Product Catalog (`ProductController`)
Manages physical attributes of sellable items.

*   `GET /api/v1/products/{productId}/weight`
    *   **Use Case:** Takes an ID and a target Unit of Measurement (`KG`, `GRAMS`, `LBS`). Converts the internal database weight metric to whatever the frontend viewing region demands before rendering the product UI.

### 5. Telemetry & Analytics (`MetricsController`)
Internal system monitoring for operational dashboards.

*   `GET /api/v1/metrics/shipping`
    *   **Use Case:** Returns massive aggregate data. The operations team can see "Total Orders Shipped", "Average Shipping Cost", "Most Used Transport Mode", and "Average Distance Traveled" in real-time.

---

## 🔒 Production Security Readiness
*   **Zero Exposed Secrets:** The `GOOGLE_MAPS_API_KEY` is loaded securely via Shell environment variables (`.env`). It is strictly ignored from Source Control via `.gitignore`.
*   **Dynamic Port Binding:** The included `Dockerfile` doesn't enforce standard local ports. It dynamically listens to Render's internal reverse-proxy port assignments upon deployment.
*   **Swagger Redirects:** Upon deployment, the root URL (`/`) bounces automatically via an `HTTP 302 Redirect` directly to the `/swagger-ui/index.html` interface so external developers have out-of-the-box documentation without knowing the specific paths.
