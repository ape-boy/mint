import { supabase, Tables } from './supabase'
import type { ProjectHistory, TeamMember } from '@/types'

// DB 스키마 (snake_case) -> 프론트엔드 타입 (camelCase) 변환
function toProjectHistory(row: Record<string, unknown>): ProjectHistory {
  return {
    id: row.id as string,
    projectId: row.project_id as string,
    action: row.action as ProjectHistory['action'],
    field: row.field as string,
    oldValue: row.old_value as string | undefined,
    newValue: row.new_value as string | undefined,
    reason: row.reason as string | undefined,
    changedBy: row.changed_by as TeamMember,
    changedAt: row.changed_at as string,
  }
}

export const projectHistoryApi = {
  async getAll(projectId?: string) {
    let query = supabase
      .from(Tables.projectHistory)
      .select('*')
      .order('changed_at', { ascending: false })

    if (projectId) {
      query = query.eq('project_id', projectId)
    }

    const { data, error } = await query

    if (error) throw error
    return { data: (data || []).map(toProjectHistory) }
  },

  async getByProjectId(projectId: string) {
    const { data, error } = await supabase
      .from(Tables.projectHistory)
      .select('*')
      .eq('project_id', projectId)
      .order('changed_at', { ascending: false })

    if (error) throw error
    return { data: (data || []).map(toProjectHistory) }
  },
}
