import api from './client'
import type { DashboardStats } from '@/types'

export const statsApi = {
  getDashboardStats: () => api.get<DashboardStats>('/stats'),
}
