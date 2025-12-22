# 🔐 PQC CyberSec Simulator

A **full-featured Post-Quantum Cryptography Simulation Suite** demonstrating quantum computing threats and the importance of PQC for prevention. This project raises awareness about the "**Harvest Now, Decrypt Later**" threat.

## 🚀 Quick Start with Docker

### Prerequisites
- Docker Desktop installed and running
- Git

### Deploy with Docker Compose

```bash
# Clone and navigate to project
cd PQC-CyberSec-Simulator

# Start all services (PostgreSQL + 3 microservices)
docker-compose up -d

# View logs
docker-compose logs -f

# Stop all services
docker-compose down
```

### Access the Application

| Service | URL | Description |
|---------|-----|-------------|
| **User & Document Service** | http://localhost:8081 | User registration, authentication, documents |
| **Messaging Service** | http://localhost:8082 | Encrypted messaging |
| **Hacker Simulation** | http://localhost:8083 | Quantum attack demos |
| **pgAdmin** | http://localhost:5050 | Database management |

### Default Credentials

| Service | Username | Password |
|---------|----------|----------|
| **Admin User** | `admin` | `Admin@PQC2024!` |
| **Officer User** | `officer` | `Officer@2024!` |
| **Citizen User** | `citizen` | `Citizen@2024!` |
| **pgAdmin** | `admin@pqc.local` | `admin123` |
| **PostgreSQL** | `pqc_admin` | `PqcSecure2024!` |

---

## 📡 API Documentation

### 🔐 Authentication (Port 8081)

#### Register New User
```bash
curl -X POST http://localhost:8081/api/auth/register \
  -H "Content-Type: application/json" \
  -d '{
    "username": "johndoe",
    "email": "john@example.com",
    "password": "SecurePass123!",
    "fullName": "John Doe"
  }'
```

#### Login
```bash
curl -X POST http://localhost:8081/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "usernameOrEmail": "admin",
    "password": "Admin@PQC2024!"
  }'
```

Response:
```json
{
  "token": "eyJhbGciOiJIUzI1NiJ9...",
  "tokenType": "Bearer",
  "expiresIn": 86400,
  "user": {
    "userId": "abc123...",
    "username": "admin",
    "role": "ADMIN",
    "signatureAlgorithm": "ML_DSA",
    "hasKeys": true
  }
}
```

### 👤 User Management (Port 8081)

#### Get Current User
```bash
curl -X GET http://localhost:8081/api/users/me \
  -H "Authorization: Bearer YOUR_JWT_TOKEN"
```

#### Get All Users (Admin only)
```bash
curl -X GET http://localhost:8081/api/users \
  -H "Authorization: Bearer ADMIN_JWT_TOKEN"
```

#### Update Algorithm Preferences
```bash
curl -X PUT http://localhost:8081/api/users/{userId}/algorithm \
  -H "Authorization: Bearer YOUR_JWT_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "signatureAlgorithm": "RSA_2048",
    "encryptionAlgorithm": "AES_256"
  }'
```

### 📄 Document Management (Port 8081)

#### Create Document
```bash
curl -X POST http://localhost:8081/api/documents \
  -H "Authorization: Bearer YOUR_JWT_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "documentType": "LICENSE",
    "title": "Driver License Application",
    "content": "I hereby apply for a driver license..."
  }'
```

#### Sign Document
```bash
curl -X POST http://localhost:8081/api/documents/{documentId}/sign \
  -H "Authorization: Bearer YOUR_JWT_TOKEN"
```

#### Verify Document Signature
```bash
curl -X POST http://localhost:8081/api/documents/{documentId}/verify \
  -H "Authorization: Bearer YOUR_JWT_TOKEN"
```

#### Approve Document (Admin/Officer)
```bash
curl -X POST http://localhost:8081/api/documents/{documentId}/approve \
  -H "Authorization: Bearer ADMIN_JWT_TOKEN"
```

### 📨 Encrypted Messaging (Port 8082)

#### Send Encrypted Message
```bash
curl -X POST http://localhost:8082/api/messages/send \
  -H "Content-Type: application/json" \
  -d '{
    "senderId": "sender-user-id",
    "recipientId": "recipient-user-id",
    "subject": "Confidential Information",
    "content": "This is a secret message..."
  }'
```

#### Decrypt Message
```bash
curl -X POST http://localhost:8082/api/messages/{messageId}/decrypt \
  -H "Content-Type: application/json" \
  -d '{
    "recipientId": "recipient-user-id"
  }'
```

### 🕵️ Hacker Simulation (Port 8083)

#### Execute Shor's Algorithm Attack
```bash
curl -X POST http://localhost:8083/api/hacker/attack/shor \
  -H "Content-Type: application/json" \
  -d '{
    "targetId": "message-id",
    "algorithm": "RSA_2048"
  }'
```

#### Execute Grover's Algorithm Attack
```bash
curl -X POST http://localhost:8083/api/hacker/attack/grover \
  -H "Content-Type: application/json" \
  -d '{
    "targetId": "message-id",
    "algorithm": "AES_128"
  }'
```

#### Harvest Now, Decrypt Later Demo
```bash
curl -X POST http://localhost:8083/api/hacker/attack/hndl \
  -H "Content-Type: application/json" \
  -d '{
    "targetId": "message-id",
    "algorithm": "RSA_2048",
    "yearsInFuture": 10
  }'
```

#### Get Attack Scenarios
```bash
curl -X GET http://localhost:8083/api/hacker/scenarios
```

---

## 🔐 Cryptographic Algorithms

### Post-Quantum (Quantum-Resistant)
| Algorithm | Standard | Purpose | Security Level |
|-----------|----------|---------|----------------|
| **ML-DSA** (Dilithium3) | FIPS 204 | Digital Signatures | 128-bit PQ |
| **ML-KEM** (Kyber768) | FIPS 203 | Key Encapsulation | 192-bit PQ |

### Classical (Quantum-Vulnerable - for demo)
| Algorithm | Purpose | Quantum Threat |
|-----------|---------|----------------|
| RSA-2048 | Signatures | 🔴 CRITICAL - Broken by Shor's |
| AES-128 | Encryption | 🟡 HIGH - Weakened by Grover's |
| AES-256 | Encryption | 🟢 MEDIUM - Still secure |

---

## 🏗️ Architecture

```
┌─────────────────────────────────────────────────────────────┐
│                    Docker Compose                           │
├─────────────────────────────────────────────────────────────┤
│                                                             │
│  ┌─────────────────┐  ┌─────────────────┐  ┌─────────────┐ │
│  │  User & Doc     │  │   Messaging     │  │   Hacker    │ │
│  │  Service        │  │   Service       │  │   Simulation│ │
│  │  Port 8081      │  │   Port 8082     │  │   Port 8083 │ │
│  │                 │  │                 │  │             │ │
│  │  • Auth/JWT     │  │  • ML-KEM       │  │  • Shor's   │ │
│  │  • ML-DSA       │  │  • AES-256      │  │  • Grover's │ │
│  │  • RSA-2048     │  │  • Encryption   │  │  • HNDL     │ │
│  └────────┬────────┘  └────────┬────────┘  └──────┬──────┘ │
│           │                    │                   │        │
│           └────────────────────┼───────────────────┘        │
│                                │                            │
│                    ┌───────────┴───────────┐                │
│                    │     PostgreSQL        │                │
│                    │     Port 5432         │                │
│                    │                       │                │
│                    │  • users              │                │
│                    │  • documents          │                │
│                    │  • messages           │                │
│                    │  • attack_attempts    │                │
│                    └───────────────────────┘                │
│                                                             │
│                    ┌───────────────────────┐                │
│                    │      pgAdmin          │                │
│                    │      Port 5050        │                │
│                    └───────────────────────┘                │
└─────────────────────────────────────────────────────────────┘
```

---

## 🛡️ User Roles & Permissions

| Role | Register | Create Docs | Sign Docs | Approve/Reject | Manage Users |
|------|----------|-------------|-----------|----------------|--------------|
| **CITIZEN** | ✅ | ✅ | ✅ | ❌ | ❌ |
| **OFFICER** | ✅ | ✅ | ✅ | ✅ | ❌ |
| **ADMIN** | ✅ | ✅ | ✅ | ✅ | ✅ |

---

## 🧪 Development Setup

### Local Development (without Docker)

1. **Start PostgreSQL** (or use H2 for development)
2. **Build the project:**
```bash
mvn clean install -DskipTests
```

3. **Run Service 1:**
```bash
cd pqc-user-document-service
mvn spring-boot:run
```

4. **Run Service 2:**
```bash
cd pqc-messaging-service
mvn spring-boot:run
```

5. **Run Service 3:**
```bash
cd pqc-hacker-simulation
mvn spring-boot:run
```

### Using H2 Database (Development)
Services default to H2 in-memory database when not using Docker profile.
Access H2 console at: http://localhost:8081/h2-console

---

## 🎓 Educational Demonstrations

### 1. Harvest Now, Decrypt Later (HNDL)
Demonstrates the threat where adversaries:
1. 🎯 Intercept encrypted communications TODAY
2. 💾 Store encrypted data indefinitely
3. ⏳ Wait for quantum computers (5-15 years)
4. 🔓 Decrypt all historical data

### 2. Shor's Algorithm
- **Target:** RSA-2048, ECDSA
- **Classical time:** 300+ trillion years
- **Quantum time:** ~8 hours
- **Result:** Complete break of public-key cryptography

### 3. Grover's Algorithm
- **Target:** AES-128, AES-256
- **Effect:** Quadratic speedup (halves key security)
- **AES-128:** 2^128 → 2^64 (VULNERABLE)
- **AES-256:** 2^256 → 2^128 (Still secure)

---

## 📚 Technology Stack

- **Backend:** Java 17, Spring Boot 3.2
- **Security:** Spring Security, JWT
- **Database:** PostgreSQL 16 (Production), H2 (Development)
- **Cryptography:** Bouncy Castle 1.79
- **Containerization:** Docker, Docker Compose
- **Build:** Maven

---

## ⚠️ Disclaimer

This project is for **EDUCATIONAL PURPOSES ONLY** to demonstrate quantum computing threats and the importance of Post-Quantum Cryptography. The hacker simulation service should never be used for malicious purposes.

---

**🔐 Migrate to PQC NOW!** Your data encrypted today may be compromised tomorrow.
