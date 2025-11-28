import { supabase, Tables } from './supabase'
import type { Build, BuildQueryParams, SCMConfig, BuildEnvConfig, BuildOptions, BuildStage, QualityMetrics, ReleaseCriteria, BuildArtifacts, TeamMember } from '@/types'

// DB 스키마 (snake_case) -> 프론트엔드 타입 (camelCase) 변환
function toBuild(row: Record<string, unknown>): Build {
  return {
    id: row.id as string,
    projectId: row.project_id as string,
    layerId: row.layer_id as string,
    round: row.round as number,
    buildNumber: row.build_number as number,
    status: row.status as Build['status'],
    scmConfig: row.scm_config as SCMConfig,
    buildEnvConfig: row.build_env_config as BuildEnvConfig | undefined,
    buildOptions: row.build_options as BuildOptions | undefined,
    stages: (row.stages || []) as BuildStage[],
    qualityMetrics: row.quality_metrics as QualityMetrics | undefined,
    releaseCriteria: row.release_criteria as ReleaseCriteria | undefined,
    releaseStatus: row.release_status as Build['releaseStatus'],
    artifacts: row.artifacts as BuildArtifacts | undefined,
    fwName: row.fw_name as string | undefined,
    triggeredBy: row.triggered_by as TeamMember | string,
    startedAt: row.started_at as string,
    finishedAt: row.finished_at as string | null | undefined,
    duration: row.duration as number | null | undefined,
  }
}

// 프론트엔드 타입 (camelCase) -> DB 스키마 (snake_case) 변환
function toDbBuild(build: Partial<Build>): Record<string, unknown> {
  const result: Record<string, unknown> = {}

  if (build.projectId !== undefined) result.project_id = build.projectId
  if (build.layerId !== undefined) result.layer_id = build.layerId
  if (build.round !== undefined) result.round = build.round
  if (build.buildNumber !== undefined) result.build_number = build.buildNumber
  if (build.status !== undefined) result.status = build.status
  if (build.scmConfig !== undefined) result.scm_config = build.scmConfig
  if (build.buildEnvConfig !== undefined) result.build_env_config = build.buildEnvConfig
  if (build.buildOptions !== undefined) result.build_options = build.buildOptions
  if (build.stages !== undefined) result.stages = build.stages
  if (build.qualityMetrics !== undefined) result.quality_metrics = build.qualityMetrics
  if (build.releaseCriteria !== undefined) result.release_criteria = build.releaseCriteria
  if (build.releaseStatus !== undefined) result.release_status = build.releaseStatus
  if (build.artifacts !== undefined) result.artifacts = build.artifacts
  if (build.fwName !== undefined) result.fw_name = build.fwName
  if (build.triggeredBy !== undefined) result.triggered_by = build.triggeredBy
  if (build.startedAt !== undefined) result.started_at = build.startedAt
  if (build.finishedAt !== undefined) result.finished_at = build.finishedAt
  if (build.duration !== undefined) result.duration = build.duration

  return result
}

export const buildApi = {
  async getAll(params?: BuildQueryParams) {
    let query = supabase
      .from(Tables.builds)
      .select('*')
      .order('started_at', { ascending: false })

    if (params?.projectId) {
      query = query.eq('project_id', params.projectId)
    }
    if (params?.layerId) {
      query = query.eq('layer_id', params.layerId)
    }
    if (params?.status) {
      query = query.eq('status', params.status)
    }

    const { data, error } = await query

    if (error) throw error
    return { data: (data || []).map(toBuild) }
  },

  async getById(id: string) {
    const { data, error } = await supabase
      .from(Tables.builds)
      .select('*')
      .eq('id', id)
      .single()

    if (error) throw error
    return { data: toBuild(data) }
  },

  async getByProjectId(projectId: string) {
    const { data, error } = await supabase
      .from(Tables.builds)
      .select('*')
      .eq('project_id', projectId)
      .order('started_at', { ascending: false })

    if (error) throw error
    return { data: (data || []).map(toBuild) }
  },

  async getByLayerId(layerId: string) {
    const { data, error } = await supabase
      .from(Tables.builds)
      .select('*')
      .eq('layer_id', layerId)
      .order('started_at', { ascending: false })

    if (error) throw error
    return { data: (data || []).map(toBuild) }
  },

  async create(data: Partial<Build>) {
    const dbData = toDbBuild(data)
    dbData.id = `build_${Date.now()}`
    dbData.started_at = new Date().toISOString()

    const { data: result, error } = await supabase
      .from(Tables.builds)
      .insert(dbData)
      .select()
      .single()

    if (error) throw error
    return { data: toBuild(result) }
  },

  async triggerRelease(buildId: string) {
    const { data, error } = await supabase
      .from(Tables.builds)
      .update({ release_status: 'released' })
      .eq('id', buildId)
      .select()
      .single()

    if (error) throw error
    return { data: toBuild(data) }
  },
}
