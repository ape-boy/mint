# MintPortal E2E Tests

## Overview

이 디렉토리는 MintPortal의 E2E(End-to-End) 테스트를 포함합니다.
모든 테스트는 `mock-server/db.json`의 Mock 데이터를 기반으로 실행됩니다.

## Test Files

| File | Description |
|------|-------------|
| `api.test.sh` | API 엔드포인트 검증 테스트 |
| `scenarios.test.sh` | 사용자 시나리오 기반 통합 테스트 |

## Prerequisites

1. Mock 서버가 실행 중이어야 합니다:
```bash
npm run mock
```

2. (선택) 개발 서버 실행 (UI 테스트 시):
```bash
npm run dev
```

## Running Tests

### API Tests
```bash
# Mock 서버 실행 후
chmod +x tests/e2e/api.test.sh
./tests/e2e/api.test.sh
```

### Scenario Tests
```bash
chmod +x tests/e2e/scenarios.test.sh
./tests/e2e/scenarios.test.sh
```

### With Custom Base URL
```bash
API_URL=http://localhost:5173/api ./tests/e2e/api.test.sh
```

## Test Coverage

### API Endpoints
- [x] GET /stats
- [x] GET /project-groups
- [x] GET /project-groups/:id
- [x] GET /projects
- [x] GET /projects/:id
- [x] GET /projects?groupId=
- [x] GET /layers
- [x] GET /layers/:id
- [x] GET /layers?projectId=
- [x] GET /builds
- [x] GET /builds/:id
- [x] GET /builds?projectId=
- [x] GET /builds?layerId=

### User Scenarios
- [x] Dashboard View: 통계 및 최근 빌드 조회
- [x] Project List View: 프로젝트 목록 및 필터링
- [x] Project Detail View: 프로젝트 상세, 레이어, 빌드 조회
- [x] Build Detail View: 빌드 상세 및 파이프라인 스테이지
- [x] Layer Configuration: 파이프라인 설정 조회
- [x] New Build Form: 빌드 생성 폼 데이터 로드

## Mock Data Convention

테스트는 다음 Mock 데이터 ID 규칙을 따릅니다:

| Entity | ID Range | Example |
|--------|----------|---------|
| Project Group | 1-99 | "1", "2" |
| Project | 1000-1999 | "1001", "1002" |
| Layer | 2000-2999 | "2001", "2002" |
| Build | 3000-3999 | "3001", "3002" |

## Exit Codes

- `0`: 모든 테스트 통과
- `1`: 하나 이상의 테스트 실패

## Notes

- 테스트는 Mock 데이터만 사용하며 실제 백엔드에 영향을 주지 않습니다
- 모든 테스트는 idempotent하게 설계되어 여러 번 실행해도 동일한 결과를 보장합니다
- 실제 코드에 하드코딩된 Mock 데이터를 사용하지 않습니다
