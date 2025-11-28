<script setup lang="ts">
import { ref, computed, onMounted, watch, h } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import {
  ArrowLeftOutlined,
  TeamOutlined,
  CalendarOutlined,
  SettingOutlined,
  EditOutlined,
  HistoryOutlined,
  DownloadOutlined,
  FileTextOutlined,
  CheckCircleOutlined,
  CloseCircleOutlined,
  ExclamationCircleOutlined,
  LinkOutlined,
  BranchesOutlined,
  CodeOutlined,
  RocketOutlined,
  ReloadOutlined,
  EyeOutlined,
  ClockCircleOutlined,
  BuildOutlined,
  ThunderboltOutlined,
  PlayCircleOutlined,
  InfoCircleOutlined,
  ExpandOutlined,
  FileSearchOutlined,
} from '@ant-design/icons-vue'
import { message, Modal } from 'ant-design-vue'
import { StatusBadge, EmptyState, MilestoneTimeline } from '@/components'
import { useProjectStore } from '@/stores/project'
import { useLayerStore } from '@/stores/layer'
import { useBuildStore } from '@/stores/build'
import { projectHistoryApi } from '@/api/projectHistory'
import type { Build, ProjectHistory, QualityResult, Layer, BuildStage, PipelineStageConfig } from '@/types'

const router = useRouter()
const route = useRoute()
const projectStore = useProjectStore()
const layerStore = useLayerStore()
const buildStore = useBuildStore()

const props = defineProps<{
  projectId: string
}>()

// View state
const currentView = ref<'summary' | 'build' | 'info'>('summary')
const currentLayerId = ref<string | null>(null)
const selectedBuildId = ref<string | null>(null)
const projectHistory = ref<ProjectHistory[]>([])
const isEditingBasicInfo = ref(false)

// Build detail modal
const buildDetailModalVisible = ref(false)
const buildDetailModalData = ref<Build | null>(null)

// Build trigger modal
const buildTriggerModalVisible = ref(false)

// Pipeline settings popover
const activeStagePopover = ref<string | null>(null)

// Get view mode from query params
watch(
  () => [route.query.tab, route.query.layerId],
  ([tab, layerId]) => {
    if (tab === 'build' && layerId) {
      currentView.value = 'build'
      currentLayerId.value = layerId as string
      selectedBuildId.value = null
    } else if (tab === 'info') {
      currentView.value = 'info'
    } else {
      currentView.value = 'summary'
    }
  },
  { immediate: true }
)

async function loadData(projectId: string) {
  await Promise.all([
    projectStore.fetchProjectById(projectId),
    layerStore.fetchLayers(projectId),
    buildStore.fetchBuilds({ projectId })
  ])

  // Load history
  try {
    const response = await projectHistoryApi.getByProjectId(projectId)
    projectHistory.value = response.data
  } catch (e) {
    console.error('Failed to load history', e)
  }
}

onMounted(() => loadData(props.projectId))
watch(() => props.projectId, loadData)

const project = computed(() => projectStore.currentProject)
const projectLayers = computed(() => layerStore.layers.filter(l => l.projectId === props.projectId))

// Members count (excluding TL/PL)
const otherMembersCount = computed(() => {
  if (!project.value?.members) return 0
  return project.value.members.filter(m => m.role !== 'PL' && m.role !== 'TL').length
})

// Current layer for build view
const currentLayer = computed(() => {
  if (!currentLayerId.value) return null
  return projectLayers.value.find(l => l.id === currentLayerId.value) || null
})

// Builds for current layer (max 10)
const layerBuilds = computed(() => {
  if (!currentLayerId.value) return []
  return buildStore.builds
    .filter(b => b.layerId === currentLayerId.value)
    .sort((a, b) => new Date(b.startedAt).getTime() - new Date(a.startedAt).getTime())
    .slice(0, 10)
})

// Selected build detail
const selectedBuild = computed(() => {
  if (!selectedBuildId.value) return null
  return buildStore.builds.find(b => b.id === selectedBuildId.value) || null
})

// Summary data
const summaryData = computed(() => {
  const releaseLayer = projectLayers.value.find(l => l.type === 'release')
  if (!releaseLayer) return null

  const builds = buildStore.builds
    .filter(b => b.layerId === releaseLayer.id)
    .sort((a, b) => new Date(b.startedAt).getTime() - new Date(a.startedAt).getTime())
    .slice(0, 5)

  return {
    layer: releaseLayer,
    latestBuild: builds[0] || null,
    recentBuilds: builds
  }
})

function goBack() {
  router.push('/')
}

function selectBuild(buildId: string) {
  selectedBuildId.value = buildId
}

function getQualityStatus(result?: QualityResult) {
  if (!result) return { icon: ExclamationCircleOutlined, color: 'var(--color-text-muted)', text: '-' }
  switch (result.status) {
    case 'pass': return { icon: CheckCircleOutlined, color: 'var(--color-accent-success)', text: 'Pass' }
    case 'fail': return { icon: CloseCircleOutlined, color: 'var(--color-accent-danger)', text: 'Fail' }
    case 'warning': return { icon: ExclamationCircleOutlined, color: 'var(--color-accent-warning)', text: 'Warning' }
    case 'skipped': return { icon: ExclamationCircleOutlined, color: 'var(--color-text-muted)', text: 'Skipped' }
    default: return { icon: ExclamationCircleOutlined, color: 'var(--color-text-muted)', text: 'Pending' }
  }
}

function canRelease(build: Build): boolean {
  return build.releaseCriteria?.overallPassed === true && build.releaseStatus === 'available'
}

function needsApproval(build: Build): boolean {
  return build.releaseCriteria?.overallPassed === false && build.releaseStatus === 'pending_approval'
}

async function handleRelease(build: Build) {
  try {
    await buildStore.triggerRelease(build.id)
    message.success('릴리즈가 완료되었습니다')
  } catch (e) {
    message.error('릴리즈 처리 중 오류가 발생했습니다')
  }
}

async function handleTriggerBuild() {
  buildTriggerModalVisible.value = true
}

async function confirmTriggerBuild() {
  try {
    message.loading({ content: '빌드를 시작하는 중...', key: 'build-trigger' })
    // API call would go here
    // await buildStore.triggerBuild({ projectId: props.projectId, layerId: currentLayerId.value })

    // Simulate API call
    await new Promise(resolve => setTimeout(resolve, 1000))

    message.success({ content: '빌드가 시작되었습니다!', key: 'build-trigger' })
    buildTriggerModalVisible.value = false

    // Refresh builds
    await buildStore.fetchBuilds({ projectId: props.projectId })
  } catch (e) {
    message.error({ content: '빌드 시작 중 오류가 발생했습니다', key: 'build-trigger' })
  }
}

function openBuildDetailModal(build: Build) {
  buildDetailModalData.value = build
  buildDetailModalVisible.value = true
}

function closeBuildDetailModal() {
  buildDetailModalVisible.value = false
  buildDetailModalData.value = null
}

function navigateToStage(buildId: string, stageName: string) {
  router.push(`/projects/${props.projectId}/builds/${buildId}/stages/${encodeURIComponent(stageName)}`)
}

function handleStageClick(stage: BuildStage, buildId: string) {
  navigateToStage(buildId, stage.name)
}

function downloadArtifact(url: string, filename: string) {
  message.info(`다운로드 시작: ${filename}`)
  // In real implementation, trigger actual download
  window.open(url, '_blank')
}

function getStageSettingsSummary(stage: PipelineStageConfig): string {
  const settings = stage.settings
  if (!settings) return '기본 설정'

  const parts: string[] = []
  if (settings.timeout) parts.push(`타임아웃: ${settings.timeout}분`)
  if (settings.retryCount) parts.push(`재시도: ${settings.retryCount}회`)

  // Stage-specific settings
  if (settings.coverity) parts.push(`임계값: ${settings.coverity.threshold}, 룰셋: ${settings.coverity.ruleSet}`)
  if (settings.sam) parts.push(`레벨: ${settings.sam.level}`)
  if (settings.onboardTest) parts.push(`스위트: ${settings.onboardTest.testSuite}`)
  if (settings.blackduck) parts.push(`스캔: ${settings.blackduck.scanMode}`)
  if (settings.warningCount) parts.push(`최대 워닝: ${settings.warningCount.maxWarnings}`)
  if (settings.codingRuleCheck) parts.push(`룰: ${settings.codingRuleCheck.rules?.join(', ')}`)
  if (settings.build) parts.push(`최적화: ${settings.build.optimization}`)

  return parts.length > 0 ? parts.join(' | ') : '기본 설정'
}

// History helper functions
function getFieldLabel(field: string): string {
  const labels: Record<string, string> = {
    name: '프로젝트명',
    oem: 'OEM',
    feature: 'Feature',
    target: 'Target',
    status: '상태',
    tl: 'TL',
    pl: 'PL',
    members: '팀원',
    milestone: '마일스톤',
    scmConfig: 'SCM 설정',
    buildEnvConfig: '빌드 환경',
    buildOptions: '빌드 옵션',
  }
  return labels[field] || field
}

function getFieldColor(field: string): string {
  const colors: Record<string, string> = {
    name: 'purple',
    oem: 'blue',
    feature: 'cyan',
    status: 'green',
    tl: 'orange',
    pl: 'orange',
    members: 'gold',
    milestone: 'magenta',
    scmConfig: 'geekblue',
    buildEnvConfig: 'geekblue',
  }
  return colors[field] || 'default'
}

function truncateValue(value: string): string {
  if (!value) return '-'
  return value.length > 30 ? value.substring(0, 30) + '...' : value
}

function expandHistoryRow(record: ProjectHistory) {
  return h('div', { class: 'history-expanded' }, [
    h('div', { class: 'expanded-row' }, [
      h('div', { class: 'expanded-label' }, '변경 항목:'),
      h('div', { class: 'expanded-value' }, getFieldLabel(record.field))
    ]),
    h('div', { class: 'expanded-row' }, [
      h('div', { class: 'expanded-label' }, '이전 값:'),
      h('div', { class: 'expanded-value old' }, record.oldValue || '(없음)')
    ]),
    h('div', { class: 'expanded-row' }, [
      h('div', { class: 'expanded-label' }, '변경 값:'),
      h('div', { class: 'expanded-value new' }, record.newValue || '(없음)')
    ]),
    record.reason ? h('div', { class: 'expanded-row' }, [
      h('div', { class: 'expanded-label' }, '변경 사유:'),
      h('div', { class: 'expanded-value' }, record.reason)
    ]) : null
  ])
}

function formatDuration(seconds?: number | null): string {
  if (!seconds) return '-'
  const mins = Math.floor(seconds / 60)
  const secs = seconds % 60
  return `${mins}m ${secs}s`
}

function formatDateTime(dateStr: string): string {
  return new Date(dateStr).toLocaleString('ko-KR', {
    year: 'numeric',
    month: '2-digit',
    day: '2-digit',
    hour: '2-digit',
    minute: '2-digit'
  })
}

function getStatusColor(status: string) {
  const colors: Record<string, string> = {
    success: 'var(--color-accent-success)',
    failed: 'var(--color-accent-danger)',
    running: 'var(--color-accent-warning)',
    pending: 'var(--color-text-muted)',
  }
  return colors[status] || colors.pending
}

function getStatusBgColor(status: string) {
  const colors: Record<string, string> = {
    success: 'rgba(76, 175, 80, 0.1)',
    failed: 'rgba(244, 67, 54, 0.1)',
    running: 'rgba(255, 183, 77, 0.1)',
    pending: 'rgba(148, 163, 184, 0.1)',
  }
  return colors[status] || colors.pending
}

// 스테이지 이름 축약 (두줄 표기를 위해 짧게)
function getStageName(name: string): string {
  const shortNames: Record<string, string> = {
    'Build': 'Build',
    'SAM': 'SAM',
    'Coverity': 'Coverity',
    'DoBEE': 'DoBEE',
    'TASTY': 'TASTY',
    'OnBoard Test': 'OnBoard',
    'Warning Count': 'Warning',
    'Coding Rule Check': 'CodingRule',
    'BlackDuck': 'BlackDuck',
  }
  return shortNames[name] || name
}

// 빌드 수행자 이름 가져오기
function getTriggeredByName(triggeredBy: any): string {
  if (!triggeredBy) return '-'
  if (typeof triggeredBy === 'string') return triggeredBy
  if (triggeredBy.name) return triggeredBy.name
  return '-'
}

// Quality grid columns for summary
const qualityColumns = [
  { title: 'Round', dataIndex: 'round', key: 'round', width: 80 },
  { title: '상태', key: 'status', width: 100 },
  { title: 'Build Result', key: 'buildResult', width: 100 },
  { title: 'Coverity', key: 'coverity', width: 90 },
  { title: 'SAM', key: 'sam', width: 90 },
  { title: 'OnBoard Test', key: 'onboardTest', width: 100 },
  { title: 'DoBEE', key: 'dobee', width: 90 },
  { title: 'Coding Rule', key: 'codingRuleCheck', width: 100 },
  { title: 'BlackDuck', key: 'blackduck', width: 90 },
]
</script>

<template>
  <div class="project-detail-view">
    <!-- Back Navigation -->
    <div class="back-nav">
      <a-button type="text" @click="goBack">
        <ArrowLeftOutlined /> 과제 목록으로
      </a-button>
    </div>

    <a-spin :spinning="projectStore.loading">
      <template v-if="project">
        <!-- Project Header -->
        <div class="project-header">
          <div class="header-main">
            <div class="header-info">
              <span class="task-code">{{ project.taskCode }}</span>
              <h1 class="project-title">{{ project.name }}</h1>
            </div>
            <div class="header-badges">
              <a-tag color="blue">{{ project.oem }}</a-tag>
              <a-tag color="purple">{{ project.feature }}</a-tag>
              <StatusBadge :status="project.status" type="project" />
            </div>
          </div>

          <!-- Milestone Progress Bar -->
          <div v-if="project.milestones" class="milestone-bar">
            <div class="milestone-track">
              <div
                v-for="(ms, index) in project.milestones"
                :key="ms.name"
                :class="['milestone-point', { active: ms.status === 'in_progress', completed: ms.status === 'completed' }]"
                :style="{ left: `${(index / (project.milestones.length - 1)) * 100}%` }"
              >
                <div class="milestone-dot" />
                <span class="milestone-label">{{ ms.name }}</span>
                <span v-if="ms.targetDate" class="milestone-date">{{ new Date(ms.targetDate).toLocaleDateString('ko-KR', { month: 'short', day: 'numeric' }) }}</span>
              </div>
              <div class="milestone-progress" :style="{ width: `${(project.milestones.findIndex(m => m.status === 'in_progress') / (project.milestones.length - 1)) * 100}%` }" />
            </div>
          </div>

          <div class="header-meta">
            <div class="meta-item">
              <TeamOutlined />
              <span v-if="project.tl">TL: <strong>{{ project.tl.name }}</strong></span>
              <span v-if="project.pl" class="meta-separator">|</span>
              <span v-if="project.pl">PL: <strong>{{ project.pl.name }}</strong></span>
              <span v-if="otherMembersCount > 0" class="member-count">외 {{ otherMembersCount }}명</span>
            </div>
            <div v-if="project.kpiScore" class="meta-item kpi-score">
              <span class="kpi-label">KPI</span>
              <span class="kpi-value">{{ project.kpiScore }}</span>
            </div>
          </div>
        </div>

        <!-- SUMMARY VIEW -->
        <div v-if="currentView === 'summary'" class="content-section">
          <div class="summary-section">
            <!-- Latest Build Summary -->
            <div v-if="summaryData?.latestBuild" class="latest-build-card">
              <div class="build-summary-header">
                <h3>최신 Release 빌드</h3>
                <a-tag :color="summaryData.latestBuild.status === 'success' ? 'success' : summaryData.latestBuild.status === 'failed' ? 'error' : 'processing'">
                  {{ summaryData.latestBuild.status }}
                </a-tag>
              </div>
              <div class="build-summary-info">
                <div class="info-item">
                  <span class="label">Round</span>
                  <span class="value">{{ summaryData.latestBuild.round }}</span>
                </div>
                <div class="info-item">
                  <span class="label">FW Name</span>
                  <span class="value">{{ summaryData.latestBuild.fwName || '-' }}</span>
                </div>
                <div class="info-item">
                  <span class="label">Branch</span>
                  <span class="value">{{ summaryData.latestBuild.scmConfig?.branch }}</span>
                </div>
                <div class="info-item">
                  <span class="label">Duration</span>
                  <span class="value">{{ formatDuration(summaryData.latestBuild.duration) }}</span>
                </div>
              </div>
            </div>

            <!-- Quality Grid -->
            <div v-if="summaryData?.recentBuilds.length" class="quality-grid">
              <h4>품질 지표 (Quality Grid)</h4>
              <a-table
                :columns="qualityColumns"
                :data-source="summaryData.recentBuilds"
                :pagination="false"
                row-key="id"
                size="small"
                :scroll="{ x: 900 }"
              >
                <template #bodyCell="{ column, record }">
                  <template v-if="column.key === 'status'">
                    <a-tag :color="record.status === 'success' ? 'success' : record.status === 'failed' ? 'error' : 'default'">
                      {{ record.status }}
                    </a-tag>
                  </template>
                  <template v-else-if="column.key === 'buildResult'">
                    <a-tag :color="record.status === 'success' ? 'success' : 'error'">
                      {{ record.status === 'success' ? 'Pass' : 'Fail' }}
                    </a-tag>
                  </template>
                  <template v-else-if="['coverity', 'sam', 'onboardTest', 'dobee', 'codingRuleCheck', 'blackduck'].includes(column.key)">
                    <div class="quality-cell">
                      <component
                        :is="getQualityStatus(record.qualityMetrics?.[column.key]).icon"
                        :style="{ color: getQualityStatus(record.qualityMetrics?.[column.key]).color }"
                      />
                      <span v-if="record.qualityMetrics?.[column.key]?.score" class="score">
                        {{ record.qualityMetrics[column.key].score }}
                      </span>
                    </div>
                  </template>
                </template>
              </a-table>
            </div>

            <EmptyState v-else title="Release 빌드가 없습니다" description="좌측 메뉴에서 빌드를 선택해주세요" />
          </div>
        </div>

        <!-- BUILD VIEW (Single Page: Config → Build Button → Results) -->
        <div v-else-if="currentView === 'build' && currentLayer" class="content-section">
          <div class="build-page">
            <!-- Layer Header -->
            <div class="layer-header">
              <div class="layer-info">
                <h2>
                  <RocketOutlined v-if="currentLayer.type === 'release'" />
                  <CodeOutlined v-else-if="currentLayer.type === 'layer'" />
                  <BuildOutlined v-else />
                  {{ currentLayer.name }}
                </h2>
                <a-tag :color="currentLayer.type === 'release' ? 'green' : currentLayer.type === 'layer' ? 'blue' : 'purple'">
                  {{ currentLayer.type }}
                </a-tag>
              </div>
            </div>

            <!-- Build Configuration Section -->
            <div class="config-section">
              <div class="section-title">
                <h3><SettingOutlined /> 빌드 설정</h3>
                <a-button type="text" size="small">
                  <EditOutlined /> 수정
                </a-button>
              </div>

              <div class="config-grid">
                <!-- SCM Config -->
                <div class="config-card">
                  <h4><BranchesOutlined /> SCM</h4>
                  <div class="config-item">
                    <span class="label">Repository</span>
                    <code>{{ project.scmConfig?.repository }}</code>
                  </div>
                  <div class="config-item">
                    <span class="label">Branch</span>
                    <a-tag color="blue">{{ project.scmConfig?.branch }}</a-tag>
                  </div>
                  <div class="config-item">
                    <span class="label">Revision</span>
                    <span>{{ project.scmConfig?.revisionTag || '-' }}</span>
                  </div>
                </div>

                <!-- Build Env Config -->
                <div class="config-card">
                  <h4><CodeOutlined /> Build Environment</h4>
                  <div class="config-item">
                    <span class="label">Batch File</span>
                    <code>{{ project.buildEnvConfig?.batchFilePath }}</code>
                  </div>
                  <div class="config-item">
                    <span class="label">Options</span>
                    <code>{{ project.buildEnvConfig?.buildBatchOptions }}</code>
                  </div>
                </div>

                <!-- Pipeline Stages with Settings -->
                <div class="config-card wide">
                  <h4><ThunderboltOutlined /> Pipeline Stages</h4>
                  <div class="pipeline-stages-table">
                    <div
                      v-for="stage in currentLayer.pipelineConfig"
                      :key="stage.name"
                      :class="['stage-row', { disabled: !stage.enabled }]"
                    >
                      <div class="stage-main">
                        <a-switch v-model:checked="stage.enabled" size="small" />
                        <span class="stage-name">{{ stage.name }}</span>
                        <a-tag v-if="stage.required" color="error" size="small">필수</a-tag>
                      </div>
                      <div class="stage-settings">
                        <a-popover
                          trigger="click"
                          placement="bottom"
                          :title="`${stage.name} 설정`"
                        >
                          <template #content>
                            <div class="stage-settings-content">
                              <div v-if="stage.settings" class="settings-list">
                                <div v-if="stage.settings.timeout" class="setting-item">
                                  <span class="setting-label">타임아웃:</span>
                                  <span class="setting-value">{{ stage.settings.timeout }}분</span>
                                </div>
                                <div v-if="stage.settings.retryCount" class="setting-item">
                                  <span class="setting-label">재시도:</span>
                                  <span class="setting-value">{{ stage.settings.retryCount }}회</span>
                                </div>
                                <!-- Stage-specific settings -->
                                <template v-if="stage.settings.coverity">
                                  <div class="setting-item">
                                    <span class="setting-label">결함 임계값:</span>
                                    <span class="setting-value">{{ stage.settings.coverity.threshold }}</span>
                                  </div>
                                  <div class="setting-item">
                                    <span class="setting-label">룰셋:</span>
                                    <span class="setting-value">{{ stage.settings.coverity.ruleSet }}</span>
                                  </div>
                                </template>
                                <template v-if="stage.settings.sam">
                                  <div class="setting-item">
                                    <span class="setting-label">분석 레벨:</span>
                                    <span class="setting-value">{{ stage.settings.sam.level }}</span>
                                  </div>
                                </template>
                                <template v-if="stage.settings.onboardTest">
                                  <div class="setting-item">
                                    <span class="setting-label">테스트 스위트:</span>
                                    <span class="setting-value">{{ stage.settings.onboardTest.testSuite }}</span>
                                  </div>
                                  <div v-if="stage.settings.onboardTest.parallelCount" class="setting-item">
                                    <span class="setting-label">병렬 실행:</span>
                                    <span class="setting-value">{{ stage.settings.onboardTest.parallelCount }}</span>
                                  </div>
                                </template>
                                <template v-if="stage.settings.blackduck">
                                  <div class="setting-item">
                                    <span class="setting-label">스캔 모드:</span>
                                    <span class="setting-value">{{ stage.settings.blackduck.scanMode }}</span>
                                  </div>
                                </template>
                                <template v-if="stage.settings.warningCount">
                                  <div class="setting-item">
                                    <span class="setting-label">최대 워닝:</span>
                                    <span class="setting-value">{{ stage.settings.warningCount.maxWarnings }}</span>
                                  </div>
                                </template>
                                <template v-if="stage.settings.codingRuleCheck">
                                  <div class="setting-item">
                                    <span class="setting-label">적용 룰:</span>
                                    <span class="setting-value">{{ stage.settings.codingRuleCheck.rules?.join(', ') }}</span>
                                  </div>
                                </template>
                                <template v-if="stage.settings.build">
                                  <div class="setting-item">
                                    <span class="setting-label">최적화:</span>
                                    <span class="setting-value">{{ stage.settings.build.optimization }}</span>
                                  </div>
                                  <div class="setting-item">
                                    <span class="setting-label">클린 빌드:</span>
                                    <span class="setting-value">{{ stage.settings.build.cleanBuild ? 'Yes' : 'No' }}</span>
                                  </div>
                                </template>
                              </div>
                              <div v-else class="no-settings">
                                기본 설정 사용 중
                              </div>
                            </div>
                          </template>
                          <a-button type="text" size="small" class="settings-btn">
                            <SettingOutlined />
                            <span class="settings-summary">{{ getStageSettingsSummary(stage) }}</span>
                          </a-button>
                        </a-popover>
                      </div>
                    </div>
                  </div>
                </div>
              </div>
            </div>

            <!-- Build Trigger Button (간격 축소) -->
            <div class="build-action compact">
              <a-button type="primary" size="large" @click="handleTriggerBuild" class="build-trigger-btn">
                <PlayCircleOutlined /> 빌드 실행
              </a-button>
            </div>

            <!-- Build Results Section (테이블 형식) -->
            <div class="results-section">
              <div class="section-title">
                <h3><HistoryOutlined /> 최근 빌드 결과</h3>
              </div>

              <div v-if="layerBuilds.length > 0" class="build-results-table">
                <a-table
                  :data-source="layerBuilds"
                  :pagination="false"
                  row-key="id"
                  size="small"
                  :row-class-name="(record: Build) => record.id === selectedBuildId ? 'selected-row' : ''"
                  @row-click="(record: Build) => selectBuild(record.id)"
                >
                  <!-- Round -->
                  <a-table-column title="Round" key="round" :width="80">
                    <template #default="{ record }">
                      <span class="build-round">#{{ record.round }}</span>
                    </template>
                  </a-table-column>

                  <!-- Status -->
                  <a-table-column title="상태" key="status" :width="90">
                    <template #default="{ record }">
                      <a-tag
                        :color="record.status === 'success' ? 'success' : record.status === 'failed' ? 'error' : record.status === 'running' ? 'processing' : 'default'"
                        size="small"
                      >
                        {{ record.status }}
                      </a-tag>
                    </template>
                  </a-table-column>

                  <!-- FW Name -->
                  <a-table-column title="FW Name" key="fwName" :width="180">
                    <template #default="{ record }">
                      <span class="fw-name">{{ record.fwName || '-' }}</span>
                    </template>
                  </a-table-column>

                  <!-- Branch -->
                  <a-table-column title="Branch" key="branch" :width="140">
                    <template #default="{ record }">
                      <span class="branch-name">
                        <BranchesOutlined /> {{ record.scmConfig?.branch || '-' }}
                      </span>
                    </template>
                  </a-table-column>

                  <!-- Pipeline Stages (핵심: Popover + 클릭 이동) - 이름 표시 버전 -->
                  <a-table-column title="파이프라인" key="stages">
                    <template #default="{ record }">
                      <div class="pipeline-grid">
                        <a-popover
                          v-for="stage in record.stages"
                          :key="stage.name"
                          trigger="hover"
                          placement="bottom"
                        >
                          <template #content>
                            <div class="stage-popover-content">
                              <div class="stage-popover-header">
                                <strong>{{ stage.name }}</strong>
                                <a-tag
                                  :color="stage.status === 'success' ? 'success' : stage.status === 'failed' ? 'error' : stage.status === 'running' ? 'processing' : 'default'"
                                  size="small"
                                >
                                  {{ stage.status }}
                                </a-tag>
                              </div>
                              <div v-if="stage.duration" class="stage-popover-duration">
                                <ClockCircleOutlined /> {{ formatDuration(stage.duration) }}
                              </div>
                              <div v-if="stage.summary" class="stage-popover-summary">
                                {{ stage.summary.message }}
                              </div>
                              <div v-if="stage.logs" class="stage-popover-logs">
                                <pre>{{ stage.logs }}</pre>
                              </div>
                              <a-button type="link" size="small" @click.stop="handleStageClick(stage, record.id)">
                                <FileSearchOutlined /> Stage 상세 보기
                              </a-button>
                            </div>
                          </template>
                          <div
                            class="stage-chip-labeled"
                            :class="stage.status"
                            @click.stop="handleStageClick(stage, record.id)"
                          >
                            <span class="stage-chip-icon">
                              <CheckCircleOutlined v-if="stage.status === 'success'" />
                              <CloseCircleOutlined v-else-if="stage.status === 'failed'" />
                              <ReloadOutlined v-else-if="stage.status === 'running'" spin />
                              <ClockCircleOutlined v-else />
                            </span>
                            <span class="stage-chip-name">{{ getStageName(stage.name) }}</span>
                          </div>
                        </a-popover>
                      </div>
                    </template>
                  </a-table-column>

                  <!-- Triggered By + Time (합침) -->
                  <a-table-column title="수행자 / 시간" key="triggeredBy" :width="180">
                    <template #default="{ record }">
                      <div class="triggered-info">
                        <div class="triggered-by">
                          <TeamOutlined />
                          <span>{{ getTriggeredByName(record.triggeredBy) }}</span>
                        </div>
                        <div class="time-info">
                          <span class="start-time">{{ formatDateTime(record.startedAt) }}</span>
                          <span class="duration" v-if="record.duration">{{ formatDuration(record.duration) }}</span>
                        </div>
                      </div>
                    </template>
                  </a-table-column>

                  <!-- TR Status (Release Layer only) -->
                  <a-table-column v-if="currentLayer?.type === 'release'" title="TR" key="tr" :width="100">
                    <template #default="{ record }">
                      <a-button
                        v-if="canRelease(record)"
                        type="primary"
                        size="small"
                        @click.stop="handleRelease(record)"
                      >
                        TR
                      </a-button>
                      <a-button
                        v-else-if="needsApproval(record)"
                        size="small"
                        danger
                      >
                        결재 필요
                      </a-button>
                      <a-tag v-else-if="record.releaseStatus === 'released'" color="success" size="small">
                        Released
                      </a-tag>
                      <span v-else>-</span>
                    </template>
                  </a-table-column>

                  <!-- Actions -->
                  <a-table-column title="" key="actions" :width="80">
                    <template #default="{ record }">
                      <a-button type="link" size="small" @click.stop="openBuildDetailModal(record)">
                        <ExpandOutlined /> 상세
                      </a-button>
                    </template>
                  </a-table-column>
                </a-table>
              </div>

              <EmptyState v-else title="빌드 이력이 없습니다" description="빌드 실행 버튼을 눌러 첫 빌드를 시작하세요" />
            </div>
          </div>
        </div>

        <!-- INFO VIEW -->
        <div v-else-if="currentView === 'info'" class="content-section">
          <div class="basic-info-section">
            <div class="section-header">
              <h3>기본 정보</h3>
              <a-button type="primary" ghost @click="isEditingBasicInfo = !isEditingBasicInfo">
                <EditOutlined /> {{ isEditingBasicInfo ? '취소' : '수정' }}
              </a-button>
            </div>

            <a-row :gutter="[24, 24]">
              <!-- Product Info -->
              <a-col :span="12">
                <div class="info-card">
                  <h4><SettingOutlined /> Product 정보</h4>
                  <a-descriptions :column="1" size="small" bordered>
                    <a-descriptions-item label="OEM">{{ project.oem }}</a-descriptions-item>
                    <a-descriptions-item label="Feature">{{ project.feature }}</a-descriptions-item>
                    <a-descriptions-item label="Target">{{ project.target }}</a-descriptions-item>
                    <a-descriptions-item label="Task Code">{{ project.taskCode }}</a-descriptions-item>
                  </a-descriptions>
                </div>
              </a-col>

              <!-- Members Info -->
              <a-col :span="12">
                <div class="info-card">
                  <h4><TeamOutlined /> Members 정보</h4>
                  <div class="members-summary">
                    <!-- Key Members (TL/PL) -->
                    <div class="key-members">
                      <div v-if="project.tl" class="member-item highlight">
                        <a-avatar size="default" :style="{ backgroundColor: 'var(--color-accent-primary)' }">
                          {{ project.tl.name.charAt(0) }}
                        </a-avatar>
                        <div class="member-info">
                          <span class="name">{{ project.tl.name }}</span>
                          <span class="role-badge tl">TL</span>
                        </div>
                      </div>
                      <div v-if="project.pl" class="member-item highlight">
                        <a-avatar size="default" :style="{ backgroundColor: 'var(--color-accent-secondary)' }">
                          {{ project.pl.name.charAt(0) }}
                        </a-avatar>
                        <div class="member-info">
                          <span class="name">{{ project.pl.name }}</span>
                          <span class="role-badge pl">PL</span>
                        </div>
                      </div>
                    </div>
                    <!-- Other Members Summary -->
                    <div v-if="otherMembersCount > 0" class="other-members">
                      <a-avatar-group :max-count="5" size="small">
                        <a-avatar
                          v-for="member in project.members?.filter(m => m.role !== 'PL' && m.role !== 'TL').slice(0, 5)"
                          :key="member.id"
                          :style="{ backgroundColor: '#4a4a52' }"
                        >
                          {{ member.name.charAt(0) }}
                        </a-avatar>
                      </a-avatar-group>
                      <span class="members-count">외 {{ otherMembersCount }}명</span>
                    </div>
                  </div>
                </div>
              </a-col>

              <!-- PLM Info -->
              <a-col :span="12">
                <div class="info-card">
                  <h4><LinkOutlined /> PLM 정보</h4>
                  <div v-if="project.plmInfo">
                    <p><strong>PLM ID:</strong> {{ project.plmInfo.plmId }}</p>
                    <a :href="project.plmInfo.plmLink" target="_blank" class="plm-link">
                      PLM 바로가기 <LinkOutlined />
                    </a>
                  </div>
                  <p v-else class="text-muted">PLM 연동 정보 없음</p>
                </div>
              </a-col>

            </a-row>

            <!-- History Section (Expandable) -->
            <div class="history-section">
              <h3><HistoryOutlined /> 변경 이력</h3>
              <a-table
                :data-source="projectHistory"
                :pagination="{ pageSize: 5 }"
                row-key="id"
                size="small"
                :expandable="{
                  expandedRowRender: expandHistoryRow,
                  expandRowByClick: true
                }"
              >
                <a-table-column title="일시" key="changedAt" width="180">
                  <template #default="{ record }">
                    {{ new Date(record.changedAt).toLocaleString('ko-KR') }}
                  </template>
                </a-table-column>
                <a-table-column title="변경자" key="changedBy" width="120">
                  <template #default="{ record }">
                    <a-avatar size="small" :style="{ backgroundColor: 'var(--color-accent-primary)', marginRight: '8px' }">
                      {{ record.changedBy?.name?.charAt(0) || '?' }}
                    </a-avatar>
                    {{ record.changedBy?.name || '-' }}
                  </template>
                </a-table-column>
                <a-table-column title="변경 항목" key="field" width="150">
                  <template #default="{ record }">
                    <a-tag :color="getFieldColor(record.field)">{{ getFieldLabel(record.field) }}</a-tag>
                  </template>
                </a-table-column>
                <a-table-column title="요약" key="summary">
                  <template #default="{ record }">
                    <span class="history-summary">
                      <span class="old-value">{{ truncateValue(record.oldValue) }}</span>
                      <span class="arrow">→</span>
                      <span class="new-value">{{ truncateValue(record.newValue) }}</span>
                    </span>
                  </template>
                </a-table-column>
              </a-table>
            </div>
          </div>
        </div>

        <!-- Empty State for Build View without layer -->
        <div v-else-if="currentView === 'build' && !currentLayer" class="content-section">
          <EmptyState title="레이어를 선택해주세요" description="좌측 BUILD 메뉴에서 빌드할 레이어를 선택하세요" />
        </div>
      </template>

      <EmptyState v-else-if="!projectStore.loading" title="과제를 찾을 수 없습니다" />
    </a-spin>

    <!-- Build Detail Modal (팝업 상세보기) -->
    <a-modal
      v-model:open="buildDetailModalVisible"
      :title="`Build #${buildDetailModalData?.round} 상세`"
      width="900px"
      :footer="null"
      @cancel="closeBuildDetailModal"
    >
      <div v-if="buildDetailModalData" class="build-detail-modal">
        <!-- Basic Info -->
        <div class="modal-section">
          <h4>기본 정보</h4>
          <a-descriptions :column="2" size="small" bordered>
            <a-descriptions-item label="FW Name">{{ buildDetailModalData.fwName || '-' }}</a-descriptions-item>
            <a-descriptions-item label="Status">
              <a-tag :color="buildDetailModalData.status === 'success' ? 'success' : buildDetailModalData.status === 'failed' ? 'error' : 'processing'">
                {{ buildDetailModalData.status }}
              </a-tag>
            </a-descriptions-item>
            <a-descriptions-item label="Branch">
              <BranchesOutlined /> {{ buildDetailModalData.scmConfig?.branch }}
            </a-descriptions-item>
            <a-descriptions-item label="Revision">{{ buildDetailModalData.scmConfig?.revisionTag || '-' }}</a-descriptions-item>
            <a-descriptions-item label="Duration">
              <ClockCircleOutlined /> {{ formatDuration(buildDetailModalData.duration) }}
            </a-descriptions-item>
            <a-descriptions-item label="Triggered By">
              {{ typeof buildDetailModalData.triggeredBy === 'string' ? buildDetailModalData.triggeredBy : buildDetailModalData.triggeredBy?.name || '-' }}
            </a-descriptions-item>
            <a-descriptions-item label="Started At">{{ formatDateTime(buildDetailModalData.startedAt) }}</a-descriptions-item>
            <a-descriptions-item label="Finished At">{{ buildDetailModalData.finishedAt ? formatDateTime(buildDetailModalData.finishedAt) : '-' }}</a-descriptions-item>
          </a-descriptions>
        </div>

        <!-- Pipeline Stages -->
        <div class="modal-section">
          <h4>파이프라인 상세</h4>
          <div class="pipeline-flow-modal">
            <div
              v-for="(stage, index) in buildDetailModalData.stages"
              :key="stage.name"
              class="pipeline-stage-modal"
            >
              <div
                class="stage-box-modal"
                :class="stage.status"
                @click="handleStageClick(stage, buildDetailModalData.id)"
              >
                <div class="stage-icon-modal">
                  <CheckCircleOutlined v-if="stage.status === 'success'" />
                  <CloseCircleOutlined v-else-if="stage.status === 'failed'" />
                  <ReloadOutlined v-else-if="stage.status === 'running'" spin />
                  <ClockCircleOutlined v-else />
                </div>
                <div class="stage-info-modal">
                  <span class="stage-name">{{ stage.name }}</span>
                  <span class="stage-duration">{{ formatDuration(stage.duration) }}</span>
                  <span v-if="stage.summary?.message" class="stage-summary">{{ stage.summary.message }}</span>
                </div>
              </div>
              <div v-if="index < buildDetailModalData.stages.length - 1" class="stage-connector-modal" :class="stage.status" />
            </div>
          </div>
        </div>

        <!-- Quality Metrics -->
        <div v-if="buildDetailModalData.qualityMetrics" class="modal-section">
          <h4>품질 지표</h4>
          <div class="metrics-grid-modal">
            <div
              v-for="(metric, key) in buildDetailModalData.qualityMetrics"
              :key="key"
              class="metric-card-modal"
            >
              <div class="metric-header-modal">
                <span class="metric-name">{{ key }}</span>
                <component
                  :is="getQualityStatus(metric).icon"
                  :style="{ color: getQualityStatus(metric).color }"
                />
              </div>
              <div v-if="metric?.score" class="metric-score-modal">{{ metric.score }}</div>
              <div v-if="metric?.issues !== undefined" class="metric-issues">이슈: {{ metric.issues }}</div>
            </div>
          </div>
        </div>

        <!-- Artifacts -->
        <div v-if="buildDetailModalData.artifacts" class="modal-section">
          <h4>아티팩트</h4>
          <div class="artifacts-list">
            <a-button
              v-if="buildDetailModalData.artifacts.binaryUrl"
              type="primary"
              @click="downloadArtifact(buildDetailModalData.artifacts.binaryUrl, 'firmware.bin')"
            >
              <DownloadOutlined /> Binary 다운로드
            </a-button>
            <a-button
              v-if="buildDetailModalData.artifacts.logsUrl"
              @click="downloadArtifact(buildDetailModalData.artifacts.logsUrl, 'build.log')"
            >
              <FileTextOutlined /> 로그 다운로드
            </a-button>
            <a-button
              v-if="buildDetailModalData.artifacts.sourceZipUrl"
              @click="downloadArtifact(buildDetailModalData.artifacts.sourceZipUrl, 'source.zip')"
            >
              <DownloadOutlined /> 소스 다운로드
            </a-button>
          </div>
        </div>
      </div>
    </a-modal>

    <!-- Build Trigger Confirmation Modal -->
    <a-modal
      v-model:open="buildTriggerModalVisible"
      title="빌드 실행 확인"
      @ok="confirmTriggerBuild"
      @cancel="buildTriggerModalVisible = false"
      ok-text="빌드 시작"
      cancel-text="취소"
    >
      <div class="build-trigger-confirm">
        <p>다음 설정으로 빌드를 시작합니다:</p>
        <a-descriptions v-if="currentLayer && project" :column="1" size="small" bordered>
          <a-descriptions-item label="Project">{{ project.name }}</a-descriptions-item>
          <a-descriptions-item label="Layer">{{ currentLayer.name }} ({{ currentLayer.type }})</a-descriptions-item>
          <a-descriptions-item label="Branch">{{ project.scmConfig?.branch }}</a-descriptions-item>
          <a-descriptions-item label="Pipeline Stages">
            {{ currentLayer.pipelineConfig?.filter(s => s.enabled).map(s => s.name).join(', ') }}
          </a-descriptions-item>
        </a-descriptions>
      </div>
    </a-modal>
  </div>
</template>

<style scoped>
.project-detail-view {
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

/* Project Header */
.project-header {
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

.task-code {
  font-size: var(--font-size-sm);
  color: var(--color-text-muted);
}

.project-title {
  margin: 0;
  font-size: var(--font-size-3xl);
  font-weight: var(--font-weight-bold);
  color: var(--color-text-primary);
}

.header-badges {
  display: flex;
  gap: var(--spacing-sm);
  flex-wrap: wrap;
}

.header-meta {
  display: flex;
  justify-content: space-between;
  align-items: center;
  gap: var(--spacing-xl);
  color: var(--color-text-secondary);
  font-size: var(--font-size-md);
}

.meta-item {
  display: flex;
  align-items: center;
  gap: var(--spacing-sm);
}

.meta-separator {
  color: var(--color-border);
  margin: 0 var(--spacing-xs);
}

.member-count {
  color: var(--color-text-muted);
  font-size: var(--font-size-sm);
  margin-left: var(--spacing-sm);
  padding: 2px 8px;
  background: var(--color-bg-tertiary);
  border-radius: var(--radius-full);
}

.kpi-score {
  background: linear-gradient(135deg, rgba(0, 212, 170, 0.15), rgba(0, 212, 170, 0.05));
  border: 1px solid rgba(0, 212, 170, 0.3);
  border-radius: var(--radius-md);
  padding: var(--spacing-xs) var(--spacing-md);
}

.kpi-label {
  font-size: var(--font-size-xs);
  color: var(--color-accent-primary);
  font-weight: var(--font-weight-medium);
}

.kpi-value {
  font-size: var(--font-size-lg);
  font-weight: var(--font-weight-bold);
  color: var(--color-accent-primary);
  margin-left: var(--spacing-xs);
}

/* Milestone Progress Bar */
.milestone-bar {
  margin: var(--spacing-lg) 0;
  padding: var(--spacing-md) var(--spacing-xl);
  background: var(--color-bg-tertiary);
  border-radius: var(--radius-sm);
}

.milestone-track {
  position: relative;
  height: 60px;
  margin: 0 40px;
}

.milestone-track::before {
  content: '';
  position: absolute;
  top: 10px;
  left: 0;
  right: 0;
  height: 4px;
  background: var(--color-border);
  border-radius: 2px;
}

.milestone-progress {
  position: absolute;
  top: 10px;
  left: 0;
  height: 4px;
  background: linear-gradient(90deg, var(--color-accent-primary), var(--color-accent-secondary));
  border-radius: 2px;
  transition: width 0.3s ease;
}

.milestone-point {
  position: absolute;
  transform: translateX(-50%);
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 4px;
}

.milestone-dot {
  width: 16px;
  height: 16px;
  border-radius: 50%;
  background: var(--color-bg-secondary);
  border: 3px solid var(--color-text-muted);
  position: relative;
  z-index: 1;
}

.milestone-point.completed .milestone-dot {
  background: var(--color-accent-primary);
  border-color: var(--color-accent-primary);
}

.milestone-point.active .milestone-dot {
  background: var(--color-accent-secondary);
  border-color: var(--color-accent-secondary);
  box-shadow: 0 0 0 4px rgba(255, 183, 77, 0.3);
}

.milestone-label {
  font-size: var(--font-size-sm);
  font-weight: var(--font-weight-medium);
  color: var(--color-text-muted);
  margin-top: 4px;
}

.milestone-point.active .milestone-label,
.milestone-point.completed .milestone-label {
  color: var(--color-text-primary);
}

.milestone-date {
  font-size: var(--font-size-xs);
  color: var(--color-text-muted);
}

/* Content Section */
.content-section {
  background: var(--color-bg-secondary);
  border-radius: var(--radius-md);
  padding: var(--spacing-lg);
  border: 1px solid var(--color-border);
}

/* Summary Section */
.summary-section {
  display: flex;
  flex-direction: column;
  gap: var(--spacing-xl);
}

.latest-build-card {
  background: var(--color-bg-tertiary);
  border-radius: var(--radius-md);
  padding: var(--spacing-lg);
}

.build-summary-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: var(--spacing-md);
}

.build-summary-header h3 {
  margin: 0;
  font-size: var(--font-size-lg);
}

.build-summary-info {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: var(--spacing-lg);
}

.info-item {
  display: flex;
  flex-direction: column;
  gap: var(--spacing-xs);
}

.info-item .label {
  font-size: var(--font-size-sm);
  color: var(--color-text-muted);
}

.info-item .value {
  font-size: var(--font-size-md);
  font-weight: var(--font-weight-medium);
  color: var(--color-text-primary);
}

.quality-grid h4 {
  margin: 0 0 var(--spacing-md);
  font-size: var(--font-size-lg);
  color: var(--color-text-primary);
}

.quality-cell {
  display: flex;
  align-items: center;
  gap: var(--spacing-xs);
}

.quality-cell .score {
  font-size: var(--font-size-sm);
  color: var(--color-text-secondary);
}

/* Build Page */
.build-page {
  display: flex;
  flex-direction: column;
  gap: var(--spacing-xl);
}

.layer-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.layer-info {
  display: flex;
  align-items: center;
  gap: var(--spacing-md);
}

.layer-info h2 {
  margin: 0;
  font-size: var(--font-size-2xl);
  display: flex;
  align-items: center;
  gap: var(--spacing-sm);
}

/* Config Section */
.config-section {
  background: var(--color-bg-tertiary);
  border-radius: var(--radius-md);
  padding: var(--spacing-lg);
}

.section-title {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: var(--spacing-lg);
}

.section-title h3 {
  margin: 0;
  font-size: var(--font-size-lg);
  display: flex;
  align-items: center;
  gap: var(--spacing-sm);
}

.config-grid {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: var(--spacing-lg);
}

.config-card {
  background: var(--color-bg-secondary);
  border-radius: var(--radius-sm);
  padding: var(--spacing-md);
}

.config-card.wide {
  grid-column: span 2;
}

.config-card h4 {
  margin: 0 0 var(--spacing-md);
  font-size: var(--font-size-md);
  color: var(--color-text-secondary);
  display: flex;
  align-items: center;
  gap: var(--spacing-sm);
}

.config-item {
  display: flex;
  flex-direction: column;
  gap: 4px;
  margin-bottom: var(--spacing-sm);
}

.config-item .label {
  font-size: var(--font-size-xs);
  color: var(--color-text-muted);
  text-transform: uppercase;
}

.config-item code {
  background: var(--color-bg-primary);
  padding: 4px 8px;
  border-radius: var(--radius-xs);
  font-size: var(--font-size-sm);
  color: var(--color-text-primary);
}

.pipeline-stages {
  display: flex;
  flex-wrap: wrap;
  gap: var(--spacing-sm);
}

.stage-chip {
  display: flex;
  align-items: center;
  gap: var(--spacing-xs);
  padding: 6px 12px;
  background: var(--color-bg-primary);
  border-radius: var(--radius-sm);
  font-size: var(--font-size-sm);
}

.stage-chip.disabled {
  opacity: 0.5;
}

.stage-chip.required {
  border: 1px solid var(--color-accent-danger);
}

/* Build Action (간격 축소) */
.build-action {
  display: flex;
  justify-content: center;
  padding: var(--spacing-lg) 0;
}

.build-action.compact {
  padding: var(--spacing-sm) 0;
}

.build-trigger-btn {
  background: var(--color-accent-primary);
  border: none;
  height: 48px;
  padding: 0 var(--spacing-xl);
  font-size: var(--font-size-md);
  font-weight: var(--font-weight-semibold);
}

.build-trigger-btn:hover {
  background: var(--color-accent-secondary) !important;
}

/* Results Section */
.results-section {
  background: var(--color-bg-tertiary);
  border-radius: var(--radius-md);
  padding: var(--spacing-lg);
}

.build-results {
  display: grid;
  grid-template-columns: 360px 1fr;
  gap: var(--spacing-lg);
  min-height: 400px;
}

.build-list {
  display: flex;
  flex-direction: column;
  gap: var(--spacing-sm);
  max-height: 600px;
  overflow-y: auto;
}

.build-card {
  background: var(--color-bg-secondary);
  border-radius: var(--radius-sm);
  padding: var(--spacing-md);
  cursor: pointer;
  border: 2px solid transparent;
  transition: all var(--transition-fast);
}

.build-card:hover {
  border-color: var(--color-border);
}

.build-card.selected {
  border-color: var(--color-accent-primary);
  background: var(--color-bg-primary);
}

.build-card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: var(--spacing-xs);
}

.build-round {
  font-weight: var(--font-weight-bold);
  font-size: var(--font-size-md);
}

.build-card-info {
  display: flex;
  justify-content: space-between;
  font-size: var(--font-size-sm);
  color: var(--color-text-muted);
  margin-bottom: var(--spacing-sm);
}

.pipeline-mini {
  display: flex;
  gap: 4px;
  flex-wrap: wrap;
}

.stage-dot {
  width: 20px;
  height: 20px;
  border-radius: 4px;
  border: 1px solid;
  display: flex;
  align-items: center;
  justify-content: center;
}

.build-card-actions {
  margin-top: var(--spacing-sm);
  padding-top: var(--spacing-sm);
  border-top: 1px solid var(--color-border);
}

/* Build Detail */
.build-detail {
  background: var(--color-bg-secondary);
  border-radius: var(--radius-sm);
  padding: var(--spacing-lg);
}

.detail-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: var(--spacing-lg);
  padding-bottom: var(--spacing-md);
  border-bottom: 1px solid var(--color-border);
}

.detail-header h4 {
  margin: 0;
  font-size: var(--font-size-lg);
}

.detail-info {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: var(--spacing-md);
  margin-bottom: var(--spacing-lg);
}

.detail-item {
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.detail-item .label {
  font-size: var(--font-size-xs);
  color: var(--color-text-muted);
  text-transform: uppercase;
}

.detail-item .value {
  font-size: var(--font-size-md);
  color: var(--color-text-primary);
  display: flex;
  align-items: center;
  gap: 4px;
}

.pipeline-detail,
.quality-detail {
  margin-top: var(--spacing-lg);
}

.pipeline-detail h5,
.quality-detail h5 {
  margin: 0 0 var(--spacing-md);
  font-size: var(--font-size-md);
  color: var(--color-text-secondary);
}

.pipeline-flow {
  display: flex;
  align-items: center;
  overflow-x: auto;
  padding: var(--spacing-sm) 0;
}

.pipeline-stage {
  display: flex;
  align-items: center;
}

.stage-box {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 4px;
  padding: var(--spacing-sm) var(--spacing-md);
  border-radius: var(--radius-sm);
  border: 2px solid;
  min-width: 80px;
}

.stage-icon {
  font-size: var(--font-size-lg);
}

.stage-box .stage-name {
  font-size: var(--font-size-xs);
  font-weight: var(--font-weight-medium);
  color: var(--color-text-primary);
}

.stage-duration {
  font-size: var(--font-size-xs);
  color: var(--color-text-muted);
}

.stage-connector {
  width: 16px;
  height: 2px;
  background: var(--color-border);
}

.metrics-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(100px, 1fr));
  gap: var(--spacing-sm);
}

.metric-card {
  background: var(--color-bg-tertiary);
  padding: var(--spacing-sm);
  border-radius: var(--radius-xs);
  text-align: center;
}

.metric-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 4px;
}

.metric-name {
  font-size: var(--font-size-xs);
  color: var(--color-text-muted);
}

.metric-score {
  font-size: var(--font-size-lg);
  font-weight: var(--font-weight-bold);
  color: var(--color-text-primary);
}

.detail-placeholder {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  height: 100%;
  color: var(--color-text-muted);
  gap: var(--spacing-md);
}

.placeholder-icon {
  font-size: 48px;
  opacity: 0.3;
}

/* Basic Info Section */
.basic-info-section {
  display: flex;
  flex-direction: column;
  gap: var(--spacing-xl);
}

.section-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.section-header h3 {
  margin: 0;
  font-size: var(--font-size-xl);
}

.info-card {
  background: var(--color-bg-tertiary);
  border-radius: var(--radius-md);
  padding: var(--spacing-lg);
  height: 100%;
}

.info-card h4 {
  margin: 0 0 var(--spacing-md);
  font-size: var(--font-size-md);
  color: var(--color-text-secondary);
  display: flex;
  align-items: center;
  gap: var(--spacing-sm);
}

/* Members Section - Simplified */
.members-summary {
  display: flex;
  flex-direction: column;
  gap: var(--spacing-lg);
}

.key-members {
  display: flex;
  gap: var(--spacing-lg);
}

.member-item {
  display: flex;
  align-items: center;
  gap: var(--spacing-sm);
}

.member-item.highlight {
  padding: var(--spacing-sm) var(--spacing-md);
  background: var(--color-bg-primary);
  border-radius: var(--radius-sm);
  border: 1px solid var(--color-border);
}

.member-info {
  display: flex;
  flex-direction: column;
}

.member-info .name {
  font-weight: var(--font-weight-medium);
  color: var(--color-text-primary);
}

.role-badge {
  font-size: var(--font-size-xs);
  font-weight: var(--font-weight-semibold);
  padding: 2px 6px;
  border-radius: var(--radius-xs);
}

.role-badge.tl {
  background: rgba(0, 212, 170, 0.15);
  color: var(--color-accent-primary);
}

.role-badge.pl {
  background: rgba(255, 183, 77, 0.15);
  color: var(--color-accent-secondary);
}

.other-members {
  display: flex;
  align-items: center;
  gap: var(--spacing-sm);
  padding-top: var(--spacing-sm);
  border-top: 1px solid var(--color-border);
}

.members-count {
  font-size: var(--font-size-sm);
  color: var(--color-text-muted);
}

.plm-link {
  color: var(--color-accent-primary);
}

.history-section {
  margin-top: var(--spacing-xl);
}

.history-section h3 {
  margin: 0 0 var(--spacing-md);
  font-size: var(--font-size-lg);
  display: flex;
  align-items: center;
  gap: var(--spacing-sm);
}

/* History Table - Summary & Expanded Row Styles */
.history-summary {
  display: flex;
  align-items: center;
  gap: var(--spacing-sm);
  font-size: var(--font-size-sm);
}

.history-summary .old-value {
  color: var(--color-text-muted);
  text-decoration: line-through;
  max-width: 150px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.history-summary .arrow {
  color: var(--color-accent-primary);
  font-weight: var(--font-weight-bold);
}

.history-summary .new-value {
  color: var(--color-accent-success);
  max-width: 150px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.history-expanded {
  padding: var(--spacing-md);
  background: rgba(var(--color-accent-primary-rgb), 0.05);
  border-radius: var(--radius-md);
  border-left: 3px solid var(--color-accent-primary);
}

.expanded-row {
  display: flex;
  gap: var(--spacing-md);
  padding: var(--spacing-xs) 0;
}

.expanded-row:not(:last-child) {
  border-bottom: 1px solid rgba(255, 255, 255, 0.05);
}

.expanded-label {
  flex-shrink: 0;
  width: 80px;
  color: var(--color-text-muted);
  font-size: var(--font-size-sm);
}

.expanded-value {
  flex: 1;
  word-break: break-word;
}

.expanded-value.old {
  color: var(--color-text-muted);
  text-decoration: line-through;
}

.expanded-value.new {
  color: var(--color-accent-success);
  font-weight: var(--font-weight-medium);
}

.text-muted {
  color: var(--color-text-muted);
}

/* Pipeline Stages Table (새로운 설정 UI) */
.pipeline-stages-table {
  display: flex;
  flex-direction: column;
  gap: var(--spacing-xs);
}

.stage-row {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: var(--spacing-sm) var(--spacing-md);
  background: var(--color-bg-primary);
  border-radius: var(--radius-sm);
  transition: background var(--transition-fast);
}

.stage-row:hover {
  background: rgba(255, 255, 255, 0.05);
}

.stage-row.disabled {
  opacity: 0.5;
}

.stage-main {
  display: flex;
  align-items: center;
  gap: var(--spacing-sm);
}

.stage-main .stage-name {
  font-weight: var(--font-weight-medium);
}

.stage-settings {
  display: flex;
  align-items: center;
}

.settings-btn {
  color: var(--color-text-muted);
  font-size: var(--font-size-sm);
}

.settings-btn:hover {
  color: var(--color-text-primary);
}

.settings-summary {
  margin-left: var(--spacing-xs);
  max-width: 300px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.stage-settings-content {
  max-width: 300px;
}

.settings-list {
  display: flex;
  flex-direction: column;
  gap: var(--spacing-xs);
}

.setting-item {
  display: flex;
  justify-content: space-between;
  gap: var(--spacing-md);
}

.setting-label {
  color: var(--color-text-muted);
  font-size: var(--font-size-sm);
}

.setting-value {
  color: var(--color-text-primary);
  font-weight: var(--font-weight-medium);
  font-size: var(--font-size-sm);
}

.no-settings {
  color: var(--color-text-muted);
  font-size: var(--font-size-sm);
}

/* Build Results Table */
.build-results-table {
  overflow-x: auto;
}

.build-results-table :deep(.ant-table) {
  background: transparent;
}

.build-results-table :deep(.ant-table-thead > tr > th) {
  background: var(--color-bg-secondary);
  border-bottom: 1px solid var(--color-border);
  color: var(--color-text-secondary);
  font-weight: var(--font-weight-medium);
}

.build-results-table :deep(.ant-table-tbody > tr > td) {
  background: var(--color-bg-secondary);
  border-bottom: 1px solid var(--color-border);
}

.build-results-table :deep(.ant-table-tbody > tr:hover > td) {
  background: rgba(255, 255, 255, 0.05);
}

.build-results-table :deep(.ant-table-tbody > tr.selected-row > td) {
  background: rgba(0, 212, 170, 0.1);
}

.build-round {
  font-weight: var(--font-weight-bold);
  color: var(--color-text-primary);
}

.fw-name {
  font-weight: var(--font-weight-medium);
  color: var(--color-text-primary);
}

.branch-name {
  color: var(--color-text-secondary);
  font-size: var(--font-size-sm);
}

.time-info {
  display: flex;
  flex-direction: column;
  gap: 2px;
}

.start-time {
  font-size: var(--font-size-sm);
  color: var(--color-text-secondary);
}

.duration {
  font-size: var(--font-size-xs);
  color: var(--color-text-muted);
}

/* Pipeline Grid (테이블 내 파이프라인 - 이름 표시) */
.pipeline-grid {
  display: flex;
  flex-wrap: wrap;
  gap: 6px;
}

.stage-chip-labeled {
  display: flex;
  flex-direction: column;
  align-items: center;
  padding: 4px 8px;
  border-radius: 6px;
  cursor: pointer;
  transition: all var(--transition-fast);
  min-width: 52px;
}

.stage-chip-labeled:hover {
  transform: scale(1.05);
}

.stage-chip-icon {
  font-size: 14px;
  line-height: 1;
}

.stage-chip-name {
  font-size: 9px;
  font-weight: var(--font-weight-medium);
  margin-top: 2px;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
  max-width: 60px;
}

.stage-chip-labeled.success {
  background: rgba(76, 175, 80, 0.15);
  color: var(--color-accent-success);
}

.stage-chip-labeled.failed {
  background: rgba(244, 67, 54, 0.15);
  color: var(--color-accent-danger);
}

.stage-chip-labeled.running {
  background: rgba(255, 183, 77, 0.15);
  color: var(--color-accent-warning);
}

.stage-chip-labeled.pending,
.stage-chip-labeled.skipped {
  background: rgba(148, 163, 184, 0.15);
  color: var(--color-text-muted);
}

/* Triggered By + Time Info */
.triggered-info {
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.triggered-by {
  display: flex;
  align-items: center;
  gap: 6px;
  font-weight: var(--font-weight-medium);
  color: var(--color-text-primary);
  font-size: var(--font-size-sm);
}

.triggered-by :deep(.anticon) {
  color: var(--color-accent-primary);
}

/* Stage Popover */
.stage-popover-content {
  max-width: 350px;
}

.stage-popover-header {
  display: flex;
  align-items: center;
  gap: var(--spacing-sm);
  margin-bottom: var(--spacing-sm);
}

.stage-popover-duration {
  font-size: var(--font-size-sm);
  color: var(--color-text-muted);
  margin-bottom: var(--spacing-xs);
}

.stage-popover-summary {
  font-size: var(--font-size-sm);
  color: var(--color-text-secondary);
  margin-bottom: var(--spacing-sm);
}

.stage-popover-logs {
  background: var(--color-bg-tertiary);
  border-radius: var(--radius-xs);
  padding: var(--spacing-sm);
  margin-bottom: var(--spacing-sm);
  max-height: 150px;
  overflow-y: auto;
}

.stage-popover-logs pre {
  margin: 0;
  font-family: 'Monaco', 'Consolas', monospace;
  font-size: 11px;
  color: var(--color-text-secondary);
  white-space: pre-wrap;
  word-break: break-all;
}

/* Build Detail Modal */
.build-detail-modal {
  display: flex;
  flex-direction: column;
  gap: var(--spacing-lg);
}

.modal-section h4 {
  margin: 0 0 var(--spacing-md);
  font-size: var(--font-size-md);
  color: var(--color-text-secondary);
}

.pipeline-flow-modal {
  display: flex;
  flex-wrap: wrap;
  align-items: center;
  gap: var(--spacing-xs);
  padding: var(--spacing-md) 0;
}

.pipeline-stage-modal {
  display: flex;
  align-items: center;
}

.stage-box-modal {
  display: flex;
  align-items: center;
  gap: var(--spacing-sm);
  padding: var(--spacing-sm) var(--spacing-md);
  border-radius: var(--radius-sm);
  cursor: pointer;
  transition: transform var(--transition-fast);
}

.stage-box-modal:hover {
  transform: scale(1.02);
}

.stage-box-modal.success {
  background: rgba(76, 175, 80, 0.15);
  border: 1px solid var(--color-accent-success);
}

.stage-box-modal.failed {
  background: rgba(244, 67, 54, 0.15);
  border: 1px solid var(--color-accent-danger);
}

.stage-box-modal.running {
  background: rgba(255, 183, 77, 0.15);
  border: 1px solid var(--color-accent-warning);
}

.stage-box-modal.pending,
.stage-box-modal.skipped {
  background: rgba(148, 163, 184, 0.1);
  border: 1px solid var(--color-border);
}

.stage-icon-modal {
  font-size: var(--font-size-lg);
}

.stage-box-modal.success .stage-icon-modal { color: var(--color-accent-success); }
.stage-box-modal.failed .stage-icon-modal { color: var(--color-accent-danger); }
.stage-box-modal.running .stage-icon-modal { color: var(--color-accent-warning); }
.stage-box-modal.pending .stage-icon-modal,
.stage-box-modal.skipped .stage-icon-modal { color: var(--color-text-muted); }

.stage-info-modal {
  display: flex;
  flex-direction: column;
  gap: 2px;
}

.stage-info-modal .stage-name {
  font-weight: var(--font-weight-medium);
  font-size: var(--font-size-sm);
  color: var(--color-text-primary);
}

.stage-info-modal .stage-duration {
  font-size: var(--font-size-xs);
  color: var(--color-text-muted);
}

.stage-info-modal .stage-summary {
  font-size: var(--font-size-xs);
  color: var(--color-text-secondary);
}

.stage-connector-modal {
  width: 20px;
  height: 2px;
  background: var(--color-border);
}

.stage-connector-modal.success {
  background: var(--color-accent-success);
}

.metrics-grid-modal {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(120px, 1fr));
  gap: var(--spacing-sm);
}

.metric-card-modal {
  background: var(--color-bg-tertiary);
  padding: var(--spacing-sm);
  border-radius: var(--radius-sm);
  text-align: center;
}

.metric-header-modal {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: var(--spacing-xs);
}

.metric-header-modal .metric-name {
  font-size: var(--font-size-xs);
  color: var(--color-text-muted);
  text-transform: capitalize;
}

.metric-score-modal {
  font-size: var(--font-size-xl);
  font-weight: var(--font-weight-bold);
  color: var(--color-text-primary);
}

.metric-issues {
  font-size: var(--font-size-xs);
  color: var(--color-text-muted);
}

.artifacts-list {
  display: flex;
  flex-wrap: wrap;
  gap: var(--spacing-sm);
}

/* Build Trigger Confirm Modal */
.build-trigger-confirm p {
  margin-bottom: var(--spacing-md);
  color: var(--color-text-secondary);
}

/* Responsive */
@media (max-width: 1200px) {
  .build-results {
    grid-template-columns: 1fr;
  }

  .build-list {
    max-height: 300px;
  }

  .config-grid {
    grid-template-columns: 1fr;
  }

  .config-card.wide {
    grid-column: span 1;
  }

  .pipeline-flow-modal {
    flex-direction: column;
    align-items: flex-start;
  }

  .stage-connector-modal {
    width: 2px;
    height: 20px;
    margin-left: 20px;
  }
}
</style>
