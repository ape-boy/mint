<script setup lang="ts">
import { ref, computed, watch } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import {
  BuildOutlined,
  ProjectOutlined,
  BellOutlined,
  UserOutlined,
  MenuFoldOutlined,
  MenuUnfoldOutlined,
  DashboardOutlined,
  UnorderedListOutlined,
  RocketOutlined,
  ExperimentOutlined,
  CodeOutlined,
  ArrowLeftOutlined,
} from '@ant-design/icons-vue'
import { useLayerStore } from '@/stores/layer'
import { useProjectStore } from '@/stores/project'

const router = useRouter()
const route = useRoute()
const layerStore = useLayerStore()
const projectStore = useProjectStore()

// Current project name for menu display
const currentProjectName = computed(() => {
  if (!route.params.projectId) return ''
  const project = projectStore.currentProject
  return project?.name || '프로젝트'
})
const collapsed = ref(false)
const selectedKeys = ref<string[]>([])
const openKeys = ref<string[]>([])

// Current project's layers grouped by type
const projectLayers = computed(() => {
  if (!route.params.projectId) return { release: [], layer: [], private: [] }
  const projectId = route.params.projectId as string
  const layers = layerStore.layers.filter(l => l.projectId === projectId)
  return {
    release: layers.filter(l => l.type === 'release'),
    layer: layers.filter(l => l.type === 'layer'),
    private: layers.filter(l => l.type === 'private')
  }
})

// Load layers when entering project detail
watch(
  () => route.params.projectId,
  async (projectId) => {
    if (projectId) {
      await layerStore.fetchLayers(projectId as string)
    }
  },
  { immediate: true }
)

// Update selected menu based on route
watch(
  [() => route.path, () => route.query.tab, () => route.query.layerId],
  ([path, tab, layerId]) => {
    if (path === '/') {
      selectedKeys.value = ['projects-list']
    } else if ((path as string).startsWith('/projects/')) {
      const tabValue = tab as string
      if (tabValue === 'build' && layerId) {
        selectedKeys.value = [`layer-${layerId}`]
        // Open build submenu
        if (!openKeys.value.includes('build-menu')) {
          openKeys.value = [...openKeys.value, 'build-menu']
        }
      } else {
        selectedKeys.value = ['project-overview']
      }
      // Open project submenu
      if (!openKeys.value.includes('current-project')) {
        openKeys.value = [...openKeys.value, 'current-project']
      }
    }
  },
  { immediate: true }
)

// Breadcrumb generation based on route meta
const breadcrumbs = computed(() => {
  const crumbs = [{ name: '홈', path: '/' }]

  if (route.meta?.title && route.path !== '/') {
    if (route.meta?.breadcrumb) {
      const parentRoute = router.getRoutes().find(r => r.name === route.meta?.breadcrumb)
      if (parentRoute) {
        crumbs.push({
          name: parentRoute.meta?.title as string || route.meta.breadcrumb as string,
          path: parentRoute.path,
        })
      }
    }
    crumbs.push({
      name: route.meta.title as string,
      path: route.path,
    })
  }

  return crumbs
})

function handleMenuClick(e: { key: string }) {
  // Handle layer menu clicks (layer-{layerId})
  if (e.key.startsWith('layer-')) {
    const layerId = e.key.replace('layer-', '')
    if (route.params.projectId) {
      router.push(`/projects/${route.params.projectId}?tab=build&layerId=${layerId}`)
    }
    return
  }

  switch (e.key) {
    case 'projects-list':
      router.push('/')
      break
    case 'project-overview':
      if (route.params.projectId) {
        router.push(`/projects/${route.params.projectId}?tab=overview`)
      } else {
        router.push('/')
      }
      break
  }
}

function goHome() {
  router.push('/')
}

function goBack() {
  router.push('/')
}

// Check if we're on a project detail page
const isProjectDetailPage = computed(() => {
  return route.path.startsWith('/projects/') && route.params.projectId
})
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
        v-model:openKeys="openKeys"
        theme="dark"
        mode="inline"
        @click="handleMenuClick"
        class="app-menu"
      >
        <!-- 과제 목록 (항상 표시) -->
        <a-menu-item key="projects-list">
          <ProjectOutlined />
          <span>과제 목록</span>
        </a-menu-item>

        <!-- 프로젝트 상세 페이지에서만 표시되는 메뉴 -->
        <template v-if="isProjectDetailPage">
          <a-divider class="menu-divider" />

          <!-- 뒤로가기 버튼 -->
          <div class="back-button" @click="goBack">
            <ArrowLeftOutlined />
            <span v-if="!collapsed">과제 목록으로</span>
          </div>

          <!-- 현재 과제 서브메뉴 -->
          <a-sub-menu key="current-project">
            <template #icon><DashboardOutlined /></template>
            <template #title>{{ currentProjectName }}</template>

            <!-- Overview (기본정보 + Summary 통합) -->
            <a-menu-item key="project-overview">
              <DashboardOutlined />
              <span>Overview</span>
            </a-menu-item>

            <!-- Build 서브메뉴 -->
            <a-sub-menu key="build-menu">
              <template #icon><BuildOutlined /></template>
              <template #title>Build</template>

              <!-- Release -->
              <a-menu-item-group v-if="projectLayers.release.length > 0" title="Release">
                <a-menu-item
                  v-for="layer in projectLayers.release"
                  :key="`layer-${layer.id}`"
                >
                  <RocketOutlined />
                  <span>{{ layer.name }}</span>
                </a-menu-item>
              </a-menu-item-group>

              <!-- Layer -->
              <a-menu-item-group v-if="projectLayers.layer.length > 0" title="Layer">
                <a-menu-item
                  v-for="layer in projectLayers.layer"
                  :key="`layer-${layer.id}`"
                >
                  <CodeOutlined />
                  <span>{{ layer.name }}</span>
                </a-menu-item>
              </a-menu-item-group>

              <!-- Private -->
              <a-menu-item-group v-if="projectLayers.private.length > 0" title="Private">
                <a-menu-item
                  v-for="layer in projectLayers.private"
                  :key="`layer-${layer.id}`"
                >
                  <ExperimentOutlined />
                  <span>{{ layer.name }}</span>
                </a-menu-item>
              </a-menu-item-group>

              <!-- Empty State -->
              <a-menu-item v-if="projectLayers.release.length === 0 && projectLayers.layer.length === 0 && projectLayers.private.length === 0" disabled>
                <span class="text-muted">등록된 Layer 없음</span>
              </a-menu-item>
            </a-sub-menu>
          </a-sub-menu>
        </template>
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
  background: var(--color-bg-primary);
}

/* Sidebar - Backstage Style */
.app-sider {
  background: #1a1a1f !important;
  border-right: 1px solid var(--color-border);
  z-index: 10;
}

.logo-container {
  height: 56px;
  display: flex;
  align-items: center;
  padding: 0 var(--spacing-md);
  border-bottom: 1px solid var(--color-border);
  background: transparent;
  cursor: pointer;
  transition: background var(--transition-fast);
}

.logo-container:hover {
  background: rgba(255, 255, 255, 0.04);
}

.logo-image {
  width: 28px;
  height: 28px;
  margin-right: var(--spacing-sm);
  border-radius: 4px;
  background: transparent;
}

.logo-text {
  font-size: var(--font-size-lg);
  font-weight: var(--font-weight-bold);
  color: var(--color-accent-primary);
  letter-spacing: -0.02em;
}

.app-menu {
  background: transparent !important;
  border-right: none;
  padding: var(--spacing-sm) 0;
}

.app-menu :deep(.ant-menu-item),
.app-menu :deep(.ant-menu-submenu-title) {
  margin: 2px var(--spacing-sm);
  height: 36px;
  line-height: 36px;
  width: calc(100% - var(--spacing-md));
  border-radius: var(--radius-sm);
  color: var(--color-text-secondary);
  font-size: var(--font-size-sm);
}

.app-menu :deep(.ant-menu-item-selected) {
  background: rgba(0, 212, 170, 0.15) !important;
  color: var(--color-accent-primary) !important;
  font-weight: var(--font-weight-medium);
}

.app-menu :deep(.ant-menu-item-selected)::before {
  content: '';
  position: absolute;
  left: 0;
  top: 50%;
  transform: translateY(-50%);
  width: 3px;
  height: 20px;
  background: var(--color-accent-primary);
  border-radius: 0 2px 2px 0;
}

.app-menu :deep(.ant-menu-item:hover:not(.ant-menu-item-selected)),
.app-menu :deep(.ant-menu-submenu-title:hover) {
  color: var(--color-text-primary);
  background: rgba(255, 255, 255, 0.04);
}

.app-menu :deep(.ant-menu-sub) {
  background: transparent !important;
}

.app-menu :deep(.ant-menu-item-group-title) {
  color: var(--color-text-muted);
  font-size: 11px;
  font-weight: var(--font-weight-semibold);
  text-transform: uppercase;
  letter-spacing: 0.05em;
  padding-left: 20px;
}

.app-menu :deep(.ant-menu-submenu-arrow) {
  color: var(--color-text-muted);
}

.app-menu :deep(.ant-menu-submenu-title) {
  font-weight: var(--font-weight-medium);
}

.menu-divider {
  margin: var(--spacing-sm) var(--spacing-md);
  border-color: var(--color-border);
}

.back-button {
  display: flex;
  align-items: center;
  gap: var(--spacing-sm);
  padding: var(--spacing-xs) var(--spacing-md);
  margin: var(--spacing-xs) var(--spacing-sm);
  color: var(--color-text-muted);
  font-size: var(--font-size-xs);
  cursor: pointer;
  border-radius: var(--radius-sm);
  transition: all var(--transition-fast);
}

.back-button:hover {
  color: var(--color-text-primary);
  background: rgba(255, 255, 255, 0.04);
}

/* Header - Backstage Style */
.app-header {
  background: #232328;
  padding: 0 var(--spacing-lg);
  height: 56px;
  display: flex;
  align-items: center;
  justify-content: space-between;
  border-bottom: 1px solid var(--color-border);
  z-index: 9;
  position: sticky;
  top: 0;
}

.header-left {
  display: flex;
  align-items: center;
  gap: var(--spacing-md);
}

.trigger {
  font-size: var(--font-size-lg);
  cursor: pointer;
  transition: color var(--transition-fast);
  color: var(--color-text-secondary);
  padding: var(--spacing-xs);
}

.trigger:hover {
  color: var(--color-accent-primary);
}

.header-breadcrumb {
  margin-left: var(--spacing-xs);
}

.header-breadcrumb :deep(.ant-breadcrumb-link),
.header-breadcrumb :deep(.ant-breadcrumb-link a) {
  color: var(--color-text-secondary);
  font-size: var(--font-size-sm);
}

.header-breadcrumb :deep(.ant-breadcrumb-link a:hover) {
  color: var(--color-accent-primary);
}

.header-breadcrumb :deep(.ant-breadcrumb-separator) {
  color: var(--color-text-muted);
}

/* Header Right */
.header-right {
  display: flex;
  align-items: center;
  gap: var(--spacing-sm);
}

.icon-btn {
  color: var(--color-text-secondary);
  font-size: var(--font-size-md);
  width: 36px;
  height: 36px;
  display: flex;
  align-items: center;
  justify-content: center;
  border-radius: var(--radius-sm);
}

.icon-btn:hover {
  color: var(--color-accent-primary);
  background: rgba(255, 255, 255, 0.04);
}

.user-profile {
  display: flex;
  align-items: center;
  gap: var(--spacing-sm);
  cursor: pointer;
  padding: 6px var(--spacing-sm);
  border-radius: var(--radius-sm);
  transition: background var(--transition-fast);
}

.user-profile:hover {
  background: rgba(255, 255, 255, 0.04);
}

.user-avatar {
  background: var(--color-accent-primary);
  color: #1a1a1f;
  font-weight: var(--font-weight-semibold);
  font-size: var(--font-size-xs);
}

.username {
  font-size: var(--font-size-sm);
  font-weight: var(--font-weight-medium);
  color: var(--color-text-primary);
}

/* Content - Backstage Style */
.app-content {
  padding: var(--spacing-lg);
  background: #1a1a1f;
  min-height: calc(100vh - 56px);
  overflow-y: auto;
}

.text-muted {
  color: var(--color-text-muted) !important;
  font-size: var(--font-size-xs);
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
