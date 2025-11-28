import { supabase, Tables } from './supabase'
import type { Project, ProjectQueryParams, TeamMember, SCMConfig, BuildEnvConfig, BuildOptions, PLMInfo, Milestone } from '@/types'

// DB 스키마 (snake_case) -> 프론트엔드 타입 (camelCase) 변환
function toProject(row: Record<string, unknown>): Project {
  return {
    id: row.id as string,
    projectGroupId: row.task_group_id as string,
    taskCode: row.task_code as string,
    name: row.name as string,
    status: row.status as Project['status'],
    oem: row.oem as string,
    feature: row.feature as string,
    target: row.target as string,
    pl: row.pl as TeamMember | undefined,
    tl: row.tl as TeamMember,
    members: (row.members || []) as TeamMember[],
    scmConfig: row.scm_config as SCMConfig,
    buildEnvConfig: row.build_env_config as BuildEnvConfig,
    buildOptions: (row.build_options || {}) as BuildOptions,
    plmInfo: row.plm_info as PLMInfo | undefined,
    milestones: (row.milestones || []) as Milestone[],
    currentMilestone: row.current_milestone as string | undefined,
    kpiScore: row.kpi_score as number | undefined,
    createdAt: row.created_at as string,
    updatedAt: row.updated_at as string,
  }
}

// 프론트엔드 타입 (camelCase) -> DB 스키마 (snake_case) 변환
function toDbProject(project: Partial<Project>): Record<string, unknown> {
  const result: Record<string, unknown> = {}

  if (project.projectGroupId !== undefined) result.task_group_id = project.projectGroupId
  if (project.taskCode !== undefined) result.task_code = project.taskCode
  if (project.name !== undefined) result.name = project.name
  if (project.status !== undefined) result.status = project.status
  if (project.oem !== undefined) result.oem = project.oem
  if (project.feature !== undefined) result.feature = project.feature
  if (project.target !== undefined) result.target = project.target
  if (project.pl !== undefined) result.pl = project.pl
  if (project.tl !== undefined) result.tl = project.tl
  if (project.members !== undefined) result.members = project.members
  if (project.scmConfig !== undefined) result.scm_config = project.scmConfig
  if (project.buildEnvConfig !== undefined) result.build_env_config = project.buildEnvConfig
  if (project.buildOptions !== undefined) result.build_options = project.buildOptions
  if (project.plmInfo !== undefined) result.plm_info = project.plmInfo
  if (project.milestones !== undefined) result.milestones = project.milestones
  if (project.currentMilestone !== undefined) result.current_milestone = project.currentMilestone
  if (project.kpiScore !== undefined) result.kpi_score = project.kpiScore

  return result
}

export const projectApi = {
  async getAll(params?: ProjectQueryParams) {
    let query = supabase
      .from(Tables.projects)
      .select('*')
      .order('created_at', { ascending: true })

    // 필터 적용
    if (params?.projectGroupId) {
      query = query.eq('task_group_id', params.projectGroupId)
    }
    if (params?.oem) {
      query = query.eq('oem', params.oem)
    }

    const { data, error } = await query

    if (error) throw error
    return { data: (data || []).map(toProject) }
  },

  // Alias for getAll for backward compatibility
  async getList(params?: ProjectQueryParams): Promise<Project[]> {
    const response = await this.getAll(params)
    return response.data
  },

  async getById(id: string) {
    const { data, error } = await supabase
      .from(Tables.projects)
      .select('*')
      .eq('id', id)
      .single()

    if (error) throw error
    return { data: toProject(data) }
  },

  async update(id: string, data: Partial<Project>) {
    const dbData = toDbProject(data)
    dbData.updated_at = new Date().toISOString()

    const { data: result, error } = await supabase
      .from(Tables.projects)
      .update(dbData)
      .eq('id', id)
      .select()
      .single()

    if (error) throw error
    return { data: toProject(result) }
  },
}
