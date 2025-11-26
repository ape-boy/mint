<script setup lang="ts">
import { ref, computed, onMounted, watch } from 'vue'
import { useRouter } from 'vue-router'
import {
  RocketOutlined,
  SettingOutlined,
  BranchesOutlined,
  HistoryOutlined,
  PlayCircleOutlined
} from '@ant-design/icons-vue'
import { PageHeader, EmptyState } from '@/components'
import { useProjectStore } from '@/stores/project'
import { useProjectGroupStore } from '@/stores/projectGroup'
import { useLayerStore } from '@/stores/layer'
import type { PipelineStageConfig } from '@/types'

const router = useRouter()
const projectStore = useProjectStore()
const projectGroupStore = useProjectGroupStore()
const layerStore = useLayerStore()

// Form State
const selectedGroupId = ref<string | null>(null)
const selectedProjectId = ref<string | null>(null)
const selectedLayerId = ref<string | null>(null)
const branch = ref('main')
const commitHash = ref('')
const buildOptions = ref({
  cleanBuild: false,
  verboseLogging: false,
  skipTests: false
})

const pipelineStages = ref<PipelineStageConfig[]>([])

onMounted(async () => {
  await Promise.all([
    projectGroupStore.fetchProjectGroups(),
    projectStore.fetchProjects()
  ])
})

// Computed
const filteredProjects = computed(() => {
  if (!selectedGroupId.value) return []
  return projectStore.projects.filter((p) => p.groupId === selectedGroupId.value)
})

const filteredLayers = computed(() => {
  if (!selectedProjectId.value) return []
  return layerStore.layers.filter((l) => l.projectId === selectedProjectId.value)
})

const currentLayer = computed(() => {
  return filteredLayers.value.find((l) => l.id === selectedLayerId.value)
})

// Watchers
watch(selectedProjectId, async (newProjectId) => {
  if (newProjectId) {
    await layerStore.fetchLayers(newProjectId)
  }
  selectedLayerId.value = null
  pipelineStages.value = []
})

watch(selectedLayerId, () => {
  if (currentLayer.value && currentLayer.value.pipelineConfig) {
    pipelineStages.value = JSON.parse(JSON.stringify(currentLayer.value.pipelineConfig))
  } else {
    pipelineStages.value = []
  }
})

function runBuild() {
  console.log('Running build with:', {
    projectId: selectedProjectId.value,
    layerId: selectedLayerId.value,
    branch: branch.value,
    commit: commitHash.value || 'HEAD',
    options: buildOptions.value,
    stages: pipelineStages.value.filter((s) => s.enabled)
  })

  // Simulate API call and redirect
  setTimeout(() => {
    router.push('/build/3001')
  }, 1000)
}
</script>

<template>
  <div class="build-new-view">
    <PageHeader title="New Build" description="Configure and trigger a new build pipeline." />

    <div class="build-form-container">
      <!-- Project Selection -->
      <div class="form-section">
        <h3><RocketOutlined /> Project & Layer Selection</h3>
        <div class="form-row">
          <div class="form-group">
            <label>Project Group</label>
            <a-select
              v-model:value="selectedGroupId"
              placeholder="Select Group"
              style="width: 100%"
              @change="selectedProjectId = null"
            >
              <a-select-option v-for="group in projectGroupStore.projectGroups" :key="group.id" :value="group.id">
                {{ group.name }}
              </a-select-option>
            </a-select>
          </div>
          <div class="form-group">
            <label>Project</label>
            <a-select
              v-model:value="selectedProjectId"
              placeholder="Select Project"
              style="width: 100%"
              :disabled="!selectedGroupId"
            >
              <a-select-option v-for="project in filteredProjects" :key="project.id" :value="project.id">
                {{ project.name }}
              </a-select-option>
            </a-select>
          </div>
        </div>
        <div class="form-row">
          <div class="form-group full-width">
            <label>Layer</label>
            <a-select
              v-model:value="selectedLayerId"
              placeholder="Select Build Layer"
              style="width: 100%"
              :disabled="!selectedProjectId"
            >
              <a-select-option v-for="layer in filteredLayers" :key="layer.id" :value="layer.id">
                {{ layer.name }}
                <span class="layer-type-badge">{{ layer.type }}</span>
              </a-select-option>
            </a-select>
          </div>
        </div>
      </div>

      <template v-if="selectedLayerId">
        <!-- SCM Configuration -->
        <div class="form-section">
          <h3><BranchesOutlined /> Source Control</h3>
          <div class="form-row">
            <div class="form-group">
              <label>Branch</label>
              <a-input v-model:value="branch" placeholder="e.g. main, develop">
                <template #prefix><BranchesOutlined /></template>
              </a-input>
            </div>
            <div class="form-group">
              <label>Commit Hash (Optional)</label>
              <a-input v-model:value="commitHash" placeholder="HEAD">
                <template #prefix><HistoryOutlined /></template>
              </a-input>
            </div>
          </div>
        </div>

        <!-- Build Options -->
        <div class="form-section">
          <h3><SettingOutlined /> Build Options</h3>
          <div class="checkbox-group">
            <a-checkbox v-model:checked="buildOptions.cleanBuild">Clean Build</a-checkbox>
            <a-checkbox v-model:checked="buildOptions.verboseLogging">Verbose Logging</a-checkbox>
            <a-checkbox v-model:checked="buildOptions.skipTests">Skip Tests</a-checkbox>
          </div>
        </div>

        <!-- Pipeline Configuration -->
        <div v-if="pipelineStages.length > 0" class="form-section">
          <h3><SettingOutlined /> Pipeline Configuration</h3>
          <div class="pipeline-config-grid">
            <div v-for="stage in pipelineStages" :key="stage.name" class="stage-checkbox">
              <a-checkbox v-model:checked="stage.enabled" :disabled="stage.required">
                {{ stage.name }}
                <span v-if="stage.required" class="required-tag">(Required)</span>
              </a-checkbox>
            </div>
          </div>
        </div>
        <EmptyState
          v-else
          title="No pipeline configuration"
          description="This layer has no pipeline stages configured"
        />

        <!-- Action -->
        <div class="form-actions">
          <a-button type="primary" size="large" @click="runBuild" class="run-btn">
            <template #icon><PlayCircleOutlined /></template>
            Run Build
          </a-button>
        </div>
      </template>
    </div>
  </div>
</template>

<style scoped>
.build-new-view {
  max-width: 800px;
  margin: 0 auto;
  display: flex;
  flex-direction: column;
  gap: var(--spacing-lg);
}

.build-form-container {
  display: flex;
  flex-direction: column;
  gap: var(--spacing-lg);
}

.form-section {
  background: var(--color-bg-secondary);
  border: 1px solid var(--color-border-light);
  border-radius: var(--radius-lg);
  padding: var(--spacing-lg);
}

.form-section h3 {
  margin: 0 0 var(--spacing-lg);
  font-size: var(--font-size-lg);
  font-weight: var(--font-weight-semibold);
  display: flex;
  align-items: center;
  gap: var(--spacing-sm);
  color: var(--color-text-primary);
  border-bottom: 1px solid var(--color-border-light);
  padding-bottom: var(--spacing-sm);
}

.form-row {
  display: flex;
  gap: var(--spacing-lg);
  margin-bottom: var(--spacing-md);
}

.form-row:last-child {
  margin-bottom: 0;
}

.form-group {
  flex: 1;
  display: flex;
  flex-direction: column;
  gap: var(--spacing-xs);
}

.form-group.full-width {
  flex: 100%;
}

.form-group label {
  font-size: var(--font-size-sm);
  font-weight: var(--font-weight-medium);
  color: var(--color-text-secondary);
}

.layer-type-badge {
  background: rgba(255, 255, 255, 0.1);
  padding: 2px 6px;
  border-radius: var(--radius-sm);
  font-size: var(--font-size-xs);
  text-transform: uppercase;
  margin-left: var(--spacing-sm);
}

.checkbox-group {
  display: flex;
  gap: var(--spacing-lg);
}

.pipeline-config-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(200px, 1fr));
  gap: var(--spacing-md);
}

.stage-checkbox {
  background: rgba(255, 255, 255, 0.02);
  padding: var(--spacing-sm);
  border-radius: var(--radius-md);
  border: 1px solid var(--color-border-light);
}

.required-tag {
  font-size: var(--font-size-xs);
  color: var(--color-text-muted);
  margin-left: var(--spacing-xs);
}

.form-actions {
  display: flex;
  justify-content: flex-end;
}

.run-btn {
  width: 200px;
  height: 48px;
  font-size: var(--font-size-md);
  font-weight: var(--font-weight-semibold);
}
</style>
