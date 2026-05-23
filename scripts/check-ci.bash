#!/bin/bash

# Stop the script immediately if any command fails
set -e

# Define colors for better readability
GREEN='\033[0;32m'
BLUE='\033[0;34m'
RED='\033[0;31m'
NC='\033[0m' # No Color

echo -e "${BLUE}=========================================${NC}"
echo -e "${BLUE} Starting Local CI Check Process...      ${NC}"
echo -e "${BLUE}=========================================${NC}\n"

# 1. Run Checkstyle
echo -e "${GREEN}[1/4] Running Checkstyle...${NC}"
if mvn -B checkstyle:check -Dcheckstyle.consoleOutput=true; then
    echo -e "${GREEN}✅ Checkstyle completed with no errors.${NC}\n"
else
    echo -e "${RED}❌ Checkstyle failed. Please check the code formatting.${NC}"
    exit 1
fi

# 2. Run Unit Tests and Build
echo -e "${GREEN}[2/4] Running Unit Tests and Build (clean verify)...${NC}"
if mvn -B clean verify; then
    echo -e "${GREEN}✅ Build and Unit Tests passed successfully.${NC}\n"
else
    echo -e "${RED}❌ Errors occurred during Build or Unit Tests.${NC}"
    exit 1
fi

# 3. Scan for vulnerabilities using Trivy (Requires Docker)
echo -e "${GREEN}[3/4] Scanning for vulnerabilities with Trivy (via Docker)...${NC}"
if docker run --rm -v "$(pwd):/app" aquasec/trivy:latest fs /app \
    --scanners vuln,secret \
    --severity CRITICAL,HIGH \
    --exit-code 0; then
    echo -e "${GREEN}✅ No CRITICAL or HIGH vulnerabilities found.${NC}\n"
else
    echo -e "${RED}❌ Trivy detected high-severity vulnerabilities or exposed secrets!${NC}"
    exit 0
fi

# 4. Scan for Secrets using TruffleHog (Requires Docker)
echo -e "${GREEN}[4/4] Scanning for Secrets with TruffleHog (via Docker)...${NC}"
if docker run --rm -v "$(pwd):/app" trufflesecurity/trufflehog:latest filesystem /app --fail; then
    echo -e "${GREEN}✅ Excellent! No leaked secrets detected.${NC}\n"
else
    echo -e "${RED}❌ TruffleHog warns of potential leaked secrets! Please review the source code.${NC}"
    exit 1
fi

echo -e "${BLUE}=====================================================${NC}"
echo -e "${GREEN} 🎉 Your Highness, all code checks passed perfectly! ${NC}"
echo -e "${BLUE}=====================================================${NC}"