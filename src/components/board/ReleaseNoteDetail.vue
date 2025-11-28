<script setup lang="ts">
import { computed } from 'vue'
import {
  ArrowLeftOutlined,
  EditOutlined,
  DeleteOutlined,
  BellOutlined,
  EyeOutlined,
  PaperClipOutlined,
  DownloadOutlined,
  TagOutlined,
  RocketOutlined,
  CheckCircleOutlined,
  BugOutlined,
  ThunderboltOutlined,
  WarningOutlined,
} from '@ant-design/icons-vue'
import type { ReleaseNote, ReleaseChange } from '../../types'
import { useFormat } from '../../composables'

const props = defineProps<{
  releaseNote: ReleaseNote | null
  loading?: boolean
}>()

const emit = defineEmits<{
  (e: 'back'): void
  (e: 'edit', releaseNote: ReleaseNote): void
  (e: 'delete', id: string): void
}>()

const { formatDate, formatDateTime, formatFileSize } = useFormat()

const releaseTypeColors: Record<string, string> = {
  major: 'red',
  minor: 'blue',
  patch: 'green',
  hotfix: 'orange',
}

const releaseTypeLabels: Record<string, string> = {
  major: 'Major Release',
  minor: 'Minor Release',
  patch: 'Patch',
  hotfix: 'Hotfix',
}

const changeTypeIcons: Record<string, typeof CheckCircleOutlined> = {
  feature: RocketOutlined,
  bugfix: BugOutlined,
  improvement: ThunderboltOutlined,
  breaking: WarningOutlined,
}

const changeTypeColors: Record<string, string> = {
  feature: 'blue',
  bugfix: 'red',
  improvement: 'green',
  breaking: 'orange',
}

const changeTypeLabels: Record<string, string> = {
  feature: 'New Feature',
  bugfix: 'Bug Fix',
  improvement: 'Improvement',
  breaking: 'Breaking Change',
}

const contentHtml = computed(() => {
  if (!props.releaseNote?.content) return ''
  return props.releaseNote.content.replace(/\n/g, '<br>')
})

const groupedChanges = computed(() => {
  if (!props.releaseNote?.changes) return {} as Record<string, ReleaseChange[]>
  const grouped: Record<string, ReleaseChange[]> = {}
  props.releaseNote.changes.forEach((change: ReleaseChange) => {
    if (!grouped[change.type]) {
      grouped[change.type] = []
    }
    grouped[change.type]!.push(change)
  })
  return grouped
})

function handleDelete() {
  if (props.releaseNote) {
    emit('delete', props.releaseNote.id)
  }
}
</script>

<template>
  <div class="release-note-detail">
    <a-spin :spinning="loading">
      <div v-if="releaseNote" class="detail-content">
        <div class="detail-header">
          <a-button type="text" @click="emit('back')" class="back-btn">
            <ArrowLeftOutlined />
            Back to List
          </a-button>

          <div class="header-actions">
            <a-button @click="emit('edit', releaseNote)">
              <EditOutlined />
              Edit
            </a-button>
            <a-popconfirm
              title="Are you sure you want to delete this release note?"
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

        <div class="release-note-card">
          <div class="release-note-meta">
            <div class="meta-tags">
              <a-tag :color="releaseTypeColors[releaseNote.releaseType]" size="large">
                {{ releaseTypeLabels[releaseNote.releaseType] }}
              </a-tag>
              <span class="version">
                <TagOutlined />
                v{{ releaseNote.version }}
              </span>
              <span v-if="releaseNote.isPopup" class="meta-badge popup">
                <BellOutlined /> Popup Enabled
              </span>
            </div>
            <div class="meta-info">
              <span class="view-count">
                <EyeOutlined /> {{ releaseNote.viewCount }} views
              </span>
            </div>
          </div>

          <h1 class="release-note-title">{{ releaseNote.title }}</h1>

          <div class="release-note-info">
            <div class="info-item" v-if="releaseNote.projectName">
              <span class="label">Project</span>
              <span class="value">
                <RocketOutlined />
                {{ releaseNote.projectName }}
              </span>
            </div>
            <div class="info-item" v-else>
              <span class="label">Type</span>
              <span class="value">System Release</span>
            </div>
            <div class="info-item">
              <span class="label">Author</span>
              <span class="value">{{ releaseNote.author?.name }} ({{ releaseNote.author?.email }})</span>
            </div>
            <div class="info-item">
              <span class="label">Released</span>
              <span class="value">{{ formatDateTime(releaseNote.releasedAt) }}</span>
            </div>
            <div v-if="releaseNote.isPopup && releaseNote.popupStartDate" class="info-item">
              <span class="label">Popup Period</span>
              <span class="value">
                {{ formatDate(releaseNote.popupStartDate) }} ~ {{ formatDate(releaseNote.popupEndDate) }}
              </span>
            </div>
          </div>

          <a-divider />

          <div class="release-note-body">
            <h3>Description</h3>
            <div class="body-content" v-html="contentHtml" />
          </div>

          <a-divider />

          <div class="changes-section">
            <h3>Changes ({{ releaseNote.changes?.length || 0 }})</h3>

            <div v-for="(changes, type) in groupedChanges" :key="type" class="change-group">
              <div class="change-group-header">
                <component :is="changeTypeIcons[type]" />
                <span>{{ changeTypeLabels[type] }}</span>
                <a-tag :color="changeTypeColors[type]" size="small">
                  {{ changes.length }}
                </a-tag>
              </div>
              <ul class="change-list">
                <li v-for="(change, index) in changes" :key="index">
                  {{ change.description }}
                </li>
              </ul>
            </div>

            <a-empty v-if="!releaseNote.changes?.length" description="No changes listed" />
          </div>

          <div v-if="releaseNote.attachments?.length" class="attachments">
            <a-divider />
            <h3><PaperClipOutlined /> Attachments ({{ releaseNote.attachments.length }})</h3>
            <div class="attachment-list">
              <a
                v-for="attachment in releaseNote.attachments"
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

      <a-empty v-else description="Release note not found" />
    </a-spin>
  </div>
</template>

<style scoped>
.release-note-detail {
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

.release-note-card {
  background: var(--color-bg-tertiary);
  border-radius: var(--radius-md);
  padding: var(--spacing-xl);
}

.release-note-meta {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: var(--spacing-lg);
}

.meta-tags {
  display: flex;
  align-items: center;
  gap: var(--spacing-md);
}

.version {
  display: flex;
  align-items: center;
  gap: 4px;
  font-size: var(--font-size-lg);
  font-weight: var(--font-weight-bold);
  color: var(--color-accent-primary);
}

.meta-badge {
  display: flex;
  align-items: center;
  gap: 4px;
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

.release-note-title {
  font-size: var(--font-size-2xl);
  font-weight: var(--font-weight-bold);
  color: var(--color-text-primary);
  margin: 0 0 var(--spacing-lg) 0;
}

.release-note-info {
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
  display: flex;
  align-items: center;
  gap: 4px;
}

.release-note-body h3,
.changes-section h3,
.attachments h3 {
  font-size: var(--font-size-md);
  color: var(--color-text-primary);
  margin-bottom: var(--spacing-md);
}

.body-content {
  color: var(--color-text-primary);
  line-height: 1.8;
}

.changes-section {
  margin-top: var(--spacing-lg);
}

.change-group {
  margin-bottom: var(--spacing-lg);
}

.change-group-header {
  display: flex;
  align-items: center;
  gap: var(--spacing-sm);
  font-weight: var(--font-weight-medium);
  color: var(--color-text-primary);
  margin-bottom: var(--spacing-sm);
}

.change-list {
  margin: 0;
  padding-left: var(--spacing-xl);
  color: var(--color-text-secondary);
}

.change-list li {
  margin-bottom: var(--spacing-xs);
}

.attachments {
  margin-top: var(--spacing-lg);
}

.attachments h3 {
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
