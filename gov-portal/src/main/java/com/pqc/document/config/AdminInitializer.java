package com.pqc.document.config;

import com.pqc.crypto.PqcCryptoService;
import com.pqc.document.entity.User;
import com.pqc.document.entity.User.UserRole;
import com.pqc.document.repository.UserRepository;
import com.pqc.model.CryptoAlgorithm;
import com.pqc.model.KeyPairResult;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

/**
 * Initialize admin user and demo data on startup.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class AdminInitializer implements CommandLineRunner {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final PqcCryptoService cryptoService = new PqcCryptoService();

    @Value("${admin.username:admin}")
    private String adminUsername;

    @Value("${admin.email:admin@pqc-cybersec.local}")
    private String adminEmail;

    @Value("${admin.password:Admin@PQC2024!}")
    private String adminPassword;

    @Value("${admin.fullname:System Administrator}")
    private String adminFullName;

    @Override
    public void run(String... args) throws Exception {
        log.info("Checking for admin user...");

        // Create admin if not exists
        if (!userRepository.existsByUsername(adminUsername)) {
            createAdminUser();
        } else {
            log.info("Admin user already exists");
        }

        // Create demo users if needed
        if (userRepository.count() < 3) {
            createDemoUsers();
        }

        printWelcomeMessage();
    }

    private void createAdminUser() throws Exception {
        log.info("Creating admin user...");

        String userId = cryptoService.hashSHA384AsString(
                adminUsername + adminEmail + System.currentTimeMillis());

        KeyPairResult mlDsaKeys = cryptoService.generateMLDSAKeyPair();
        KeyPairResult mlKemKeys = cryptoService.generateMLKEMKeyPair();
        KeyPairResult rsaKeys = cryptoService.generateRSAKeyPair();

        User admin = User.builder()
                .userId(userId)
                .username(adminUsername)
                .email(adminEmail)
                .passwordHash(passwordEncoder.encode(adminPassword))
                .fullName(adminFullName)
                .role(UserRole.ADMIN)
                .mlDsaPublicKey(mlDsaKeys.getPublicKey())
                .mlDsaPrivateKey(mlDsaKeys.getPrivateKey())
                .mlKemPublicKey(mlKemKeys.getPublicKey())
                .mlKemPrivateKey(mlKemKeys.getPrivateKey())
                .rsaPublicKey(rsaKeys.getPublicKey())
                .rsaPrivateKey(rsaKeys.getPrivateKey())
                .preferredSignatureAlgorithm(CryptoAlgorithm.ML_DSA)
                .preferredEncryptionAlgorithm(CryptoAlgorithm.ML_KEM)
                .keyGeneratedAt(LocalDateTime.now())
                .verified(true)
                .active(true)
                .build();

        userRepository.save(admin);
        log.info("Admin user created: {}", adminUsername);
    }

    private void createDemoUsers() throws Exception {
        log.info("Creating demo users...");

        // Create officer
        if (!userRepository.existsByUsername("officer")) {
            createUser("officer", "officer@pqc-cybersec.local", "Officer@2024!",
                    "John Officer", UserRole.OFFICER);
        }

        // Create citizens with specific names for demo
        if (!userRepository.existsByUsername("john.citizen")) {
            createUser("john.citizen", "john.citizen@email.com", "Citizen@2024!",
                    "John Smith", UserRole.CITIZEN);
        }

        if (!userRepository.existsByUsername("emily.chen")) {
            createUser("emily.chen", "emily.chen@email.com", "Citizen@2024!",
                    "Emily Chen", UserRole.CITIZEN);
        }

        // Legacy citizen account for backward compatibility
        if (!userRepository.existsByUsername("citizen")) {
            createUser("citizen", "citizen@pqc-cybersec.local", "Citizen@2024!",
                    "Jane Citizen", UserRole.CITIZEN);
        }

        log.info("Demo users created");
    }

    private void createUser(String username, String email, String password,
            String fullName, UserRole role) throws Exception {
        String userId = cryptoService.hashSHA384AsString(
                username + email + System.currentTimeMillis());

        KeyPairResult mlDsaKeys = cryptoService.generateMLDSAKeyPair();
        KeyPairResult mlKemKeys = cryptoService.generateMLKEMKeyPair();
        KeyPairResult rsaKeys = cryptoService.generateRSAKeyPair();

        User user = User.builder()
                .userId(userId)
                .username(username)
                .email(email)
                .passwordHash(passwordEncoder.encode(password))
                .fullName(fullName)
                .role(role)
                .mlDsaPublicKey(mlDsaKeys.getPublicKey())
                .mlDsaPrivateKey(mlDsaKeys.getPrivateKey())
                .mlKemPublicKey(mlKemKeys.getPublicKey())
                .mlKemPrivateKey(mlKemKeys.getPrivateKey())
                .rsaPublicKey(rsaKeys.getPublicKey())
                .rsaPrivateKey(rsaKeys.getPrivateKey())
                .preferredSignatureAlgorithm(CryptoAlgorithm.ML_DSA)
                .preferredEncryptionAlgorithm(CryptoAlgorithm.ML_KEM)
                .keyGeneratedAt(LocalDateTime.now())
                .verified(true)
                .active(true)
                .build();

        userRepository.save(user);
        log.info("Created demo user: {} ({})", username, role);
    }

    private void printWelcomeMessage() {
        log.info("""
                
                ╔══════════════════════════════════════════════════════════════════╗
                ║         PQC Government Portal - READY                            ║
                ╠══════════════════════════════════════════════════════════════════╣
                ║                                                                  ║
                ║  🌐 Web Interface: http://localhost:8181                         ║
                ║                                                                  ║
                ║  Demo Accounts (for Web Login):                                  ║
                ║  ─────────────────────────────────────────────────────────────── ║
                ║  👑 Admin:      admin / Admin@PQC2024!                           ║
                ║  👨‍💼 Officer:    officer / Officer@2024!                          ║
                ║  👤 John:       john.citizen / Citizen@2024!                     ║
                ║  👤 Emily:      emily.chen / Citizen@2024!                       ║
                ║                                                                  ║
                ║  Available Services:                                             ║
                ║  ─────────────────────────────────────────────────────────────── ║
                ║  🚗 Car License:   /services/car-license                         ║
                ║  💰 Tax Filing:    /services/tax-filing                          ║
                ║                                                                  ║
                ║  API Endpoints:                                                  ║
                ║  ─────────────────────────────────────────────────────────────── ║
                ║  GET  /api/transactions   - Transaction log for hacker monitor  ║
                ║  POST /api/auth/login     - Login and get JWT                    ║
                ║  GET  /api/users/me       - Get current user                     ║
                ║                                                                  ║
                ╚══════════════════════════════════════════════════════════════════╝
                """);
    }
}
