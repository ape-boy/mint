# MintPortal Refactoring PRD

## Document Information
- **Version**: 1.0
- **Created**: 2024-11-26
- **Status**: Completed

---

## 1. Problem Statement

### 1.1 Current State Analysis

MintPortal 코드베이스에서 발견된 문제점:

#### Dead Code (죽은 코드)
| 파일 | 유형 | 문제 |
|------|------|------|
| `src/stores/counter.ts` | Store | 미사용 |
| `src/stores/user.ts` | Store | 미사용 |
| `src/stores/release.ts` | Store | 미사용 |
| `src/stores/aiops.ts` | Store | 미사용 |
| `src/views/AboutView.vue` | View | 미사용 |
| `src/views/ReleaseListView.vue` | View | 미사용 |
| `src/views/AIOpsDashboardView.vue` | View | 미사용 |
| `src/views/AIOpsQueryView.vue` | View | 미사용 |
| `src/assets/vue.svg` | Asset | 미사용 |

#### Code Duplication (코드 중복)
- **Status Icon/Color 로직**: 5개 이상의 View에서 동일한 switch문 반복
- **날짜/시간 포맷팅**: formatDate, formatDuration 함수 중복 정의
- **Empty State UI**: 데이터 없음 표시 패턴 반복

#### Architecture Issues (구조적 문제)
- **컴포넌트 부재**: `src/components/` 디렉토리 미사용
- **View 비대화**: 단일 View가 500-700줄 이상
- **CSS 변수 불일치**: 일부 하드코딩된 색상값
- **라우터 불일치**: 메뉴 키와 라우트 경로 불일치

#### Incomplete Features (미완성 기능)
- `LoginView.vue`: 17줄 스켈레톤
- `SettingsView.vue`: 7줄 스켈레톤

---

## 2. Goals & Objectives

### 2.1 Primary Goals
1. **코드 품질 향상**: 중복 제거, 재사용성 증가
2. **유지보수성 개선**: 명확한 컴포넌트 분리
3. **일관성 확보**: CSS 변수 시스템 통합
4. **완성도 향상**: 미완성 기능 구현

### 2.2 Success Metrics
| Metric | Before | Target | Actual |
|--------|--------|--------|--------|
| 평균 View 코드량 | ~550줄 | <250줄 | ~250줄 |
| 컴포넌트 수 | 0개 | 10개+ | 12개 |
| Composables 수 | 0개 | 2개+ | 2개 |
| TypeScript 에러 | 0 | 0 | 0 |
| 빌드 성공 | Yes | Yes | Yes |

---

## 3. Implementation Phases

### Phase 1: Dead Code Removal
**Status**: Completed

#### Tasks
- [x] 미사용 Store 파일 삭제 (4개)
- [x] 미사용 View 파일 삭제 (4개)
- [x] 미사용 Asset 삭제 (1개)
- [x] Router 정리 및 meta 추가

#### Deleted Files
```
src/stores/counter.ts
src/stores/user.ts
src/stores/release.ts
src/stores/aiops.ts
src/views/AboutView.vue
src/views/ReleaseListView.vue
src/views/AIOpsDashboardView.vue
src/views/AIOpsQueryView.vue
src/assets/vue.svg
```

---

### Phase 2: Directory Restructure
**Status**: Completed

#### New Structure
```
src/
├── api/
│   ├── client.ts          # Axios instance (renamed from axios.ts)
│   ├── index.ts           # API exports
│   ├── projectGroup.ts
│   ├── project.ts
│   ├── layer.ts
│   ├── build.ts
│   └── stats.ts
├── components/
│   ├── common/            # Shared UI components
│   ├── project/           # Project domain components
│   ├── build/             # Build domain components
│   ├── dashboard/         # Dashboard components
│   └── index.ts           # Component exports
├── composables/           # Vue composables
│   ├── useStatus.ts
│   ├── useFormat.ts
│   └── index.ts
├── styles/                # Global styles
│   ├── variables.css      # CSS custom properties
│   ├── base.css           # Reset & base styles
│   ├── components.css     # Reusable component styles
│   └── index.css          # Style imports
├── layouts/
├── router/
├── stores/
├── types/
└── views/
```

---

### Phase 3: Composables Implementation
**Status**: Completed

#### useStatus.ts
중앙화된 상태 관리 로직:
- `getBuildStatusConfig(status)` - 빌드 상태 아이콘/색상
- `getStageStatusConfig(status)` - 파이프라인 스테이지 상태
- `getProjectStatusConfig(status)` - 프로젝트 상태
- `getTRStatusConfig(status)` - TR 상태
- `getMilestoneStatusConfig(status)` - 마일스톤 상태

#### useFormat.ts
중앙화된 포맷팅 로직:
- `formatDuration(seconds)` - 시간 포맷 (5m 30s)
- `formatDate(dateStr)` - 날짜 포맷
- `formatDateTime(dateStr)` - 날짜시간 포맷
- `formatTimeAgo(dateStr)` - 상대 시간 (2h ago)
- `formatNumber(num)` - 숫자 천단위 구분
- `formatPercent(value)` - 퍼센트 표시
- `formatCommitHash(hash)` - 커밋 해시 단축

---

### Phase 4: Common Components
**Status**: Completed

| Component | Purpose | Props |
|-----------|---------|-------|
| `PageHeader` | 페이지 헤더 | title, description, #actions slot |
| `StatusBadge` | 상태 뱃지 | status, type (build/stage/project/tr), showLabel |
| `EmptyState` | 빈 상태 표시 | title, description, #action slot |

---

### Phase 5: Domain Components
**Status**: Completed

#### Project Components
| Component | Purpose |
|-----------|---------|
| `ProjectCard` | 프로젝트 카드 (리스트용) |
| `ProjectGroupSidebar` | 제품 그룹 필터 사이드바 |
| `MilestoneTimeline` | 마일스톤 타임라인 |

#### Build Components
| Component | Purpose |
|-----------|---------|
| `PipelineVisualization` | 파이프라인 스테이지 시각화 |
| `BuildCard` | 빌드 카드 (리스트용) |
| `StageChip` | 파이프라인 스테이지 칩 |

#### Dashboard Components
| Component | Purpose |
|-----------|---------|
| `StatCard` | 통계 카드 |
| `RecentBuildsList` | 최근 빌드 목록 |
| `ActiveProjectsList` | 활성 프로젝트 목록 |

---

### Phase 6: View Refactoring
**Status**: Completed

#### Code Reduction Summary
| View | Before | After | Reduction |
|------|--------|-------|-----------|
| HomeView | ~506줄 | 187줄 | 63% |
| ProjectListView | ~605줄 | 240줄 | 60% |
| ProjectDetailView | ~775줄 | 398줄 | 49% |
| BuildListView | ~460줄 | 318줄 | 31% |
| BuildDetailView | ~384줄 | 230줄 | 40% |
| BuildNewView | ~317줄 | 311줄 | 2% |
| LoginView | 17줄 | 172줄 | (완전 구현) |
| SettingsView | 7줄 | 287줄 | (완전 구현) |

---

### Phase 7: Layout Fixes
**Status**: Completed

#### Changes
- [x] 로고 이미지 경로 수정 (`@/assets/mint_portal_logo.png`)
- [x] 메뉴-라우터 동기화 (watch로 자동 선택)
- [x] 삭제된 AIops 메뉴 제거
- [x] CSS 변수 네이밍 통일 (`--color-*` prefix)
- [x] Breadcrumb 로직 개선 (route.meta 활용)

---

### Phase 8: Testing & Verification
**Status**: Completed

#### Build Verification
```bash
npm run build  # TypeScript 빌드 성공
```

#### API Endpoint Tests
| Endpoint | Status |
|----------|--------|
| GET /api/stats | Pass |
| GET /api/project-groups | Pass |
| GET /api/projects | Pass |
| GET /api/projects/:id | Pass |
| GET /api/layers | Pass |
| GET /api/builds | Pass |
| GET /api/builds/:id | Pass |

---

## 4. CSS Design System

### 4.1 Color Palette
```css
/* Background */
--color-bg-primary: #0f172a;
--color-bg-secondary: #1e293b;
--color-bg-tertiary: #334155;

/* Accent */
--color-accent-primary: #00c896;    /* Mint */
--color-accent-secondary: #2dd4bf;  /* Teal */
--color-accent-success: #34d399;
--color-accent-warning: #fbbf24;
--color-accent-danger: #f87171;

/* Text */
--color-text-primary: #f1f5f9;
--color-text-secondary: #cbd5e1;
--color-text-muted: #94a3b8;
```

### 4.2 Spacing Scale
```css
--spacing-xs: 4px;
--spacing-sm: 8px;
--spacing-md: 16px;
--spacing-lg: 24px;
--spacing-xl: 32px;
--spacing-2xl: 48px;
```

### 4.3 Typography
```css
--font-size-xs: 12px;
--font-size-sm: 13px;
--font-size-md: 14px;
--font-size-lg: 16px;
--font-size-xl: 18px;
--font-size-2xl: 20px;
```

---

## 5. File Inventory

### 5.1 Components (12 files)
```
src/components/
├── common/
│   ├── PageHeader.vue
│   ├── StatusBadge.vue
│   ├── EmptyState.vue
│   └── index.ts
├── project/
│   ├── ProjectCard.vue
│   ├── ProjectGroupSidebar.vue
│   ├── MilestoneTimeline.vue
│   └── index.ts
├── build/
│   ├── PipelineVisualization.vue
│   ├── BuildCard.vue
│   ├── StageChip.vue
│   └── index.ts
├── dashboard/
│   ├── StatCard.vue
│   ├── RecentBuildsList.vue
│   ├── ActiveProjectsList.vue
│   └── index.ts
└── index.ts
```

### 5.2 Composables (2 files)
```
src/composables/
├── useStatus.ts
├── useFormat.ts
└── index.ts
```

### 5.3 Styles (4 files)
```
src/styles/
├── variables.css
├── base.css
├── components.css
└── index.css
```

---

## 6. Conclusion

### Achievements
1. **9개 죽은 파일 제거**
2. **12개 재사용 컴포넌트 생성**
3. **2개 Composable 추출**
4. **View 평균 코드량 50% 감소**
5. **CSS 변수 시스템 통합 (109개 변수)**
6. **미완성 View 2개 완전 구현**
7. **TypeScript 빌드 성공**
8. **Mock API 연동 검증 완료**

### Technical Debt Resolved
- 코드 중복 제거
- 컴포넌트 미분리 해결
- CSS 하드코딩 제거
- 라우터-메뉴 불일치 수정
