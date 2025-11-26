# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

MintPortal is a Vue 3 + TypeScript web application for CI/CD pipeline management and DevOps operations for an **SSD Firmware Development Organization**. It provides a dashboard for build management, release tracking, project management, and AI-powered operations insights.

**Domain Context:** SSD FW 개발 조직의 CI/CD 빌드 및 릴리즈 관리 시스템

---

## Domain Model (도메인 모델)

### 1. Project Hierarchy (프로젝트 계층 구조)

```
Project Group (제품)           예: BM1743, PM9E1, PM9A3
    └── Project (세부 구성)     예: BM1743_LENOVO_1TB, BM1743_DELL_2TB
        └── Layer (빌드 계층)   예: Release, HIL, FIL, FTL
            └── Build (빌드)    파이프라인 실행 단위
```

| 계층 | 설명 | 예시 |
|------|------|------|
| **Project Group** | 제품 라인 (SSD 모델) | BM1743 (Enterprise SSD), PM9E1 (Consumer SSD) |
| **Project** | 제품의 세부 구성 (고객사 + 용량/스펙) | BM1743_LENOVO_1TB, BM1743_DELL_2TB |
| **Layer** | 빌드 유형/목적 (Release, 테스트 등) | Release, HIL, FIL, FTL (사용자 정의 가능) |
| **Build** | 특정 Layer에서 실행된 빌드 인스턴스 | Build #1245 |

---

### 2. Project Information (프로젝트 정보)

각 프로젝트에는 다음 정보가 포함됩니다:

| 필드 | 설명 | 예시 |
|------|------|------|
| **OEM** | 고객사/제조사 | Lenovo, Dell, Samsung, AWS |
| **TL (Tech Lead)** | 프로젝트 기술 리더 | 김철수 (kim.cs@company.com) |
| **Members** | 프로젝트 팀원 목록 | TL, Developer, QA, PM 역할 |
| **Milestones** | 마일스톤 일정 | MP, ES, CS, QS |
| **Config** | 기본 빌드 형상 정보 | Repository, Branch, Build Options |

**Milestone 유형:**
- **MP** (Mass Production): 양산 마일스톤
- **ES** (Engineering Sample): 엔지니어링 샘플
- **CS** (Customer Sample): 고객 샘플
- **QS** (Qualification Sample): 품질 검증 샘플

**Config (형상 정보):**
- Nightly KPI 빌드에 적용되는 기본 설정
- Repository URL, Default Branch, Build Options 정의

---

### 3. Build Pipeline (빌드 파이프라인)

파이프라인은 **YAML 설정으로 동적 구성**되며, 웹뷰에서 시각화됩니다.

#### Standard Pipeline Stages (표준 파이프라인 스테이지)

| Stage | 설명 |
|-------|------|
| **Build** | 코어 컴파일 (FW 빌드) |
| **SAM** | Static Analysis Module |
| **Coverity** | 정적 코드 분석 |
| **DoBEE** | 내부 검증 도구 |
| **TASTY** | 내부 테스트 도구 |
| **Warning Count** | 빌드 경고 메트릭 수집 |
| **BlackDuck** | 오픈소스 라이선스 컴플라이언스 |
| **TEST1** | 커스텀 테스트 스테이지 1 |
| **TEST2** | 커스텀 테스트 스테이지 2 |

#### Pipeline Configuration (파이프라인 설정)

```yaml
# 예시: pipeline.yml
stages:
  - name: Build
    enabled: true
    required: true
  - name: SAM
    enabled: true
    required: false
  - name: Coverity
    enabled: true
    required: true
  - name: DoBEE
    enabled: false
  - name: TASTY
    enabled: true
  - name: Warning Count
    enabled: true
  - name: BlackDuck
    enabled: true
  - name: TEST1
    enabled: true
  - name: TEST2
    enabled: false
```

#### Pipeline Features
- **Dynamic Configuration**: YAML 기반으로 스테이지 활성화/비활성화 가능
- **Visual Pipeline**: 웹뷰에서 파이프라인 그래프로 시각화
- **Stage Status**: 각 스테이지별 성공/실패/진행중/대기 상태 표시

---

### 4. Layer Types (빌드 계층 유형)

#### Release Layer (릴리즈 빌드)

공식 릴리즈 후보를 위한 빌드 계층입니다.

| 기능 | 설명 |
|------|------|
| **TR (Test Request)** | 릴리즈 요청 기능 |
| **Release Criteria** | 모든 파이프라인 스테이지 통과 필요 |
| **Approval Flow** | Criteria 미충족 시 → 결재 후 TR 가능 |
| **Binary Download** | 빌드 결과물 바이너리 다운로드 |
| **Log Download** | 빌드 로그 다운로드 |
| **Build Info** | 빌더 정보, 빌드 상세 설정 팝업 |

**TR (Test Request) Flow:**
```
┌─────────────────────────────────────────────────────────────┐
│ All Pipeline Stages Pass?                                   │
│   ├─ YES → TR Button Enabled → Release 가능                │
│   └─ NO  → Approval Required → 결재 완료 후 TR 가능        │
└─────────────────────────────────────────────────────────────┘
```

#### Custom Layers (사용자 정의 계층)

Release 외에 다양한 테스트/검증 목적의 Layer를 추가할 수 있습니다.

| Layer 예시 | 용도 |
|-----------|------|
| **HIL** | Hardware-in-the-Loop 테스트 |
| **FIL** | Firmware-in-the-Loop 테스트 |
| **FTL** | Flash Translation Layer 테스트 |
| **Custom** | 사용자 정의 테스트 |

**Custom Layer 특징:**
- 파이프라인 실행 및 빌드 기능 제공
- **Release/TR 기능 없음** (Release Layer 전용)
- 빌드 결과 및 로그 조회 가능

---

## Tech Stack

| Category | Technology |
|----------|------------|
| **Framework** | Vue 3 with Composition API (`<script setup>`) |
| **Build Tool** | Vite 7 |
| **Language** | TypeScript 5.9 (strict mode) |
| **State Management** | Pinia 3 |
| **UI Library** | Ant Design Vue 4.2 (Dark Theme) |
| **HTTP Client** | Axios |
| **Routing** | Vue Router 4 |
| **Mock Server** | json-server |

---

## Architecture

### Application Flow

```
index.html → src/main.ts → App.vue → Router → Views
```

### Directory Structure

```
src/
├── api/                    # API 서비스 모듈
│   ├── client.ts          # Axios 인스턴스 (인터셉터 포함)
│   ├── index.ts           # API exports
│   ├── projectGroup.ts    # Project Group API
│   ├── project.ts         # Project API
│   ├── layer.ts           # Layer API
│   ├── build.ts           # Build API
│   └── stats.ts           # Dashboard Stats API
├── components/             # 재사용 컴포넌트
│   ├── common/            # 공통 UI 컴포넌트
│   │   ├── PageHeader.vue
│   │   ├── StatusBadge.vue
│   │   ├── EmptyState.vue
│   │   └── index.ts
│   ├── project/           # 프로젝트 도메인 컴포넌트
│   │   ├── ProjectCard.vue
│   │   ├── ProjectGroupSidebar.vue
│   │   ├── MilestoneTimeline.vue
│   │   └── index.ts
│   ├── build/             # 빌드 도메인 컴포넌트
│   │   ├── PipelineVisualization.vue
│   │   ├── BuildCard.vue
│   │   ├── StageChip.vue
│   │   └── index.ts
│   ├── dashboard/         # 대시보드 컴포넌트
│   │   ├── StatCard.vue
│   │   ├── RecentBuildsList.vue
│   │   ├── ActiveProjectsList.vue
│   │   └── index.ts
│   └── index.ts           # 전체 컴포넌트 exports
├── composables/            # Vue Composables
│   ├── useStatus.ts       # 상태 아이콘/색상 로직
│   ├── useFormat.ts       # 날짜/시간/숫자 포맷팅
│   └── index.ts
├── styles/                 # 글로벌 스타일
│   ├── variables.css      # CSS 커스텀 프로퍼티
│   ├── base.css           # 리셋 및 기본 스타일
│   ├── components.css     # 재사용 컴포넌트 스타일
│   └── index.css          # 스타일 imports
├── types/                  # TypeScript 타입 정의
│   └── index.ts
├── stores/                 # Pinia 상태 관리
│   ├── projectGroup.ts
│   ├── project.ts
│   ├── layer.ts
│   └── build.ts
├── views/                  # 페이지 컴포넌트
│   ├── HomeView.vue
│   ├── ProjectListView.vue
│   ├── ProjectDetailView.vue
│   ├── BuildListView.vue
│   ├── BuildDetailView.vue
│   ├── BuildNewView.vue
│   ├── LoginView.vue
│   └── SettingsView.vue
├── layouts/
│   └── AppLayout.vue
└── router/
    └── index.ts
tests/
└── e2e/                    # E2E 테스트
    ├── api.test.sh        # API 엔드포인트 테스트
    ├── scenarios.test.sh  # 사용자 시나리오 테스트
    └── README.md
mock-server/
└── db.json                # Mock 데이터
docs/
└── REFACTORING_PRD.md     # 리팩토링 PRD 문서
```

### Routing Pattern

| Route | Description | Notes |
|-------|-------------|-------|
| `/login` | 로그인 (비인증) | |
| `/` | **프로젝트 목록** (메인 화면) | Card/Table 뷰 토글 지원 |
| `/projects/:projectId` | 프로젝트 상세 | TL, Members, Milestones, Layers, Builds |
| `/dashboard` | 대시보드 | Mock API 연동 통계 |
| `/build` | 빌드 목록 | |
| `/build/:id` | 빌드 상세 | 파이프라인 시각화, 아티팩트 |
| `/settings` | 설정 | |

### Design System

- **Theme**: Modern Dark Theme (Deep Blue/Purple gradient)
- **UI Pattern**: Glassmorphism (Sidebar, Header, Cards)
- **CSS Variables**: `src/style.css`에 정의

---

## Commands

```bash
npm run dev       # 개발 서버 실행 (HMR)
npm run build     # 프로덕션 빌드 (TypeScript 체크 포함)
npm run preview   # 프로덕션 빌드 미리보기
npm run mock      # Mock 서버 실행 (port 3001)
npm run dev:all   # Mock 서버 + 개발 서버 동시 실행
npm run test:e2e  # E2E 테스트 실행 (Mock 서버 필요)
```

---

## Development & Testing Rules (개발 및 테스트 규칙)

### Mock-First Development (Mock 우선 개발 원칙)

**모든 개발 및 테스트는 `mock-server/db.json` 기반으로 진행되어야 합니다.**

#### 핵심 원칙

| 규칙 | 설명 |
|------|------|
| **Mock 데이터 완전성** | 모든 API endpoint는 `mock-server/db.json`에 100% mocking 되어야 함 |
| **실제 코드 내 Mock 금지** | 컴포넌트/서비스 코드에 하드코딩된 mock 데이터 삽입 금지 |
| **임의 데이터 금지** | 테스트 통과를 위한 임의의 더미 데이터 사용 금지 |
| **TDD 방식 준수** | 새 기능 개발 시 mock 데이터 → API → UI 순서로 구현 |

#### 개발 워크플로우

```
┌─────────────────────────────────────────────────────────────────┐
│  1. Mock 서버 실행                                              │
│     npm run mock  (port 3001)                                   │
├─────────────────────────────────────────────────────────────────┤
│  2. 기능 구현/수정/변경                                          │
│     - 새 API 필요 시 → db.json에 먼저 데이터 추가               │
│     - 기존 API 변경 시 → db.json 스키마도 함께 수정             │
├─────────────────────────────────────────────────────────────────┤
│  3. Mock 서버 검증 (필수)                                       │
│     - curl 또는 브라우저로 API 응답 확인                        │
│     - 예: curl http://localhost:3001/api/projects               │
├─────────────────────────────────────────────────────────────────┤
│  4. 통합 테스트                                                  │
│     npm run dev:all  (Mock + Dev 서버 동시 실행)                │
└─────────────────────────────────────────────────────────────────┘
```

#### 금지 사항 (Anti-patterns)

```typescript
// ❌ 금지: 컴포넌트 내 하드코딩된 mock 데이터
const mockProjects = [
  { id: 1, name: 'Test Project' }  // 절대 금지
]

// ❌ 금지: API 실패 시 fallback mock 데이터
const projects = await fetchProjects().catch(() => [
  { id: 1, name: 'Fallback' }  // 절대 금지
])

// ❌ 금지: 테스트용 임시 데이터
if (import.meta.env.DEV) {
  return fakeData  // 절대 금지
}
```

```typescript
// ✅ 올바른 방식: 항상 mock-server API 호출
const projects = await projectApi.getProjects()

// ✅ 올바른 방식: 새 기능은 db.json에 먼저 데이터 추가 후 개발
// mock-server/db.json에 데이터 추가 → API 호출 → UI 구현
```

#### Mock 데이터 변경 체크리스트

기능 구현/수정/변경 후 반드시 확인:

- [ ] `mock-server/db.json`에 필요한 모든 데이터가 존재하는가?
- [ ] 새로 추가된 API endpoint가 db.json 스키마와 일치하는가?
- [ ] ID 컨벤션을 준수하는가? (Project: 1000-1999, Layer: 2000-2999, Build: 3000-3999)
- [ ] `npm run mock` 실행 후 API 응답이 정상인가?
- [ ] 관계 데이터(groupId, projectId, layerId)가 올바르게 연결되어 있는가?

#### E2E 테스트 규칙

```
┌─────────────────────────────────────────────────────────────────┐
│ E2E 테스트는 반드시 mock-server가 제공하는 시나리오 내에서     │
│ 수행되어야 합니다.                                              │
├─────────────────────────────────────────────────────────────────┤
│ • 테스트 시나리오 = db.json에 존재하는 데이터                   │
│ • 새 테스트 케이스 = db.json에 먼저 데이터 추가                 │
│ • 엣지 케이스 테스트 = db.json에 해당 케이스 데이터 추가        │
└─────────────────────────────────────────────────────────────────┘
```

#### Mock 서버 실행 확인

```bash
# Mock 서버 단독 실행
npm run mock

# 정상 실행 확인
curl http://localhost:3001/api/projects
curl http://localhost:3001/api/builds
curl http://localhost:3001/api/stats

# 개발 서버와 함께 실행
npm run dev:all
```

---

## Mock Server API Endpoints

| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | `/api/project-groups` | 프로젝트 그룹(제품) 목록 |
| GET | `/api/project-groups/:id` | 프로젝트 그룹 상세 |
| GET | `/api/projects` | 프로젝트 목록 (?groupId= 필터) |
| GET | `/api/projects/:id` | 프로젝트 상세 |
| GET | `/api/layers` | Layer 목록 (?projectId= 필터) |
| GET | `/api/layers/:id` | Layer 상세 |
| GET | `/api/builds` | 빌드 목록 (?projectId=, ?layerId= 필터) |
| GET | `/api/builds/:id` | 빌드 상세 (파이프라인 상태 포함) |
| GET | `/api/stats` | 대시보드 통계 |

### Mock Data ID Convention

| Entity | ID Range | Example |
|--------|----------|---------|
| Project Group | 1-99 | "1", "2", "3" |
| Project | 1000-1999 | "1001", "1002" |
| Layer | 2000-2999 | "2001", "2002" |
| Build | 3000-3999 | "3001", "3002" |
| User | 100-199 | "101", "102" |

---

## Key Concepts Summary (핵심 개념 요약)

| 개념 | 정의 |
|------|------|
| **Project Group** | 제품 라인 (BM1743, PM9E1) |
| **Project** | 제품 세부 구성 (고객사_용량) |
| **Layer** | 빌드 계층/유형 (Release, HIL, FIL, FTL) |
| **Build** | 파이프라인 실행 인스턴스 |
| **Pipeline** | 빌드 스테이지 시퀀스 (YAML 설정) |
| **TR (Test Request)** | Release Layer에서 릴리즈 요청 |
| **Release Criteria** | TR 가능 조건 (모든 파이프라인 통과) |
| **Milestone** | 프로젝트 마일스톤 (MP/ES/CS/QS) |
| **Config** | 프로젝트 기본 형상 정보 (Nightly KPI용) |

---

## View Features

### ProjectListView (프로젝트 목록)

- **메인 화면** (`/` 경로)
- **Card View / Table View** 토글 지원
- **Project Group 필터** (좌측 사이드바)
- **검색** (이름, OEM, TL 기준)
- **표시 정보**: Status, Name, OEM, TL, Members 수, Updated 날짜

### ProjectDetailView (프로젝트 상세)

- **Project Header**: Name, OEM Badge, Status
- **Info Cards**: TL, Team Members, Build Config, Milestones
- **Layer Tabs**: Release, HIL, FIL, FTL 탭
- **Build List**: 각 Layer별 빌드 목록
- **Pipeline Visualization**: 빌드별 파이프라인 스테이지 상태
- **TR Status**: Release Layer 빌드의 TR 상태 표시

### HomeView (대시보드)

- **Stats Cards**: Success Rate, Total Builds, Active Projects, Running Builds
- **Recent Builds**: 최근 빌드 목록 (Mock API 연동)
- **Active Projects**: 프로젝트 요약 목록
