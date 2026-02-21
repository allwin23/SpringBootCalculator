# End-to-End API Test Script for Jumbotail Shipping

function Run-Test {
    param (
        [string]$Name,
        [string]$Method,
        [string]$Url,
        [string]$Body = $null,
        [hashtable]$Headers = @{"Content-Type" = "application/json"}
    )

    Write-Host "`n--- Testing: $Name ---" -ForegroundColor Cyan
    Write-Host "$Method $Url" -ForegroundColor Gray

    $params = @{
        Method = $Method
        Uri = $Url
        Headers = $Headers
    }

    if ($Body) {
        $params.Body = $Body
    }

    try {
        $response = Invoke-RestMethod @params
        $response | ConvertTo-Json -Depth 10 | Write-Host
        Write-Host "Status: SUCCESS" -ForegroundColor Green
        return $response
    } catch {
        Write-Host "Status: FAILED" -ForegroundColor Red
        Write-Host $_.Exception.Message
        return $null
    }
}

$baseUrl = "http://localhost:9090/api/v1"
$actuatorUrl = "http://localhost:9090/actuator"

# 1. Master Data Lookups
Write-Host "`n=== STEP 1: MASTER DATA LOOKUPS ===" -ForegroundColor Yellow -BackgroundColor Black
Run-Test "Get Nearest Warehouse" "GET" "$baseUrl/warehouse/nearest?sellerId=123&productId=456"
Run-Test "Get Product Weight" "GET" "$baseUrl/products/456/weight?unit=kg"

# 2. Order Management
Write-Host "`n=== STEP 2: ORDER MANAGEMENT ===" -ForegroundColor Yellow -BackgroundColor Black
$orderBody = @{
    sellerId = "123"
    customerId = "Cust-123"
    items = @(
        @{ productId = "456"; quantity = 5 }
    )
} | ConvertTo-Json
$createdOrder = Run-Test "Create New Order" "POST" "$baseUrl/orders" -Body $orderBody

if ($createdOrder) {
    # The API returns OrderResponse which usually has the external orderId
    $extOrderId = $createdOrder.orderId
    Run-Test "Get Order Details" "GET" "$baseUrl/orders/$extOrderId"
    Run-Test "Get Order Shipping Estimate (Express)" "GET" "$baseUrl/orders/$extOrderId/shipping?deliverySpeed=express"
}

# 3. Logistics Optimization Engine
Write-Host "`n=== STEP 3: LOGISTICS OPTIMIZATION ENGINE ===" -ForegroundColor Yellow -BackgroundColor Black
# Note: This expects an INTERNAL ID (Long). ORD-001 is usually ID 1.
$recommendationBody = @{
    orderId = 1
    priority = "BALANCED"
} | ConvertTo-Json
Run-Test "Get Optimal Logistics Recommendation" "POST" "$baseUrl/logistics/recommendation" -Body $recommendationBody

# 4. Shipping Charge Calculations
Write-Host "`n=== STEP 4: SHIPPING CHARGE CALCULATIONS ===" -ForegroundColor Yellow -BackgroundColor Black
# Warehouse 789 is BLR_Warehouse
Run-Test "Direct Shipping Charge Calculation" "GET" "$baseUrl/shipping-charge?warehouseId=789&customerId=Cust-123&deliverySpeed=standard&productId=456"

$calculateBody = @{
    sellerId = "123"
    customerId = "Cust-123"
    deliverySpeed = "express"
} | ConvertTo-Json
Run-Test "Combined Calculation (Post)" "POST" "$baseUrl/shipping-charge/calculate" -Body $calculateBody

# 5. System Health & Metrics
Write-Host "`n=== STEP 5: SYSTEM HEALTH & METRICS ===" -ForegroundColor Yellow -BackgroundColor Black
Run-Test "Get Shipping Metrics" "GET" "$baseUrl/metrics/shipping"
Run-Test "Get Actuator Health" "GET" "$actuatorUrl/health"

Write-Host "`n=== ALL TESTS COMPLETED ===" -ForegroundColor Yellow -BackgroundColor Black
