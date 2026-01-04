#!/bin/bash
# ═══════════════════════════════════════════════════════════════════════════════
#  PQC CYBERSEC SIMULATOR - 4-PANEL DEMO (Linux/macOS)
# ═══════════════════════════════════════════════════════════════════════════════
#  Demonstrates Post-Quantum Cryptography vs Classical Encryption
#  
#  4 Browser Panels:
#    TOP-LEFT:     Citizen - Submits government documents
#    TOP-RIGHT:    Officer - Reviews and approves applications  
#    BOTTOM-LEFT:  Hacker Harvest - Intercepts encrypted traffic
#    BOTTOM-RIGHT: Hacker Decrypt - Quantum attack progress
# ═══════════════════════════════════════════════════════════════════════════════

set -e
SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"

echo ""
echo "╔══════════════════════════════════════════════════════════════════╗"
echo "║        PQC CYBERSEC - 4-PANEL DEMO LAUNCHER                      ║"
echo "║        Demonstrating Quantum-Resistant Cryptography              ║"
echo "╚══════════════════════════════════════════════════════════════════╝"
echo ""

# Step 1: Check Docker
echo "[1/5] Checking Docker..."
if ! docker info >/dev/null 2>&1; then
    echo "[ERROR] Docker is not running! Please start Docker."
    exit 1
fi
echo "[OK] Docker is running"

# Step 2: Restart Docker services with fresh database
echo "[2/5] Starting Docker services with fresh database..."
cd "$SCRIPT_DIR"
docker-compose down -v >/dev/null 2>&1 || true
docker-compose up -d postgres >/dev/null 2>&1
sleep 8
docker-compose up -d gov-portal secure-messaging >/dev/null 2>&1
echo "[INFO] Waiting 25 seconds for services..."
sleep 25
echo "[OK] Docker services started"

# Step 3: Stop existing hacker-console
echo "[3/5] Preparing hacker-console..."
HACKER_PID=$(lsof -ti:8183 2>/dev/null || true)
if [ -n "$HACKER_PID" ]; then
    kill -9 $HACKER_PID 2>/dev/null || true
fi
rm -rf "$SCRIPT_DIR/hacker-console/hacker-data" 2>/dev/null || true
sleep 2
echo "[OK] Ready"

# Step 4: Start hacker-console in new terminal
echo "[4/5] Starting Hacker Console..."

# Detect terminal emulator
if command -v gnome-terminal &> /dev/null; then
    gnome-terminal --title="Hacker Console" -- bash -c "cd '$SCRIPT_DIR/hacker-console' && mvn spring-boot:run -Dspring-boot.run.profiles=standalone; exec bash"
elif command -v xterm &> /dev/null; then
    xterm -T "Hacker Console" -e "cd '$SCRIPT_DIR/hacker-console' && mvn spring-boot:run -Dspring-boot.run.profiles=standalone; bash" &
elif command -v konsole &> /dev/null; then
    konsole --new-tab -e bash -c "cd '$SCRIPT_DIR/hacker-console' && mvn spring-boot:run -Dspring-boot.run.profiles=standalone; exec bash" &
elif [[ "$OSTYPE" == "darwin"* ]]; then
    # macOS
    osascript -e "tell application \"Terminal\" to do script \"cd '$SCRIPT_DIR/hacker-console' && mvn spring-boot:run -Dspring-boot.run.profiles=standalone\""
else
    echo "[WARN] No supported terminal found. Starting in background..."
    cd "$SCRIPT_DIR/hacker-console"
    mvn spring-boot:run -Dspring-boot.run.profiles=standalone &
fi

# Wait for hacker-console
RETRY=0
MAX_RETRY=12
while [ $RETRY -lt $MAX_RETRY ]; do
    sleep 5
    RETRY=$((RETRY + 1))
    if curl -s http://localhost:8183/harvest >/dev/null 2>&1; then
        break
    fi
    echo "[INFO] Waiting for Hacker Console... [$RETRY/$MAX_RETRY]"
done

if ! curl -s http://localhost:8183/harvest >/dev/null 2>&1; then
    echo "[ERROR] Hacker Console failed to start!"
    exit 1
fi
echo "[OK] Hacker Console running"

# Step 5: Run test
echo "[5/5] Running 4-Panel Demo..."
echo ""
echo "════════════════════════════════════════════════════════════════════"
echo "  WATCH THE 4 BROWSER PANELS:"
echo "  - Top-Left:     Citizen submits documents"
echo "  - Top-Right:    Officer reviews applications"
echo "  - Bottom-Left:  Hacker intercepts encrypted traffic"
echo "  - Bottom-Right: Quantum decryption attack"
echo "════════════════════════════════════════════════════════════════════"
echo ""

cd "$SCRIPT_DIR/ui-tests"
echo ""
echo "Running Selenium test - Watch the 4 browser panels!"
echo ""
mvn test -Dtest=com.pqc.selenium.FourPanelRealisticDemoTest

echo ""
echo "════════════════════════════════════════════════════════════════════"
echo "  Demo Complete! Hacker Console window remains open."
echo "════════════════════════════════════════════════════════════════════"
