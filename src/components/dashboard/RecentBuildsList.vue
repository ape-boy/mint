<script setup lang="ts">
import { ArrowRightOutlined } from '@ant-design/icons-vue'
import { StatusBadge, EmptyState } from '@/components/common'
import { useFormat } from '@/composables'
import type { Build } from '@/types'

const props = defineProps<{
  builds: Build[]
  getProjectName: (projectId: string) => string
}>()

const emit = defineEmits<{
  viewAll: []
  viewBuild: [buildId: string]
}>()

const { formatTimeAgo } = useFormat()
</script>

<template>
  <div class="recent-builds">
    <div class="section-header">
      <h3>Recent Builds</h3>
      <a-button type="link" @click="emit('viewAll')">View All</a-button>
    </div>
    <div v-if="builds.length > 0" class="build-list">
      <div
        v-for="build in builds"
        :key="build.id"
        class="build-item"
        @click="emit('viewBuild', build.id)"
      >
        <div class="build-status">
          <StatusBadge :status="build.status" type="build" :show-label="false" />
        </div>
        <div class="build-details">
          <span class="build-name">
            {{ getProjectName(build.projectId) }} #{{ build.buildNumber }}
          </span>
          <span class="build-meta">
            {{ build.branch }} • {{ formatTimeAgo(build.startedAt) }} • {{ build.triggeredBy }}
          </span>
        </div>
        <div class="build-action">
          <ArrowRightOutlined />
        </div>
      </div>
    </div>
    <EmptyState v-else title="No recent builds" description="Builds will appear here once they are triggered" />
  </div>
</template>

<style scoped>
.recent-builds {
  padding: var(--spacing-lg);
  background: var(--color-bg-secondary);
  border-radius: var(--radius-lg);
  border: 1px solid var(--color-border-light);
}

.section-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: var(--spacing-lg);
}

.section-header h3 {
  margin: 0;
  font-size: var(--font-size-xl);
  font-weight: var(--font-weight-semibold);
  color: var(--color-text-primary);
}

.build-list {
  display: flex;
  flex-direction: column;
  gap: var(--spacing-sm);
}

.build-item {
  display: flex;
  align-items: center;
  padding: var(--spacing-sm) var(--spacing-md);
  border-radius: var(--radius-sm);
  background: rgba(255, 255, 255, 0.02);
  border: 1px solid var(--color-border-light);
  transition: background var(--transition-fast);
  cursor: pointer;
}

.build-item:hover {
  background: rgba(255, 255, 255, 0.05);
}

.build-status {
  margin-right: var(--spacing-md);
}

.build-details {
  flex: 1;
  display: flex;
  flex-direction: column;
  min-width: 0;
}

.build-name {
  font-weight: var(--font-weight-semibold);
  color: var(--color-text-primary);
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.build-meta {
  font-size: var(--font-size-xs);
  color: var(--color-text-muted);
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.build-action {
  color: var(--color-text-muted);
  flex-shrink: 0;
}
</style>
