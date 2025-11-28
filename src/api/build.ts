import api from './client'
import type { Build, BuildQueryParams } from '@/types'

export const buildApi = {
  getAll(params?: BuildQueryParams) {
    return api.get<Build[]>('/builds', { params })
  },

  getById(id: string) {
    return api.get<Build>(`/builds/${id}`)
  },

  getByProjectId(projectId: string) {
    return api.get<Build[]>('/builds', { params: { projectId } })
  },

  getByLayerId(layerId: string) {
    return api.get<Build[]>('/builds', { params: { layerId } })
  },

  create(data: Partial<Build>) {
    return api.post<Build>('/builds', data)
  },

  triggerRelease(buildId: string) {
    return api.patch<Build>(`/builds/${buildId}`, { releaseStatus: 'released' })
  },
}
