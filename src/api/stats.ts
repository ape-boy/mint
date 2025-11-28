import api from './client'
import type { DashboardStats } from '@/types'

export const statsApi = {
  getDashboardStats() {
    return api.get<DashboardStats>('/stats')
  },
}
