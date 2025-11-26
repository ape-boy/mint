#!/bin/bash
# MintPortal E2E API Tests
# Usage: ./tests/e2e/api.test.sh
# Prerequisites: Mock server must be running on port 3001

set -e

BASE_URL="${API_URL:-http://localhost:3001}"
PASSED=0
FAILED=0

# Colors
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

echo "======================================"
echo "MintPortal E2E API Tests"
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
        echo "  Response: $response"
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

echo "--- Stats Endpoint ---"
test_endpoint "GET /stats" "/stats" "successRate"
test_endpoint "GET /stats - totalBuilds" "/stats" "totalBuilds"
test_endpoint "GET /stats - activeProjects" "/stats" "activeProjects"
test_endpoint "GET /stats - runningBuilds" "/stats" "runningBuilds"

echo ""
echo "--- Project Groups Endpoint ---"
test_array_endpoint "GET /project-groups" "/project-groups" 1
test_endpoint "GET /project-groups/:id" "/project-groups/1" "BM1743"

echo ""
echo "--- Projects Endpoint ---"
test_array_endpoint "GET /projects" "/projects" 1
test_endpoint "GET /projects/:id" "/projects/1001" "BM1743_LENOVO"
test_endpoint "GET /projects - has TL info" "/projects/1001" "tl"
test_endpoint "GET /projects - has members" "/projects/1001" "members"
test_endpoint "GET /projects - has milestones" "/projects/1001" "milestones"
test_endpoint "GET /projects - has config" "/projects/1001" "config"

echo ""
echo "--- Layers Endpoint ---"
test_array_endpoint "GET /layers" "/layers" 1
test_endpoint "GET /layers/:id" "/layers/2001" "Release"
test_endpoint "GET /layers - has pipelineConfig" "/layers/2001" "pipelineConfig"
test_endpoint "GET /layers - has type" "/layers/2001" "release"

echo ""
echo "--- Builds Endpoint ---"
test_array_endpoint "GET /builds" "/builds" 1
test_endpoint "GET /builds/:id" "/builds/3001" "buildNumber"
test_endpoint "GET /builds - has stages" "/builds/3001" "stages"
test_endpoint "GET /builds - has artifacts" "/builds/3001" "artifacts"
test_endpoint "GET /builds - has branch" "/builds/3001" "branch"
test_endpoint "GET /builds - has commitHash" "/builds/3001" "commitHash"
test_endpoint "GET /builds - has triggeredBy" "/builds/3001" "triggeredBy"

echo ""
echo "--- Query Parameters ---"
test_endpoint "GET /projects?groupId=1" "/projects?groupId=1" "BM1743"
test_endpoint "GET /layers?projectId=1001" "/layers?projectId=1001" "2001"
test_endpoint "GET /builds?projectId=1001" "/builds?projectId=1001" "3001"

echo ""
echo "======================================"
echo "Results: ${GREEN}$PASSED passed${NC}, ${RED}$FAILED failed${NC}"
echo "======================================"

if [ "$FAILED" -gt 0 ]; then
    exit 1
fi
