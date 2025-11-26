<script setup lang="ts">
import { ref, computed, onMounted, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import {
  ArrowLeftOutlined,
  UserOutlined,
  TeamOutlined,
  CalendarOutlined,
  BranchesOutlined,
  SettingOutlined,
  CheckCircleOutlined,
  ClockCircleOutlined,
  ExclamationCircleOutlined,
  PlayCircleOutlined,
  LoadingOutlined,
  PauseCircleOutlined,
  StopOutlined,
} from '@ant-design/icons-vue'
import { useProjectStore } from '@/stores/project'
import { useLayerStore } from '@/stores/layer'
import { useBuildStore } from '@/stores/build'
import type { Build, BuildStage, Layer, Milestone } from '@/types'

const route = useRoute()
const router = useRouter()
const projectStore = useProjectStore()
const layerStore = useLayerStore()
const buildStore = useBuildStore()

const props = defineProps<{
  projectId: string
}>()

const activeLayerId = ref<string | null>(null)

onMounted(async () => {
  await projectStore.fetchProjectById(props.projectId)
  await layerStore.fetchLayers(props.projectId)
  await buildStore.fetchBuilds(props.projectId)

  // Set first layer as active if exists
  const layers = layerStore.layers.filter(l => l.projectId === props.projectId)
  if (layers.length > 0 && layers[0]) {
    activeLayerId.value = layers[0].id
  }
})

watch(() => props.projectId, async (newId) => {
  await projectStore.fetchProjectById(newId)
  await layerStore.fetchLayers(newId)
  await buildStore.fetchBuilds(newId)

  const layers = layerStore.layers.filter(l => l.projectId === newId)
  if (layers.length > 0 && layers[0]) {
    activeLayerId.value = layers[0].id
  }
})

const project = computed(() => projectStore.currentProject)

const projectLayers = computed(() => {
  return layerStore.layers.filter(l => l.projectId === props.projectId)
})

const activeLayer = computed(() => {
  if (!activeLayerId.value) return null
  return projectLayers.value.find(l => l.id === activeLayerId.value) || null
})

const layerBuilds = computed(() => {
  if (!activeLayerId.value) return []
  return buildStore.builds.filter(b => b.layerId === activeLayerId.value)
})

function goBack() {
  router.push('/')
}

function getMilestoneStatus(milestone: Milestone) {
  switch (milestone.status) {
    case 'completed':
      return { icon: CheckCircleOutlined, color: 'var(--accent-success)' }
    case 'in_progress':
      return { icon: LoadingOutlined, color: 'var(--accent-primary)' }
    case 'pending':
      return { icon: ClockCircleOutlined, color: 'var(--text-muted)' }
    default:
      return { icon: ClockCircleOutlined, color: 'var(--text-muted)' }
  }
}

function getBuildStatusIcon(status: Build['status']) {
  switch (status) {
    case 'success':
      return CheckCircleOutlined
    case 'failed':
      return ExclamationCircleOutlined
    case 'running':
      return LoadingOutlined
    case 'pending':
      return ClockCircleOutlined
    case 'cancelled':
      return StopOutlined
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
    case 'cancelled':
      return 'var(--accent-warning)'
    default:
      return 'var(--text-muted)'
  }
}

function getStageStatusColor(status: BuildStage['status']) {
  switch (status) {
    case 'success':
      return '#22c55e'
    case 'failed':
      return '#ef4444'
    case 'running':
      return '#3b82f6'
    case 'pending':
      return '#6b7280'
    case 'skipped':
      return '#4b5563'
    default:
      return '#6b7280'
  }
}

function formatDuration(seconds: number | null | undefined) {
  if (!seconds) return '-'
  const mins = Math.floor(seconds / 60)
  const secs = seconds % 60
  return mins > 0 ? `${mins}m ${secs}s` : `${secs}s`
}

function formatDate(dateStr: string) {
  return new Date(dateStr).toLocaleString()
}
</script>

<template>
  <div class="project-detail-page">
    <!-- Back Navigation -->
    <div class="back-nav">
      <a-button type="text" @click="goBack">
        <arrow-left-outlined /> Back to Projects
      </a-button>
    </div>

    <a-spin :spinning="projectStore.loading">
      <template v-if="project">
        <!-- Project Header -->
        <div class="project-header">
          <div class="header-main">
            <h1 class="project-title">{{ project.name }}</h1>
            <div class="project-badges">
              <span class="oem-badge">{{ project.oem }}</span>
              <span class="status-badge" :class="project.status">{{ project.status }}</span>
            </div>
          </div>
        </div>

        <!-- Project Info Section -->
        <div class="info-section">
          <div class="info-grid">
            <!-- Team Info -->
            <div class="info-card">
              <h3><user-outlined /> Tech Lead</h3>
              <div class="tl-info" v-if="project.tl">
                <a-avatar size="small" :style="{ backgroundColor: 'var(--accent-primary)' }">
                  {{ project.tl.name.charAt(0) }}
                </a-avatar>
                <div class="tl-details">
                  <span class="tl-name">{{ project.tl.name }}</span>
                  <span class="tl-email">{{ project.tl.email }}</span>
                </div>
              </div>
            </div>

            <!-- Team Members -->
            <div class="info-card">
              <h3><team-outlined /> Team Members</h3>
              <div class="members-list" v-if="project.members">
                <a-avatar-group :max-count="5" :max-style="{ color: '#f56a00', backgroundColor: '#fde3cf' }">
                  <a-tooltip v-for="member in project.members" :key="member.id" :title="`${member.name} (${member.role})`">
                    <a-avatar :style="{ backgroundColor: member.role === 'TL' ? 'var(--accent-primary)' : 'var(--accent-secondary)' }">
                      {{ member.name.charAt(0) }}
                    </a-avatar>
                  </a-tooltip>
                </a-avatar-group>
                <span class="member-count">{{ project.members.length }} members</span>
              </div>
            </div>

            <!-- Config Info -->
            <div class="info-card">
              <h3><setting-outlined /> Build Config</h3>
              <div class="config-info" v-if="project.config">
                <div class="config-item">
                  <branches-outlined />
                  <span>{{ project.config.defaultBranch }}</span>
                </div>
                <div class="config-item">
                  <span class="config-label">Options:</span>
                  <span>{{ project.config.buildOptions?.length || 0 }} defined</span>
                </div>
              </div>
            </div>

            <!-- Milestones -->
            <div class="info-card milestones-card">
              <h3><calendar-outlined /> Milestones</h3>
              <div class="milestones-timeline" v-if="project.milestones">
                <div
                  v-for="milestone in project.milestones"
                  :key="milestone.id"
                  class="milestone-item"
                  :class="milestone.status"
                >
                  <component
                    :is="getMilestoneStatus(milestone).icon"
                    :style="{ color: getMilestoneStatus(milestone).color }"
                  />
                  <div class="milestone-info">
                    <span class="milestone-name">{{ milestone.name }}</span>
                    <span class="milestone-date">{{ milestone.targetDate }}</span>
                  </div>
                </div>
              </div>
            </div>
          </div>
        </div>

        <!-- Layers & Builds Section -->
        <div class="layers-section">
          <h2>Build Layers</h2>

          <!-- Layer Tabs -->
          <a-tabs v-model:activeKey="activeLayerId" type="card" class="layer-tabs">
            <a-tab-pane
              v-for="layer in projectLayers"
              :key="layer.id"
              :tab="layer.name"
            >
              <div class="layer-content">
                <!-- Layer Info -->
                <div class="layer-info">
                  <span class="layer-type" :class="layer.type">{{ layer.type }}</span>
                  <span class="pipeline-stages">
                    {{ layer.pipelineConfig?.filter(s => s.enabled).length || 0 }} pipeline stages
                  </span>
                </div>

                <!-- Pipeline Config -->
                <div class="pipeline-config" v-if="layer.pipelineConfig">
                  <h4>Pipeline Configuration</h4>
                  <div class="stage-chips">
                    <span
                      v-for="stage in layer.pipelineConfig.filter(s => s.enabled)"
                      :key="stage.name"
                      class="stage-chip"
                      :class="{ required: stage.required }"
                    >
                      {{ stage.name }}
                      <span v-if="stage.required" class="required-mark">*</span>
                    </span>
                  </div>
                </div>

                <!-- Builds List -->
                <div class="builds-section">
                  <h4>Recent Builds</h4>
                  <div class="builds-list" v-if="layerBuilds.length > 0">
                    <div
                      v-for="build in layerBuilds"
                      :key="build.id"
                      class="build-card"
                    >
                      <div class="build-header">
                        <div class="build-info">
                          <component
                            :is="getBuildStatusIcon(build.status)"
                            :style="{ color: getBuildStatusColor(build.status), fontSize: '18px' }"
                          />
                          <span class="build-number">#{{ build.buildNumber }}</span>
                          <span class="build-branch">
                            <branches-outlined /> {{ build.branch }}
                          </span>
                        </div>
                        <div class="build-meta">
                          <span class="build-time">{{ formatDate(build.startedAt) }}</span>
                          <span class="build-duration" v-if="build.duration">
                            {{ formatDuration(build.duration) }}
                          </span>
                        </div>
                      </div>

                      <!-- Pipeline Stages -->
                      <div class="build-stages">
                        <div
                          v-for="stage in build.stages"
                          :key="stage.name"
                          class="stage-block"
                          :style="{ borderLeftColor: getStageStatusColor(stage.status) }"
                        >
                          <span class="stage-name">{{ stage.name }}</span>
                          <span class="stage-duration">{{ formatDuration(stage.duration) }}</span>
                        </div>
                      </div>

                      <!-- TR Status (for Release layers) -->
                      <div class="tr-status" v-if="layer.type === 'release' && build.trStatus">
                        <span class="tr-label">TR Status:</span>
                        <span class="tr-badge" :class="build.trStatus">{{ build.trStatus }}</span>
                      </div>

                      <!-- Artifacts -->
                      <div class="build-artifacts" v-if="build.artifacts">
                        <a-button
                          v-if="build.artifacts.binaryUrl"
                          size="small"
                          type="primary"
                          ghost
                        >
                          Download Binary
                        </a-button>
                        <a-button
                          v-if="build.artifacts.logsUrl"
                          size="small"
                        >
                          View Logs
                        </a-button>
                      </div>
                    </div>
                  </div>
                  <a-empty v-else description="No builds yet" />
                </div>
              </div>
            </a-tab-pane>
          </a-tabs>
        </div>
      </template>
      <a-empty v-else-if="!projectStore.loading" description="Project not found" />
    </a-spin>
  </div>
</template>

<style scoped>
.project-detail-page {
  display: flex;
  flex-direction: column;
  gap: 24px;
}

.back-nav {
  margin-bottom: 8px;
}

.back-nav .ant-btn {
  color: var(--text-secondary);
  padding-left: 0;
}

.back-nav .ant-btn:hover {
  color: var(--accent-primary);
}

/* Project Header */
.project-header {
  background: var(--bg-secondary);
  border-radius: 16px;
  padding: 24px;
  border: 1px solid rgba(255, 255, 255, 0.05);
}

.header-main {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.project-title {
  margin: 0;
  font-size: 28px;
  font-weight: 700;
  color: var(--text-primary);
}

.project-badges {
  display: flex;
  gap: 12px;
}

.oem-badge {
  background: rgba(34, 197, 94, 0.15);
  color: #22c55e;
  padding: 6px 16px;
  border-radius: 20px;
  font-size: 14px;
  font-weight: 600;
}

.status-badge {
  padding: 6px 16px;
  border-radius: 20px;
  font-size: 14px;
  font-weight: 600;
  text-transform: capitalize;
}

.status-badge.active {
  background: rgba(59, 130, 246, 0.15);
  color: #3b82f6;
}

.status-badge.inactive {
  background: rgba(234, 179, 8, 0.15);
  color: #eab308;
}

.status-badge.archived {
  background: rgba(107, 114, 128, 0.15);
  color: #6b7280;
}

/* Info Section */
.info-section {
  margin-bottom: 8px;
}

.info-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(280px, 1fr));
  gap: 16px;
}

.info-card {
  background: var(--bg-secondary);
  border-radius: 12px;
  padding: 20px;
  border: 1px solid rgba(255, 255, 255, 0.05);
}

.info-card h3 {
  margin: 0 0 16px;
  font-size: 14px;
  font-weight: 600;
  color: var(--text-secondary);
  display: flex;
  align-items: center;
  gap: 8px;
}

.tl-info {
  display: flex;
  align-items: center;
  gap: 12px;
}

.tl-details {
  display: flex;
  flex-direction: column;
}

.tl-name {
  font-weight: 600;
  color: var(--text-primary);
}

.tl-email {
  font-size: 12px;
  color: var(--text-muted);
}

.members-list {
  display: flex;
  align-items: center;
  gap: 12px;
}

.member-count {
  font-size: 14px;
  color: var(--text-secondary);
}

.config-info {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.config-item {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 14px;
  color: var(--text-secondary);
}

.config-label {
  color: var(--text-muted);
}

/* Milestones */
.milestones-timeline {
  display: flex;
  gap: 16px;
  flex-wrap: wrap;
}

.milestone-item {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 8px 12px;
  background: rgba(255, 255, 255, 0.03);
  border-radius: 8px;
}

.milestone-info {
  display: flex;
  flex-direction: column;
}

.milestone-name {
  font-weight: 600;
  font-size: 14px;
  color: var(--text-primary);
}

.milestone-date {
  font-size: 11px;
  color: var(--text-muted);
}

/* Layers Section */
.layers-section {
  background: var(--bg-secondary);
  border-radius: 16px;
  padding: 24px;
  border: 1px solid rgba(255, 255, 255, 0.05);
}

.layers-section h2 {
  margin: 0 0 20px;
  font-size: 20px;
  font-weight: 600;
  color: var(--text-primary);
}

.layer-tabs :deep(.ant-tabs-nav) {
  margin-bottom: 20px;
}

.layer-content {
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.layer-info {
  display: flex;
  align-items: center;
  gap: 16px;
}

.layer-type {
  padding: 4px 12px;
  border-radius: 12px;
  font-size: 12px;
  font-weight: 600;
  text-transform: uppercase;
}

.layer-type.release {
  background: rgba(139, 92, 246, 0.15);
  color: #a78bfa;
}

.layer-type.custom {
  background: rgba(59, 130, 246, 0.15);
  color: #3b82f6;
}

.pipeline-stages {
  font-size: 14px;
  color: var(--text-secondary);
}

/* Pipeline Config */
.pipeline-config {
  background: rgba(255, 255, 255, 0.02);
  border-radius: 12px;
  padding: 16px;
}

.pipeline-config h4 {
  margin: 0 0 12px;
  font-size: 14px;
  font-weight: 600;
  color: var(--text-secondary);
}

.stage-chips {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
}

.stage-chip {
  padding: 6px 12px;
  background: rgba(255, 255, 255, 0.05);
  border-radius: 16px;
  font-size: 12px;
  color: var(--text-secondary);
}

.stage-chip.required {
  background: rgba(139, 92, 246, 0.1);
  color: #a78bfa;
}

.required-mark {
  color: var(--accent-danger);
  margin-left: 2px;
}

/* Builds Section */
.builds-section h4 {
  margin: 0 0 16px;
  font-size: 16px;
  font-weight: 600;
  color: var(--text-primary);
}

.builds-list {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.build-card {
  background: rgba(255, 255, 255, 0.02);
  border-radius: 12px;
  padding: 16px;
  border: 1px solid rgba(255, 255, 255, 0.05);
}

.build-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 16px;
}

.build-info {
  display: flex;
  align-items: center;
  gap: 12px;
}

.build-number {
  font-weight: 700;
  font-size: 16px;
  color: var(--text-primary);
}

.build-branch {
  display: flex;
  align-items: center;
  gap: 4px;
  font-size: 14px;
  color: var(--text-secondary);
}

.build-meta {
  display: flex;
  align-items: center;
  gap: 16px;
  font-size: 13px;
  color: var(--text-muted);
}

/* Build Stages */
.build-stages {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
  margin-bottom: 12px;
}

.stage-block {
  padding: 8px 12px;
  background: rgba(0, 0, 0, 0.2);
  border-radius: 6px;
  border-left: 3px solid;
  display: flex;
  flex-direction: column;
  min-width: 80px;
}

.stage-name {
  font-size: 12px;
  font-weight: 500;
  color: var(--text-primary);
}

.stage-duration {
  font-size: 11px;
  color: var(--text-muted);
}

/* TR Status */
.tr-status {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-bottom: 12px;
}

.tr-label {
  font-size: 13px;
  color: var(--text-secondary);
}

.tr-badge {
  padding: 4px 10px;
  border-radius: 12px;
  font-size: 12px;
  font-weight: 600;
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

/* Build Artifacts */
.build-artifacts {
  display: flex;
  gap: 8px;
  padding-top: 12px;
  border-top: 1px solid rgba(255, 255, 255, 0.05);
}
</style>
