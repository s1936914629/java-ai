# 测试限流功能
# 发送15个请求，看看是否会触发限流

Write-Host "开始测试限流功能..."
Write-Host "发送15个请求到 /api/llm/simple-chat 接口"
Write-Host ""

for ($i = 1; $i -le 15; $i++) {
    Write-Host "发送第 $i 个请求..."
    $response = Invoke-RestMethod -Uri "http://localhost:8080/api/llm/simple-chat" `
        -Method POST `
        -ContentType "application/json" `
        -Body '"测试限流功能"'
    Write-Host "响应: $response"
    Write-Host ""
    # 稍微延迟一下，模拟真实请求间隔
    Start-Sleep -Milliseconds 100
}

Write-Host "测试完成！"
