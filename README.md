# 🔐 PQC CyberSec Simulator

**A Post-Quantum Cryptography Security Simulation Suite** demonstrating quantum computing threats and the importance of PQC migration. This educational platform shows the "**Harvest Now, Decrypt Later**" (HNDL) attack in real-time with interactive government services.

---

## 📁 Project Structure

```
PQC-CyberSec-Simulator/
├── crypto-lib/           # Cryptography Library (ML-DSA, ML-KEM, RSA, AES)
├── gov-portal/           # Government Portal with Web UI (Port 8181)
├── secure-messaging/     # Encrypted Messaging Service (Port 8182)
├── hacker-console/       # Hacker Attack Simulation (Port 8183)
├── quantum-simulator/    # Python cuQuantum GPU Quantum Simulator (Port 8184)
├── ui-tests/             # Selenium UI Tests (Three-Panel Demo)
├── docker-compose.yml    # Docker deployment configuration
└── pom.xml               # Parent Maven configuration
```

---

## 🚀 Quick Start Guide

### Prerequisites

| Requirement | Version | Notes |
|-------------|---------|-------|
| **Java JDK** | 21+ | Required for all Java services |
| **Maven** | 3.9+ | Build tool |
| **Docker Desktop** | Latest | For PostgreSQL & government services |
| **Chrome Browser** | Latest | For Selenium UI tests |
| **Python** | 3.10+ | For quantum simulator (optional) |
| **NVIDIA GPU** | RTX 20 series+ | Optional - for GPU quantum simulation |

---

## 📦 Installation & Setup

### Step 1: Clone and Build

```bash
# Clone the repository
git clone <repository-url>
cd PQC-CyberSec-Simulator

# Build all modules
mvn clean compile -DskipTests

# Or build with tests
mvn clean verify
```

### Step 2: Start Database (PostgreSQL)

```bash
# Start PostgreSQL container
docker-compose up -d postgres

# Wait for database to be ready (about 10 seconds)
docker-compose logs postgres
```

### Step 3: Start Government Services (Docker)

```bash
# Start all government services
docker-compose up -d

# Verify containers are running
docker-compose ps
```

**Services Started:**

| Service | URL | Description |
|---------|-----|-------------|
| **Government Portal** | http://localhost:8181 | Web UI for citizens & officers |
| **Secure Messaging** | http://localhost:8182 | Encrypted communications API |
| **PostgreSQL** | localhost:5432 | Database |

### Step 4: Start Hacker Console (Local)

The hacker console runs **OUTSIDE** Docker to simulate a realistic external threat actor:

**Option A: Using Batch File (Windows)**
```bash
# From project root
start-hacker-standalone.bat
```

**Option B: Using Maven**
```bash
cd hacker-console
mvn spring-boot:run -Dspring-boot.run.profiles=standalone
```

| Hacker Console | URL |
|----------------|-----|
| **Attack Dashboard** | http://localhost:8183 |

### Step 5: Start Quantum Simulator (Optional)

For REAL GPU-accelerated quantum circuit simulation:

**Option A: Using Batch File (Windows)**
```bash
# From project root
start-quantum.bat
```

**Option B: Manual Setup**
```bash
cd quantum-simulator

# Create virtual environment
python -m venv venv
venv\Scripts\activate      # Windows
# source venv/bin/activate  # Linux/Mac

# Install dependencies
pip install -r requirements.txt

# For GPU support (requires CUDA 12)
pip install cupy-cuda12x cuquantum-python-cu12

# Start the service
python quantum_service.py
```

| Quantum Simulator | URL |
|-------------------|-----|
| **API Status** | http://localhost:8184/api/quantum/status |

---

## 👥 Demo User Accounts

| Role | Username | Password | Description |
|------|----------|----------|-------------|
| 👤 **Citizen** | `john.citizen` | `Citizen@2024!` | Regular citizen account |
| 👤 **Citizen** | `emily.chen` | `Citizen@2024!` | Another citizen account |
| 👮 **Officer** | `officer` | `Officer@2024!` | Government officer |
| ⚙️ **Admin** | `admin` | `Admin@PQC2024!` | System administrator |

---

## 🎮 Running the Demo

### Interactive Three-Panel UI Demo

The main demonstration shows **three Chrome browser windows simultaneously** demonstrating a realistic HNDL (Harvest Now, Decrypt Later) attack:

| Panel | User | Description |
|-------|------|-------------|
| **LEFT** | 👤 Citizen | Regular citizen using government services |
| **CENTER** | 👮 Officer | Government officer reviewing applications |
| **RIGHT** | 🕵️ Hacker | Threat actor with quantum attack capability |

### Prerequisites for UI Test

Before running the demo, ensure ALL services are running:

```bash
# 1. Start Docker services (gov-portal, secure-messaging, postgres)
docker-compose up -d

# 2. Start Quantum Simulator (Terminal 1)
cd quantum-simulator
python quantum_service.py

# 3. Start Hacker Console (Terminal 2)
cd hacker-console
mvn spring-boot:run -Dspring-boot.run.profiles=standalone

# 4. Verify all services are running
# - Gov Portal: http://localhost:8181 
# - Hacker Console: http://localhost:8183
# - Quantum Sim: http://localhost:8184
```

### Run the Three-Panel Demo

```bash
cd ui-tests
mvn test -Dtest=PqcSecurityDemoTest
```

**⏱️ Test Duration:** ~4-5 minutes  
**📺 Display:** Three Chrome windows will appear - position them side-by-side for best experience!

### What the Demo Shows

#### Phase 1: Authentication
- **Citizen** logs in as `john.citizen`
- **Officer** logs in with elevated privileges
- **Hacker** detects active sessions via network monitoring

#### Phase 2: Data Submission (RSA - VULNERABLE)
- **Citizen** submits Car License application with RSA-2048 encryption
- **Hacker** intercepts ENCRYPTED packets (shows raw hex data):
  ```
  🔒 ENCRYPTED PAYLOAD:
     3F8CD0C0D3BC1822 BDDC9DB950F71F4D 3AC2F3C19AC110...
  ⚠️ VULNERABLE: Shor's Algorithm can break this!
  ```

#### Phase 3: Data Submission (ML-KEM - QUANTUM-SAFE)
- **Citizen** submits Tax Filing with ML-KEM-768 encryption
- **Hacker** intercepts but notes quantum-resistant cipher:
  ```
  🛡️ QUANTUM-SAFE: No known attack exists
     → Stored but likely UNDECRYPTABLE
  ```

#### Phase 4: Quantum Attack Execution
- **Hacker** executes Shor's Algorithm on RSA packets
- **RSA-2048 BROKEN** - Decrypted citizen data exposed:
  ```
  ╔═════════════════════════════════════════════════════════╗
  ║     💔 DECRYPTED DATA - Car License Application         ║
  ╠═════════════════════════════════════════════════════════╣
  ║ 👤 Name: John Michael Citizen                           ║
  ║ 📅 DOB: 1985-06-15                                      ║
  ║ 🏠 Address: 1247 Oak Street, Springfield, IL 62701      ║
  ╚═════════════════════════════════════════════════════════╝
  ```

#### Phase 5: ML-KEM Attack (FAILS)
- **Hacker** attempts Lattice attack on ML-KEM packets
- **ATTACK FAILED** - Tax Filing data remains protected:
  ```
  ╔═════════════════════════════════════════════════════════╗
  ║     🔒 Tax Filing Data REMAINS ENCRYPTED                ║
  ╠═════════════════════════════════════════════════════════╣
  ║ [ENCRYPTED - CANNOT DECRYPT]                            ║
  ║ Income, SSN, Bank Account remain PROTECTED              ║
  ╚═════════════════════════════════════════════════════════╝
  ```

### Demo Summary Output

```
╔════════════════════════════════════════════════════════════════════════════════╗
║                    PQC SECURITY DEMONSTRATION COMPLETE                         ║
╠════════════════════════════════════════════════════════════════════════════════╣
║  📋 DOCUMENTS SUBMITTED:                                                       ║
║     • Car License (RSA-2048)  → ⚠️ VULNERABLE - Data exposed by quantum       ║
║     • Tax Filing (ML-KEM-768) → ✅ PROTECTED - Data remains secure            ║
║                                                                                ║
║  ⚛️ QUANTUM ATTACK RESULTS:                                                    ║
║     • Shor's Algorithm on RSA-2048: 💔 SUCCESS (key factored)                  ║
║     • Lattice Attack on ML-KEM:     🛡️ FAILED (no efficient attack)           ║
╚════════════════════════════════════════════════════════════════════════════════╝
```

---

## 🔐 Cryptographic Algorithms

### ✅ Quantum-Safe (NIST FIPS 203/204)

| Algorithm | Type | Security Level |
|-----------|------|----------------|
| **ML-KEM** (Kyber768) | Key Encapsulation | 192-bit quantum |
| **ML-DSA** (Dilithium3) | Digital Signature | 128-bit quantum |

### ⚠️ Classical (Vulnerable to Quantum)

| Algorithm | Type | Quantum Threat |
|-----------|------|----------------|
| **RSA-2048** | Digital Signature | ❌ Broken by Shor's |
| **AES-256** | Symmetric | ⚡ Reduced by Grover's |

---

## 🧪 Running Tests

### Quick Test Commands

| Test Type | Command | Description |
|-----------|---------|-------------|
| **All Tests** | `mvn test` | Run all unit tests |
| **UI Demo** | `cd ui-tests && mvn test -Dtest=PqcSecurityDemoTest` | Run 3-panel demo |
| **Single Module** | `mvn test -pl gov-portal` | Test specific module |
| **Skip Tests** | `mvn install -DskipTests` | Build without tests |

### Full Three-Panel UI Demo Test

```bash
# Step 1: Ensure services are running
docker-compose up -d
cd quantum-simulator && python quantum_service.py &
cd hacker-console && mvn spring-boot:run -Dspring-boot.run.profiles=standalone &

# Step 2: Run UI test (wait for services ~30 seconds)
cd ui-tests
mvn test -Dtest=PqcSecurityDemoTest
```

**Expected Output:**
```
[INFO] Tests run: 8, Failures: 0, Errors: 0, Skipped: 0
[INFO] BUILD SUCCESS
[INFO] Total time:  04:19 min
```

### Test Descriptions

| Test # | Name | What It Tests |
|--------|------|---------------|
| 1 | Initialize Panels | Opens 3 Chrome windows, connects to services |
| 2 | Authentication | Citizen & Officer login |
| 3 | RSA Submission | Car license with RSA-2048 (vulnerable) |
| 4 | Officer Review | Officer reviews pending applications |
| 5 | ML-KEM Submission | Tax filing with ML-KEM-768 (quantum-safe) |
| 6 | Quantum Attack | Shor's & Grover's algorithms execution |
| 7 | Tax Processing | Officer processes quantum-safe document |
| 8 | Summary | Final security demonstration report |

### All Unit Tests

```bash
mvn test
```

### UI Demo Test Only

```bash
cd ui-tests
mvn test -Dtest=PqcSecurityDemoTest
```

### Test with Specific Browser

```bash
cd ui-tests
mvn test -Dtest=PqcSecurityDemoTest -Dwebdriver.chrome.driver=/path/to/chromedriver
```

---

## 📡 API Endpoints

### Government Portal (8181)

| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | `/login` | Login page |
| GET | `/dashboard` | User dashboard |
| GET | `/services/car-license` | Car license form |
| POST | `/services/car-license` | Submit application |
| GET | `/services/tax-filing` | Tax filing form |
| POST | `/services/tax-filing` | Submit tax return |
| GET | `/officer/review/{id}` | Review document |
| POST | `/officer/approve/{id}` | Approve document |

### Hacker Console (8183)

| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | `/api/hacker/gpu` | GPU information |
| POST | `/api/hacker/harvest/transactions` | Intercept data |
| POST | `/api/hacker/quantum-attack` | Execute attack |
| GET | `/api/hacker/harvested` | List captured data |

### Quantum Simulator (8184)

| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | `/api/quantum/status` | Service status |
| POST | `/api/quantum/attack/rsa` | Shor's algorithm |
| POST | `/api/quantum/attack/lattice` | Lattice attack |

---

## 🗄️ Database Configuration

| Setting | Value |
|---------|-------|
| **Host** | localhost |
| **Port** | 5432 |
| **Database** | pqc_cybersec |
| **Username** | pqc_admin |
| **Password** | PqcSecure2024! |

---

## ⚠️ Troubleshooting

### Common Issues

| Problem | Solution |
|---------|----------|
| **Port already in use** | Stop conflicting service or check Docker containers |
| **Services not starting** | Check `docker-compose logs` for errors |
| **Database connection refused** | Ensure PostgreSQL is running: `docker-compose up -d postgres` |
| **Chrome not found** | Install Chrome or update WebDriver |
| **Hacker console can't connect** | Verify services are running on correct ports |
| **Quantum service unavailable** | Run `python quantum_service.py` manually |

### UI Test Issues

| Problem | Solution |
|---------|----------|
| **Browsers don't appear** | Test runs in headed mode by default - check display |
| **Test fails at login** | Verify gov-portal is running: `curl http://localhost:8181` |
| **Quantum attack shows simulation** | Start Python quantum service on port 8184 |
| **"No such element" error** | Services may not be ready - increase sleep times |
| **ChromeDriver version mismatch** | Update Chrome or let WebDriverManager auto-download |

### Verify Services Are Running

```bash
# Check all ports are listening
# Windows PowerShell:
Get-NetTCPConnection -LocalPort 8181,8182,8183,8184 -ErrorAction SilentlyContinue | Format-Table LocalPort,State

# Linux/Mac:
netstat -tlnp | grep -E '8181|8182|8183|8184'

# Expected output: All ports should show "Listen" state
```

### Check Service Health

```bash
# Gov Portal
curl http://localhost:8181/login

# Hacker Console
curl http://localhost:8183/api/hacker/gpu

# Quantum Simulator
curl http://localhost:8184/api/quantum/status
```

### Viewing Logs

```bash
# Docker service logs
docker-compose logs -f gov-portal
docker-compose logs -f secure-messaging

# Hacker console logs (if running via Maven)
# Logs appear in terminal
```

### Resetting the Demo

```bash
# Stop all services
docker-compose down

# Remove database volume (fresh start)
docker-compose down -v

# Restart everything
docker-compose up -d
```

---

## 🏗️ Building for Production

### Build JAR Files

```bash
mvn clean package -DskipTests

# JARs created:
# - gov-portal/target/gov-portal-1.0.0.jar
# - secure-messaging/target/secure-messaging-1.0.0.jar
# - hacker-console/target/hacker-console-1.0.0.jar
```

### Build Docker Images

```bash
docker-compose build
```

---

## 📚 Educational Purpose

This simulator demonstrates:

1. **Shor's Algorithm** - How quantum computers break RSA encryption
2. **Grover's Algorithm** - How quantum computers reduce symmetric key security
3. **HNDL Attack** - Why "Harvest Now, Decrypt Later" is a real threat
4. **PQC Migration** - Why organizations must migrate to quantum-safe cryptography NOW

### Realistic HNDL Attack Simulation

The demo shows a **realistic** Harvest Now, Decrypt Later attack flow:

| Phase | Action | Data Shown |
|-------|--------|------------|
| **1. Harvest** | Intercept encrypted packets | Raw hex encrypted data (unreadable) |
| **2. Store** | Save for future attack | Encrypted payload + cipher metadata |
| **3. Attack** | Run quantum algorithms | Shor's algorithm progress (4099 qubits) |
| **4. Decrypt** | Extract plaintext (RSA only) | Decrypted personal information |
| **5. Fail** | Attack ML-KEM | "CANNOT DECRYPT" message |

### GPU Quantum Simulation

When running with NVIDIA GPU:
- **Detected GPU:** NVIDIA GeForce RTX 4060 Laptop GPU (8GB VRAM)
- **Max Qubits:** 28 (limited by GPU memory)
- **Shor's Algorithm:** Requires ~4099 qubits for RSA-2048 (simulated)

### NIST Post-Quantum Standards

- **FIPS 203** - ML-KEM (Kyber) - Key Encapsulation
- **FIPS 204** - ML-DSA (Dilithium) - Digital Signatures
- **FIPS 205** - SLH-DSA (SPHINCS+) - Stateless Hash Signatures

---

## 📜 License

Open Source - Educational Use Only

**⚠️ Disclaimer:** This simulation is for educational purposes only. The "hacker" functionality demonstrates real security threats but should never be used maliciously.

---

## 🙏 Credits

- **Bouncy Castle** - PQC cryptography library (v1.79)
- **NIST** - Post-Quantum Cryptography standards
- **Spring Boot 3.5** - Microservices framework
- **Selenium** - UI testing framework
- **cuQuantum** - NVIDIA quantum simulation SDK
