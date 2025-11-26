<script setup lang="ts">
import { BranchesOutlined } from '@ant-design/icons-vue'
import { StatusBadge } from '@/components/common'
import { useStatus, useFormat } from '@/composables'
import type { Build, Layer } from '@/types'

const props = defineProps<{
  build: Build
  layer?: Layer
  showTrStatus?: boolean
}>()

const emit = defineEmits<{
  click: []
}>()

const { getStageStatusColor } = useStatus()
const { formatDuration, formatDateTime } = useFormat()
</script>

<template>
  <div class="build-card" @click="emit('click')">
    <div class="build-card__header">
      <div class="build-card__info">
        <StatusBadge :status="build.status" type="build" :show-label="false" />
        <span class="build-number">#{{ build.buildNumber }}</span>
        <span class="build-branch">
          <BranchesOutlined /> {{ build.branch }}
        </span>
      </div>
      <div class="build-card__meta">
        <span class="build-time">{{ formatDateTime(build.startedAt) }}</span>
        <span v-if="build.duration" class="build-duration">
          {{ formatDuration(build.duration) }}
        </span>
      </div>
    </div>

    <!-- Pipeline Stages -->
    <div class="build-card__stages">
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

    <!-- TR Status -->
    <div v-if="showTrStatus && layer?.type === 'release' && build.trStatus" class="build-card__tr">
      <span class="tr-label">TR Status:</span>
      <StatusBadge :status="build.trStatus" type="tr" size="small" />
    </div>

    <!-- Artifacts -->
    <div v-if="build.artifacts" class="build-card__artifacts">
      <a-button
        v-if="build.artifacts.binaryUrl"
        size="small"
        type="primary"
        ghost
        @click.stop
      >
        Download Binary
      </a-button>
      <a-button v-if="build.artifacts.logsUrl" size="small" @click.stop>
        View Logs
      </a-button>
    </div>
  </div>
</template>

<style scoped>
.build-card {
  background: rgba(255, 255, 255, 0.02);
  border-radius: var(--radius-md);
  padding: var(--spacing-md);
  border: 1px solid var(--color-border-light);
  cursor: pointer;
  transition: background var(--transition-fast);
}

.build-card:hover {
  background: rgba(255, 255, 255, 0.04);
}

.build-card__header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: var(--spacing-md);
}

.build-card__info {
  display: flex;
  align-items: center;
  gap: var(--spacing-sm);
}

.build-number {
  font-weight: var(--font-weight-bold);
  font-size: var(--font-size-lg);
  color: var(--color-text-primary);
}

.build-branch {
  display: flex;
  align-items: center;
  gap: 4px;
  font-size: var(--font-size-md);
  color: var(--color-text-secondary);
}

.build-card__meta {
  display: flex;
  align-items: center;
  gap: var(--spacing-md);
  font-size: var(--font-size-sm);
  color: var(--color-text-muted);
}

.build-card__stages {
  display: flex;
  flex-wrap: wrap;
  gap: var(--spacing-sm);
  margin-bottom: var(--spacing-sm);
}

.stage-block {
  padding: var(--spacing-sm) var(--spacing-md);
  background: rgba(0, 0, 0, 0.2);
  border-radius: var(--radius-sm);
  border-left: 3px solid;
  display: flex;
  flex-direction: column;
  min-width: 80px;
}

.stage-name {
  font-size: var(--font-size-xs);
  font-weight: var(--font-weight-medium);
  color: var(--color-text-primary);
}

.stage-duration {
  font-size: 11px;
  color: var(--color-text-muted);
}

.build-card__tr {
  display: flex;
  align-items: center;
  gap: var(--spacing-sm);
  margin-bottom: var(--spacing-sm);
}

.tr-label {
  font-size: var(--font-size-sm);
  color: var(--color-text-secondary);
}

.build-card__artifacts {
  display: flex;
  gap: var(--spacing-sm);
  padding-top: var(--spacing-sm);
  border-top: 1px solid var(--color-border-light);
}
</style>
