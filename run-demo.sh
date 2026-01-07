#!/bin/bash
# ═══════════════════════════════════════════════════════════════════════════════
#  PQC CYBERSEC SIMULATOR - COMPREHENSIVE 4-PANEL DEMO
# ═══════════════════════════════════════════════════════════════════════════════
#  Usage:
#    ./run-demo.sh           - Auto-detect (Docker if available, else local)
#    ./run-demo.sh --local   - Force local on-premise mode (no Docker)
#    ./run-demo.sh --docker  - Force Docker mode
#
#  Runs ALL 4 scenarios comparing Classical vs PQC cryptography
# ═══════════════════════════════════════════════════════════════════════════════

set -e

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
CYAN='\033[0;36m'
NC='\033[0m' # No Color

# Get script directory
SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"

# Parse command line arguments
FORCE_LOCAL=false
FORCE_DOCKER=false

case "$1" in
    --local|-l|--onprem)
        FORCE_LOCAL=true
        ;;
    --docker|-d)
        FORCE_DOCKER=true
        ;;
esac

# Cleanup function
cleanup() {
    echo ""
    echo -e "${YELLOW}Shutting down services...${NC}"
    pkill -f "spring-boot:run" 2>/dev/null || true
    pkill -f "quantum_service.py" 2>/dev/null || true
    if $USE_DOCKER; then
        docker-compose down 2>/dev/null || true
    fi
    echo -e "${GREEN}Cleanup complete${NC}"
    exit 0
}

trap cleanup SIGINT SIGTERM

echo ""
echo "============================================================================================="
echo "       PQC CYBERSEC - COMPREHENSIVE 4-PANEL DEMO"
echo "       Demonstrating Quantum-Resistant vs Classical Cryptography"
echo "============================================================================================="
echo "  ALL 4 SCENARIOS WILL RUN:"
echo "    1. RSA KEM + RSA Sig     - FULLY VULNERABLE (Both broken by quantum)"
echo "    2. ML-KEM + ML-DSA       - FULLY QUANTUM-SAFE (Both protected)"
echo "    3. RSA KEM + ML-DSA      - MIXED (Encryption vulnerable, Signature safe)"
echo "    4. ML-KEM + RSA Sig      - MIXED (Encryption safe, Signature vulnerable)"
echo "============================================================================================="
echo "  Usage: ./run-demo.sh [--local | --docker]"
echo "    --local, -l, --onprem : Force local on-premise mode (SQLite database, no Docker)"
echo "    --docker, -d          : Force Docker mode (PostgreSQL in container)"
echo "    (no flag)             : Auto-detect infrastructure"
echo "============================================================================================="
echo ""

# ═══════════════════════════════════════════════════════════════════════════════
# Step 1: Check Prerequisites and Determine Infrastructure
# ═══════════════════════════════════════════════════════════════════════════════
echo -e "${BLUE}[1/6] Checking Prerequisites and Determining Infrastructure...${NC}"

# Check Java
if ! command -v java &> /dev/null; then
    echo -e "${RED}   [ERROR] Java is not installed! Please install JDK 17+.${NC}"
    exit 1
fi
echo -e "   ${GREEN}[OK] Java found${NC}"

# Check Maven
if ! command -v mvn &> /dev/null; then
    echo -e "${RED}   [ERROR] Maven is not installed! Please install Apache Maven.${NC}"
    exit 1
fi
echo -e "   ${GREEN}[OK] Maven found${NC}"

# Check Python for quantum simulator
PYTHON_AVAILABLE=false
PYTHON_CMD=""
if command -v python3 &> /dev/null; then
    echo -e "   ${GREEN}[OK] Python3 found${NC}"
    PYTHON_AVAILABLE=true
    PYTHON_CMD="python3"
elif command -v python &> /dev/null; then
    echo -e "   ${GREEN}[OK] Python found${NC}"
    PYTHON_AVAILABLE=true
    PYTHON_CMD="python"
else
    echo -e "   ${YELLOW}[WARN] Python not found - Quantum simulator will use simulation mode${NC}"
fi

# Determine deployment mode
USE_DOCKER=false

if $FORCE_LOCAL; then
    echo -e "   ${CYAN}[INFO] Forced LOCAL mode via command line${NC}"
    USE_DOCKER=false
elif $FORCE_DOCKER; then
    if command -v docker &> /dev/null && docker info &> /dev/null; then
        echo -e "   ${CYAN}[INFO] Forced DOCKER mode via command line${NC}"
        USE_DOCKER=true
    else
        echo -e "${RED}   [ERROR] Docker requested but not available!${NC}"
        exit 1
    fi
else
    # Auto-detect Docker availability
    if command -v docker &> /dev/null && docker info &> /dev/null; then
        echo -e "   ${GREEN}[OK] Docker detected and running (auto-detected)${NC}"
        USE_DOCKER=true
    else
        echo -e "   ${CYAN}[INFO] Docker not available - using local on-premise mode${NC}"
    fi
fi

echo ""
if $USE_DOCKER; then
    echo -e "   ${CYAN}=== DEPLOYMENT MODE: DOCKER CONTAINERS ===${NC}"
    echo "   Database: PostgreSQL (containerized)"
    echo "   Gov-Portal: Docker container"
else
    echo -e "   ${CYAN}=== DEPLOYMENT MODE: LOCAL ON-PREMISE ===${NC}"
    echo "   Database: SQLite (file-based, persistent)"
    echo "   Gov-Portal: Local Maven process"
fi
echo "   Hacker Console: Local Maven process (always on-premise)"
echo ""

# ═══════════════════════════════════════════════════════════════════════════════
# Step 2: Start Database
# ═══════════════════════════════════════════════════════════════════════════════
echo -e "${BLUE}[2/6] Starting Database...${NC}"

if $USE_DOCKER; then
    echo "   Starting PostgreSQL via Docker..."
    docker-compose down -v 2>/dev/null || true
    docker-compose up -d postgres
    echo "   [INFO] Waiting for PostgreSQL container..."
    sleep 10
    echo -e "   ${GREEN}[OK] PostgreSQL started (Docker)${NC}"
    DB_PROFILE="docker"
else
    echo -e "   ${GREEN}[OK] Using SQLite database (file-based, persistent)${NC}"
    # Create data directory for SQLite
    mkdir -p "$SCRIPT_DIR/gov-portal/data"
    DB_PROFILE="sqlite"
fi

# ═══════════════════════════════════════════════════════════════════════════════
# Step 3: Deploy Gov-Portal Service
# ═══════════════════════════════════════════════════════════════════════════════
echo -e "${BLUE}[3/6] Deploying Gov-Portal Service...${NC}"

# Kill existing processes on port 8181
lsof -ti:8181 | xargs kill -9 2>/dev/null || true
sleep 2

if $USE_DOCKER; then
    echo "   Starting Gov-Portal container..."
    docker-compose up -d gov-portal
    echo "   [INFO] Waiting for Gov-Portal container to be healthy..."
    for i in {1..12}; do
        if curl -s http://localhost:8181/ > /dev/null 2>&1; then
            break
        fi
        echo "   [INFO] Waiting for Gov-Portal... [$i/12]"
        sleep 5
    done
    if ! curl -s http://localhost:8181/ > /dev/null 2>&1; then
        echo -e "${RED}   [ERROR] Gov-Portal container failed to start!${NC}"
        docker-compose logs gov-portal
        exit 1
    fi
else
    echo "   Starting Gov-Portal locally with $DB_PROFILE profile..."
    cd "$SCRIPT_DIR/gov-portal"
    mvn spring-boot:run -Dspring-boot.run.profiles=$DB_PROFILE > /tmp/gov-portal.log 2>&1 &
    GOV_PID=$!
    cd "$SCRIPT_DIR"
    
    for i in {1..15}; do
        if curl -s http://localhost:8181/ > /dev/null 2>&1; then
            break
        fi
        echo "   [INFO] Waiting for Gov-Portal... [$i/15]"
        sleep 5
    done
    if ! curl -s http://localhost:8181/ > /dev/null 2>&1; then
        echo -e "${RED}   [ERROR] Gov-Portal failed to start!${NC}"
        exit 1
    fi
fi
echo -e "   ${GREEN}[OK] Gov-Portal running on http://localhost:8181${NC}"

# ═══════════════════════════════════════════════════════════════════════════════
# Step 4: Deploy Hacker Console UI (ALWAYS LOCAL - simulates external attacker)
# ═══════════════════════════════════════════════════════════════════════════════
echo -e "${BLUE}[4/6] Deploying Hacker Console UI (Local On-Premise)...${NC}"

# Kill existing processes on port 8183
lsof -ti:8183 | xargs kill -9 2>/dev/null || true
rm -rf "$SCRIPT_DIR/hacker-console/hacker-data" 2>/dev/null || true
sleep 2

# Hacker Console ALWAYS runs locally to simulate external threat actor
echo "   Starting Hacker Console locally (standalone mode - external attacker)..."
cd "$SCRIPT_DIR/hacker-console"
mvn spring-boot:run -Dspring-boot.run.profiles=standalone > /tmp/hacker-console.log 2>&1 &
HACKER_PID=$!
cd "$SCRIPT_DIR"

for i in {1..12}; do
    if curl -s http://localhost:8183/harvest > /dev/null 2>&1; then
        break
    fi
    echo "   [INFO] Waiting for Hacker Console... [$i/12]"
    sleep 5
done
if ! curl -s http://localhost:8183/harvest > /dev/null 2>&1; then
    echo -e "${RED}   [ERROR] Hacker Console failed to start!${NC}"
    exit 1
fi
echo -e "   ${GREEN}[OK] Hacker Console UI running on http://localhost:8183 (Local)${NC}"

# ═══════════════════════════════════════════════════════════════════════════════
# Step 5: Deploy Quantum Simulator
# ═══════════════════════════════════════════════════════════════════════════════
echo -e "${BLUE}[5/6] Deploying Quantum Simulator...${NC}"

# Kill existing processes on port 8184
lsof -ti:8184 | xargs kill -9 2>/dev/null || true
sleep 2

QUANTUM_AVAILABLE=false

# Quantum Simulator always runs locally (Python)
if $PYTHON_AVAILABLE; then
    echo "   Starting Quantum Simulator locally (Python)..."
    cd "$SCRIPT_DIR/quantum-simulator"
    $PYTHON_CMD quantum_service.py > /tmp/quantum-simulator.log 2>&1 &
    QS_PID=$!
    cd "$SCRIPT_DIR"
    
    for i in {1..8}; do
        if curl -s http://localhost:8184/api/quantum/status > /dev/null 2>&1; then
            QUANTUM_AVAILABLE=true
            break
        fi
        echo "   [INFO] Waiting for Quantum Simulator... [$i/8]"
        sleep 3
    done
else
    echo -e "   ${YELLOW}[SKIP] Quantum Simulator (Python not available)${NC}"
fi

if $QUANTUM_AVAILABLE; then
    echo -e "   ${GREEN}[OK] Quantum Simulator running on http://localhost:8184${NC}"
else
    echo -e "   ${YELLOW}[INFO] Proceeding with simulation mode for quantum attacks${NC}"
fi

# ═══════════════════════════════════════════════════════════════════════════════
# Step 6: Run Comprehensive 4-Panel Demo (ALL SCENARIOS)
# ═══════════════════════════════════════════════════════════════════════════════
echo -e "${BLUE}[6/6] Running Comprehensive 4-Panel Demo...${NC}"
echo ""
echo "============================================================================================="
echo "  DEPLOYMENT SUMMARY:"
if $USE_DOCKER; then
    echo "    Infrastructure: Docker Containers + Local Services"
    echo "    Database:       PostgreSQL (containerized)"
    echo "    Gov-Portal:     Docker container"
else
    echo "    Infrastructure: Fully Local On-Premise (No Docker)"
    echo "    Database:       SQLite (file-based, persistent)"
    echo "    Gov-Portal:     Local Maven process"
fi
echo "    Hacker Console: Local Maven process (simulates external attacker)"
echo "    Quantum Sim:    Local Python process"
echo ""
echo "  SERVICES DEPLOYED:"
echo "    Gov-Portal:        http://localhost:8181"
echo "    Hacker Console UI: http://localhost:8183"
echo "    Quantum Simulator: http://localhost:8184"
echo ""
echo "  WATCH THE 4 BROWSER PANELS:"
echo "    Top-Left:     Citizen submits documents with different algorithms"
echo "    Top-Right:    Officer reviews applications"
echo "    Bottom-Left:  Hacker intercepts encrypted traffic"
echo "    Bottom-Right: Quantum decryption attack progress"
echo ""
echo "  RUNNING ALL 4 SCENARIOS:"
echo "    Scenario 1: RSA + RSA       - FULLY VULNERABLE"
echo "    Scenario 2: ML-KEM + ML-DSA - FULLY QUANTUM-SAFE"
echo "    Scenario 3: RSA + ML-DSA    - MIXED"
echo "    Scenario 4: ML-KEM + RSA    - MIXED"
echo "============================================================================================="
echo ""

cd "$SCRIPT_DIR/ui-tests"
echo "Starting Selenium tests - Watch the 4 browser panels!"
echo ""

# Run comprehensive test with all scenarios
mvn test -Dtest=com.pqc.selenium.ComprehensiveCryptoTest

echo ""
echo "============================================================================================="
echo "  DEMO COMPLETE!"
echo ""
echo "  Services remain running for manual exploration:"
echo "    Gov-Portal:        http://localhost:8181"
echo "    Hacker Console UI: http://localhost:8183"
echo "    Quantum Simulator: http://localhost:8184"
echo ""
echo "  To stop all services:"
if $USE_DOCKER; then
    echo "    docker-compose down"
    echo "    pkill -f spring-boot:run"
else
    echo "    Press Ctrl+C or run: pkill -f spring-boot:run"
fi
echo "============================================================================================="
echo ""
echo "Press Enter to stop services and exit..."
read -r
cleanup
