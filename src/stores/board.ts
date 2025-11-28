import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import { noticeApi, releaseNoteApi, vocApi, popupApi } from '../api'
import type {
  Notice,
  NoticeQueryParams,
  ReleaseNote,
  ReleaseNoteQueryParams,
  VOC,
  VOCQueryParams,
  PopupConfig,
  VOCComment,
  TeamMember,
} from '../types'

// ============================================
// Notice Store
// ============================================
export const useNoticeStore = defineStore('notice', () => {
  // State
  const notices = ref<Notice[]>([])
  const currentNotice = ref<Notice | null>(null)
  const loading = ref(false)
  const error = ref<string | null>(null)

  // Getters
  const pinnedNotices = computed(() =>
    notices.value.filter(n => n.isPinned).sort((a, b) =>
      new Date(b.createdAt).getTime() - new Date(a.createdAt).getTime()
    )
  )

  const popupNotices = computed(() =>
    notices.value.filter(n => n.isPopup && isPopupActive(n))
  )

  const noticesByCategory = computed(() => {
    const grouped: Record<string, Notice[]> = {}
    notices.value.forEach(notice => {
      const cat = notice.category
      if (!grouped[cat]) {
        grouped[cat] = []
      }
      grouped[cat]!.push(notice)
    })
    return grouped
  })

  // Helper
  function isPopupActive(notice: Notice): boolean {
    if (!notice.isPopup) return false
    const now = new Date()
    const start = notice.popupStartDate ? new Date(notice.popupStartDate) : null
    const end = notice.popupEndDate ? new Date(notice.popupEndDate) : null
    if (start && now < start) return false
    if (end && now > end) return false
    return true
  }

  // Actions
  async function fetchNotices(params?: NoticeQueryParams) {
    loading.value = true
    error.value = null
    try {
      notices.value = await noticeApi.getList(params)
    } catch (e) {
      error.value = e instanceof Error ? e.message : 'Failed to fetch notices'
      throw e
    } finally {
      loading.value = false
    }
  }

  async function fetchNoticeById(id: string) {
    loading.value = true
    error.value = null
    try {
      currentNotice.value = await noticeApi.getById(id)
      // Increment view count
      if (currentNotice.value) {
        await noticeApi.incrementViewCount(id, currentNotice.value.viewCount)
        currentNotice.value.viewCount += 1
      }
    } catch (e) {
      error.value = e instanceof Error ? e.message : 'Failed to fetch notice'
      throw e
    } finally {
      loading.value = false
    }
  }

  async function createNotice(notice: Omit<Notice, 'id' | 'viewCount' | 'createdAt' | 'updatedAt'>) {
    loading.value = true
    error.value = null
    try {
      const created = await noticeApi.create(notice)
      notices.value.unshift(created)
      return created
    } catch (e) {
      error.value = e instanceof Error ? e.message : 'Failed to create notice'
      throw e
    } finally {
      loading.value = false
    }
  }

  async function updateNotice(id: string, notice: Partial<Notice>) {
    loading.value = true
    error.value = null
    try {
      const updated = await noticeApi.update(id, notice)
      const index = notices.value.findIndex(n => n.id === id)
      if (index !== -1) {
        notices.value[index] = updated
      }
      if (currentNotice.value?.id === id) {
        currentNotice.value = updated
      }
      return updated
    } catch (e) {
      error.value = e instanceof Error ? e.message : 'Failed to update notice'
      throw e
    } finally {
      loading.value = false
    }
  }

  async function deleteNotice(id: string) {
    loading.value = true
    error.value = null
    try {
      await noticeApi.delete(id)
      notices.value = notices.value.filter(n => n.id !== id)
      if (currentNotice.value?.id === id) {
        currentNotice.value = null
      }
    } catch (e) {
      error.value = e instanceof Error ? e.message : 'Failed to delete notice'
      throw e
    } finally {
      loading.value = false
    }
  }

  function clearCurrentNotice() {
    currentNotice.value = null
  }

  return {
    // State
    notices,
    currentNotice,
    loading,
    error,
    // Getters
    pinnedNotices,
    popupNotices,
    noticesByCategory,
    // Actions
    fetchNotices,
    fetchNoticeById,
    createNotice,
    updateNotice,
    deleteNotice,
    clearCurrentNotice,
  }
})

// ============================================
// Release Note Store
// ============================================
export const useReleaseNoteStore = defineStore('releaseNote', () => {
  // State
  const releaseNotes = ref<ReleaseNote[]>([])
  const currentReleaseNote = ref<ReleaseNote | null>(null)
  const loading = ref(false)
  const error = ref<string | null>(null)

  // Getters
  const popupReleaseNotes = computed(() =>
    releaseNotes.value.filter(rn => rn.isPopup && isPopupActive(rn))
  )

  const releaseNotesByProject = computed(() => {
    const grouped: Record<string, ReleaseNote[]> = {}
    releaseNotes.value.forEach(rn => {
      const key = rn.projectId || 'system'
      if (!grouped[key]) {
        grouped[key] = []
      }
      grouped[key].push(rn)
    })
    return grouped
  })

  const releaseNotesByType = computed(() => {
    const grouped: Record<string, ReleaseNote[]> = {}
    releaseNotes.value.forEach(rn => {
      const type = rn.releaseType
      if (!grouped[type]) {
        grouped[type] = []
      }
      grouped[type]!.push(rn)
    })
    return grouped
  })

  // Helper
  function isPopupActive(rn: ReleaseNote): boolean {
    if (!rn.isPopup) return false
    const now = new Date()
    const start = rn.popupStartDate ? new Date(rn.popupStartDate) : null
    const end = rn.popupEndDate ? new Date(rn.popupEndDate) : null
    if (start && now < start) return false
    if (end && now > end) return false
    return true
  }

  // Actions
  async function fetchReleaseNotes(params?: ReleaseNoteQueryParams) {
    loading.value = true
    error.value = null
    try {
      releaseNotes.value = await releaseNoteApi.getList(params)
    } catch (e) {
      error.value = e instanceof Error ? e.message : 'Failed to fetch release notes'
      throw e
    } finally {
      loading.value = false
    }
  }

  async function fetchReleaseNoteById(id: string) {
    loading.value = true
    error.value = null
    try {
      currentReleaseNote.value = await releaseNoteApi.getById(id)
      // Increment view count
      if (currentReleaseNote.value) {
        await releaseNoteApi.incrementViewCount(id, currentReleaseNote.value.viewCount)
        currentReleaseNote.value.viewCount += 1
      }
    } catch (e) {
      error.value = e instanceof Error ? e.message : 'Failed to fetch release note'
      throw e
    } finally {
      loading.value = false
    }
  }

  async function createReleaseNote(releaseNote: Partial<ReleaseNote>) {
    loading.value = true
    error.value = null
    try {
      const created = await releaseNoteApi.create(releaseNote as Omit<ReleaseNote, 'id' | 'viewCount' | 'createdAt' | 'updatedAt'>)
      releaseNotes.value.unshift(created)
      return created
    } catch (e) {
      error.value = e instanceof Error ? e.message : 'Failed to create release note'
      throw e
    } finally {
      loading.value = false
    }
  }

  async function updateReleaseNote(id: string, releaseNote: Partial<ReleaseNote>) {
    loading.value = true
    error.value = null
    try {
      const updated = await releaseNoteApi.update(id, releaseNote)
      const index = releaseNotes.value.findIndex(rn => rn.id === id)
      if (index !== -1) {
        releaseNotes.value[index] = updated
      }
      if (currentReleaseNote.value?.id === id) {
        currentReleaseNote.value = updated
      }
      return updated
    } catch (e) {
      error.value = e instanceof Error ? e.message : 'Failed to update release note'
      throw e
    } finally {
      loading.value = false
    }
  }

  async function deleteReleaseNote(id: string) {
    loading.value = true
    error.value = null
    try {
      await releaseNoteApi.delete(id)
      releaseNotes.value = releaseNotes.value.filter(rn => rn.id !== id)
      if (currentReleaseNote.value?.id === id) {
        currentReleaseNote.value = null
      }
    } catch (e) {
      error.value = e instanceof Error ? e.message : 'Failed to delete release note'
      throw e
    } finally {
      loading.value = false
    }
  }

  function clearCurrentReleaseNote() {
    currentReleaseNote.value = null
  }

  return {
    // State
    releaseNotes,
    currentReleaseNote,
    loading,
    error,
    // Getters
    popupReleaseNotes,
    releaseNotesByProject,
    releaseNotesByType,
    // Actions
    fetchReleaseNotes,
    fetchReleaseNoteById,
    createReleaseNote,
    updateReleaseNote,
    deleteReleaseNote,
    clearCurrentReleaseNote,
  }
})

// ============================================
// VOC Store
// ============================================
export const useVOCStore = defineStore('voc', () => {
  // State
  const vocs = ref<VOC[]>([])
  const currentVOC = ref<VOC | null>(null)
  const loading = ref(false)
  const error = ref<string | null>(null)

  // Getters
  const openVOCs = computed(() =>
    vocs.value.filter(v => v.status === 'open')
  )

  const inProgressVOCs = computed(() =>
    vocs.value.filter(v => v.status === 'in_progress')
  )

  const resolvedVOCs = computed(() =>
    vocs.value.filter(v => v.status === 'resolved' || v.status === 'closed')
  )

  const vocsByStatus = computed(() => {
    const grouped: Record<string, VOC[]> = {
      open: [],
      in_progress: [],
      pending: [],
      resolved: [],
      closed: [],
    }
    vocs.value.forEach(voc => {
      if (grouped[voc.status]) {
        grouped[voc.status]!.push(voc)
      }
    })
    return grouped
  })

  const vocsByCategory = computed(() => {
    const grouped: Record<string, VOC[]> = {}
    vocs.value.forEach(voc => {
      const cat = voc.category
      if (!grouped[cat]) {
        grouped[cat] = []
      }
      grouped[cat]!.push(voc)
    })
    return grouped
  })

  const vocsByPriority = computed(() => {
    const grouped: Record<string, VOC[]> = {
      critical: [],
      high: [],
      medium: [],
      low: [],
    }
    vocs.value.forEach(voc => {
      if (grouped[voc.priority]) {
        grouped[voc.priority]!.push(voc)
      }
    })
    return grouped
  })

  const overdueVOCs = computed(() =>
    vocs.value.filter(v => {
      if (!v.dueDate || v.status === 'resolved' || v.status === 'closed') return false
      return new Date(v.dueDate) < new Date()
    })
  )

  // Actions
  async function fetchVOCs(params?: VOCQueryParams) {
    loading.value = true
    error.value = null
    try {
      vocs.value = await vocApi.getList(params)
    } catch (e) {
      error.value = e instanceof Error ? e.message : 'Failed to fetch VOCs'
      throw e
    } finally {
      loading.value = false
    }
  }

  async function fetchVOCById(id: string) {
    loading.value = true
    error.value = null
    try {
      currentVOC.value = await vocApi.getById(id)
      // Increment view count
      if (currentVOC.value) {
        await vocApi.incrementViewCount(id, currentVOC.value.viewCount)
        currentVOC.value.viewCount += 1
      }
    } catch (e) {
      error.value = e instanceof Error ? e.message : 'Failed to fetch VOC'
      throw e
    } finally {
      loading.value = false
    }
  }

  async function createVOC(voc: Omit<VOC, 'id' | 'viewCount' | 'comments' | 'createdAt' | 'updatedAt'>) {
    loading.value = true
    error.value = null
    try {
      const created = await vocApi.create(voc)
      vocs.value.unshift(created)
      return created
    } catch (e) {
      error.value = e instanceof Error ? e.message : 'Failed to create VOC'
      throw e
    } finally {
      loading.value = false
    }
  }

  async function updateVOC(id: string, voc: Partial<VOC>) {
    loading.value = true
    error.value = null
    try {
      const updated = await vocApi.update(id, voc)
      const index = vocs.value.findIndex(v => v.id === id)
      if (index !== -1) {
        vocs.value[index] = updated
      }
      if (currentVOC.value?.id === id) {
        currentVOC.value = updated
      }
      return updated
    } catch (e) {
      error.value = e instanceof Error ? e.message : 'Failed to update VOC'
      throw e
    } finally {
      loading.value = false
    }
  }

  async function deleteVOC(id: string) {
    loading.value = true
    error.value = null
    try {
      await vocApi.delete(id)
      vocs.value = vocs.value.filter(v => v.id !== id)
      if (currentVOC.value?.id === id) {
        currentVOC.value = null
      }
    } catch (e) {
      error.value = e instanceof Error ? e.message : 'Failed to delete VOC'
      throw e
    } finally {
      loading.value = false
    }
  }

  async function updateVOCStatus(id: string, status: VOC['status'], resolution?: string) {
    loading.value = true
    error.value = null
    try {
      const updated = await vocApi.updateStatus(id, status, resolution)
      const index = vocs.value.findIndex(v => v.id === id)
      if (index !== -1) {
        vocs.value[index] = updated
      }
      if (currentVOC.value?.id === id) {
        currentVOC.value = updated
      }
      return updated
    } catch (e) {
      error.value = e instanceof Error ? e.message : 'Failed to update VOC status'
      throw e
    } finally {
      loading.value = false
    }
  }

  async function assignVOC(id: string, assignee: TeamMember) {
    loading.value = true
    error.value = null
    try {
      const updated = await vocApi.assignTo(id, assignee)
      const index = vocs.value.findIndex(v => v.id === id)
      if (index !== -1) {
        vocs.value[index] = updated
      }
      if (currentVOC.value?.id === id) {
        currentVOC.value = updated
      }
      return updated
    } catch (e) {
      error.value = e instanceof Error ? e.message : 'Failed to assign VOC'
      throw e
    } finally {
      loading.value = false
    }
  }

  async function addComment(id: string, comment: Omit<VOCComment, 'id' | 'createdAt'>) {
    loading.value = true
    error.value = null
    try {
      const voc = currentVOC.value || vocs.value.find(v => v.id === id)
      const updated = await vocApi.addComment(id, comment, voc?.comments || [])
      const index = vocs.value.findIndex(v => v.id === id)
      if (index !== -1) {
        vocs.value[index] = updated
      }
      if (currentVOC.value?.id === id) {
        currentVOC.value = updated
      }
      return updated
    } catch (e) {
      error.value = e instanceof Error ? e.message : 'Failed to add comment'
      throw e
    } finally {
      loading.value = false
    }
  }

  function clearCurrentVOC() {
    currentVOC.value = null
  }

  return {
    // State
    vocs,
    currentVOC,
    loading,
    error,
    // Getters
    openVOCs,
    inProgressVOCs,
    resolvedVOCs,
    vocsByStatus,
    vocsByCategory,
    vocsByPriority,
    overdueVOCs,
    // Actions
    fetchVOCs,
    fetchVOCById,
    createVOC,
    updateVOC,
    deleteVOC,
    updateVOCStatus,
    assignVOC,
    addComment,
    clearCurrentVOC,
  }
})

// ============================================
// Popup Store
// ============================================
export const usePopupStore = defineStore('popup', () => {
  // State
  const activePopups = ref<PopupConfig[]>([])
  const dismissedPopupIds = ref<Set<string>>(new Set())
  const loading = ref(false)
  const error = ref<string | null>(null)

  // Getters
  const visiblePopups = computed(() =>
    activePopups.value.filter(p => {
      if (!p.isActive) return false
      if (dismissedPopupIds.value.has(p.id)) return false

      const now = new Date()
      const start = new Date(p.startDate)
      const end = new Date(p.endDate)

      return now >= start && now <= end
    })
  )

  const noticePopups = computed(() =>
    visiblePopups.value.filter(p => p.type === 'notice')
  )

  const releaseNotePopups = computed(() =>
    visiblePopups.value.filter(p => p.type === 'release-note')
  )

  // Actions
  async function fetchActivePopups() {
    loading.value = true
    error.value = null
    try {
      activePopups.value = await popupApi.getActivePopups()

      // Load dismissed popups from localStorage
      const dismissed = localStorage.getItem('dismissedPopups')
      if (dismissed) {
        dismissedPopupIds.value = new Set(JSON.parse(dismissed))
      }
    } catch (e) {
      error.value = e instanceof Error ? e.message : 'Failed to fetch popups'
      throw e
    } finally {
      loading.value = false
    }
  }

  function dismissPopup(popupId: string, showOnce: boolean) {
    dismissedPopupIds.value.add(popupId)

    if (showOnce) {
      // Persist to localStorage if "show once" is enabled
      const dismissed = Array.from(dismissedPopupIds.value)
      localStorage.setItem('dismissedPopups', JSON.stringify(dismissed))
    }
  }

  function resetDismissedPopups() {
    dismissedPopupIds.value.clear()
    localStorage.removeItem('dismissedPopups')
  }

  async function createPopup(popup: Omit<PopupConfig, 'id' | 'createdAt'>) {
    loading.value = true
    error.value = null
    try {
      const created = await popupApi.create(popup)
      activePopups.value.push(created)
      return created
    } catch (e) {
      error.value = e instanceof Error ? e.message : 'Failed to create popup'
      throw e
    } finally {
      loading.value = false
    }
  }

  async function updatePopup(id: string, popup: Partial<PopupConfig>) {
    loading.value = true
    error.value = null
    try {
      const updated = await popupApi.update(id, popup)
      const index = activePopups.value.findIndex(p => p.id === id)
      if (index !== -1) {
        activePopups.value[index] = updated
      }
      return updated
    } catch (e) {
      error.value = e instanceof Error ? e.message : 'Failed to update popup'
      throw e
    } finally {
      loading.value = false
    }
  }

  async function deactivatePopup(id: string) {
    loading.value = true
    error.value = null
    try {
      await popupApi.deactivate(id)
      activePopups.value = activePopups.value.filter(p => p.id !== id)
    } catch (e) {
      error.value = e instanceof Error ? e.message : 'Failed to deactivate popup'
      throw e
    } finally {
      loading.value = false
    }
  }

  return {
    // State
    activePopups,
    dismissedPopupIds,
    loading,
    error,
    // Getters
    visiblePopups,
    noticePopups,
    releaseNotePopups,
    // Actions
    fetchActivePopups,
    dismissPopup,
    resetDismissedPopups,
    createPopup,
    updatePopup,
    deactivatePopup,
  }
})
