package com.pqc.hacker.intercept;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.pqc.hacker.quantum.CuQuantumGpuSimulator;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.math.BigInteger;
import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.TimeUnit;

/**
 * Encrypted Data Harvester - HNDL Attack Simulation
 * 
 * This service demonstrates "Harvest Now, Decrypt Later" attacks:
 * 1. Intercepts encrypted transaction logs from the government portal
 * 2. Stores the encrypted data for later quantum decryption
 * 3. Uses cuQuantum GPU simulator to attempt cryptographic attacks
 * 
 * EDUCATIONAL PURPOSE: Shows why post-quantum cryptography is needed NOW
 */
@Service
@Slf4j
public class EncryptedDataHarvester {

    private final CuQuantumGpuSimulator quantumSimulator;
    private final OkHttpClient httpClient;
    private final ObjectMapper objectMapper;
    private final SecureRandom secureRandom = new SecureRandom();

    @Value("${hacker.target.gov-portal:http://localhost:8181}")
    private String govPortalUrl;

    @Value("${hacker.target.messaging:http://localhost:8182}")
    private String messagingUrl;

    // Harvested encrypted data storage (simulates attacker's database)
    private final List<InterceptedTransaction> harvestedData = Collections.synchronizedList(new ArrayList<>());

    public EncryptedDataHarvester(CuQuantumGpuSimulator quantumSimulator) {
        this.quantumSimulator = quantumSimulator;
        this.objectMapper = new ObjectMapper();
        this.httpClient = new OkHttpClient.Builder()
                .connectTimeout(10, TimeUnit.SECONDS)
                .readTimeout(30, TimeUnit.SECONDS)
                .build();
        log.warn("🕵️ Encrypted Data Harvester initialized - targeting {} and {}", govPortalUrl, messagingUrl);
    }

    /**
     * PHASE 1: HARVEST - Intercept encrypted transaction logs from the network
     */
    public InterceptionResult harvestTransactionLogs() {
        log.warn("🎯 HARVESTING: Intercepting encrypted transaction logs from network traffic...");
        
        InterceptionResult result = new InterceptionResult();
        result.setTimestamp(LocalDateTime.now());
        result.setTargetUrl(govPortalUrl + "/api/transactions");
        
        try {
            Request request = new Request.Builder()
                    .url(govPortalUrl + "/api/transactions")
                    .header("User-Agent", "Mozilla/5.0 (Network Interceptor)")
                    .header("X-Intercepted-By", "HNDL-Attack-Simulator")
                    .build();

            try (Response response = httpClient.newCall(request).execute()) {
                if (response.isSuccessful() && response.body() != null) {
                    String rawJson = response.body().string();
                    List<Map<String, Object>> transactions = objectMapper.readValue(
                            rawJson, new TypeReference<List<Map<String, Object>>>() {});
                    
                    result.setRawDataCaptured(rawJson);
                    result.setTransactionCount(transactions.size());
                    
                    List<InterceptedTransaction> intercepted = new ArrayList<>();
                    
                    for (Map<String, Object> tx : transactions) {
                        InterceptedTransaction itx = new InterceptedTransaction();
                        itx.setDocumentId(String.valueOf(tx.get("documentId")));
                        itx.setDocumentType(String.valueOf(tx.get("type")));
                        itx.setApplicant(String.valueOf(tx.get("applicant")));
                        itx.setEncryptionAlgorithm(String.valueOf(tx.get("encryption")));
                        itx.setStatus(String.valueOf(tx.get("status")));
                        itx.setInterceptedAt(LocalDateTime.now());
                        
                        // Simulate capturing encrypted payload
                        itx.setEncryptedPayload(generateSimulatedEncryptedData(itx.getEncryptionAlgorithm()));
                        
                        // Extract encryption metadata for quantum attack
                        itx.setKeyMetadata(extractKeyMetadata(itx.getEncryptionAlgorithm()));
                        
                        intercepted.add(itx);
                        harvestedData.add(itx);
                    }
                    
                    result.setSuccess(true);
                    result.setInterceptedTransactions(intercepted);
                    result.setMessage(String.format(
                            "✅ HARVEST COMPLETE: Captured %d encrypted transactions. " +
                            "Data stored for quantum decryption attack.", 
                            intercepted.size()));
                    
                    log.warn("📦 HARVESTED {} encrypted transactions - stored for HNDL attack", intercepted.size());
                    
                } else {
                    result.setSuccess(false);
                    result.setMessage("Failed to intercept: HTTP " + response.code());
                }
            }
            
        } catch (Exception e) {
            result.setSuccess(false);
            result.setMessage("Interception failed: " + e.getMessage());
            log.error("Interception error", e);
        }
        
        return result;
    }

    /**
     * PHASE 2: ATTACK - Attempt quantum decryption on harvested data
     */
    public QuantumAttackReport executeQuantumAttack() {
        log.warn("⚛️ QUANTUM ATTACK: Deploying Shor's algorithm against {} intercepted transactions", 
                harvestedData.size());
        
        QuantumAttackReport report = new QuantumAttackReport();
        report.setAttackStartTime(LocalDateTime.now());
        report.setGpuInfo(quantumSimulator.getGpuInfo());
        report.setTotalTargets(harvestedData.size());
        
        List<AttackResult> results = new ArrayList<>();
        int rsaBroken = 0;
        int pqcProtected = 0;
        
        for (InterceptedTransaction tx : harvestedData) {
            AttackResult ar = attackTransaction(tx);
            results.add(ar);
            
            if (ar.isDecrypted()) {
                rsaBroken++;
            } else if (ar.getAlgorithm().contains("ML-KEM") || ar.getAlgorithm().contains("ML-DSA")) {
                pqcProtected++;
            }
        }
        
        report.setAttackResults(results);
        report.setRsaKeysBroken(rsaBroken);
        report.setPqcProtectedCount(pqcProtected);
        report.setAttackEndTime(LocalDateTime.now());
        
        // Generate attack summary
        if (rsaBroken > 0) {
            report.setOverallResult("⚠️ CRITICAL: " + rsaBroken + " RSA-encrypted documents DECRYPTED!");
            report.setSeverity("CRITICAL");
        } else if (pqcProtected > 0) {
            report.setOverallResult("🛡️ PROTECTED: All documents using Post-Quantum Cryptography remain secure");
            report.setSeverity("SECURE");
        } else {
            report.setOverallResult("⏳ PENDING: Insufficient quantum resources for attack");
            report.setSeverity("UNKNOWN");
        }
        
        log.warn("📊 ATTACK COMPLETE: {} RSA broken, {} PQC protected", rsaBroken, pqcProtected);
        
        return report;
    }

    /**
     * Attack a single intercepted transaction
     */
    private AttackResult attackTransaction(InterceptedTransaction tx) {
        AttackResult result = new AttackResult();
        result.setDocumentId(tx.getDocumentId());
        result.setDocumentType(tx.getDocumentType());
        result.setAlgorithm(tx.getEncryptionAlgorithm());
        
        String algo = tx.getEncryptionAlgorithm().toUpperCase();
        
        if (algo.contains("RSA")) {
            // RSA is vulnerable to Shor's algorithm
            int keyBits = algo.contains("4096") ? 4096 : 2048;
            BigInteger fakeModulus = generateFakeRsaModulus(keyBits);
            
            CuQuantumGpuSimulator.ShorsAttackResult shorsResult = 
                    quantumSimulator.simulateShorsAlgorithm(fakeModulus, keyBits);
            
            result.setDecrypted(shorsResult.isSuccess());
            result.setAttackType("Shor's Algorithm");
            result.setQubitsUsed(shorsResult.getQubitsRequired());
            result.setAttackTimeMs(shorsResult.getExecutionTimeMs());
            result.setDetails(shorsResult.getMessage());
            
            if (shorsResult.isSuccess()) {
                result.setDecryptedPreview("[DECRYPTED] Document for " + tx.getApplicant() + 
                        " - " + tx.getDocumentType());
            }
            
        } else if (algo.contains("ML-KEM") || algo.contains("KYBER")) {
            // ML-KEM is quantum-resistant
            CuQuantumGpuSimulator.LatticeAttackResult latticeResult = 
                    quantumSimulator.simulateLatticeAttack(algo, tx.getEncryptedPayload());
            
            result.setDecrypted(false);
            result.setAttackType("Lattice Reduction (Quantum-Enhanced BKZ)");
            result.setQubitsUsed(0);  // Lattice attacks don't use qubits directly
            result.setAttackTimeMs(latticeResult.getExecutionTimeMs());
            result.setDetails(latticeResult.getMessage());
            
        } else if (algo.contains("ML-DSA") || algo.contains("DILITHIUM")) {
            // ML-DSA signatures are quantum-resistant
            result.setDecrypted(false);
            result.setAttackType("Signature Forgery Attempt");
            result.setDetails("ML-DSA signatures cannot be forged with quantum computers. " +
                    "Based on Module-LWE hard problem.");
            
        } else if (algo.contains("AES")) {
            // AES with Grover's algorithm
            result.setDecrypted(false);
            result.setAttackType("Grover's Algorithm (Key Search)");
            result.setDetails("AES-256 remains secure. Grover's provides sqrt speedup, " +
                    "reducing security from 256-bit to 128-bit (still infeasible).");
            
        } else {
            result.setDecrypted(false);
            result.setAttackType("Unknown Algorithm");
            result.setDetails("No quantum attack available for: " + algo);
        }
        
        return result;
    }

    /**
     * Generate simulated encrypted data payload
     */
    private byte[] generateSimulatedEncryptedData(String algorithm) {
        // Generate realistic-looking encrypted data
        int size = algorithm.contains("ML-KEM") ? 1088 : 256;  // ML-KEM has larger ciphertexts
        byte[] data = new byte[size];
        secureRandom.nextBytes(data);
        return data;
    }

    /**
     * Extract key metadata for attack planning
     */
    private KeyMetadata extractKeyMetadata(String algorithm) {
        KeyMetadata meta = new KeyMetadata();
        meta.setAlgorithm(algorithm);
        
        if (algorithm.contains("RSA-2048")) {
            meta.setKeySize(2048);
            meta.setQuantumVulnerable(true);
            meta.setEstimatedQubits(4099);  // 2*2048 + 3
            meta.setEstimatedAttackTime("~8 hours (future quantum computer)");
        } else if (algorithm.contains("RSA-4096")) {
            meta.setKeySize(4096);
            meta.setQuantumVulnerable(true);
            meta.setEstimatedQubits(8195);
            meta.setEstimatedAttackTime("~32 hours (future quantum computer)");
        } else if (algorithm.contains("ML-KEM")) {
            meta.setKeySize(768);  // ML-KEM-768
            meta.setQuantumVulnerable(false);
            meta.setEstimatedQubits(0);
            meta.setEstimatedAttackTime("Infeasible (quantum-resistant)");
        } else if (algorithm.contains("ML-DSA")) {
            meta.setKeySize(2048);
            meta.setQuantumVulnerable(false);
            meta.setEstimatedQubits(0);
            meta.setEstimatedAttackTime("Infeasible (quantum-resistant)");
        }
        
        return meta;
    }

    /**
     * Generate a fake RSA modulus for simulation
     */
    private BigInteger generateFakeRsaModulus(int bits) {
        // For simulation, generate a product of two primes
        BigInteger p = BigInteger.probablePrime(bits / 2, secureRandom);
        BigInteger q = BigInteger.probablePrime(bits / 2, secureRandom);
        return p.multiply(q);
    }

    /**
     * Get current harvested data count
     */
    public int getHarvestedCount() {
        return harvestedData.size();
    }

    /**
     * Clear harvested data
     */
    public void clearHarvestedData() {
        harvestedData.clear();
    }

    // ==================== Data Classes ====================

    @Data
    public static class InterceptionResult {
        private LocalDateTime timestamp;
        private String targetUrl;
        private boolean success;
        private String rawDataCaptured;
        private int transactionCount;
        private List<InterceptedTransaction> interceptedTransactions;
        private String message;
    }

    @Data
    public static class InterceptedTransaction {
        private String documentId;
        private String documentType;
        private String applicant;
        private String encryptionAlgorithm;
        private String status;
        private LocalDateTime interceptedAt;
        private byte[] encryptedPayload;
        private KeyMetadata keyMetadata;
    }

    @Data
    public static class KeyMetadata {
        private String algorithm;
        private int keySize;
        private boolean quantumVulnerable;
        private int estimatedQubits;
        private String estimatedAttackTime;
    }

    @Data
    public static class QuantumAttackReport {
        private LocalDateTime attackStartTime;
        private LocalDateTime attackEndTime;
        private CuQuantumGpuSimulator.GpuInfo gpuInfo;
        private int totalTargets;
        private List<AttackResult> attackResults;
        private int rsaKeysBroken;
        private int pqcProtectedCount;
        private String overallResult;
        private String severity;
    }

    @Data
    public static class AttackResult {
        private String documentId;
        private String documentType;
        private String algorithm;
        private String attackType;
        private boolean decrypted;
        private int qubitsUsed;
        private long attackTimeMs;
        private String details;
        private String decryptedPreview;
    }
}
