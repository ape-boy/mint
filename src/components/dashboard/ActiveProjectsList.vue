<script setup lang="ts">
import { RocketOutlined } from '@ant-design/icons-vue'
import { EmptyState } from '@/components/common'
import type { Project } from '@/types'

const props = defineProps<{
  projects: Project[]
}>()

const emit = defineEmits<{
  viewAll: []
  viewProject: [projectId: string]
}>()
</script>

<template>
  <div class="active-projects">
    <div class="section-header">
      <h3><RocketOutlined /> Active Projects</h3>
    </div>
    <div v-if="projects.length > 0" class="projects-list">
      <div
        v-for="project in projects"
        :key="project.id"
        class="project-item"
        @click="emit('viewProject', project.id)"
      >
        <div class="project-icon">
          <span class="project-initial">{{ project.name.charAt(0) }}</span>
        </div>
        <div class="project-content">
          <p class="project-name">{{ project.name }}</p>
          <span class="project-meta">{{ project.oem }} • {{ project.tl?.name }}</span>
        </div>
      </div>
    </div>
    <EmptyState v-else title="No projects" description="Create your first project to get started" />
    <div class="projects-action">
      <a-button type="primary" block @click="emit('viewAll')">
        View All Projects
      </a-button>
    </div>
  </div>
</template>

<style scoped>
.active-projects {
  padding: var(--spacing-lg);
  background: var(--color-bg-secondary);
  border-radius: var(--radius-lg);
  border: 1px solid var(--color-border-light);
  display: flex;
  flex-direction: column;
}

.section-header {
  margin-bottom: var(--spacing-lg);
}

.section-header h3 {
  margin: 0;
  font-size: var(--font-size-xl);
  font-weight: var(--font-weight-semibold);
  color: var(--color-text-primary);
  display: flex;
  align-items: center;
  gap: var(--spacing-sm);
}

.projects-list {
  display: flex;
  flex-direction: column;
  gap: var(--spacing-sm);
  flex: 1;
  margin-bottom: var(--spacing-lg);
}

.project-item {
  display: flex;
  gap: var(--spacing-sm);
  padding: var(--spacing-sm) var(--spacing-md);
  background: rgba(255, 255, 255, 0.02);
  border: 1px solid var(--color-border-light);
  border-radius: var(--radius-sm);
  cursor: pointer;
  transition: background var(--transition-fast);
  align-items: center;
}

.project-item:hover {
  background: rgba(255, 255, 255, 0.05);
}

.project-icon {
  width: 40px;
  height: 40px;
  border-radius: var(--radius-sm);
  background: var(--color-bg-indigo);
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
}

.project-initial {
  font-weight: var(--font-weight-bold);
  font-size: var(--font-size-lg);
  color: var(--color-accent-secondary);
}

.project-content {
  flex: 1;
  display: flex;
  flex-direction: column;
  justify-content: center;
  min-width: 0;
}

.project-content p {
  margin: 0;
  font-size: var(--font-size-md);
  font-weight: var(--font-weight-semibold);
  color: var(--color-text-primary);
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.project-meta {
  font-size: var(--font-size-xs);
  color: var(--color-text-muted);
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.projects-action {
  margin-top: auto;
}
</style>
