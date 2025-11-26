<script setup lang="ts">
import { ref, computed, onMounted, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { BranchesOutlined, UserOutlined } from '@ant-design/icons-vue'
import { PageHeader, StatusBadge, EmptyState } from '@/components'
import { useBuildStore } from '@/stores/build'
import { useProjectStore } from '@/stores/project'
import { useProjectGroupStore } from '@/stores/projectGroup'
import { useFormat } from '@/composables'
import type { Build, BuildStatus } from '@/types'

const route = useRoute()
const router = useRouter()
const buildStore = useBuildStore()
const projectStore = useProjectStore()
const projectGroupStore = useProjectGroupStore()
const { formatDuration, formatDateTime } = useFormat()

const selectedProjectId = ref<string | null>(null)
const selectedStatus = ref<BuildStatus | null>(null)
const searchText = ref('')

onMounted(async () => {
  await Promise.all([
    projectGroupStore.fetchProjectGroups(),
    projectStore.fetchProjects(),
    buildStore.fetchBuilds()
  ])

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
  { title: 'Build', key: 'build', width: 120 },
  { title: 'Status', key: 'status', width: 120 },
  { title: 'Project', key: 'project', width: 200 },
  { title: 'Branch', key: 'branch', width: 180 },
  { title: 'Triggered By', key: 'triggeredBy', width: 140 },
  { title: 'Started', key: 'startedAt', width: 180 },
  { title: 'Duration', key: 'duration', width: 100 },
]
</script>

<template>
  <div class="build-list-view">
    <PageHeader title="Builds" description="CI/CD 빌드 현황 및 이력">
      <template #actions>
        <a-button type="primary" @click="router.push('/build/new')">
          New Build
        </a-button>
      </template>
    </PageHeader>

    <!-- Filters -->
    <div class="filters-section">
      <a-input-search
        v-model:value="searchText"
        placeholder="Search builds..."
        style="width: 280px"
        allow-clear
      />

      <a-select
        v-model:value="selectedProjectId"
        :options="projectOptions"
        placeholder="Filter by project"
        style="width: 240px"
        allow-clear
      />

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
    <div class="table-container">
      <a-spin :spinning="buildStore.loading">
        <a-table
          v-if="filteredBuilds.length > 0"
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
              <StatusBadge :status="record.status" type="build" />
            </template>

            <template v-else-if="column.key === 'project'">
              <div class="project-cell">
                <span class="project-name">{{ getProjectName(record.projectId) }}</span>
                <span class="group-name">{{ getGroupName(record.projectId) }}</span>
              </div>
            </template>

            <template v-else-if="column.key === 'branch'">
              <div class="branch-cell">
                <BranchesOutlined />
                <span>{{ record.branch }}</span>
              </div>
            </template>

            <template v-else-if="column.key === 'triggeredBy'">
              <div class="user-cell">
                <UserOutlined />
                <span>{{ record.triggeredBy }}</span>
              </div>
            </template>

            <template v-else-if="column.key === 'startedAt'">
              {{ formatDateTime(record.startedAt) }}
            </template>

            <template v-else-if="column.key === 'duration'">
              {{ formatDuration(record.duration) }}
            </template>
          </template>
        </a-table>
        <EmptyState v-else-if="!buildStore.loading" title="No builds found" description="Adjust filters or trigger a new build" />
      </a-spin>
    </div>
  </div>
</template>

<style scoped>
.build-list-view {
  display: flex;
  flex-direction: column;
  gap: var(--spacing-lg);
}

.filters-section {
  display: flex;
  align-items: center;
  gap: var(--spacing-md);
  flex-wrap: wrap;
}

.filter-info {
  margin-left: auto;
  color: var(--color-text-muted);
  font-size: var(--font-size-sm);
}

.table-container {
  background: var(--color-bg-secondary);
  border: 1px solid var(--color-border-light);
  border-radius: var(--radius-lg);
  padding: var(--spacing-lg);
}

.table-container :deep(.ant-table) {
  background: transparent;
}

.table-container :deep(.ant-table-thead > tr > th) {
  background: rgba(255, 255, 255, 0.02);
  border-bottom: 1px solid var(--color-border-light);
  color: var(--color-text-secondary);
  font-weight: var(--font-weight-semibold);
}

.table-container :deep(.ant-table-tbody > tr > td) {
  border-bottom: 1px solid var(--color-border-light);
}

.table-container :deep(.ant-table-tbody > tr:hover > td) {
  background: rgba(255, 255, 255, 0.03);
}

.table-container :deep(.build-row) {
  cursor: pointer;
}

.build-number {
  display: flex;
  flex-direction: column;
}

.build-number .number {
  font-weight: var(--font-weight-semibold);
  color: var(--color-text-primary);
  font-family: var(--font-mono);
}

.build-number .commit {
  font-size: var(--font-size-xs);
  color: var(--color-text-muted);
  font-family: var(--font-mono);
}

.project-cell {
  display: flex;
  flex-direction: column;
}

.project-cell .project-name {
  font-weight: var(--font-weight-medium);
  color: var(--color-text-primary);
}

.project-cell .group-name {
  font-size: var(--font-size-xs);
  color: var(--color-text-muted);
}

.branch-cell,
.user-cell {
  display: flex;
  align-items: center;
  gap: var(--spacing-xs);
  color: var(--color-text-secondary);
}
</style>
