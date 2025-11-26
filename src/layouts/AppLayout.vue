<script setup lang="ts">
import { ref, computed, watch } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import {
  HomeOutlined,
  BuildOutlined,
  ProjectOutlined,
  SettingOutlined,
  BellOutlined,
  UserOutlined,
  MenuFoldOutlined,
  MenuUnfoldOutlined,
} from '@ant-design/icons-vue'

const router = useRouter()
const route = useRoute()
const collapsed = ref(false)
const selectedKeys = ref<string[]>([])

// Route to menu key mapping
const routeMenuMap: Record<string, string> = {
  '/': 'projects',
  '/dashboard': 'dashboard',
  '/build': 'builds',
  '/settings': 'settings',
}

// Update selected menu based on route
watch(
  () => route.path,
  (path) => {
    // Find the base path (first segment)
    const basePath = '/' + (path.split('/').filter(Boolean)[0] || '')
    const menuKey = routeMenuMap[basePath] || routeMenuMap[path]
    if (menuKey) {
      selectedKeys.value = [menuKey]
    }
  },
  { immediate: true }
)

// Breadcrumb generation based on route meta
const breadcrumbs = computed(() => {
  const crumbs = [{ name: 'Home', path: '/' }]

  if (route.meta?.title && route.path !== '/') {
    // Add parent breadcrumb if exists
    if (route.meta?.breadcrumb) {
      const parentRoute = router.getRoutes().find(r => r.name === route.meta?.breadcrumb)
      if (parentRoute) {
        crumbs.push({
          name: parentRoute.meta?.title as string || route.meta.breadcrumb as string,
          path: parentRoute.path,
        })
      }
    }
    // Add current page
    crumbs.push({
      name: route.meta.title as string,
      path: route.path,
    })
  }

  return crumbs
})

const menuItems = [
  { key: 'dashboard', icon: HomeOutlined, label: 'Dashboard', path: '/dashboard' },
  { key: 'projects', icon: ProjectOutlined, label: 'Projects', path: '/' },
  { key: 'builds', icon: BuildOutlined, label: 'Builds', path: '/build' },
  { key: 'settings', icon: SettingOutlined, label: 'Settings', path: '/settings' },
]

function handleMenuClick(e: { key: string }) {
  const item = menuItems.find(m => m.key === e.key)
  if (item) {
    router.push(item.path)
  }
}

function goHome() {
  router.push('/')
}
</script>

<template>
  <a-layout class="app-layout">
    <a-layout-sider
      v-model:collapsed="collapsed"
      :trigger="null"
      collapsible
      width="260"
      class="app-sider"
    >
      <div class="logo-container" @click="goHome">
        <img src="@/assets/mint_portal_logo.png" alt="MintPortal" class="logo-image" />
        <span v-if="!collapsed" class="logo-text">MintPortal</span>
      </div>

      <a-menu
        v-model:selectedKeys="selectedKeys"
        theme="dark"
        mode="inline"
        @click="handleMenuClick"
        class="app-menu"
      >
        <a-menu-item v-for="item in menuItems" :key="item.key">
          <component :is="item.icon" />
          <span>{{ item.label }}</span>
        </a-menu-item>
      </a-menu>
    </a-layout-sider>

    <a-layout>
      <a-layout-header class="app-header">
        <div class="header-left">
          <MenuUnfoldOutlined
            v-if="collapsed"
            class="trigger"
            @click="collapsed = !collapsed"
          />
          <MenuFoldOutlined
            v-else
            class="trigger"
            @click="collapsed = !collapsed"
          />

          <a-breadcrumb class="header-breadcrumb">
            <a-breadcrumb-item v-for="crumb in breadcrumbs" :key="crumb.path">
              <router-link :to="crumb.path">{{ crumb.name }}</router-link>
            </a-breadcrumb-item>
          </a-breadcrumb>
        </div>

        <div class="header-right">
          <a-button type="text" class="icon-btn">
            <BellOutlined />
          </a-button>

          <a-dropdown placement="bottomRight">
            <div class="user-profile">
              <a-avatar size="small" class="user-avatar">
                <template #icon><UserOutlined /></template>
              </a-avatar>
              <span class="username">Admin User</span>
            </div>
            <template #overlay>
              <a-menu>
                <a-menu-item @click="router.push('/settings')">Profile</a-menu-item>
                <a-menu-item @click="router.push('/login')">Logout</a-menu-item>
              </a-menu>
            </template>
          </a-dropdown>
        </div>
      </a-layout-header>

      <a-layout-content class="app-content">
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
.app-sider {
  background: var(--color-bg-primary) !important;
  border-right: 1px solid var(--color-border-light);
  box-shadow: 2px 0 8px rgba(0, 0, 0, 0.3);
  z-index: 10;
}

.logo-container {
  height: 64px;
  display: flex;
  align-items: center;
  padding: 0 var(--spacing-lg);
  border-bottom: 1px solid var(--color-border-light);
  background: transparent;
  cursor: pointer;
  transition: background var(--transition-fast);
}

.logo-container:hover {
  background: rgba(255, 255, 255, 0.03);
}

.logo-image {
  width: 32px;
  height: 32px;
  margin-right: var(--spacing-sm);
}

.logo-text {
  font-size: var(--font-size-xl);
  font-weight: var(--font-weight-bold);
  background: linear-gradient(135deg, var(--color-accent-primary), var(--color-accent-secondary));
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
}

.app-menu {
  background: transparent;
  border-right: none;
  padding-top: var(--spacing-md);
}

.app-menu :deep(.ant-menu-item) {
  margin: 4px var(--spacing-sm);
  width: calc(100% - var(--spacing-lg));
  border-radius: var(--radius-sm);
  color: var(--color-text-muted);
}

.app-menu :deep(.ant-menu-item-selected) {
  background: linear-gradient(135deg, var(--color-accent-primary), var(--color-accent-secondary)) !important;
  color: var(--color-bg-primary) !important;
  font-weight: var(--font-weight-semibold);
}

.app-menu :deep(.ant-menu-item:hover:not(.ant-menu-item-selected)) {
  color: var(--color-text-primary);
  background-color: var(--color-bg-tertiary);
}

/* Header */
.app-header {
  background: rgba(24, 24, 27, 0.8);
  backdrop-filter: blur(12px);
  padding: 0 var(--spacing-2xl);
  height: 64px;
  display: flex;
  align-items: center;
  justify-content: space-between;
  border-bottom: 1px solid var(--color-border-light);
  z-index: 9;
  position: sticky;
  top: 0;
}

.header-left {
  display: flex;
  align-items: center;
  gap: var(--spacing-lg);
}

.trigger {
  font-size: var(--font-size-lg);
  cursor: pointer;
  transition: color var(--transition-fast);
  color: var(--color-text-secondary);
}

.trigger:hover {
  color: var(--color-accent-primary);
}

.header-breadcrumb {
  margin-left: var(--spacing-sm);
}

.header-breadcrumb :deep(.ant-breadcrumb-link),
.header-breadcrumb :deep(.ant-breadcrumb-link a) {
  color: var(--color-text-secondary);
}

.header-breadcrumb :deep(.ant-breadcrumb-link a:hover) {
  color: var(--color-accent-primary);
}

/* Header Right */
.header-right {
  display: flex;
  align-items: center;
  gap: var(--spacing-md);
}

.icon-btn {
  color: var(--color-text-secondary);
  font-size: var(--font-size-lg);
}

.icon-btn:hover {
  color: var(--color-accent-primary);
}

.user-profile {
  display: flex;
  align-items: center;
  gap: var(--spacing-sm);
  cursor: pointer;
  padding: 4px var(--spacing-sm);
  border-radius: var(--radius-sm);
  transition: background var(--transition-fast);
}

.user-profile:hover {
  background: var(--color-bg-tertiary);
}

.user-avatar {
  background: var(--color-accent-primary);
  color: var(--color-bg-primary);
  font-weight: var(--font-weight-semibold);
}

.username {
  font-size: var(--font-size-sm);
  font-weight: var(--font-weight-medium);
  color: var(--color-text-primary);
}

/* Content */
.app-content {
  padding: var(--spacing-2xl);
  background: var(--color-bg-primary);
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
