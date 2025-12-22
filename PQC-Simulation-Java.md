# Post-Quantum Cryptography (PQC) Hacking Prevention Simulation - Java Implementation Guide

**Document Version:** 1.0  
**Target Framework:** Java 11+  
**Primary Library:** Bouncy Castle Cryptography 1.79+  
**Date Created:** December 2025  

---

## Table of Contents

1. [Executive Summary](#executive-summary)
2. [Architecture Overview](#architecture-overview)
3. [PQC Algorithms & Classical Fallback](#pqc-algorithms--classical-fallback)
4. [Three Mini-Services Architecture](#three-mini-services-architecture)
5. [Features & Requirements](#features--requirements)
6. [Best Practices & Security Guidelines](#best-practices--security-guidelines)
7. [Implementation Examples](#implementation-examples)
8. [Hacker Simulation Scenarios](#hacker-simulation-scenarios)
9. [Tools & Dependencies](#tools--dependencies)
10. [Testing & Validation](#testing--validation)

---

## Executive Summary

This guide provides a complete blueprint for building a **Post-Quantum Cryptography (PQC) hacking-prevention simulation webapp** in Java. The simulation demonstrates the risks of quantum computing and the effectiveness of quantum-resistant algorithms.

### Key Objectives

- **Digital Signatures:** Use **ML-DSA (CRYSTALS-Dilithium)** for government officers, users, and automated services to sign licenses, permits, housing documents, and registrations
- **Encryption:** Use **ML-KEM (CRYSTALS-Kyber)** for securing transaction data and messaging between officers and users
- **Hybrid Mode:** Fallback to **RSA-2048** and **AES-256** with classical algorithm switching
- **Awareness:** Simulate attacker scenarios where:
  - Malicious software intercepts encrypted messages
  - Quantum computers (via simulated API) attempt to decrypt stored data
  - Timing attacks and side-channel analysis are demonstrated
  - Classical algorithms succumb to quantum algorithms (Shor's algorithm simulation)

### NIST Standardization Status

**August 2024:** NIST finalized three principal PQC standards:
- **ML-KEM (FIPS 203):** Key Encapsulation Mechanism – Kyber
- **ML-DSA (FIPS 204):** Digital Signature Algorithm – Dilithium  
- **SLH-DSA (FIPS 205):** Stateless Hash-Based Digital Signature Algorithm – SPHINCS+

This implementation uses **ML-DSA** and **ML-KEM** as primary algorithms with **AES-256 + RSA-2048** as fallback.

---

## Architecture Overview

```
┌─────────────────────────────────────────────────────────────┐
│         PQC Hacking Prevention Simulation System            │
├─────────────────────────────────────────────────────────────┤
│                                                              │
│  ┌──────────────────┐  ┌──────────────────┐  ┌─────────────┐│
│  │ Service 1        │  │ Service 2        │  │ Service 3   ││
│  │ Document & User  │  │ Transaction &    │  │ Hacker      ││
│  │ Registration     │  │ Messaging        │  │ Simulation  ││
│  │ Service          │  │ Service          │  │ Service     ││
│  └──────────────────┘  └──────────────────┘  └─────────────┘│
│         │                      │                      │      │
│         └──────────┬───────────┴──────────┬───────────┘      │
│                    │                      │                  │
│            ┌───────▼──────────┐  ┌────────▼─────────┐       │
│            │ Crypto Manager   │  │ Algorithm        │       │
│            │ (PQC/Classic)    │  │ Switching Logic  │       │
│            └──────────────────┘  └──────────────────┘       │
│                    │                      │                  │
│         ┌──────────┴──────────────────────┴──────────┐       │
│         │                                             │       │
│    ┌────▼─────┐  ┌────────┐  ┌────────┐  ┌────────┐│       │
│    │ ML-DSA   │  │ML-KEM  │  │ RSA-   │  │ AES-   ││       │
│    │Dilithium │  │ Kyber  │  │ 2048   │  │ 256    ││       │
│    │(Primary) │  │(Primary)  │(Fallback) │(Fallback)       │
│    └──────────┘  └────────┘  └────────┘  └────────┘│       │
│         │             │            │          │      │       │
│         └─────────────┴────────────┴──────────┘      │       │
│                      │                                │       │
│           ┌──────────▼──────────────────────┐        │       │
│           │  Database / Key Storage         │        │       │
│           │  - Identity Hashes (SHA-384)    │        │       │
│           │  - Public Keys                  │        │       │
│           │  - Digital Signatures           │        │       │
│           │  - Encrypted Messages           │        │       │
│           └────────────────────────────────┘        │       │
│                                                      │       │
└──────────────────────────────────────────────────────┘       │
```

### Service Communication Flow

1. **Document Registration → Signature + Encryption**
2. **Officer Approvals → ML-DSA Signature Verification**
3. **Citizen Transactions → ML-KEM Encrypted Channels**
4. **Hacker Interception → Classical Algorithm Decryption Attempts**

---

## PQC Algorithms & Classical Fallback

### 1. Digital Signatures: ML-DSA (CRYSTALS-Dilithium)

**Primary Algorithm:** ML-DSA-65 (equivalent to Dilithium3)
- **Security Level:** 128-bit post-quantum security
- **Signature Size:** ~2,420 bytes
- **Verification Speed:** ~40-50 µs
- **Use Cases:** 
  - License applications (citizen signature)
  - Officer approvals (government signature)
  - Automated service confirmations (system signature)

**Fallback:** RSA-2048
- **Signature Size:** 256 bytes
- **Security Level:** Vulnerable to quantum attacks (classical only)
- **Use When:** ML-DSA verification fails or user chooses classical mode

### 2. Key Encapsulation: ML-KEM (CRYSTALS-Kyber)

**Primary Algorithm:** ML-KEM-768 (equivalent to Kyber768)
- **Security Level:** 192-bit post-quantum security
- **Encapsulated Key Size:** ~1,088 bytes
- **Decapsulation Speed:** ~20-25 µs
- **Use Cases:**
  - Transaction data encryption
  - Officer-to-citizen messaging
  - Document transmission protection

**Fallback:** AES-256 (Symmetric)
- **Key Size:** 32 bytes (256 bits)
- **Block Size:** 16 bytes
- **Use When:** KEM fails or bandwidth constraints exist

### 3. Symmetric Encryption: AES-256

**Configuration:**
- **Mode:** GCM (Galois/Counter Mode) for authenticated encryption
- **IV Size:** 12 bytes (96 bits)
- **Tag Size:** 16 bytes (128 bits)
- **Key Derivation:** PBKDF2 with SHA-256

### 4. Cryptographic Hashing: SHA-384

**Use Cases:**
- Identity/User hash generation
- Message digests for signatures
- KEM shared secret derivation
- Data integrity verification

---

## Three Mini-Services Architecture

### Service 1: Document & User Registration Service

**Responsibilities:**
- User registration (citizen, officer, automated service)
- License/permit/housing application submission
- Document storage and retrieval
- User signature generation and verification

**API Endpoints:**
```java
POST   /api/users/register              // Create user account
POST   /api/documents/apply             // Submit application
POST   /api/documents/:id/sign          // Sign document (user/officer)
GET    /api/documents/:id               // Retrieve document with signatures
POST   /api/documents/:id/verify        // Verify document signatures
PUT    /api/users/:id/toggle-algorithm  // Switch PQC/Classical algorithm
```

**Key Data Models:**
```java
class User {
    String userId;                           // SHA-384 hash
    String role;                             // CITIZEN, OFFICER, SYSTEM
    PublicKey mlDsaPublicKey;               // ML-DSA public key
    PublicKey rsaPublicKey;                 // RSA fallback key
    CryptoAlgorithm preferredAlgorithm;    // PQC or CLASSICAL
    LocalDateTime keyGeneratedAt;
    LocalDateTime lastSignatureAt;
}

class Document {
    String documentId;                       // SHA-384 hash
    String documentType;                     // LICENSE, PERMIT, HOUSING, etc.
    String userId;                           // Applicant user ID
    String content;                          // Document content
    Map<String, Signature> signatures;      // userId -> signature mapping
    Map<String, LocalDateTime> signedAt;    // userId -> timestamp
    CryptoAlgorithm algorithmUsed;         // Algorithm for each signature
    LocalDateTime createdAt;
    LocalDateTime lastModifiedAt;
}

class Signature {
    byte[] signatureBytes;
    CryptoAlgorithm algorithm;              // ML_DSA, RSA
    LocalDateTime createdAt;
    String signerUserId;
}
```

---

### Service 2: Transaction & Messaging Service

**Responsibilities:**
- Encrypt officer-to-citizen communications
- Encrypt transaction data (fees, approvals, status updates)
- Manage encrypted message queues
- Decrypt messages only by intended recipient

**API Endpoints:**
```java
POST   /api/messages/encrypt             // Encrypt message to recipient
POST   /api/messages/decrypt             // Decrypt received message
GET    /api/messages/inbox               // List encrypted messages
POST   /api/transactions/create          // Create encrypted transaction record
GET    /api/transactions/:id             // Retrieve and decrypt transaction
POST   /api/messages/:id/toggle-kem      // Switch KEM/AES encryption
```

**Key Data Models:**
```java
class EncryptedMessage {
    String messageId;                        // SHA-384 hash
    String senderId;                         // User ID of sender
    String recipientId;                      // User ID of recipient
    byte[] encryptedContent;                // KEM-encapsulated + encrypted payload
    byte[] iv;                               // Initialization vector
    byte[] authTag;                          // Authentication tag (GCM)
    CryptoAlgorithm kemAlgorithm;           // ML_KEM or AES
    LocalDateTime createdAt;
    boolean decryptedSuccessfully;
}

class Transaction {
    String transactionId;                    // SHA-384 hash
    String citizenId;                        // Citizen involved
    String officerId;                        // Officer approving
    String description;                      // Transaction details
    byte[] encryptedData;                   // KEM-encrypted payload
    byte[] publicKeyShare;                   // Encapsulated shared secret
    CryptoAlgorithm encryptionAlgorithm;   // Algorithm used
    TransactionStatus status;               // PENDING, APPROVED, REJECTED
    LocalDateTime createdAt;
}

class KEMCiphertext {
    byte[] encapsulatedKey;                 // KEM public key ciphertext
    byte[] symmetricCiphertext;             // AES-GCM encrypted data
    byte[] iv;                              // 12-byte IV
    byte[] authTag;                         // 16-byte auth tag
}
```

---

### Service 3: Hacker Simulation Service

**Responsibilities:**
- Simulate malicious software intercepting encrypted data
- Simulate quantum computer API for decryption attempts
- Demonstrate timing attacks
- Show classical vs. quantum attack scenarios
- Visualize successful and failed decryption attempts

**API Endpoints:**
```java
POST   /api/hacker/intercept-message     // Intercept encrypted message
POST   /api/hacker/quantum-break         // Attempt quantum decryption (RSA/AES)
POST   /api/hacker/timing-attack         // Timing side-channel analysis
GET    /api/hacker/attack-history        // List all attack attempts
POST   /api/hacker/simulate-scenario      // Run attack scenario
POST   /api/hacker/crack-rsa              // Simulate Shor's algorithm (factorize)
POST   /api/hacker/crack-aes              // Simulate Grover's algorithm (search)
```

**Key Data Models:**
```java
class InterceptedMessage {
    String interceptId;                      // SHA-384 hash
    String originalMessageId;                // Target message ID
    byte[] interceptedData;                  // Captured encrypted content
    CryptoAlgorithm targetAlgorithm;        // Algorithm used in message
    LocalDateTime interceptedAt;
    List<AttackAttempt> attempts;           // All decryption attempts on this data
}

class AttackAttempt {
    String attemptId;                        // SHA-384 hash
    String attackType;                       // SHOR_RSA, GROVER_AES, TIMING, CLASSICAL_BRUTE
    AttackStatus status;                     // IN_PROGRESS, SUCCESS, FAILED, TIME_EXCEEDED
    long executionTimeMs;                    // Time spent on attack
    long estimatedCompleteTimeMs;            // Estimated time for classical algorithms
    String resultDescription;                 // Detailed result message
    byte[] recoveredPlaintext;              // Only if status == SUCCESS
    LocalDateTime attemptedAt;
    int quantumResourcesRequired;            // Simulated quantum gates (for education)
}

class TimingAnalysis {
    String analysisId;                       // SHA-384 hash
    List<Long> decryptionTimes;             // Time samples (in nanoseconds)
    double variance;                         // Statistical variance
    boolean vulnerableToTiming;             // Timing pattern detectable?
    String vulnerability Description;       // Educational explanation
}

class AttackScenario {
    String scenarioId;
    String name;                             // "Harvest-Now-Decrypt-Later", etc.
    String description;
    CryptoAlgorithm targetAlgorithm;
    AttackType attackType;
    boolean quantumComputerRequired;
    long estimatedBreakTimeClassical;        // In seconds (classical computer)
    long estimatedBreakTimeQuantum;          // In seconds (quantum computer)
    String successCriteria;
    String educationalLesson;
}
```

---

## Features & Requirements

### Feature Matrix

| Feature | Service 1 | Service 2 | Service 3 |
|---------|-----------|-----------|-----------|
| **User Management** | ✓ | ✓ |  |
| **Document Signing (ML-DSA)** | ✓ |  |  |
| **RSA Fallback Signatures** | ✓ |  |  |
| **Message Encryption (ML-KEM)** |  | ✓ |  |
| **AES Fallback Encryption** |  | ✓ |  |
| **Algorithm Switching** | ✓ | ✓ |  |
| **Identity Hashing (SHA-384)** | ✓ | ✓ |  |
| **Message Interception** |  |  | ✓ |
| **Quantum Decryption Simulation** |  |  | ✓ |
| **Timing Attack Simulation** |  |  | ✓ |
| **Shor's Algorithm (RSA break)** |  |  | ✓ |
| **Grover's Algorithm (AES break)** |  |  | ✓ |
| **Attack History/Logging** |  |  | ✓ |

### System Requirements

**Hardware:**
- Minimum: Intel Core i5 / AMD Ryzen 5 (2GHz+)
- Recommended: Intel Core i7 / AMD Ryzen 7 (3GHz+)
- RAM: Minimum 4GB, Recommended 8GB+
- Storage: 2GB for application + logs

**Software:**
- **JDK:** OpenJDK 11+ or Oracle JDK 11+
- **Build Tool:** Maven 3.6+ or Gradle 7.0+
- **Database:** PostgreSQL 12+ (optional, embedded H2 for demo)
- **Web Framework:** Spring Boot 2.7+ / Quarkus 2.10+
- **Web Server:** Embedded Tomcat/Undertow

**Dependencies:**

```xml
<!-- pom.xml -->
<dependency>
    <groupId>org.bouncycastle</groupId>
    <artifactId>bcprov-jdk15on</artifactId>
    <version>1.79</version>
</dependency>

<dependency>
    <groupId>org.bouncycastle</groupId>
    <artifactId>bctsp-jdk15on</artifactId>
    <version>1.79</version>
</dependency>

<!-- For PQC support (ML-DSA, ML-KEM) -->
<dependency>
    <groupId>org.bouncycastle</groupId>
    <artifactId>bcpqc-jdk15on</artifactId>
    <version>1.79</version>
</dependency>

<!-- Web Framework -->
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-web</artifactId>
    <version>2.7.10</version>
</dependency>

<!-- Database -->
<dependency>
    <groupId>org.postgresql</groupId>
    <artifactId>postgresql</artifactId>
    <version>42.5.1</version>
</dependency>

<!-- Logging -->
<dependency>
    <groupId>org.slf4j</groupId>
    <artifactId>slf4j-api</artifactId>
    <version>2.0.4</version>
</dependency>

<!-- Testing -->
<dependency>
    <groupId>junit</groupId>
    <artifactId>junit</artifactId>
    <version>4.13.2</version>
    <scope>test</scope>
</dependency>
```

---

## Best Practices & Security Guidelines

### 1. Key Generation & Storage

**DO:**
```java
✓ Generate keys in secure HSM or memory-protected regions
✓ Use SecureRandom from Bouncy Castle
✓ Store private keys in encrypted key stores (PKCS#12 with strong password)
✓ Rotate keys annually or per security policy
✓ Use separate key pairs for signing and encryption
✓ Implement key versioning with timestamps
```

**DON'T:**
```java
✗ Store private keys in plain text in files/databases
✗ Use weak random number generators (Math.random())
✗ Reuse keys across multiple purposes
✗ Log or display private key bytes
✗ Share private keys between services
```

### 2. Hybrid Cryptography Implementation

**Best Practice Pattern:**

```java
class HybridCryptoManager {
    enum CryptoMode { PQC_PRIMARY, CLASSICAL_FALLBACK, HYBRID }
    
    private CryptoMode mode;
    
    // Always try PQC first, fall back to classical if needed
    public byte[] encryptHybrid(byte[] plaintext, PublicKey recipientKey) {
        try {
            // Attempt ML-KEM encryption first
            return encryptWithMLKEM(plaintext, recipientKey);
        } catch (Exception e) {
            logger.warn("ML-KEM encryption failed, falling back to AES-256", e);
            return encryptWithAES256(plaintext);
        }
    }
    
    public byte[] decryptHybrid(byte[] ciphertext, PrivateKey key) {
        // Try to detect which algorithm was used
        try {
            // First, assume ML-KEM encapsulation
            return decryptMLKEM(ciphertext, key);
        } catch (Exception e) {
            logger.warn("ML-KEM decryption failed, attempting AES-256", e);
            return decryptAES256(ciphertext);
        }
    }
}
```

### 3. Message Authentication & Integrity

**Pattern:**
```java
// Always use authenticated encryption (GCM mode)
// Verify signatures before processing documents
// Include timestamps in all cryptographic operations
// Hash sensitive data (user IDs, document IDs) with SHA-384
```

### 4. Timing Attack Mitigation

**Pattern:**
```java
// Use constant-time comparison for cryptographic values
private boolean constantTimeEquals(byte[] a, byte[] b) {
    if (a.length != b.length) return false;
    int result = 0;
    for (int i = 0; i < a.length; i++) {
        result |= a[i] ^ b[i];
    }
    return result == 0;
}

// Add random delays to obscure operation timing
private void addRandomDelay(int minMs, int maxMs) {
    int delay = ThreadLocalRandom.current().nextInt(minMs, maxMs + 1);
    try { Thread.sleep(delay); } catch (InterruptedException e) {}
}
```

### 5. Quantum-Safe Transition Strategy

**Phase 1 (Now):**
- Deploy ML-DSA and ML-KEM as primary
- Maintain RSA-2048 and AES-256 as fallback
- Log which algorithm was used for each operation

**Phase 2 (6-12 months):**
- Require ML-DSA for all new documents/signatures
- Migrate existing RSA signatures to ML-DSA (re-sign)
- Deprecate RSA for new operations

**Phase 3 (12-24 months):**
- Complete migration of all signatures to ML-DSA
- Retain AES-256 for symmetric encryption (quantum-safe with 256-bit keys)
- Phase out RSA completely

### 6. Harvest-Now-Decrypt-Later (HNDL) Prevention

**Threat Model:**
- Attacker captures encrypted data today
- Attacker waits for quantum computer to become available
- Attacker decrypts data retroactively

**Mitigation:**
```java
// Use ML-KEM now to protect against future quantum attacks
// Assume all unencrypted data can be harvested
// Encrypt all sensitive data before transmission
// Include explicit algorithm identifier in ciphertext
// Enable algorithm downgrade detection
```

---

## Implementation Examples

### Example 1: ML-DSA Key Generation and Signature

```java
import org.bouncycastle.pqc.jcajce.provider.BouncyCastlePQCProvider;
import org.bouncycastle.pqc.jcajce.spec.MLDSAParameterSpec;
import java.security.*;

public class MLDSASignatureExample {
    
    static {
        Security.addProvider(new BouncyCastlePQCProvider());
    }
    
    // Generate ML-DSA key pair
    public static KeyPair generateMLDSAKeyPair() throws Exception {
        KeyPairGenerator kpg = KeyPairGenerator.getInstance("ML-DSA", "BCPQC");
        kpg.initialize(MLDSAParameterSpec.ml_dsa_65); // 128-bit security level
        return kpg.generateKeyPair();
    }
    
    // Sign a document
    public static byte[] signDocument(
        byte[] documentContent,
        PrivateKey privateKey
    ) throws Exception {
        Signature signer = Signature.getInstance("ML-DSA", "BCPQC");
        signer.initSign(privateKey);
        signer.update(documentContent);
        return signer.sign();
    }
    
    // Verify a signature
    public static boolean verifySignature(
        byte[] documentContent,
        byte[] signature,
        PublicKey publicKey
    ) throws Exception {
        Signature verifier = Signature.getInstance("ML-DSA", "BCPQC");
        verifier.initVerify(publicKey);
        verifier.update(documentContent);
        return verifier.verify(signature);
    }
}
```

**Usage:**
```java
// Generate keys
KeyPair keyPair = MLDSASignatureExample.generateMLDSAKeyPair();
byte[] documentBytes = "License Application #12345".getBytes();

// Sign
byte[] signature = MLDSASignatureExample.signDocument(
    documentBytes,
    keyPair.getPrivate()
);
System.out.println("Signature size: " + signature.length + " bytes");

// Verify
boolean isValid = MLDSASignatureExample.verifySignature(
    documentBytes,
    signature,
    keyPair.getPublic()
);
System.out.println("Signature valid: " + isValid);
```

---

### Example 2: ML-KEM Encryption and Decryption

```java
import org.bouncycastle.pqc.jcajce.provider.BouncyCastlePQCProvider;
import org.bouncycastle.pqc.jcajce.spec.MLKEMParameterSpec;
import javax.crypto.Cipher;
import java.security.*;

public class MLKEMEncryptionExample {
    
    static {
        Security.addProvider(new BouncyCastlePQCProvider());
    }
    
    // Generate ML-KEM key pair
    public static KeyPair generateMLKEMKeyPair() throws Exception {
        KeyPairGenerator kpg = KeyPairGenerator.getInstance("ML-KEM", "BCPQC");
        kpg.initialize(MLKEMParameterSpec.ml_kem_768); // 192-bit security level
        return kpg.generateKeyPair();
    }
    
    // Encapsulate shared secret (sender side)
    public static KemResult encapsulate(PublicKey publicKey) throws Exception {
        Cipher cipher = Cipher.getInstance("ML-KEM", "BCPQC");
        cipher.init(Cipher.ENCRYPT_MODE, publicKey);
        
        byte[] encapsulatedKey = cipher.doFinal();
        byte[] sharedSecret = cipher.getIV(); // 32-byte shared secret
        
        return new KemResult(encapsulatedKey, sharedSecret);
    }
    
    // Decapsulate shared secret (recipient side)
    public static byte[] decapsulate(
        byte[] encapsulatedKey,
        PrivateKey privateKey
    ) throws Exception {
        Cipher cipher = Cipher.getInstance("ML-KEM", "BCPQC");
        cipher.init(Cipher.DECRYPT_MODE, privateKey, 
            new IvParameterSpec(new byte[12]));
        return cipher.doFinal(encapsulatedKey);
    }
    
    // Encrypt message using derived symmetric key
    public static byte[] encryptMessageWithDerivedKey(
        byte[] message,
        byte[] sharedSecret
    ) throws Exception {
        // Derive symmetric key from shared secret using SHA-256
        MessageDigest digest = MessageDigest.getInstance("SHA-256");
        byte[] symmetricKey = digest.digest(sharedSecret);
        
        // Use AES-256-GCM for message encryption
        Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
        SecureRandom random = new SecureRandom();
        byte[] iv = new byte[12];
        random.nextBytes(iv);
        
        GCMParameterSpec gcmSpec = new GCMParameterSpec(128, iv);
        SecretKeySpec keySpec = new SecretKeySpec(symmetricKey, 0, 32, "AES");
        
        cipher.init(Cipher.ENCRYPT_MODE, keySpec, gcmSpec);
        byte[] ciphertext = cipher.doFinal(message);
        
        // Return: IV + ciphertext
        byte[] result = new byte[iv.length + ciphertext.length];
        System.arraycopy(iv, 0, result, 0, iv.length);
        System.arraycopy(ciphertext, 0, result, iv.length, ciphertext.length);
        
        return result;
    }
    
    static class KemResult {
        public byte[] encapsulatedKey;
        public byte[] sharedSecret;
        
        public KemResult(byte[] encapsulatedKey, byte[] sharedSecret) {
            this.encapsulatedKey = encapsulatedKey;
            this.sharedSecret = sharedSecret;
        }
    }
}
```

**Usage:**
```java
// Generate keys
KeyPair keyPair = MLKEMEncryptionExample.generateMLKEMKeyPair();
byte[] messageBytes = "Confidential transaction data".getBytes();

// Encapsulate (sender)
KemResult result = MLKEMEncryptionExample.encapsulate(keyPair.getPublic());
byte[] sharedSecret = result.sharedSecret;
byte[] encapsulatedKey = result.encapsulatedKey;

// Encrypt message
byte[] encryptedMessage = MLKEMEncryptionExample.encryptMessageWithDerivedKey(
    messageBytes,
    sharedSecret
);

// Decapsulate (recipient)
byte[] decapsulatedSecret = MLKEMEncryptionExample.decapsulate(
    encapsulatedKey,
    keyPair.getPrivate()
);

// Verify shared secrets match
boolean secretMatch = Arrays.equals(sharedSecret, decapsulatedSecret);
System.out.println("Shared secrets match: " + secretMatch);
```

---

### Example 3: Hybrid Algorithm Switching

```java
public class HybridCryptoService {
    
    enum Algorithm { ML_DSA, RSA_2048, ML_KEM, AES_256 }
    
    private Map<String, Algorithm> userAlgorithmPreferences = new ConcurrentHashMap<>();
    
    // Get preferred algorithm for user
    public Algorithm getPreferredSignatureAlgorithm(String userId) {
        return userAlgorithmPreferences.getOrDefault(userId, Algorithm.ML_DSA);
    }
    
    public Algorithm getPreferredEncryptionAlgorithm(String userId) {
        return userAlgorithmPreferences.getOrDefault(userId, Algorithm.ML_KEM);
    }
    
    // Sign with preferred algorithm
    public byte[] signDocumentWithPreference(
        String userId,
        byte[] documentContent,
        PrivateKey userPrivateKey
    ) throws Exception {
        Algorithm algo = getPreferredSignatureAlgorithm(userId);
        
        switch(algo) {
            case ML_DSA:
                return signWithMLDSA(documentContent, userPrivateKey);
            case RSA_2048:
                return signWithRSA(documentContent, userPrivateKey);
            default:
                throw new IllegalArgumentException("Unknown algorithm: " + algo);
        }
    }
    
    private byte[] signWithMLDSA(byte[] content, PrivateKey privateKey) throws Exception {
        Signature signer = Signature.getInstance("ML-DSA", "BCPQC");
        signer.initSign(privateKey);
        signer.update(content);
        return signer.sign();
    }
    
    private byte[] signWithRSA(byte[] content, PrivateKey privateKey) throws Exception {
        Signature signer = Signature.getInstance("SHA256withRSA");
        signer.initSign(privateKey);
        signer.update(content);
        return signer.sign();
    }
    
    // Switch user's preferred algorithm
    public void switchAlgorithmPreference(String userId, Algorithm algorithm) {
        userAlgorithmPreferences.put(userId, algorithm);
        logger.info("User {} switched to algorithm: {}", userId, algorithm);
    }
    
    // Toggle between PQC and Classical for awareness demonstration
    public void toggleQuantumSafety(String userId, boolean enablePQC) {
        Algorithm prefSig = enablePQC ? Algorithm.ML_DSA : Algorithm.RSA_2048;
        Algorithm prefEnc = enablePQC ? Algorithm.ML_KEM : Algorithm.AES_256;
        
        userAlgorithmPreferences.put(userId + "_sig", prefSig);
        userAlgorithmPreferences.put(userId + "_enc", prefEnc);
    }
}
```

---

### Example 4: Identity Hashing (SHA-384)

```java
public class IdentityHashService {
    
    /**
     * Generate consistent identity hash for user
     * Uses: userId + emailAddress + dateOfBirth
     */
    public static String generateUserIdentityHash(
        String userId,
        String emailAddress,
        LocalDate dateOfBirth
    ) throws NoSuchAlgorithmException {
        
        MessageDigest digest = MessageDigest.getInstance("SHA-384");
        
        String combined = userId + "|" + emailAddress + "|" + 
            dateOfBirth.toString();
        
        byte[] hashBytes = digest.digest(combined.getBytes(StandardCharsets.UTF_8));
        
        // Convert to hex string
        StringBuilder hexString = new StringBuilder();
        for (byte b : hashBytes) {
            String hex = Integer.toHexString(0xff & b);
            if (hex.length() == 1) hexString.append('0');
            hexString.append(hex);
        }
        
        return hexString.toString();
    }
    
    /**
     * Generate document ID hash
     * Uses: documentType + userId + timestamp + randomNonce
     */
    public static String generateDocumentIdHash(
        String documentType,
        String userId,
        LocalDateTime timestamp,
        String nonce
    ) throws NoSuchAlgorithmException {
        
        MessageDigest digest = MessageDigest.getInstance("SHA-384");
        
        String combined = documentType + "|" + userId + "|" + 
            timestamp.toString() + "|" + nonce;
        
        byte[] hashBytes = digest.digest(combined.getBytes(StandardCharsets.UTF_8));
        
        return bytesToHex(hashBytes);
    }
    
    private static String bytesToHex(byte[] bytes) {
        StringBuilder hexString = new StringBuilder();
        for (byte b : bytes) {
            String hex = Integer.toHexString(0xff & b);
            if (hex.length() == 1) hexString.append('0');
            hexString.append(hex);
        }
        return hexString.toString();
    }
}
```

**Usage:**
```java
String userHash = IdentityHashService.generateUserIdentityHash(
    "USER123",
    "john@example.com",
    LocalDate.of(1990, 5, 15)
);
System.out.println("User Identity Hash: " + userHash);

String docHash = IdentityHashService.generateDocumentIdHash(
    "LICENSE",
    "USER123",
    LocalDateTime.now(),
    "nonce-random-value"
);
System.out.println("Document ID Hash: " + docHash);
```

---

### Example 5: Hacker Simulation - Shor's Algorithm (RSA Breaking)

```java
public class HackerSimulationService {
    
    /**
     * Simulate Shor's Algorithm attack on RSA-2048
     * Educational demonstration of quantum threat
     */
    public AttackResult simulateShorAlgorithmAttack(
        byte[] ciphertext,
        RSAPublicKey publicKey
    ) throws Exception {
        
        long startTime = System.currentTimeMillis();
        
        // Simulate quantum gate operations
        int keyBitLength = publicKey.getModulus().bitLength();
        long estimatedQuantumGates = (long) Math.pow(keyBitLength, 2.373); // ~2000 gates for RSA-2048
        
        // For demonstration, use simulated quantum computer API
        // In reality, this would require actual quantum hardware
        QuantumComputerSimulator quantumSim = new QuantumComputerSimulator();
        BigInteger p = quantumSim.findPrimeFactors(
            publicKey.getModulus(),
            keyBitLength,
            estimatedQuantumGates
        );
        
        long executionTimeMs = System.currentTimeMillis() - startTime;
        
        // Derive private exponent
        BigInteger q = publicKey.getModulus().divide(p);
        BigInteger phi = (p.subtract(BigInteger.ONE))
            .multiply(q.subtract(BigInteger.ONE));
        BigInteger d = publicKey.getPublicExponent()
            .modInverse(phi);
        
        // Decrypt ciphertext
        BigInteger c = new BigInteger(ciphertext);
        byte[] plaintext = c.modPow(d, publicKey.getModulus()).toByteArray();
        
        return new AttackResult(
            AttackType.SHOR_RSA,
            true,
            plaintext,
            executionTimeMs,
            estimatedQuantumGates,
            "Successfully factorized RSA-2048 using Shor's algorithm"
        );
    }
    
    /**
     * Simulate Grover's Algorithm attack on AES-256
     * Shows quantum speedup on symmetric encryption
     */
    public AttackResult simulateGroverAlgorithmAttack(
        byte[] ciphertext,
        byte[] iv,
        String plaintextPattern
    ) throws Exception {
        
        long startTime = System.currentTimeMillis();
        
        // Classical AES search: O(2^256) operations
        // Grover's algorithm: O(2^128) quantum operations
        long classicalComplexity = (long) Math.pow(2, 256);
        long quantumComplexity = (long) Math.pow(2, 128);
        
        // For demonstration, simulate probability-based search
        QuantumComputerSimulator quantumSim = new QuantumComputerSimulator();
        byte[] recoveredKey = quantumSim.searchKeyspace(
            ciphertext,
            iv,
            plaintextPattern,
            quantumComplexity
        );
        
        long executionTimeMs = System.currentTimeMillis() - startTime;
        
        return new AttackResult(
            AttackType.GROVER_AES,
            true,
            recoveredKey,
            executionTimeMs,
            quantumComplexity,
            "Grover's algorithm would reduce AES-256 search from 2^256 to 2^128"
        );
    }
    
    static class AttackResult {
        public AttackType type;
        public boolean successful;
        public byte[] recoveredData;
        public long executionTimeMs;
        public long estimatedQuantumGates;
        public String description;
        
        public AttackResult(
            AttackType type,
            boolean successful,
            byte[] recoveredData,
            long executionTimeMs,
            long estimatedQuantumGates,
            String description
        ) {
            this.type = type;
            this.successful = successful;
            this.recoveredData = recoveredData;
            this.executionTimeMs = executionTimeMs;
            this.estimatedQuantumGates = estimatedQuantumGates;
            this.description = description;
        }
    }
    
    enum AttackType {
        SHOR_RSA,           // Shor's algorithm for RSA factorization
        GROVER_AES,         // Grover's algorithm for AES key search
        TIMING_ATTACK,      // Side-channel timing analysis
        CLASSICAL_BRUTE,    // Classical brute force (for reference)
        HARVEST_NOW_DECRYPT_LATER
    }
}
```

---

### Example 6: Timing Attack Simulation

```java
public class TimingAttackSimulator {
    
    /**
     * Analyze decryption timing to detect algorithm vulnerabilities
     * Educational demonstration of side-channel attacks
     */
    public TimingAnalysisResult analyzeDecryptionTiming(
        Function<byte[], Long> decryptOperation,
        byte[] testCiphertexts[],
        int sampleCount
    ) throws Exception {
        
        List<Long> timingMeasurements = new ArrayList<>();
        
        for (int sample = 0; sample < sampleCount; sample++) {
            for (byte[] ciphertext : testCiphertexts) {
                long startTime = System.nanoTime();
                decryptOperation.apply(ciphertext);
                long endTime = System.nanoTime();
                
                timingMeasurements.add(endTime - startTime);
            }
        }
        
        // Statistical analysis
        double mean = timingMeasurements.stream()
            .mapToLong(Long::longValue)
            .average()
            .orElse(0);
        
        double variance = timingMeasurements.stream()
            .mapToDouble(t -> Math.pow(t - mean, 2))
            .average()
            .orElse(0);
        
        double stdDeviation = Math.sqrt(variance);
        double coefficientOfVariation = stdDeviation / mean; // Lower = more constant-time
        
        // Determine vulnerability
        boolean vulnerableToTiming = coefficientOfVariation > 0.05; // >5% variation = vulnerable
        
        String analysis = generateTimingAnalysis(
            mean,
            variance,
            stdDeviation,
            coefficientOfVariation,
            vulnerableToTiming
        );
        
        return new TimingAnalysisResult(
            timingMeasurements,
            mean,
            variance,
            stdDeviation,
            coefficientOfVariation,
            vulnerableToTiming,
            analysis
        );
    }
    
    private String generateTimingAnalysis(
        double mean,
        double variance,
        double stdDev,
        double cv,
        boolean vulnerable
    ) {
        StringBuilder analysis = new StringBuilder();
        
        analysis.append("=== TIMING ANALYSIS REPORT ===\n");
        analysis.append(String.format("Mean execution time: %.2f ns\n", mean));
        analysis.append(String.format("Variance: %.2f\n", variance));
        analysis.append(String.format("Standard Deviation: %.2f ns\n", stdDev));
        analysis.append(String.format("Coefficient of Variation: %.4f (%.2f%%)\n", cv, cv * 100));
        
        if (vulnerable) {
            analysis.append("\n⚠️  VULNERABLE TO TIMING ATTACKS\n");
            analysis.append("The decryption operation shows significant timing variation,\n");
            analysis.append("which could allow an attacker to infer information about\n");
            analysis.append("the decryption key or plaintext through statistical analysis\n");
            analysis.append("of execution times.\n\n");
            analysis.append("MITIGATION: Use constant-time comparison functions,\n");
            analysis.append("add random delays, and implement algorithmic hardening.\n");
        } else {
            analysis.append("\n✓ CONSTANT-TIME RESISTANT\n");
            analysis.append("The decryption operation shows minimal timing variation,\n");
            analysis.append("making it resistant to timing-based side-channel attacks.\n");
        }
        
        return analysis.toString();
    }
    
    static class TimingAnalysisResult {
        public List<Long> allTimings;
        public double mean;
        public double variance;
        public double stdDeviation;
        public double coefficientOfVariation;
        public boolean vulnerableToTiming;
        public String analysis;
        
        public TimingAnalysisResult(
            List<Long> allTimings,
            double mean,
            double variance,
            double stdDeviation,
            double coefficientOfVariation,
            boolean vulnerableToTiming,
            String analysis
        ) {
            this.allTimings = allTimings;
            this.mean = mean;
            this.variance = variance;
            this.stdDeviation = stdDeviation;
            this.coefficientOfVariation = coefficientOfVariation;
            this.vulnerableToTiming = vulnerableToTiming;
            this.analysis = analysis;
        }
    }
}
```

---

## Hacker Simulation Scenarios

### Scenario 1: Harvest-Now-Decrypt-Later (HNDL)

**Setup:**
```
1. Citizen applies for housing license in 2025
2. Government encrypts housing records with classical RSA-2048
3. Hacker intercepts encrypted data
4. Hacker waits for quantum computer (assume 2030)
5. Hacker uses Shor's algorithm to break RSA
6. Hacker decrypts sensitive housing data retroactively
```

**Simulation Code:**
```java
public class HarvestNowDecryptLaterScenario {
    
    public void runScenario() throws Exception {
        // 2025: Citizen submits housing application
        String housingApplication = "HOUSING_RECORD_001: John Doe, " +
            "123 Main St, Bangkok, 10100";
        
        // Government encrypts with RSA-2048 (vulnerable!)
        KeyPair rsaKeys = generateRSA2048Keys();
        byte[] encryptedData = encryptWithRSA(
            housingApplication.getBytes(),
            rsaKeys.getPublic()
        );
        
        System.out.println("[2025] Housing record encrypted with RSA-2048");
        System.out.println("Ciphertext size: " + encryptedData.length + " bytes");
        
        // Hacker intercepts encrypted data
        byte[] interceptedCiphertext = encryptedData;
        System.out.println("[2025] Hacker intercepts encrypted housing record");
        
        // Simulate waiting 5 years for quantum computer
        System.out.println("[2030] Quantum computer becomes available...");
        System.out.println("[2030] Hacker initiates Shor's algorithm attack");
        
        // Attack on RSA
        HackerSimulationService hacker = new HackerSimulationService();
        AttackResult result = hacker.simulateShorAlgorithmAttack(
            interceptedCiphertext,
            (RSAPublicKey) rsaKeys.getPublic()
        );
        
        System.out.println("[2030] Attack successful: " + result.successful);
        System.out.println("Execution time: " + result.executionTimeMs + " ms");
        System.out.println("Estimated quantum gates: " + 
            result.estimatedQuantumGates);
        System.out.println("Recovered plaintext: " + 
            new String(result.recoveredData));
        
        // NOW with ML-KEM (quantum-safe)
        System.out.println("\n=== QUANTUM-SAFE ALTERNATIVE ===");
        System.out.println("[2025] Government encrypts with ML-KEM (quantum-safe)");
        
        KeyPair kemKeys = generateMLKEMKeyPair();
        byte[] quantumSafeEncrypted = encryptWithMLKEM(
            housingApplication.getBytes(),
            kemKeys.getPublic()
        );
        
        System.out.println("[2030] Hacker attempts Shor's algorithm...");
        System.out.println("Result: FAILED - ML-KEM is lattice-based, not vulnerable");
        System.out.println("Housing record remains secure even in 2030!");
    }
}
```

---

### Scenario 2: Classical vs. Quantum Algorithm Performance

**Comparison:**
```
┌──────────────────────────────────────────────────────────┐
│ ALGORITHM COMPARISON                                      │
├──────────────────────────────────────────────────────────┤
│                                                            │
│ CLASSICAL ALGORITHMS (Vulnerable to Quantum):             │
│ ┌──────────────────────────────────────────────────────┐ │
│ │ RSA-2048:                                            │ │
│ │ - Break time (classical): 300,000+ years             │ │
│ │ - Break time (quantum/Shor): ~8 hours                │ │
│ │ - Threat level: CRITICAL (post-quantum era)          │ │
│ └──────────────────────────────────────────────────────┘ │
│                                                            │
│ │ AES-256 (Symmetric):                                  │ │
│ │ - Break time (classical): 2^256 attempts (~years)    │ │
│ │ - Break time (quantum/Grover): ~2^128 ops (~seconds) │ │
│ │ - Threat level: REDUCED (still secure with 256 bits) │ │
│ └──────────────────────────────────────────────────────┘ │
│                                                            │
│ QUANTUM-SAFE ALGORITHMS (Lattice-Based):                  │
│ ┌──────────────────────────────────────────────────────┐ │
│ │ ML-DSA (Digital Signatures):                         │ │
│ │ - Break time (quantum): Intractable (MLWE hard)      │ │
│ │ - Security: 128-bit (post-quantum)                   │ │
│ │ - Threat level: MINIMAL (future-proof)               │ │
│ └──────────────────────────────────────────────────────┘ │
│                                                            │
│ │ ML-KEM (Key Encapsulation):                          │ │
│ │ - Break time (quantum): Intractable (MLWE hard)      │ │
│ │ - Security: 192-bit (post-quantum)                   │ │
│ │ - Threat level: MINIMAL (future-proof)               │ │
│ └──────────────────────────────────────────────────────┘ │
└──────────────────────────────────────────────────────────┘
```

---

## Tools & Dependencies

### Build Configuration (Maven pom.xml)

```xml
<project>
    <modelVersion>4.0.0</modelVersion>
    <groupId>com.pqc.simulation</groupId>
    <artifactId>pqc-hacking-prevention-java</artifactId>
    <version>1.0.0</version>
    <packaging>jar</packaging>
    
    <properties>
        <maven.compiler.source>11</maven.compiler.source>
        <maven.compiler.target>11</maven.compiler.target>
        <project.build.sourceEncoding>UTF-8</project.build.sourceEncoding>
        
        <spring-boot.version>2.7.10</spring-boot.version>
        <bouncycastle.version>1.79</bouncycastle.version>
        <junit.version>4.13.2</junit.version>
        <postgresql.version>42.5.1</postgresql.version>
    </properties>
    
    <dependencies>
        <!-- Bouncy Castle PQC -->
        <dependency>
            <groupId>org.bouncycastle</groupId>
            <artifactId>bcprov-jdk15on</artifactId>
            <version>${bouncycastle.version}</version>
        </dependency>
        
        <dependency>
            <groupId>org.bouncycastle</groupId>
            <artifactId>bcpqc-jdk15on</artifactId>
            <version>${bouncycastle.version}</version>
        </dependency>
        
        <!-- Spring Boot -->
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-web</artifactId>
            <version>${spring-boot.version}</version>
        </dependency>
        
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-data-jpa</artifactId>
            <version>${spring-boot.version}</version>
        </dependency>
        
        <!-- Database -->
        <dependency>
            <groupId>org.postgresql</groupId>
            <artifactId>postgresql</artifactId>
            <version>${postgresql.version}</version>
            <scope>runtime</scope>
        </dependency>
        
        <!-- H2 for testing/demo -->
        <dependency>
            <groupId>com.h2database</groupId>
            <artifactId>h2</artifactId>
            <version>2.1.214</version>
            <scope>runtime</scope>
        </dependency>
        
        <!-- JSON Processing -->
        <dependency>
            <groupId>com.google.code.gson</groupId>
            <artifactId>gson</artifactId>
            <version>2.10.1</version>
        </dependency>
        
        <!-- Logging -->
        <dependency>
            <groupId>org.slf4j</groupId>
            <artifactId>slf4j-api</artifactId>
            <version>2.0.4</version>
        </dependency>
        
        <!-- Testing -->
        <dependency>
            <groupId>junit</groupId>
            <artifactId>junit</artifactId>
            <version>${junit.version}</version>
            <scope>test</scope>
        </dependency>
    </dependencies>
    
    <build>
        <plugins>
            <plugin>
                <groupId>org.springframework.boot</groupId>
                <artifactId>spring-boot-maven-plugin</artifactId>
                <version>${spring-boot.version}</version>
            </plugin>
        </plugins>
    </build>
</project>
```

---

## Testing & Validation

### Unit Test Examples

```java
import org.junit.Test;
import static org.junit.Assert.*;

public class MLDSASignatureTest {
    
    @Test
    public void testMLDSAKeyGeneration() throws Exception {
        KeyPair keyPair = MLDSASignatureExample.generateMLDSAKeyPair();
        assertNotNull(keyPair.getPublic());
        assertNotNull(keyPair.getPrivate());
    }
    
    @Test
    public void testMLDSASignAndVerify() throws Exception {
        KeyPair keyPair = MLDSASignatureExample.generateMLDSAKeyPair();
        byte[] message = "Test document content".getBytes();
        
        byte[] signature = MLDSASignatureExample.signDocument(
            message,
            keyPair.getPrivate()
        );
        
        boolean isValid = MLDSASignatureExample.verifySignature(
            message,
            signature,
            keyPair.getPublic()
        );
        
        assertTrue("Signature should be valid", isValid);
    }
    
    @Test
    public void testMLDSASignatureInvalidation() throws Exception {
        KeyPair keyPair = MLDSASignatureExample.generateMLDSAKeyPair();
        byte[] message = "Test document content".getBytes();
        
        byte[] signature = MLDSASignatureExample.signDocument(
            message,
            keyPair.getPrivate()
        );
        
        // Tamper with message
        message[0] = (byte) (message[0] ^ 0xFF);
        
        boolean isValid = MLDSASignatureExample.verifySignature(
            message,
            signature,
            keyPair.getPublic()
        );
        
        assertFalse("Signature should be invalid after tampering", isValid);
    }
}

public class HybridCryptoServiceTest {
    
    private HybridCryptoService service = new HybridCryptoService();
    
    @Test
    public void testAlgorithmSwitching() throws Exception {
        String userId = "USER001";
        
        // Default should be ML-DSA
        Algorithm initial = service.getPreferredSignatureAlgorithm(userId);
        assertEquals(Algorithm.ML_DSA, initial);
        
        // Switch to RSA
        service.switchAlgorithmPreference(userId, Algorithm.RSA_2048);
        Algorithm after = service.getPreferredSignatureAlgorithm(userId);
        assertEquals(Algorithm.RSA_2048, after);
    }
    
    @Test
    public void testToggleQuantumSafety() throws Exception {
        String userId = "USER002";
        
        // Enable PQC
        service.toggleQuantumSafety(userId, true);
        
        // Disable PQC (classical fallback)
        service.toggleQuantumSafety(userId, false);
    }
}
```

---

## Deployment Checklist

- [ ] Install Java 11+ and Maven 3.6+
- [ ] Clone/download project repository
- [ ] Run `mvn clean install` to download dependencies
- [ ] Configure database connection (PostgreSQL or H2)
- [ ] Set environment variables for API keys/secrets
- [ ] Run `mvn spring-boot:run` to start application
- [ ] Test each API endpoint with provided examples
- [ ] Verify PQC algorithms are working (check logs)
- [ ] Run timing analysis tests
- [ ] Execute hacker simulation scenarios
- [ ] Document results and findings

---

## References & Further Reading

1. **NIST Post-Quantum Cryptography Standardization** - https://csrc.nist.gov/projects/post-quantum-cryptography
2. **ML-DSA (FIPS 204) Standard** - https://nvlpubs.nist.gov/nistpubs/FIPS/NIST.FIPS.204.pdf
3. **ML-KEM (FIPS 203) Standard** - https://nvlpubs.nist.gov/nistpubs/FIPS/NIST.FIPS.203.pdf
4. **Bouncy Castle Documentation** - https://www.bouncycastle.org
5. **Quantum Computing & Cryptography** - https://www.quantum.gov
6. **Harvest-Now-Decrypt-Later Threat** - https://csrc.nist.gov/projects/post-quantum-cryptography/post-quantum-cryptography-standardization/threat-models
7. **Hybrid Cryptography Best Practices** - RFC 8773 (Hybrid Post-Quantum Public-Key Cryptography)
8. **Side-Channel Attack Mitigation** - https://csrc.nist.gov/publications/detail/sp/800-56c/rev-2/final

---

**End of Java Implementation Guide**

Document maintained by: PQC Security Research Team  
Last updated: December 2025  
License: Open Source (CC-BY-4.0)
