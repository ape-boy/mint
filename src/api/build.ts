import api from './axios'
import type { Build } from '@/types'

export const buildApi = {
  getAll: (projectId?: string, layerId?: string) => {
    const params: Record<string, string> = {}
    if (projectId) params.projectId = projectId
    if (layerId) params.layerId = layerId
    return api.get<Build[]>('/builds', { params })
  },
  getById: (id: string) => api.get<Build>(`/builds/${id}`),
}
