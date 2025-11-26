<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import {
  CheckCircleOutlined,
  CloseCircleOutlined,
  SyncOutlined,
  ClockCircleOutlined,
  StopOutlined,
  ArrowLeftOutlined,
  DownloadOutlined,
  BranchesOutlined,
  UserOutlined
} from '@ant-design/icons-vue'
import { useBuildStore } from '@/stores/build'
import type { Build } from '@/types'

const route = useRoute()
const router = useRouter()
const buildStore = useBuildStore()
const build = ref<Build | null>(null)

onMounted(async () => {
  const buildId = route.params.id as string
  await buildStore.fetchBuildById(buildId)
  build.value = buildStore.currentBuild
})

function getStatusColor(status: string) {
  switch (status) {
    case 'success': return 'var(--accent-success)'
    case 'failed': return 'var(--accent-danger)'
    case 'running': return 'var(--accent-primary)'
    case 'pending': return 'var(--accent-warning)'
    default: return 'var(--text-muted)'
  }
}

function getStatusIcon(status: string) {
  switch (status) {
    case 'success': return CheckCircleOutlined
    case 'failed': return CloseCircleOutlined
    case 'running': return SyncOutlined
    case 'pending': return ClockCircleOutlined
    default: return StopOutlined
  }
}

function formatDuration(seconds: number | null | undefined) {
  if (!seconds) return '-'
  const mins = Math.floor(seconds / 60)
  const secs = seconds % 60
  return mins > 0 ? `${mins}m ${secs}s` : `${secs}s`
}
</script>

<template>
  <div class="build-detail-page" v-if="build">
    <!-- Header -->
    <div class="page-header">
      <div class="header-left">
        <a-button type="text" @click="router.back()">
          <template #icon><arrow-left-outlined /></template>
        </a-button>
        <div class="title-section">
          <h1>Build #{{ build.buildNumber }}</h1>
          <span class="status-badge" :style="{ color: getStatusColor(build.status), borderColor: getStatusColor(build.status) }">
            <component :is="getStatusIcon(build.status)" /> {{ build.status }}
          </span>
        </div>
      </div>
      <div class="header-right">
        <a-button v-if="build.artifacts?.binaryUrl" type="primary" ghost>
          <template #icon><download-outlined /></template>
          Binary
        </a-button>
        <a-button v-if="build.artifacts?.logsUrl">
          <template #icon><download-outlined /></template>
          Logs
        </a-button>
      </div>
    </div>

    <!-- Pipeline Visualization -->
    <div class="pipeline-section">
      <h3>Build Pipeline</h3>
      <div class="pipeline-container">
        <div v-for="(stage, index) in build.stages" :key="stage.name" class="pipeline-stage">
          <div class="stage-node" :class="stage.status">
            <div class="stage-icon">
              <component :is="getStatusIcon(stage.status)" :spin="stage.status === 'running'" />
            </div>
            <div class="stage-info">
              <span class="stage-name">{{ stage.name }}</span>
              <span class="stage-duration">{{ formatDuration(stage.duration) }}</span>
            </div>
          </div>
          <div v-if="index < build.stages.length - 1" class="stage-connector" :class="stage.status"></div>
        </div>
      </div>
    </div>

    <!-- Details Grid -->
    <div class="details-grid">
      <div class="detail-card">
        <h3>Build Information</h3>
        <div class="info-row">
          <span class="label">Project ID</span>
          <span class="value">{{ build.projectId }}</span>
        </div>
        <div class="info-row">
          <span class="label">Layer ID</span>
          <span class="value">{{ build.layerId }}</span>
        </div>
        <div class="info-row">
          <span class="label">Branch</span>
          <span class="value"><branches-outlined /> {{ build.branch }}</span>
        </div>
        <div class="info-row">
          <span class="label">Commit</span>
          <span class="value commit-hash">{{ build.commitHash }}</span>
        </div>
        <div class="info-row">
          <span class="label">Triggered By</span>
          <span class="value"><user-outlined /> {{ build.triggeredBy }}</span>
        </div>
      </div>

      <div class="detail-card">
        <h3>Timing</h3>
        <div class="info-row">
          <span class="label">Started At</span>
          <span class="value">{{ new Date(build.startedAt).toLocaleString() }}</span>
        </div>
        <div class="info-row">
          <span class="label">Duration</span>
          <span class="value">{{ formatDuration(build.duration) }}</span>
        </div>
        <div class="info-row" v-if="build.trStatus">
          <span class="label">TR Status</span>
          <span class="value tr-badge" :class="build.trStatus">{{ build.trStatus }}</span>
        </div>
      </div>
    </div>
  </div>
  <a-spin v-else :spinning="true" />
</template>

<style scoped>
.build-detail-page {
  display: flex;
  flex-direction: column;
  gap: 24px;
}

.page-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.header-left {
  display: flex;
  align-items: center;
  gap: 16px;
}

.title-section {
  display: flex;
  align-items: center;
  gap: 16px;
}

.title-section h1 {
  margin: 0;
  font-size: 24px;
}

.status-badge {
  display: flex;
  align-items: center;
  gap: 6px;
  padding: 4px 12px;
  border: 1px solid;
  border-radius: 20px;
  font-size: 14px;
  font-weight: 600;
  text-transform: uppercase;
  background: rgba(255, 255, 255, 0.05);
}

.header-right {
  display: flex;
  gap: 12px;
}

/* Pipeline Visualization */
.pipeline-section {
  background: var(--bg-secondary);
  border: 1px solid rgba(255, 255, 255, 0.05);
  border-radius: 16px;
  padding: 24px;
  overflow-x: auto;
}

.pipeline-section h3 {
  margin: 0 0 24px;
  font-size: 18px;
}

.pipeline-container {
  display: flex;
  align-items: center;
  padding: 20px 0;
  min-width: max-content;
}

.pipeline-stage {
  display: flex;
  align-items: center;
}

.stage-node {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 8px;
  position: relative;
  z-index: 2;
  cursor: pointer;
  transition: transform 0.2s;
}

.stage-node:hover {
  transform: scale(1.05);
}

.stage-icon {
  width: 48px;
  height: 48px;
  border-radius: 50%;
  background: var(--bg-tertiary);
  border: 2px solid var(--text-muted);
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 20px;
  color: var(--text-muted);
  transition: all 0.3s;
}

.stage-node.success .stage-icon {
  border-color: var(--accent-success);
  color: var(--accent-success);
  background: rgba(16, 185, 129, 0.1);
  box-shadow: 0 0 15px rgba(16, 185, 129, 0.3);
}

.stage-node.failed .stage-icon {
  border-color: var(--accent-danger);
  color: var(--accent-danger);
  background: rgba(239, 68, 68, 0.1);
  box-shadow: 0 0 15px rgba(239, 68, 68, 0.3);
}

.stage-node.running .stage-icon {
  border-color: var(--accent-primary);
  color: var(--accent-primary);
  background: rgba(59, 130, 246, 0.1);
  box-shadow: 0 0 15px rgba(59, 130, 246, 0.3);
}

.stage-node.skipped .stage-icon {
  opacity: 0.5;
}

.stage-info {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 2px;
}

.stage-name {
  font-weight: 600;
  font-size: 14px;
  white-space: nowrap;
}

.stage-duration {
  font-size: 12px;
  color: var(--text-muted);
}

.stage-connector {
  width: 60px;
  height: 2px;
  background: var(--text-muted);
  opacity: 0.3;
  margin: 0 8px;
  margin-bottom: 24px; /* Align with icon center */
}

.stage-connector.success {
  background: var(--accent-success);
  opacity: 1;
}

/* Details Grid */
.details-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(300px, 1fr));
  gap: 24px;
}

.detail-card {
  background: var(--bg-secondary);
  border: 1px solid rgba(255, 255, 255, 0.05);
  border-radius: 16px;
  padding: 24px;
}

.detail-card h3 {
  margin: 0 0 20px;
  font-size: 18px;
  border-bottom: 1px solid rgba(255, 255, 255, 0.05);
  padding-bottom: 12px;
}

.info-row {
  display: flex;
  justify-content: space-between;
  padding: 8px 0;
  border-bottom: 1px solid rgba(255, 255, 255, 0.02);
}

.info-row:last-child {
  border-bottom: none;
}

.info-row .label {
  color: var(--text-muted);
}

.info-row .value {
  font-weight: 500;
  display: flex;
  align-items: center;
  gap: 8px;
}

.commit-hash {
  font-family: monospace;
  background: rgba(255, 255, 255, 0.1);
  padding: 2px 6px;
  border-radius: 4px;
}

.tr-badge {
  padding: 2px 8px;
  border-radius: 8px;
  font-size: 12px;
}

.tr-badge.available {
  background: rgba(34, 197, 94, 0.15);
  color: #22c55e;
}

.tr-badge.pending_approval {
  background: rgba(234, 179, 8, 0.15);
  color: #eab308;
}

.tr-badge.approved {
  background: rgba(59, 130, 246, 0.15);
  color: #3b82f6;
}

.tr-badge.rejected {
  background: rgba(239, 68, 68, 0.15);
  color: #ef4444;
}
</style>
