<script setup lang="ts">
import { ref, computed, onMounted, watch } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import {
  ArrowLeftOutlined,
  CheckCircleOutlined,
  CloseCircleOutlined,
  ClockCircleOutlined,
  ReloadOutlined,
  DownloadOutlined,
  FileTextOutlined,
  BranchesOutlined,
  ExclamationCircleOutlined,
  CopyOutlined,
} from '@ant-design/icons-vue'
import { message } from 'ant-design-vue'
import { StatusBadge, EmptyState } from '@/components'
import { useProjectStore } from '@/stores/project'
import { useBuildStore } from '@/stores/build'
import type { Build, BuildStage } from '@/types'

const router = useRouter()
const route = useRoute()
const projectStore = useProjectStore()
const buildStore = useBuildStore()

const props = defineProps<{
  projectId: string
  buildId: string
  stageName: string
}>()

const loading = ref(true)
const build = ref<Build | null>(null)
const stage = ref<BuildStage | null>(null)

onMounted(async () => {
  await loadData()
})

watch(() => [props.projectId, props.buildId, props.stageName], loadData)

async function loadData() {
  loading.value = true
  try {
    // Fetch project for context
    await projectStore.fetchProjectById(props.projectId)

    // Fetch build
    await buildStore.fetchBuildById(props.buildId)
    build.value = buildStore.currentBuild

    // Find the specific stage
    if (build.value) {
      const decodedStageName = decodeURIComponent(props.stageName)
      stage.value = build.value.stages?.find(s => s.name === decodedStageName) || null
    }
  } catch (e) {
    console.error('Failed to load stage data', e)
    message.error('스테이지 정보를 불러오는데 실패했습니다')
  } finally {
    loading.value = false
  }
}

const project = computed(() => projectStore.currentProject)

function goBack() {
  router.push(`/projects/${props.projectId}?tab=build&layerId=${build.value?.layerId}`)
}

function formatDuration(seconds?: number | null): string {
  if (!seconds) return '-'
  const hours = Math.floor(seconds / 3600)
  const mins = Math.floor((seconds % 3600) / 60)
  const secs = seconds % 60
  if (hours > 0) {
    return `${hours}h ${mins}m ${secs}s`
  }
  return `${mins}m ${secs}s`
}

function formatDateTime(dateStr?: string): string {
  if (!dateStr) return '-'
  return new Date(dateStr).toLocaleString('ko-KR', {
    year: 'numeric',
    month: '2-digit',
    day: '2-digit',
    hour: '2-digit',
    minute: '2-digit',
    second: '2-digit'
  })
}

function getStatusColor(status?: string) {
  const colors: Record<string, string> = {
    success: 'var(--color-accent-success)',
    failed: 'var(--color-accent-danger)',
    running: 'var(--color-accent-warning)',
    pending: 'var(--color-text-muted)',
    skipped: 'var(--color-text-muted)',
  }
  return colors[status || 'pending'] || colors.pending
}

function getStatusBgColor(status?: string) {
  const colors: Record<string, string> = {
    success: 'rgba(76, 175, 80, 0.1)',
    failed: 'rgba(244, 67, 54, 0.1)',
    running: 'rgba(255, 183, 77, 0.1)',
    pending: 'rgba(148, 163, 184, 0.1)',
    skipped: 'rgba(148, 163, 184, 0.1)',
  }
  return colors[status || 'pending'] || colors.pending
}

function downloadLog() {
  if (stage.value?.logUrl) {
    message.info('로그 다운로드를 시작합니다')
    window.open(stage.value.logUrl, '_blank')
  }
}

function copyLogsToClipboard() {
  if (stage.value?.logs) {
    navigator.clipboard.writeText(stage.value.logs)
    message.success('로그가 클립보드에 복사되었습니다')
  }
}

function downloadArtifactFile(url?: string) {
  if (url) {
    window.open(url, '_blank')
  }
}
</script>

<template>
  <div class="stage-detail-view">
    <!-- Back Navigation -->
    <div class="back-nav">
      <a-button type="text" @click="goBack">
        <ArrowLeftOutlined /> 빌드 목록으로
      </a-button>
    </div>

    <a-spin :spinning="loading">
      <template v-if="stage && build">
        <!-- Stage Header -->
        <div class="stage-header">
          <div class="header-main">
            <div class="header-info">
              <span class="build-info">Build #{{ build.round }} / {{ build.fwName || '-' }}</span>
              <h1 class="stage-title">
                <CheckCircleOutlined v-if="stage.status === 'success'" :style="{ color: getStatusColor(stage.status) }" />
                <CloseCircleOutlined v-else-if="stage.status === 'failed'" :style="{ color: getStatusColor(stage.status) }" />
                <ReloadOutlined v-else-if="stage.status === 'running'" spin :style="{ color: getStatusColor(stage.status) }" />
                <ClockCircleOutlined v-else :style="{ color: getStatusColor(stage.status) }" />
                {{ stage.name }}
              </h1>
            </div>
            <div class="header-badges">
              <a-tag
                :color="stage.status === 'success' ? 'success' : stage.status === 'failed' ? 'error' : stage.status === 'running' ? 'processing' : 'default'"
              >
                {{ stage.status }}
              </a-tag>
              <span class="duration" v-if="stage.duration">
                <ClockCircleOutlined /> {{ formatDuration(stage.duration) }}
              </span>
            </div>
          </div>

          <div class="header-meta">
            <div class="meta-item" v-if="project">
              <span class="label">Project:</span>
              <span class="value">{{ project.name }}</span>
            </div>
            <div class="meta-item" v-if="build.scmConfig?.branch">
              <span class="label">Branch:</span>
              <span class="value"><BranchesOutlined /> {{ build.scmConfig.branch }}</span>
            </div>
            <div class="meta-item" v-if="stage.startedAt">
              <span class="label">Started:</span>
              <span class="value">{{ formatDateTime(stage.startedAt) }}</span>
            </div>
            <div class="meta-item" v-if="stage.finishedAt">
              <span class="label">Finished:</span>
              <span class="value">{{ formatDateTime(stage.finishedAt) }}</span>
            </div>
          </div>
        </div>

        <!-- Stage Summary -->
        <div v-if="stage.summary" class="content-section">
          <div class="section-header">
            <h3>요약</h3>
          </div>
          <div class="summary-content">
            <div v-if="stage.summary.message" class="summary-message">
              {{ stage.summary.message }}
            </div>
            <div class="summary-stats">
              <div v-if="stage.summary.errorCount !== undefined" class="stat-item error">
                <CloseCircleOutlined />
                <span class="stat-label">Errors</span>
                <span class="stat-value">{{ stage.summary.errorCount }}</span>
              </div>
              <div v-if="stage.summary.warningCount !== undefined" class="stat-item warning">
                <ExclamationCircleOutlined />
                <span class="stat-label">Warnings</span>
                <span class="stat-value">{{ stage.summary.warningCount }}</span>
              </div>
              <div v-if="stage.summary.passedTests !== undefined" class="stat-item success">
                <CheckCircleOutlined />
                <span class="stat-label">Passed Tests</span>
                <span class="stat-value">{{ stage.summary.passedTests }}</span>
              </div>
              <div v-if="stage.summary.failedTests !== undefined" class="stat-item error">
                <CloseCircleOutlined />
                <span class="stat-label">Failed Tests</span>
                <span class="stat-value">{{ stage.summary.failedTests }}</span>
              </div>
              <div v-if="stage.summary.coverage !== undefined" class="stat-item info">
                <FileTextOutlined />
                <span class="stat-label">Coverage</span>
                <span class="stat-value">{{ stage.summary.coverage }}%</span>
              </div>
            </div>
          </div>
        </div>

        <!-- Log Viewer -->
        <div class="content-section">
          <div class="section-header">
            <h3><FileTextOutlined /> 로그</h3>
            <div class="section-actions">
              <a-button size="small" @click="copyLogsToClipboard" :disabled="!stage.logs">
                <CopyOutlined /> 복사
              </a-button>
              <a-button size="small" @click="downloadLog" :disabled="!stage.logUrl">
                <DownloadOutlined /> 전체 로그 다운로드
              </a-button>
            </div>
          </div>
          <div class="log-viewer" v-if="stage.logs">
            <pre>{{ stage.logs }}</pre>
          </div>
          <div v-else class="no-logs">
            로그가 없습니다
          </div>
        </div>

        <!-- Artifacts -->
        <div v-if="stage.artifacts" class="content-section">
          <div class="section-header">
            <h3><DownloadOutlined /> 아티팩트</h3>
          </div>
          <div class="artifacts-list">
            <a-button
              v-if="stage.artifacts.binaryUrl"
              type="primary"
              ghost
              @click="downloadArtifactFile(stage.artifacts.binaryUrl)"
            >
              <DownloadOutlined /> Binary 다운로드
            </a-button>
            <a-button
              v-if="stage.artifacts.logsUrl"
              @click="downloadArtifactFile(stage.artifacts.logsUrl)"
            >
              <FileTextOutlined /> 상세 로그 다운로드
            </a-button>
          </div>
        </div>
      </template>

      <EmptyState v-else-if="!loading" title="스테이지를 찾을 수 없습니다" description="빌드 목록으로 돌아가세요" />
    </a-spin>
  </div>
</template>

<style scoped>
.stage-detail-view {
  display: flex;
  flex-direction: column;
  gap: var(--spacing-lg);
}

.back-nav {
  margin-bottom: var(--spacing-sm);
}

.back-nav :deep(.ant-btn) {
  color: var(--color-text-secondary);
  padding-left: 0;
}

/* Stage Header */
.stage-header {
  background: var(--color-bg-secondary);
  border-radius: var(--radius-md);
  padding: var(--spacing-lg);
  border: 1px solid var(--color-border);
}

.header-main {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  flex-wrap: wrap;
  gap: var(--spacing-md);
  margin-bottom: var(--spacing-md);
}

.header-info {
  display: flex;
  flex-direction: column;
  gap: var(--spacing-xs);
}

.build-info {
  font-size: var(--font-size-sm);
  color: var(--color-text-muted);
}

.stage-title {
  margin: 0;
  font-size: var(--font-size-2xl);
  font-weight: var(--font-weight-bold);
  color: var(--color-text-primary);
  display: flex;
  align-items: center;
  gap: var(--spacing-sm);
}

.header-badges {
  display: flex;
  align-items: center;
  gap: var(--spacing-md);
}

.duration {
  font-size: var(--font-size-md);
  color: var(--color-text-secondary);
  display: flex;
  align-items: center;
  gap: var(--spacing-xs);
}

.header-meta {
  display: flex;
  flex-wrap: wrap;
  gap: var(--spacing-lg);
  color: var(--color-text-secondary);
  font-size: var(--font-size-sm);
}

.meta-item {
  display: flex;
  align-items: center;
  gap: var(--spacing-xs);
}

.meta-item .label {
  color: var(--color-text-muted);
}

.meta-item .value {
  color: var(--color-text-primary);
  font-weight: var(--font-weight-medium);
}

/* Content Section */
.content-section {
  background: var(--color-bg-secondary);
  border-radius: var(--radius-md);
  padding: var(--spacing-lg);
  border: 1px solid var(--color-border);
}

.section-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: var(--spacing-md);
}

.section-header h3 {
  margin: 0;
  font-size: var(--font-size-lg);
  display: flex;
  align-items: center;
  gap: var(--spacing-sm);
}

.section-actions {
  display: flex;
  gap: var(--spacing-sm);
}

/* Summary */
.summary-content {
  display: flex;
  flex-direction: column;
  gap: var(--spacing-md);
}

.summary-message {
  font-size: var(--font-size-md);
  color: var(--color-text-primary);
  padding: var(--spacing-md);
  background: var(--color-bg-tertiary);
  border-radius: var(--radius-sm);
}

.summary-stats {
  display: flex;
  flex-wrap: wrap;
  gap: var(--spacing-md);
}

.stat-item {
  display: flex;
  align-items: center;
  gap: var(--spacing-sm);
  padding: var(--spacing-sm) var(--spacing-md);
  background: var(--color-bg-tertiary);
  border-radius: var(--radius-sm);
  border-left: 3px solid;
}

.stat-item.success {
  border-color: var(--color-accent-success);
  color: var(--color-accent-success);
}

.stat-item.error {
  border-color: var(--color-accent-danger);
  color: var(--color-accent-danger);
}

.stat-item.warning {
  border-color: var(--color-accent-warning);
  color: var(--color-accent-warning);
}

.stat-item.info {
  border-color: var(--color-accent-primary);
  color: var(--color-accent-primary);
}

.stat-label {
  font-size: var(--font-size-sm);
  color: var(--color-text-muted);
}

.stat-value {
  font-size: var(--font-size-lg);
  font-weight: var(--font-weight-bold);
  color: inherit;
}

/* Log Viewer */
.log-viewer {
  background: var(--color-bg-primary);
  border-radius: var(--radius-sm);
  padding: var(--spacing-md);
  max-height: 500px;
  overflow: auto;
}

.log-viewer pre {
  margin: 0;
  font-family: 'Monaco', 'Consolas', 'Courier New', monospace;
  font-size: 13px;
  line-height: 1.6;
  color: var(--color-text-secondary);
  white-space: pre-wrap;
  word-break: break-all;
}

.no-logs {
  padding: var(--spacing-xl);
  text-align: center;
  color: var(--color-text-muted);
}

/* Artifacts */
.artifacts-list {
  display: flex;
  flex-wrap: wrap;
  gap: var(--spacing-sm);
}
</style>
