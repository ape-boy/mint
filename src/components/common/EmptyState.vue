<script setup lang="ts">
import { InboxOutlined } from '@ant-design/icons-vue'

defineProps<{
  title?: string
  description?: string
  icon?: boolean
}>()
</script>

<template>
  <div class="empty-state">
    <div v-if="icon !== false" class="empty-state__icon">
      <slot name="icon">
        <InboxOutlined />
      </slot>
    </div>
    <h3 v-if="title" class="empty-state__title">{{ title }}</h3>
    <p v-if="description" class="empty-state__description">{{ description }}</p>
    <div v-if="$slots.default" class="empty-state__actions">
      <slot />
    </div>
  </div>
</template>

<style scoped>
.empty-state {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: var(--spacing-2xl);
  text-align: center;
  min-height: 200px;
}

.empty-state__icon {
  font-size: 48px;
  color: var(--color-text-muted);
  margin-bottom: var(--spacing-md);
  opacity: 0.5;
}

.empty-state__title {
  margin: 0 0 var(--spacing-sm);
  font-size: var(--font-size-lg);
  font-weight: var(--font-weight-semibold);
  color: var(--color-text-primary);
}

.empty-state__description {
  margin: 0 0 var(--spacing-lg);
  color: var(--color-text-muted);
  font-size: var(--font-size-md);
  max-width: 300px;
}

.empty-state__actions {
  margin-top: var(--spacing-md);
}
</style>
