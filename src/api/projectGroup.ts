import api from './axios'
import type { ProjectGroup } from '@/types'

export const projectGroupApi = {
  getAll: () => api.get<ProjectGroup[]>('/project-groups'),
  getById: (id: string) => api.get<ProjectGroup>(`/project-groups/${id}`),
}
