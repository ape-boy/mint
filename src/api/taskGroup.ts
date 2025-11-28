import api from './client'
import type { TaskGroup } from '@/types'

export const taskGroupApi = {
  getAll() {
    return api.get<TaskGroup[]>('/task-groups')
  },

  getById(id: string) {
    return api.get<TaskGroup>(`/task-groups/${id}`)
  },
}
