<script setup lang="ts">
import { ref, computed } from 'vue'
import {
  ArrowLeftOutlined,
  EditOutlined,
  DeleteOutlined,
  UserOutlined,
  CalendarOutlined,
  SendOutlined,
  CheckCircleOutlined,
} from '@ant-design/icons-vue'
import type { VOC, VOCCategory, VOCPriority, VOCStatus, TeamMember } from '../../types'
import { useFormat } from '../../composables'

const props = defineProps<{
  voc: VOC | null
  loading?: boolean
}>()

const emit = defineEmits<{
  (e: 'back'): void
  (e: 'edit', voc: VOC): void
  (e: 'delete', id: string): void
  (e: 'updateStatus', payload: { id: string; status: VOCStatus; resolution?: string }): void
  (e: 'addComment', payload: { id: string; content: string }): void
}>()

const { formatDate, formatDateTime } = useFormat()

const newComment = ref('')
const resolutionText = ref('')
const showResolveModal = ref(false)

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

const contentHtml = computed(() => {
  if (!props.voc?.content) return ''
  return props.voc.content.replace(/\n/g, '<br>')
})

const isOverdue = computed(() => {
  if (!props.voc?.dueDate) return false
  if (props.voc.status === 'resolved' || props.voc.status === 'closed') return false
  return new Date(props.voc.dueDate) < new Date()
})

function handleDelete() {
  if (props.voc) {
    emit('delete', props.voc.id)
  }
}

function handleAddComment() {
  if (!newComment.value.trim() || !props.voc) return
  emit('addComment', { id: props.voc.id, content: newComment.value })
  newComment.value = ''
}

function handleResolve() {
  if (!props.voc) return
  emit('updateStatus', { id: props.voc.id, status: 'resolved', resolution: resolutionText.value })
  showResolveModal.value = false
  resolutionText.value = ''
}

function changeStatus(status: VOCStatus) {
  if (!props.voc) return
  if (status === 'resolved') {
    showResolveModal.value = true
  } else {
    emit('updateStatus', { id: props.voc.id, status })
  }
}
</script>

<template>
  <div class="voc-detail">
    <a-spin :spinning="loading">
      <div v-if="voc" class="detail-content">
        <div class="detail-header">
          <a-button type="text" @click="emit('back')" class="back-btn">
            <ArrowLeftOutlined />
            Back to List
          </a-button>

          <div class="header-actions">
            <a-dropdown>
              <a-button>
                Change Status
              </a-button>
              <template #overlay>
                <a-menu>
                  <a-menu-item
                    v-for="(label, status) in statusLabels"
                    :key="status"
                    @click="changeStatus(status as VOCStatus)"
                    :disabled="voc.status === status"
                  >
                    <a-tag :color="statusColors[status as VOCStatus]" size="small">
                      {{ label }}
                    </a-tag>
                  </a-menu-item>
                </a-menu>
              </template>
            </a-dropdown>
            <a-button @click="emit('edit', voc)">
              <EditOutlined />
              Edit
            </a-button>
            <a-popconfirm
              title="Are you sure you want to delete this VOC?"
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

        <div class="voc-card">
          <div class="voc-meta">
            <div class="meta-tags">
              <a-tag :color="statusColors[voc.status]" size="large">
                {{ statusLabels[voc.status] }}
              </a-tag>
              <a-tag :color="categoryColors[voc.category]">
                {{ categoryLabels[voc.category] }}
              </a-tag>
              <a-tag :color="priorityColors[voc.priority]">
                {{ voc.priority }}
              </a-tag>
            </div>
          </div>

          <h1 class="voc-title">{{ voc.title }}</h1>

          <div class="voc-info">
            <div class="info-row">
              <div class="info-item">
                <span class="label">Requester</span>
                <span class="value user">
                  <UserOutlined />
                  {{ voc.requester?.name }} ({{ voc.requester?.email }})
                </span>
              </div>
              <div class="info-item">
                <span class="label">Assignee</span>
                <span v-if="voc.assignee" class="value user">
                  <UserOutlined />
                  {{ voc.assignee.name }} ({{ voc.assignee.email }})
                </span>
                <span v-else class="value unassigned">Unassigned</span>
              </div>
            </div>
            <div class="info-row">
              <div v-if="voc.projectName" class="info-item">
                <span class="label">Project</span>
                <span class="value">{{ voc.projectGroupName }} / {{ voc.projectName }}</span>
              </div>
              <div v-if="voc.dueDate" class="info-item">
                <span class="label">Due Date</span>
                <span :class="['value', { overdue: isOverdue }]">
                  <CalendarOutlined />
                  {{ formatDate(voc.dueDate) }}
                  <a-tag v-if="isOverdue" color="error" size="small">Overdue</a-tag>
                </span>
              </div>
            </div>
            <div class="info-row">
              <div class="info-item">
                <span class="label">Created</span>
                <span class="value">{{ formatDateTime(voc.createdAt) }}</span>
              </div>
              <div v-if="voc.resolvedAt" class="info-item">
                <span class="label">Resolved</span>
                <span class="value">{{ formatDateTime(voc.resolvedAt) }}</span>
              </div>
            </div>
          </div>

          <a-divider />

          <div class="voc-body">
            <h3>Description</h3>
            <div class="body-content" v-html="contentHtml" />
          </div>

          <div v-if="voc.resolution" class="resolution-section">
            <a-divider />
            <h3><CheckCircleOutlined /> Resolution</h3>
            <div class="resolution-content">
              {{ voc.resolution }}
            </div>
          </div>

          <a-divider />

          <div class="comments-section">
            <h3>Comments ({{ voc.comments?.length || 0 }})</h3>

            <div class="comment-list">
              <div
                v-for="comment in voc.comments"
                :key="comment.id"
                class="comment-item"
              >
                <div class="comment-header">
                  <span class="comment-author">
                    <UserOutlined />
                    {{ comment.author?.name }}
                  </span>
                  <span class="comment-date">{{ formatDateTime(comment.createdAt) }}</span>
                </div>
                <div class="comment-content">{{ comment.content }}</div>
              </div>

              <a-empty v-if="!voc.comments?.length" description="No comments yet" />
            </div>

            <div class="comment-form">
              <a-textarea
                v-model:value="newComment"
                placeholder="Add a comment..."
                :rows="3"
              />
              <a-button
                type="primary"
                :disabled="!newComment.trim()"
                @click="handleAddComment"
              >
                <SendOutlined />
                Add Comment
              </a-button>
            </div>
          </div>
        </div>
      </div>

      <a-empty v-else description="VOC not found" />
    </a-spin>

    <a-modal
      v-model:open="showResolveModal"
      title="Resolve VOC"
      @ok="handleResolve"
    >
      <a-form layout="vertical">
        <a-form-item label="Resolution" required>
          <a-textarea
            v-model:value="resolutionText"
            placeholder="Enter resolution details..."
            :rows="4"
          />
        </a-form-item>
      </a-form>
    </a-modal>
  </div>
</template>

<style scoped>
.voc-detail {
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

.voc-card {
  background: var(--color-bg-tertiary);
  border-radius: var(--radius-md);
  padding: var(--spacing-xl);
}

.voc-meta {
  margin-bottom: var(--spacing-lg);
}

.meta-tags {
  display: flex;
  gap: var(--spacing-sm);
}

.voc-title {
  font-size: var(--font-size-2xl);
  font-weight: var(--font-weight-bold);
  color: var(--color-text-primary);
  margin: 0 0 var(--spacing-lg) 0;
}

.voc-info {
  background: var(--color-bg-primary);
  padding: var(--spacing-md);
  border-radius: var(--radius-sm);
}

.info-row {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: var(--spacing-md);
  margin-bottom: var(--spacing-md);
}

.info-row:last-child {
  margin-bottom: 0;
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

.info-item .value.user {
  color: var(--color-text-secondary);
}

.info-item .value.unassigned {
  color: var(--color-text-muted);
  font-style: italic;
}

.info-item .value.overdue {
  color: var(--color-status-error);
}

.voc-body h3 {
  font-size: var(--font-size-md);
  color: var(--color-text-primary);
  margin-bottom: var(--spacing-md);
}

.body-content {
  color: var(--color-text-primary);
  line-height: 1.8;
}

.resolution-section h3 {
  font-size: var(--font-size-md);
  color: var(--color-status-success);
  margin-bottom: var(--spacing-md);
  display: flex;
  align-items: center;
  gap: var(--spacing-sm);
}

.resolution-content {
  background: rgba(82, 196, 26, 0.1);
  border-left: 3px solid var(--color-status-success);
  padding: var(--spacing-md);
  border-radius: 0 var(--radius-sm) var(--radius-sm) 0;
  color: var(--color-text-primary);
}

.comments-section h3 {
  font-size: var(--font-size-md);
  color: var(--color-text-primary);
  margin-bottom: var(--spacing-md);
}

.comment-list {
  display: flex;
  flex-direction: column;
  gap: var(--spacing-md);
  margin-bottom: var(--spacing-lg);
}

.comment-item {
  background: var(--color-bg-primary);
  padding: var(--spacing-md);
  border-radius: var(--radius-sm);
}

.comment-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: var(--spacing-sm);
}

.comment-author {
  display: flex;
  align-items: center;
  gap: 4px;
  color: var(--color-text-secondary);
  font-weight: var(--font-weight-medium);
}

.comment-date {
  font-size: var(--font-size-xs);
  color: var(--color-text-muted);
}

.comment-content {
  color: var(--color-text-primary);
  white-space: pre-wrap;
}

.comment-form {
  display: flex;
  flex-direction: column;
  gap: var(--spacing-sm);
}

.comment-form .ant-btn {
  align-self: flex-end;
}
</style>
