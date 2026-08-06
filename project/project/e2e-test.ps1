
$ErrorActionPreference = "Stop"

$gatewayBaseUrl = "http://localhost:8084"
$authBaseUrl = "$gatewayBaseUrl/api/auth"
$walletBaseUrl = "$gatewayBaseUrl/api/wallet"
$shopBaseUrl = "$gatewayBaseUrl/api/shop"
$inventoryBaseUrl = "$gatewayBaseUrl/api/inventory"

# USE GATEWAY URLS FOR TESTING
$authUrl = $authBaseUrl
$walletUrl = $walletBaseUrl
$shopUrl = $shopBaseUrl
$inventoryUrl = $inventoryBaseUrl

Write-Host "--- E2E Test Suite (Direct) ---" -ForegroundColor Cyan

# 1. Register User (Auth)
Write-Host "1. Registering User..." -ForegroundColor Yellow
$email = "testuser_" + (Get-Date -Format "yyyyMMddHHmmss") + "@example.com"
$registerBody = @{
    email = $email
    firstName = "Test"
    lastName = "E2E"
    password = "password123"
    active = $true
} | ConvertTo-Json

# DEBUG: Try direct call first
Write-Host "DEBUG: Trying direct call to Auth Service..."
try {
    $directAuthUrl = "http://localhost:8080/api/v1/auth/register"
    $registerResponse = Invoke-RestMethod -Uri $directAuthUrl -Method Post -Body $registerBody -ContentType "application/json"
    Write-Host "DEBUG: Direct call successful!" -ForegroundColor Green
    $userId = $registerResponse.id
} catch {
    Write-Host "DEBUG: Direct call failed: $_" -ForegroundColor Red
}

if (-not $userId) {
    Write-Host "1. Registering User (Gateway)..." -ForegroundColor Yellow
    try {
        $registerResponse = Invoke-RestMethod -Uri "$authUrl/register" -Method Post -Body $registerBody -ContentType "application/json"
        $userId = $registerResponse.id
    } catch {
        Write-Host "Register failed: $_" -ForegroundColor Red
        if ($_.Exception.Response) {
            $stream = [System.IO.StreamReader]::new($_.Exception.Response.GetResponseStream())
            Write-Host "Response Body: $($stream.ReadToEnd())" -ForegroundColor Red
        }
        return
    }
}
Write-Host "User registered with ID: $userId and email: $email" -ForegroundColor Green

# 2. Check Wallet Balance
Write-Host "2. Checking Wallet Balance..." -ForegroundColor Yellow
try {
    # Direct endpoint: /api/v1/wallets/user/{userId}
    $walletResponse = Invoke-RestMethod -Uri "$walletUrl/user/$userId" -Method Get
    Write-Host "Wallet found: $($walletResponse.id)" -ForegroundColor Green
} catch {
    Write-Host "Wallet check failed: $_" -ForegroundColor Red
    if ($_.Exception.Response) {
        $stream = [System.IO.StreamReader]::new($_.Exception.Response.GetResponseStream())
        Write-Host "Response Body: $($stream.ReadToEnd())" -ForegroundColor Red
    }
    return
}

# 3. Create Inventory Product
Write-Host "3. Creating Inventory Product..." -ForegroundColor Yellow
$productBody = @{
    name = "Test Product"
    price = 10.00
    quantity = 5
} | ConvertTo-Json
try {
    $productResponse = Invoke-RestMethod -Uri "$inventoryUrl/products" -Method Post -Body $productBody -ContentType "application/json"
    $inventoryProductId = $productResponse.id
    Write-Host "Inventory product created with ID: $inventoryProductId" -ForegroundColor Green
} catch {
    Write-Host "Inventory product creation failed: $_" -ForegroundColor Red
    return
}

# 4. Create Shop Product
Write-Host "4. Creating Shop Product..." -ForegroundColor Yellow
$shopProductBody = @{
    inventoryProductId = $inventoryProductId
    title = "Test Product Shop"
    price = 12.00
} | ConvertTo-Json
try {
    $shopProductResponse = Invoke-RestMethod -Uri "$shopUrl/products" -Method Post -Body $shopProductBody -ContentType "application/json"
    $shopProductId = $shopProductResponse.id
    Write-Host "Shop product created with ID: $shopProductId" -ForegroundColor Green
} catch {
    Write-Host "Shop product creation failed: $_" -ForegroundColor Red
    return
}

# 5. Add to Cart
Write-Host "5. Adding to cart..." -ForegroundColor Yellow
$cartBody = @{
    userId = $userId
    shopProductId = $shopProductId
    quantity = 1
} | ConvertTo-Json
try {
    Invoke-RestMethod -Uri "$shopUrl/cart/add" -Method Post -Body $cartBody -ContentType "application/json"
    Write-Host "Added to cart" -ForegroundColor Green
} catch {
    Write-Host "Add to cart failed: $_" -ForegroundColor Red
    if ($_.Exception.Response) {
        $stream = [System.IO.StreamReader]::new($_.Exception.Response.GetResponseStream())
        Write-Host "Response Body: $($stream.ReadToEnd())" -ForegroundColor Red
    }
    return
}

# 6. Checkout
Write-Host "6. Checking out..." -ForegroundColor Yellow
$checkoutBody = @{
    userId = $userId
    items = @(
        @{
            productId = $shopProductId
            quantity = 1
        }
    )
} | ConvertTo-Json
try {
    $checkoutResponse = Invoke-RestMethod -Uri "$shopUrl/checkout" -Method Post -Body $checkoutBody -ContentType "application/json"
    Write-Host "Checkout successful! Order ID: $($checkoutResponse.orderId)" -ForegroundColor Green
} catch {
    Write-Host "Checkout failed: $_" -ForegroundColor Red
    if ($_.Exception.Response) {
        $stream = [System.IO.StreamReader]::new($_.Exception.Response.GetResponseStream())
        Write-Host "Response Body: $($stream.ReadToEnd())" -ForegroundColor Red
    }
    return
}
