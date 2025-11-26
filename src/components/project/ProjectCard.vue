<script setup lang="ts">
import { UserOutlined, TeamOutlined } from '@ant-design/icons-vue'
import { StatusBadge } from '@/components/common'
import { useFormat } from '@/composables'
import type { Project } from '@/types'

const props = defineProps<{
  project: Project
  groupName?: string
}>()

const emit = defineEmits<{
  click: []
  viewBuilds: []
}>()

const { formatDate } = useFormat()
</script>

<template>
  <div class="project-card" @click="emit('click')">
    <div class="project-card__header">
      <StatusBadge :status="project.status" type="project" :show-label="false" size="small" />
      <span v-if="groupName" class="project-card__group">{{ groupName }}</span>
    </div>

    <div class="project-card__body">
      <h4 class="project-card__name">{{ project.name }}</h4>
      <div class="project-card__meta">
        <span class="project-card__oem">{{ project.oem }}</span>
      </div>
      <div class="project-card__team">
        <div v-if="project.tl" class="project-card__tl">
          <UserOutlined />
          <span>{{ project.tl.name }}</span>
        </div>
        <div v-if="project.members" class="project-card__members">
          <TeamOutlined />
          <span>{{ project.members.length }} members</span>
        </div>
      </div>
    </div>

    <div class="project-card__footer">
      <span class="project-card__date">
        Updated: {{ formatDate(project.updatedAt) }}
      </span>
      <a-button type="primary" size="small" ghost @click.stop="emit('viewBuilds')">
        View Builds
      </a-button>
    </div>
  </div>
</template>

<style scoped>
.project-card {
  background: var(--color-bg-secondary);
  border: 1px solid var(--color-border-light);
  border-radius: var(--radius-lg);
  padding: var(--spacing-lg);
  cursor: pointer;
  transition: all var(--transition-normal);
  display: flex;
  flex-direction: column;
}

.project-card:hover {
  transform: translateY(-4px);
  box-shadow: var(--shadow-lg);
  border-color: rgba(255, 255, 255, 0.1);
}

.project-card__header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: var(--spacing-md);
}

.project-card__group {
  background: var(--color-bg-indigo);
  color: var(--color-accent-secondary);
  padding: 4px 12px;
  border-radius: var(--radius-full);
  font-size: var(--font-size-xs);
  font-weight: var(--font-weight-semibold);
}

.project-card__body {
  flex: 1;
  margin-bottom: var(--spacing-md);
}

.project-card__name {
  margin: 0 0 var(--spacing-sm);
  font-size: var(--font-size-lg);
  font-weight: var(--font-weight-semibold);
  color: var(--color-text-primary);
}

.project-card__meta {
  display: flex;
  gap: var(--spacing-sm);
  margin-bottom: var(--spacing-sm);
}

.project-card__oem {
  background: var(--color-bg-success);
  color: var(--color-accent-primary);
  padding: 2px 10px;
  border-radius: var(--radius-full);
  font-size: var(--font-size-xs);
  font-weight: var(--font-weight-medium);
}

.project-card__team {
  display: flex;
  flex-direction: column;
  gap: 6px;
}

.project-card__tl,
.project-card__members {
  display: flex;
  align-items: center;
  gap: var(--spacing-sm);
  font-size: var(--font-size-sm);
  color: var(--color-text-secondary);
}

.project-card__tl :deep(.anticon),
.project-card__members :deep(.anticon) {
  color: var(--color-text-muted);
}

.project-card__footer {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding-top: var(--spacing-md);
  border-top: 1px solid var(--color-border-light);
}

.project-card__date {
  font-size: var(--font-size-xs);
  color: var(--color-text-muted);
}
</style>
