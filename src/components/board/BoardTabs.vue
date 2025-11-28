<script setup lang="ts">
import { computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import {
  NotificationOutlined,
  FileTextOutlined,
  CommentOutlined,
} from '@ant-design/icons-vue'

const route = useRoute()
const router = useRouter()

const activeKey = computed(() => {
  const path = route.path
  if (path.includes('/board/notice')) return 'notice'
  if (path.includes('/board/release-note')) return 'release-note'
  if (path.includes('/board/voc')) return 'voc'
  return 'notice'
})

function handleTabChange(key: string) {
  router.push(`/board/${key}`)
}
</script>

<template>
  <div class="board-tabs">
    <a-tabs :activeKey="activeKey" @change="handleTabChange" size="large">
      <a-tab-pane key="notice">
        <template #tab>
          <span class="tab-item">
            <NotificationOutlined />
            Notice
          </span>
        </template>
      </a-tab-pane>
      <a-tab-pane key="release-note">
        <template #tab>
          <span class="tab-item">
            <FileTextOutlined />
            Release Notes
          </span>
        </template>
      </a-tab-pane>
      <a-tab-pane key="voc">
        <template #tab>
          <span class="tab-item">
            <CommentOutlined />
            VOC
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
