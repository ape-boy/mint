---
description: "Agentic Dev Workflow 실행 - 요구사항 분석 → PRD → TDD → 구현 → 리뷰 → E2E → 루프백"
---

# Agentic Dev Workflow 실행

$ARGUMENTS 에 대해 완전한 개발 워크플로우를 시작합니다.

## 워크플로우 단계

1. **Phase 1: 요구사항 분석** (자동)
   - Socratic 5W1H 질문으로 요구사항 구체화
   - Scope 정의 (In/Out, Must/Nice-to-have)

2. **Phase 2: PRD 작성** [승인 필요]
   - User Stories, Acceptance Criteria
   - Technical Spec, Mock Data Plan

3. **Phase 3: TDD/Mock** (자동)
   - mock-server/db.json에 Mock 데이터 추가
   - 실패하는 테스트 작성 → 구현 → 리팩토링

4. **Phase 4: 코드 리뷰** [승인 필요]
   - Quality, Security, Performance, Architecture 검토
   - 자동 수정 가능 항목 제안

5. **Phase 5: E2E 테스트** (자동)
   - 통합 테스트 실행

6. **Phase 6: 루프백** (실패 시)
   - 근본 원인 분석 → 수정 → 재테스트

## 실행

지금부터 "$ARGUMENTS" 기능 개발을 위한 Agentic Dev Workflow를 시작합니다.

---

### Phase 1 시작: 요구사항 분석

"$ARGUMENTS"에 대해 다음을 파악하겠습니다:

**WHO** - 이 기능의 사용자는?
**WHAT** - 해결하려는 문제는?
**WHY** - 왜 필요한가?
**HOW** - 어떻게 사용하며 성공 기준은?
