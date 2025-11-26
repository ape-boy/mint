import api from './client'
import type { Layer } from '@/types'

export const layerApi = {
  getAll: (projectId?: string) => {
    const params = projectId ? { projectId } : {}
    return api.get<Layer[]>('/layers', { params })
  },
  getById: (id: string) => api.get<Layer>(`/layers/${id}`),
}
