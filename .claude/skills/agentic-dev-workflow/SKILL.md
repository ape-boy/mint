---
name: agentic-dev-workflow
description: |
  Complete Agentic Development Workflow - 전체 소프트웨어 개발 라이프사이클을 자동으로 오케스트레이션합니다.
  [자동 활성화 조건]
  - 새 기능 개발: "기능 추가", "구현해줘", "만들어줘", "개발해줘", "implement", "build", "create feature"
  - PRD/설계: "PRD", "명세서", "설계", "specification", "design doc"
  - 테스트: "TDD", "테스트 작성", "unit test", "E2E test", "mock"
  - 리뷰: "코드 리뷰", "review", "검토"
  - 문제해결: "에러", "버그", "안돼", "실패", "error", "bug", "failed"
  [수행 워크플로우]
  Phase 1: 요구사항 분석 (Socratic Questioning)
  Phase 2: PRD 작성 [사용자 승인]
  Phase 3: TDD - Mock 데이터 → 테스트 작성 → 구현
  Phase 4: 코드 리뷰 + 자동 수정
  Phase 5: E2E 테스트
  Phase 6: 루프백 (실패 시 근본 원인 분석 → 수정 → 재테스트)
allowed-tools:
  - Read
  - Write
  - Edit
  - Glob
  - Grep
  - Bash
  - TodoWrite
  - Task
---

# Agentic Dev Workflow

> **완전 자동화된 개발 라이프사이클 오케스트레이터**
> 요구사항 분석부터 E2E 테스트까지 모든 개발 단계를 자동으로 수행합니다.

## 워크플로우 개요

```
┌─────────────────────────────────────────────────────────────────────────────┐
│                     AGENTIC DEVELOPMENT WORKFLOW                            │
├─────────────────────────────────────────────────────────────────────────────┤
│                                                                             │
│  ┌──────────────┐    ┌──────────────┐    ┌──────────────┐                  │
│  │   PHASE 1    │───▶│   PHASE 2    │───▶│   PHASE 3    │                  │
│  │  요구사항 분석  │    │  PRD 작성    │    │  TDD/Mock   │                  │
│  │   (자동)      │    │  [승인 필요]  │    │   (자동)     │                  │
│  └──────────────┘    └──────────────┘    └──────────────┘                  │
│                                                 │                           │
│                                                 ▼                           │
│  ┌──────────────┐    ┌──────────────┐    ┌──────────────┐                  │
│  │   PHASE 6    │◀───│   PHASE 5    │◀───│   PHASE 4    │                  │
│  │   루프백      │    │  E2E 테스트   │    │  코드 리뷰    │                  │
│  │  (실패 시)    │    │   (자동)      │    │  [승인 필요]  │                  │
│  └──────┬───────┘    └──────────────┘    └──────────────┘                  │
│         │                                                                   │
│         │ 실패 시                                                            │
│         └──────────────────▶ Phase 3으로 돌아가 수정                         │
│                                                                             │
│  ✅ 모든 테스트 통과 시 → COMPLETE                                           │
│                                                                             │
└─────────────────────────────────────────────────────────────────────────────┘
```

---

## PHASE 1: 요구사항 분석 (자동)

### 트리거
- 새 기능 요청 시 자동 실행
- 요구사항이 모호할 때

### 수행 작업

#### 1.1 Socratic Questioning (5W1H)
```markdown
Claude가 자동으로 다음을 파악:

WHO   - 이 기능의 사용자는 누구인가?
WHAT  - 해결하려는 핵심 문제는 무엇인가?
WHY   - 왜 이 기능이 필요한가?
WHEN  - 언제, 얼마나 자주 사용되는가?
WHERE - 어떤 컨텍스트에서 사용되는가?
HOW   - 어떻게 사용할 것인가? 성공 기준은?
```

#### 1.2 Scope Definition
```markdown
✅ In-Scope (포함): 이번에 구현할 것
❌ Out-of-Scope (제외): 이번에 하지 않을 것
⭐ Must-Have (필수): 핵심 기능
💡 Nice-to-Have (선택): 추가 기능
```

#### 1.3 MintPortal 도메인 체크
```markdown
- 관련 계층: Project Group / Project / Layer / Build
- 파이프라인 연관: Build, SAM, Coverity, DoBEE, TASTY, etc.
- TR/릴리즈 영향: Yes/No
```

### 출력
- 요구사항 요약 (콘솔 출력)
- `docs/requirements/[feature].md` (선택적)

---

## PHASE 2: PRD 작성 [🔔 사용자 승인 필요]

### 트리거
- Phase 1 완료 후 자동 진행
- "PRD", "명세서" 요청 시

### PRD 구조

```markdown
# [Feature Name] PRD

## 1. 개요
- 문제 정의
- 솔루션 요약
- 성공 지표

## 2. User Stories
As a [사용자], I want to [액션] so that [가치]

## 3. Acceptance Criteria (Given-When-Then)
Scenario: [시나리오명]
  Given [초기 조건]
  When [사용자 액션]
  Then [예상 결과]

## 4. Technical Spec
- Data Model (TypeScript interface)
- API Contract (endpoint, request, response)
- Component Structure

## 5. Mock Data Plan
- db.json 추가 항목
- ID Convention 준수 확인

## 6. Test Plan
- Unit Test 시나리오
- E2E Test 시나리오
```

### 승인 프로세스
```
📋 PRD가 생성되었습니다.

[PRD 내용 표시]

✅ 승인하고 구현 진행
❌ 수정 요청
```

### 출력
- `docs/prd/[feature]-prd.md`

---

## PHASE 3: TDD/Mock 개발 (자동)

### 트리거
- PRD 승인 후 자동 진행
- "TDD", "테스트 먼저" 요청 시

### TDD 사이클

```
┌─────────────────────────────────────────┐
│            TDD CYCLE                    │
│                                         │
│    🔴 RED → 🟢 GREEN → 🔵 REFACTOR      │
│                                         │
│    1. Mock 데이터 작성 (db.json)         │
│    2. 실패하는 테스트 작성               │
│    3. 테스트 통과하는 최소 코드          │
│    4. 리팩토링 (테스트 유지)             │
│                                         │
└─────────────────────────────────────────┘
```

### 3.1 Mock-First (CLAUDE.md 필수 준수)

```typescript
// ✅ 올바른 방법: mock-server/db.json에 먼저 추가
{
  "features": [
    { "id": 4000, "name": "Test Feature", "status": "active" }
  ]
}

// ❌ 금지: 컴포넌트 내 하드코딩
const mockData = [{ id: 1, name: 'Test' }]  // NEVER!

// ❌ 금지: API 실패 시 fallback
.catch(() => [{ id: 1 }])  // NEVER!
```

**ID Convention:**
| Entity | ID Range |
|--------|----------|
| Project Group | 1-99 |
| Project | 1000-1999 |
| Layer | 2000-2999 |
| Build | 3000-3999 |
| User | 100-199 |

### 3.2 테스트 작성

```typescript
// tests/unit/[feature].spec.ts
import { describe, it, expect, beforeEach, vi } from 'vitest'

describe('[Feature]', () => {
  // Happy Path
  it('should [expected behavior] when [condition]', () => {
    // Arrange
    // Act
    // Assert
  })

  // Edge Cases
  it('should handle empty state', () => {})
  it('should handle error state', () => {})

  // Error Cases
  it('should show error when API fails', () => {})
})
```

### 3.3 구현

```typescript
// src/api/[feature].ts - API 모듈
// src/stores/[feature].ts - Pinia Store
// src/components/[feature]/ - Vue 컴포넌트
```

### 출력
- `mock-server/db.json` 업데이트
- `src/api/[feature].ts`
- `src/stores/[feature].ts`
- `src/components/[feature]/`
- `tests/unit/[feature].spec.ts`

---

## PHASE 4: 코드 리뷰 [🔔 사용자 승인 필요]

### 트리거
- Phase 3 완료 후 자동 진행
- "리뷰", "review" 요청 시

### 리뷰 영역 (4대 검토)

```
┌─────────────────────────────────────────┐
│           CODE REVIEW                   │
├─────────────────────────────────────────┤
│  🔍 QUALITY     │  🔒 SECURITY          │
│  - 가독성        │  - XSS 방지           │
│  - 유지보수성    │  - 인증/인가          │
│  - 타입 안전성   │  - 입력 검증          │
├─────────────────────────────────────────┤
│  ⚡ PERFORMANCE │  🏗️ ARCHITECTURE      │
│  - 불필요 렌더링 │  - 컴포넌트 분리      │
│  - API 최적화   │  - 상태 관리          │
│  - 번들 사이즈   │  - 의존성 방향        │
└─────────────────────────────────────────┘
```

### Severity Levels
- 🔴 **CRITICAL**: 즉시 수정 (보안, 데이터 손실)
- 🟠 **HIGH**: 빠른 수정 (버그, 성능)
- 🟡 **MEDIUM**: 개선 권장 (품질)
- 🟢 **LOW**: 선택적 (스타일)

### MintPortal 특화 체크
```markdown
□ Mock-First 원칙 준수
□ ID Convention 준수
□ 도메인 모델 정합성
□ CLAUDE.md 규칙 준수
```

### 승인 프로세스
```
📝 코드 리뷰 완료

[Critical/High 이슈 표시]

🔧 자동 수정 가능 항목:
- [ ] unused imports 제거
- [ ] console.log 제거

✅ 승인하고 테스트 진행
🔧 자동 수정 적용 후 진행
❌ 수동 수정 필요
```

### 출력
- `docs/reviews/[feature]-review.md`
- 자동 수정된 코드 (승인 시)

---

## PHASE 5: E2E 테스트 (자동)

### 트리거
- Phase 4 승인 후 자동 진행
- "E2E", "통합 테스트" 요청 시

### E2E 테스트 전략

```markdown
1. Critical User Flows (핵심 사용자 흐름)
2. Happy Path + Edge Cases
3. Error Handling 검증
```

### 테스트 실행

```bash
# MintPortal E2E 테스트
npm run test:e2e

# 또는 특정 테스트
bash tests/e2e/[feature].test.sh
```

### E2E 테스트 템플릿

```bash
#!/bin/bash
# tests/e2e/[feature].test.sh

echo "🧪 [Feature] E2E Test"

# Test 1: Happy Path
echo "Test 1: Basic functionality..."
# curl or API test commands

# Test 2: Edge Case
echo "Test 2: Edge case handling..."

# Test 3: Error Case
echo "Test 3: Error handling..."

echo "✅ All E2E tests passed"
```

### 출력
- 테스트 결과 리포트 (콘솔)
- `tests/e2e/[feature].test.sh`

---

## PHASE 6: 루프백 (실패 시 자동)

### 트리거
- 테스트 실패 시 자동 활성화
- 코드 리뷰에서 Critical 이슈 발견 시
- "에러", "버그", "안돼" 언급 시

### 루프백 프로세스

```
┌─────────────────────────────────────────────────────────────────┐
│                    LOOPBACK CYCLE                               │
├─────────────────────────────────────────────────────────────────┤
│                                                                 │
│  1. 🔍 DETECT (감지)                                            │
│     - 에러/실패 감지                                             │
│     - 증상 수집                                                  │
│                                                                 │
│  2. 🔬 ANALYZE (분석) ← 핵심!                                    │
│     - 근본 원인 분석 (Root Cause Analysis)                       │
│     - 공식 문서 조사                                             │
│     - 유사 이슈 검색                                             │
│                                                                 │
│  3. 💡 HYPOTHESIZE (가설)                                       │
│     - "원인은 X라고 추정. 근거: Y"                                │
│     - 해결 방안 수립                                             │
│                                                                 │
│  4. 🔧 FIX (수정)                                               │
│     - 가설 기반 수정 적용                                        │
│     - 이전과 다른 접근법 사용                                     │
│                                                                 │
│  5. ✅ VERIFY (검증)                                            │
│     - 테스트 재실행                                              │
│     - 통과 → 완료, 실패 → Step 2로                              │
│                                                                 │
│  6. 📚 DOCUMENT (기록)                                          │
│     - 해결 과정 문서화                                           │
│     - 학습 내용 기록                                             │
│                                                                 │
└─────────────────────────────────────────────────────────────────┘
```

### 절대 금지 (Anti-Patterns)

```markdown
❌ 같은 접근법 반복 시도
   "에러 났네, 다시 해보자" → NEVER!

❌ 원인 분석 없이 수정
   "일단 이렇게 바꿔보자" → NEVER!

❌ Warning 무시
   "경고지만 동작하니까 OK" → NEVER!
```

### 올바른 접근

```markdown
✅ 근본 원인 먼저 파악
   "에러가 발생했다. 공식 문서를 확인해보자"

✅ 가설 수립 후 수정
   "원인은 환경변수 누락. 왜냐하면 에러 메시지에서..."

✅ 다른 방법 시도
   "방법 A가 실패했으니, 방법 B를 시도하자"
```

### 출력
- `docs/loopback/[feature]-[date].md`
- 수정된 코드
- 재실행된 테스트 결과

---

## 워크플로우 상태 머신

```
           ┌───────────────────────────────────────────┐
           │                                           │
           ▼                                           │
┌──────────────────┐                                   │
│  REQUIREMENTS    │                                   │
│   (자동 분석)     │                                   │
└────────┬─────────┘                                   │
         │                                             │
         ▼                                             │
┌──────────────────┐                                   │
│      PRD         │                                   │
│  [사용자 승인]    │──────┐                            │
└────────┬─────────┘      │ 수정 요청                  │
         │ 승인            │                            │
         ▼                │                            │
┌──────────────────┐      │                            │
│    TDD/MOCK      │◀─────┘                            │
│   (자동 구현)     │                                   │
└────────┬─────────┘                                   │
         │                                             │
         ▼                                             │
┌──────────────────┐                                   │
│   CODE REVIEW    │                                   │
│  [사용자 승인]    │──────┐                            │
└────────┬─────────┘      │ 수정 필요                  │
         │ 승인            │                            │
         ▼                │                            │
┌──────────────────┐      │                            │
│   E2E TEST       │◀─────┘                            │
│    (자동)        │                                   │
└────────┬─────────┘                                   │
         │                                             │
         ├─── 실패 ───▶ LOOPBACK ──────────────────────┘
         │
         │ 성공
         ▼
┌──────────────────┐
│    COMPLETE      │
│      ✅          │
└──────────────────┘
```

---

## 사용 예시

### 예시 1: 새 기능 개발
```
User: "프로젝트 목록에 검색 기능을 추가해줘"

Claude (자동 활성화):
📍 Phase 1: 요구사항 분석 시작...
  - 사용자: 개발자/관리자
  - 문제: 프로젝트가 많아 찾기 어려움
  - 해결: 키워드 검색 기능

📋 Phase 2: PRD 작성 중...
  [PRD 내용]

  ✅ 승인하시겠습니까? (y/n)

User: y

🔴 Phase 3: TDD 시작...
  - Mock 데이터 추가 중...
  - 테스트 작성 중...
  - 구현 중...

📝 Phase 4: 코드 리뷰...
  [리뷰 결과]

  ✅ 승인하시겠습니까? (y/n)

User: y

🧪 Phase 5: E2E 테스트 실행 중...
  ✅ 모든 테스트 통과!

🎉 완료! 검색 기능이 성공적으로 구현되었습니다.
```

### 예시 2: 테스트 실패 시 루프백
```
🧪 Phase 5: E2E 테스트 실행 중...
  ❌ Test 2 실패: "검색 결과 없음 처리"

🔄 Phase 6: 루프백 시작...
  1. 🔍 감지: 빈 검색 결과 시 에러
  2. 🔬 분석: Empty state 처리 누락
  3. 💡 가설: v-if 조건 추가 필요
  4. 🔧 수정: EmptyState 컴포넌트 추가
  5. ✅ 검증: 테스트 재실행...
     ✅ 모든 테스트 통과!
  6. 📚 기록: loopback 문서 생성

🎉 완료!
```

---

## MintPortal 프로젝트 통합

### 기술 스택 인식
- Vue 3 + Composition API (`<script setup>`)
- TypeScript 5.9 (strict mode)
- Pinia 3 (상태 관리)
- Ant Design Vue 4.2 (Dark Theme)
- json-server (Mock API)

### 디렉토리 구조 준수
```
src/
├── api/           # API 모듈
├── components/    # 도메인별 컴포넌트
├── composables/   # Vue composables
├── stores/        # Pinia stores
├── types/         # TypeScript 인터페이스
├── views/         # 페이지 컴포넌트
└── router/        # 라우트 정의

mock-server/
└── db.json        # Mock 데이터

docs/
├── requirements/  # 요구사항 문서
├── prd/           # PRD 문서
├── reviews/       # 코드 리뷰 문서
└── loopback/      # 루프백 기록

tests/
├── unit/          # 단위 테스트
└── e2e/           # E2E 테스트
```

---

## 핵심 원칙

### 1. Mock-First Development
모든 데이터는 `mock-server/db.json`에서 시작

### 2. Test-Driven Development
테스트가 구현을 가이드

### 3. Root Cause Analysis
문제 발생 시 근본 원인 먼저 파악

### 4. Continuous Documentation
모든 단계에서 문서화

### 5. Quality Gates
각 단계에서 품질 검증 후 다음 단계 진행

---

## Tips

1. **작은 단위로**: 큰 기능은 작게 나눠서 각각 워크플로우 수행
2. **피드백 빠르게**: 각 단계에서 빠른 검증
3. **문서는 코드와 함께**: 코드 변경 시 문서도 업데이트
4. **루프백은 학습**: 실패는 개선의 기회
