<script setup lang="ts">
import { computed } from 'vue'
import { useRouter } from 'vue-router'
import {
  PushpinOutlined,
  EyeOutlined,
  PaperClipOutlined,
  BellOutlined,
} from '@ant-design/icons-vue'
import type { Notice, NoticeCategory } from '../../types'
import { useFormat } from '../../composables'

defineProps<{
  notices: Notice[]
  loading?: boolean
}>()

const emit = defineEmits<{
  (e: 'create'): void
}>()

const router = useRouter()
const { formatDate } = useFormat()

const categoryColors: Record<NoticeCategory, string> = {
  general: 'blue',
  system: 'purple',
  maintenance: 'orange',
  event: 'green',
}

const categoryLabels: Record<NoticeCategory, string> = {
  general: 'General',
  system: 'System',
  maintenance: 'Maintenance',
  event: 'Event',
}

const priorityColors: Record<string, string> = {
  normal: 'default',
  important: 'warning',
  urgent: 'error',
}

const columns = [
  {
    title: '',
    key: 'pin',
    width: 40,
  },
  {
    title: 'Category',
    dataIndex: 'category',
    key: 'category',
    width: 120,
  },
  {
    title: 'Title',
    dataIndex: 'title',
    key: 'title',
  },
  {
    title: 'Author',
    dataIndex: 'author',
    key: 'author',
    width: 120,
  },
  {
    title: 'Views',
    dataIndex: 'viewCount',
    key: 'viewCount',
    width: 80,
    align: 'center' as const,
  },
  {
    title: 'Date',
    dataIndex: 'createdAt',
    key: 'createdAt',
    width: 120,
  },
]

function goToDetail(record: Notice) {
  router.push(`/board/notice/${record.id}`)
}
</script>

<template>
  <div class="notice-list">
    <div class="list-header">
      <div class="header-title">
        <h2>Notice</h2>
        <span class="count">{{ notices.length }} items</span>
      </div>
      <a-button type="primary" @click="emit('create')">
        New Notice
      </a-button>
    </div>

    <a-table
      :dataSource="notices"
      :columns="columns"
      :loading="loading"
      :pagination="{ pageSize: 10, showSizeChanger: true, showTotal: (total: number) => `Total ${total} items` }"
      row-key="id"
      class="notice-table"
      @row-click="goToDetail"
    >
      <template #bodyCell="{ column, record }">
        <template v-if="column.key === 'pin'">
          <PushpinOutlined v-if="record.isPinned" class="pin-icon" />
          <BellOutlined v-if="record.isPopup" class="popup-icon" />
        </template>

        <template v-if="column.key === 'category'">
          <a-tag :color="categoryColors[record.category as NoticeCategory]">
            {{ categoryLabels[record.category as NoticeCategory] }}
          </a-tag>
          <a-tag v-if="record.priority !== 'normal'" :color="priorityColors[record.priority]">
            {{ record.priority }}
          </a-tag>
        </template>

        <template v-if="column.key === 'title'">
          <div class="title-cell">
            <span class="title-text" @click="goToDetail(record)">{{ record.title }}</span>
            <PaperClipOutlined v-if="record.attachments?.length" class="attachment-icon" />
          </div>
        </template>

        <template v-if="column.key === 'author'">
          {{ record.author?.name }}
        </template>

        <template v-if="column.key === 'viewCount'">
          <span class="view-count">
            <EyeOutlined />
            {{ record.viewCount }}
          </span>
        </template>

        <template v-if="column.key === 'createdAt'">
          {{ formatDate(record.createdAt) }}
        </template>
      </template>
    </a-table>
  </div>
</template>

<style scoped>
.notice-list {
  background: var(--color-bg-secondary);
  border-radius: var(--radius-lg);
  padding: var(--spacing-xl);
}

.list-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: var(--spacing-lg);
}

.header-title {
  display: flex;
  align-items: baseline;
  gap: var(--spacing-sm);
}

.header-title h2 {
  margin: 0;
  font-size: var(--font-size-xl);
  color: var(--color-text-primary);
}

.count {
  color: var(--color-text-muted);
  font-size: var(--font-size-sm);
}

.notice-table :deep(.ant-table) {
  background: transparent;
}

.notice-table :deep(.ant-table-row) {
  cursor: pointer;
  transition: background var(--transition-fast);
}

.notice-table :deep(.ant-table-row:hover) {
  background: var(--color-bg-tertiary);
}

.pin-icon {
  color: var(--color-accent-primary);
}

.popup-icon {
  color: var(--color-status-warning);
  margin-left: 4px;
}

.title-cell {
  display: flex;
  align-items: center;
  gap: var(--spacing-sm);
}

.title-text {
  color: var(--color-text-primary);
  cursor: pointer;
}

.title-text:hover {
  color: var(--color-accent-primary);
}

.attachment-icon {
  color: var(--color-text-muted);
  font-size: var(--font-size-sm);
}

.view-count {
  display: flex;
  align-items: center;
  gap: 4px;
  color: var(--color-text-muted);
  font-size: var(--font-size-sm);
}
</style>
