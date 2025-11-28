import api from './client'
import type { Layer, LayerQueryParams } from '@/types'

export const layerApi = {
  getAll(params?: LayerQueryParams) {
    return api.get<Layer[]>('/layers', { params })
  },

  getById(id: string) {
    return api.get<Layer>(`/layers/${id}`)
  },

  getByProjectId(projectId: string) {
    return api.get<Layer[]>('/layers', { params: { projectId } })
  },
}
