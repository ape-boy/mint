#!/bin/bash
# MintPortal E2E Scenario Tests
# Tests complete user workflows against mock data
# Usage: ./tests/e2e/scenarios.test.sh

set -e

BASE_URL="${API_URL:-http://localhost:3001}"
PASSED=0
FAILED=0

RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
NC='\033[0m'

echo "======================================"
echo "MintPortal E2E Scenario Tests"
echo "======================================"
echo ""

# Helper function
assert_contains() {
    local response="$1"
    local expected="$2"
    local message="$3"

    if echo "$response" | grep -q "$expected"; then
        echo -e "  ${GREEN}✓${NC} $message"
        ((PASSED++))
        return 0
    else
        echo -e "  ${RED}✗${NC} $message"
        ((FAILED++))
        return 1
    fi
}

assert_count() {
    local response="$1"
    local min_count="$2"
    local message="$3"

    count=$(echo "$response" | grep -o '"id"' | wc -l)
    if [ "$count" -ge "$min_count" ]; then
        echo -e "  ${GREEN}✓${NC} $message (count: $count)"
        ((PASSED++))
        return 0
    else
        echo -e "  ${RED}✗${NC} $message (expected >= $min_count, got $count)"
        ((FAILED++))
        return 1
    fi
}

# ==========================================
# Scenario 1: Dashboard View Flow
# ==========================================
echo "--- Scenario 1: Dashboard View Flow ---"
echo "User opens dashboard and sees stats"

stats=$(curl -s "$BASE_URL/stats")
assert_contains "$stats" "successRate" "Stats contains successRate"
assert_contains "$stats" "totalBuilds" "Stats contains totalBuilds"
assert_contains "$stats" "activeProjects" "Stats contains activeProjects"

builds=$(curl -s "$BASE_URL/builds")
assert_count "$builds" 1 "Recent builds are available"

echo ""

# ==========================================
# Scenario 2: Project List View Flow
# ==========================================
echo "--- Scenario 2: Project List View Flow ---"
echo "User browses project list with group filter"

groups=$(curl -s "$BASE_URL/project-groups")
assert_count "$groups" 1 "Project groups are available"

projects=$(curl -s "$BASE_URL/projects")
assert_count "$projects" 1 "Projects are available"

filtered=$(curl -s "$BASE_URL/projects?groupId=1")
assert_contains "$filtered" "BM1743" "Filter by group works"

echo ""

# ==========================================
# Scenario 3: Project Detail View Flow
# ==========================================
echo "--- Scenario 3: Project Detail View Flow ---"
echo "User views project details with layers and builds"

project=$(curl -s "$BASE_URL/projects/1001")
assert_contains "$project" "BM1743_LENOVO" "Project name is correct"
assert_contains "$project" "Lenovo" "OEM is present"
assert_contains "$project" "김철수" "TL info is present"
assert_contains "$project" "members" "Team members are present"
assert_contains "$project" "milestones" "Milestones are present"
assert_contains "$project" "config" "Build config is present"

layers=$(curl -s "$BASE_URL/layers?projectId=1001")
assert_count "$layers" 1 "Project has layers"

builds=$(curl -s "$BASE_URL/builds?projectId=1001")
assert_count "$builds" 1 "Project has builds"

echo ""

# ==========================================
# Scenario 4: Build Detail View Flow
# ==========================================
echo "--- Scenario 4: Build Detail View Flow ---"
echo "User views build details with pipeline stages"

build=$(curl -s "$BASE_URL/builds/3001")
assert_contains "$build" "buildNumber" "Build number is present"
assert_contains "$build" "success" "Build status is present"
assert_contains "$build" "main" "Branch is present"
assert_contains "$build" "commitHash" "Commit hash is present"
assert_contains "$build" "triggeredBy" "Triggered by is present"
assert_contains "$build" "stages" "Pipeline stages are present"
assert_contains "$build" "Build" "Build stage exists"
assert_contains "$build" "SAM" "SAM stage exists"
assert_contains "$build" "Coverity" "Coverity stage exists"
assert_contains "$build" "artifacts" "Artifacts info is present"

echo ""

# ==========================================
# Scenario 5: Layer Pipeline Configuration
# ==========================================
echo "--- Scenario 5: Layer Pipeline Configuration ---"
echo "User checks layer pipeline configuration"

layer=$(curl -s "$BASE_URL/layers/2001")
assert_contains "$layer" "Release" "Layer name is Release"
assert_contains "$layer" "release" "Layer type is release"
assert_contains "$layer" "pipelineConfig" "Pipeline config exists"
assert_contains "$layer" "enabled" "Stage enabled flag exists"
assert_contains "$layer" "required" "Stage required flag exists"

echo ""

# ==========================================
# Scenario 6: New Build Form Data
# ==========================================
echo "--- Scenario 6: New Build Form Data ---"
echo "User prepares to create new build"

groups=$(curl -s "$BASE_URL/project-groups")
assert_count "$groups" 1 "Can fetch project groups for selection"

projects=$(curl -s "$BASE_URL/projects?groupId=1")
assert_count "$projects" 1 "Can fetch projects for selected group"

layers=$(curl -s "$BASE_URL/layers?projectId=1001")
assert_count "$layers" 1 "Can fetch layers for selected project"

layer=$(curl -s "$BASE_URL/layers/2001")
assert_contains "$layer" "pipelineConfig" "Can fetch pipeline config for form"

echo ""

# ==========================================
# Summary
# ==========================================
echo "======================================"
echo "Scenario Test Results"
echo "======================================"
echo -e "${GREEN}Passed: $PASSED${NC}"
echo -e "${RED}Failed: $FAILED${NC}"
echo "======================================"

if [ "$FAILED" -gt 0 ]; then
    exit 1
fi
