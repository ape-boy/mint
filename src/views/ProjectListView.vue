<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import {
  FolderOutlined,
  AppstoreOutlined,
  SearchOutlined,
  CheckCircleOutlined,
  MinusCircleOutlined,
  ClockCircleOutlined,
  TableOutlined,
  UserOutlined,
  TeamOutlined,
} from '@ant-design/icons-vue'
import { useProjectGroupStore } from '@/stores/projectGroup'
import { useProjectStore } from '@/stores/project'
import type { Project } from '@/types'

const router = useRouter()
const projectGroupStore = useProjectGroupStore()
const projectStore = useProjectStore()

const selectedGroupId = ref<string | null>(null)
const searchText = ref('')
const viewMode = ref<'card' | 'table'>('card')

onMounted(async () => {
  await projectGroupStore.fetchProjectGroups()
  await projectStore.fetchProjects()
})

const filteredProjects = computed(() => {
  let projects = projectStore.projects

  if (selectedGroupId.value) {
    projects = projects.filter((p) => p.groupId === selectedGroupId.value)
  }

  if (searchText.value) {
    const search = searchText.value.toLowerCase()
    projects = projects.filter(
      (p) =>
        p.name.toLowerCase().includes(search) ||
        p.oem.toLowerCase().includes(search) ||
        p.tl?.name?.toLowerCase().includes(search)
    )
  }

  return projects
})

const projectCountByGroup = computed(() => {
  const counts: Record<string, number> = {}
  projectStore.projects.forEach((p) => {
    counts[p.groupId] = (counts[p.groupId] || 0) + 1
  })
  return counts
})

function selectGroup(groupId: string | null) {
  selectedGroupId.value = groupId
}

function getStatusIcon(status: Project['status']) {
  switch (status) {
    case 'active':
      return CheckCircleOutlined
    case 'inactive':
      return MinusCircleOutlined
    case 'archived':
      return ClockCircleOutlined
  }
}

function getStatusColor(status: Project['status']) {
  switch (status) {
    case 'active':
      return 'var(--accent-success)'
    case 'inactive':
      return 'var(--accent-warning)'
    case 'archived':
      return 'var(--text-muted)'
  }
}

function getGroupName(groupId: string) {
  const group = projectGroupStore.projectGroups.find((g) => g.id === groupId)
  return group?.name || groupId
}

function goToProject(projectId: string) {
  router.push(`/projects/${projectId}`)
}

function goToBuild(projectId: string) {
  router.push({ path: '/build', query: { projectId } })
}

// Table columns
const tableColumns = [
  {
    title: 'Status',
    dataIndex: 'status',
    key: 'status',
    width: 80,
  },
  {
    title: 'Project Name',
    dataIndex: 'name',
    key: 'name',
  },
  {
    title: 'Product',
    dataIndex: 'groupId',
    key: 'groupId',
    width: 120,
  },
  {
    title: 'OEM',
    dataIndex: 'oem',
    key: 'oem',
    width: 100,
  },
  {
    title: 'TL',
    key: 'tl',
    width: 150,
  },
  {
    title: 'Members',
    key: 'members',
    width: 100,
  },
  {
    title: 'Updated',
    dataIndex: 'updatedAt',
    key: 'updatedAt',
    width: 120,
  },
  {
    title: 'Actions',
    key: 'actions',
    width: 120,
  },
]
</script>

<template>
  <div class="project-list-page">
    <div class="page-header">
      <h1>Projects</h1>
      <p class="page-description">SSD FW 프로젝트 목록</p>
    </div>

    <div class="content-layout">
      <!-- Project Groups Sidebar -->
      <div class="groups-sidebar">
        <div class="sidebar-header">
          <h3><appstore-outlined /> Product Groups</h3>
        </div>
        <div class="group-list">
          <div
            class="group-item"
            :class="{ active: selectedGroupId === null }"
            @click="selectGroup(null)"
          >
            <folder-outlined />
            <span class="group-name">All Projects</span>
            <span class="group-count">{{ projectStore.projects.length }}</span>
          </div>
          <div
            v-for="group in projectGroupStore.projectGroups"
            :key="group.id"
            class="group-item"
            :class="{ active: selectedGroupId === group.id }"
            @click="selectGroup(group.id)"
          >
            <folder-outlined />
            <span class="group-name">{{ group.name }}</span>
            <span class="group-count">{{ projectCountByGroup[group.id] || 0 }}</span>
          </div>
        </div>
      </div>

      <!-- Projects Content -->
      <div class="projects-content">
        <div class="content-header">
          <a-input-search
            v-model:value="searchText"
            placeholder="Search by name, OEM, or TL..."
            style="width: 300px"
            allow-clear
          >
            <template #prefix>
              <search-outlined />
            </template>
          </a-input-search>
          <div class="header-actions">
            <span class="project-count">{{ filteredProjects.length }} projects</span>
            <a-radio-group v-model:value="viewMode" button-style="solid" size="small">
              <a-radio-button value="card">
                <appstore-outlined />
              </a-radio-button>
              <a-radio-button value="table">
                <table-outlined />
              </a-radio-button>
            </a-radio-group>
          </div>
        </div>

        <a-spin :spinning="projectStore.loading || projectGroupStore.loading">
          <!-- Card View -->
          <div v-if="viewMode === 'card'" class="projects-grid">
            <div
              v-for="project in filteredProjects"
              :key="project.id"
              class="project-card"
              @click="goToProject(project.id)"
            >
              <div class="card-header">
                <div class="project-status">
                  <component
                    :is="getStatusIcon(project.status)"
                    :style="{ color: getStatusColor(project.status) }"
                  />
                </div>
                <span class="project-group-badge">{{ getGroupName(project.groupId) }}</span>
              </div>
              <div class="card-body">
                <h4 class="project-name">{{ project.name }}</h4>
                <div class="project-meta">
                  <span class="meta-item oem-badge">{{ project.oem }}</span>
                </div>
                <div class="project-team">
                  <div class="tl-info" v-if="project.tl">
                    <user-outlined />
                    <span>{{ project.tl.name }}</span>
                  </div>
                  <div class="members-count" v-if="project.members">
                    <team-outlined />
                    <span>{{ project.members.length }} members</span>
                  </div>
                </div>
              </div>
              <div class="card-footer">
                <span class="project-date">
                  Updated: {{ new Date(project.updatedAt).toLocaleDateString() }}
                </span>
                <a-button
                  type="primary"
                  size="small"
                  ghost
                  @click.stop="goToBuild(project.id)"
                >
                  View Builds
                </a-button>
              </div>
            </div>
          </div>

          <!-- Table View -->
          <a-table
            v-else
            :columns="tableColumns"
            :data-source="filteredProjects"
            :pagination="{ pageSize: 20 }"
            row-key="id"
            class="projects-table"
            :row-class-name="() => 'clickable-row'"
            @row-click="(record: Project) => goToProject(record.id)"
          >
            <template #bodyCell="{ column, record }">
              <template v-if="column.key === 'status'">
                <component
                  :is="getStatusIcon(record.status)"
                  :style="{ color: getStatusColor(record.status), fontSize: '16px' }"
                />
              </template>
              <template v-else-if="column.key === 'name'">
                <a class="project-link" @click.stop="goToProject(record.id)">{{ record.name }}</a>
              </template>
              <template v-else-if="column.key === 'groupId'">
                <span class="product-tag">{{ getGroupName(record.groupId) }}</span>
              </template>
              <template v-else-if="column.key === 'tl'">
                <span v-if="record.tl">{{ record.tl.name }}</span>
              </template>
              <template v-else-if="column.key === 'members'">
                <span v-if="record.members">{{ record.members.length }}</span>
              </template>
              <template v-else-if="column.key === 'updatedAt'">
                {{ new Date(record.updatedAt).toLocaleDateString() }}
              </template>
              <template v-else-if="column.key === 'actions'">
                <a-button
                  type="primary"
                  size="small"
                  ghost
                  @click.stop="goToBuild(record.id)"
                >
                  Builds
                </a-button>
              </template>
            </template>
          </a-table>

          <a-empty
            v-if="filteredProjects.length === 0 && !projectStore.loading"
            description="No projects found"
          />
        </a-spin>
      </div>
    </div>
  </div>
</template>

<style scoped>
.project-list-page {
  display: flex;
  flex-direction: column;
  gap: 24px;
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

.content-layout {
  display: grid;
  grid-template-columns: 280px 1fr;
  gap: 24px;
}

@media (max-width: 1024px) {
  .content-layout {
    grid-template-columns: 1fr;
  }
}

/* Groups Sidebar */
.groups-sidebar {
  background: var(--bg-secondary);
  border: 1px solid rgba(255, 255, 255, 0.05);
  border-radius: 16px;
  padding: 20px;
  height: fit-content;
  position: sticky;
  top: 24px;
}

.sidebar-header h3 {
  margin: 0 0 16px;
  font-size: 16px;
  font-weight: 600;
  color: var(--text-primary);
  display: flex;
  align-items: center;
  gap: 8px;
}

.group-list {
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.group-item {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 12px 16px;
  border-radius: 10px;
  cursor: pointer;
  transition: all 0.2s;
  color: var(--text-secondary);
}

.group-item:hover {
  background: rgba(255, 255, 255, 0.05);
  color: var(--text-primary);
}

.group-item.active {
  background: rgba(59, 130, 246, 0.15);
  color: var(--accent-primary);
}

.group-name {
  flex: 1;
  font-weight: 500;
}

.group-count {
  background: rgba(255, 255, 255, 0.1);
  padding: 2px 8px;
  border-radius: 12px;
  font-size: 12px;
  font-weight: 600;
}

/* Projects Content */
.projects-content {
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.content-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.header-actions {
  display: flex;
  align-items: center;
  gap: 16px;
}

.project-count {
  color: var(--text-muted);
  font-size: 14px;
}

.projects-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(320px, 1fr));
  gap: 20px;
}

.project-card {
  background: var(--bg-secondary);
  border: 1px solid rgba(255, 255, 255, 0.05);
  border-radius: 16px;
  padding: 20px;
  cursor: pointer;
  transition: all 0.2s;
  display: flex;
  flex-direction: column;
}

.project-card:hover {
  transform: translateY(-4px);
  box-shadow: 0 10px 30px -10px rgba(0, 0, 0, 0.5);
  border-color: rgba(255, 255, 255, 0.1);
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 16px;
}

.project-status {
  font-size: 18px;
}

.project-group-badge {
  background: rgba(139, 92, 246, 0.15);
  color: #a78bfa;
  padding: 4px 12px;
  border-radius: 20px;
  font-size: 12px;
  font-weight: 600;
}

.card-body {
  flex: 1;
  margin-bottom: 16px;
}

.project-name {
  margin: 0 0 12px;
  font-size: 16px;
  font-weight: 600;
  color: var(--text-primary);
}

.project-meta {
  display: flex;
  gap: 8px;
  margin-bottom: 12px;
}

.oem-badge {
  background: rgba(34, 197, 94, 0.15);
  color: #22c55e;
  padding: 2px 10px;
  border-radius: 12px;
  font-size: 12px;
  font-weight: 500;
}

.project-team {
  display: flex;
  flex-direction: column;
  gap: 6px;
}

.tl-info,
.members-count {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 13px;
  color: var(--text-secondary);
}

.tl-info :deep(.anticon),
.members-count :deep(.anticon) {
  color: var(--text-muted);
}

.card-footer {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding-top: 16px;
  border-top: 1px solid rgba(255, 255, 255, 0.05);
}

.project-date {
  font-size: 12px;
  color: var(--text-muted);
}

/* Table View */
.projects-table {
  background: var(--bg-secondary);
  border-radius: 16px;
}

.projects-table :deep(.ant-table) {
  background: transparent;
}

.projects-table :deep(.ant-table-thead > tr > th) {
  background: rgba(255, 255, 255, 0.03);
  border-bottom: 1px solid rgba(255, 255, 255, 0.05);
  color: var(--text-secondary);
}

.projects-table :deep(.ant-table-tbody > tr > td) {
  border-bottom: 1px solid rgba(255, 255, 255, 0.03);
}

.projects-table :deep(.ant-table-tbody > tr.clickable-row) {
  cursor: pointer;
}

.projects-table :deep(.ant-table-tbody > tr.clickable-row:hover > td) {
  background: rgba(255, 255, 255, 0.03);
}

.project-link {
  color: var(--accent-primary);
  font-weight: 500;
}

.project-link:hover {
  text-decoration: underline;
}

.product-tag {
  background: rgba(139, 92, 246, 0.15);
  color: #a78bfa;
  padding: 2px 8px;
  border-radius: 8px;
  font-size: 12px;
}
</style>
