<template>
  <div class="container mt-5">
    <h1 class="mb-4">手写数字识别</h1>

    <!-- 警告信息 -->
    <div v-if="!modelStatus.trained" class="alert alert-warning">
      <i class="bi bi-exclamation-triangle"></i>
      模型尚未训练！请先到训练页面训练模型。
    </div>

    <div class="row">
      <!-- 左侧：画板区域 -->
      <div class="col-md-6">
        <div class="card">
          <div class="card-body">
            <h5 class="card-title">手写输入</h5>

            <!-- 画笔控制 -->
            <div class="brush-controls mb-3">
              <div class="row">
                <div class="col-6">
                  <label for="brushSize" class="form-label">画笔粗细</label>
                  <input type="range" class="form-range" id="brushSize" 
                         min="5" max="30" v-model.number="brushSize">
                </div>
                <div class="col-6">
                  <div class="d-grid gap-2">
                    <button class="btn btn-outline-danger" @click="clearCanvas">
                      <i class="bi bi-eraser"></i> 清空画板
                    </button>
                  </div>
                </div>
              </div>
            </div>

            <!-- 画布 -->
            <div class="canvas-container mb-3" style="width: 280px; height: 280px; display: inline-block;">
              <canvas ref="drawCanvas" id="drawCanvas" width="280" height="280" style="width: 100%; height: 100%; object-fit: contain;"></canvas>
            </div>

            <!-- 控制按钮 -->
            <div class="d-grid gap-2">
              <button class="btn btn-primary btn-lg" @click="predict" :disabled="!modelStatus.trained">
                <i class="bi bi-search"></i> 识别数字
              </button>
              <button class="btn btn-outline-secondary" @click="saveImage">
                <i class="bi bi-download"></i> 保存图片
              </button>
            </div>

            <!-- 文件上传 -->
            <div class="mt-4">
              <label class="form-label">或上传图片识别</label>
              <div class="input-group">
                <input type="file" class="form-control" id="imageUpload" 
                       accept=".png,.jpg,.jpeg" @change="uploadImage">
                <button class="btn btn-outline-secondary" type="button" 
                        @click="document.getElementById('imageUpload').click()">
                  <i class="bi bi-upload"></i>
                </button>
              </div>
              <div class="form-text">支持PNG、JPG格式，图片将被自动调整为28x28像素</div>
            </div>
          </div>
        </div>
      </div>

      <!-- 右侧：识别结果 -->
      <div class="col-md-6">
        <div class="card">
          <div class="card-body">
            <h5 class="card-title">识别结果</h5>

            <!-- 预测数字 -->
            <div class="text-center mb-4">
              <div class="digit-display" id="predictionResult">{{ predictionResult }}</div>
              <div class="text-muted" id="confidenceText">{{ confidenceText }}</div>
            </div>

            <!-- 置信度分布 -->
            <div id="probabilitiesSection" v-if="showProbabilities">
              <h6>置信度分布</h6>
              <div id="probabilityBars">
                <div class="row align-items-center mb-2" v-for="(prob, index) in probabilities" :key="index">
                  <div class="col-1">
                    <span class="badge" :class="{ 'bg-success': prob === maxProbability, 'bg-secondary': prob !== maxProbability }">
                      {{ index }}
                    </span>
                  </div>
                  <div class="col-9">
                    <div class="probability-bar">
                      <div class="probability-fill" :style="{ width: (prob * 100).toFixed(1) + '%' }"></div>
                    </div>
                  </div>
                  <div class="col-2">
                    <small>{{ (prob * 100).toFixed(1) }}%</small>
                  </div>
                </div>
              </div>
            </div>

            <!-- 处理后的图像预览 -->
            <div class="mt-4">
              <h6>处理后的图像</h6>
              <div class="text-center">
                <img id="processedImage" class="img-fluid" :src="processedImageSrc"
                     style="image-rendering: pixelated; width: 112px;">
                <div class="form-text">28x28像素（放大显示）</div>
              </div>
            </div>
          </div>
        </div>

        <!-- 使用说明 -->
        <div class="card mt-4">
          <div class="card-body">
            <h6><i class="bi bi-info-circle"></i> 使用说明</h6>
            <ul class="small">
              <li>在左侧画板上书写数字0-9</li>
              <li>点击"识别数字"按钮进行识别</li>
              <li>可使用滑块调整画笔粗细</li>
              <li>识别结果显示在右侧，包含置信度</li>
              <li>也可以直接上传数字图片进行识别</li>
            </ul>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script>
import { ref, onMounted, onBeforeUnmount } from 'vue'

export default {
  name: 'PredictView',
  setup() {
    const drawCanvas = ref(null)
    const brushSize = ref(15)
    const drawing = ref(false)
    const ctx = ref(null)
    const predictionResult = ref('?')
    const confidenceText = ref('请在手写区书写数字')
    const showProbabilities = ref(false)
    const probabilities = ref([])
    const processedImageSrc = ref('')
    const modelStatus = ref({ trained: false })
    let canvasElement = null

    // 初始化画板
    const initCanvas = () => {
      if (!drawCanvas.value) return
      
      canvasElement = drawCanvas.value
      ctx.value = canvasElement.getContext('2d')
      
      // 清空画布
      clearCanvas()
      
      // 设置画笔属性
      ctx.value.lineCap = 'round'
      ctx.value.lineJoin = 'round'
      ctx.value.strokeStyle = 'black'

      // 鼠标事件
      canvasElement.addEventListener('mousedown', startDrawing)
      canvasElement.addEventListener('mousemove', draw)
      canvasElement.addEventListener('mouseup', stopDrawing)
      canvasElement.addEventListener('mouseout', stopDrawing)

      // 触摸事件
      canvasElement.addEventListener('touchstart', handleTouch)
      canvasElement.addEventListener('touchmove', handleTouch)
      canvasElement.addEventListener('touchend', stopDrawing)
    }

    // 处理触摸事件
    const handleTouch = (e) => {
      e.preventDefault()
      if (!canvasElement || !ctx.value) return
      
      if (e.type === 'touchstart') {
        const touch = e.touches[0]
        const mouseEvent = new MouseEvent('mousedown', {
          clientX: touch.clientX,
          clientY: touch.clientY
        })
        canvasElement.dispatchEvent(mouseEvent)
      } else if (e.type === 'touchmove') {
        const touch = e.touches[0]
        const mouseEvent = new MouseEvent('mousemove', {
          clientX: touch.clientX,
          clientY: touch.clientY
        })
        canvasElement.dispatchEvent(mouseEvent)
      }
    }

    // 开始绘制
    const startDrawing = (e) => {
      if (!canvasElement || !ctx.value) return
      drawing.value = true
      draw(e)
    }

    // 绘制
    const draw = (e) => {
      if (!drawing.value || !canvasElement || !ctx.value) return
      
      ctx.value.lineWidth = brushSize.value
      
      const rect = canvasElement.getBoundingClientRect()
      
      // 计算缩放比例
      const scaleX = canvasElement.width / rect.width
      const scaleY = canvasElement.height / rect.height
      
      let x = 0
      let y = 0
      
      // 处理鼠标事件
      if (e.type === 'mousemove' || e.type === 'mousedown') {
        x = (e.clientX - rect.left) * scaleX
        y = (e.clientY - rect.top) * scaleY
      } 
      // 处理触摸事件
      else if (e.type === 'touchmove' || e.type === 'touchstart') {
        const touch = e.touches[0]
        x = (touch.clientX - rect.left) * scaleX
        y = (touch.clientY - rect.top) * scaleY
      }
      
      // 确保坐标在Canvas范围内
      x = Math.max(0, Math.min(x, canvasElement.width))
      y = Math.max(0, Math.min(y, canvasElement.height))
      
      ctx.value.lineTo(x, y)
      ctx.value.stroke()
      ctx.value.beginPath()
      ctx.value.moveTo(x, y)
    }

    // 停止绘制
    const stopDrawing = () => {
      drawing.value = false
      if (ctx.value) {
        ctx.value.beginPath()
      }
    }

    // 清空画布
    const clearCanvas = () => {
      if (!canvasElement || !ctx.value) return
      
      ctx.value.fillStyle = 'white'
      ctx.value.fillRect(0, 0, canvasElement.width, canvasElement.height)
      predictionResult.value = '?'
      confidenceText.value = '请在手写区书写数字'
      showProbabilities.value = false
      probabilities.value = []
      processedImageSrc.value = ''
    }

    // 预测
    const predict = async () => {
      if (!canvasElement || !modelStatus.value.trained) return
      
      // 获取画布图像数据
      const imageData = canvasElement.toDataURL('image/png')
      
      try {
        // 显示加载状态
        predictionResult.value = '...'
        confidenceText.value = '识别中...'
        
        // 发送预测请求
        const response = await fetch('/api/predict', {
          method: 'POST',
          headers: {
            'Content-Type': 'application/x-www-form-urlencoded',
          },
          body: 'image=' + encodeURIComponent(imageData)
        })
        
        const result = await response.json()
        
        if (result.success) {
          // 显示预测结果
          predictionResult.value = result.prediction
          
          // 显示置信度
          const maxProb = Math.max(...result.probabilities)
          confidenceText.value = `置信度: ${(maxProb * 100).toFixed(2)}%`
          
          // 显示概率分布
          showProbabilities.value = true
          probabilities.value = result.probabilities
          
          // 显示处理后的图像预览
          showProcessedImage(imageData)
        } else {
          predictionResult.value = '错误'
          confidenceText.value = result.error
        }
      } catch (error) {
        predictionResult.value = '错误'
        confidenceText.value = '请求失败'
        console.error('预测失败:', error)
      }
    }

    // 显示处理后的图像
    const showProcessedImage = (imageData) => {
      // 创建临时图像显示处理后的效果
      const img = new Image()
      img.onload = () => {
        // 创建缩小版的canvas
        const tempCanvas = document.createElement('canvas')
        const tempCtx = tempCanvas.getContext('2d')
        tempCanvas.width = 28
        tempCanvas.height = 28
        
        // 绘制并缩小
        tempCtx.drawImage(img, 0, 0, 28, 28)
        
        // 像素化处理
        processedImageSrc.value = tempCanvas.toDataURL()
      }
      img.src = imageData
    }

    // 上传图片
    const uploadImage = async (e) => {
      if (!e.target.files[0] || !modelStatus.value.trained) return
      
      const file = e.target.files[0]
      const formData = new FormData()
      formData.append('file', file)
      
      try {
        // 显示加载状态
        predictionResult.value = '...'
        confidenceText.value = '上传识别中...'
        
        // 发送上传请求
        const response = await fetch('/api/upload', {
          method: 'POST',
          body: formData
        })
        
        const result = await response.json()
        
        if (result.success) {
          // 显示结果
          predictionResult.value = result.prediction
          const maxProb = Math.max(...result.probabilities)
          confidenceText.value = `置信度: ${(maxProb * 100).toFixed(2)}%`
          
          // 显示概率分布
          showProbabilities.value = true
          probabilities.value = result.probabilities
          
          // 清空画板并显示上传的图片
          clearCanvas()
          const img = new Image()
          img.onload = () => {
            if (canvasElement && ctx.value) {
              ctx.value.drawImage(img, 0, 0, canvasElement.width, canvasElement.height)
              showProcessedImage(canvasElement.toDataURL())
            }
          }
          img.src = URL.createObjectURL(file)
        } else {
          alert('上传识别失败: ' + result.error)
        }
      } catch (error) {
        alert('上传失败: ' + error.message)
      }
      
      // 清空文件输入
      e.target.value = ''
    }

    // 保存图片
    const saveImage = () => {
      if (!canvasElement) return
      
      const link = document.createElement('a')
      link.download = 'mnist_digit_' + new Date().getTime() + '.png'
      link.href = canvasElement.toDataURL('image/png')
      link.click()
    }

    // 检查模型状态
    const checkModelStatus = async () => {
      try {
        const response = await fetch('/api/model/status')
        const result = await response.json()
        modelStatus.value = result
      } catch (error) {
        console.error('检查模型状态失败:', error)
        modelStatus.value = { trained: false }
      }
    }

    // 计算最大概率
    const maxProbability = ref(0)
    probabilities.value.forEach(prob => {
      if (prob > maxProbability.value) {
        maxProbability.value = prob
      }
    })

    onMounted(() => {
      // 延迟初始化画布，确保DOM已加载
      setTimeout(() => {
        initCanvas()
      }, 100)
      
      // 检查模型状态
      checkModelStatus()
    })

    onBeforeUnmount(() => {
      // 移除事件监听器
      if (canvasElement) {
        canvasElement.removeEventListener('mousedown', startDrawing)
        canvasElement.removeEventListener('mousemove', draw)
        canvasElement.removeEventListener('mouseup', stopDrawing)
        canvasElement.removeEventListener('mouseout', stopDrawing)
        canvasElement.removeEventListener('touchstart', handleTouch)
        canvasElement.removeEventListener('touchmove', handleTouch)
        canvasElement.removeEventListener('touchend', stopDrawing)
      }
    })

    return {
      drawCanvas,
      brushSize,
      predictionResult,
      confidenceText,
      showProbabilities,
      probabilities,
      processedImageSrc,
      modelStatus,
      maxProbability,
      clearCanvas,
      predict,
      saveImage,
      uploadImage
    }
  }
}
</script>