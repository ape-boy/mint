import api from './client'
import type { ProjectHistory } from '@/types'

export const projectHistoryApi = {
  getAll(projectId?: string) {
    const params = projectId ? { projectId } : undefined
    return api.get<ProjectHistory[]>('/project-history', { params })
  },

  getByProjectId(projectId: string) {
    return api.get<ProjectHistory[]>('/project-history', { params: { projectId } })
  },
}
