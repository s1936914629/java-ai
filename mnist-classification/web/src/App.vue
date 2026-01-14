<template>
  <div>
    <!-- 导航栏 -->
    <nav class="navbar navbar-expand-lg navbar-dark bg-dark">
      <div class="container">
        <a class="navbar-brand" href="/">MNIST识别系统</a>
        <button class="navbar-toggler" type="button" data-bs-toggle="collapse" data-bs-target="#navbarNav">
          <span class="navbar-toggler-icon"></span>
        </button>
        <div class="collapse navbar-collapse" id="navbarNav">
          <ul class="navbar-nav ms-auto">
            <li class="nav-item">
              <a class="nav-link" :class="{ active: currentRoute === 'home' }" href="#" @click.prevent="navigateTo('home')">首页</a>
            </li>
            <li class="nav-item">
              <a class="nav-link" :class="{ active: currentRoute === 'train' }" href="#" @click.prevent="navigateTo('train')">模型训练</a>
            </li>
            <li class="nav-item">
              <a class="nav-link" :class="{ active: currentRoute === 'predict' }" href="#" @click.prevent="navigateTo('predict')">在线识别</a>
            </li>
          </ul>
        </div>
      </div>
    </nav>

    <!-- 内容区域 -->
    <main>
      <HomeView v-if="currentRoute === 'home'" @navigate="navigateTo" />
      <TrainView v-else-if="currentRoute === 'train'" />
      <PredictView v-else-if="currentRoute === 'predict'" />
    </main>

    <!-- 页脚 -->
    <footer class="bg-dark text-white py-4">
      <div class="container text-center">
        <p class="mb-0">MNIST手写数字识别系统 &copy; 2024</p>
        <p class="mb-0">基于Deeplearning4j和Spring Boot构建</p>
      </div>
    </footer>
  </div>
</template>

<script>
import { ref, onMounted } from 'vue'
import HomeView from './views/HomeView.vue'
import TrainView from './views/TrainView.vue'
import PredictView from './views/PredictView.vue'

export default {
  name: 'App',
  components: {
    HomeView,
    TrainView,
    PredictView
  },
  setup() {
    const currentRoute = ref('home')

    const navigateTo = (route) => {
      currentRoute.value = route
    }

    onMounted(() => {
      // 可以在这里添加初始化逻辑
    })

    return {
      currentRoute,
      navigateTo
    }
  }
}
</script>

<style>
.hero-section {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white;
  padding: 100px 0;
}

.feature-card {
  transition: transform 0.3s;
  height: 100%;
}

.feature-card:hover {
  transform: translateY(-5px);
}

.canvas-container {
  border: 2px solid #ddd;
  border-radius: 10px;
  background: white;
  cursor: crosshair;
}

.probability-bar {
  height: 24px;
  margin: 5px 0;
  border-radius: 12px;
  background: #f0f0f0;
  overflow: hidden;
}

.probability-fill {
  height: 100%;
  background: linear-gradient(90deg, #4CAF50, #8BC34A);
  transition: width 0.5s;
}

.digit-display {
  font-size: 8rem;
  font-weight: bold;
  color: #0d6efd;
  text-shadow: 2px 2px 4px rgba(0,0,0,0.1);
}

.brush-controls {
  background: #f8f9fa;
  border-radius: 8px;
  padding: 15px;
}

.result-card {
  border: 2px solid #28a745;
  animation: pulse 2s infinite;
}

@keyframes pulse {
  0% { border-color: #28a745; }
  50% { border-color: #20c997; }
  100% { border-color: #28a745; }
}
</style>

