<script setup lang="ts">
import { useStatus } from '@/composables'
import type { Milestone } from '@/types'

const props = defineProps<{
  milestones: Milestone[]
}>()

const { getMilestoneStatusConfig } = useStatus()
</script>

<template>
  <div class="milestones-timeline">
    <div
      v-for="milestone in milestones"
      :key="milestone.id"
      class="milestone-item"
      :class="milestone.status"
    >
      <component
        :is="getMilestoneStatusConfig(milestone.status).icon"
        :style="{ color: getMilestoneStatusConfig(milestone.status).color }"
        class="milestone-icon"
      />
      <div class="milestone-info">
        <span class="milestone-name">{{ milestone.name }}</span>
        <span class="milestone-date">{{ milestone.targetDate }}</span>
      </div>
    </div>
  </div>
</template>

<style scoped>
.milestones-timeline {
  display: flex;
  gap: var(--spacing-md);
  flex-wrap: wrap;
}

.milestone-item {
  display: flex;
  align-items: center;
  gap: var(--spacing-sm);
  padding: var(--spacing-sm) var(--spacing-md);
  background: rgba(255, 255, 255, 0.03);
  border-radius: var(--radius-sm);
  transition: background var(--transition-fast);
}

.milestone-item:hover {
  background: rgba(255, 255, 255, 0.05);
}

.milestone-icon {
  font-size: var(--font-size-lg);
}

.milestone-info {
  display: flex;
  flex-direction: column;
}

.milestone-name {
  font-weight: var(--font-weight-semibold);
  font-size: var(--font-size-md);
  color: var(--color-text-primary);
}

.milestone-date {
  font-size: var(--font-size-xs);
  color: var(--color-text-muted);
}
</style>
