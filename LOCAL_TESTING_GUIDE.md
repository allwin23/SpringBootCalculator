# Local Testing Guide

Follow these steps to run the application and test the API endpoints locally.

> [!NOTE]
> On Windows, use `curl.exe` instead of `curl` in PowerShell to avoid interference with PowerShell's `Invoke-WebRequest` alias.

## Step 1: Run the Application
Run this command from the root directory:
```powershell
mvn spring-boot:run
```
Port: **9090**

---

## Step 2: Test API Endpoints

### 1. Create Order
Create an order with multiple products.

**Request:**
```powershell
curl.exe -X POST http://localhost:9090/api/v1/orders `
     -H "Content-Type: application/json" `
     -d '{ \"sellerId\": \"123\", \"customerId\": \"Cust-123\", \"items\": [ { \"productId\": \"456\", \"quantity\": 2 }, { \"productId\": \"458\", \"quantity\": 1 } ] }'
```

---

### 2. Get Order Shipping Estimate (New Feature)
Get logistics intelligence for the created order.

**Request:**
```powershell
# Replace ORD-XXXX with the orderId from the previous response
curl.exe "http://localhost:9090/api/v1/orders/ORD-XXXX/shipping?deliverySpeed=express"
```

---

### 3. Find Nearest Warehouse
```powershell
curl.exe "http://localhost:9090/api/v1/warehouse/nearest?sellerId=123&productId=456"
```

---

### 4. Calculate Shipping Charge (Legacy)
```powershell
curl.exe "http://localhost:9090/api/v1/shipping-charge?warehouseId=789&customerId=Cust-123&deliverySpeed=express&productId=456"
```

---

## Step 3: Test Error Handling

### 1. Invalid Order ID
```powershell
curl.exe "http://localhost:9090/api/v1/orders/INVALID-ID/shipping"
```

### 2. Product mismatch
```powershell
curl.exe -X POST http://localhost:9090/api/v1/orders `
     -H "Content-Type: application/json" `
     -d '{ \"sellerId\": \"125\", \"customerId\": \"Cust-123\", \"items\": [ { \"productId\": \"456\", \"quantity\": 1 } ] }'
```

---

## Access H2 Console
- **URL:** [http://localhost:9090/h2-console](http://localhost:9090/h2-console)
- **JDBC URL:** `jdbc:h2:mem:shippingdb`
- **User:** `sa`
