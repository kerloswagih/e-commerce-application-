#!/usr/bin/env pwsh

param(
    [string]$BaseUrl = 'http://localhost:8084',
    [switch]$SkipBackend
)

$ErrorActionPreference = 'Stop'

Write-Host "Gateway test script - BaseUrl=$BaseUrl SkipBackend=$SkipBackend" -ForegroundColor Cyan

function Invoke-Http {
    param(
        [string]$Method = 'GET',
        [string]$Path,
        $Body = $null,
        [hashtable]$Headers = @{}
    )

    $url = ($BaseUrl.TrimEnd('/') + '/' + $Path.TrimStart('/'))

    try {
        if ($Body -ne $null) {
            $resp = Invoke-WebRequest -Uri $url -Method $Method -Body $Body -ContentType 'application/json' -Headers $Headers -ErrorAction Stop
        } else {
            $resp = Invoke-WebRequest -Uri $url -Method $Method -Headers $Headers -ErrorAction Stop
        }
        $status = [int]$resp.StatusCode
        $content = $resp.Content
        return @{ status = $status; content = $content }
    } catch {
        # Try to extract response from exception (non-2xx responses)
        $err = $_.Exception
        if ($err -and $err.Response) {
            $webResp = $err.Response
            try {
                $stream = [System.IO.StreamReader]::new($webResp.GetResponseStream())
                $body = $stream.ReadToEnd()
            } catch {
                $body = ''
            }
            $status = 0
            try { $status = [int]$webResp.StatusCode } catch {}
            return @{ status = $status; content = $body }
        } else {
            throw
        }
    }
}

function Test-Endpoint {
    param(
        [string]$Method = 'GET',
        [string]$Path,
        [int]$ExpectedStatus = 200,
        [string]$ExpectedContains = $null,
        $Body = $null
    )

    Write-Host "Testing $Method $Path (expect $ExpectedStatus)" -ForegroundColor Yellow
    $result = Invoke-Http -Method $Method -Path $Path -Body $Body
    if ($result.status -eq $ExpectedStatus) {
        Write-Host "Status OK: $($result.status)" -ForegroundColor Green
    } else {
        Write-Host "Status MISMATCH: got $($result.status) expected $ExpectedStatus" -ForegroundColor Red
        Write-Host "Response body:" -ForegroundColor Red
        Write-Host $result.content
        exit 2
    }

    if ($ExpectedContains) {
        if ($result.content -and $result.content -like "*${ExpectedContains}*") {
            Write-Host "Response contains expected text '$ExpectedContains'" -ForegroundColor Green
        } else {
            Write-Host "Response did NOT contain expected text '$ExpectedContains'" -ForegroundColor Red
            Write-Host "Response body:" -ForegroundColor Red
            Write-Host $result.content
            exit 3
        }
    }
}

# --- Tests ---

Test-Endpoint -Path '/api/gateway/routes' -ExpectedStatus 200 -ExpectedContains 'routeCount'

if (-not $SkipBackend) {
    # These tests require backend services to be up and registered with discovery
    Test-Endpoint -Path '/api/auth/health' -ExpectedStatus 200 -ExpectedContains 'status'
    Test-Endpoint -Path '/api/shop/items/123' -ExpectedStatus 200 -ExpectedContains '"id"'
    # POST example to wallet (adjust body to match your API)
    $walletBody = '{"amount":10}'
    Test-Endpoint -Method 'POST' -Path '/api/wallet/charge' -ExpectedStatus 201 -Body $walletBody
} else {
    Write-Host "Skipping backend-dependent tests (use -SkipBackend to skip)." -ForegroundColor Cyan
}

Write-Host "All tests passed." -ForegroundColor Green
exit 0

