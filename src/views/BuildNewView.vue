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
  await projectGroupStore.fetchProjectGroups()
  await projectStore.fetchProjects()
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
    router.push('/build/3001') // Redirect to a build detail
  }, 1000)
}
</script>

<template>
  <div class="build-new-page">
    <div class="page-header">
      <h1>New Build</h1>
      <p class="page-description">Configure and trigger a new build pipeline.</p>
    </div>

    <div class="build-form-container">
      <!-- Project Selection -->
      <div class="form-section">
        <h3><rocket-outlined /> Project & Layer Selection</h3>
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
                {{ layer.name }} <span class="layer-type-badge">{{ layer.type }}</span>
              </a-select-option>
            </a-select>
          </div>
        </div>
      </div>

      <!-- SCM Configuration -->
      <div class="form-section" v-if="selectedLayerId">
        <h3><branches-outlined /> Source Control</h3>
        <div class="form-row">
          <div class="form-group">
            <label>Branch</label>
            <a-input v-model:value="branch" placeholder="e.g. main, develop">
              <template #prefix><branches-outlined /></template>
            </a-input>
          </div>
          <div class="form-group">
            <label>Commit Hash (Optional)</label>
            <a-input v-model:value="commitHash" placeholder="HEAD">
              <template #prefix><history-outlined /></template>
            </a-input>
          </div>
        </div>
      </div>

      <!-- Build Options -->
      <div class="form-section" v-if="selectedLayerId">
        <h3><setting-outlined /> Build Options</h3>
        <div class="checkbox-group">
          <a-checkbox v-model:checked="buildOptions.cleanBuild">Clean Build</a-checkbox>
          <a-checkbox v-model:checked="buildOptions.verboseLogging">Verbose Logging</a-checkbox>
          <a-checkbox v-model:checked="buildOptions.skipTests">Skip Tests</a-checkbox>
        </div>
      </div>

      <!-- Pipeline Configuration -->
      <div class="form-section" v-if="selectedLayerId && pipelineStages.length > 0">
        <h3><setting-outlined /> Pipeline Configuration</h3>
        <div class="pipeline-config-grid">
          <div v-for="stage in pipelineStages" :key="stage.name" class="stage-checkbox">
            <a-checkbox v-model:checked="stage.enabled" :disabled="stage.required">
              {{ stage.name }}
              <span v-if="stage.required" class="required-tag">(Required)</span>
            </a-checkbox>
          </div>
        </div>
      </div>

      <!-- Action -->
      <div class="form-actions" v-if="selectedLayerId">
        <a-button type="primary" size="large" @click="runBuild" class="run-btn">
          <template #icon><play-circle-outlined /></template>
          Run Build
        </a-button>
      </div>
    </div>
  </div>
</template>

<style scoped>
.build-new-page {
  max-width: 800px;
  margin: 0 auto;
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

.build-form-container {
  display: flex;
  flex-direction: column;
  gap: 24px;
}

.form-section {
  background: var(--bg-secondary);
  border: 1px solid rgba(255, 255, 255, 0.05);
  border-radius: 16px;
  padding: 24px;
}

.form-section h3 {
  margin: 0 0 20px;
  font-size: 18px;
  display: flex;
  align-items: center;
  gap: 8px;
  color: var(--text-primary);
  border-bottom: 1px solid rgba(255, 255, 255, 0.05);
  padding-bottom: 12px;
}

.form-row {
  display: flex;
  gap: 20px;
  margin-bottom: 16px;
}

.form-row:last-child {
  margin-bottom: 0;
}

.form-group {
  flex: 1;
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.form-group.full-width {
  flex: 100%;
}

.form-group label {
  font-size: 14px;
  font-weight: 500;
  color: var(--text-secondary);
}

.layer-type-badge {
  background: rgba(255, 255, 255, 0.1);
  padding: 2px 6px;
  border-radius: 4px;
  font-size: 10px;
  text-transform: uppercase;
  margin-left: 8px;
}

.checkbox-group {
  display: flex;
  gap: 24px;
}

.pipeline-config-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(200px, 1fr));
  gap: 16px;
}

.stage-checkbox {
  background: rgba(255, 255, 255, 0.02);
  padding: 12px;
  border-radius: 8px;
  border: 1px solid rgba(255, 255, 255, 0.05);
}

.required-tag {
  font-size: 12px;
  color: var(--text-muted);
  margin-left: 4px;
}

.form-actions {
  display: flex;
  justify-content: flex-end;
}

.run-btn {
  width: 200px;
  height: 48px;
  font-size: 16px;
  font-weight: 600;
}
</style>
