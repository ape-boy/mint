<script setup lang="ts">
import { ref, computed, watch, onMounted } from 'vue'
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
  MessageOutlined,
  DashboardOutlined,
  TeamOutlined,
  UnorderedListOutlined,
  NotificationOutlined,
  CustomerServiceOutlined,
  RocketOutlined,
  AppstoreOutlined,
  ExperimentOutlined,
  CodeOutlined,
} from '@ant-design/icons-vue'
import { useLayerStore } from '@/stores/layer'
import { useProjectStore } from '@/stores/project'
import type { Layer } from '@/types'

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
    // Map routes to menu keys
    if (path === '/') {
      selectedKeys.value = ['projects-list']
      // 과제 목록에서는 PROJECT 메뉴 열기
      if (!openKeys.value.includes('project-menu')) {
        openKeys.value = [...openKeys.value, 'project-menu']
      }
    } else if ((path as string).startsWith('/projects/')) {
      const tabValue = tab as string
      // 프로젝트 상세 페이지에서는 PROJECT 메뉴 열기
      if (!openKeys.value.includes('project-menu')) {
        openKeys.value = [...openKeys.value, 'project-menu']
      }
      if (tabValue === 'info') {
        selectedKeys.value = ['project-info']
      } else if (tabValue === 'build' && layerId) {
        selectedKeys.value = [`layer-${layerId}`]
        // Open appropriate submenu (build-menu under project-menu)
        const layer = layerStore.layers.find(l => l.id === layerId)
        if (layer) {
          if (!openKeys.value.includes('build-menu')) {
            openKeys.value = [...openKeys.value, 'build-menu']
          }
        }
      } else {
        selectedKeys.value = ['project-summary']
      }
    } else if (path === '/dashboard') {
      selectedKeys.value = ['dashboard']
    } else if (path === '/board' || (path as string).startsWith('/board/')) {
      // Board 하위 메뉴 선택
      const tabValue = tab as string
      if (tabValue === 'voc' || (path as string).includes('/voc/')) {
        selectedKeys.value = ['board-voc']
      } else if (tabValue === 'release-note' || (path as string).includes('/release-note/')) {
        selectedKeys.value = ['board-release-note']
      } else {
        selectedKeys.value = ['board-notice']
      }
      // Board 메뉴 열기
      if (!openKeys.value.includes('board-menu')) {
        openKeys.value = [...openKeys.value, 'board-menu']
      }
    } else if (path === '/settings') {
      selectedKeys.value = ['settings']
    }
  },
  { immediate: true }
)

// Breadcrumb generation based on route meta
const breadcrumbs = computed(() => {
  const crumbs = [{ name: '홈', path: '/' }]

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
    case 'project-summary':
      // Go to project detail with summary tab
      if (route.params.projectId) {
        router.push(`/projects/${route.params.projectId}?tab=summary`)
      } else {
        router.push('/')
      }
      break
    case 'project-info':
      if (route.params.projectId) {
        router.push(`/projects/${route.params.projectId}?tab=info`)
      } else {
        router.push('/')
      }
      break
    case 'dashboard':
      router.push('/dashboard')
      break
    // Board 하위 메뉴
    case 'board-notice':
      router.push('/board?tab=notice')
      break
    case 'board-voc':
      router.push('/board?tab=voc')
      break
    case 'board-release-note':
      router.push('/board?tab=release-note')
      break
    case 'settings':
      router.push('/settings')
      break
  }
}

function goHome() {
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
        <!-- PROJECT (sub-menu로 변경하여 계층 구조 표현) -->
        <a-sub-menu key="project-menu">
          <template #icon><ProjectOutlined /></template>
          <template #title>PROJECT</template>

          <!-- 과제 목록 -->
          <a-menu-item key="projects-list">
            <UnorderedListOutlined />
            <span>과제 목록</span>
          </a-menu-item>

          <!-- 프로젝트 상세 페이지에서만 표시되는 메뉴 -->
          <template v-if="isProjectDetailPage">
            <a-menu-item-group :title="currentProjectName">
              <a-menu-item key="project-summary">
                <DashboardOutlined />
                <span>Summary & Quality</span>
              </a-menu-item>
              <a-menu-item key="project-info">
                <TeamOutlined />
                <span>기본 정보</span>
              </a-menu-item>
            </a-menu-item-group>

            <!-- BUILD (PROJECT 하위의 sub-menu) -->
            <a-sub-menu key="build-menu">
              <template #icon><BuildOutlined /></template>
              <template #title>BUILD</template>

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
          </template>
        </a-sub-menu>

        <!-- 대시보드 -->
        <a-menu-item key="dashboard">
          <HomeOutlined />
          <span>대시보드</span>
        </a-menu-item>

        <!-- Board (게시판) -->
        <a-sub-menu key="board-menu">
          <template #icon><MessageOutlined /></template>
          <template #title>게시판</template>
          <a-menu-item key="board-notice">
            <NotificationOutlined />
            <span>공지사항</span>
          </a-menu-item>
          <a-menu-item key="board-voc">
            <CustomerServiceOutlined />
            <span>VOC</span>
          </a-menu-item>
          <a-menu-item key="board-release-note">
            <RocketOutlined />
            <span>Release Note</span>
          </a-menu-item>
        </a-sub-menu>

        <!-- 설정 -->
        <a-menu-item key="settings">
          <SettingOutlined />
          <span>설정</span>
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
