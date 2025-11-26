<script setup lang="ts">
import { ref, computed, onMounted, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import {
  CheckCircleOutlined,
  CloseCircleOutlined,
  SyncOutlined,
  ClockCircleOutlined,
  StopOutlined,
  BranchesOutlined,
  UserOutlined,
  SearchOutlined,
  FilterOutlined,
} from '@ant-design/icons-vue'
import { useBuildStore } from '@/stores/build'
import { useProjectStore } from '@/stores/project'
import { useProjectGroupStore } from '@/stores/projectGroup'
import type { Build, BuildStatus } from '@/types'

const route = useRoute()
const router = useRouter()
const buildStore = useBuildStore()
const projectStore = useProjectStore()
const projectGroupStore = useProjectGroupStore()

const selectedProjectId = ref<string | null>(null)
const selectedStatus = ref<BuildStatus | null>(null)
const searchText = ref('')

onMounted(async () => {
  await projectGroupStore.fetchProjectGroups()
  await projectStore.fetchProjects()
  await buildStore.fetchBuilds()

  // Check for projectId query param
  if (route.query.projectId) {
    selectedProjectId.value = route.query.projectId as string
  }
})

watch(
  () => route.query.projectId,
  (newVal) => {
    selectedProjectId.value = newVal ? (newVal as string) : null
  }
)

const filteredBuilds = computed(() => {
  let builds = buildStore.builds

  if (selectedProjectId.value) {
    builds = builds.filter((b) => b.projectId === selectedProjectId.value)
  }

  if (selectedStatus.value) {
    builds = builds.filter((b) => b.status === selectedStatus.value)
  }

  if (searchText.value) {
    const search = searchText.value.toLowerCase()
    builds = builds.filter(
      (b) =>
        b.branch.toLowerCase().includes(search) ||
        b.commitHash.toLowerCase().includes(search) ||
        b.triggeredBy.toLowerCase().includes(search) ||
        getProjectName(b.projectId).toLowerCase().includes(search)
    )
  }

  // Sort by startedAt descending
  return [...builds].sort(
    (a, b) => new Date(b.startedAt).getTime() - new Date(a.startedAt).getTime()
  )
})

const statusOptions = [
  { value: null, label: 'All Status' },
  { value: 'success', label: 'Success' },
  { value: 'failed', label: 'Failed' },
  { value: 'running', label: 'Running' },
  { value: 'pending', label: 'Pending' },
  { value: 'cancelled', label: 'Cancelled' },
]

const projectOptions = computed(() => {
  const options: { value: string | null; label: string }[] = [
    { value: null, label: 'All Projects' },
  ]
  projectStore.projects.forEach((p) => {
    options.push({ value: p.id, label: p.name })
  })
  return options
})

function getProjectName(projectId: string) {
  const project = projectStore.projects.find((p) => p.id === projectId)
  return project?.name || projectId
}

function getGroupName(projectId: string) {
  const project = projectStore.projects.find((p) => p.id === projectId)
  if (!project) return ''
  const group = projectGroupStore.projectGroups.find((g) => g.id === project.groupId)
  return group?.name || ''
}

function getStatusIcon(status: BuildStatus) {
  switch (status) {
    case 'success':
      return CheckCircleOutlined
    case 'failed':
      return CloseCircleOutlined
    case 'running':
      return SyncOutlined
    case 'pending':
      return ClockCircleOutlined
    case 'cancelled':
      return StopOutlined
  }
}

function getStatusColor(status: BuildStatus) {
  switch (status) {
    case 'success':
      return 'var(--accent-success)'
    case 'failed':
      return 'var(--accent-danger)'
    case 'running':
      return 'var(--accent-primary)'
    case 'pending':
      return 'var(--accent-warning)'
    case 'cancelled':
      return 'var(--text-muted)'
  }
}

function formatDuration(seconds: number | null) {
  if (seconds === null) return '-'
  const mins = Math.floor(seconds / 60)
  const secs = seconds % 60
  return `${mins}m ${secs}s`
}

function formatDate(dateStr: string) {
  const date = new Date(dateStr)
  return date.toLocaleString()
}

function goToBuildDetail(buildId: string) {
  router.push(`/build/${buildId}`)
}

function clearFilters() {
  selectedProjectId.value = null
  selectedStatus.value = null
  searchText.value = ''
  router.replace({ query: {} })
}

const buildColumns = [
  {
    title: 'Build',
    key: 'build',
    width: 120,
  },
  {
    title: 'Status',
    key: 'status',
    width: 120,
  },
  {
    title: 'Project',
    key: 'project',
    width: 200,
  },
  {
    title: 'Branch',
    key: 'branch',
    width: 180,
  },
  {
    title: 'Triggered By',
    key: 'triggeredBy',
    width: 140,
  },
  {
    title: 'Started',
    key: 'startedAt',
    width: 180,
  },
  {
    title: 'Duration',
    key: 'duration',
    width: 100,
  },
]
</script>

<template>
  <div class="build-list-page">
    <div class="page-header">
      <div class="header-content">
        <h1>Builds</h1>
        <p class="page-description">CI/CD 빌드 현황 및 이력</p>
      </div>
      <a-button type="primary" @click="router.push('/build/new')">
        New Build
      </a-button>
    </div>

    <!-- Filters -->
    <div class="filters-section">
      <a-input-search
        v-model:value="searchText"
        placeholder="Search builds..."
        style="width: 280px"
        allow-clear
      >
        <template #prefix>
          <search-outlined />
        </template>
      </a-input-search>

      <a-select
        v-model:value="selectedProjectId"
        :options="projectOptions"
        placeholder="Filter by project"
        style="width: 240px"
        allow-clear
      >
        <template #prefix>
          <filter-outlined />
        </template>
      </a-select>

      <a-select
        v-model:value="selectedStatus"
        :options="statusOptions"
        placeholder="Filter by status"
        style="width: 160px"
        allow-clear
      />

      <a-button
        v-if="selectedProjectId || selectedStatus || searchText"
        @click="clearFilters"
      >
        Clear Filters
      </a-button>

      <div class="filter-info">
        <span>{{ filteredBuilds.length }} builds</span>
      </div>
    </div>

    <!-- Build Table -->
    <div class="builds-table-container">
      <a-spin :spinning="buildStore.loading">
        <a-table
          :columns="buildColumns"
          :data-source="filteredBuilds"
          :pagination="{ pageSize: 15, showSizeChanger: true }"
          :row-key="(record: Build) => record.id"
          :row-class-name="() => 'build-row'"
          @row-click="(record: Build) => goToBuildDetail(record.id)"
        >
          <template #bodyCell="{ column, record }">
            <template v-if="column.key === 'build'">
              <div class="build-number">
                <span class="number">#{{ record.buildNumber }}</span>
                <span class="commit">{{ record.commitHash.slice(0, 7) }}</span>
              </div>
            </template>

            <template v-else-if="column.key === 'status'">
              <div class="status-cell">
                <component
                  :is="getStatusIcon(record.status)"
                  :style="{ color: getStatusColor(record.status) }"
                  :spin="record.status === 'running'"
                />
                <span
                  class="status-text"
                  :style="{ color: getStatusColor(record.status) }"
                >
                  {{ record.status }}
                </span>
              </div>
            </template>

            <template v-else-if="column.key === 'project'">
              <div class="project-cell">
                <span class="project-name">{{ getProjectName(record.projectId) }}</span>
                <span class="group-name">{{ getGroupName(record.projectId) }}</span>
              </div>
            </template>

            <template v-else-if="column.key === 'branch'">
              <div class="branch-cell">
                <branches-outlined />
                <span>{{ record.branch }}</span>
              </div>
            </template>

            <template v-else-if="column.key === 'triggeredBy'">
              <div class="user-cell">
                <user-outlined />
                <span>{{ record.triggeredBy }}</span>
              </div>
            </template>

            <template v-else-if="column.key === 'startedAt'">
              {{ formatDate(record.startedAt) }}
            </template>

            <template v-else-if="column.key === 'duration'">
              {{ formatDuration(record.duration) }}
            </template>
          </template>
        </a-table>
      </a-spin>
    </div>
  </div>
</template>

<style scoped>
.build-list-page {
  display: flex;
  flex-direction: column;
  gap: 24px;
}

.page-header {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
}

.page-header h1 {
  margin: 0;
  font-size: 28px;
  font-weight: 700;
  background: linear-gradient(to right, #fff, #94a3b8);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
}

.page-description {
  color: var(--text-muted);
  margin: 4px 0 0;
}

/* Filters */
.filters-section {
  display: flex;
  align-items: center;
  gap: 16px;
  flex-wrap: wrap;
}

.filter-info {
  margin-left: auto;
  color: var(--text-muted);
  font-size: 14px;
}

/* Table Container */
.builds-table-container {
  background: var(--bg-secondary);
  border: 1px solid rgba(255, 255, 255, 0.05);
  border-radius: 16px;
  padding: 20px;
}

.builds-table-container :deep(.ant-table) {
  background: transparent;
}

.builds-table-container :deep(.ant-table-thead > tr > th) {
  background: rgba(255, 255, 255, 0.02);
  border-bottom: 1px solid rgba(255, 255, 255, 0.05);
  color: var(--text-secondary);
  font-weight: 600;
}

.builds-table-container :deep(.ant-table-tbody > tr > td) {
  border-bottom: 1px solid rgba(255, 255, 255, 0.03);
}

.builds-table-container :deep(.ant-table-tbody > tr:hover > td) {
  background: rgba(255, 255, 255, 0.03);
}

.builds-table-container :deep(.build-row) {
  cursor: pointer;
}

/* Build Number Cell */
.build-number {
  display: flex;
  flex-direction: column;
}

.build-number .number {
  font-weight: 600;
  color: var(--text-primary);
  font-family: monospace;
}

.build-number .commit {
  font-size: 12px;
  color: var(--text-muted);
  font-family: monospace;
}

/* Status Cell */
.status-cell {
  display: flex;
  align-items: center;
  gap: 8px;
}

.status-text {
  text-transform: capitalize;
  font-weight: 500;
}

/* Project Cell */
.project-cell {
  display: flex;
  flex-direction: column;
}

.project-cell .project-name {
  font-weight: 500;
  color: var(--text-primary);
}

.project-cell .group-name {
  font-size: 12px;
  color: var(--text-muted);
}

/* Branch Cell */
.branch-cell {
  display: flex;
  align-items: center;
  gap: 8px;
  color: var(--text-secondary);
}

/* User Cell */
.user-cell {
  display: flex;
  align-items: center;
  gap: 8px;
  color: var(--text-secondary);
}
</style>
