# MintPortal 메뉴 구조 재설계 문서

## 1. 현재 상태 분석 (AS-IS)

### 1.1 발견된 문제점

#### 문제 1: 중복된 빌드 관련 페이지/기능
| 파일 | 용도 | 문제점 |
|------|------|--------|
| `BuildListView.vue` | 전체 빌드 목록 | ProjectDetailView의 빌드 탭과 기능 중복 |
| `BuildNewView.vue` | 새 빌드 생성 | ProjectDetailView의 빌드 설정과 중복 |
| `BuildDetailView.vue` | 빌드 상세 | ProjectDetailView의 빌드 상세와 중복 |
| `ProjectDetailView.vue` > 빌드 탭 | 프로젝트별 빌드 관리 | **실제 사용해야 할 핵심 기능** |

**결론:** BuildListView, BuildNewView, BuildDetailView는 불필요한 중복. 빌드는 항상 **프로젝트 컨텍스트** 내에서 관리되어야 함.

#### 문제 2: 라우터와 메뉴 불일치

현재 라우터 (`router/index.ts`):
```
/ → ProjectListView (과제 현황)
/projects/:projectId → ProjectDetailView
/dashboard → HomeView (대시보드)
/build → BuildListView (불필요)
/build/new → BuildNewView (불필요)
/build/:id → BuildDetailView (불필요)
/board → BoardView
/board/notice/:id → BoardView
/board/voc/:id → BoardView
/board/release-note/:id → BoardView
/settings → SettingsView
```

현재 메뉴 (`AppLayout.vue`):
```
├─ 과제 관리 (sub-menu)
│   ├─ 과제 현황 (projects-list)
│   └─ [프로젝트 상세 페이지에서만]
│       ├─ 과제 상세 그룹
│       │   ├─ Summary & Quality
│       │   ├─ 기본 정보
│       │   └─ DevOps 설정 (사용 안 함)
│       └─ 빌드 관리 그룹
│           ├─ 빌드 목록
│           └─ 새 빌드 (불필요)
├─ 대시보드
├─ 게시판 (sub-menu)
│   ├─ 공지사항
│   ├─ VOC
│   └─ Release Note
└─ 설정
```

#### 문제 3: 탭 vs 메뉴 불일치

ProjectDetailView 내부 탭:
- `summary` → Summary & Quality
- `builds` → 빌드 (IDP 스타일 통합)
- `basic-info` → 기본 정보

AppLayout 메뉴에서 기대하는 탭:
- `summary` → Summary & Quality
- `info` → 기본 정보 (→ `basic-info`로 매핑됨)
- `devops` → DevOps 설정 (탭 없음, 빌드 탭에 통합됨)
- `builds` → 빌드 목록 (빌드 탭과 같음)
- `build-new` → 새 빌드 (탭 없음, 빌드 탭에 통합됨)

---

## 2. 목표 상태 (TO-BE)

### 2.1 설계 원칙

1. **Context-Driven Navigation**: 빌드는 항상 프로젝트 내에서 관리
2. **Single Source of Truth**: 동일 기능은 한 곳에서만 제공
3. **IDP 패턴 적용**: Single Pane of Glass (설정 + 목록 + 상세가 한 화면)
4. **역할 기반 메뉴**: 개발자/관리자 모두 효율적으로 사용 가능

### 2.2 새로운 메뉴 계층도

```
MintPortal
├─ 과제 관리
│   └─ 과제 현황 ────────────→ / (ProjectListView)
│       └─ [프로젝트 선택 시]
│           ├─ Summary & Quality → /projects/:id?tab=summary
│           ├─ 빌드 ─────────────→ /projects/:id?tab=builds
│           │   ├─ Layer 선택 (Release/HIL/FIL/FTL)
│           │   ├─ 빌드 설정 (Config)
│           │   ├─ 빌드 목록 (List)
│           │   └─ 빌드 실행 (Trigger)
│           └─ 기본 정보 ────────→ /projects/:id?tab=info
│
├─ 대시보드 ──────────────────────→ /dashboard (HomeView)
│   ├─ 전체 빌드 통계
│   ├─ 최근 빌드
│   └─ 활성 프로젝트
│
├─ 게시판
│   ├─ 공지사항 ─────────────────→ /board?tab=notice
│   ├─ VOC ─────────────────────→ /board?tab=voc
│   └─ Release Note ────────────→ /board?tab=release-note
│
└─ 설정 ─────────────────────────→ /settings
```

### 2.3 삭제할 항목

| 항목 | 사유 |
|------|------|
| `/build` 라우트 | ProjectDetailView 빌드 탭으로 대체 |
| `/build/new` 라우트 | ProjectDetailView 빌드 탭으로 대체 |
| `/build/:id` 라우트 | ProjectDetailView 빌드 탭으로 대체 |
| `BuildListView.vue` | 삭제 또는 dashboard 용도로 축소 |
| `BuildNewView.vue` | 삭제 |
| `BuildDetailView.vue` | 삭제 |
| 메뉴: "새 빌드" | 빌드 탭 내 버튼으로 대체 |
| 메뉴: "DevOps 설정" | 빌드 탭 내 Config 패널로 통합 |

---

## 3. 각 페이지 역할 정의

### 3.1 ProjectListView (과제 현황) - `/`

**역할:** 전사 프로젝트 현황 대시보드 (메인 화면)

**표시할 정보:**
- 프로젝트 목록 테이블
- P/J, OEM, Feature, TL, Milestone, KPI, Result, Date, FW Name
- 검색 및 필터링

**수행할 액션:**
- 프로젝트 상세 이동
- 필터링 (P/J, OEM, Feature)
- 검색

### 3.2 ProjectDetailView (과제 상세) - `/projects/:id`

**역할:** 프로젝트의 모든 정보 및 빌드 관리 통합

#### Tab 1: Summary & Quality (`?tab=summary`)
**표시할 정보:**
- Layer별 최신 빌드 요약
- 최근 빌드 결과 (3개)
- Quality Grid (Coverity, SAM, DoBEE, BlackDuck 등)

**수행할 액션:**
- Layer 선택
- 빌드 탭으로 이동

#### Tab 2: 빌드 (`?tab=builds`) - **핵심 기능**
**표시할 정보:**
- Layer 선택 (Release/HIL/FIL/FTL)
- 빌드 설정 (Config): SCM, Build Env, Options, Pipeline
- 빌드 목록 (Split View 좌측)
- 빌드 상세 (Split View 우측): Pipeline 시각화, Quality Metrics
- Release Layer: TR 상태 및 버튼

**수행할 액션:**
- Layer 전환
- 빌드 실행 (Trigger)
- 빌드 상세 조회
- Binary/Log 다운로드
- TR (Test Request) 처리

#### Tab 3: 기본 정보 (`?tab=info`)
**표시할 정보:**
- Product 정보: OEM, Feature, Target, Task Code
- Members 정보: PL, TL, 팀원
- PLM 정보: PLM ID, 링크
- Milestones: MP, ES, CS, QS
- 변경 이력

**수행할 액션:**
- 정보 수정 (Edit 모드)

### 3.3 HomeView (대시보드) - `/dashboard`

**역할:** 전체 시스템 현황 모니터링

**표시할 정보:**
- Success Rate, Total Builds, Active Projects, Running Builds
- 최근 빌드 목록 (전체)
- 활성 프로젝트 목록

**수행할 액션:**
- 빌드/프로젝트 상세 이동 (링크 클릭)

### 3.4 BoardView (게시판) - `/board`

**역할:** 공지사항, VOC, Release Note 관리

**Tab별 역할:**
- 공지사항: 시스템/조직 공지
- VOC: 요청/피드백 관리 (Status, Priority 포함)
- Release Note: 릴리즈 기록

### 3.5 SettingsView (설정) - `/settings`

**역할:** 사용자 설정 관리

**표시할 정보:**
- Profile, Notifications, Security, Display

---

## 4. 네비게이션 UX 설계

### 4.1 사이드바 메뉴 구조

```vue
<a-menu>
  <!-- 과제 관리 -->
  <a-sub-menu key="projects-menu">
    <template #icon><ProjectOutlined /></template>
    <template #title>과제 관리</template>

    <a-menu-item key="projects-list">
      <UnorderedListOutlined />
      <span>과제 현황</span>
    </a-menu-item>

    <!-- 프로젝트 상세 페이지에서만 표시 -->
    <template v-if="isProjectDetailPage">
      <a-menu-divider />
      <a-menu-item key="project-summary">
        <DashboardOutlined />
        <span>Summary & Quality</span>
      </a-menu-item>
      <a-menu-item key="project-builds">
        <BuildOutlined />
        <span>빌드</span>
      </a-menu-item>
      <a-menu-item key="project-info">
        <TeamOutlined />
        <span>기본 정보</span>
      </a-menu-item>
    </template>
  </a-sub-menu>

  <!-- 대시보드 -->
  <a-menu-item key="dashboard">
    <HomeOutlined />
    <span>대시보드</span>
  </a-menu-item>

  <!-- 게시판 -->
  <a-sub-menu key="board-menu">
    <template #icon><MessageOutlined /></template>
    <template #title>게시판</template>
    <a-menu-item key="board-notice">공지사항</a-menu-item>
    <a-menu-item key="board-voc">VOC</a-menu-item>
    <a-menu-item key="board-release-note">Release Note</a-menu-item>
  </a-sub-menu>

  <!-- 설정 -->
  <a-menu-item key="settings">
    <SettingOutlined />
    <span>설정</span>
  </a-menu-item>
</a-menu>
```

### 4.2 메뉴-탭 매핑

| 메뉴 Key | 라우트 | 탭 |
|----------|--------|-----|
| `projects-list` | `/` | - |
| `project-summary` | `/projects/:id?tab=summary` | summary |
| `project-builds` | `/projects/:id?tab=builds` | builds |
| `project-info` | `/projects/:id?tab=info` | basic-info |
| `dashboard` | `/dashboard` | - |
| `board-notice` | `/board?tab=notice` | notice |
| `board-voc` | `/board?tab=voc` | voc |
| `board-release-note` | `/board?tab=release-note` | release-note |
| `settings` | `/settings` | - |

---

## 5. 라우터 수정 계획

### 5.1 제거할 라우트

```typescript
// 삭제
{ path: 'build', name: 'build-list', ... }
{ path: 'build/new', name: 'build-new', ... }
{ path: 'build/:id', name: 'build-detail', ... }
```

### 5.2 최종 라우트 구조

```typescript
const routes: RouteRecordRaw[] = [
  { path: '/login', name: 'login', component: LoginView },
  {
    path: '/',
    component: AppLayout,
    children: [
      { path: '', name: 'projects', component: ProjectListView },
      { path: 'projects/:projectId', name: 'project-detail', component: ProjectDetailView },
      { path: 'dashboard', name: 'dashboard', component: HomeView },
      { path: 'board', name: 'board', component: BoardView },
      { path: 'board/notice/:id', name: 'notice-detail', component: BoardView },
      { path: 'board/voc/:id', name: 'voc-detail', component: BoardView },
      { path: 'board/release-note/:id', name: 'release-note-detail', component: BoardView },
      { path: 'settings', name: 'settings', component: SettingsView },
    ],
  },
]
```

---

## 6. 파일 정리 계획

### 6.1 삭제할 파일

| 파일 | 이유 |
|------|------|
| `src/views/BuildListView.vue` | ProjectDetailView로 통합됨 |
| `src/views/BuildNewView.vue` | ProjectDetailView로 통합됨 |
| `src/views/BuildDetailView.vue` | ProjectDetailView로 통합됨 |

### 6.2 수정할 파일

| 파일 | 수정 내용 |
|------|-----------|
| `src/router/index.ts` | build 관련 라우트 제거 |
| `src/layouts/AppLayout.vue` | 메뉴 구조 단순화 |
| `src/views/ProjectDetailView.vue` | 탭 키 통일 (info → basic-info) |
| `src/views/HomeView.vue` | 빌드 목록 링크 수정 |
| `src/components/dashboard/RecentBuildsList.vue` | 빌드 링크 수정 |

---

## 7. 구현 순서

1. **라우터 정리** - build 관련 라우트 제거
2. **AppLayout 메뉴 수정** - 단순화된 메뉴 적용
3. **ProjectDetailView 탭 매핑 수정** - query param 통일
4. **대시보드 링크 수정** - 빌드 링크가 프로젝트 상세로 이동하도록
5. **불필요한 View 파일 삭제**
6. **빌드 검증**

---

## 8. 검증 체크리스트

- [ ] 모든 메뉴 항목이 올바른 페이지로 이동
- [ ] 프로젝트 상세 탭 전환이 URL과 동기화
- [ ] 대시보드에서 빌드 클릭 시 프로젝트 상세 빌드 탭으로 이동
- [ ] 게시판 탭 전환이 정상 동작
- [ ] 빌드 기능(설정, 목록, 실행, 상세)이 프로젝트 상세에서 정상 동작
- [ ] 브라우저 뒤로가기/앞으로가기 정상 동작
