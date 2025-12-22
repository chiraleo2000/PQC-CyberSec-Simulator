package com.pqc.hacker.controller;

import com.pqc.hacker.entity.AttackAttempt;
import com.pqc.hacker.entity.HarvestedData;
import com.pqc.hacker.quantum.QuantumProviderService;
import com.pqc.hacker.service.InterceptionService;
import com.pqc.hacker.service.QuantumAttackService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * Hacker Simulation API Controller
 * 
 * Provides endpoints for demonstrating quantum computing threats:
 * - Network interception (document/message harvesting)
 * - Quantum attack execution (Shor's, Grover's)
 * - HNDL (Harvest Now, Decrypt Later) scenarios
 * - Educational attack scenarios
 * 
 * ⚠️ EDUCATIONAL PURPOSES ONLY
 */
@RestController
@RequestMapping("/api/hacker")
@RequiredArgsConstructor
@Slf4j
@CrossOrigin(origins = "*")
public class HackerSimulationController {

        private final InterceptionService interceptionService;
        private final QuantumAttackService quantumAttackService;
        private final QuantumProviderService quantumProviderService;

        // ==================== System Status ====================

        /**
         * Get quantum provider status and configuration.
         * GET /api/hacker/status
         */
        @GetMapping("/status")
        public ResponseEntity<?> getStatus() {
                return ResponseEntity.ok(Map.of(
                                "service", "PQC Hacker Simulation",
                                "purpose", "EDUCATIONAL DEMONSTRATION ONLY",
                                "quantumProviders", quantumProviderService.getProviderStatus(),
                                "harvestStatistics", interceptionService.getStatistics(),
                                "attackStatistics", quantumAttackService.getStatistics(),
                                "disclaimer",
                                "This service demonstrates quantum computing threats for awareness purposes."));
        }

        // ==================== Data Interception ====================

        /**
         * Intercept a document from the User Document Service.
         * POST /api/hacker/intercept/document
         */
        @PostMapping("/intercept/document")
        public ResponseEntity<?> interceptDocument(@RequestBody InterceptRequest request) {
                log.warn("🕵️ INTERCEPTION REQUEST: Document {}", request.targetId());

                InterceptionService.InterceptionResult result = interceptionService
                                .interceptDocument(request.targetId());

                return ResponseEntity.ok(result);
        }

        /**
         * Intercept a message from the Messaging Service.
         * POST /api/hacker/intercept/message
         */
        @PostMapping("/intercept/message")
        public ResponseEntity<?> interceptMessage(@RequestBody InterceptRequest request) {
                log.warn("🕵️ INTERCEPTION REQUEST: Message {}", request.targetId());

                InterceptionService.InterceptionResult result = interceptionService
                                .interceptMessage(request.targetId());

                return ResponseEntity.ok(result);
        }

        /**
         * Bulk harvest all available messages.
         * POST /api/hacker/intercept/bulk
         */
        @PostMapping("/intercept/bulk")
        public ResponseEntity<?> bulkHarvest() {
                log.warn("🕵️ BULK HARVEST REQUEST");

                InterceptionService.BulkHarvestResult result = interceptionService.bulkHarvestMessages();

                return ResponseEntity.ok(result);
        }

        /**
         * Get all harvested data.
         * GET /api/hacker/harvested
         */
        @GetMapping("/harvested")
        public ResponseEntity<List<HarvestedData>> getHarvestedData() {
                return ResponseEntity.ok(interceptionService.getHarvestedData());
        }

        /**
         * Get quantum-vulnerable harvested data.
         * GET /api/hacker/harvested/vulnerable
         */
        @GetMapping("/harvested/vulnerable")
        public ResponseEntity<List<HarvestedData>> getVulnerableData() {
                return ResponseEntity.ok(interceptionService.getQuantumVulnerableData());
        }

        // ==================== Quantum Attacks ====================

        /**
         * Execute full HNDL (Harvest Now, Decrypt Later) attack scenario.
         * POST /api/hacker/attack/hndl
         */
        @PostMapping("/attack/hndl")
        public ResponseEntity<?> executeHNDLAttack(@RequestBody HNDLRequest request) {
                log.warn("🚨 HNDL ATTACK: {} {}", request.targetType(), request.targetId());

                QuantumAttackService.HNDLAttackResult result = quantumAttackService
                                .executeHNDLScenario(request.targetId(), request.targetType());

                return ResponseEntity.ok(result);
        }

        /**
         * Execute Shor's algorithm attack on harvested RSA data.
         * POST /api/hacker/attack/shor
         */
        @PostMapping("/attack/shor")
        public ResponseEntity<?> executeShorAttack(@RequestBody AttackRequest request) {
                log.warn("🚨 SHOR'S ATTACK: {}", request.harvestId());

                try {
                        AttackAttempt result = quantumAttackService.executeShorAttack(request.harvestId());
                        return ResponseEntity.ok(result);
                } catch (IllegalArgumentException e) {
                        return ResponseEntity.badRequest().body(Map.of(
                                        "error", e.getMessage(),
                                        "note", "Shor's algorithm only works on RSA-encrypted data"));
                }
        }

        /**
         * Execute Grover's algorithm attack on harvested AES data.
         * POST /api/hacker/attack/grover
         */
        @PostMapping("/attack/grover")
        public ResponseEntity<?> executeGroverAttack(@RequestBody AttackRequest request) {
                log.warn("🚨 GROVER'S ATTACK: {}", request.harvestId());

                try {
                        AttackAttempt result = quantumAttackService.executeGroverAttack(request.harvestId());
                        return ResponseEntity.ok(result);
                } catch (IllegalArgumentException e) {
                        return ResponseEntity.badRequest().body(Map.of(
                                        "error", e.getMessage(),
                                        "note", "Grover's algorithm only works on AES-encrypted data"));
                }
        }

        /**
         * Attack all quantum-vulnerable harvested data.
         * POST /api/hacker/attack/all
         */
        @PostMapping("/attack/all")
        public ResponseEntity<?> attackAll() {
                log.warn("🚨 BULK QUANTUM ATTACK");

                List<AttackAttempt> results = quantumAttackService.attackAllVulnerable();

                return ResponseEntity.ok(Map.of(
                                "attacksExecuted", results.size(),
                                "results", results));
        }

        /**
         * Get attack history.
         * GET /api/hacker/attacks
         */
        @GetMapping("/attacks")
        public ResponseEntity<List<AttackAttempt>> getAttackHistory() {
                return ResponseEntity.ok(quantumAttackService.getAttackHistory());
        }

        /**
         * Get attack statistics.
         * GET /api/hacker/statistics
         */
        @GetMapping("/statistics")
        public ResponseEntity<?> getStatistics() {
                return ResponseEntity.ok(quantumAttackService.getStatistics());
        }

        // ==================== Educational Scenarios ====================

        /**
         * Get available attack scenarios with educational descriptions.
         * GET /api/hacker/scenarios
         */
        @GetMapping("/scenarios")
        public ResponseEntity<?> getScenarios() {
                return ResponseEntity.ok(List.of(
                                Map.of(
                                                "id", "HNDL_RSA",
                                                "name", "Harvest Now, Decrypt Later (RSA)",
                                                "description",
                                                "Demonstrates capturing RSA-encrypted data and decrypting with Shor's algorithm",
                                                "targetAlgorithm", "RSA-2048",
                                                "quantumAttack", "Shor's Algorithm",
                                                "classicalBreakTime", "300+ trillion years",
                                                "quantumBreakTime", "~8 hours",
                                                "education",
                                                "Shows why RSA must be replaced before quantum computers mature"),
                                Map.of(
                                                "id", "HNDL_AES",
                                                "name", "Grover's Attack on AES",
                                                "description",
                                                "Demonstrates Grover's algorithm reducing AES security by half",
                                                "targetAlgorithm", "AES-128/256",
                                                "quantumAttack", "Grover's Algorithm",
                                                "securityReduction", "AES-128: 128→64 bits, AES-256: 256→128 bits",
                                                "education",
                                                "Shows quadratic speedup but AES-256 remains practically secure"),
                                Map.of(
                                                "id", "PQC_PROTECTION",
                                                "name", "Post-Quantum Protection",
                                                "description",
                                                "Demonstrates how ML-KEM and ML-DSA resist quantum attacks",
                                                "targetAlgorithm", "ML-DSA, ML-KEM",
                                                "quantumAttack", "NONE EFFECTIVE",
                                                "protection", "Complete immunity to Shor's and Grover's algorithms",
                                                "education",
                                                "Shows the importance of migrating to NIST PQC standards NOW"),
                                Map.of(
                                                "id", "TIMING_ATTACK",
                                                "name", "Timing Side-Channel Attack",
                                                "description", "Demonstrates timing analysis to extract secrets",
                                                "targetAlgorithm", "Any with timing leaks",
                                                "classicalAttack", "Statistical timing analysis",
                                                "education", "Shows importance of constant-time implementations")));
        }

        /**
         * Get quantum computing timeline projection.
         * GET /api/hacker/timeline
         */
        @GetMapping("/timeline")
        public ResponseEntity<?> getQuantumTimeline() {
                return ResponseEntity.ok(Map.of(
                                "currentYear", 2024,
                                "milestones", List.of(
                                                Map.of("year", 2024, "event", "IBM Condor: 1,121 qubits available"),
                                                Map.of("year", 2025, "event", "Expected: 5,000 qubit systems"),
                                                Map.of("year", 2027, "event",
                                                                "Projected: Error-corrected logical qubits"),
                                                Map.of("year", 2030, "event",
                                                                "Projected: Cryptographically relevant QC"),
                                                Map.of("year", 2035, "event",
                                                                "Projected: Widespread quantum advantage")),
                                "rsaBreakEstimate", Map.of(
                                                "qubitsNeeded", 4000,
                                                "estimatedYear", "2030-2035",
                                                "recommendation", "Migrate to ML-KEM NOW"),
                                "recommendation", "All sensitive data encrypted today with RSA/classical methods " +
                                                "should be considered compromised by 2035. Migrate to PQC immediately."));
        }

        // ==================== Request DTOs ====================

        public record InterceptRequest(String targetId) {
        }

        public record AttackRequest(String harvestId) {
        }

        public record HNDLRequest(String targetId, String targetType) {
        }
}
