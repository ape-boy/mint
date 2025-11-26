<script setup lang="ts">
import { computed } from 'vue'
import { useStatus } from '@/composables'
import type { BuildStatus, BuildStageStatus, ProjectStatus, TRStatus } from '@/types'

type StatusType = 'build' | 'stage' | 'project' | 'tr'

const props = defineProps<{
  status: BuildStatus | BuildStageStatus | ProjectStatus | TRStatus
  type?: StatusType
  showIcon?: boolean
  showLabel?: boolean
  size?: 'small' | 'default' | 'large'
}>()

const { getBuildStatusConfig, getStageStatusConfig, getProjectStatusConfig, getTRStatusConfig } = useStatus()

const config = computed(() => {
  switch (props.type) {
    case 'stage':
      return getStageStatusConfig(props.status as BuildStageStatus)
    case 'project':
      return getProjectStatusConfig(props.status as ProjectStatus)
    case 'tr':
      return getTRStatusConfig(props.status as TRStatus)
    case 'build':
    default:
      return getBuildStatusConfig(props.status as BuildStatus)
  }
})

const sizeClass = computed(() => {
  switch (props.size) {
    case 'small': return 'status-badge--small'
    case 'large': return 'status-badge--large'
    default: return ''
  }
})

const showIconFinal = computed(() => props.showIcon ?? true)
const showLabelFinal = computed(() => props.showLabel ?? true)
</script>

<template>
  <span
    class="status-badge"
    :class="[`status-badge--${status}`, sizeClass]"
    :style="{
      backgroundColor: config.bgColor,
      color: config.color,
    }"
  >
    <component
      v-if="showIconFinal"
      :is="config.icon"
      class="status-badge__icon"
      :spin="status === 'running'"
    />
    <span v-if="showLabelFinal" class="status-badge__label">{{ config.label }}</span>
  </span>
</template>

<style scoped>
.status-badge {
  display: inline-flex;
  align-items: center;
  gap: 6px;
  padding: 4px 12px;
  border-radius: var(--radius-full);
  font-size: var(--font-size-xs);
  font-weight: var(--font-weight-semibold);
  white-space: nowrap;
}

.status-badge--small {
  padding: 2px 8px;
  font-size: 11px;
  gap: 4px;
}

.status-badge--large {
  padding: 6px 16px;
  font-size: var(--font-size-sm);
  gap: 8px;
}

.status-badge__icon {
  font-size: 1em;
}

.status-badge__label {
  text-transform: capitalize;
}
</style>
