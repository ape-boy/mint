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
npm run test:e2e  # E2E 테스트 (bash tests/e2e/*.sh)
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
├── components/    # Reusable components by domain (common/, project/, build/, dashboard/)
├── composables/   # Vue composables (useStatus, useFormat)
├── stores/        # Pinia stores (projectGroup, project, layer, build)
├── types/         # TypeScript interfaces
├── views/         # Page components
├── layouts/       # AppLayout.vue
└── router/        # Route definitions
mock-server/
└── db.json        # Mock data (json-server)
```

### Key Patterns

- **API Layer**: `src/api/client.ts` creates Axios instance with interceptors. Each entity has its own API module.
- **Stores**: Pinia stores use Composition API style (`defineStore` with setup function)
- **Components**: Organized by domain. Each folder has `index.ts` for barrel exports.
- **Types**: All domain types defined in `src/types/index.ts`

### Routes

| Route | View |
|-------|------|
| `/` | ProjectListView (메인) |
| `/projects/:projectId` | ProjectDetailView |
| `/dashboard` | HomeView |
| `/build`, `/build/:id` | BuildListView, BuildDetailView |
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

Build, SAM, Coverity, DoBEE, TASTY, Warning Count, BlackDuck, TEST1, TEST2

### TR Flow (Release Layer Only)

```
All Pipeline Stages Pass? → YES → TR Available
                         → NO  → Approval Required → TR 가능
```

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
| Project | 1000-1999 |
| Layer | 2000-2999 |
| Build | 3000-3999 |
| User | 100-199 |

### API Endpoints (json-server)

Base: `http://localhost:3001/api/`

- `project-groups`, `projects` (?groupId=), `layers` (?projectId=), `builds` (?projectId=, ?layerId=), `stats`

---

## Design System

- **Theme**: Dark Theme (Deep Blue/Purple gradient)
- **UI Pattern**: Glassmorphism
- **CSS Variables**: `src/styles/variables.css`
