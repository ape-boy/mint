<script setup lang="ts">
import { useRouter } from 'vue-router'
import {
  EyeOutlined,
  PaperClipOutlined,
  BellOutlined,
  RocketOutlined,
  TagOutlined,
} from '@ant-design/icons-vue'
import type { ReleaseNote } from '../../types'
import { useFormat } from '../../composables'

defineProps<{
  releaseNotes: ReleaseNote[]
  loading?: boolean
}>()

const emit = defineEmits<{
  (e: 'create'): void
}>()

const router = useRouter()
const { formatDate } = useFormat()

const releaseTypeColors: Record<string, string> = {
  major: 'red',
  minor: 'blue',
  patch: 'green',
  hotfix: 'orange',
}

const releaseTypeLabels: Record<string, string> = {
  major: 'Major',
  minor: 'Minor',
  patch: 'Patch',
  hotfix: 'Hotfix',
}

const columns = [
  {
    title: '',
    key: 'popup',
    width: 40,
  },
  {
    title: 'Version',
    dataIndex: 'version',
    key: 'version',
    width: 100,
  },
  {
    title: 'Type',
    dataIndex: 'releaseType',
    key: 'releaseType',
    width: 100,
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
    title: 'Released',
    dataIndex: 'releasedAt',
    key: 'releasedAt',
    width: 120,
  },
]

function goToDetail(record: ReleaseNote) {
  router.push(`/board/release-note/${record.id}`)
}
</script>

<template>
  <div class="release-note-list">
    <div class="list-header">
      <div class="header-title">
        <h2>Release Notes</h2>
        <span class="count">{{ releaseNotes.length }} items</span>
      </div>
      <a-button type="primary" @click="emit('create')">
        New Release Note
      </a-button>
    </div>

    <a-table
      :dataSource="releaseNotes"
      :columns="columns"
      :loading="loading"
      :pagination="{ pageSize: 10, showSizeChanger: true, showTotal: (total: number) => `Total ${total} items` }"
      row-key="id"
      class="release-note-table"
      @row-click="goToDetail"
    >
      <template #bodyCell="{ column, record }">
        <template v-if="column.key === 'popup'">
          <BellOutlined v-if="record.isPopup" class="popup-icon" />
        </template>

        <template v-if="column.key === 'version'">
          <span class="version">
            <TagOutlined />
            v{{ record.version }}
          </span>
        </template>

        <template v-if="column.key === 'releaseType'">
          <a-tag :color="releaseTypeColors[record.releaseType]">
            {{ releaseTypeLabels[record.releaseType] }}
          </a-tag>
        </template>

        <template v-if="column.key === 'title'">
          <div class="title-cell">
            <span class="title-text" @click="goToDetail(record)">{{ record.title }}</span>
            <PaperClipOutlined v-if="record.attachments?.length" class="attachment-icon" />
          </div>
        </template>

        <template v-if="column.key === 'projectName'">
          <span v-if="record.projectName" class="project-name">
            <RocketOutlined />
            {{ record.projectName }}
          </span>
          <span v-else class="text-muted">System</span>
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

        <template v-if="column.key === 'releasedAt'">
          {{ formatDate(record.releasedAt) }}
        </template>
      </template>
    </a-table>
  </div>
</template>

<style scoped>
.release-note-list {
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

.release-note-table :deep(.ant-table) {
  background: transparent;
}

.release-note-table :deep(.ant-table-row) {
  cursor: pointer;
  transition: background var(--transition-fast);
}

.release-note-table :deep(.ant-table-row:hover) {
  background: var(--color-bg-tertiary);
}

.popup-icon {
  color: var(--color-status-warning);
}

.version {
  display: flex;
  align-items: center;
  gap: 4px;
  color: var(--color-accent-primary);
  font-weight: var(--font-weight-medium);
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

.project-name {
  display: flex;
  align-items: center;
  gap: 4px;
  color: var(--color-text-secondary);
  font-size: var(--font-size-sm);
}

.text-muted {
  color: var(--color-text-muted);
  font-style: italic;
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
