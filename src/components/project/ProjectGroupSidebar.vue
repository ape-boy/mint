<script setup lang="ts">
import { FolderOutlined, AppstoreOutlined } from '@ant-design/icons-vue'
import type { ProjectGroup } from '@/types'

const props = defineProps<{
  groups: ProjectGroup[]
  selectedGroupId: string | null
  projectCountByGroup: Record<string, number>
  totalCount: number
}>()

const emit = defineEmits<{
  select: [groupId: string | null]
}>()
</script>

<template>
  <div class="groups-sidebar">
    <div class="sidebar-header">
      <h3><AppstoreOutlined /> Product Groups</h3>
    </div>
    <div class="group-list">
      <div
        class="group-item"
        :class="{ active: selectedGroupId === null }"
        @click="emit('select', null)"
      >
        <FolderOutlined />
        <span class="group-name">All Projects</span>
        <span class="group-count">{{ totalCount }}</span>
      </div>
      <div
        v-for="group in groups"
        :key="group.id"
        class="group-item"
        :class="{ active: selectedGroupId === group.id }"
        @click="emit('select', group.id)"
      >
        <FolderOutlined />
        <span class="group-name">{{ group.name }}</span>
        <span class="group-count">{{ projectCountByGroup[group.id] || 0 }}</span>
      </div>
    </div>
  </div>
</template>

<style scoped>
.groups-sidebar {
  background: var(--color-bg-secondary);
  border: 1px solid var(--color-border-light);
  border-radius: var(--radius-lg);
  padding: var(--spacing-lg);
  height: fit-content;
  position: sticky;
  top: var(--spacing-lg);
}

.sidebar-header h3 {
  margin: 0 0 var(--spacing-md);
  font-size: var(--font-size-lg);
  font-weight: var(--font-weight-semibold);
  color: var(--color-text-primary);
  display: flex;
  align-items: center;
  gap: var(--spacing-sm);
}

.group-list {
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.group-item {
  display: flex;
  align-items: center;
  gap: var(--spacing-sm);
  padding: var(--spacing-sm) var(--spacing-md);
  border-radius: var(--radius-md);
  cursor: pointer;
  transition: all var(--transition-fast);
  color: var(--color-text-secondary);
}

.group-item:hover {
  background: rgba(255, 255, 255, 0.05);
  color: var(--color-text-primary);
}

.group-item.active {
  background: var(--color-bg-info);
  color: var(--color-accent-primary);
}

.group-name {
  flex: 1;
  font-weight: var(--font-weight-medium);
}

.group-count {
  background: rgba(255, 255, 255, 0.1);
  padding: 2px 8px;
  border-radius: var(--radius-full);
  font-size: var(--font-size-xs);
  font-weight: var(--font-weight-semibold);
}

.group-item.active .group-count {
  background: rgba(255, 255, 255, 0.2);
}
</style>
