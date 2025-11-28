import { supabase, Tables } from './supabase'
import type { DashboardStats, Build } from '@/types'

export const statsApi = {
  async getDashboardStats() {
    // 빌드 통계 계산
    const { data: builds, error: buildsError } = await supabase
      .from(Tables.builds)
      .select('*')
      .order('started_at', { ascending: false })

    if (buildsError) throw buildsError

    // 프로젝트 수 계산
    const { count: activeProjects, error: projectsError } = await supabase
      .from(Tables.projects)
      .select('*', { count: 'exact', head: true })
      .eq('status', 'active')

    if (projectsError) throw projectsError

    const allBuilds = builds || []
    const totalBuilds = allBuilds.length
    const successBuilds = allBuilds.filter(b => b.status === 'success').length
    const runningBuilds = allBuilds.filter(b => b.status === 'running').length
    const successRate = totalBuilds > 0 ? Math.round((successBuilds / totalBuilds) * 100) : 0

    // 최근 빌드 5개
    const recentBuilds = allBuilds.slice(0, 5).map(row => ({
      id: row.id,
      projectId: row.project_id,
      layerId: row.layer_id,
      round: row.round,
      buildNumber: row.build_number,
      status: row.status,
      fwName: row.fw_name,
      triggeredBy: typeof row.triggered_by === 'string' ? row.triggered_by : row.triggered_by?.name || 'Unknown',
      startedAt: row.started_at,
      finishedAt: row.finished_at,
      duration: row.duration,
    })) as unknown as Build[]

    // 품질 지표 평균 계산
    const buildsWithMetrics = allBuilds.filter(b => b.quality_metrics)
    const coverityScores = buildsWithMetrics
      .map(b => b.quality_metrics?.coverity?.score)
      .filter((s): s is number => typeof s === 'number')
    const samScores = buildsWithMetrics
      .map(b => b.quality_metrics?.sam?.score)
      .filter((s): s is number => typeof s === 'number')

    const coverityAvg = coverityScores.length > 0
      ? Math.round(coverityScores.reduce((a, b) => a + b, 0) / coverityScores.length)
      : 0
    const samAvg = samScores.length > 0
      ? Math.round(samScores.reduce((a, b) => a + b, 0) / samScores.length)
      : 0

    const stats: DashboardStats = {
      successRate,
      totalBuilds,
      activeProjects: activeProjects || 0,
      runningBuilds,
      recentBuilds,
      qualityOverview: {
        coverityAvg,
        samAvg,
        passRate: successRate,
      },
    }

    return { data: stats }
  },
}
