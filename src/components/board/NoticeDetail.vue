<script setup lang="ts">
import { computed } from 'vue'
import {
  ArrowLeftOutlined,
  EditOutlined,
  DeleteOutlined,
  PushpinOutlined,
  BellOutlined,
  EyeOutlined,
  PaperClipOutlined,
  DownloadOutlined,
} from '@ant-design/icons-vue'
import type { Notice, NoticeCategory } from '../../types'
import { useFormat } from '../../composables'

const props = defineProps<{
  notice: Notice | null
  loading?: boolean
}>()

const emit = defineEmits<{
  (e: 'back'): void
  (e: 'edit', notice: Notice): void
  (e: 'delete', id: string): void
}>()

const { formatDate, formatDateTime, formatFileSize } = useFormat()

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

const contentHtml = computed(() => {
  if (!props.notice?.content) return ''
  return props.notice.content.replace(/\n/g, '<br>')
})

function handleDelete() {
  if (props.notice) {
    emit('delete', props.notice.id)
  }
}
</script>

<template>
  <div class="notice-detail">
    <a-spin :spinning="loading">
      <div v-if="notice" class="detail-content">
        <div class="detail-header">
          <a-button type="text" @click="emit('back')" class="back-btn">
            <ArrowLeftOutlined />
            Back to List
          </a-button>

          <div class="header-actions">
            <a-button @click="emit('edit', notice)">
              <EditOutlined />
              Edit
            </a-button>
            <a-popconfirm
              title="Are you sure you want to delete this notice?"
              ok-text="Delete"
              cancel-text="Cancel"
              @confirm="handleDelete"
            >
              <a-button danger>
                <DeleteOutlined />
                Delete
              </a-button>
            </a-popconfirm>
          </div>
        </div>

        <div class="notice-card">
          <div class="notice-meta">
            <div class="meta-tags">
              <a-tag :color="categoryColors[notice.category]">
                {{ categoryLabels[notice.category] }}
              </a-tag>
              <a-tag v-if="notice.priority !== 'normal'" :color="priorityColors[notice.priority]">
                {{ notice.priority }}
              </a-tag>
              <span v-if="notice.isPinned" class="meta-badge">
                <PushpinOutlined /> Pinned
              </span>
              <span v-if="notice.isPopup" class="meta-badge popup">
                <BellOutlined /> Popup Enabled
              </span>
            </div>
            <div class="meta-info">
              <span class="view-count">
                <EyeOutlined /> {{ notice.viewCount }} views
              </span>
            </div>
          </div>

          <h1 class="notice-title">{{ notice.title }}</h1>

          <div class="notice-info">
            <div class="info-item">
              <span class="label">Author</span>
              <span class="value">{{ notice.author?.name }} ({{ notice.author?.email }})</span>
            </div>
            <div class="info-item">
              <span class="label">Created</span>
              <span class="value">{{ formatDateTime(notice.createdAt) }}</span>
            </div>
            <div v-if="notice.updatedAt !== notice.createdAt" class="info-item">
              <span class="label">Updated</span>
              <span class="value">{{ formatDateTime(notice.updatedAt) }}</span>
            </div>
            <div v-if="notice.isPopup && notice.popupStartDate" class="info-item">
              <span class="label">Popup Period</span>
              <span class="value">
                {{ formatDate(notice.popupStartDate) }} ~ {{ formatDate(notice.popupEndDate) }}
              </span>
            </div>
          </div>

          <a-divider />

          <div class="notice-body" v-html="contentHtml" />

          <div v-if="notice.attachments?.length" class="attachments">
            <h3><PaperClipOutlined /> Attachments ({{ notice.attachments.length }})</h3>
            <div class="attachment-list">
              <a
                v-for="attachment in notice.attachments"
                :key="attachment.id"
                :href="attachment.url"
                target="_blank"
                class="attachment-item"
              >
                <PaperClipOutlined />
                <span class="file-name">{{ attachment.name }}</span>
                <span class="file-size">({{ formatFileSize(attachment.size) }})</span>
                <DownloadOutlined class="download-icon" />
              </a>
            </div>
          </div>
        </div>
      </div>

      <a-empty v-else description="Notice not found" />
    </a-spin>
  </div>
</template>

<style scoped>
.notice-detail {
  background: var(--color-bg-secondary);
  border-radius: var(--radius-lg);
  padding: var(--spacing-xl);
}

.detail-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: var(--spacing-xl);
}

.back-btn {
  color: var(--color-text-secondary);
  padding-left: 0;
}

.back-btn:hover {
  color: var(--color-accent-primary);
}

.header-actions {
  display: flex;
  gap: var(--spacing-sm);
}

.notice-card {
  background: var(--color-bg-tertiary);
  border-radius: var(--radius-md);
  padding: var(--spacing-xl);
}

.notice-meta {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: var(--spacing-lg);
}

.meta-tags {
  display: flex;
  align-items: center;
  gap: var(--spacing-sm);
}

.meta-badge {
  display: flex;
  align-items: center;
  gap: 4px;
  color: var(--color-accent-primary);
  font-size: var(--font-size-sm);
}

.meta-badge.popup {
  color: var(--color-status-warning);
}

.view-count {
  display: flex;
  align-items: center;
  gap: 4px;
  color: var(--color-text-muted);
  font-size: var(--font-size-sm);
}

.notice-title {
  font-size: var(--font-size-2xl);
  font-weight: var(--font-weight-bold);
  color: var(--color-text-primary);
  margin: 0 0 var(--spacing-lg) 0;
}

.notice-info {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(250px, 1fr));
  gap: var(--spacing-md);
  background: var(--color-bg-primary);
  padding: var(--spacing-md);
  border-radius: var(--radius-sm);
}

.info-item {
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.info-item .label {
  font-size: var(--font-size-xs);
  color: var(--color-text-muted);
  text-transform: uppercase;
}

.info-item .value {
  color: var(--color-text-primary);
}

.notice-body {
  color: var(--color-text-primary);
  line-height: 1.8;
  font-size: var(--font-size-md);
}

.attachments {
  margin-top: var(--spacing-xl);
  padding-top: var(--spacing-lg);
  border-top: 1px solid var(--color-border-light);
}

.attachments h3 {
  font-size: var(--font-size-md);
  color: var(--color-text-primary);
  margin-bottom: var(--spacing-md);
  display: flex;
  align-items: center;
  gap: var(--spacing-sm);
}

.attachment-list {
  display: flex;
  flex-direction: column;
  gap: var(--spacing-sm);
}

.attachment-item {
  display: flex;
  align-items: center;
  gap: var(--spacing-sm);
  padding: var(--spacing-sm) var(--spacing-md);
  background: var(--color-bg-primary);
  border-radius: var(--radius-sm);
  color: var(--color-text-secondary);
  text-decoration: none;
  transition: all var(--transition-fast);
}

.attachment-item:hover {
  background: var(--color-bg-secondary);
  color: var(--color-accent-primary);
}

.file-name {
  flex: 1;
}

.file-size {
  font-size: var(--font-size-sm);
  color: var(--color-text-muted);
}

.download-icon {
  opacity: 0;
  transition: opacity var(--transition-fast);
}

.attachment-item:hover .download-icon {
  opacity: 1;
}
</style>
