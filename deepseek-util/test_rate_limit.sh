#!/bin/bash

# 测试限流功能
# 发送15个请求，看看是否会触发限流

echo "开始测试限流功能..."
echo "发送15个请求到 /api/llm/simple-chat 接口"
echo ""

for i in {1..15}
do
    echo "发送第 $i 个请求..."
    response=$(curl -s -X POST "http://localhost:8080/api/llm/simple-chat" \
        -H "Content-Type: application/json" \
        -d '"测试限流功能"')
    echo "响应: $response"
    echo ""
    # 稍微延迟一下，模拟真实请求间隔
    sleep 0.1
done

echo "测试完成！"
