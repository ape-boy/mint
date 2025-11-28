import api from './client'
import type {
  Notice,
  NoticeQueryParams,
  ReleaseNote,
  ReleaseNoteQueryParams,
  VOC,
  VOCQueryParams,
  PopupConfig,
  VOCComment,
} from '../types'

// ============================================
// Notice API
// ============================================

export const noticeApi = {
  // 공지사항 목록 조회
  getList: async (params?: NoticeQueryParams): Promise<Notice[]> => {
    const response = await api.get<Notice[]>('/notices', { params })
    return response.data
  },

  // 공지사항 상세 조회
  getById: async (id: string): Promise<Notice> => {
    const response = await api.get<Notice>(`/notices/${id}`)
    return response.data
  },

  // 공지사항 생성
  create: async (notice: Omit<Notice, 'id' | 'viewCount' | 'createdAt' | 'updatedAt'>): Promise<Notice> => {
    const response = await api.post<Notice>('/notices', {
      ...notice,
      viewCount: 0,
      createdAt: new Date().toISOString(),
      updatedAt: new Date().toISOString(),
    })
    return response.data
  },

  // 공지사항 수정
  update: async (id: string, notice: Partial<Notice>): Promise<Notice> => {
    const response = await api.patch<Notice>(`/notices/${id}`, {
      ...notice,
      updatedAt: new Date().toISOString(),
    })
    return response.data
  },

  // 공지사항 삭제
  delete: async (id: string): Promise<void> => {
    await api.delete(`/notices/${id}`)
  },

  // 조회수 증가
  incrementViewCount: async (id: string, currentCount: number): Promise<Notice> => {
    const response = await api.patch<Notice>(`/notices/${id}`, {
      viewCount: currentCount + 1,
    })
    return response.data
  },
}

// ============================================
// Release Note API
// ============================================

export const releaseNoteApi = {
  // 릴리즈 노트 목록 조회
  getList: async (params?: ReleaseNoteQueryParams): Promise<ReleaseNote[]> => {
    const response = await api.get<ReleaseNote[]>('/release-notes', { params })
    return response.data
  },

  // 릴리즈 노트 상세 조회
  getById: async (id: string): Promise<ReleaseNote> => {
    const response = await api.get<ReleaseNote>(`/release-notes/${id}`)
    return response.data
  },

  // 릴리즈 노트 생성
  create: async (releaseNote: Omit<ReleaseNote, 'id' | 'viewCount' | 'createdAt' | 'updatedAt'>): Promise<ReleaseNote> => {
    const response = await api.post<ReleaseNote>('/release-notes', {
      ...releaseNote,
      viewCount: 0,
      createdAt: new Date().toISOString(),
      updatedAt: new Date().toISOString(),
    })
    return response.data
  },

  // 릴리즈 노트 수정
  update: async (id: string, releaseNote: Partial<ReleaseNote>): Promise<ReleaseNote> => {
    const response = await api.patch<ReleaseNote>(`/release-notes/${id}`, {
      ...releaseNote,
      updatedAt: new Date().toISOString(),
    })
    return response.data
  },

  // 릴리즈 노트 삭제
  delete: async (id: string): Promise<void> => {
    await api.delete(`/release-notes/${id}`)
  },

  // 조회수 증가
  incrementViewCount: async (id: string, currentCount: number): Promise<ReleaseNote> => {
    const response = await api.patch<ReleaseNote>(`/release-notes/${id}`, {
      viewCount: currentCount + 1,
    })
    return response.data
  },
}

// ============================================
// VOC API
// ============================================

export const vocApi = {
  // VOC 목록 조회
  getList: async (params?: VOCQueryParams): Promise<VOC[]> => {
    const response = await api.get<VOC[]>('/vocs', { params })
    return response.data
  },

  // VOC 상세 조회
  getById: async (id: string): Promise<VOC> => {
    const response = await api.get<VOC>(`/vocs/${id}`)
    return response.data
  },

  // VOC 생성
  create: async (voc: Omit<VOC, 'id' | 'viewCount' | 'comments' | 'createdAt' | 'updatedAt'>): Promise<VOC> => {
    const response = await api.post<VOC>('/vocs', {
      ...voc,
      viewCount: 0,
      comments: [],
      createdAt: new Date().toISOString(),
      updatedAt: new Date().toISOString(),
    })
    return response.data
  },

  // VOC 수정
  update: async (id: string, voc: Partial<VOC>): Promise<VOC> => {
    const response = await api.patch<VOC>(`/vocs/${id}`, {
      ...voc,
      updatedAt: new Date().toISOString(),
    })
    return response.data
  },

  // VOC 삭제
  delete: async (id: string): Promise<void> => {
    await api.delete(`/vocs/${id}`)
  },

  // VOC 상태 변경
  updateStatus: async (id: string, status: VOC['status'], resolution?: string): Promise<VOC> => {
    const updateData: Partial<VOC> = {
      status,
      updatedAt: new Date().toISOString(),
    }

    if (status === 'resolved' || status === 'closed') {
      updateData.resolvedAt = new Date().toISOString()
      if (resolution) {
        updateData.resolution = resolution
      }
    }

    const response = await api.patch<VOC>(`/vocs/${id}`, updateData)
    return response.data
  },

  // VOC 담당자 배정
  assignTo: async (id: string, assignee: VOC['assignee']): Promise<VOC> => {
    const response = await api.patch<VOC>(`/vocs/${id}`, {
      assignee,
      status: 'in_progress',
      updatedAt: new Date().toISOString(),
    })
    return response.data
  },

  // 댓글 추가
  addComment: async (id: string, comment: Omit<VOCComment, 'id' | 'createdAt'>, existingComments: VOCComment[] = []): Promise<VOC> => {
    const newComment: VOCComment = {
      ...comment,
      id: `cmt${Date.now()}`,
      createdAt: new Date().toISOString(),
    }

    const response = await api.patch<VOC>(`/vocs/${id}`, {
      comments: [...existingComments, newComment],
      updatedAt: new Date().toISOString(),
    })
    return response.data
  },

  // 조회수 증가
  incrementViewCount: async (id: string, currentCount: number): Promise<VOC> => {
    const response = await api.patch<VOC>(`/vocs/${id}`, {
      viewCount: currentCount + 1,
    })
    return response.data
  },
}

// ============================================
// Popup API
// ============================================

export const popupApi = {
  // 활성화된 팝업 목록 조회
  getActivePopups: async (): Promise<PopupConfig[]> => {
    const response = await api.get<PopupConfig[]>('/active-popups', {
      params: { isActive: true },
    })
    return response.data
  },

  // 팝업 설정 조회
  getById: async (id: string): Promise<PopupConfig> => {
    const response = await api.get<PopupConfig>(`/active-popups/${id}`)
    return response.data
  },

  // 팝업 설정 생성
  create: async (popup: Omit<PopupConfig, 'id' | 'createdAt'>): Promise<PopupConfig> => {
    const response = await api.post<PopupConfig>('/active-popups', {
      ...popup,
      createdAt: new Date().toISOString(),
    })
    return response.data
  },

  // 팝업 설정 수정
  update: async (id: string, popup: Partial<PopupConfig>): Promise<PopupConfig> => {
    const response = await api.patch<PopupConfig>(`/active-popups/${id}`, popup)
    return response.data
  },

  // 팝업 비활성화
  deactivate: async (id: string): Promise<PopupConfig> => {
    const response = await api.patch<PopupConfig>(`/active-popups/${id}`, {
      isActive: false,
    })
    return response.data
  },

  // 팝업 삭제
  delete: async (id: string): Promise<void> => {
    await api.delete(`/active-popups/${id}`)
  },
}
