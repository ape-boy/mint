import { supabase, Tables } from './supabase'
import type { Layer, LayerQueryParams, PipelineStageConfig } from '@/types'

// DB 스키마 (snake_case) -> 프론트엔드 타입 (camelCase) 변환
function toLayer(row: Record<string, unknown>): Layer {
  return {
    id: row.id as string,
    projectId: row.project_id as string,
    name: row.name as string,
    type: row.type as Layer['type'],
    pipelineConfig: (row.pipeline_config || []) as PipelineStageConfig[],
    createdAt: row.created_at as string,
    updatedAt: row.updated_at as string,
  }
}

export const layerApi = {
  async getAll(params?: LayerQueryParams) {
    let query = supabase
      .from(Tables.layers)
      .select('*')
      .order('created_at', { ascending: true })

    if (params?.projectId) {
      query = query.eq('project_id', params.projectId)
    }

    const { data, error } = await query

    if (error) throw error
    return { data: (data || []).map(toLayer) }
  },

  async getById(id: string) {
    const { data, error } = await supabase
      .from(Tables.layers)
      .select('*')
      .eq('id', id)
      .single()

    if (error) throw error
    return { data: toLayer(data) }
  },

  async getByProjectId(projectId: string) {
    const { data, error } = await supabase
      .from(Tables.layers)
      .select('*')
      .eq('project_id', projectId)
      .order('created_at', { ascending: true })

    if (error) throw error
    return { data: (data || []).map(toLayer) }
  },
}
