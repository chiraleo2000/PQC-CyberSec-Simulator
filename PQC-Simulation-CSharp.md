# Post-Quantum Cryptography (PQC) Hacking Prevention Simulation - C# .NET Implementation Guide

**Document Version:** 1.0  
**Target Framework:** .NET 6.0+  
**Primary Library:** Bouncy Castle Cryptography 2.5.1+  
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

This guide provides a complete blueprint for building a **Post-Quantum Cryptography (PQC) hacking-prevention simulation webapp** in C# .NET. The simulation demonstrates quantum computing threats and the effectiveness of quantum-resistant algorithms.

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
- **SLH-DSA (FIPS 205):** Stateless Hash-Based Digital Signature – SPHINCS+

This implementation uses **ML-DSA** and **ML-KEM** as primary algorithms with **AES-256 + RSA-2048** as fallback.

---

## Architecture Overview

```
┌─────────────────────────────────────────────────────────────┐
│    PQC Hacking Prevention Simulation System (.NET)          │
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
│           │ SQL Server / PostgreSQL         │        │       │
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
- **Block Size:** 16 bytes (GCM mode)
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
```csharp
// Controllers/UsersController.cs
[ApiController]
[Route("api/users")]
public class UsersController : ControllerBase
{
    [HttpPost("register")]
    public async Task<IActionResult> RegisterUser(RegisterUserRequest request)
    
    [HttpPost("documents/apply")]
    public async Task<IActionResult> SubmitApplication(DocumentApplicationRequest request)
    
    [HttpPost("documents/{id}/sign")]
    public async Task<IActionResult> SignDocument(string id, SignDocumentRequest request)
    
    [HttpGet("documents/{id}")]
    public async Task<IActionResult> GetDocument(string id)
    
    [HttpPost("documents/{id}/verify")]
    public async Task<IActionResult> VerifyDocumentSignature(string id)
    
    [HttpPut("users/{id}/toggle-algorithm")]
    public async Task<IActionResult> ToggleAlgorithm(string id, AlgorithmToggleRequest request)
}
```

**Key Data Models:**
```csharp
public class User
{
    public string UserId { get; set; }              // SHA-384 hash
    public string Role { get; set; }                // CITIZEN, OFFICER, SYSTEM
    public byte[] MLDsaPublicKey { get; set; }     // ML-DSA public key
    public byte[] RsaPublicKey { get; set; }        // RSA fallback key
    public CryptoAlgorithm PreferredAlgorithm { get; set; }
    public DateTime KeyGeneratedAt { get; set; }
    public DateTime LastSignatureAt { get; set; }
}

public class Document
{
    public string DocumentId { get; set; }          // SHA-384 hash
    public string DocumentType { get; set; }        // LICENSE, PERMIT, HOUSING, etc.
    public string UserId { get; set; }              // Applicant user ID
    public string Content { get; set; }             // Document content
    public Dictionary<string, byte[]> Signatures { get; set; }  // userId -> signature
    public Dictionary<string, DateTime> SignedAt { get; set; }
    public CryptoAlgorithm AlgorithmUsed { get; set; }
    public DateTime CreatedAt { get; set; }
    public DateTime LastModifiedAt { get; set; }
}

public class Signature
{
    public byte[] SignatureBytes { get; set; }
    public CryptoAlgorithm Algorithm { get; set; }   // ML_DSA, RSA
    public DateTime CreatedAt { get; set; }
    public string SignerUserId { get; set; }
}

public enum CryptoAlgorithm
{
    ML_DSA,      // Quantum-safe digital signature
    RSA_2048,    // Classical fallback (vulnerable to quantum)
    ML_KEM,      // Quantum-safe encryption
    AES_256      // Classical symmetric encryption
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
```csharp
// Controllers/MessagesController.cs
[ApiController]
[Route("api/messages")]
public class MessagesController : ControllerBase
{
    [HttpPost("encrypt")]
    public async Task<IActionResult> EncryptMessage(EncryptMessageRequest request)
    
    [HttpPost("decrypt")]
    public async Task<IActionResult> DecryptMessage(DecryptMessageRequest request)
    
    [HttpGet("inbox")]
    public async Task<IActionResult> GetInbox(string userId)
    
    [HttpPost("transactions/create")]
    public async Task<IActionResult> CreateTransaction(CreateTransactionRequest request)
    
    [HttpGet("transactions/{id}")]
    public async Task<IActionResult> GetTransaction(string id)
    
    [HttpPost("messages/{id}/toggle-kem")]
    public async Task<IActionResult> ToggleKEMEncryption(string id)
}
```

**Key Data Models:**
```csharp
public class EncryptedMessage
{
    public string MessageId { get; set; }            // SHA-384 hash
    public string SenderId { get; set; }            // User ID of sender
    public string RecipientId { get; set; }         // User ID of recipient
    public byte[] EncryptedContent { get; set; }    // KEM-encapsulated + encrypted
    public byte[] IV { get; set; }                  // 12-byte initialization vector
    public byte[] AuthTag { get; set; }             // 16-byte authentication tag (GCM)
    public CryptoAlgorithm KemAlgorithm { get; set; }
    public DateTime CreatedAt { get; set; }
    public bool DecryptedSuccessfully { get; set; }
}

public class Transaction
{
    public string TransactionId { get; set; }       // SHA-384 hash
    public string CitizenId { get; set; }           // Citizen involved
    public string OfficerId { get; set; }           // Officer approving
    public string Description { get; set; }         // Transaction details
    public byte[] EncryptedData { get; set; }       // KEM-encrypted payload
    public byte[] PublicKeyShare { get; set; }      // Encapsulated shared secret
    public CryptoAlgorithm EncryptionAlgorithm { get; set; }
    public TransactionStatus Status { get; set; }   // PENDING, APPROVED, REJECTED
    public DateTime CreatedAt { get; set; }
}

public class KEMCiphertext
{
    public byte[] EncapsulatedKey { get; set; }     // KEM public key ciphertext
    public byte[] SymmetricCiphertext { get; set; } // AES-GCM encrypted data
    public byte[] IV { get; set; }                  // 12-byte IV
    public byte[] AuthTag { get; set; }             // 16-byte auth tag
}

public enum TransactionStatus
{
    PENDING,
    APPROVED,
    REJECTED,
    COMPLETED
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
```csharp
// Controllers/HackerSimulationController.cs
[ApiController]
[Route("api/hacker")]
public class HackerSimulationController : ControllerBase
{
    [HttpPost("intercept-message")]
    public async Task<IActionResult> InterceptMessage(InterceptMessageRequest request)
    
    [HttpPost("quantum-break")]
    public async Task<IActionResult> AttemptQuantumBreak(QuantumAttackRequest request)
    
    [HttpPost("timing-attack")]
    public async Task<IActionResult> PerformTimingAttack(TimingAttackRequest request)
    
    [HttpGet("attack-history")]
    public async Task<IActionResult> GetAttackHistory()
    
    [HttpPost("simulate-scenario")]
    public async Task<IActionResult> RunAttackScenario(AttackScenarioRequest request)
    
    [HttpPost("crack-rsa")]
    public async Task<IActionResult> CrackRSA(RsaCrackRequest request)
    
    [HttpPost("crack-aes")]
    public async Task<IActionResult> CrackAES(AesCrackRequest request)
}
```

**Key Data Models:**
```csharp
public class InterceptedMessage
{
    public string InterceptId { get; set; }         // SHA-384 hash
    public string OriginalMessageId { get; set; }   // Target message ID
    public byte[] InterceptedData { get; set; }     // Captured encrypted content
    public CryptoAlgorithm TargetAlgorithm { get; set; }
    public DateTime InterceptedAt { get; set; }
    public List<AttackAttempt> Attempts { get; set; }
}

public class AttackAttempt
{
    public string AttemptId { get; set; }           // SHA-384 hash
    public string AttackType { get; set; }          // SHOR_RSA, GROVER_AES, TIMING
    public AttackStatus Status { get; set; }        // IN_PROGRESS, SUCCESS, FAILED
    public long ExecutionTimeMs { get; set; }
    public long EstimatedCompleteTimeMs { get; set; }
    public string ResultDescription { get; set; }
    public byte[] RecoveredPlaintext { get; set; }  // Only if Status == SUCCESS
    public DateTime AttemptedAt { get; set; }
    public int QuantumResourcesRequired { get; set; }
}

public class TimingAnalysis
{
    public string AnalysisId { get; set; }
    public List<long> DecryptionTimes { get; set; } // In nanoseconds
    public double Variance { get; set; }
    public bool VulnerableToTiming { get; set; }
    public string VulnerabilityDescription { get; set; }
}

public enum AttackStatus
{
    IN_PROGRESS,
    SUCCESS,
    FAILED,
    TIME_EXCEEDED
}

public class AttackScenario
{
    public string ScenarioId { get; set; }
    public string Name { get; set; }
    public string Description { get; set; }
    public CryptoAlgorithm TargetAlgorithm { get; set; }
    public string AttackType { get; set; }
    public bool QuantumComputerRequired { get; set; }
    public long EstimatedBreakTimeClassical { get; set; }  // In seconds
    public long EstimatedBreakTimeQuantum { get; set; }
    public string SuccessCriteria { get; set; }
    public string EducationalLesson { get; set; }
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
- **.NET Runtime:** .NET 6.0+ (LTS recommended)
- **IDE:** Visual Studio 2022 / Visual Studio Code + C# extension
- **Database:** SQL Server 2019+ / PostgreSQL 12+
- **Web Framework:** ASP.NET Core 6.0+
- **Package Manager:** NuGet

**Dependencies:**

```xml
<!-- .csproj file -->
<ItemGroup>
    <!-- Bouncy Castle PQC Support -->
    <PackageReference Include="BouncyCastle.Cryptography" Version="2.5.1" />
    
    <!-- ASP.NET Core -->
    <PackageReference Include="Microsoft.AspNetCore.App" Version="6.0.0" />
    <PackageReference Include="Microsoft.AspNetCore.Mvc" Version="2.2.0" />
    
    <!-- Entity Framework Core -->
    <PackageReference Include="Microsoft.EntityFrameworkCore" Version="6.0.10" />
    <PackageReference Include="Microsoft.EntityFrameworkCore.SqlServer" Version="6.0.10" />
    <PackageReference Include="Microsoft.EntityFrameworkCore.Tools" Version="6.0.10" />
    
    <!-- Database Providers -->
    <PackageReference Include="Npgsql.EntityFrameworkCore.PostgreSQL" Version="6.0.7" />
    
    <!-- JSON/Serialization -->
    <PackageReference Include="Newtonsoft.Json" Version="13.0.3" />
    <PackageReference Include="System.Text.Json" Version="4.7.2" />
    
    <!-- Logging -->
    <PackageReference Include="Serilog" Version="3.0.1" />
    <PackageReference Include="Serilog.Sinks.Console" Version="5.0.0" />
    
    <!-- Testing -->
    <PackageReference Include="xunit" Version="2.4.2" />
    <PackageReference Include="xunit.runner.visualstudio" Version="2.4.5" />
    <PackageReference Include="Microsoft.NET.Test.Sdk" Version="17.3.2" />
</ItemGroup>
```

---

## Best Practices & Security Guidelines

### 1. Key Generation & Storage

**DO:**
```csharp
✓ Generate keys in secure memory
✓ Use RNGCryptoServiceProvider for cryptographic randomness
✓ Store private keys in encrypted key containers (DPAPI)
✓ Rotate keys annually or per security policy
✓ Use separate key pairs for signing and encryption
✓ Implement key versioning with timestamps
✓ Log key generation events with audit trails
```

**DON'T:**
```csharp
✗ Store private keys in plain text in configuration files
✗ Use weak random number generators (Random.Next())
✗ Reuse keys across multiple purposes
✗ Log or display private key bytes in console/files
✗ Share private keys between services/users
✗ Hard-code cryptographic keys in source code
✗ Use old deprecated RSA/AES without migration plan
```

### 2. Hybrid Cryptography Implementation

**Best Practice Pattern:**

```csharp
public class HybridCryptoManager
{
    public enum CryptoMode { PqcPrimary, ClassicalFallback, Hybrid }
    
    private CryptoMode _mode;
    private readonly ILogger<HybridCryptoManager> _logger;
    
    // Always try PQC first, fall back to classical if needed
    public async Task<byte[]> EncryptHybridAsync(
        byte[] plaintext,
        AsymmetricKeyParameter recipientKey)
    {
        try
        {
            // Attempt ML-KEM encryption first
            return await EncryptWithMLKEMAsync(plaintext, recipientKey);
        }
        catch (Exception ex)
        {
            _logger.LogWarning(ex, "ML-KEM encryption failed, falling back to AES-256");
            return EncryptWithAES256(plaintext);
        }
    }
    
    public async Task<byte[]> DecryptHybridAsync(
        byte[] ciphertext,
        AsymmetricKeyParameter key)
    {
        try
        {
            // First, assume ML-KEM encapsulation
            return await DecryptMLKEMAsync(ciphertext, key);
        }
        catch (Exception ex)
        {
            _logger.LogWarning(ex, "ML-KEM decryption failed, attempting AES-256");
            return DecryptAES256(ciphertext);
        }
    }
}
```

### 3. Message Authentication & Integrity

**Pattern:**
```csharp
// Always use authenticated encryption (GCM mode)
// Verify signatures before processing documents
// Include timestamps in all cryptographic operations
// Hash sensitive data (user IDs, document IDs) with SHA-384
// Implement HMAC for additional authentication
```

### 4. Timing Attack Mitigation

**Pattern:**
```csharp
// Use constant-time comparison for cryptographic values
private bool ConstantTimeEquals(byte[] a, byte[] b)
{
    if (a.Length != b.Length) return false;
    
    int result = 0;
    for (int i = 0; i < a.Length; i++)
    {
        result |= a[i] ^ b[i];
    }
    return result == 0;
}

// Add random delays to obscure operation timing
private void AddRandomDelay(int minMs, int maxMs)
{
    using (var rng = new System.Security.Cryptography.RNGCryptoServiceProvider())
    {
        byte[] randomBytes = new byte[4];
        rng.GetBytes(randomBytes);
        int delay = BitConverter.ToInt32(randomBytes, 0) % (maxMs - minMs + 1) + minMs;
        System.Threading.Thread.Sleep(Math.Abs(delay));
    }
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
```csharp
// Use ML-KEM now to protect against future quantum attacks
// Assume all unencrypted data can be harvested
// Encrypt all sensitive data before transmission
// Include explicit algorithm identifier in ciphertext
// Enable algorithm downgrade detection and logging
// Implement key rotation schedules
```

---

## Implementation Examples

### Example 1: ML-DSA Key Generation and Signature

```csharp
using Org.BouncyCastle.Pqc.Crypto.Crystals;
using Org.BouncyCastle.Security;
using System;

public class MLDSASignatureExample
{
    // Generate ML-DSA key pair
    public static (byte[] publicKey, byte[] privateKey) GenerateMLDSAKeyPair()
    {
        var keyGen = new MLDSAKeyPairGenerator();
        keyGen.Init(new MLDSAKeyGenParameters(MLDSAParameterSet.ml_dsa_65));
        
        var keyPair = keyGen.GenerateKeyPair();
        var publicKey = keyPair.Public as MLDSAPublicKeyParameters;
        var privateKey = keyPair.Private as MLDSAPrivateKeyParameters;
        
        return (publicKey.GetEncoded(), privateKey.GetEncoded());
    }
    
    // Sign a document
    public static byte[] SignDocument(
        byte[] documentContent,
        byte[] privateKeyBytes)
    {
        var privateKey = new MLDSAPrivateKeyParameters(privateKeyBytes);
        var signer = new MLDSASignature();
        signer.Init(true, privateKey);
        
        return signer.GenerateSignature(documentContent);
    }
    
    // Verify a signature
    public static bool VerifySignature(
        byte[] documentContent,
        byte[] signature,
        byte[] publicKeyBytes)
    {
        var publicKey = new MLDSAPublicKeyParameters(publicKeyBytes);
        var verifier = new MLDSASignature();
        verifier.Init(false, publicKey);
        
        return verifier.VerifySignature(documentContent, signature);
    }
}
```

**Usage:**
```csharp
// Generate keys
var (pubKey, privKey) = MLDSASignatureExample.GenerateMLDSAKeyPair();
byte[] documentBytes = System.Text.Encoding.UTF8.GetBytes("License Application #12345");

// Sign
byte[] signature = MLDSASignatureExample.SignDocument(documentBytes, privKey);
Console.WriteLine($"Signature size: {signature.Length} bytes");

// Verify
bool isValid = MLDSASignatureExample.VerifySignature(
    documentBytes,
    signature,
    pubKey
);
Console.WriteLine($"Signature valid: {isValid}");
```

---

### Example 2: ML-KEM Encryption and Decryption

```csharp
using Org.BouncyCastle.Pqc.Crypto.Crystals;
using Org.BouncyCastle.Crypto;
using Org.BouncyCastle.Security;
using System;

public class MLKEMEncryptionExample
{
    // Generate ML-KEM key pair
    public static (byte[] publicKey, byte[] privateKey) GenerateMLKEMKeyPair()
    {
        var keyGen = new MLKEMKeyPairGenerator();
        keyGen.Init(new MLKEMKeyGenParameters(MLKEMParameterSet.ml_kem_768));
        
        var keyPair = keyGen.GenerateKeyPair();
        var publicKey = keyPair.Public as MLKEMPublicKeyParameters;
        var privateKey = keyPair.Private as MLKEMPrivateKeyParameters;
        
        return (publicKey.GetEncoded(), privateKey.GetEncoded());
    }
    
    // Encapsulate shared secret (sender side)
    public static (byte[] encapsulatedKey, byte[] sharedSecret) Encapsulate(
        byte[] publicKeyBytes)
    {
        var publicKey = new MLKEMPublicKeyParameters(publicKeyBytes);
        var kem = new MLKEMKEMGenerator(new SecureRandom());
        
        var encapsulation = kem.GenerateEncapsulated(publicKey);
        return (encapsulation.GetEncapsulation(), encapsulation.GetShared());
    }
    
    // Decapsulate shared secret (recipient side)
    public static byte[] Decapsulate(
        byte[] encapsulatedKey,
        byte[] privateKeyBytes)
    {
        var privateKey = new MLKEMPrivateKeyParameters(privateKeyBytes);
        var kem = new MLKEMKEMExtractor(privateKey);
        
        return kem.ExtractSecret(encapsulatedKey);
    }
    
    // Encrypt message using derived symmetric key (AES-256-GCM)
    public static byte[] EncryptMessageWithDerivedKey(
        byte[] message,
        byte[] sharedSecret)
    {
        // Derive symmetric key from shared secret using SHA-256
        using (var sha256 = System.Security.Cryptography.SHA256.Create())
        {
            byte[] symmetricKey = sha256.ComputeHash(sharedSecret);
            
            // Use AES-256-GCM for message encryption
            using (var aes = new System.Security.Cryptography.AesGcm(symmetricKey))
            {
                byte[] iv = new byte[12];
                using (var rng = System.Security.Cryptography.RandomNumberGenerator.Create())
                {
                    rng.GetBytes(iv);
                }
                
                byte[] ciphertext = new byte[message.Length];
                byte[] tag = new byte[16];
                
                aes.Encrypt(iv, message, ciphertext, tag);
                
                // Return: IV + ciphertext + tag
                byte[] result = new byte[iv.Length + ciphertext.Length + tag.Length];
                Array.Copy(iv, 0, result, 0, iv.Length);
                Array.Copy(ciphertext, 0, result, iv.Length, ciphertext.Length);
                Array.Copy(tag, 0, result, iv.Length + ciphertext.Length, tag.Length);
                
                return result;
            }
        }
    }
}
```

**Usage:**
```csharp
// Generate keys
var (pubKey, privKey) = MLKEMEncryptionExample.GenerateMLKEMKeyPair();
byte[] messageBytes = System.Text.Encoding.UTF8.GetBytes(
    "Confidential transaction data");

// Encapsulate (sender)
var (encapKey, sharedSecret) = MLKEMEncryptionExample.Encapsulate(pubKey);

// Encrypt message
byte[] encryptedMessage = MLKEMEncryptionExample.EncryptMessageWithDerivedKey(
    messageBytes,
    sharedSecret
);

// Decapsulate (recipient)
byte[] decapsulatedSecret = MLKEMEncryptionExample.Decapsulate(encapKey, privKey);

// Verify shared secrets match
bool secretMatch = CompareByteArrays(sharedSecret, decapsulatedSecret);
Console.WriteLine($"Shared secrets match: {secretMatch}");
```

---

### Example 3: Hybrid Algorithm Switching

```csharp
using System;
using System.Collections.Generic;
using System.Collections.Concurrent;

public class HybridCryptoService
{
    public enum Algorithm
    {
        ML_DSA,      // Quantum-safe signatures
        RSA_2048,    // Classical fallback
        ML_KEM,      // Quantum-safe encryption
        AES_256      // Classical symmetric
    }
    
    private readonly IDictionary<string, Algorithm> _userAlgorithmPreferences
        = new ConcurrentDictionary<string, Algorithm>();
    
    private readonly ILogger<HybridCryptoService> _logger;
    
    public HybridCryptoService(ILogger<HybridCryptoService> logger)
    {
        _logger = logger;
    }
    
    // Get preferred algorithm for user
    public Algorithm GetPreferredSignatureAlgorithm(string userId)
    {
        return _userAlgorithmPreferences.TryGetValue(userId, out var algo)
            ? algo
            : Algorithm.ML_DSA; // Default to ML-DSA
    }
    
    public Algorithm GetPreferredEncryptionAlgorithm(string userId)
    {
        return _userAlgorithmPreferences.TryGetValue(userId, out var algo)
            ? algo
            : Algorithm.ML_KEM; // Default to ML-KEM
    }
    
    // Sign with preferred algorithm
    public async Task<byte[]> SignDocumentWithPreferenceAsync(
        string userId,
        byte[] documentContent,
        byte[] userPrivateKey)
    {
        var algo = GetPreferredSignatureAlgorithm(userId);
        
        switch(algo)
        {
            case Algorithm.ML_DSA:
                return SignWithMLDSA(documentContent, userPrivateKey);
            case Algorithm.RSA_2048:
                return SignWithRSA(documentContent, userPrivateKey);
            default:
                throw new ArgumentException($"Unknown algorithm: {algo}");
        }
    }
    
    private byte[] SignWithMLDSA(byte[] content, byte[] privateKey)
    {
        return MLDSASignatureExample.SignDocument(content, privateKey);
    }
    
    private byte[] SignWithRSA(byte[] content, byte[] privateKey)
    {
        using (var rsa = System.Security.Cryptography.RSA.Create())
        {
            rsa.ImportPkcs8PrivateKey(privateKey, out _);
            return rsa.SignData(content, System.Security.Cryptography.HashAlgorithmName.SHA256,
                System.Security.Cryptography.RSASignaturePadding.Pkcs1);
        }
    }
    
    // Switch user's preferred algorithm
    public void SwitchAlgorithmPreference(string userId, Algorithm algorithm)
    {
        _userAlgorithmPreferences[userId] = algorithm;
        _logger.LogInformation("User {UserId} switched to algorithm: {Algorithm}",
            userId, algorithm);
    }
    
    // Toggle between PQC and Classical for awareness demonstration
    public void ToggleQuantumSafety(string userId, bool enablePQC)
    {
        var prefSig = enablePQC ? Algorithm.ML_DSA : Algorithm.RSA_2048;
        var prefEnc = enablePQC ? Algorithm.ML_KEM : Algorithm.AES_256;
        
        _userAlgorithmPreferences[$"{userId}_sig"] = prefSig;
        _userAlgorithmPreferences[$"{userId}_enc"] = prefEnc;
        
        _logger.LogInformation("User {UserId} toggled quantum safety: {Enabled}",
            userId, enablePQC);
    }
}
```

---

### Example 4: Identity Hashing (SHA-384)

```csharp
using System;
using System.Security.Cryptography;
using System.Text;

public class IdentityHashService
{
    /// <summary>
    /// Generate consistent identity hash for user
    /// Uses: userId + emailAddress + dateOfBirth
    /// </summary>
    public static string GenerateUserIdentityHash(
        string userId,
        string emailAddress,
        DateTime dateOfBirth)
    {
        using (var sha384 = SHA384.Create())
        {
            string combined = $"{userId}|{emailAddress}|{dateOfBirth:yyyy-MM-dd}";
            byte[] hashBytes = sha384.ComputeHash(Encoding.UTF8.GetBytes(combined));
            
            return BytesToHex(hashBytes);
        }
    }
    
    /// <summary>
    /// Generate document ID hash
    /// Uses: documentType + userId + timestamp + randomNonce
    /// </summary>
    public static string GenerateDocumentIdHash(
        string documentType,
        string userId,
        DateTime timestamp,
        string nonce)
    {
        using (var sha384 = SHA384.Create())
        {
            string combined = $"{documentType}|{userId}|{timestamp:O}|{nonce}";
            byte[] hashBytes = sha384.ComputeHash(Encoding.UTF8.GetBytes(combined));
            
            return BytesToHex(hashBytes);
        }
    }
    
    private static string BytesToHex(byte[] bytes)
    {
        var hexString = new StringBuilder();
        foreach (byte b in bytes)
        {
            hexString.AppendFormat("{0:x2}", b);
        }
        return hexString.ToString();
    }
}
```

**Usage:**
```csharp
string userHash = IdentityHashService.GenerateUserIdentityHash(
    "USER123",
    "john@example.com",
    new DateTime(1990, 5, 15)
);
Console.WriteLine($"User Identity Hash: {userHash}");

string docHash = IdentityHashService.GenerateDocumentIdHash(
    "LICENSE",
    "USER123",
    DateTime.Now,
    "nonce-random-value"
);
Console.WriteLine($"Document ID Hash: {docHash}");
```

---

### Example 5: Hacker Simulation - Shor's Algorithm (RSA Breaking)

```csharp
using System;
using System.Numerics;
using System.Diagnostics;

public class HackerSimulationService
{
    public enum AttackType
    {
        SHOR_RSA,           // Shor's algorithm for RSA factorization
        GROVER_AES,         // Grover's algorithm for AES key search
        TIMING_ATTACK,      // Side-channel timing analysis
        CLASSICAL_BRUTE,    // Classical brute force
        HARVEST_NOW_DECRYPT_LATER
    }
    
    public enum AttackStatus
    {
        IN_PROGRESS,
        SUCCESS,
        FAILED,
        TIME_EXCEEDED
    }
    
    /// <summary>
    /// Simulate Shor's Algorithm attack on RSA-2048
    /// Educational demonstration of quantum threat
    /// </summary>
    public async Task<AttackResult> SimulateShorAlgorithmAttackAsync(
        byte[] ciphertext,
        System.Security.Cryptography.RSA publicKey)
    {
        var stopwatch = Stopwatch.StartNew();
        
        // Simulate quantum gate operations
        int keyBitLength = publicKey.KeySize;
        long estimatedQuantumGates = (long)Math.Pow(keyBitLength, 2.373);
        
        // For demonstration, we can't actually break RSA,
        // so we simulate the quantum computer's work
        await SimulateQuantumComputationAsync(estimatedQuantumGates);
        
        stopwatch.Stop();
        
        return new AttackResult
        {
            Type = AttackType.SHOR_RSA,
            Successful = true,
            ExecutionTimeMs = stopwatch.ElapsedMilliseconds,
            EstimatedQuantumGates = estimatedQuantumGates,
            Description = "Shor's algorithm would factor RSA-2048 in hours with a quantum computer. " +
                         "Classical factorization would take millennia."
        };
    }
    
    /// <summary>
    /// Simulate Grover's Algorithm attack on AES-256
    /// Shows quantum speedup on symmetric encryption
    /// </summary>
    public async Task<AttackResult> SimulateGroverAlgorithmAttackAsync(
        byte[] ciphertext,
        byte[] iv,
        string plaintextPattern)
    {
        var stopwatch = Stopwatch.StartNew();
        
        // Classical AES search: O(2^256) operations
        // Grover's algorithm: O(2^128) quantum operations
        long classicalComplexity = (long)Math.Pow(2, 256);
        long quantumComplexity = (long)Math.Pow(2, 128);
        
        // Simulate quantum computation
        await SimulateQuantumComputationAsync(quantumComplexity);
        
        stopwatch.Stop();
        
        return new AttackResult
        {
            Type = AttackType.GROVER_AES,
            Successful = true,
            ExecutionTimeMs = stopwatch.ElapsedMilliseconds,
            EstimatedQuantumGates = quantumComplexity,
            Description = "Grover's algorithm would reduce AES-256 search from 2^256 to 2^128. " +
                         "Still infeasible but represents significant speedup vs classical."
        };
    }
    
    private async Task SimulateQuantumComputationAsync(long gateCount)
    {
        // Simulate quantum computer processing
        // In reality, this would depend on physical quantum computer properties
        long delayMs = Math.Max(1, gateCount / 1000000); // Arbitrary scaling
        await Task.Delay((int)Math.Min(delayMs, 5000)); // Max 5 seconds simulation
    }
    
    public class AttackResult
    {
        public AttackType Type { get; set; }
        public bool Successful { get; set; }
        public byte[] RecoveredData { get; set; }
        public long ExecutionTimeMs { get; set; }
        public long EstimatedQuantumGates { get; set; }
        public string Description { get; set; }
    }
}
```

---

### Example 6: Timing Attack Simulation

```csharp
using System;
using System.Collections.Generic;
using System.Diagnostics;
using System.Linq;

public class TimingAttackSimulator
{
    /// <summary>
    /// Analyze decryption timing to detect algorithm vulnerabilities
    /// Educational demonstration of side-channel attacks
    /// </summary>
    public TimingAnalysisResult AnalyzeDecryptionTiming(
        Func<byte[], long> decryptOperation,
        byte[][] testCiphertexts,
        int sampleCount)
    {
        var timingMeasurements = new List<long>();
        
        for (int sample = 0; sample < sampleCount; sample++)
        {
            foreach (byte[] ciphertext in testCiphertexts)
            {
                var stopwatch = Stopwatch.StartNew();
                decryptOperation(ciphertext);
                stopwatch.Stop();
                
                timingMeasurements.Add(stopwatch.ElapsedTicks);
            }
        }
        
        // Statistical analysis
        double mean = timingMeasurements.Average();
        double variance = timingMeasurements
            .Average(t => Math.Pow(t - mean, 2));
        double stdDeviation = Math.Sqrt(variance);
        double coefficientOfVariation = stdDeviation / mean;
        
        // Determine vulnerability
        bool vulnerableToTiming = coefficientOfVariation > 0.05; // >5% variation = vulnerable
        
        string analysis = GenerateTimingAnalysis(
            mean, variance, stdDeviation,
            coefficientOfVariation, vulnerableToTiming);
        
        return new TimingAnalysisResult
        {
            AllTimings = timingMeasurements,
            Mean = mean,
            Variance = variance,
            StandardDeviation = stdDeviation,
            CoefficientOfVariation = coefficientOfVariation,
            VulnerableToTiming = vulnerableToTiming,
            Analysis = analysis
        };
    }
    
    private string GenerateTimingAnalysis(
        double mean,
        double variance,
        double stdDev,
        double cv,
        bool vulnerable)
    {
        var analysis = new System.Text.StringBuilder();
        
        analysis.AppendLine("=== TIMING ANALYSIS REPORT ===");
        analysis.AppendLine($"Mean execution time: {mean:F2} ticks");
        analysis.AppendLine($"Variance: {variance:F2}");
        analysis.AppendLine($"Standard Deviation: {stdDev:F2} ticks");
        analysis.AppendLine($"Coefficient of Variation: {cv:F4} ({cv * 100:F2}%)");
        
        if (vulnerable)
        {
            analysis.AppendLine("\n⚠️  VULNERABLE TO TIMING ATTACKS");
            analysis.AppendLine("The decryption operation shows significant timing variation.");
            analysis.AppendLine("This could allow attackers to infer information about");
            analysis.AppendLine("the decryption key through statistical timing analysis.");
            analysis.AppendLine("\nMITIGATION: Use constant-time comparison functions");
            analysis.AppendLine("and add random delays.");
        }
        else
        {
            analysis.AppendLine("\n✓ CONSTANT-TIME RESISTANT");
            analysis.AppendLine("The decryption operation shows minimal timing variation.");
        }
        
        return analysis.ToString();
    }
    
    public class TimingAnalysisResult
    {
        public List<long> AllTimings { get; set; }
        public double Mean { get; set; }
        public double Variance { get; set; }
        public double StandardDeviation { get; set; }
        public double CoefficientOfVariation { get; set; }
        public bool VulnerableToTiming { get; set; }
        public string Analysis { get; set; }
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
```csharp
public class HarvestNowDecryptLaterScenario
{
    private readonly ILogger<HarvestNowDecryptLaterScenario> _logger;
    private readonly HackerSimulationService _hackerService;
    
    public async Task RunScenarioAsync()
    {
        // 2025: Citizen submits housing application
        string housingApplication = "HOUSING_RECORD_001: John Doe, " +
            "123 Main St, Bangkok, 10100";
        
        _logger.LogInformation("[2025] Citizen submits housing application");
        
        // Government encrypts with RSA-2048 (vulnerable!)
        using (var rsa = System.Security.Cryptography.RSA.Create(2048))
        {
            byte[] publicKeyBytes = rsa.ExportSubjectPublicKeyInfo();
            byte[] encryptedData = rsa.Encrypt(
                Encoding.UTF8.GetBytes(housingApplication),
                System.Security.Cryptography.RSAEncryptionPadding.OaepSHA256
            );
            
            _logger.LogInformation("[2025] Housing record encrypted with RSA-2048");
            _logger.LogInformation("Ciphertext size: {Size} bytes", encryptedData.Length);
            
            // Hacker intercepts encrypted data
            byte[] interceptedCiphertext = encryptedData;
            _logger.LogInformation("[2025] Hacker intercepts encrypted housing record");
            
            // Simulate waiting 5 years for quantum computer
            _logger.LogInformation("[2030] Quantum computer becomes available...");
            _logger.LogInformation("[2030] Hacker initiates Shor's algorithm attack");
            
            // Attack on RSA
            var result = await _hackerService.SimulateShorAlgorithmAttackAsync(
                interceptedCiphertext,
                rsa
            );
            
            _logger.LogInformation("[2030] Attack successful: {Success}", result.Successful);
            _logger.LogInformation("Execution time: {Time} ms", result.ExecutionTimeMs);
            _logger.LogInformation("Description: {Desc}", result.Description);
        }
        
        // NOW with ML-KEM (quantum-safe)
        _logger.LogInformation("\n=== QUANTUM-SAFE ALTERNATIVE ===");
        _logger.LogInformation("[2025] Government encrypts with ML-KEM (quantum-safe)");
        _logger.LogInformation("[2030] Hacker attempts Shor's algorithm...");
        _logger.LogInformation("Result: FAILED - ML-KEM is lattice-based, not vulnerable");
        _logger.LogInformation("Housing record remains secure even in 2030!");
    }
}
```

---

### Scenario 2: Algorithm Performance Comparison

```
┌──────────────────────────────────────────────────────────┐
│ ALGORITHM COMPARISON                                      │
├──────────────────────────────────────────────────────────┤
│                                                            │
│ CLASSICAL ALGORITHMS (Vulnerable):                        │
│ ┌──────────────────────────────────────────────────────┐ │
│ │ RSA-2048:                                            │ │
│ │ - Break time (classical): 300,000+ years             │ │
│ │ - Break time (quantum): ~8 hours                     │ │
│ │ - Threat: CRITICAL (post-quantum)                    │ │
│ └──────────────────────────────────────────────────────┘ │
│                                                            │
│ │ AES-256:                                             │ │
│ │ - Break time (classical): 2^256 attempts             │ │
│ │ - Break time (quantum): ~2^128 ops                   │ │
│ │ - Threat: REDUCED (still secure with 256 bits)      │ │
│ └──────────────────────────────────────────────────────┘ │
│                                                            │
│ QUANTUM-SAFE ALGORITHMS:                                  │
│ ┌──────────────────────────────────────────────────────┐ │
│ │ ML-DSA (Signatures):                                 │ │
│ │ - Break time (quantum): Intractable (MLWE)          │ │
│ │ - Security: 128-bit (post-quantum)                   │ │
│ │ - Threat: MINIMAL (future-proof)                     │ │
│ └──────────────────────────────────────────────────────┘ │
│                                                            │
│ │ ML-KEM (Encryption):                                 │ │
│ │ - Break time (quantum): Intractable (MLWE)          │ │
│ │ - Security: 192-bit (post-quantum)                   │ │
│ │ - Threat: MINIMAL (future-proof)                     │ │
│ └──────────────────────────────────────────────────────┘ │
└──────────────────────────────────────────────────────────┘
```

---

## Tools & Dependencies

### Project File (.csproj) Configuration

```xml
<Project Sdk="Microsoft.NET.Sdk.Web">

    <PropertyGroup>
        <TargetFramework>net6.0</TargetFramework>
        <Nullable>enable</Nullable>
        <ImplicitUsings>enable</ImplicitUsings>
    </PropertyGroup>

    <PropertyGroup>
        <AssemblyName>PQCHackingPreventionSimulation</AssemblyName>
        <RootNamespace>PQCSimulation</RootNamespace>
        <Version>1.0.0</Version>
        <Authors>PQC Security Team</Authors>
        <Description>Post-Quantum Cryptography Hacking Prevention Simulation</Description>
        <LangVersion>latest</LangVersion>
    </PropertyGroup>

    <ItemGroup>
        <!-- Bouncy Castle PQC Support -->
        <PackageReference Include="BouncyCastle.Cryptography" Version="2.5.1" />
        
        <!-- ASP.NET Core Web API -->
        <PackageReference Include="Microsoft.AspNetCore.App" Version="6.0.0" />
        
        <!-- Entity Framework Core -->
        <PackageReference Include="Microsoft.EntityFrameworkCore" Version="6.0.10" />
        <PackageReference Include="Microsoft.EntityFrameworkCore.SqlServer" Version="6.0.10" />
        <PackageReference Include="Microsoft.EntityFrameworkCore.Tools" Version="6.0.10">
            <PrivateAssets>all</PrivateAssets>
            <IncludeAssets>runtime; build; native; contentfiles; analyzers; buildtransitive</IncludeAssets>
        </PackageReference>
        
        <!-- PostgreSQL Support -->
        <PackageReference Include="Npgsql.EntityFrameworkCore.PostgreSQL" Version="6.0.7" />
        
        <!-- JSON/Serialization -->
        <PackageReference Include="Newtonsoft.Json" Version="13.0.3" />
        <PackageReference Include="System.Text.Json" Version="4.7.2" />
        
        <!-- Dependency Injection & Configuration -->
        <PackageReference Include="Microsoft.Extensions.DependencyInjection" Version="6.0.0" />
        <PackageReference Include="Microsoft.Extensions.Configuration" Version="6.0.0" />
        
        <!-- Logging -->
        <PackageReference Include="Serilog" Version="3.0.1" />
        <PackageReference Include="Serilog.AspNetCore" Version="6.0.1" />
        <PackageReference Include="Serilog.Sinks.Console" Version="5.0.0" />
        <PackageReference Include="Serilog.Sinks.File" Version="5.0.0" />
        
        <!-- Testing -->
        <PackageReference Include="xunit" Version="2.4.2" />
        <PackageReference Include="xunit.runner.visualstudio" Version="2.4.5">
            <PrivateAssets>all</PrivateAssets>
            <IncludeAssets>runtime; build; native; contentfiles; analyzers; buildtransitive</IncludeAssets>
        </PackageReference>
        <PackageReference Include="Microsoft.NET.Test.Sdk" Version="17.3.2" />
        <PackageReference Include="Moq" Version="4.18.3" />
    </ItemGroup>

</Project>
```

---

## Testing & Validation

### Unit Test Examples

```csharp
using Xunit;
using System.Text;

public class MLDSASignatureTests
{
    [Fact]
    public void GenerateMLDSAKeyPair_ReturnsValidKeyPair()
    {
        // Arrange & Act
        var (pubKey, privKey) = MLDSASignatureExample.GenerateMLDSAKeyPair();
        
        // Assert
        Assert.NotNull(pubKey);
        Assert.NotNull(privKey);
        Assert.NotEmpty(pubKey);
        Assert.NotEmpty(privKey);
    }
    
    [Fact]
    public void SignAndVerify_SucceedsWithValidSignature()
    {
        // Arrange
        var (pubKey, privKey) = MLDSASignatureExample.GenerateMLDSAKeyPair();
        byte[] message = Encoding.UTF8.GetBytes("Test document content");
        
        // Act
        byte[] signature = MLDSASignatureExample.SignDocument(message, privKey);
        bool isValid = MLDSASignatureExample.VerifySignature(message, signature, pubKey);
        
        // Assert
        Assert.NotNull(signature);
        Assert.NotEmpty(signature);
        Assert.True(isValid);
    }
    
    [Fact]
    public void Verify_FailsWithTamperedMessage()
    {
        // Arrange
        var (pubKey, privKey) = MLDSASignatureExample.GenerateMLDSAKeyPair();
        byte[] message = Encoding.UTF8.GetBytes("Test document content");
        byte[] signature = MLDSASignatureExample.SignDocument(message, privKey);
        
        // Tamper with message
        message[0] = (byte)(message[0] ^ 0xFF);
        
        // Act
        bool isValid = MLDSASignatureExample.VerifySignature(message, signature, pubKey);
        
        // Assert
        Assert.False(isValid);
    }
}

public class HybridCryptoServiceTests
{
    [Fact]
    public void GetPreferredSignatureAlgorithm_ReturnsMLDSAByDefault()
    {
        // Arrange
        var logger = new Mock<ILogger<HybridCryptoService>>();
        var service = new HybridCryptoService(logger.Object);
        string userId = "USER001";
        
        // Act
        var algorithm = service.GetPreferredSignatureAlgorithm(userId);
        
        // Assert
        Assert.Equal(HybridCryptoService.Algorithm.ML_DSA, algorithm);
    }
    
    [Fact]
    public void SwitchAlgorithmPreference_UpdatesUserPreference()
    {
        // Arrange
        var logger = new Mock<ILogger<HybridCryptoService>>();
        var service = new HybridCryptoService(logger.Object);
        string userId = "USER002";
        
        // Act
        service.SwitchAlgorithmPreference(userId, HybridCryptoService.Algorithm.RSA_2048);
        var algorithm = service.GetPreferredSignatureAlgorithm(userId);
        
        // Assert
        Assert.Equal(HybridCryptoService.Algorithm.RSA_2048, algorithm);
    }
    
    [Fact]
    public void ToggleQuantumSafety_SwitchesToClassicalWhenDisabled()
    {
        // Arrange
        var logger = new Mock<ILogger<HybridCryptoService>>();
        var service = new HybridCryptoService(logger.Object);
        string userId = "USER003";
        
        // Act
        service.ToggleQuantumSafety(userId, false);
        
        // Assert - should now prefer classical algorithms
        var sigAlgo = service.GetPreferredSignatureAlgorithm($"{userId}_sig");
        var encAlgo = service.GetPreferredEncryptionAlgorithm($"{userId}_enc");
        
        Assert.Equal(HybridCryptoService.Algorithm.RSA_2048, sigAlgo);
        Assert.Equal(HybridCryptoService.Algorithm.AES_256, encAlgo);
    }
}

public class IdentityHashServiceTests
{
    [Fact]
    public void GenerateUserIdentityHash_ProducesSHA384Hash()
    {
        // Arrange
        string userId = "USER123";
        string email = "user@example.com";
        var dob = new System.DateTime(1990, 5, 15);
        
        // Act
        string hash = IdentityHashService.GenerateUserIdentityHash(userId, email, dob);
        
        // Assert
        Assert.NotNull(hash);
        Assert.NotEmpty(hash);
        Assert.Equal(96, hash.Length); // SHA-384 = 48 bytes = 96 hex characters
    }
    
    [Fact]
    public void GenerateUserIdentityHash_IsDeterministic()
    {
        // Arrange
        string userId = "USER123";
        string email = "user@example.com";
        var dob = new System.DateTime(1990, 5, 15);
        
        // Act
        string hash1 = IdentityHashService.GenerateUserIdentityHash(userId, email, dob);
        string hash2 = IdentityHashService.GenerateUserIdentityHash(userId, email, dob);
        
        // Assert
        Assert.Equal(hash1, hash2);
    }
}
```

---

## Deployment Checklist

- [ ] Install .NET 6.0+ runtime and SDK
- [ ] Install SQL Server or PostgreSQL
- [ ] Clone/download project repository
- [ ] Run `dotnet restore` to download NuGet packages
- [ ] Configure appsettings.json with database connection string
- [ ] Run `dotnet ef database update` to create database schema
- [ ] Set environment variables (API keys, secrets, etc.)
- [ ] Run `dotnet run` to start application
- [ ] Test each API endpoint with provided examples
- [ ] Verify PQC algorithms are working (check logs)
- [ ] Run unit tests: `dotnet test`
- [ ] Execute hacker simulation scenarios
- [ ] Document results and findings

---

## References & Further Reading

1. **NIST Post-Quantum Cryptography Standardization** - https://csrc.nist.gov/projects/post-quantum-cryptography
2. **ML-DSA (FIPS 204) Standard** - https://nvlpubs.nist.gov/nistpubs/FIPS/NIST.FIPS.204.pdf
3. **ML-KEM (FIPS 203) Standard** - https://nvlpubs.nist.gov/nistpubs/FIPS/NIST.FIPS.203.pdf
4. **Bouncy Castle .NET Documentation** - https://www.bouncycastle.org
5. **ASP.NET Core Documentation** - https://docs.microsoft.com/aspnet/core
6. **Entity Framework Core** - https://docs.microsoft.com/ef/core
7. **Quantum Computing & Cryptography** - https://www.quantum.gov
8. **Harvest-Now-Decrypt-Later Threat** - https://csrc.nist.gov/projects/post-quantum-cryptography/post-quantum-cryptography-standardization/threat-models
9. **Hybrid Cryptography Best Practices** - RFC 8773 (Hybrid Post-Quantum Public-Key Cryptography)
10. **Side-Channel Attack Mitigation** - https://csrc.nist.gov/publications/detail/sp/800-56c/rev-2/final

---

**End of C# .NET Implementation Guide**

Document maintained by: PQC Security Research Team  
Last updated: December 2025  
License: Open Source (CC-BY-4.0)
