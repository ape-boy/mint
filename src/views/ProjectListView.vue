<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import {
  SearchOutlined,
  FilterOutlined,
  ReloadOutlined,
  HistoryOutlined,
} from '@ant-design/icons-vue'
import { PageHeader, StatusBadge, EmptyState } from '@/components'
import { useTaskGroupStore } from '@/stores/projectGroup'
import { useProjectStore } from '@/stores/project'
import { useBuildStore } from '@/stores/build'
import type { Project, Build, TaskGroup } from '@/types'

const router = useRouter()
const taskGroupStore = useTaskGroupStore()
const projectStore = useProjectStore()
const buildStore = useBuildStore()

// Filter states
const filters = ref({
  groupId: undefined as string | undefined,
  oem: undefined as string | undefined,
  feature: undefined as string | undefined,
  search: '',
})

const showFilters = ref(false)

onMounted(async () => {
  await Promise.all([
    taskGroupStore.fetchTaskGroups(),
    projectStore.fetchProjects(),
    buildStore.fetchBuilds()
  ])
})

// Computed options for filters
const oemOptions = computed(() => {
  const oems = new Set(projectStore.projects.map(p => p.oem))
  return Array.from(oems).map(oem => ({ label: oem, value: oem }))
})

const featureOptions = computed(() => {
  const features = new Set(projectStore.projects.map(p => p.feature))
  return Array.from(features).map(f => ({ label: f, value: f }))
})

const taskGroupOptions = computed(() => {
  return taskGroupStore.taskGroups.map(g => ({ label: g.name, value: g.id }))
})

// Get latest build for a project
function getLatestBuild(projectId: string): Build | undefined {
  const projectBuilds = buildStore.builds
    .filter(b => b.projectId === projectId)
    .sort((a, b) => new Date(b.startedAt).getTime() - new Date(a.startedAt).getTime())
  return projectBuilds[0]
}

// Get task group name
function getGroupName(groupId: string): string {
  const group = taskGroupStore.taskGroups.find(g => g.id === groupId)
  return group?.name || '-'
}

// Filtered projects with latest build info
const tableData = computed(() => {
  let projects = projectStore.projects

  if (filters.value.groupId) {
    projects = projects.filter(p => p.groupId === filters.value.groupId)
  }
  if (filters.value.oem) {
    projects = projects.filter(p => p.oem === filters.value.oem)
  }
  if (filters.value.feature) {
    projects = projects.filter(p => p.feature === filters.value.feature)
  }
  if (filters.value.search) {
    const search = filters.value.search.toLowerCase()
    projects = projects.filter(p =>
      p.name.toLowerCase().includes(search) ||
      p.taskCode?.toLowerCase().includes(search) ||
      p.tl?.name?.toLowerCase().includes(search) ||
      p.oem?.toLowerCase().includes(search)
    )
  }

  return projects.map(project => {
    const latestBuild = getLatestBuild(project.id)
    return {
      ...project,
      groupTitle: getGroupName(project.groupId),
      latestBuild,
      result: latestBuild?.status || 'pending',
      date: latestBuild?.finishedAt || latestBuild?.startedAt || project.updatedAt,
      fwName: latestBuild?.fwName || '-',
    }
  })
})

function clearFilters() {
  filters.value = {
    groupId: undefined,
    oem: undefined,
    feature: undefined,
    search: '',
  }
}

function goToProject(projectId: string) {
  router.push(`/projects/${projectId}`)
}

function goToHistory(projectId: string) {
  router.push(`/projects/${projectId}?tab=history`)
}

function getStatusColor(status: string) {
  const colors: Record<string, string> = {
    success: 'var(--color-status-success)',
    failed: 'var(--color-status-error)',
    running: 'var(--color-status-warning)',
    pending: 'var(--color-text-muted)',
    cancelled: 'var(--color-text-muted)',
  }
  return colors[status] || colors.pending
}

function getKpiColor(score: number | undefined) {
  if (!score) return 'var(--color-text-muted)'
  if (score >= 90) return 'var(--color-status-success)'
  if (score >= 70) return 'var(--color-status-warning)'
  return 'var(--color-status-error)'
}

const tableColumns = [
  { title: 'P/J', dataIndex: 'groupTitle', key: 'groupTitle', width: 100 },
  { title: 'OEM', dataIndex: 'oem', key: 'oem', width: 100 },
  { title: 'Feature', dataIndex: 'feature', key: 'feature', width: 120 },
  { title: '과제 이름', dataIndex: 'name', key: 'name', ellipsis: true },
  { title: 'TL', key: 'tl', width: 100 },
  { title: 'Milestone', key: 'milestone', width: 100 },
  { title: 'KPI', key: 'kpiScore', width: 80 },
  { title: 'Result', key: 'result', width: 100 },
  { title: 'Date', key: 'date', width: 120 },
  { title: 'FW Name', dataIndex: 'fwName', key: 'fwName', width: 180, ellipsis: true },
  { title: 'History', key: 'history', width: 80, align: 'center' as const },
]
</script>

<template>
  <div class="project-list-view">
    <PageHeader title="과제 현황" description="전사 프로젝트 현황 조회 및 관리">
      <template #extra>
        <a-space>
          <a-button @click="showFilters = !showFilters">
            <FilterOutlined />
            필터
          </a-button>
          <a-button @click="clearFilters">
            <ReloadOutlined />
            초기화
          </a-button>
        </a-space>
      </template>
    </PageHeader>

    <!-- Filter Panel -->
    <div v-if="showFilters" class="filter-panel">
      <a-row :gutter="16">
        <a-col :span="6">
          <div class="filter-item">
            <label>P/J (과제 그룹)</label>
            <a-select
              v-model:value="filters.groupId"
              placeholder="전체"
              :options="taskGroupOptions"
              allow-clear
              style="width: 100%"
            />
          </div>
        </a-col>
        <a-col :span="6">
          <div class="filter-item">
            <label>OEM</label>
            <a-select
              v-model:value="filters.oem"
              placeholder="전체"
              :options="oemOptions"
              allow-clear
              style="width: 100%"
            />
          </div>
        </a-col>
        <a-col :span="6">
          <div class="filter-item">
            <label>Feature</label>
            <a-select
              v-model:value="filters.feature"
              placeholder="전체"
              :options="featureOptions"
              allow-clear
              style="width: 100%"
            />
          </div>
        </a-col>
        <a-col :span="6">
          <div class="filter-item">
            <label>검색</label>
            <a-input-search
              v-model:value="filters.search"
              placeholder="과제 이름, 코드, TL..."
              allow-clear
            >
              <template #prefix><SearchOutlined /></template>
            </a-input-search>
          </div>
        </a-col>
      </a-row>
    </div>

    <!-- Quick Search (always visible) -->
    <div class="quick-search">
      <a-input-search
        v-model:value="filters.search"
        placeholder="과제 이름, 코드, TL로 검색..."
        style="width: 400px"
        allow-clear
        size="large"
      >
        <template #prefix><SearchOutlined /></template>
      </a-input-search>
      <span class="result-count">{{ tableData.length }} 과제</span>
    </div>

    <!-- Data Table -->
    <a-spin :spinning="projectStore.loading || buildStore.loading">
      <a-table
        :columns="tableColumns"
        :data-source="tableData"
        :pagination="{ pageSize: 20, showSizeChanger: true, showTotal: (total: number) => `총 ${total}개` }"
        row-key="id"
        class="data-table data-table--clickable"
        :scroll="{ x: 1200 }"
      >
        <template #bodyCell="{ column, record }">
          <template v-if="column.key === 'name'">
            <a class="project-link" @click="goToProject(record.id)">
              <span class="task-code">{{ record.taskCode }}</span>
              <span class="project-name">{{ record.name }}</span>
            </a>
          </template>

          <template v-else-if="column.key === 'tl'">
            <span v-if="record.tl">{{ record.tl.name }}</span>
            <span v-else class="text-muted">-</span>
          </template>

          <template v-else-if="column.key === 'milestone'">
            <a-tag :color="record.currentMilestone ? 'blue' : 'default'">
              {{ record.currentMilestone || '-' }}
            </a-tag>
          </template>

          <template v-else-if="column.key === 'kpiScore'">
            <span
              v-if="record.kpiScore"
              class="kpi-score"
              :style="{ color: getKpiColor(record.kpiScore) }"
            >
              {{ record.kpiScore }}
            </span>
            <span v-else class="text-muted">-</span>
          </template>

          <template v-else-if="column.key === 'result'">
            <div class="result-cell">
              <span
                class="result-dot"
                :style="{ backgroundColor: getStatusColor(record.result) }"
              />
              <span class="result-text">{{ record.result }}</span>
            </div>
          </template>

          <template v-else-if="column.key === 'date'">
            <span class="date-text">
              {{ record.date ? new Date(record.date).toLocaleDateString('ko-KR') : '-' }}
            </span>
          </template>

          <template v-else-if="column.key === 'history'">
            <a-tooltip title="변경 이력">
              <a-button type="text" size="small" @click.stop="goToHistory(record.id)">
                <HistoryOutlined />
              </a-button>
            </a-tooltip>
          </template>
        </template>
      </a-table>

      <EmptyState
        v-if="tableData.length === 0 && !projectStore.loading"
        title="과제가 없습니다"
        description="검색 조건에 맞는 과제가 없습니다"
      >
        <a-button type="primary" @click="clearFilters">필터 초기화</a-button>
      </EmptyState>
    </a-spin>
  </div>
</template>

<style scoped>
.project-list-view {
  display: flex;
  flex-direction: column;
  gap: var(--spacing-lg);
}

.filter-panel {
  background: var(--color-bg-secondary);
  border-radius: var(--radius-md);
  padding: var(--spacing-lg);
  border: 1px solid var(--color-border-light);
}

.filter-item {
  display: flex;
  flex-direction: column;
  gap: var(--spacing-xs);
}

.filter-item label {
  font-size: var(--font-size-sm);
  color: var(--color-text-secondary);
  font-weight: var(--font-weight-medium);
}

.quick-search {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: var(--spacing-lg);
}

.result-count {
  color: var(--color-text-muted);
  font-size: var(--font-size-md);
}

.project-link {
  display: flex;
  flex-direction: column;
  gap: 2px;
  cursor: pointer;
}

.task-code {
  font-size: var(--font-size-xs);
  color: var(--color-text-muted);
}

.project-name {
  color: var(--color-accent-primary);
  font-weight: var(--font-weight-medium);
}

.project-link:hover .project-name {
  text-decoration: underline;
}

.kpi-score {
  font-weight: var(--font-weight-bold);
  font-size: var(--font-size-md);
}

.result-cell {
  display: flex;
  align-items: center;
  gap: var(--spacing-xs);
}

.result-dot {
  width: 8px;
  height: 8px;
  border-radius: 50%;
}

.result-text {
  font-size: var(--font-size-sm);
  text-transform: capitalize;
}

.date-text {
  font-size: var(--font-size-sm);
  color: var(--color-text-secondary);
}

.text-muted {
  color: var(--color-text-muted);
}

:deep(.ant-table) {
  background: var(--color-bg-secondary);
  border-radius: var(--radius-md);
}

:deep(.ant-table-thead > tr > th) {
  background: var(--color-bg-tertiary) !important;
  color: var(--color-text-secondary);
  font-weight: var(--font-weight-semibold);
  border-bottom: 1px solid var(--color-border-light);
}

:deep(.ant-table-tbody > tr > td) {
  border-bottom: 1px solid var(--color-border-light);
}

:deep(.ant-table-tbody > tr:hover > td) {
  background: var(--color-bg-tertiary) !important;
}

:deep(.ant-table-tbody > tr) {
  cursor: pointer;
}
</style>
