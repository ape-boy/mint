<script setup lang="ts">
import type { PipelineStageConfig } from '@/types'

const props = defineProps<{
  stage: PipelineStageConfig
}>()
</script>

<template>
  <span class="stage-chip" :class="{ required: stage.required }">
    {{ stage.name }}
    <span v-if="stage.required" class="required-mark">*</span>
  </span>
</template>

<style scoped>
.stage-chip {
  padding: 6px 12px;
  background: rgba(255, 255, 255, 0.05);
  border-radius: var(--radius-lg);
  font-size: var(--font-size-xs);
  color: var(--color-text-secondary);
  display: inline-flex;
  align-items: center;
  gap: 4px;
}

.stage-chip.required {
  background: var(--color-bg-indigo);
  color: var(--color-accent-secondary);
}

.required-mark {
  color: var(--color-accent-danger);
}
</style>
