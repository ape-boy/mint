<script setup lang="ts">
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import { UserOutlined, LockOutlined } from '@ant-design/icons-vue'

const router = useRouter()

const username = ref('')
const password = ref('')
const loading = ref(false)
const rememberMe = ref(false)

async function handleLogin() {
  loading.value = true

  // Simulate API call
  setTimeout(() => {
    loading.value = false
    router.push('/')
  }, 1000)
}
</script>

<template>
  <div class="login-view">
    <div class="login-container">
      <div class="login-card">
        <!-- Logo & Title -->
        <div class="login-header">
          <img src="@/assets/mint_portal_logo.png" alt="MintPortal" class="logo" />
          <h1>MintPortal</h1>
          <p>CI/CD Build Management System</p>
        </div>

        <!-- Login Form -->
        <a-form layout="vertical" @finish="handleLogin">
          <a-form-item label="Username">
            <a-input
              v-model:value="username"
              placeholder="Enter your username"
              size="large"
            >
              <template #prefix><UserOutlined /></template>
            </a-input>
          </a-form-item>

          <a-form-item label="Password">
            <a-input-password
              v-model:value="password"
              placeholder="Enter your password"
              size="large"
            >
              <template #prefix><LockOutlined /></template>
            </a-input-password>
          </a-form-item>

          <div class="login-options">
            <a-checkbox v-model:checked="rememberMe">Remember me</a-checkbox>
            <a class="forgot-link">Forgot password?</a>
          </div>

          <a-button
            type="primary"
            html-type="submit"
            size="large"
            block
            :loading="loading"
            class="login-btn"
          >
            Sign In
          </a-button>
        </a-form>

        <!-- Footer -->
        <div class="login-footer">
          <p>SSD Firmware Development Organization</p>
        </div>
      </div>
    </div>
  </div>
</template>

<style scoped>
.login-view {
  min-height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  background: var(--color-bg-primary);
  background-image: url('@/assets/mint_portal_background.png');
  background-size: cover;
  background-position: center;
}

.login-container {
  width: 100%;
  max-width: 420px;
  padding: var(--spacing-lg);
}

.login-card {
  background: var(--color-bg-secondary);
  border: 1px solid var(--color-border-light);
  border-radius: var(--radius-lg);
  padding: var(--spacing-2xl);
  backdrop-filter: blur(10px);
}

.login-header {
  text-align: center;
  margin-bottom: var(--spacing-2xl);
}

.login-header .logo {
  width: 80px;
  height: 80px;
  margin-bottom: var(--spacing-md);
}

.login-header h1 {
  margin: 0;
  font-size: var(--font-size-3xl);
  font-weight: var(--font-weight-bold);
  background: linear-gradient(135deg, var(--color-accent-primary), var(--color-accent-secondary));
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
}

.login-header p {
  margin: var(--spacing-xs) 0 0;
  color: var(--color-text-muted);
  font-size: var(--font-size-sm);
}

.login-options {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: var(--spacing-lg);
}

.forgot-link {
  color: var(--color-accent-primary);
  font-size: var(--font-size-sm);
  cursor: pointer;
}

.forgot-link:hover {
  color: var(--color-accent-secondary);
}

.login-btn {
  margin-top: var(--spacing-md);
  height: 48px;
  font-size: var(--font-size-md);
  font-weight: var(--font-weight-semibold);
}

.login-footer {
  text-align: center;
  margin-top: var(--spacing-xl);
  padding-top: var(--spacing-lg);
  border-top: 1px solid var(--color-border-light);
}

.login-footer p {
  margin: 0;
  color: var(--color-text-muted);
  font-size: var(--font-size-xs);
}
</style>
