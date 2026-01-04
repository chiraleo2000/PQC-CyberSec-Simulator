package com.pqc.document.controller;

import com.pqc.document.entity.Document;
import com.pqc.document.entity.User;
import com.pqc.document.repository.DocumentRepository;
import com.pqc.document.repository.UserRepository;
import com.pqc.model.CryptoAlgorithm;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import jakarta.servlet.http.HttpSession;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

/**
 * Web Controller for Government Portal UI
 * Provides real interactive forms for car licensing and tax services
 */
@Controller
@RequiredArgsConstructor
@Slf4j
public class WebController {

    private final UserRepository userRepository;
    private final DocumentRepository documentRepository;

    /**
     * Helper method to get current authenticated user from Spring Security
     */
    private User getCurrentUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated() || "anonymousUser".equals(auth.getPrincipal())) {
            return null;
        }
        String username = auth.getName();
        return userRepository.findByUsername(username).orElse(null);
    }

    // ============ HOME & LOGIN ============

    @GetMapping("/")
    public String home(Model model) {
        User user = getCurrentUser();
        model.addAttribute("user", user);
        model.addAttribute("services", getAvailableServices());
        return "index";
    }

    @GetMapping("/login")
    public String loginPage(Model model) {
        // If already logged in, redirect to dashboard
        User user = getCurrentUser();
        if (user != null) {
            return "redirect:/dashboard";
        }
        return "login";
    }

    @GetMapping("/logout")
    public String logout(HttpSession session) {
        session.invalidate();
        return "redirect:/login";
    }

    // ============ DASHBOARD ============

    @GetMapping("/dashboard")
    public String dashboard(Model model) {
        User user = getCurrentUser();
        if (user == null) {
            return "redirect:/login";
        }

        model.addAttribute("user", user);
        
        // Get user's documents
        List<Document> myDocuments = documentRepository.findByApplicantOrderByCreatedAtDesc(user);
        model.addAttribute("myDocuments", myDocuments);
        
        // For officers: get pending documents
        if (user.getRole() == User.UserRole.OFFICER || user.getRole() == User.UserRole.ADMIN) {
            List<Document> pendingDocuments = documentRepository.findByStatus(Document.DocumentStatus.PENDING);
            model.addAttribute("pendingDocuments", pendingDocuments);
        }
        
        return "dashboard";
    }

    // ============ CAR LICENSE SERVICES ============

    @GetMapping("/services/car-license")
    public String carLicenseForm(Model model) {
        User user = getCurrentUser();
        if (user == null) {
            return "redirect:/login";
        }
        model.addAttribute("user", user);
        return "services/car-license";
    }

    @PostMapping("/services/car-license/submit")
    public String submitCarLicense(
            @RequestParam String fullName,
            @RequestParam String dob,
            @RequestParam String address,
            @RequestParam String licenseType,
            @RequestParam String vehiclePlate,
            @RequestParam String vehicleMake,
            @RequestParam String vehicleYear,
            @RequestParam(defaultValue = "RSA_2048") String encryptionType,
            RedirectAttributes redirectAttributes) {
        
        User user = getCurrentUser();
        if (user == null) {
            return "redirect:/login";
        }

        String content = String.format("""
            ╔══════════════════════════════════════════════════════════════╗
            ║           CAR LICENSE APPLICATION                           ║
            ╠══════════════════════════════════════════════════════════════╣
            ║ Full Name:      %s
            ║ Date of Birth:  %s
            ║ Address:        %s
            ║ License Type:   %s
            ╠══════════════════════════════════════════════════════════════╣
            ║                  VEHICLE INFORMATION                        ║
            ╠══════════════════════════════════════════════════════════════╣
            ║ License Plate:  %s
            ║ Vehicle Make:   %s
            ║ Vehicle Year:   %s
            ╠══════════════════════════════════════════════════════════════╣
            ║ Encryption:     %s
            ║ Submitted:      %s
            ╚══════════════════════════════════════════════════════════════╝
            """, fullName, dob, address, licenseType, vehiclePlate, vehicleMake, vehicleYear,
            encryptionType, LocalDateTime.now());

        CryptoAlgorithm algorithm = "ML_KEM".equals(encryptionType) ? 
            CryptoAlgorithm.ML_KEM : CryptoAlgorithm.RSA_2048;

        Document doc = Document.builder()
                .documentId("CAR-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase())
                .documentType(Document.DocumentType.LICENSE)
                .title("Car License Application - " + fullName)
                .content(content)
                .applicant(user)
                .signatureAlgorithm(algorithm)
                .status(Document.DocumentStatus.PENDING)
                .build();

        documentRepository.save(doc);
        
        log.info("📋 Car License Application submitted by {} [Algorithm: {}]", 
                user.getUsername(), algorithm);

        redirectAttributes.addFlashAttribute("success", 
            "Car License Application submitted successfully! Document ID: " + doc.getDocumentId());
        redirectAttributes.addFlashAttribute("encryptionUsed", encryptionType);
        
        return "redirect:/dashboard";
    }

    // ============ TAX FILING SERVICES ============

    @GetMapping("/services/tax-filing")
    public String taxFilingForm(Model model) {
        User user = getCurrentUser();
        if (user == null) {
            return "redirect:/login";
        }
        model.addAttribute("user", user);
        return "services/tax-filing";
    }

    @PostMapping("/services/tax-filing/submit")
    public String submitTaxFiling(
            @RequestParam String fullName,
            @RequestParam String taxId,
            @RequestParam String filingYear,
            @RequestParam String grossIncome,
            @RequestParam String deductions,
            @RequestParam String taxOwed,
            @RequestParam String bankAccount,
            @RequestParam(defaultValue = "RSA_2048") String encryptionType,
            RedirectAttributes redirectAttributes) {
        
        User user = getCurrentUser();
        if (user == null) {
            return "redirect:/login";
        }

        String content = String.format("""
            ╔══════════════════════════════════════════════════════════════╗
            ║           TAX FILING %s                                     ║
            ╠══════════════════════════════════════════════════════════════╣
            ║ Taxpayer:       %s
            ║ Tax ID:         %s
            ╠══════════════════════════════════════════════════════════════╣
            ║                  FINANCIAL INFORMATION                      ║
            ╠══════════════════════════════════════════════════════════════╣
            ║ Gross Income:   $%s
            ║ Deductions:     $%s
            ║ Tax Owed:       $%s
            ╠══════════════════════════════════════════════════════════════╣
            ║ Bank Account:   ****%s (Direct Deposit)
            ╠══════════════════════════════════════════════════════════════╣
            ║ Encryption:     %s
            ║ Filed:          %s
            ╚══════════════════════════════════════════════════════════════╝
            """, filingYear, fullName, taxId, grossIncome, deductions, taxOwed,
            bankAccount.length() > 4 ? bankAccount.substring(bankAccount.length() - 4) : bankAccount,
            encryptionType, LocalDateTime.now());

        CryptoAlgorithm algorithm = "ML_KEM".equals(encryptionType) ? 
            CryptoAlgorithm.ML_KEM : CryptoAlgorithm.RSA_2048;

        Document doc = Document.builder()
                .documentId("TAX-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase())
                .documentType(Document.DocumentType.REGISTRATION)
                .title("Tax Filing " + filingYear + " - " + fullName)
                .content(content)
                .applicant(user)
                .signatureAlgorithm(algorithm)
                .status(Document.DocumentStatus.PENDING)
                .build();

        documentRepository.save(doc);
        
        log.info("💰 Tax Filing submitted by {} for year {} [Algorithm: {}]", 
                user.getUsername(), filingYear, algorithm);

        redirectAttributes.addFlashAttribute("success", 
            "Tax Filing submitted successfully! Document ID: " + doc.getDocumentId());
        redirectAttributes.addFlashAttribute("encryptionUsed", encryptionType);
        
        return "redirect:/dashboard";
    }

    // ============ OFFICER ACTIONS ============

    @GetMapping("/officer/review/{documentId}")
    public String reviewDocument(@PathVariable String documentId, Model model) {
        User user = getCurrentUser();
        if (user == null || (user.getRole() != User.UserRole.OFFICER && user.getRole() != User.UserRole.ADMIN)) {
            return "redirect:/login";
        }

        Document doc = documentRepository.findByDocumentId(documentId).orElse(null);
        if (doc == null) {
            return "redirect:/dashboard";
        }

        model.addAttribute("user", user);
        model.addAttribute("document", doc);
        return "officer/review";
    }

    @PostMapping("/officer/approve/{documentId}")
    public String approveDocument(@PathVariable String documentId, 
                                   RedirectAttributes redirectAttributes) {
        User user = getCurrentUser();
        if (user == null || (user.getRole() != User.UserRole.OFFICER && user.getRole() != User.UserRole.ADMIN)) {
            return "redirect:/login";
        }

        Document doc = documentRepository.findByDocumentId(documentId).orElse(null);
        if (doc != null) {
            doc.setStatus(Document.DocumentStatus.APPROVED);
            doc.setSigner(user);
            doc.setSignedAt(LocalDateTime.now());
            documentRepository.save(doc);
            
            log.info("✅ Document {} APPROVED by Officer {}", documentId, user.getUsername());
            redirectAttributes.addFlashAttribute("success", "Document " + documentId + " approved!");
        }

        return "redirect:/dashboard";
    }

    @PostMapping("/officer/reject/{documentId}")
    public String rejectDocument(@PathVariable String documentId,
                                  @RequestParam(required = false) String reason,
                                  RedirectAttributes redirectAttributes) {
        User user = getCurrentUser();
        if (user == null || (user.getRole() != User.UserRole.OFFICER && user.getRole() != User.UserRole.ADMIN)) {
            return "redirect:/login";
        }

        Document doc = documentRepository.findByDocumentId(documentId).orElse(null);
        if (doc != null) {
            doc.setStatus(Document.DocumentStatus.REJECTED);
            doc.setSigner(user);
            doc.setSignedAt(LocalDateTime.now());
            documentRepository.save(doc);
            
            log.info("❌ Document {} REJECTED by Officer {}", documentId, user.getUsername());
            redirectAttributes.addFlashAttribute("error", "Document " + documentId + " rejected.");
        }

        return "redirect:/dashboard";
    }

    // ============ TRANSACTION LOG (For Hacker Monitoring) ============

    @GetMapping("/api/transactions")
    @ResponseBody
    public List<TransactionLog> getTransactionLog() {
        List<Document> recentDocs = documentRepository.findTop20ByOrderByCreatedAtDesc();
        return recentDocs.stream().map(doc -> {
            // Get encryption algorithm from the applicant's preferred settings
            String encryptionAlgo = "RSA-2048";  // Default to vulnerable encryption for demo
            if (doc.getApplicant() != null && doc.getApplicant().getPreferredEncryptionAlgorithm() != null) {
                encryptionAlgo = doc.getApplicant().getPreferredEncryptionAlgorithm().name();
            }
            // If signature is PQC, encryption should also be PQC
            if (doc.getSignatureAlgorithm() != null && 
                (doc.getSignatureAlgorithm().name().contains("ML_DSA") || 
                 doc.getSignatureAlgorithm().name().contains("ML_KEM"))) {
                encryptionAlgo = "ML-KEM-768";
            }
            
            return new TransactionLog(
                doc.getDocumentId(),
                doc.getDocumentType().getDisplayName(),
                doc.getApplicant() != null ? doc.getApplicant().getUsername() : "Unknown",
                encryptionAlgo,
                doc.getSignatureAlgorithm() != null ? doc.getSignatureAlgorithm().name() : "NONE",
                doc.getStatus().name(),
                doc.getCreatedAt()
            );
        }).toList();
    }

    // ============ HELPER METHODS ============

    private List<ServiceInfo> getAvailableServices() {
        return List.of(
            new ServiceInfo("car-license", "🚗 Car License", "Apply for new driver's license or renewal"),
            new ServiceInfo("tax-filing", "💰 Tax Filing", "Submit your annual tax documents"),
            new ServiceInfo("passport", "🛂 Passport", "Apply for passport services"),
            new ServiceInfo("permits", "📋 Permits", "Various permit applications")
        );
    }

    // ============ INNER CLASSES ============

    public record ServiceInfo(String id, String name, String description) {}
    
    public record TransactionLog(
        String documentId, 
        String type, 
        String applicant, 
        String encryption,
        String signature,
        String status,
        LocalDateTime timestamp
    ) {}
}
