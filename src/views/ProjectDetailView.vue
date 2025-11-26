<script setup lang="ts">
import { ref, computed, onMounted, watch } from 'vue'
import { useRouter } from 'vue-router'
import {
  ArrowLeftOutlined,
  UserOutlined,
  TeamOutlined,
  CalendarOutlined,
  BranchesOutlined,
  SettingOutlined,
} from '@ant-design/icons-vue'
import { StatusBadge, EmptyState, MilestoneTimeline, BuildCard, StageChip } from '@/components'
import { useProjectStore } from '@/stores/project'
import { useLayerStore } from '@/stores/layer'
import { useBuildStore } from '@/stores/build'

const router = useRouter()
const projectStore = useProjectStore()
const layerStore = useLayerStore()
const buildStore = useBuildStore()

const props = defineProps<{
  projectId: string
}>()

const activeLayerId = ref<string | null>(null)

async function loadData(projectId: string) {
  await Promise.all([
    projectStore.fetchProjectById(projectId),
    layerStore.fetchLayers(projectId),
    buildStore.fetchBuilds(projectId)
  ])

  const layers = layerStore.layers.filter(l => l.projectId === projectId)
  if (layers.length > 0 && layers[0]) {
    activeLayerId.value = layers[0].id
  }
}

onMounted(() => loadData(props.projectId))
watch(() => props.projectId, loadData)

const project = computed(() => projectStore.currentProject)
const projectLayers = computed(() => layerStore.layers.filter(l => l.projectId === props.projectId))
const activeLayer = computed(() => projectLayers.value.find(l => l.id === activeLayerId.value) || null)
const layerBuilds = computed(() => {
  if (!activeLayerId.value) return []
  return buildStore.builds.filter(b => b.layerId === activeLayerId.value)
})

function goBack() {
  router.push('/')
}
</script>

<template>
  <div class="project-detail-view">
    <!-- Back Navigation -->
    <div class="back-nav">
      <a-button type="text" @click="goBack">
        <ArrowLeftOutlined /> Back to Projects
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
              <StatusBadge :status="project.status" type="project" />
            </div>
          </div>
        </div>

        <!-- Project Info Section -->
        <div class="info-section">
          <div class="info-grid">
            <!-- Team Info -->
            <div class="info-card">
              <h3><UserOutlined /> Tech Lead</h3>
              <div v-if="project.tl" class="tl-info">
                <a-avatar size="small" :style="{ backgroundColor: 'var(--color-accent-primary)' }">
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
              <h3><TeamOutlined /> Team Members</h3>
              <div v-if="project.members" class="members-list">
                <a-avatar-group :max-count="5" :max-style="{ backgroundColor: 'var(--color-accent-secondary)' }">
                  <a-tooltip v-for="member in project.members" :key="member.id" :title="`${member.name} (${member.role})`">
                    <a-avatar :style="{ backgroundColor: member.role === 'TL' ? 'var(--color-accent-primary)' : 'var(--color-accent-secondary)' }">
                      {{ member.name.charAt(0) }}
                    </a-avatar>
                  </a-tooltip>
                </a-avatar-group>
                <span class="member-count">{{ project.members.length }} members</span>
              </div>
            </div>

            <!-- Config Info -->
            <div class="info-card">
              <h3><SettingOutlined /> Build Config</h3>
              <div v-if="project.config" class="config-info">
                <div class="config-item">
                  <BranchesOutlined />
                  <span>{{ project.config.defaultBranch }}</span>
                </div>
                <div class="config-item">
                  <span class="config-label">Options:</span>
                  <span>{{ project.config.buildOptions?.length || 0 }} defined</span>
                </div>
              </div>
            </div>

            <!-- Milestones -->
            <div class="info-card">
              <h3><CalendarOutlined /> Milestones</h3>
              <MilestoneTimeline v-if="project.milestones" :milestones="project.milestones" />
            </div>
          </div>
        </div>

        <!-- Layers & Builds Section -->
        <div class="layers-section">
          <h2>Build Layers</h2>

          <a-tabs v-model:activeKey="activeLayerId" type="card" class="layer-tabs">
            <a-tab-pane v-for="layer in projectLayers" :key="layer.id" :tab="layer.name">
              <div class="layer-content">
                <!-- Layer Info -->
                <div class="layer-info">
                  <span class="layer-type" :class="layer.type">{{ layer.type }}</span>
                  <span class="pipeline-stages">
                    {{ layer.pipelineConfig?.filter(s => s.enabled).length || 0 }} pipeline stages
                  </span>
                </div>

                <!-- Pipeline Config -->
                <div v-if="layer.pipelineConfig" class="pipeline-config">
                  <h4>Pipeline Configuration</h4>
                  <div class="stage-chips">
                    <StageChip
                      v-for="stage in layer.pipelineConfig.filter(s => s.enabled)"
                      :key="stage.name"
                      :stage="stage"
                    />
                  </div>
                </div>

                <!-- Builds List -->
                <div class="builds-section">
                  <h4>Recent Builds</h4>
                  <div v-if="layerBuilds.length > 0" class="builds-list">
                    <BuildCard
                      v-for="build in layerBuilds"
                      :key="build.id"
                      :build="build"
                      :layer="activeLayer || undefined"
                      :show-tr-status="activeLayer?.type === 'release'"
                    />
                  </div>
                  <EmptyState v-else title="No builds yet" description="Trigger a build to see it here" />
                </div>
              </div>
            </a-tab-pane>
          </a-tabs>
        </div>
      </template>

      <EmptyState v-else-if="!projectStore.loading" title="Project not found" />
    </a-spin>
  </div>
</template>

<style scoped>
.project-detail-view {
  display: flex;
  flex-direction: column;
  gap: var(--spacing-lg);
}

.back-nav {
  margin-bottom: var(--spacing-sm);
}

.back-nav :deep(.ant-btn) {
  color: var(--color-text-secondary);
  padding-left: 0;
}

.back-nav :deep(.ant-btn:hover) {
  color: var(--color-accent-primary);
}

/* Project Header */
.project-header {
  background: var(--color-bg-secondary);
  border-radius: var(--radius-lg);
  padding: var(--spacing-lg);
  border: 1px solid var(--color-border-light);
}

.header-main {
  display: flex;
  justify-content: space-between;
  align-items: center;
  flex-wrap: wrap;
  gap: var(--spacing-md);
}

.project-title {
  margin: 0;
  font-size: var(--font-size-4xl);
  font-weight: var(--font-weight-bold);
  color: var(--color-text-primary);
}

.project-badges {
  display: flex;
  gap: var(--spacing-sm);
}

.oem-badge {
  background: var(--color-bg-success);
  color: var(--color-accent-primary);
  padding: 6px 16px;
  border-radius: var(--radius-full);
  font-size: var(--font-size-md);
  font-weight: var(--font-weight-semibold);
}

/* Info Section */
.info-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(280px, 1fr));
  gap: var(--spacing-md);
}

.info-card {
  background: var(--color-bg-secondary);
  border-radius: var(--radius-md);
  padding: var(--spacing-lg);
  border: 1px solid var(--color-border-light);
}

.info-card h3 {
  margin: 0 0 var(--spacing-md);
  font-size: var(--font-size-md);
  font-weight: var(--font-weight-semibold);
  color: var(--color-text-secondary);
  display: flex;
  align-items: center;
  gap: var(--spacing-sm);
}

.tl-info {
  display: flex;
  align-items: center;
  gap: var(--spacing-sm);
}

.tl-details {
  display: flex;
  flex-direction: column;
}

.tl-name {
  font-weight: var(--font-weight-semibold);
  color: var(--color-text-primary);
}

.tl-email {
  font-size: var(--font-size-xs);
  color: var(--color-text-muted);
}

.members-list {
  display: flex;
  align-items: center;
  gap: var(--spacing-sm);
}

.member-count {
  font-size: var(--font-size-md);
  color: var(--color-text-secondary);
}

.config-info {
  display: flex;
  flex-direction: column;
  gap: var(--spacing-sm);
}

.config-item {
  display: flex;
  align-items: center;
  gap: var(--spacing-sm);
  font-size: var(--font-size-md);
  color: var(--color-text-secondary);
}

.config-label {
  color: var(--color-text-muted);
}

/* Layers Section */
.layers-section {
  background: var(--color-bg-secondary);
  border-radius: var(--radius-lg);
  padding: var(--spacing-lg);
  border: 1px solid var(--color-border-light);
}

.layers-section h2 {
  margin: 0 0 var(--spacing-lg);
  font-size: var(--font-size-2xl);
  font-weight: var(--font-weight-semibold);
  color: var(--color-text-primary);
}

.layer-content {
  display: flex;
  flex-direction: column;
  gap: var(--spacing-lg);
}

.layer-info {
  display: flex;
  align-items: center;
  gap: var(--spacing-md);
}

.layer-type {
  padding: 4px 12px;
  border-radius: var(--radius-full);
  font-size: var(--font-size-xs);
  font-weight: var(--font-weight-semibold);
  text-transform: uppercase;
}

.layer-type.release {
  background: var(--color-bg-indigo);
  color: var(--color-accent-secondary);
}

.layer-type.custom {
  background: var(--color-bg-info);
  color: var(--color-accent-primary);
}

.pipeline-stages {
  font-size: var(--font-size-md);
  color: var(--color-text-secondary);
}

.pipeline-config {
  background: rgba(255, 255, 255, 0.02);
  border-radius: var(--radius-md);
  padding: var(--spacing-md);
}

.pipeline-config h4 {
  margin: 0 0 var(--spacing-sm);
  font-size: var(--font-size-md);
  font-weight: var(--font-weight-semibold);
  color: var(--color-text-secondary);
}

.stage-chips {
  display: flex;
  flex-wrap: wrap;
  gap: var(--spacing-sm);
}

.builds-section h4 {
  margin: 0 0 var(--spacing-md);
  font-size: var(--font-size-lg);
  font-weight: var(--font-weight-semibold);
  color: var(--color-text-primary);
}

.builds-list {
  display: flex;
  flex-direction: column;
  gap: var(--spacing-md);
}
</style>
