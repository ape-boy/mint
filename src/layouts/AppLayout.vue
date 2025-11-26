<script setup lang="ts">
import { ref, computed } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { 
  HomeOutlined, 
  BuildOutlined, 
  RocketOutlined, 
  ProjectOutlined, 
  RobotOutlined, 
  SettingOutlined,
  SearchOutlined,
  BellOutlined,
  UserOutlined,
  MenuFoldOutlined,
  MenuUnfoldOutlined
} from '@ant-design/icons-vue'

const router = useRouter()
const route = useRoute()
const collapsed = ref(false)
const selectedKeys = ref<string[]>(['home'])

// Simple Breadcrumb Logic
const breadcrumbs = computed(() => {
  const path = route.path
  const parts = path.split('/').filter(Boolean)
  const crumbs = [{ name: 'Home', path: '/' }]
  
  let currentPath = ''
  parts.forEach(part => {
    currentPath += `/${part}`
    crumbs.push({ 
      name: part.charAt(0).toUpperCase() + part.slice(1), 
      path: currentPath 
    })
  })
  return crumbs
})

const handleMenuClick = (e: any) => {
  switch (e.key) {
    case 'home': router.push('/dashboard'); break;
    case 'project': router.push('/'); break;
    case 'build': router.push('/build'); break;
    case 'aiops': router.push('/aiops'); break;
    case 'settings': router.push('/settings'); break;
  }
}
</script>

<template>
  <a-layout class="app-layout">
    <a-layout-sider 
      v-model:collapsed="collapsed" 
      :trigger="null" 
      collapsible 
      width="260"
      class="enterprise-sider"
    >
      <div class="logo-container">
        <img src="/src/assets/logo.png" alt="Logo" class="logo-img" />
        <span v-if="!collapsed" class="logo-text">SE DevOps Portal</span>
      </div>
      
      <a-menu 
        v-model:selectedKeys="selectedKeys" 
        theme="dark" 
        mode="inline" 
        @click="handleMenuClick"
        class="enterprise-menu"
      >
        <a-menu-item key="home">
          <home-outlined />
          <span>Dashboard</span>
        </a-menu-item>
        <a-menu-item key="project">
          <project-outlined />
          <span>Projects</span>
        </a-menu-item>
        <a-menu-item key="build">
          <build-outlined />
          <span>Builds</span>
        </a-menu-item>
        <a-menu-item key="settings">
          <setting-outlined />
          <span>Settings</span>
        </a-menu-item>
        <a-menu-item key="aiops">
          <robot-outlined />
          <span>AI Ops (Admin)</span>
        </a-menu-item>
      </a-menu>
    </a-layout-sider>
    
    <a-layout>
      <a-layout-header class="enterprise-header">
        <div class="header-left">
          <menu-unfold-outlined
            v-if="collapsed"
            class="trigger"
            @click="() => (collapsed = !collapsed)"
          />
          <menu-fold-outlined v-else class="trigger" @click="() => (collapsed = !collapsed)" />
          
          <!-- Breadcrumbs -->
          <a-breadcrumb class="header-breadcrumb">
            <a-breadcrumb-item v-for="crumb in breadcrumbs" :key="crumb.path">
              <router-link :to="crumb.path">{{ crumb.name }}</router-link>
            </a-breadcrumb-item>
          </a-breadcrumb>
        </div>
        
        <div class="header-right">
          <div class="search-wrapper">
            <search-outlined class="search-icon" />
            <input type="text" placeholder="Search..." class="search-input" />
          </div>
          
          <a-button type="text" class="icon-btn">
            <bell-outlined />
          </a-button>
          
          <a-dropdown placement="bottomRight">
            <div class="user-profile">
              <a-avatar size="small" class="user-avatar">
                <template #icon><user-outlined /></template>
              </a-avatar>
              <span class="username">Admin User</span>
            </div>
            <template #overlay>
              <a-menu>
                <a-menu-item>Profile</a-menu-item>
                <a-menu-item>Logout</a-menu-item>
              </a-menu>
            </template>
          </a-dropdown>
        </div>
      </a-layout-header>
      
      <a-layout-content class="enterprise-content">
        <router-view v-slot="{ Component }">
          <transition name="fade" mode="out-in">
            <component :is="Component" />
          </transition>
        </router-view>
      </a-layout-content>
    </a-layout>
  </a-layout>
</template>

<style scoped>
.app-layout {
  min-height: 100vh;
}

/* Sidebar */
.enterprise-sider {
  background: #0f172a !important; /* Dark Navy Sidebar for Contrast */
  border-right: 1px solid #1e293b;
  box-shadow: 2px 0 8px rgba(0,0,0,0.15);
  z-index: 10;
}

.logo-container {
  height: 64px;
  display: flex;
  align-items: center;
  padding: 0 24px;
  border-bottom: 1px solid rgba(255, 255, 255, 0.1);
  background: #0f172a;
}

.logo-img {
  width: 32px;
  height: 32px;
  margin-right: 12px;
  border-radius: 4px;
}

.logo-text {
  font-size: 18px;
  font-weight: 700;
  color: #f8fafc; /* White Text */
  letter-spacing: -0.5px;
}

.enterprise-menu {
  background: transparent;
  border-right: none;
  padding-top: 16px;
}

.enterprise-menu :deep(.ant-menu-item) {
  margin: 4px 12px;
  width: calc(100% - 24px);
  border-radius: 4px;
  color: #94a3b8; /* Muted Text */
}

.enterprise-menu :deep(.ant-menu-item-selected) {
  background-color: var(--accent-primary) !important;
  color: white !important;
}

.enterprise-menu :deep(.ant-menu-item:hover) {
  color: white;
  background-color: rgba(255, 255, 255, 0.05);
}

/* Header */
.enterprise-header {
  background: #ffffff;
  padding: 0 24px;
  height: 64px;
  display: flex;
  align-items: center;
  justify-content: space-between;
  border-bottom: 1px solid var(--border-color);
  z-index: 9;
}

.header-left {
  display: flex;
  align-items: center;
  gap: 20px;
}

.trigger {
  font-size: 18px;
  cursor: pointer;
  transition: color 0.3s;
  color: var(--text-secondary);
}

.trigger:hover {
  color: var(--accent-primary);
}

.header-breadcrumb {
  margin-left: 8px;
}

.header-breadcrumb :deep(.ant-breadcrumb-link) {
  color: var(--text-secondary);
}

.header-breadcrumb :deep(.ant-breadcrumb-link a) {
  color: var(--text-secondary);
}

.header-breadcrumb :deep(.ant-breadcrumb-link a:hover) {
  color: var(--accent-primary);
}

/* Header Right */
.header-right {
  display: flex;
  align-items: center;
  gap: 16px;
}

.search-wrapper {
  position: relative;
  margin-right: 16px;
}

.search-icon {
  position: absolute;
  left: 10px;
  top: 50%;
  transform: translateY(-50%);
  color: var(--text-muted);
}

.search-input {
  background: var(--bg-secondary);
  border: 1px solid var(--border-color);
  border-radius: 4px;
  padding: 6px 12px 6px 32px;
  color: var(--text-primary);
  width: 200px;
  outline: none;
  transition: all 0.2s;
}

.search-input:focus {
  border-color: var(--accent-primary);
  background: #ffffff;
  width: 240px;
}

.icon-btn {
  color: var(--text-secondary);
  font-size: 18px;
}

.icon-btn:hover {
  color: var(--accent-primary);
}

.user-profile {
  display: flex;
  align-items: center;
  gap: 8px;
  cursor: pointer;
  padding: 4px 8px;
  border-radius: 4px;
  transition: background 0.2s;
}

.user-profile:hover {
  background: var(--bg-secondary);
}

.user-avatar {
  background: var(--accent-primary);
  color: white;
}

.username {
  font-size: 14px;
  font-weight: 500;
  color: var(--text-primary);
}

/* Content */
.enterprise-content {
  padding: 24px;
  background: var(--bg-secondary);
  overflow-y: auto;
}

/* Transitions */
.fade-enter-active,
.fade-leave-active {
  transition: opacity 0.2s ease;
}

.fade-enter-from,
.fade-leave-to {
  opacity: 0;
}
</style>
