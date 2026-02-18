# Test rate limiting functionality
# Send 15 requests to test if rate limiting is triggered

Write-Host "Starting rate limit test..."
Write-Host "Sending 15 requests to /api/llm/simple-chat endpoint"
Write-Host ""

for ($i = 1; $i -le 15; $i++) {
    Write-Host "Sending request $i..."
    try {
        $response = Invoke-RestMethod -Uri "http://localhost:8080/api/llm/simple-chat" `
            -Method POST `
            -ContentType "application/json" `
            -Body '"Test rate limiting"'
        Write-Host "Response: $response"
    } catch {
        Write-Host "Error: $($_.Exception.Message)"
        Write-Host "Status code: $($_.Exception.Response.StatusCode.Value__)"
    }
    Write-Host ""
    # Add a small delay to simulate real request intervals
    Start-Sleep -Milliseconds 100
}

Write-Host "Test completed!"
