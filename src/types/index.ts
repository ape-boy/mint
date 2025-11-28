// ============================================
// 1. Task Group (과제 그룹 / Product)
// ============================================
export interface TaskGroup {
  id: string
  name: string                    // BM1743, PM9E1, PM9A3
  product: string                 // 제품명
  controller: string              // 컨트롤러
  createdAt: string
  updatedAt: string
}

// ProjectGroup (레거시 호환 - TaskGroup 별칭)
export type ProjectGroup = TaskGroup

// ============================================
// 2. Team Member (팀원)
// ============================================
export interface TeamMember {
  id: string
  name: string
  email: string
  role: 'PL' | 'TL' | 'Developer' | 'QA' | 'PM'
  avatar?: string
}

// ============================================
// 3. Milestone (마일스톤)
// ============================================
export interface Milestone {
  id: string
  name: 'MP' | 'ES' | 'CS' | 'QS'   // Mass Production, Engineering Sample, Customer Sample, Qualification Sample
  targetDate: string
  status: 'pending' | 'in_progress' | 'completed'
}

// ============================================
// 4. PLM Info (PLM 연동 정보)
// ============================================
export interface PLMInfo {
  plmId: string
  plmLink: string
}

// ============================================
// 5. SCM Config (형상 관리 설정)
// ============================================
export interface SCMConfig {
  repository: string
  branch: string
  revisionTag?: string
}

// ============================================
// 6. Build Environment (빌드 환경 설정)
// ============================================
export interface BuildEnvConfig {
  batchFilePath: string
  buildBatchOptions: string
}

// ============================================
// 7. Build Options (빌드 옵션)
// ============================================
export interface BuildOptions {
  sourceCodeZip: boolean
  onboardTestBinaryName?: string
}

// ============================================
// 8. Project (과제)
// ============================================
export interface Project {
  id: string
  groupId: string                  // TaskGroup ID
  taskCode: string                 // 과제 코드
  name: string                     // 과제 이름 (BM1743_LENOVO_1TB)
  status: ProjectStatus

  // 기본 정보
  oem: string                      // Lenovo, Dell, HP, AWS
  feature: string                  // 기능/특성
  target: string                   // 타겟

  // 팀 정보
  pl?: TeamMember                  // Project Lead
  tl: TeamMember                   // Tech Lead
  members: TeamMember[]            // 팀원 목록

  // 형상 정보
  scmConfig: SCMConfig
  buildEnvConfig: BuildEnvConfig
  buildOptions: BuildOptions

  // PLM 정보
  plmInfo?: PLMInfo

  // 마일스톤
  milestones: Milestone[]
  currentMilestone?: string        // 현재 마일스톤 단계

  // KPI
  kpiScore?: number

  // 메타
  createdAt: string
  updatedAt: string
}

export type ProjectStatus = 'active' | 'inactive' | 'archived'

// ============================================
// 9. Layer (빌드 계층)
// ============================================
export interface Layer {
  id: string
  projectId: string
  name: string                     // Release, HIL, FIL, FTL, Private
  type: 'release' | 'layer' | 'private'
  pipelineConfig: PipelineStageConfig[]
  createdAt: string
  updatedAt: string
}

export interface PipelineStageConfig {
  name: string                     // Build, SAM, Coverity, DoBEE, TASTY, Warning Count, BlackDuck, OnBoard Test, Coding Rule Check
  enabled: boolean
  required: boolean                // Release criteria용
  settings?: PipelineStageSettings // 스테이지별 세부 설정
}

// ============================================
// 9-1. Pipeline Stage Settings (세부 설정)
// ============================================
export interface PipelineStageSettings {
  // 공통 설정
  timeout?: number                 // 분 단위 타임아웃
  retryCount?: number              // 재시도 횟수

  // 스테이지별 특수 설정
  coverity?: CoveritySettings
  sam?: SAMSettings
  onboardTest?: OnboardTestSettings
  blackduck?: BlackDuckSettings
  codingRuleCheck?: CodingRuleCheckSettings
  warningCount?: WarningCountSettings
  dobee?: DoBEESettings
  tasty?: TASTYSettings
  build?: BuildStageSettings
}

export interface CoveritySettings {
  threshold: number                // 허용 결함 임계값
  ruleSet: 'default' | 'strict' | 'custom'
  customRules?: string[]
}

export interface SAMSettings {
  level: 'basic' | 'standard' | 'strict'
  excludePaths?: string[]
}

export interface OnboardTestSettings {
  testSuite: string                // 테스트 스위트 이름
  skipTests?: string[]             // 스킵할 테스트 케이스
  parallelCount?: number           // 병렬 실행 수
}

export interface BlackDuckSettings {
  scanMode: 'full' | 'incremental'
  excludePatterns?: string[]
}

export interface CodingRuleCheckSettings {
  rules: string[]                  // 적용할 룰 목록
  severity: 'error' | 'warning' | 'info'
}

export interface WarningCountSettings {
  maxWarnings: number              // 최대 허용 워닝 수
  treatAsError: boolean            // 초과시 에러 처리 여부
}

export interface DoBEESettings {
  profile: string                  // DoBEE 프로파일
  targetBranch?: string
}

export interface TASTYSettings {
  configFile: string               // TASTY 설정 파일 경로
  testTimeout?: number             // 테스트별 타임아웃
}

export interface BuildStageSettings {
  optimization: 'debug' | 'release' | 'profile'
  cleanBuild: boolean              // 클린 빌드 여부
  parallelJobs?: number            // 병렬 컴파일 작업 수
}

// ============================================
// 10. Quality Metrics (품질 지표)
// ============================================
export interface QualityMetrics {
  coverity: QualityResult
  sam: QualityResult
  onboardTest: QualityResult
  dobee: QualityResult
  codingRuleCheck: QualityResult
  blackduck: QualityResult
}

export interface QualityResult {
  status: 'pass' | 'fail' | 'warning' | 'pending' | 'skipped'
  score?: number
  issues?: number
  details?: string
}

// ============================================
// 11. Build (빌드)
// ============================================
export interface Build {
  id: string
  projectId: string
  layerId: string
  round: number                    // Round 회차
  buildNumber: number
  status: BuildStatus

  // 빌드 설정 (SCM)
  scmConfig: SCMConfig

  // 빌드 환경
  buildEnvConfig?: BuildEnvConfig
  buildOptions?: BuildOptions

  // 파이프라인 스테이지
  stages: BuildStage[]

  // 품질 지표
  qualityMetrics?: QualityMetrics

  // Release 관련 (Release Layer)
  releaseCriteria?: ReleaseCriteria
  releaseStatus?: ReleaseStatus

  // 아티팩트
  artifacts?: BuildArtifacts

  // FW Name
  fwName?: string

  // 메타
  triggeredBy: TeamMember | string
  startedAt: string
  finishedAt?: string | null
  duration?: number | null
}

export type BuildStatus = 'pending' | 'running' | 'success' | 'failed' | 'cancelled'

export interface ReleaseCriteria {
  allStagesPassed: boolean
  coverityPassed: boolean
  samPassed: boolean
  onboardTestPassed: boolean
  blackduckPassed: boolean
  overallPassed: boolean
}

export type ReleaseStatus = 'available' | 'pending_approval' | 'approved' | 'rejected' | 'released'

// TR Status (레거시 호환)
export type TRStatus = 'available' | 'pending_approval' | 'approved' | 'rejected' | 'released'

export interface BuildStage {
  name: string
  status: BuildStageStatus
  duration?: number | null
  startedAt?: string
  finishedAt?: string
  artifacts?: StageArtifacts
  logs?: string                    // 로그 내용 (미리보기용)
  logUrl?: string                  // 전체 로그 URL
  summary?: StageSummary           // 스테이지 요약 정보
}

export interface StageSummary {
  message?: string                 // 요약 메시지
  errorCount?: number              // 에러 수
  warningCount?: number            // 워닝 수
  passedTests?: number             // 통과 테스트 수
  failedTests?: number             // 실패 테스트 수
  coverage?: number                // 커버리지 %
}

export type BuildStageStatus = 'pending' | 'running' | 'success' | 'failed' | 'skipped'

export interface StageArtifacts {
  binaryUrl?: string
  logsUrl?: string
}

export interface BuildArtifacts {
  binaryUrl?: string
  logsUrl?: string
  sourceZipUrl?: string
}

// ============================================
// 12. Project History (변경 이력)
// ============================================
export interface ProjectHistory {
  id: string
  projectId: string
  action: 'create' | 'update' | 'delete'
  field: string
  oldValue?: string
  newValue?: string
  reason?: string
  changedBy: TeamMember
  changedAt: string
}

// ============================================
// 13. Dashboard Stats (대시보드 통계)
// ============================================
export interface DashboardStats {
  successRate: number
  totalBuilds: number
  activeProjects: number
  runningBuilds: number
  recentBuilds: Build[]
  qualityOverview: {
    coverityAvg: number
    samAvg: number
    passRate: number
  }
}

// ============================================
// 14. API Query Params
// ============================================
export interface ProjectQueryParams {
  groupId?: string
  oem?: string
  feature?: string
  tl?: string
  taskCode?: string
  search?: string
}

export interface LayerQueryParams {
  projectId?: string
}

export interface BuildQueryParams {
  projectId?: string
  layerId?: string
  status?: BuildStatus
}

// ============================================
// 15. Main Dashboard Grid Data
// ============================================
export interface DashboardGridRow {
  id: string
  projectId: string
  groupTitle: string              // Task Group name
  oem: string
  feature: string
  projectName: string
  tl: TeamMember
  milestone: string
  kpiScore?: number
  result: BuildStatus
  date: string
  fwName?: string
  latestBuildId?: string
}

// ============================================
// 16. Board System (게시판 시스템)
// ============================================

// 공지사항 (Notice)
export interface Notice {
  id: string
  title: string
  content: string
  category: NoticeCategory
  priority: 'normal' | 'important' | 'urgent'
  isPinned: boolean
  isPopup: boolean                 // 팝업 표시 여부
  popupStartDate?: string          // 팝업 시작일
  popupEndDate?: string            // 팝업 종료일
  viewCount: number
  attachments?: Attachment[]
  author: TeamMember
  createdAt: string
  updatedAt: string
}

export type NoticeCategory = 'general' | 'system' | 'maintenance' | 'event'

// Release Note (릴리즈 노트)
export interface ReleaseNote {
  id: string
  title: string
  content: string
  version: string                  // 릴리즈 버전
  projectId?: string               // 연관 프로젝트
  projectName?: string
  releaseType: 'major' | 'minor' | 'patch' | 'hotfix'
  changes: ReleaseChange[]         // 변경 사항 목록
  isPopup: boolean                 // 팝업 표시 여부
  popupStartDate?: string
  popupEndDate?: string
  viewCount: number
  attachments?: Attachment[]
  author: TeamMember
  releasedAt: string
  createdAt: string
  updatedAt: string
}

export interface ReleaseChange {
  type: 'feature' | 'bugfix' | 'improvement' | 'breaking'
  description: string
}

// VOC (Voice of Customer)
export interface VOC {
  id: string
  title: string
  content: string
  category: VOCCategory
  priority: VOCPriority
  status: VOCStatus

  // 프로젝트 연관
  projectId?: string
  projectName?: string
  projectGroupId?: string
  projectGroupName?: string

  // 담당자 정보
  requester: TeamMember            // 신청자
  assignee?: TeamMember            // 담당자

  // 처리 정보
  dueDate?: string                 // 처리 기한
  resolvedAt?: string              // 처리 완료일
  resolution?: string              // 처리 내용

  // 메타
  viewCount: number
  attachments?: Attachment[]
  comments?: VOCComment[]
  createdAt: string
  updatedAt: string
}

export type VOCCategory =
  | 'bug'                          // 버그/오류
  | 'feature_request'              // 기능 요청
  | 'improvement'                  // 개선 요청
  | 'question'                     // 문의
  | 'documentation'                // 문서 관련
  | 'other'                        // 기타

export type VOCPriority = 'low' | 'medium' | 'high' | 'critical'

export type VOCStatus =
  | 'open'                         // 신규 접수
  | 'in_progress'                  // 처리 중
  | 'pending'                      // 보류
  | 'resolved'                     // 해결됨
  | 'closed'                       // 종료

export interface VOCComment {
  id: string
  content: string
  author: TeamMember
  createdAt: string
}

// 첨부파일
export interface Attachment {
  id: string
  name: string
  url: string
  size: number                     // bytes
  mimeType: string
}

// 팝업 설정
export interface PopupConfig {
  id: string
  type: 'notice' | 'release-note'
  boardId: string
  title: string
  content: string
  isActive: boolean
  startDate: string
  endDate: string
  showOnce: boolean                // 한 번만 표시
  createdAt: string

  // Notice specific
  category?: NoticeCategory
  priority?: 'normal' | 'important' | 'urgent'

  // Release Note specific
  version?: string
  releaseType?: 'major' | 'minor' | 'patch' | 'hotfix'
}

// Board Query Params
export interface NoticeQueryParams {
  category?: NoticeCategory
  priority?: string
  isPinned?: boolean
  search?: string
  page?: number
  limit?: number
}

export interface ReleaseNoteQueryParams {
  projectId?: string
  releaseType?: string
  search?: string
  page?: number
  limit?: number
}

export interface VOCQueryParams {
  category?: VOCCategory
  priority?: VOCPriority
  status?: VOCStatus
  projectId?: string
  requesterId?: string
  assigneeId?: string
  search?: string
  page?: number
  limit?: number
}
