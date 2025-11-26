// ============================================
// Project Group (제품 라인)
// ============================================
export interface ProjectGroup {
  id: string
  name: string                    // BM1743, PM9E1
  createdAt: string
  updatedAt: string
}

// ============================================
// Team Member (팀원)
// ============================================
export interface TeamMember {
  id: string
  name: string
  email: string
  role: 'TL' | 'Developer' | 'QA' | 'PM'
  avatar?: string
}

// ============================================
// Milestone (마일스톤)
// ============================================
export interface Milestone {
  id: string
  name: string                    // MP, ES, CS, QS
  targetDate: string
  status: 'pending' | 'in_progress' | 'completed'
}

// ============================================
// Project Config (형상 정보)
// ============================================
export interface ProjectConfig {
  repository: string
  defaultBranch: string
  buildOptions: BuildOption[]
}

export interface BuildOption {
  key: string
  label: string
  defaultValue: boolean
}

// ============================================
// Project (프로젝트)
// ============================================
export interface Project {
  id: string
  groupId: string
  name: string                    // BM1743_LENOVO_1TB
  status: ProjectStatus

  // 프로젝트 정보
  oem: string                     // Lenovo, Dell, HP
  tl: TeamMember                  // Tech Lead
  members: TeamMember[]           // 팀원 목록

  // 형상 정보
  config: ProjectConfig

  // 마일스톤
  milestones: Milestone[]

  // 메타
  createdAt: string
  updatedAt: string
}

export type ProjectStatus = 'active' | 'inactive' | 'archived'

// ============================================
// Layer (빌드 계층)
// ============================================
export interface Layer {
  id: string
  projectId: string
  name: string                    // Release, HIL, FIL, FTL
  type: 'release' | 'custom'
  pipelineConfig: PipelineStageConfig[]
  createdAt: string
  updatedAt: string
}

export interface PipelineStageConfig {
  name: string                    // Build, SAM, Coverity...
  enabled: boolean
  required: boolean               // Release criteria용
}

// ============================================
// Build (빌드)
// ============================================
export interface Build {
  id: string
  projectId: string
  layerId: string
  buildNumber: number
  status: BuildStatus

  // 빌드 설정
  branch: string
  commitHash: string
  buildOptions: Record<string, boolean>

  // 파이프라인 스테이지
  stages: BuildStage[]

  // TR 관련 (Release Layer)
  trStatus?: TRStatus

  // 아티팩트
  artifacts?: BuildArtifacts

  // 메타
  triggeredBy: string
  startedAt: string
  finishedAt?: string | null
  duration?: number | null
}

export type BuildStatus = 'pending' | 'running' | 'success' | 'failed' | 'cancelled'
export type TRStatus = 'available' | 'pending_approval' | 'approved' | 'rejected'

export interface BuildStage {
  name: string
  status: BuildStageStatus
  duration?: number | null
  startedAt?: string
  finishedAt?: string
}

export type BuildStageStatus = 'pending' | 'running' | 'success' | 'failed' | 'skipped'

export interface BuildArtifacts {
  binaryUrl?: string
  logsUrl?: string
}

// ============================================
// Dashboard Stats (대시보드 통계)
// ============================================
export interface DashboardStats {
  successRate: number
  totalBuilds: number
  activeProjects: number
  runningBuilds: number
  recentBuilds: Build[]
}

// ============================================
// API Query Params
// ============================================
export interface ProjectQueryParams {
  groupId?: string
}

export interface LayerQueryParams {
  projectId?: string
}

export interface BuildQueryParams {
  projectId?: string
  layerId?: string
}
