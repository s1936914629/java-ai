<template>
  <div class="container mt-5">
    <h1 class="mb-4">模型训练管理</h1>

    <!-- 模型状态 -->
    <div class="card mb-4 status-card">
      <div class="card-body">
        <h5 class="card-title">模型状态</h5>
        <div v-if="modelStatus.trained">
          <span class="badge bg-success">已训练</span>
          <p class="mt-2">模型已完成训练，可以用于识别任务</p>
        </div>
        <div v-else>
          <span class="badge bg-warning">未训练</span>
          <p class="mt-2">模型尚未训练，请点击下方按钮开始训练</p>
        </div>
      </div>
    </div>

    <!-- 训练控制 -->
    <div class="card mb-4">
      <div class="card-body">
        <h5 class="card-title">训练参数</h5>
        <div class="row">
          <div class="col-md-4">
            <label for="epochs" class="form-label">训练轮数</label>
            <input type="number" class="form-control" v-model.number="epochs" min="1" max="50">
            <div class="form-text">建议5-10轮，轮数越多训练时间越长</div>
          </div>
          <div class="col-md-8 d-flex align-items-end">
            <button type="button" class="btn btn-primary me-2" @click="startTraining" :disabled="isTraining">
              <i class="bi bi-play-circle"></i> 开始训练
            </button>
            <button type="button" class="btn btn-outline-secondary" @click="stopTraining" :disabled="!isTraining">
              <i class="bi bi-stop-circle"></i> 停止训练
            </button>
          </div>
        </div>

        <!-- 训练进度 -->
        <div id="trainingProgress" class="mt-4" v-if="isTraining">
          <div class="d-flex justify-content-between mb-2">
            <span>训练进度</span>
            <span id="progressText">{{ Math.round(progress) }}%</span>
          </div>
          <div class="progress">
            <div id="progressBar" class="progress-bar progress-bar-striped progress-bar-animated" 
                 :style="{ width: progress + '%' }"></div>
          </div>
          <div class="mt-2 text-muted" id="timeEstimate">{{ timeEstimate }}</div>
        </div>
      </div>
    </div>

    <!-- 训练日志 -->
    <div class="card mb-4">
      <div class="card-body">
        <h5 class="card-title">训练日志</h5>
        <div class="training-log" id="trainingLog">
          <div class="log-item" v-for="(log, index) in logs" :key="index">{{ log }}</div>
        </div>
      </div>
    </div>

    <!-- 训练结果 -->
    <div class="card" v-if="trainingResult">
      <div class="card-body">
        <h5 class="card-title">训练结果</h5>
        <div v-if="trainingResult.success">
          <div class="alert alert-success">
            <h6><i class="bi bi-check-circle"></i> 训练成功完成！</h6>
            <p>训练时间: {{ trainingResult.trainingTime }} 秒</p>
          </div>
          <div class="row mt-3">
            <div class="col-md-3">
              <div class="card text-center">
                <div class="card-body">
                  <h2 class="text-primary">{{ (trainingResult.accuracy * 100).toFixed(2) }}%</h2>
                  <p class="text-muted">准确率</p>
                </div>
              </div>
            </div>
            <div class="col-md-3">
              <div class="card text-center">
                <div class="card-body">
                  <h2 class="text-success">{{ (trainingResult.precision * 100).toFixed(2) }}%</h2>
                  <p class="text-muted">精确率</p>
                </div>
              </div>
            </div>
            <div class="col-md-3">
              <div class="card text-center">
                <div class="card-body">
                  <h2 class="text-warning">{{ (trainingResult.recall * 100).toFixed(2) }}%</h2>
                  <p class="text-muted">召回率</p>
                </div>
              </div>
            </div>
            <div class="col-md-3">
              <div class="card text-center">
                <div class="card-body">
                  <h2 class="text-info">{{ (trainingResult.f1 * 100).toFixed(2) }}%</h2>
                  <p class="text-muted">F1分数</p>
                </div>
              </div>
            </div>
          </div>
          <div class="mt-4">
            <h6>混淆矩阵:</h6>
            <pre class="bg-light p-3">{{ trainingResult.confusionMatrix }}</pre>
          </div>
        </div>
        <div v-else>
          <div class="alert alert-danger">
            <h6><i class="bi bi-exclamation-circle"></i> 训练失败！</h6>
            <p>{{ trainingResult.error }}</p>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script>
import { ref, onMounted, onBeforeUnmount } from 'vue'

export default {
  name: 'TrainView',
  setup() {
    const epochs = ref(5)
    const isTraining = ref(false)
    const progress = ref(0)
    const timeEstimate = ref('')
    const logs = ref(['等待训练开始...'])
    const trainingResult = ref(null)
    const modelStatus = ref({ trained: false })
    const startTime = ref(null)
    let progressInterval = null

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

    // 开始训练
    const startTraining = async () => {
      if (epochs.value < 1) {
        addLog('请输入有效的训练轮数')
        return
      }

      isTraining.value = true
      progress.value = 0
      timeEstimate.value = '' // 重置时间估计
      logs.value = []
      trainingResult.value = null
      startTime.value = Date.now() // 记录训练开始时间
      addLog('开始训练模型...')

      // 开始模拟进度更新
      simulateProgress()

      try {
        const response = await fetch(`/api/train?epochs=${epochs.value}`, {
          method: 'POST'
        })
        const data = await response.json()
        isTraining.value = false
        clearInterval(progressInterval)

        if (data.success) {
          addLog('训练完成！')
          trainingResult.value = data
          modelStatus.value.trained = true
          // 设置进度为100%
          progress.value = 100
        } else {
          addLog(`训练失败: ${data.error}`)
          trainingResult.value = data
        }
      } catch (error) {
        isTraining.value = false
        clearInterval(progressInterval)
        addLog(`请求失败: ${error.message}`)
        trainingResult.value = { success: false, error: error.message }
      }
    }

    // 停止训练
    const stopTraining = () => {
      if (!isTraining.value) return

      isTraining.value = false
      clearInterval(progressInterval)
      timeEstimate.value = '' // 清除时间估计
      addLog('训练已停止')
      trainingResult.value = { success: false, error: '训练已被用户停止' }
    }

    // 模拟进度更新
    const simulateProgress = () => {
      if (!isTraining.value || !startTime.value) return

      // 使用当前progress.value作为初始值
      let simulatedProgress = progress.value
      progressInterval = setInterval(() => {
        if (!isTraining.value) {
          clearInterval(progressInterval)
          return
        }

        // 计算已经过去的时间（秒）
        const elapsedSeconds = (Date.now() - startTime.value) / 1000
        
        // 假设每轮训练大约需要5-10秒（根据实际情况调整）
        const estimatedSecondsPerEpoch = 7
        const totalEstimatedSeconds = epochs.value * estimatedSecondsPerEpoch
        
        // 计算预期进度（基于已过去时间和总预计时间）
        let expectedProgress = (elapsedSeconds / totalEstimatedSeconds) * 100
        
        // 确保进度不会超过95%，留一点空间给最终完成
        expectedProgress = Math.min(expectedProgress, 95)
        
        // 确保进度缓慢、平滑地增加
        simulatedProgress = Math.min(simulatedProgress + Math.random() * 2, expectedProgress)
        
        progress.value = simulatedProgress
        
        // 更新时间估计
        const remainingSeconds = Math.max(0, totalEstimatedSeconds - elapsedSeconds)
        const minutes = Math.floor(remainingSeconds / 60)
        const seconds = Math.floor(remainingSeconds % 60)
        timeEstimate.value = `预计剩余时间: ${minutes} 分 ${seconds} 秒`
        
      }, 300) // 更频繁地更新进度，使动画更平滑
    }

    // 添加日志
    const addLog = (message) => {
      const logMessage = `${new Date().toLocaleTimeString()} - ${message}`
      logs.value.push(logMessage)
      // 自动滚动到最新日志
      setTimeout(() => {
        const logDiv = document.getElementById('trainingLog')
        if (logDiv) {
          logDiv.scrollTop = logDiv.scrollHeight
        }
      }, 100)
    }

    onMounted(() => {
      checkModelStatus()
    })

    onBeforeUnmount(() => {
      if (progressInterval) {
        clearInterval(progressInterval)
      }
    })

    return {
      epochs,
      isTraining,
      progress,
      timeEstimate,
      logs,
      trainingResult,
      modelStatus,
      startTraining,
      stopTraining
    }
  }
}
</script>

<style scoped>
.status-card {
  border-left: 4px solid #0d6efd;
}

.training-log {
  max-height: 400px;
  overflow-y: auto;
  background: #f8f9fa;
  border-radius: 5px;
  padding: 15px;
}

.log-item {
  padding: 5px 0;
  border-bottom: 1px solid #dee2e6;
}

.progress {
  height: 25px;
}
</style>