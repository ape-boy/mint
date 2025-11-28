<script setup lang="ts">
import { ref, computed, watch, onMounted } from 'vue'
import { CloseOutlined, BellOutlined } from '@ant-design/icons-vue'
import type { Notice, ReleaseNote } from '../../types'
import { popupApi } from '../../api'
import { useFormat } from '../../composables'

const props = defineProps<{
  visible: boolean
}>()

const emit = defineEmits<{
  (e: 'update:visible', value: boolean): void
}>()

const { formatDate } = useFormat()

interface PopupItem {
  id: string
  type: 'notice' | 'release-note'
  title: string
  content: string
  version?: string
  releaseType?: string
  priority?: string
  category?: string
}

const popups = ref<PopupItem[]>([])
const currentIndex = ref(0)
const loading = ref(false)
const dontShowToday = ref(false)

const currentPopup = computed(() => popups.value[currentIndex.value] || null)
const hasMultiple = computed(() => popups.value.length > 1)
const progressText = computed(() => `${currentIndex.value + 1} / ${popups.value.length}`)

const releaseTypeColors: Record<string, string> = {
  major: '#ff4d4f',
  minor: '#1890ff',
  patch: '#52c41a',
  hotfix: '#fa8c16',
}

onMounted(async () => {
  await loadPopups()
})

async function loadPopups() {
  loading.value = true
  try {
    const activePopups = await popupApi.getActivePopups()
    popups.value = activePopups.map(popup => ({
      id: popup.id,
      type: popup.type,
      title: popup.title,
      content: popup.content,
      version: popup.version,
      releaseType: popup.releaseType,
      priority: popup.priority,
      category: popup.category,
    }))

    if (popups.value.length > 0) {
      emit('update:visible', true)
    }
  } catch (error) {
    console.error('Failed to load popups:', error)
  } finally {
    loading.value = false
  }
}

function handleClose() {
  if (dontShowToday.value) {
    const today = new Date().toDateString()
    localStorage.setItem('popup_dismissed_date', today)
  }
  emit('update:visible', false)
}

function handleNext() {
  if (currentIndex.value < popups.value.length - 1) {
    currentIndex.value++
  } else {
    handleClose()
  }
}

function handlePrev() {
  if (currentIndex.value > 0) {
    currentIndex.value--
  }
}

watch(() => props.visible, (visible) => {
  if (visible) {
    currentIndex.value = 0
    dontShowToday.value = false
  }
})
</script>

<template>
  <a-modal
    :open="visible"
    :footer="null"
    :closable="false"
    width="600px"
    class="popup-modal"
    centered
  >
    <div v-if="currentPopup" class="popup-content">
      <div class="popup-header">
        <div class="popup-type">
          <BellOutlined />
          <span v-if="currentPopup.type === 'notice'">Notice</span>
          <span v-else>Release Note</span>
        </div>
        <a-button type="text" class="close-btn" @click="handleClose">
          <CloseOutlined />
        </a-button>
      </div>

      <div class="popup-meta" v-if="currentPopup.type === 'release-note'">
        <a-tag :color="releaseTypeColors[currentPopup.releaseType || 'minor']">
          {{ currentPopup.releaseType?.toUpperCase() }}
        </a-tag>
        <span class="version">v{{ currentPopup.version }}</span>
      </div>

      <div class="popup-meta" v-else-if="currentPopup.priority && currentPopup.priority !== 'normal'">
        <a-tag :color="currentPopup.priority === 'urgent' ? 'error' : 'warning'">
          {{ currentPopup.priority?.toUpperCase() }}
        </a-tag>
      </div>

      <h2 class="popup-title">{{ currentPopup.title }}</h2>

      <div class="popup-body" v-html="currentPopup.content.replace(/\n/g, '<br>')" />

      <div class="popup-footer">
        <a-checkbox v-model:checked="dontShowToday">
          Don't show today
        </a-checkbox>

        <div class="popup-actions">
          <span v-if="hasMultiple" class="progress">{{ progressText }}</span>
          <a-button v-if="hasMultiple && currentIndex > 0" @click="handlePrev">
            Previous
          </a-button>
          <a-button type="primary" @click="handleNext">
            {{ currentIndex < popups.length - 1 ? 'Next' : 'Close' }}
          </a-button>
        </div>
      </div>
    </div>

    <a-spin v-else :spinning="loading">
      <div class="popup-empty">
        <a-empty description="No popups to show" />
      </div>
    </a-spin>
  </a-modal>
</template>

<style scoped>
.popup-modal :deep(.ant-modal-content) {
  background: var(--color-bg-secondary);
  border-radius: var(--radius-lg);
  overflow: hidden;
}

.popup-content {
  padding: var(--spacing-md);
}

.popup-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: var(--spacing-md);
}

.popup-type {
  display: flex;
  align-items: center;
  gap: var(--spacing-sm);
  color: var(--color-accent-primary);
  font-weight: var(--font-weight-medium);
}

.close-btn {
  color: var(--color-text-muted);
}

.close-btn:hover {
  color: var(--color-text-primary);
}

.popup-meta {
  display: flex;
  align-items: center;
  gap: var(--spacing-sm);
  margin-bottom: var(--spacing-md);
}

.version {
  font-weight: var(--font-weight-bold);
  color: var(--color-accent-primary);
}

.popup-title {
  font-size: var(--font-size-xl);
  font-weight: var(--font-weight-bold);
  color: var(--color-text-primary);
  margin: 0 0 var(--spacing-lg) 0;
}

.popup-body {
  color: var(--color-text-secondary);
  line-height: 1.8;
  max-height: 300px;
  overflow-y: auto;
  padding: var(--spacing-md);
  background: var(--color-bg-tertiary);
  border-radius: var(--radius-md);
  margin-bottom: var(--spacing-lg);
}

.popup-footer {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding-top: var(--spacing-md);
  border-top: 1px solid var(--color-border-light);
}

.popup-actions {
  display: flex;
  align-items: center;
  gap: var(--spacing-sm);
}

.progress {
  color: var(--color-text-muted);
  font-size: var(--font-size-sm);
  margin-right: var(--spacing-sm);
}

.popup-empty {
  padding: var(--spacing-xl);
}
</style>
