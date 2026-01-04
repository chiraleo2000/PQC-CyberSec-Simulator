package com.pqc.hacker.quantum;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.math.BigInteger;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.List;
import java.util.Map;

/**
 * Python cuQuantum Service Client
 * 
 * This client connects to the Python cuQuantum service running on port 8184
 * which provides REAL GPU-accelerated quantum simulation using NVIDIA cuQuantum SDK.
 * 
 * The Python service offers:
 * - Shor's Algorithm for RSA factorization
 * - Grover's Algorithm for key search
 * - State vector simulation on RTX 4060 GPU
 * 
 * @author PQC CyberSec Simulator
 */
@Service
@Slf4j
public class PythonQuantumClient {
    
    private final HttpClient httpClient;
    private final ObjectMapper objectMapper;
    private final String quantumServiceUrl;
    private boolean serviceAvailable = false;
    
    public PythonQuantumClient(
            @Value("${quantum.service.url:http://localhost:8184}") String quantumServiceUrl) {
        this.quantumServiceUrl = quantumServiceUrl;
        this.httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(10))
                .build();
        this.objectMapper = new ObjectMapper();
        
        // Check if Python quantum service is available
        checkServiceAvailability();
    }
    
    private void checkServiceAvailability() {
        try {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(quantumServiceUrl + "/api/quantum/status"))
                    .timeout(Duration.ofSeconds(3))
                    .GET()
                    .build();
            
            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            serviceAvailable = response.statusCode() == 200;
            
            if (serviceAvailable) {
                log.info("✅ Python cuQuantum service connected at {}", quantumServiceUrl);
            }
        } catch (Exception e) {
            log.warn("⚠️ Python cuQuantum service not available at {}. Using Java simulation fallback.", 
                    quantumServiceUrl);
            serviceAvailable = false;
        }
    }
    
    public boolean isServiceAvailable() {
        return serviceAvailable;
    }
    
    /**
     * Get quantum simulator status from Python service
     */
    public QuantumStatus getStatus() {
        if (!serviceAvailable) {
            return createFallbackStatus();
        }
        
        try {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(quantumServiceUrl + "/api/quantum/status"))
                    .timeout(Duration.ofSeconds(10))
                    .GET()
                    .build();
            
            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            return objectMapper.readValue(response.body(), QuantumStatus.class);
            
        } catch (Exception e) {
            log.error("Error getting quantum status: {}", e.getMessage());
            return createFallbackStatus();
        }
    }
    
    private QuantumStatus createFallbackStatus() {
        QuantumStatus status = new QuantumStatus();
        status.setService("Java cuQuantum Fallback");
        status.setCuquantumAvailable(false);
        status.setCupyAvailable(false);
        return status;
    }
    
    /**
     * Execute Shor's Algorithm via Python cuQuantum service
     */
    public ShorsResponse runShorsAlgorithm(BigInteger modulus, int keyBits) {
        if (!serviceAvailable) {
            return createFallbackShorsResponse(modulus, keyBits);
        }
        
        try {
            String requestBody = objectMapper.writeValueAsString(Map.of(
                    "modulus", modulus.intValue(),
                    "key_bits", keyBits
            ));
            
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(quantumServiceUrl + "/api/quantum/shor"))
                    .header("Content-Type", "application/json")
                    .timeout(Duration.ofSeconds(60))
                    .POST(HttpRequest.BodyPublishers.ofString(requestBody))
                    .build();
            
            log.info("⚛️ Calling Python cuQuantum Shor's algorithm: modulus={}, keyBits={}", modulus, keyBits);
            
            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            ShorsResponse result = objectMapper.readValue(response.body(), ShorsResponse.class);
            
            log.info("✅ Shor's algorithm result: {}", result.getVulnerability());
            return result;
            
        } catch (Exception e) {
            log.error("Error running Shor's algorithm: {}", e.getMessage());
            return createFallbackShorsResponse(modulus, keyBits);
        }
    }
    
    private ShorsResponse createFallbackShorsResponse(BigInteger modulus, int keyBits) {
        ShorsResponse response = new ShorsResponse();
        response.setAlgorithm("Shor's Algorithm (Java Fallback)");
        response.setPurpose("RSA Factorization");
        response.setVulnerability("RSA is VULNERABLE to quantum attacks (simulated)");
        response.setRecommendation("Migrate to ML-KEM (Kyber) for quantum-safe encryption");
        
        ShorsResult result = new ShorsResult();
        result.setSuccess(true);
        result.setModulus(modulus.longValue());
        result.setQubitsUsed(keyBits * 2);
        result.setGpuName("Java CPU Simulation");
        response.setResult(result);
        
        return response;
    }
    
    /**
     * Execute Grover's Algorithm via Python cuQuantum service
     */
    public GroversResponse runGroversAlgorithm(int keyBits) {
        if (!serviceAvailable) {
            return createFallbackGroversResponse(keyBits);
        }
        
        try {
            String requestBody = objectMapper.writeValueAsString(Map.of(
                    "key_bits", keyBits
            ));
            
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(quantumServiceUrl + "/api/quantum/grover"))
                    .header("Content-Type", "application/json")
                    .timeout(Duration.ofSeconds(60))
                    .POST(HttpRequest.BodyPublishers.ofString(requestBody))
                    .build();
            
            log.info("🔍 Calling Python cuQuantum Grover's algorithm: keyBits={}", keyBits);
            
            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            return objectMapper.readValue(response.body(), GroversResponse.class);
            
        } catch (Exception e) {
            log.error("Error running Grover's algorithm: {}", e.getMessage());
            return createFallbackGroversResponse(keyBits);
        }
    }
    
    private GroversResponse createFallbackGroversResponse(int keyBits) {
        GroversResponse response = new GroversResponse();
        response.setAlgorithm("Grover's Algorithm (Java Fallback)");
        response.setPurpose("Symmetric Key Search");
        response.setRecommendation("Use AES-256 for 128-bit post-quantum security");
        return response;
    }
    
    /**
     * Execute RSA attack via Python cuQuantum service
     */
    public RsaAttackResponse attackRsa(int keySize) {
        if (!serviceAvailable) {
            return createFallbackRsaAttackResponse(keySize);
        }
        
        try {
            String requestBody = objectMapper.writeValueAsString(Map.of(
                    "key_size", keySize
            ));
            
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(quantumServiceUrl + "/api/quantum/attack/rsa"))
                    .header("Content-Type", "application/json")
                    .timeout(Duration.ofSeconds(60))
                    .POST(HttpRequest.BodyPublishers.ofString(requestBody))
                    .build();
            
            log.warn("⚛️ Quantum RSA-{} attack via Python cuQuantum...", keySize);
            
            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            return objectMapper.readValue(response.body(), RsaAttackResponse.class);
            
        } catch (Exception e) {
            log.error("Error attacking RSA: {}", e.getMessage());
            return createFallbackRsaAttackResponse(keySize);
        }
    }
    
    private RsaAttackResponse createFallbackRsaAttackResponse(int keySize) {
        RsaAttackResponse response = new RsaAttackResponse();
        response.setAttackType("Shor's Algorithm (Java Fallback)");
        response.setTarget("RSA-" + keySize);
        response.setVerdict("🔓 RSA BROKEN (simulated)");
        return response;
    }
    
    /**
     * Execute lattice attack via Python cuQuantum service
     */
    public LatticeAttackResponse attackLattice(String algorithm, int securityLevel) {
        if (!serviceAvailable) {
            return createFallbackLatticeResponse(algorithm, securityLevel);
        }
        
        try {
            String requestBody = objectMapper.writeValueAsString(Map.of(
                    "algorithm", algorithm,
                    "security_level", securityLevel
            ));
            
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(quantumServiceUrl + "/api/quantum/attack/lattice"))
                    .header("Content-Type", "application/json")
                    .timeout(Duration.ofSeconds(60))
                    .POST(HttpRequest.BodyPublishers.ofString(requestBody))
                    .build();
            
            log.info("🛡️ Quantum lattice attack on {} via Python cuQuantum...", algorithm);
            
            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            return objectMapper.readValue(response.body(), LatticeAttackResponse.class);
            
        } catch (Exception e) {
            log.error("Error attacking lattice crypto: {}", e.getMessage());
            return createFallbackLatticeResponse(algorithm, securityLevel);
        }
    }
    
    private LatticeAttackResponse createFallbackLatticeResponse(String algorithm, int securityLevel) {
        LatticeAttackResponse response = new LatticeAttackResponse();
        response.setAttackType("Quantum-Enhanced BKZ (Java Fallback)");
        response.setTarget(algorithm);
        response.setVerdict("🛡️ " + algorithm + " is QUANTUM RESISTANT");
        response.setRecommendation("ML-KEM/Kyber is safe for post-quantum cryptography");
        return response;
    }
    
    // ========================================================================
    // Response DTOs
    // ========================================================================
    
    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class QuantumStatus {
        private String service;
        private String version;
        private boolean cuquantumAvailable;
        private boolean cupyAvailable;
        private Map<String, Object> gpu;
        private Map<String, Object> capabilities;
        private String timestamp;
    }
    
    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class ShorsResponse {
        private String algorithm;
        private String purpose;
        private ShorsResult result;
        private String vulnerability;
        private String recommendation;
    }
    
    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class ShorsResult {
        private boolean success;
        private long modulus;
        private Long factorP;
        private Long factorQ;
        private int qubitsUsed;
        private double executionTimeMs;
        private String gpuName;
        private List<String> algorithmSteps;
        private String errorMessage;
    }
    
    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class GroversResponse {
        private String algorithm;
        private String purpose;
        private GroversResult result;
        private Map<String, Object> securityAnalysis;
        private String recommendation;
    }
    
    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class GroversResult {
        private boolean success;
        private long searchSpaceSize;
        private Long targetFound;
        private int iterations;
        private int qubitsUsed;
        private double executionTimeMs;
        private String gpuName;
        private double speedupFactor;
        private String errorMessage;
    }
    
    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class RsaAttackResponse {
        private String attackType;
        private String target;
        private long modulus;
        private ShorsResult result;
        private String verdict;
        private Map<String, Object> impact;
    }
    
    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class LatticeAttackResponse {
        private String attackType;
        private String target;
        private LatticeResult result;
        private Map<String, Object> securityAnalysis;
        private String verdict;
        private String recommendation;
    }
    
    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class LatticeResult {
        private boolean success;
        private String reason;
        private double executionTimeMs;
        private String gpuName;
    }
}
