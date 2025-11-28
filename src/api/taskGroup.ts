import { supabase, Tables } from './supabase'
import type { TaskGroup } from '@/types'

// DB 스키마 (snake_case) -> 프론트엔드 타입 (camelCase) 변환
function toTaskGroup(row: Record<string, unknown>): TaskGroup {
  return {
    id: row.id as string,
    name: row.name as string,
    product: row.product as string,
    controller: row.controller as string,
    createdAt: row.created_at as string,
    updatedAt: row.updated_at as string,
  }
}

export const taskGroupApi = {
  async getAll() {
    const { data, error } = await supabase
      .from(Tables.taskGroups)
      .select('*')
      .order('created_at', { ascending: true })

    if (error) throw error
    return { data: (data || []).map(toTaskGroup) }
  },

  async getById(id: string) {
    const { data, error } = await supabase
      .from(Tables.taskGroups)
      .select('*')
      .eq('id', id)
      .single()

    if (error) throw error
    return { data: toTaskGroup(data) }
  },
}
