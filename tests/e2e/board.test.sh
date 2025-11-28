#!/bin/bash
# MintPortal E2E Board API Tests
# Usage: ./tests/e2e/board.test.sh
# Prerequisites: Mock server must be running on port 3001

# set -e  # 전체 테스트 결과를 보기 위해 비활성화

BASE_URL="${API_URL:-http://localhost:3001}"
PASSED=0
FAILED=0

# Colors
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

echo "======================================"
echo "MintPortal E2E Board API Tests"
echo "Base URL: $BASE_URL"
echo "======================================"
echo ""

# Test function
test_endpoint() {
    local name="$1"
    local endpoint="$2"
    local expected_field="$3"

    echo -n "Testing $name... "

    response=$(curl -s "$BASE_URL$endpoint")

    if echo "$response" | grep -q "$expected_field"; then
        echo -e "${GREEN}PASS${NC}"
        ((PASSED++))
    else
        echo -e "${RED}FAIL${NC}"
        echo "  Expected field: $expected_field"
        echo "  Response: ${response:0:200}..."
        ((FAILED++))
    fi
}

# Test array endpoint
test_array_endpoint() {
    local name="$1"
    local endpoint="$2"
    local min_count="$3"

    echo -n "Testing $name... "

    response=$(curl -s "$BASE_URL$endpoint")
    count=$(echo "$response" | grep -o '"id"' | wc -l)

    if [ "$count" -ge "$min_count" ]; then
        echo -e "${GREEN}PASS${NC} (found $count items)"
        ((PASSED++))
    else
        echo -e "${RED}FAIL${NC} (expected >= $min_count, got $count)"
        ((FAILED++))
    fi
}

echo -e "${BLUE}=== Notice Board Tests ===${NC}"
echo ""

echo "--- Notice Endpoints ---"
test_array_endpoint "GET /notices" "/notices" 3
test_endpoint "GET /notices/:id" "/notices/4001" "MintPortal"
test_endpoint "GET /notices - has title" "/notices/4001" "title"
test_endpoint "GET /notices - has content" "/notices/4001" "content"
test_endpoint "GET /notices - has category" "/notices/4001" "category"
test_endpoint "GET /notices - has priority" "/notices/4001" "priority"
test_endpoint "GET /notices - has isPinned" "/notices/4001" "isPinned"
test_endpoint "GET /notices - has author" "/notices/4001" "author"
test_endpoint "GET /notices - has viewCount" "/notices/4001" "viewCount"
test_endpoint "GET /notices - maintenance category" "/notices/4001" "maintenance"

echo ""
echo "--- Notice Query Parameters ---"
test_endpoint "GET /notices?category=maintenance" "/notices?category=maintenance" "점검"
test_endpoint "GET /notices?isPinned=true" "/notices?isPinned=true" "isPinned"

echo ""
echo -e "${BLUE}=== Release Notes Tests ===${NC}"
echo ""

echo "--- Release Notes Endpoints ---"
test_array_endpoint "GET /release-notes" "/release-notes" 3
test_endpoint "GET /release-notes/:id" "/release-notes/5001" "v2.3.1"
test_endpoint "GET /release-notes - has title" "/release-notes/5001" "title"
test_endpoint "GET /release-notes - has content" "/release-notes/5001" "content"
test_endpoint "GET /release-notes - has version" "/release-notes/5001" "version"
test_endpoint "GET /release-notes - has changeType" "/release-notes/5001" "changeType"
test_endpoint "GET /release-notes - has author" "/release-notes/5001" "author"
test_endpoint "GET /release-notes - has releaseDate" "/release-notes/5001" "releaseDate"
test_endpoint "GET /release-notes - has projectId" "/release-notes/5001" "projectId"
test_endpoint "GET /release-notes - has buildId" "/release-notes/5001" "buildId"

echo ""
echo "--- Release Notes Query Parameters ---"
test_endpoint "GET /release-notes?projectId=1001" "/release-notes?projectId=1001" "BM1743"

echo ""
echo -e "${BLUE}=== VOC Board Tests ===${NC}"
echo ""

echo "--- VOC Endpoints ---"
test_array_endpoint "GET /vocs" "/vocs" 3
test_endpoint "GET /vocs/:id" "/vocs/6001" "빌드"
test_endpoint "GET /vocs - has title" "/vocs/6001" "title"
test_endpoint "GET /vocs - has content" "/vocs/6001" "content"
test_endpoint "GET /vocs - has category" "/vocs/6001" "category"
test_endpoint "GET /vocs - has priority" "/vocs/6001" "priority"
test_endpoint "GET /vocs - has status" "/vocs/6001" "status"
test_endpoint "GET /vocs - has author" "/vocs/6001" "author"
test_endpoint "GET /vocs - has comments" "/vocs/6001" "comments"

echo ""
echo "--- VOC Project Association ---"
test_endpoint "GET /vocs - has projectId" "/vocs/6001" "projectId"
test_endpoint "GET /vocs - has assignee" "/vocs/6001" "assignee"

echo ""
echo "--- VOC Resolution ---"
test_endpoint "GET /vocs - resolved VOC" "/vocs/6003" "resolved"
test_endpoint "GET /vocs - has resolution" "/vocs/6003" "resolution"

echo ""
echo "--- VOC Query Parameters ---"
test_endpoint "GET /vocs?status=open" "/vocs?status=open" "open"
test_endpoint "GET /vocs?status=in_progress" "/vocs?status=in_progress" "in_progress"
test_endpoint "GET /vocs?status=resolved" "/vocs?status=resolved" "resolved"
test_endpoint "GET /vocs?category=bug" "/vocs?category=bug" "bug"
test_endpoint "GET /vocs?category=feature" "/vocs?category=feature" "feature"
test_endpoint "GET /vocs?priority=critical" "/vocs?priority=critical" "critical"
test_endpoint "GET /vocs?priority=high" "/vocs?priority=high" "high"

echo ""
echo -e "${BLUE}=== Users Endpoint Tests ===${NC}"
echo ""

test_array_endpoint "GET /users" "/users" 10
test_endpoint "GET /users/:id" "/users/101" "김철수"
test_endpoint "GET /users - has name" "/users/101" "name"
test_endpoint "GET /users - has email" "/users/101" "email"
test_endpoint "GET /users - has role" "/users/101" "role"

echo ""
echo "======================================"
echo -e "Results: ${GREEN}$PASSED passed${NC}, ${RED}$FAILED failed${NC}"
echo "======================================"

if [ "$FAILED" -gt 0 ]; then
    exit 1
fi
