<script setup lang="ts">
import { ref, onMounted, computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ArrowLeftOutlined, DownloadOutlined, BranchesOutlined, UserOutlined } from '@ant-design/icons-vue'
import { StatusBadge, EmptyState, PipelineVisualization } from '@/components'
import { useBuildStore } from '@/stores/build'
import { useProjectStore } from '@/stores/project'
import { useLayerStore } from '@/stores/layer'
import { useFormat } from '@/composables'
import type { Build } from '@/types'

const route = useRoute()
const router = useRouter()
const buildStore = useBuildStore()
const projectStore = useProjectStore()
const layerStore = useLayerStore()
const { formatDuration, formatDateTime } = useFormat()

const build = ref<Build | null>(null)

onMounted(async () => {
  const buildId = route.params.id as string
  await buildStore.fetchBuildById(buildId)
  build.value = buildStore.currentBuild

  if (build.value) {
    await Promise.all([
      projectStore.fetchProjects(),
      layerStore.fetchLayers(build.value.projectId)
    ])
  }
})

const project = computed(() => {
  if (!build.value) return null
  return projectStore.projects.find((p) => p.id === build.value!.projectId)
})

const layer = computed(() => {
  if (!build.value) return null
  return layerStore.layers.find((l) => l.id === build.value!.layerId)
})
</script>

<template>
  <div class="build-detail-view">
    <template v-if="build">
      <!-- Header -->
      <div class="page-header">
        <div class="header-left">
          <a-button type="text" @click="router.back()">
            <template #icon><ArrowLeftOutlined /></template>
          </a-button>
          <div class="title-section">
            <h1>Build #{{ build.buildNumber }}</h1>
            <StatusBadge :status="build.status" type="build" />
          </div>
        </div>
        <div class="header-right">
          <a-button v-if="build.artifacts?.binaryUrl" type="primary" ghost>
            <template #icon><DownloadOutlined /></template>
            Binary
          </a-button>
          <a-button v-if="build.artifacts?.logsUrl">
            <template #icon><DownloadOutlined /></template>
            Logs
          </a-button>
        </div>
      </div>

      <!-- Pipeline Visualization -->
      <div class="pipeline-section">
        <h3>Build Pipeline</h3>
        <PipelineVisualization :stages="build.stages" />
      </div>

      <!-- Details Grid -->
      <div class="details-grid">
        <div class="detail-card">
          <h3>Build Information</h3>
          <div class="info-row">
            <span class="label">Project</span>
            <span class="value">{{ project?.name || build.projectId }}</span>
          </div>
          <div class="info-row">
            <span class="label">Layer</span>
            <span class="value">
              <span v-if="layer" class="layer-badge" :class="layer.type">{{ layer.name }}</span>
              <span v-else>{{ build.layerId }}</span>
            </span>
          </div>
          <div class="info-row">
            <span class="label">Branch</span>
            <span class="value"><BranchesOutlined /> {{ build.branch }}</span>
          </div>
          <div class="info-row">
            <span class="label">Commit</span>
            <span class="value commit-hash">{{ build.commitHash }}</span>
          </div>
          <div class="info-row">
            <span class="label">Triggered By</span>
            <span class="value"><UserOutlined /> {{ build.triggeredBy }}</span>
          </div>
        </div>

        <div class="detail-card">
          <h3>Timing</h3>
          <div class="info-row">
            <span class="label">Started At</span>
            <span class="value">{{ formatDateTime(build.startedAt) }}</span>
          </div>
          <div class="info-row">
            <span class="label">Duration</span>
            <span class="value">{{ formatDuration(build.duration) }}</span>
          </div>
          <div class="info-row" v-if="build.trStatus">
            <span class="label">TR Status</span>
            <StatusBadge :status="build.trStatus" type="tr" />
          </div>
        </div>
      </div>
    </template>

    <a-spin v-else-if="buildStore.loading" :spinning="true" class="loading-spinner" />
    <EmptyState v-else title="Build not found" description="The requested build could not be found" />
  </div>
</template>

<style scoped>
.build-detail-view {
  display: flex;
  flex-direction: column;
  gap: var(--spacing-lg);
}

.page-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.header-left {
  display: flex;
  align-items: center;
  gap: var(--spacing-md);
}

.title-section {
  display: flex;
  align-items: center;
  gap: var(--spacing-md);
}

.title-section h1 {
  margin: 0;
  font-size: var(--font-size-2xl);
  font-weight: var(--font-weight-bold);
  color: var(--color-text-primary);
}

.header-right {
  display: flex;
  gap: var(--spacing-sm);
}

/* Pipeline Section */
.pipeline-section {
  background: var(--color-bg-secondary);
  border: 1px solid var(--color-border-light);
  border-radius: var(--radius-lg);
  padding: var(--spacing-lg);
}

.pipeline-section h3 {
  margin: 0 0 var(--spacing-lg);
  font-size: var(--font-size-lg);
  font-weight: var(--font-weight-semibold);
  color: var(--color-text-primary);
}

/* Details Grid */
.details-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(300px, 1fr));
  gap: var(--spacing-lg);
}

.detail-card {
  background: var(--color-bg-secondary);
  border: 1px solid var(--color-border-light);
  border-radius: var(--radius-lg);
  padding: var(--spacing-lg);
}

.detail-card h3 {
  margin: 0 0 var(--spacing-lg);
  font-size: var(--font-size-lg);
  font-weight: var(--font-weight-semibold);
  color: var(--color-text-primary);
  border-bottom: 1px solid var(--color-border-light);
  padding-bottom: var(--spacing-sm);
}

.info-row {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: var(--spacing-sm) 0;
  border-bottom: 1px solid var(--color-border-light);
}

.info-row:last-child {
  border-bottom: none;
}

.info-row .label {
  color: var(--color-text-muted);
  font-size: var(--font-size-sm);
}

.info-row .value {
  font-weight: var(--font-weight-medium);
  color: var(--color-text-primary);
  display: flex;
  align-items: center;
  gap: var(--spacing-xs);
}

.commit-hash {
  font-family: var(--font-mono);
  background: rgba(255, 255, 255, 0.1);
  padding: 2px 8px;
  border-radius: var(--radius-sm);
  font-size: var(--font-size-sm);
}

.loading-spinner {
  display: flex;
  justify-content: center;
  align-items: center;
  min-height: 300px;
}

.layer-badge {
  padding: 2px 10px;
  border-radius: var(--radius-full);
  font-size: var(--font-size-xs);
  font-weight: var(--font-weight-semibold);
  text-transform: uppercase;
}

.layer-badge.release {
  background: var(--color-bg-indigo);
  color: var(--color-accent-secondary);
}

.layer-badge.custom {
  background: var(--color-bg-info);
  color: var(--color-accent-info);
}
</style>
