import api from './client'
import type { Project } from '@/types'

export const projectApi = {
  getAll: (groupId?: string) => {
    const params = groupId ? { groupId } : {}
    return api.get<Project[]>('/projects', { params })
  },
  getById: (id: string) => api.get<Project>(`/projects/${id}`),
}
