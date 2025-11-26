<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import {
  CheckCircleOutlined,
  CloseCircleOutlined,
  SyncOutlined,
  WarningOutlined,
  ArrowRightOutlined,
  RobotOutlined,
  BuildOutlined,
  RocketOutlined,
  BugOutlined,
  InfoCircleOutlined,
  ClockCircleOutlined,
  LoadingOutlined,
} from '@ant-design/icons-vue'
import { useBuildStore } from '@/stores/build'
import { useProjectStore } from '@/stores/project'
import { statsApi } from '@/api/stats'
import type { Build, DashboardStats } from '@/types'

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
  if (!dashboardStats.value) {
    return [
      { title: 'Success Rate', value: '-', trend: '-', status: 'info', icon: CheckCircleOutlined },
      { title: 'Total Builds', value: '-', trend: '-', status: 'info', icon: BuildOutlined },
      { title: 'Active Projects', value: '-', trend: '-', status: 'info', icon: RocketOutlined },
      { title: 'Running Builds', value: '-', trend: '-', status: 'info', icon: SyncOutlined },
    ]
  }

  return [
    {
      title: 'Success Rate',
      value: `${dashboardStats.value.successRate}%`,
      trend: '+2.1%',
      status: 'success',
      icon: CheckCircleOutlined
    },
    {
      title: 'Total Builds',
      value: dashboardStats.value.totalBuilds.toLocaleString(),
      trend: '+124',
      status: 'info',
      icon: BuildOutlined
    },
    {
      title: 'Active Projects',
      value: dashboardStats.value.activeProjects.toString(),
      trend: 'Stable',
      status: 'warning',
      icon: RocketOutlined
    },
    {
      title: 'Running Builds',
      value: dashboardStats.value.runningBuilds.toString(),
      trend: 'Live',
      status: 'danger',
      icon: SyncOutlined
    },
  ]
})

const recentBuilds = computed(() => {
  return buildStore.builds.slice(0, 5)
})

function getBuildStatusIcon(status: Build['status']) {
  switch (status) {
    case 'success':
      return CheckCircleOutlined
    case 'failed':
      return CloseCircleOutlined
    case 'running':
      return LoadingOutlined
    case 'pending':
      return ClockCircleOutlined
    default:
      return ClockCircleOutlined
  }
}

function getBuildStatusColor(status: Build['status']) {
  switch (status) {
    case 'success':
      return 'var(--accent-success)'
    case 'failed':
      return 'var(--accent-danger)'
    case 'running':
      return 'var(--accent-primary)'
    case 'pending':
      return 'var(--text-muted)'
    default:
      return 'var(--text-muted)'
  }
}

function getProjectName(projectId: string) {
  const project = projectStore.projects.find(p => p.id === projectId)
  return project?.name || projectId
}

function formatTimeAgo(dateStr: string) {
  const date = new Date(dateStr)
  const now = new Date()
  const diffMs = now.getTime() - date.getTime()
  const diffHours = Math.floor(diffMs / (1000 * 60 * 60))
  const diffDays = Math.floor(diffHours / 24)

  if (diffDays > 0) return `${diffDays}d ago`
  if (diffHours > 0) return `${diffHours}h ago`
  return 'Just now'
}

function goToBuild(buildId: string) {
  router.push(`/build/${buildId}`)
}
</script>

<template>
  <div class="home-container">
    <!-- Welcome Section -->
    <div class="welcome-section">
      <div class="welcome-text">
        <h1>Welcome back, Admin</h1>
        <p>Here's what's happening with your projects today.</p>
      </div>
      <div class="ai-summary">
        <robot-outlined class="ai-icon" />
        <span>AI Summary: System health is optimal. {{ dashboardStats?.runningBuilds || 0 }} builds currently running.</span>
      </div>
    </div>

    <a-spin :spinning="loading">
      <!-- Stats Cards -->
      <div class="stats-grid">
        <div v-for="stat in stats" :key="stat.title" class="stat-card glass-panel">
          <div class="stat-icon" :class="stat.status">
            <component :is="stat.icon" />
          </div>
          <div class="stat-info">
            <span class="stat-title">{{ stat.title }}</span>
            <div class="stat-value-wrapper">
              <span class="stat-value">{{ stat.value }}</span>
              <span class="stat-trend" :class="{ positive: stat.trend.startsWith('+'), negative: stat.trend.startsWith('-') }">
                {{ stat.trend }}
              </span>
            </div>
          </div>
        </div>
      </div>

      <!-- Main Content Grid -->
      <div class="main-content-grid">
        <!-- Recent Builds -->
        <div class="recent-builds glass-panel">
          <div class="section-header">
            <h3>Recent Builds</h3>
            <a-button type="link" @click="router.push('/build')">View All</a-button>
          </div>
          <div class="build-list" v-if="recentBuilds.length > 0">
            <div
              v-for="build in recentBuilds"
              :key="build.id"
              class="build-item"
              @click="goToBuild(build.id)"
            >
              <div class="build-status">
                <component
                  :is="getBuildStatusIcon(build.status)"
                  :style="{ color: getBuildStatusColor(build.status) }"
                />
              </div>
              <div class="build-details">
                <span class="build-name">{{ getProjectName(build.projectId) }} #{{ build.buildNumber }}</span>
                <span class="build-meta">{{ build.branch }} • {{ formatTimeAgo(build.startedAt) }} • {{ build.triggeredBy }}</span>
              </div>
              <div class="build-action">
                <arrow-right-outlined />
              </div>
            </div>
          </div>
          <a-empty v-else description="No recent builds" />
        </div>

        <!-- Project Summary -->
        <div class="ai-insights glass-panel">
          <div class="section-header">
            <h3><rocket-outlined /> Active Projects</h3>
          </div>
          <div class="insights-list" v-if="projectStore.projects.length > 0">
            <div
              v-for="project in projectStore.projects.slice(0, 4)"
              :key="project.id"
              class="insight-item project-item"
              @click="router.push(`/projects/${project.id}`)"
            >
              <div class="project-icon">
                <span class="project-initial">{{ project.name.charAt(0) }}</span>
              </div>
              <div class="insight-content">
                <p class="project-name">{{ project.name }}</p>
                <span class="project-meta">{{ project.oem }} • {{ project.tl?.name }}</span>
              </div>
            </div>
          </div>
          <a-empty v-else description="No projects" />
          <div class="ai-query">
            <a-button type="primary" block @click="router.push('/')">
              View All Projects
            </a-button>
          </div>
        </div>
      </div>
    </a-spin>
  </div>
</template>

<style scoped>
.home-container {
  display: flex;
  flex-direction: column;
  gap: 32px;
}

.welcome-section {
  display: flex;
  justify-content: space-between;
  align-items: flex-end;
  flex-wrap: wrap;
  gap: 16px;
}

.welcome-text h1 {
  font-size: 32px;
  font-weight: 700;
  margin-bottom: 8px;
  color: var(--text-primary);
}

.welcome-text p {
  color: var(--text-secondary);
  margin: 0;
  font-size: 16px;
}

.ai-summary {
  background: var(--bg-indigo-light);
  border: 1px solid var(--border-indigo-light);
  padding: 8px 16px;
  border-radius: 20px;
  display: flex;
  align-items: center;
  gap: 8px;
  color: var(--accent-primary);
  font-size: 14px;
  font-weight: 500;
}

/* Stats Grid */
.stats-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(240px, 1fr));
  gap: 24px;
  margin-bottom: 24px;
}

.stat-card {
  padding: 24px;
  display: flex;
  align-items: center;
  gap: 20px;
  transition: transform 0.2s, box-shadow 0.2s;
  background: var(--bg-secondary);
  border-radius: 16px;
  border: 1px solid rgba(255, 255, 255, 0.05);
}

.stat-card:hover {
  transform: translateY(-5px);
  box-shadow: var(--shadow-md);
}

.stat-icon {
  width: 56px;
  height: 56px;
  border-radius: 16px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 24px;
}

.stat-icon.success { background: rgba(34, 197, 94, 0.15); color: #22c55e; }
.stat-icon.info { background: rgba(59, 130, 246, 0.15); color: #3b82f6; }
.stat-icon.warning { background: rgba(234, 179, 8, 0.15); color: #eab308; }
.stat-icon.danger { background: rgba(239, 68, 68, 0.15); color: #ef4444; }

.stat-info {
  flex: 1;
}

.stat-title {
  color: var(--text-muted);
  font-size: 14px;
  display: block;
  margin-bottom: 4px;
  font-weight: 500;
}

.stat-value-wrapper {
  display: flex;
  align-items: baseline;
  gap: 8px;
}

.stat-value {
  font-size: 28px;
  font-weight: 700;
  color: var(--text-primary);
}

.stat-trend {
  font-size: 13px;
  font-weight: 600;
  color: var(--text-muted);
}

.stat-trend.positive { color: #22c55e; }
.stat-trend.negative { color: #ef4444; }

/* Main Content Grid */
.main-content-grid {
  display: grid;
  grid-template-columns: 2fr 1fr;
  gap: 24px;
}

@media (max-width: 1024px) {
  .main-content-grid {
    grid-template-columns: 1fr;
  }
}

.section-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}

.section-header h3 {
  margin: 0;
  font-size: 18px;
  font-weight: 600;
  color: var(--text-primary);
  display: flex;
  align-items: center;
  gap: 8px;
}

.recent-builds, .ai-insights {
  padding: 24px;
  background: var(--bg-secondary);
  border-radius: 16px;
  border: 1px solid rgba(255, 255, 255, 0.05);
}

/* Build List */
.build-list {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.build-item {
  display: flex;
  align-items: center;
  padding: 12px;
  border-radius: 8px;
  background: rgba(255, 255, 255, 0.02);
  border: 1px solid rgba(255, 255, 255, 0.05);
  transition: background 0.2s;
  cursor: pointer;
}

.build-item:hover {
  background: rgba(255, 255, 255, 0.05);
}

.build-status {
  font-size: 20px;
  margin-right: 16px;
}

.build-details {
  flex: 1;
  display: flex;
  flex-direction: column;
}

.build-name {
  font-weight: 600;
  color: var(--text-primary);
}

.build-meta {
  font-size: 12px;
  color: var(--text-muted);
}

.build-action {
  color: var(--text-muted);
}

/* Project List */
.insights-list {
  display: flex;
  flex-direction: column;
  gap: 12px;
  margin-bottom: 20px;
}

.insight-item {
  display: flex;
  gap: 12px;
  padding: 12px;
  background: rgba(255, 255, 255, 0.02);
  border: 1px solid rgba(255, 255, 255, 0.05);
  border-radius: 8px;
  cursor: pointer;
  transition: background 0.2s;
}

.insight-item:hover {
  background: rgba(255, 255, 255, 0.05);
}

.project-item {
  align-items: center;
}

.project-icon {
  width: 40px;
  height: 40px;
  border-radius: 8px;
  background: rgba(139, 92, 246, 0.15);
  display: flex;
  align-items: center;
  justify-content: center;
}

.project-initial {
  font-weight: 700;
  font-size: 16px;
  color: #a78bfa;
}

.insight-content {
  flex: 1;
  display: flex;
  flex-direction: column;
  justify-content: center;
}

.insight-content p {
  margin: 0;
  font-size: 14px;
  font-weight: 600;
  color: var(--text-primary);
}

.project-meta {
  font-size: 12px;
  color: var(--text-muted);
}

.ai-query {
  margin-top: auto;
}
</style>
