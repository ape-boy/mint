<script setup lang="ts">
import { ref, computed, onMounted, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { message } from 'ant-design-vue'
import { PageHeader } from '@/components'
import {
  NoticeList,
  NoticeDetail,
  NoticeForm,
  VOCList,
  VOCDetail,
  VOCForm,
  ReleaseNoteList,
  ReleaseNoteDetail,
  ReleaseNoteForm,
  BoardTabs,
} from '@/components/board'
import { useNoticeStore, useVOCStore, useReleaseNoteStore } from '@/stores/board'
import type { Notice, VOC, ReleaseNote } from '@/types'

const route = useRoute()
const router = useRouter()

const noticeStore = useNoticeStore()
const vocStore = useVOCStore()
const releaseNoteStore = useReleaseNoteStore()

// Tab state
const activeTab = ref<'notice' | 'voc' | 'release-note'>('notice')

// View modes
const viewMode = ref<'list' | 'detail'>('list')
const selectedId = ref<string | null>(null)

// Form state
const formVisible = ref(false)
const editingItem = ref<Notice | VOC | ReleaseNote | null>(null)
const formLoading = ref(false)

// Detect tab from route path
function detectTabFromRoute(): 'notice' | 'voc' | 'release-note' {
  const path = route.path
  if (path.includes('/notice/')) return 'notice'
  if (path.includes('/voc/')) return 'voc'
  if (path.includes('/release-note/')) return 'release-note'

  // Check query param
  const tab = route.query.tab as string
  if (tab && ['notice', 'voc', 'release-note'].includes(tab)) {
    return tab as 'notice' | 'voc' | 'release-note'
  }

  return 'notice'
}

// Initialize from route
onMounted(async () => {
  // Parse route parameters
  const id = route.params.id as string
  activeTab.value = detectTabFromRoute()

  if (id) {
    selectedId.value = id
    viewMode.value = 'detail'
  }

  // Load data
  await Promise.all([
    noticeStore.fetchNotices(),
    vocStore.fetchVOCs(),
    releaseNoteStore.fetchReleaseNotes(),
  ])
})

// Watch route changes (path and query)
watch([() => route.path, () => route.query.tab], () => {
  const id = route.params.id as string
  activeTab.value = detectTabFromRoute()

  if (id) {
    selectedId.value = id
    viewMode.value = 'detail'
  } else {
    viewMode.value = 'list'
    selectedId.value = null
  }
})

// Computed data
const currentNotice = computed(() => {
  if (activeTab.value === 'notice' && selectedId.value) {
    return noticeStore.notices.find(n => n.id === selectedId.value) || null
  }
  return null
})

const currentVOC = computed(() => {
  if (activeTab.value === 'voc' && selectedId.value) {
    return vocStore.vocs.find(v => v.id === selectedId.value) || null
  }
  return null
})

const currentReleaseNote = computed(() => {
  if (activeTab.value === 'release-note' && selectedId.value) {
    return releaseNoteStore.releaseNotes.find(r => r.id === selectedId.value) || null
  }
  return null
})

// Tab change handler
function handleTabChange(tab: 'notice' | 'voc' | 'release-note') {
  activeTab.value = tab
  viewMode.value = 'list'
  selectedId.value = null
  router.push({ path: '/board', query: { tab } })
}

// Navigation handlers
function handleViewDetail(id: string) {
  selectedId.value = id
  viewMode.value = 'detail'
  router.push(`/board/${activeTab.value}/${id}`)
}

function handleBack() {
  viewMode.value = 'list'
  selectedId.value = null
  router.push({ path: '/board', query: { tab: activeTab.value } })
}

// Create handlers
function handleCreate() {
  editingItem.value = null
  formVisible.value = true
}

// Edit handlers
function handleEdit(item: Notice | VOC | ReleaseNote) {
  editingItem.value = item
  formVisible.value = true
}

// Delete handlers
async function handleDeleteNotice(id: string) {
  try {
    await noticeStore.deleteNotice(id)
    message.success('Notice deleted successfully')
    handleBack()
  } catch (error) {
    message.error('Failed to delete notice')
  }
}

async function handleDeleteVOC(id: string) {
  try {
    await vocStore.deleteVOC(id)
    message.success('VOC deleted successfully')
    handleBack()
  } catch (error) {
    message.error('Failed to delete VOC')
  }
}

async function handleDeleteReleaseNote(id: string) {
  try {
    await releaseNoteStore.deleteReleaseNote(id)
    message.success('Release note deleted successfully')
    handleBack()
  } catch (error) {
    message.error('Failed to delete release note')
  }
}

// Submit handlers
async function handleNoticeSubmit(data: Partial<Notice>) {
  formLoading.value = true
  try {
    if (editingItem.value?.id) {
      await noticeStore.updateNotice(editingItem.value.id, data)
      message.success('Notice updated successfully')
    } else {
      await noticeStore.createNotice(data as Omit<Notice, 'id' | 'viewCount' | 'createdAt' | 'updatedAt'>)
      message.success('Notice created successfully')
    }
    formVisible.value = false
    editingItem.value = null
  } catch (error) {
    message.error('Failed to save notice')
  } finally {
    formLoading.value = false
  }
}

async function handleVOCSubmit(data: Partial<VOC>) {
  formLoading.value = true
  try {
    if (editingItem.value?.id) {
      await vocStore.updateVOC(editingItem.value.id, data)
      message.success('VOC updated successfully')
    } else {
      await vocStore.createVOC(data as Omit<VOC, 'id' | 'viewCount' | 'createdAt' | 'updatedAt' | 'comments'>)
      message.success('VOC created successfully')
    }
    formVisible.value = false
    editingItem.value = null
  } catch (error) {
    message.error('Failed to save VOC')
  } finally {
    formLoading.value = false
  }
}

async function handleReleaseNoteSubmit(data: Partial<ReleaseNote>) {
  formLoading.value = true
  try {
    if (editingItem.value?.id) {
      await releaseNoteStore.updateReleaseNote(editingItem.value.id, data)
      message.success('Release note updated successfully')
    } else {
      await releaseNoteStore.createReleaseNote(data as Omit<ReleaseNote, 'id' | 'viewCount' | 'createdAt' | 'updatedAt'>)
      message.success('Release note created successfully')
    }
    formVisible.value = false
    editingItem.value = null
  } catch (error) {
    message.error('Failed to save release note')
  } finally {
    formLoading.value = false
  }
}

function handleFormCancel() {
  formVisible.value = false
  editingItem.value = null
}

// Page title
const pageTitle = computed(() => {
  switch (activeTab.value) {
    case 'notice': return 'Notices'
    case 'voc': return 'VOC'
    case 'release-note': return 'Release Notes'
    default: return 'Board'
  }
})

const pageDescription = computed(() => {
  switch (activeTab.value) {
    case 'notice': return 'Company announcements and important notices'
    case 'voc': return 'Voice of Customer - requests and feedback management'
    case 'release-note': return 'Software release notes and changelogs'
    default: return 'Board management'
  }
})
</script>

<template>
  <div class="board-view">
    <PageHeader :title="pageTitle" :description="pageDescription" />

    <BoardTabs
      :active-tab="activeTab"
      :notice-count="noticeStore.notices.length"
      :voc-count="vocStore.vocs.length"
      :release-note-count="releaseNoteStore.releaseNotes.length"
      @update:active-tab="handleTabChange"
    />

    <div class="board-content">
      <!-- Notice Tab -->
      <template v-if="activeTab === 'notice'">
        <NoticeList
          v-if="viewMode === 'list'"
          :notices="noticeStore.notices"
          :loading="noticeStore.loading"
          @create="handleCreate"
          @view="handleViewDetail"
        />
        <NoticeDetail
          v-else
          :notice="currentNotice"
          :loading="noticeStore.loading"
          @back="handleBack"
          @edit="handleEdit"
          @delete="handleDeleteNotice"
        />
        <NoticeForm
          :visible="formVisible"
          :notice="editingItem as Notice | null"
          :loading="formLoading"
          @update:visible="formVisible = $event"
          @submit="handleNoticeSubmit"
          @cancel="handleFormCancel"
        />
      </template>

      <!-- VOC Tab -->
      <template v-if="activeTab === 'voc'">
        <VOCList
          v-if="viewMode === 'list'"
          :vocs="vocStore.vocs"
          :loading="vocStore.loading"
          @create="handleCreate"
          @view="handleViewDetail"
        />
        <VOCDetail
          v-else
          :voc="currentVOC"
          :loading="vocStore.loading"
          @back="handleBack"
          @edit="handleEdit"
          @delete="handleDeleteVOC"
        />
        <VOCForm
          :visible="formVisible"
          :voc="editingItem as VOC | null"
          :loading="formLoading"
          @update:visible="formVisible = $event"
          @submit="handleVOCSubmit"
          @cancel="handleFormCancel"
        />
      </template>

      <!-- Release Note Tab -->
      <template v-if="activeTab === 'release-note'">
        <ReleaseNoteList
          v-if="viewMode === 'list'"
          :release-notes="releaseNoteStore.releaseNotes"
          :loading="releaseNoteStore.loading"
          @create="handleCreate"
          @view="handleViewDetail"
        />
        <ReleaseNoteDetail
          v-else
          :release-note="currentReleaseNote"
          :loading="releaseNoteStore.loading"
          @back="handleBack"
          @edit="handleEdit"
          @delete="handleDeleteReleaseNote"
        />
        <ReleaseNoteForm
          :visible="formVisible"
          :release-note="editingItem as ReleaseNote | null"
          :loading="formLoading"
          @update:visible="formVisible = $event"
          @submit="handleReleaseNoteSubmit"
          @cancel="handleFormCancel"
        />
      </template>
    </div>
  </div>
</template>

<style scoped>
.board-view {
  display: flex;
  flex-direction: column;
  gap: var(--spacing-lg);
}

.board-content {
  min-height: 500px;
}
</style>
