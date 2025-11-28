<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import {
  CheckCircleOutlined,
  BuildOutlined,
  RocketOutlined,
  SyncOutlined,
  RobotOutlined,
} from '@ant-design/icons-vue'
import { PageHeader, StatCard, RecentBuildsList, ActiveProjectsList } from '@/components'
import { useBuildStore } from '@/stores/build'
import { useProjectStore } from '@/stores/project'
import { statsApi } from '@/api/stats'
import type { DashboardStats } from '@/types'

const router = useRouter()
const buildStore = useBuildStore()
const projectStore = useProjectStore()

const dashboardStats = ref<DashboardStats | null>(null)
const loading = ref(false)

onMounted(async () => {
  loading.value = true
  try {
    const [statsResponse] = await Promise.all([
      statsApi.getDashboardStats(),
      buildStore.fetchBuilds(),
      projectStore.fetchProjects()
    ])
    dashboardStats.value = statsResponse.data
  } catch (e) {
    console.error('Failed to load dashboard data:', e)
  } finally {
    loading.value = false
  }
})

const stats = computed(() => {
  const data = dashboardStats.value
  return [
    {
      title: 'Success Rate',
      value: data ? `${data.successRate}%` : '-',
      trend: '+2.1%',
      status: 'success' as const,
      icon: CheckCircleOutlined
    },
    {
      title: 'Total Builds',
      value: data ? data.totalBuilds.toLocaleString() : '-',
      trend: '+124',
      status: 'info' as const,
      icon: BuildOutlined
    },
    {
      title: 'Active Projects',
      value: data ? data.activeProjects.toString() : '-',
      trend: 'Stable',
      status: 'warning' as const,
      icon: RocketOutlined
    },
    {
      title: 'Running Builds',
      value: data ? data.runningBuilds.toString() : '-',
      trend: 'Live',
      status: 'danger' as const,
      icon: SyncOutlined
    },
  ]
})

const recentBuilds = computed(() => buildStore.builds.slice(0, 5))
const activeProjects = computed(() => projectStore.projects.slice(0, 4))

function getProjectName(projectId: string) {
  const project = projectStore.projects.find(p => p.id === projectId)
  return project?.name || projectId
}
</script>

<template>
  <div class="home-view">
    <!-- Welcome Section -->
    <div class="welcome-section">
      <div class="welcome-text">
        <h1>Welcome back, Admin</h1>
        <p>Here's what's happening with your projects today.</p>
      </div>
      <div class="ai-summary">
        <RobotOutlined class="ai-icon" />
        <span>AI Summary: System health is optimal. {{ dashboardStats?.runningBuilds || 0 }} builds currently running.</span>
      </div>
    </div>

    <a-spin :spinning="loading">
      <!-- Stats Cards -->
      <div class="stats-grid">
        <StatCard
          v-for="stat in stats"
          :key="stat.title"
          :title="stat.title"
          :value="stat.value"
          :trend="stat.trend"
          :status="stat.status"
          :icon="stat.icon"
        />
      </div>

      <!-- Main Content Grid -->
      <div class="main-content-grid">
        <RecentBuildsList
          :builds="recentBuilds"
          :get-project-name="getProjectName"
          @view-all="router.push('/')"
          @view-build="(id) => {
            const build = buildStore.builds.find(b => b.id === id)
            if (build) {
              router.push(`/projects/${build.projectId}?tab=builds`)
            }
          }"
        />
        <ActiveProjectsList
          :projects="activeProjects"
          @view-all="router.push('/')"
          @view-project="(id) => router.push(`/projects/${id}`)"
        />
      </div>
    </a-spin>
  </div>
</template>

<style scoped>
.home-view {
  display: flex;
  flex-direction: column;
  gap: var(--spacing-xl);
}

.welcome-section {
  display: flex;
  justify-content: space-between;
  align-items: flex-end;
  flex-wrap: wrap;
  gap: var(--spacing-md);
}

.welcome-text h1 {
  font-size: var(--font-size-5xl);
  font-weight: var(--font-weight-bold);
  margin-bottom: var(--spacing-sm);
  color: var(--color-text-primary);
}

.welcome-text p {
  color: var(--color-text-secondary);
  margin: 0;
  font-size: var(--font-size-lg);
}

.ai-summary {
  background: var(--color-bg-indigo);
  border: 1px solid var(--color-border-indigo);
  padding: var(--spacing-sm) var(--spacing-md);
  border-radius: var(--radius-full);
  display: flex;
  align-items: center;
  gap: var(--spacing-sm);
  color: var(--color-accent-primary);
  font-size: var(--font-size-md);
  font-weight: var(--font-weight-medium);
}

.stats-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(240px, 1fr));
  gap: var(--spacing-lg);
}

.main-content-grid {
  display: grid;
  grid-template-columns: 2fr 1fr;
  gap: var(--spacing-lg);
}

@media (max-width: 1024px) {
  .main-content-grid {
    grid-template-columns: 1fr;
  }
}
</style>
