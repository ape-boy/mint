<script setup lang="ts">
import {
  NotificationOutlined,
  FileTextOutlined,
  CommentOutlined,
} from '@ant-design/icons-vue'

const props = defineProps<{
  activeTab: 'notice' | 'voc' | 'release-note'
  noticeCount?: number
  vocCount?: number
  releaseNoteCount?: number
}>()

const emit = defineEmits<{
  'update:active-tab': [tab: 'notice' | 'voc' | 'release-note']
}>()

function handleTabChange(key: string) {
  emit('update:active-tab', key as 'notice' | 'voc' | 'release-note')
}
</script>

<template>
  <div class="board-tabs">
    <a-tabs :activeKey="activeTab" @change="handleTabChange" size="large">
      <a-tab-pane key="notice">
        <template #tab>
          <span class="tab-item">
            <NotificationOutlined />
            Notice
            <a-badge v-if="noticeCount" :count="noticeCount" :number-style="{ backgroundColor: 'var(--color-accent-primary)' }" />
          </span>
        </template>
      </a-tab-pane>
      <a-tab-pane key="release-note">
        <template #tab>
          <span class="tab-item">
            <FileTextOutlined />
            Release Notes
            <a-badge v-if="releaseNoteCount" :count="releaseNoteCount" :number-style="{ backgroundColor: 'var(--color-accent-primary)' }" />
          </span>
        </template>
      </a-tab-pane>
      <a-tab-pane key="voc">
        <template #tab>
          <span class="tab-item">
            <CommentOutlined />
            VOC
            <a-badge v-if="vocCount" :count="vocCount" :number-style="{ backgroundColor: 'var(--color-accent-primary)' }" />
          </span>
        </template>
      </a-tab-pane>
    </a-tabs>
  </div>
</template>

<style scoped>
.board-tabs {
  margin-bottom: var(--spacing-lg);
}

.tab-item {
  display: flex;
  align-items: center;
  gap: var(--spacing-sm);
}

.board-tabs :deep(.ant-tabs-nav) {
  margin-bottom: 0;
}

.board-tabs :deep(.ant-tabs-tab) {
  padding: var(--spacing-md) var(--spacing-lg);
}
</style>
