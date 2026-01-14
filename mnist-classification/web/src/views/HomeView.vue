<template>
  <div>
    <!-- 首页内容 -->
    <section class="hero-section text-center">
      <div class="container">
        <h1 class="display-4 mb-4">手写数字识别系统</h1>
        <p class="lead mb-4">基于Deeplearning4j和Spring Boot构建的智能识别平台</p>
        <p class="mb-4" v-if="trained">
          <span class="badge bg-success">模型已训练，可以开始识别</span>
        </p>
        <p class="mb-4" v-else>
          <span class="badge bg-warning">模型未训练，请先训练模型</span>
        </p>
        <div class="mt-4">
          <button class="btn btn-light btn-lg me-3" @click="navigateTo('train')">
            <i class="bi bi-gear"></i> 训练模型
          </button>
          <button class="btn btn-outline-light btn-lg" @click="navigateTo('predict')">
            <i class="bi bi-pencil"></i> 开始识别
          </button>
        </div>
      </div>
    </section>

    <!-- 功能特性 -->
    <section class="py-5">
      <div class="container">
        <h2 class="text-center mb-5">系统功能特性</h2>
        <div class="row g-4">
          <div class="col-md-4">
            <div class="card feature-card shadow">
              <div class="card-body text-center">
                <div class="mb-3">
                  <i class="bi bi-cpu fs-1 text-primary"></i>
                </div>
                <h4 class="card-title">深度学习模型</h4>
                <p class="card-text">使用卷积神经网络(CNN)，识别准确率达98%以上</p>
              </div>
            </div>
          </div>
          <div class="col-md-4">
            <div class="card feature-card shadow">
              <div class="card-body text-center">
                <div class="mb-3">
                  <i class="bi bi-pencil-square fs-1 text-success"></i>
                </div>
                <h4 class="card-title">实时手写识别</h4>
                <p class="card-text">支持画板手写输入，实时识别数字并显示置信度</p>
              </div>
            </div>
          </div>
          <div class="col-md-4">
            <div class="card feature-card shadow">
              <div class="card-body text-center">
                <div class="mb-3">
                  <i class="bi bi-graph-up fs-1 text-warning"></i>
                </div>
                <h4 class="card-title">训练可视化</h4>
                <p class="card-text">实时监控训练过程，查看准确率和损失曲线</p>
              </div>
            </div>
          </div>
        </div>
      </div>
    </section>

    <!-- 技术栈 -->
    <section class="py-5 bg-light">
      <div class="container">
        <h2 class="text-center mb-5">技术栈</h2>
        <div class="row text-center">
          <div class="col-md-3 mb-4">
            <img src="https://spring.io/images/projects/spring-boot-7f2e24e962af5c4c4e5c6c8c4c4c4c4c.svg" height="60" alt="Spring Boot">
            <h5 class="mt-3">Spring Boot</h5>
          </div>
          <div class="col-md-3 mb-4">
            <img src="https://deeplearning4j.org/img/deeplearning4j-logo.png" height="60" alt="Deeplearning4j">
            <h5 class="mt-3">Deeplearning4j</h5>
          </div>
          <div class="col-md-3 mb-4">
            <img src="https://upload.wikimedia.org/wikipedia/commons/2/29/Postgresql_elephant.svg" height="60" alt="PostgreSQL">
            <h5 class="mt-3">卷积神经网络</h5>
          </div>
          <div class="col-md-3 mb-4">
            <img src="https://getbootstrap.com/docs/5.1/assets/brand/bootstrap-logo.svg" height="60" alt="Bootstrap">
            <h5 class="mt-3">Bootstrap 5</h5>
          </div>
        </div>
      </div>
    </section>
  </div>
</template>

<script>
import { ref, onMounted } from 'vue'

export default {
  name: 'HomeView',
  emits: ['navigate'],
  setup(props, { emit }) {
    const trained = ref(false)

    const navigateTo = (route) => {
      emit('navigate', route)
    }

    // 检查模型是否已训练
    const checkModelStatus = async () => {
      try {
        const response = await fetch('/api/model/status')
        const result = await response.json()
        trained.value = result.trained
      } catch (error) {
        console.error('检查模型状态失败:', error)
      }
    }

    onMounted(() => {
      checkModelStatus()
    })

    return {
      trained,
      navigateTo
    }
  }
}
</script>