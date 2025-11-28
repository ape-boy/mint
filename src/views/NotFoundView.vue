<script setup lang="ts">
import { useRouter } from 'vue-router'

const router = useRouter()

function goHome() {
  router.push({ name: 'projects' })
}

function goBack() {
  router.back()
}
</script>

<template>
  <div class="not-found-container">
    <div class="not-found-content">
      <div class="error-code">404</div>
      <h1 class="error-title">Page Not Found</h1>
      <p class="error-message">
        The page you are looking for does not exist or has been moved.
      </p>
      <div class="action-buttons">
        <a-button type="primary" size="large" @click="goHome">
          Go to Home
        </a-button>
        <a-button size="large" @click="goBack">
          Go Back
        </a-button>
      </div>
    </div>
  </div>
</template>

<style scoped>
.not-found-container {
  min-height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  background: linear-gradient(135deg, #0f0f23 0%, #1a1a3e 50%, #0f0f23 100%);
  padding: 24px;
}

.not-found-content {
  text-align: center;
  max-width: 480px;
}

.error-code {
  font-size: 120px;
  font-weight: 700;
  line-height: 1;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
  margin-bottom: 16px;
}

.error-title {
  font-size: 32px;
  color: #fff;
  margin-bottom: 12px;
}

.error-message {
  font-size: 16px;
  color: rgba(255, 255, 255, 0.65);
  margin-bottom: 32px;
}

.action-buttons {
  display: flex;
  gap: 16px;
  justify-content: center;
}

.action-buttons .ant-btn {
  min-width: 120px;
}
</style>
