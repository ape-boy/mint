<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import {
  SearchOutlined,
  AppstoreOutlined,
  TableOutlined,
} from '@ant-design/icons-vue'
import { PageHeader, StatusBadge, EmptyState, ProjectCard, ProjectGroupSidebar } from '@/components'
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
  await Promise.all([
    projectGroupStore.fetchProjectGroups(),
    projectStore.fetchProjects()
  ])
})

const filteredProjects = computed(() => {
  let projects = projectStore.projects

  if (selectedGroupId.value) {
    projects = projects.filter(p => p.groupId === selectedGroupId.value)
  }

  if (searchText.value) {
    const search = searchText.value.toLowerCase()
    projects = projects.filter(p =>
      p.name.toLowerCase().includes(search) ||
      p.oem.toLowerCase().includes(search) ||
      p.tl?.name?.toLowerCase().includes(search)
    )
  }

  return projects
})

const projectCountByGroup = computed(() => {
  const counts: Record<string, number> = {}
  projectStore.projects.forEach(p => {
    counts[p.groupId] = (counts[p.groupId] || 0) + 1
  })
  return counts
})

function getGroupName(groupId: string) {
  const group = projectGroupStore.projectGroups.find(g => g.id === groupId)
  return group?.name || groupId
}

function goToProject(projectId: string) {
  router.push(`/projects/${projectId}`)
}

function goToBuild(projectId: string) {
  router.push({ path: '/build', query: { projectId } })
}

function clearFilters() {
  searchText.value = ''
  selectedGroupId.value = null
}

const tableColumns = [
  { title: 'Status', key: 'status', width: 80 },
  { title: 'Project Name', dataIndex: 'name', key: 'name' },
  { title: 'Product', key: 'groupId', width: 120 },
  { title: 'OEM', dataIndex: 'oem', key: 'oem', width: 100 },
  { title: 'TL', key: 'tl', width: 150 },
  { title: 'Members', key: 'members', width: 100 },
  { title: 'Updated', key: 'updatedAt', width: 120 },
  { title: 'Actions', key: 'actions', width: 120 },
]
</script>

<template>
  <div class="project-list-view">
    <PageHeader title="Projects" description="SSD FW 프로젝트 목록" />

    <div class="content-layout">
      <!-- Sidebar -->
      <ProjectGroupSidebar
        :groups="projectGroupStore.projectGroups"
        :selected-group-id="selectedGroupId"
        :project-count-by-group="projectCountByGroup"
        :total-count="projectStore.projects.length"
        @select="selectedGroupId = $event"
      />

      <!-- Content -->
      <div class="projects-content">
        <div class="content-header">
          <a-input-search
            v-model:value="searchText"
            placeholder="Search by name, OEM, or TL..."
            style="width: 300px"
            allow-clear
          >
            <template #prefix><SearchOutlined /></template>
          </a-input-search>

          <div class="header-actions">
            <span class="project-count">{{ filteredProjects.length }} projects</span>
            <a-radio-group v-model:value="viewMode" button-style="solid" size="small">
              <a-radio-button value="card"><AppstoreOutlined /></a-radio-button>
              <a-radio-button value="table"><TableOutlined /></a-radio-button>
            </a-radio-group>
          </div>
        </div>

        <a-spin :spinning="projectStore.loading || projectGroupStore.loading">
          <!-- Card View -->
          <div v-if="viewMode === 'card'" class="projects-grid">
            <ProjectCard
              v-for="project in filteredProjects"
              :key="project.id"
              :project="project"
              :group-name="getGroupName(project.groupId)"
              @click="goToProject(project.id)"
              @view-builds="goToBuild(project.id)"
            />
          </div>

          <!-- Table View -->
          <a-table
            v-else
            :columns="tableColumns"
            :data-source="filteredProjects"
            :pagination="{ pageSize: 20 }"
            row-key="id"
            class="data-table data-table--clickable"
            @row-click="(record: Project) => goToProject(record.id)"
          >
            <template #bodyCell="{ column, record }">
              <template v-if="column.key === 'status'">
                <StatusBadge :status="record.status" type="project" :show-label="false" />
              </template>
              <template v-else-if="column.key === 'name'">
                <a class="project-link" @click.stop="goToProject(record.id)">{{ record.name }}</a>
              </template>
              <template v-else-if="column.key === 'groupId'">
                <span class="tag">{{ getGroupName(record.groupId) }}</span>
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
                <a-button type="primary" size="small" ghost @click.stop="goToBuild(record.id)">
                  Builds
                </a-button>
              </template>
            </template>
          </a-table>

          <EmptyState
            v-if="filteredProjects.length === 0 && !projectStore.loading"
            title="No projects found"
            description="No projects match your current filters"
          >
            <a-button type="primary" @click="clearFilters">Clear Filters</a-button>
          </EmptyState>
        </a-spin>
      </div>
    </div>
  </div>
</template>

<style scoped>
.project-list-view {
  display: flex;
  flex-direction: column;
  gap: var(--spacing-lg);
}

.content-layout {
  display: grid;
  grid-template-columns: 280px 1fr;
  gap: var(--spacing-lg);
}

@media (max-width: 1024px) {
  .content-layout {
    grid-template-columns: 1fr;
  }
}

.projects-content {
  display: flex;
  flex-direction: column;
  gap: var(--spacing-lg);
}

.content-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.header-actions {
  display: flex;
  align-items: center;
  gap: var(--spacing-md);
}

.project-count {
  color: var(--color-text-muted);
  font-size: var(--font-size-md);
}

.projects-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(320px, 1fr));
  gap: var(--spacing-lg);
}

.project-link {
  color: var(--color-accent-primary);
  font-weight: var(--font-weight-medium);
}

.project-link:hover {
  text-decoration: underline;
}
</style>
