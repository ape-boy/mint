<script setup lang="ts">
import { useRouter } from 'vue-router'
import {
  EyeOutlined,
  PaperClipOutlined,
  CommentOutlined,
  UserOutlined,
  CalendarOutlined,
} from '@ant-design/icons-vue'
import type { VOC, VOCCategory, VOCPriority, VOCStatus } from '../../types'
import { useFormat } from '../../composables'

defineProps<{
  vocs: VOC[]
  loading?: boolean
}>()

const emit = defineEmits<{
  (e: 'create'): void
}>()

const router = useRouter()
const { formatDate } = useFormat()

const categoryColors: Record<VOCCategory, string> = {
  bug: 'red',
  feature_request: 'blue',
  improvement: 'green',
  question: 'cyan',
  documentation: 'purple',
  other: 'default',
}

const categoryLabels: Record<VOCCategory, string> = {
  bug: 'Bug',
  feature_request: 'Feature Request',
  improvement: 'Improvement',
  question: 'Question',
  documentation: 'Documentation',
  other: 'Other',
}

const priorityColors: Record<VOCPriority, string> = {
  low: 'default',
  medium: 'blue',
  high: 'orange',
  critical: 'red',
}

const statusColors: Record<VOCStatus, string> = {
  open: 'blue',
  in_progress: 'processing',
  pending: 'warning',
  resolved: 'success',
  closed: 'default',
}

const statusLabels: Record<VOCStatus, string> = {
  open: 'Open',
  in_progress: 'In Progress',
  pending: 'Pending',
  resolved: 'Resolved',
  closed: 'Closed',
}

const columns = [
  {
    title: 'Status',
    dataIndex: 'status',
    key: 'status',
    width: 120,
  },
  {
    title: 'Category',
    dataIndex: 'category',
    key: 'category',
    width: 140,
  },
  {
    title: 'Title',
    dataIndex: 'title',
    key: 'title',
  },
  {
    title: 'Project',
    dataIndex: 'projectName',
    key: 'projectName',
    width: 150,
  },
  {
    title: 'Requester',
    dataIndex: 'requester',
    key: 'requester',
    width: 120,
  },
  {
    title: 'Assignee',
    dataIndex: 'assignee',
    key: 'assignee',
    width: 120,
  },
  {
    title: 'Due Date',
    dataIndex: 'dueDate',
    key: 'dueDate',
    width: 110,
  },
  {
    title: '',
    key: 'meta',
    width: 80,
    align: 'center' as const,
  },
]

function goToDetail(record: VOC) {
  router.push(`/board/voc/${record.id}`)
}

function isOverdue(voc: VOC): boolean {
  if (!voc.dueDate) return false
  if (voc.status === 'resolved' || voc.status === 'closed') return false
  return new Date(voc.dueDate) < new Date()
}
</script>

<template>
  <div class="voc-list">
    <div class="list-header">
      <div class="header-title">
        <h2>VOC (Voice of Customer)</h2>
        <span class="count">{{ vocs.length }} items</span>
      </div>
      <a-button type="primary" @click="emit('create')">
        New VOC
      </a-button>
    </div>

    <a-table
      :dataSource="vocs"
      :columns="columns"
      :loading="loading"
      :pagination="{ pageSize: 10, showSizeChanger: true, showTotal: (total: number) => `Total ${total} items` }"
      row-key="id"
      class="voc-table"
      @row-click="goToDetail"
    >
      <template #bodyCell="{ column, record }">
        <template v-if="column.key === 'status'">
          <a-tag :color="statusColors[record.status as VOCStatus]">
            {{ statusLabels[record.status as VOCStatus] }}
          </a-tag>
        </template>

        <template v-if="column.key === 'category'">
          <a-tag :color="categoryColors[record.category as VOCCategory]">
            {{ categoryLabels[record.category as VOCCategory] }}
          </a-tag>
          <a-tag :color="priorityColors[record.priority as VOCPriority]" class="priority-tag">
            {{ record.priority }}
          </a-tag>
        </template>

        <template v-if="column.key === 'title'">
          <div class="title-cell">
            <span class="title-text" @click="goToDetail(record)">{{ record.title }}</span>
          </div>
        </template>

        <template v-if="column.key === 'projectName'">
          <span v-if="record.projectName">{{ record.projectName }}</span>
          <span v-else class="text-muted">-</span>
        </template>

        <template v-if="column.key === 'requester'">
          <div class="user-cell">
            <UserOutlined />
            {{ record.requester?.name }}
          </div>
        </template>

        <template v-if="column.key === 'assignee'">
          <div v-if="record.assignee" class="user-cell">
            <UserOutlined />
            {{ record.assignee.name }}
          </div>
          <span v-else class="text-muted unassigned">Unassigned</span>
        </template>

        <template v-if="column.key === 'dueDate'">
          <div v-if="record.dueDate" :class="['due-date', { overdue: isOverdue(record) }]">
            <CalendarOutlined />
            {{ formatDate(record.dueDate) }}
          </div>
          <span v-else class="text-muted">-</span>
        </template>

        <template v-if="column.key === 'meta'">
          <div class="meta-cell">
            <span class="meta-item" :title="`${record.viewCount} views`">
              <EyeOutlined />
              {{ record.viewCount }}
            </span>
            <span v-if="record.comments?.length" class="meta-item" :title="`${record.comments.length} comments`">
              <CommentOutlined />
              {{ record.comments.length }}
            </span>
            <PaperClipOutlined v-if="record.attachments?.length" class="attachment-icon" />
          </div>
        </template>
      </template>
    </a-table>
  </div>
</template>

<style scoped>
.voc-list {
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

.voc-table :deep(.ant-table) {
  background: transparent;
}

.voc-table :deep(.ant-table-row) {
  cursor: pointer;
  transition: background var(--transition-fast);
}

.voc-table :deep(.ant-table-row:hover) {
  background: var(--color-bg-tertiary);
}

.priority-tag {
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

.user-cell {
  display: flex;
  align-items: center;
  gap: 4px;
  color: var(--color-text-secondary);
  font-size: var(--font-size-sm);
}

.text-muted {
  color: var(--color-text-muted);
  font-size: var(--font-size-sm);
}

.unassigned {
  font-style: italic;
}

.due-date {
  display: flex;
  align-items: center;
  gap: 4px;
  font-size: var(--font-size-sm);
  color: var(--color-text-secondary);
}

.due-date.overdue {
  color: var(--color-status-error);
}

.meta-cell {
  display: flex;
  align-items: center;
  gap: var(--spacing-sm);
}

.meta-item {
  display: flex;
  align-items: center;
  gap: 2px;
  color: var(--color-text-muted);
  font-size: var(--font-size-xs);
}

.attachment-icon {
  color: var(--color-text-muted);
  font-size: var(--font-size-sm);
}
</style>
