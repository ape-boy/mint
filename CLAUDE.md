# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

MintPortal is a Vue 3 + TypeScript web application for CI/CD pipeline management for an **SSD Firmware Development Organization**.

**Domain Context:** SSD FW 개발 조직의 CI/CD 빌드 및 릴리즈 관리 시스템

---

## Commands

```bash
npm run dev       # 개발 서버 실행 (HMR)
npm run build     # 프로덕션 빌드 (vue-tsc + vite build)
npm run mock      # Mock 서버 실행 (port 3001)
npm run dev:all   # Mock 서버 + 개발 서버 동시 실행
npm run test:e2e  # E2E 테스트 (mock 서버 필요)
```

E2E 테스트 개별 실행:
```bash
bash tests/e2e/api.test.sh       # API 엔드포인트 테스트
bash tests/e2e/scenarios.test.sh # 시나리오 테스트
bash tests/e2e/board.test.sh     # 게시판 API 테스트
```

---

## Tech Stack

- **Vue 3** with Composition API (`<script setup>`)
- **TypeScript 5.9** (strict mode)
- **Vite 7** / **Pinia 3** / **Vue Router 4**
- **Ant Design Vue 4.2** (Dark Theme)
- **Axios** + **json-server** (Mock API)

---

## Architecture

```
src/
├── api/           # API modules (client.ts = Axios instance)
├── components/    # Reusable components by domain (common/, project/, build/, board/, dashboard/)
├── composables/   # Vue composables (useStatus, useFormat)
├── stores/        # Pinia stores (projectGroup, project, layer, build, board)
├── types/         # TypeScript interfaces (index.ts에 모든 타입 정의)
├── views/         # Page components
├── layouts/       # AppLayout.vue
└── router/        # Route definitions
mock-server/
└── db.json        # Mock data (json-server)
tests/e2e/
└── *.test.sh      # E2E 테스트 스크립트
```

### Key Patterns

- **Path Alias**: `@/` → `src/` (vite.config.ts에서 설정)
- **API Proxy**: `/api` → `http://localhost:3001` (개발 서버에서 프록시 처리)
- **API Layer**: `src/api/client.ts`에서 Axios 인스턴스 생성. 각 엔티티별 API 모듈 분리
- **Stores**: Pinia Composition API 스타일 (`defineStore` with setup function)
- **Components**: 도메인별 폴더 구조. 각 폴더에 `index.ts` barrel export
- **Types**: `src/types/index.ts`에 모든 도메인 타입 정의

### Routes

| Route | View |
|-------|------|
| `/` | ProjectListView (메인) |
| `/projects/:projectId` | ProjectDetailView |
| `/dashboard` | HomeView |
| `/build`, `/build/:id` | BuildListView, BuildDetailView |
| `/board` | BoardView (공지사항, 릴리즈노트, VOC) |
| `/login`, `/settings` | LoginView, SettingsView |

---

## Domain Model (도메인 모델)

### Hierarchy (계층 구조)

```
Project Group (제품)      → BM1743, PM9E1, PM9A3
  └── Project (세부 구성)  → BM1743_LENOVO_1TB, BM1743_DELL_2TB
      └── Layer (빌드 계층) → Release, HIL, FIL, FTL
          └── Build (빌드)  → 파이프라인 실행 단위
```

### Key Domain Concepts

| 개념 | 정의 |
|------|------|
| **Project Group** | 제품 라인 (SSD 모델) |
| **Project** | 제품 세부 구성 (OEM + 스펙) |
| **Layer** | 빌드 계층 - `release` 또는 `custom` type |
| **Build** | 파이프라인 실행 인스턴스 |
| **TR (Test Request)** | Release Layer 전용 릴리즈 요청 |
| **Milestone** | MP, ES, CS, QS (양산/샘플 마일스톤) |

### Pipeline Stages (파이프라인 스테이지)

Build, SAM, Coverity, DoBEE, TASTY, Warning Count, BlackDuck, OnBoard Test, Coding Rule Check

### TR Flow (Release Layer Only)

```
All Pipeline Stages Pass? → YES → TR Available
                         → NO  → Approval Required → TR 가능
```

### Board System (게시판)

| Type | Description |
|------|-------------|
| **Notice** | 공지사항 - 시스템/일반/유지보수/이벤트 카테고리 |
| **Release Note** | 릴리즈 노트 - 버전별 변경사항 문서화 |
| **VOC** | Voice of Customer - 버그/기능요청/개선/문의 접수 및 처리 |

---

## Mock-First Development (필수 규칙)

**모든 개발은 `mock-server/db.json` 기반으로 진행**

### 핵심 원칙

1. 모든 API endpoint는 `mock-server/db.json`에 100% mocking
2. 컴포넌트/서비스 코드에 하드코딩된 mock 데이터 **절대 금지**
3. 새 기능: mock 데이터 → API → UI 순서로 구현

### Anti-patterns (금지)

```typescript
// ❌ 컴포넌트 내 하드코딩
const mockProjects = [{ id: 1, name: 'Test' }]

// ❌ API 실패 시 fallback
const projects = await fetch().catch(() => [{ id: 1 }])

// ❌ DEV 환경 임시 데이터
if (import.meta.env.DEV) return fakeData
```

### Mock Data ID Convention

| Entity | ID Range |
|--------|----------|
| Project Group | 1-99 |
| User | 100-199 |
| Project | 1000-1999 |
| Layer | 2000-2999 |
| Build | 3000-3999 |
| Notice | 4000-4999 |
| Release Note | 5000-5999 |
| VOC | 6000-6999 |

### API Endpoints (json-server)

Base: `http://localhost:3001/` (Vite proxy: `/api` → `http://localhost:3001`)

**Core:**
- `project-groups`, `projects` (?groupId=), `layers` (?projectId=), `builds` (?projectId=, ?layerId=), `stats`

**Board:**
- `notices`, `release-notes`, `vocs`

---

## Design System

- **Theme**: Dark Theme (Deep Blue/Purple gradient)
- **UI Pattern**: Glassmorphism
- **CSS Variables**: `src/styles/variables.css`
