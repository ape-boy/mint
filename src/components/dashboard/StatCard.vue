<script setup lang="ts">
import { type Component } from 'vue'

const props = defineProps<{
  title: string
  value: string | number
  trend?: string
  status?: 'success' | 'info' | 'warning' | 'danger'
  icon: Component
}>()

const trendClass = (trend?: string) => {
  if (!trend) return ''
  if (trend.startsWith('+')) return 'positive'
  if (trend.startsWith('-')) return 'negative'
  return ''
}
</script>

<template>
  <div class="stat-card">
    <div class="stat-card__icon" :class="status">
      <component :is="icon" />
    </div>
    <div class="stat-card__info">
      <span class="stat-card__title">{{ title }}</span>
      <div class="stat-card__value-wrapper">
        <span class="stat-card__value">{{ value }}</span>
        <span v-if="trend" class="stat-card__trend" :class="trendClass(trend)">
          {{ trend }}
        </span>
      </div>
    </div>
  </div>
</template>

<style scoped>
.stat-card {
  padding: var(--spacing-lg);
  display: flex;
  align-items: center;
  gap: var(--spacing-lg);
  transition: transform var(--transition-normal), box-shadow var(--transition-normal);
  background: var(--color-bg-secondary);
  border-radius: var(--radius-lg);
  border: 1px solid var(--color-border-light);
}

.stat-card:hover {
  transform: translateY(-5px);
  box-shadow: var(--shadow-md);
}

.stat-card__icon {
  width: 56px;
  height: 56px;
  border-radius: var(--radius-lg);
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 24px;
  flex-shrink: 0;
}

.stat-card__icon.success {
  background: var(--color-bg-success);
  color: var(--color-accent-success);
}

.stat-card__icon.info {
  background: var(--color-bg-info);
  color: var(--color-accent-secondary);
}

.stat-card__icon.warning {
  background: var(--color-bg-warning);
  color: var(--color-accent-warning);
}

.stat-card__icon.danger {
  background: var(--color-bg-danger);
  color: var(--color-accent-danger);
}

.stat-card__info {
  flex: 1;
  min-width: 0;
}

.stat-card__title {
  color: var(--color-text-muted);
  font-size: var(--font-size-md);
  display: block;
  margin-bottom: 4px;
  font-weight: var(--font-weight-medium);
}

.stat-card__value-wrapper {
  display: flex;
  align-items: baseline;
  gap: var(--spacing-sm);
}

.stat-card__value {
  font-size: var(--font-size-4xl);
  font-weight: var(--font-weight-bold);
  color: var(--color-text-primary);
}

.stat-card__trend {
  font-size: var(--font-size-sm);
  font-weight: var(--font-weight-semibold);
  color: var(--color-text-muted);
}

.stat-card__trend.positive {
  color: var(--color-accent-success);
}

.stat-card__trend.negative {
  color: var(--color-accent-danger);
}
</style>
