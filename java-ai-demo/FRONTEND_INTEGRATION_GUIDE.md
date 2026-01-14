# 前端集成指南

## 1. 概述

本指南介绍如何将前端应用与Java AI Demo的图像分类API进行集成。后端提供了基于ResNet50模型的图像分类服务，前端可以通过REST API上传图片并获取分类结果。

## 2. 后端API接口

### 2.1 图像分类接口

**URL**: `/api/predict`
**方法**: `POST`
**内容类型**: `multipart/form-data`
**请求参数**:
- `image`: 要分类的图片文件（支持JPG、PNG等格式）

**响应格式**:
```json
{
  "classId": 285,
  "score": 0.4010,
  "latencyMs": 270
}
```

**响应字段说明**:
- `classId`: 预测的类别ID（ImageNet类别索引）
- `score`: 预测的置信度分数（0-1之间）
- `latencyMs`: 推理延迟时间（毫秒）

### 2.2 健康检查接口

**URL**: `/api/health`
**方法**: `GET`
**响应**: `Java AI 推理服务运行正常！`

## 3. 前端集成示例

### 3.1 使用原生JavaScript实现

```html
<!DOCTYPE html>
<html lang="zh-CN">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>图像分类演示</title>
    <style>
        body {
            font-family: Arial, sans-serif;
            max-width: 800px;
            margin: 0 auto;
            padding: 20px;
        }
        .container {
            display: flex;
            flex-direction: column;
            align-items: center;
        }
        .input-group {
            margin: 20px 0;
        }
        #imagePreview {
            width: 300px;
            height: 300px;
            border: 1px solid #ccc;
            margin: 20px 0;
            display: flex;
            align-items: center;
            justify-content: center;
            overflow: hidden;
        }
        #imagePreview img {
            max-width: 100%;
            max-height: 100%;
        }
        #result {
            margin-top: 20px;
            padding: 15px;
            border-radius: 5px;
            background-color: #f5f5f5;
            width: 100%;
        }
        .button {
            padding: 10px 20px;
            background-color: #4CAF50;
            color: white;
            border: none;
            border-radius: 5px;
            cursor: pointer;
        }
        .button:hover {
            background-color: #45a049;
        }
    </style>
</head>
<body>
    <div class="container">
        <h1>图像分类演示</h1>
        
        <div class="input-group">
            <label for="imageFile">选择图片:</label>
            <input type="file" id="imageFile" accept="image/*">
        </div>
        
        <div id="imagePreview">
            <span>预览区域</span>
        </div>
        
        <button class="button" id="predictBtn">进行分类</button>
        
        <div id="result">
            <h3>分类结果</h3>
            <div id="predictionResult">请选择图片并点击分类按钮</div>
        </div>
    </div>

    <script>
        // 图片预览
        document.getElementById('imageFile').addEventListener('change', function(e) {
            const file = e.target.files[0];
            if (file) {
                const reader = new FileReader();
                reader.onload = function(event) {
                    const preview = document.getElementById('imagePreview');
                    preview.innerHTML = `<img src="${event.target.result}" alt="预览">`;
                };
                reader.readAsDataURL(file);
            }
        });

        // 预测按钮点击事件
        document.getElementById('predictBtn').addEventListener('click', async function() {
            const fileInput = document.getElementById('imageFile');
            const file = fileInput.files[0];
            
            if (!file) {
                alert('请先选择图片');
                return;
            }

            const resultDiv = document.getElementById('predictionResult');
            resultDiv.innerHTML = '<p>正在分类中...</p>';

            try {
                const formData = new FormData();
                formData.append('image', file);

                const response = await fetch('http://localhost:8080/api/predict', {
                    method: 'POST',
                    body: formData
                });

                if (!response.ok) {
                    throw new Error('服务器错误: ' + response.status);
                }

                const result = await response.json();
                
                // 显示结果
                resultDiv.innerHTML = `
                    <p><strong>类别ID:</strong> ${result.classId}</p>
                    <p><strong>置信度:</strong> ${result.score.toFixed(4)}</p>
                    <p><strong>推理延迟:</strong> ${result.latencyMs}ms</p>
                `;

            } catch (error) {
                resultDiv.innerHTML = `<p style="color: red;">错误: ${error.message}</p>`;
            }
        });
    </script>
</body>
</html>
```

### 3.2 使用Axios实现

如果使用Axios库，可以简化HTTP请求的代码：

```html
<!DOCTYPE html>
<html lang="zh-CN">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>图像分类演示</title>
    <script src="https://cdn.jsdelivr.net/npm/axios/dist/axios.min.js"></script>
    <style>
        /* 样式与上面的示例相同 */
    </style>
</head>
<body>
    <!-- HTML结构与上面的示例相同 -->
    
    <script>
        // 图片预览代码与上面的示例相同

        // 使用Axios进行预测
        document.getElementById('predictBtn').addEventListener('click', async function() {
            const fileInput = document.getElementById('imageFile');
            const file = fileInput.files[0];
            
            if (!file) {
                alert('请先选择图片');
                return;
            }

            const resultDiv = document.getElementById('predictionResult');
            resultDiv.innerHTML = '<p>正在分类中...</p>';

            try {
                const formData = new FormData();
                formData.append('image', file);

                const response = await axios.post('http://localhost:8080/api/predict', formData, {
                    headers: {
                        'Content-Type': 'multipart/form-data'
                    }
                });

                const result = response.data;
                
                // 显示结果
                resultDiv.innerHTML = `
                    <p><strong>类别ID:</strong> ${result.classId}</p>
                    <p><strong>置信度:</strong> ${result.score.toFixed(4)}</p>
                    <p><strong>推理延迟:</strong> ${result.latencyMs}ms</p>
                `;

            } catch (error) {
                let errorMessage = '未知错误';
                if (error.response) {
                    errorMessage = `服务器错误: ${error.response.status} ${error.response.statusText}`;
                } else if (error.request) {
                    errorMessage = '网络错误: 无法连接到服务器';
                } else {
                    errorMessage = `错误: ${error.message}`;
                }
                resultDiv.innerHTML = `<p style="color: red;">${errorMessage}</p>`;
            }
        });
    </script>
</body>
</html>
```

### 3.3 使用React实现

```jsx
import React, { useState } from 'react';
import axios from 'axios';

function ImageClassifier() {
  const [selectedFile, setSelectedFile] = useState(null);
  const [previewUrl, setPreviewUrl] = useState(null);
  const [prediction, setPrediction] = useState(null);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState(null);

  // 处理文件选择
  const handleFileChange = (event) => {
    const file = event.target.files[0];
    if (file) {
      setSelectedFile(file);
      setPreviewUrl(URL.createObjectURL(file));
    }
  };

  // 处理分类请求
  const handlePredict = async () => {
    if (!selectedFile) {
      alert('请先选择图片');
      return;
    }

    setLoading(true);
    setError(null);
    setPrediction(null);

    try {
      const formData = new FormData();
      formData.append('image', selectedFile);

      const response = await axios.post('http://localhost:8080/api/predict', formData, {
        headers: {
          'Content-Type': 'multipart/form-data'
        }
      });

      setPrediction(response.data);
    } catch (err) {
      let errorMessage = '未知错误';
      if (err.response) {
        errorMessage = `服务器错误: ${err.response.status} ${err.response.statusText}`;
      } else if (err.request) {
        errorMessage = '网络错误: 无法连接到服务器';
      } else {
        errorMessage = `错误: ${err.message}`;
      }
      setError(errorMessage);
    } finally {
      setLoading(false);
    }
  };

  return (
    <div style={{ maxWidth: '800px', margin: '0 auto', padding: '20px' }}>
      <h1>图像分类演示</h1>
      
      <div style={{ marginBottom: '20px' }}>
        <input type="file" accept="image/*" onChange={handleFileChange} />
      </div>
      
      {previewUrl && (
        <div style={{ marginBottom: '20px' }}>
          <img 
            src={previewUrl} 
            alt="预览" 
            style={{ maxWidth: '300px', maxHeight: '300px', border: '1px solid #ccc' }} 
          />
        </div>
      )}
      
      <button 
        onClick={handlePredict} 
        disabled={loading}
        style={{ 
          padding: '10px 20px', 
          backgroundColor: '#4CAF50', 
          color: 'white', 
          border: 'none', 
          borderRadius: '5px',
          cursor: loading ? 'not-allowed' : 'pointer'
        }}
      >
        {loading ? '分类中...' : '进行分类'}
      </button>
      
      {error && (
        <div style={{ marginTop: '20px', padding: '15px', backgroundColor: '#ffebee', color: '#c62828' }}>
          <h3>错误</h3>
          <p>{error}</p>
        </div>
      )}
      
      {prediction && (
        <div style={{ marginTop: '20px', padding: '15px', backgroundColor: '#e8f5e9', borderRadius: '5px' }}>
          <h3>分类结果</h3>
          <p><strong>类别ID:</strong> {prediction.classId}</p>
          <p><strong>置信度:</strong> {prediction.score.toFixed(4)}</p>
          <p><strong>推理延迟:</strong> {prediction.latencyMs}ms</p>
        </div>
      )}
    </div>
  );
}

export default ImageClassifier;
```

## 4. 启动后端服务

在前端调用API之前，需要先启动后端服务。使用以下命令启动后端：

```bash
cd e:\AI\Study\java-ai-week1\java-ai-demo
mvn spring-boot:run
```

服务将在 `http://localhost:8080` 上启动。

## 5. 处理跨域问题

如果前端应用与后端服务不在同一个域名或端口下，可能会遇到跨域问题。可以通过以下方式解决：

### 5.1 后端配置CORS

在Spring Boot应用中添加CORS配置：

```java
@Configuration
public class CorsConfig implements WebMvcConfigurer {
    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**")
                .allowedOrigins("*")
                .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")
                .allowedHeaders("*")
                .allowCredentials(true);
    }
}
```

### 5.2 使用代理

在前端开发环境中配置代理，例如在React的`package.json`中添加：

```json
"proxy": "http://localhost:8080"
```

## 6. 响应数据处理

前端收到后端的响应后，可以根据需要进行处理。以下是一个将类别ID转换为类名的示例：

```javascript
// 简化的ImageNet类别映射（完整映射需要1000个类别）
const imagenetClasses = {
  285: "Siamese cat, Siamese",
  281: "tabby, tabby cat",
  282: "tiger cat",
  283: "Persian cat",
  // 更多类别...
};

// 使用示例
if (prediction) {
  const className = imagenetClasses[prediction.classId] || `类别 ${prediction.classId}`;
  console.log(`预测类别: ${className}`);
}
```

## 7. 错误处理

前端应该处理各种可能的错误情况：

- 网络连接错误
- 服务器错误（5xx）
- 请求错误（4xx）
- 图片格式错误
- 空文件错误

## 8. 性能优化

- 限制上传图片的大小
- 使用图片压缩
- 添加加载状态指示器
- 优化图片预览显示

## 9. 完整的前端项目结构

```
frontend/
├── public/
│   ├── index.html
│   └── styles.css
├── src/
│   ├── components/
│   │   └── ImageClassifier.js
│   ├── utils/
│   │   └── api.js
│   ├── App.js
│   └── index.js
├── package.json
└── README.md
```

## 10. 部署建议

- 前端和后端可以分别部署在不同的服务器上
- 使用Nginx或Apache作为前端静态文件服务器和反向代理
- 配置HTTPS确保数据传输安全
- 考虑使用CDN加速静态资源

以上就是前端集成Java AI Demo的详细指南，希望对你有所帮助！