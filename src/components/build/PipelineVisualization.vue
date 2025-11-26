<script setup lang="ts">
import { useStatus, useFormat } from '@/composables'
import type { BuildStage } from '@/types'

const props = defineProps<{
  stages: BuildStage[]
  compact?: boolean
}>()

const { getStageStatusConfig } = useStatus()
const { formatDuration } = useFormat()
</script>

<template>
  <div class="pipeline" :class="{ 'pipeline--compact': compact }">
    <div v-for="(stage, index) in stages" :key="stage.name" class="pipeline__stage">
      <div class="stage-node" :class="stage.status">
        <div class="stage-icon">
          <component
            :is="getStageStatusConfig(stage.status).icon"
            :spin="stage.status === 'running'"
          />
        </div>
        <div class="stage-info">
          <span class="stage-name">{{ stage.name }}</span>
          <span class="stage-duration">{{ formatDuration(stage.duration) }}</span>
        </div>
      </div>
      <div
        v-if="index < stages.length - 1"
        class="stage-connector"
        :class="stage.status"
      />
    </div>
  </div>
</template>

<style scoped>
.pipeline {
  display: flex;
  align-items: center;
  padding: var(--spacing-lg) 0;
  min-width: max-content;
  overflow-x: auto;
}

.pipeline--compact {
  padding: var(--spacing-sm) 0;
}

.pipeline__stage {
  display: flex;
  align-items: center;
}

.stage-node {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: var(--spacing-sm);
  position: relative;
  z-index: 2;
  cursor: pointer;
  transition: transform var(--transition-fast);
}

.stage-node:hover {
  transform: scale(1.05);
}

.stage-icon {
  width: 48px;
  height: 48px;
  border-radius: 50%;
  background: var(--color-bg-tertiary);
  border: 2px solid var(--color-text-muted);
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 20px;
  color: var(--color-text-muted);
  transition: all var(--transition-normal);
}

.pipeline--compact .stage-icon {
  width: 36px;
  height: 36px;
  font-size: 16px;
}

.stage-node.success .stage-icon {
  border-color: var(--color-accent-success);
  color: var(--color-accent-success);
  background: var(--color-bg-success);
  box-shadow: 0 0 15px rgba(52, 211, 153, 0.3);
}

.stage-node.failed .stage-icon {
  border-color: var(--color-accent-danger);
  color: var(--color-accent-danger);
  background: var(--color-bg-danger);
  box-shadow: 0 0 15px rgba(248, 113, 113, 0.3);
}

.stage-node.running .stage-icon {
  border-color: var(--color-accent-primary);
  color: var(--color-accent-primary);
  background: var(--color-bg-info);
  box-shadow: 0 0 15px rgba(45, 212, 191, 0.3);
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

.pipeline--compact .stage-info {
  display: none;
}

.stage-name {
  font-weight: var(--font-weight-semibold);
  font-size: var(--font-size-md);
  white-space: nowrap;
  color: var(--color-text-primary);
}

.stage-duration {
  font-size: var(--font-size-xs);
  color: var(--color-text-muted);
}

.stage-connector {
  width: 60px;
  height: 2px;
  background: var(--color-text-muted);
  opacity: 0.3;
  margin: 0 var(--spacing-sm);
  margin-bottom: 24px;
}

.pipeline--compact .stage-connector {
  width: 30px;
  margin-bottom: 0;
}

.stage-connector.success {
  background: var(--color-accent-success);
  opacity: 1;
}
</style>
