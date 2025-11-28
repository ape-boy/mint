import api from './client'
import type { Project, ProjectQueryParams } from '@/types'

export const projectApi = {
  getAll(params?: ProjectQueryParams) {
    return api.get<Project[]>('/projects', { params })
  },

  // Alias for getAll for backward compatibility
  async getList(params?: ProjectQueryParams): Promise<Project[]> {
    const response = await api.get<Project[]>('/projects', { params })
    return response.data
  },

  getById(id: string) {
    return api.get<Project>(`/projects/${id}`)
  },

  update(id: string, data: Partial<Project>) {
    return api.patch<Project>(`/projects/${id}`, data)
  },
}
