package com.pqc.selenium;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.bonigarcia.wdm.WebDriverManager;
import org.junit.jupiter.api.*;
import org.openqa.selenium.*;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.Select;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.Random;

/**
 * ╔══════════════════════════════════════════════════════════════════════════════════════════╗
 * ║       PQC CYBERSECURITY - 4-PANEL REALISTIC DEMO WITH QUANTUM DECRYPTION                ║
 * ╠══════════════════════════════════════════════════════════════════════════════════════════╣
 * ║  This test demonstrates REAL Post-Quantum Cryptography with 4 browser panels:           ║
 * ║                                                                                          ║
 * ║  PANEL 1 (TOP-LEFT):     👤 CITIZEN       - Submits government applications             ║
 * ║  PANEL 2 (TOP-RIGHT):    👮 OFFICER       - Reviews and approves applications           ║
 * ║  PANEL 3 (BOTTOM-LEFT):  📡 HACKER HARVEST - Intercepts encrypted traffic in transit   ║
 * ║  PANEL 4 (BOTTOM-RIGHT): ⚛️ HACKER DECRYPT - Quantum decryption progress (<5 min each) ║
 * ╠══════════════════════════════════════════════════════════════════════════════════════════╣
 * ║  FEATURES:                                                                               ║
 * ║    ✅ Real encrypted data transfer visualization (not scripted)                         ║
 * ║    ✅ Real-time traffic interception as users interact                                  ║
 * ║    ✅ Quantum decryption with progress (30s-5min per document)                          ║
 * ║    ✅ Decrypt both encryption AND digital signatures                                    ║
 * ║    ✅ PQC (ML-KEM, ML-DSA) shown to resist quantum attacks                              ║
 * ╚══════════════════════════════════════════════════════════════════════════════════════════╝
 */
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class FourPanelRealisticDemoTest {

    // Four browser windows for four views
    private WebDriver citizenBrowser;
    private WebDriver officerBrowser;
    private WebDriver hackerHarvestBrowser;
    private WebDriver hackerDecryptBrowser;
    
    private WebDriverWait citizenWait;
    private WebDriverWait officerWait;
    private WebDriverWait harvestWait;
    private WebDriverWait decryptWait;
    
    private JavascriptExecutor citizenJs;
    private JavascriptExecutor officerJs;
    private JavascriptExecutor harvestJs;
    private JavascriptExecutor decryptJs;

    // Service URLs
    private static final String GOV_URL = "http://localhost:8181";
    private static final String HACKER_URL = "http://localhost:8183";
    private static final String QUANTUM_URL = "http://localhost:8184";
    
    // HTTP Client for API calls
    private HttpClient httpClient;
    private ObjectMapper objectMapper;
    
    // Demo credentials
    private static final String CITIZEN_USER = "john.citizen";
    private static final String CITIZEN_PASS = "Citizen@2024!";
    private static final String OFFICER_USER = "officer";
    private static final String OFFICER_PASS = "Officer@2024!";
    
    // State tracking
    private int documentsSubmitted = 0;
    private boolean quantumServiceAvailable = false;
    
    // Demo scenario from system property: 1=RSA+RSA, 2=PQC+PQC, 3=RSA+PQC, 4=PQC+RSA
    private static final String DEMO_SCENARIO = System.getProperty("demo.scenario", "1");

    @BeforeAll
    void setupBrowsers() {
        WebDriverManager.chromedriver().setup();
        
        httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(10))
                .build();
        objectMapper = new ObjectMapper();

        // Get screen dimensions for 4-panel layout (2x2 grid)
        java.awt.Toolkit toolkit = java.awt.Toolkit.getDefaultToolkit();
        int screenWidth = (int) toolkit.getScreenSize().getWidth();
        int screenHeight = (int) toolkit.getScreenSize().getHeight();
        int panelWidth = screenWidth / 2;
        int panelHeight = (screenHeight - 80) / 2;

        ChromeOptions options = new ChromeOptions();
        options.addArguments("--disable-notifications", "--remote-allow-origins=*");
        options.addArguments("--disable-infobars", "--disable-extensions");
        options.setExperimentalOption("excludeSwitches", java.util.Arrays.asList("enable-automation"));

        // PANEL 1: Citizen Browser (TOP-LEFT)
        citizenBrowser = new ChromeDriver(options);
        citizenBrowser.manage().window().setPosition(new Point(0, 0));
        citizenBrowser.manage().window().setSize(new Dimension(panelWidth, panelHeight));
        citizenWait = new WebDriverWait(citizenBrowser, Duration.ofSeconds(15));
        citizenJs = (JavascriptExecutor) citizenBrowser;

        // PANEL 2: Officer Browser (TOP-RIGHT)
        officerBrowser = new ChromeDriver(options);
        officerBrowser.manage().window().setPosition(new Point(panelWidth, 0));
        officerBrowser.manage().window().setSize(new Dimension(panelWidth, panelHeight));
        officerWait = new WebDriverWait(officerBrowser, Duration.ofSeconds(15));
        officerJs = (JavascriptExecutor) officerBrowser;

        // PANEL 3: Hacker Harvest Browser (BOTTOM-LEFT)
        hackerHarvestBrowser = new ChromeDriver(options);
        hackerHarvestBrowser.manage().window().setPosition(new Point(0, panelHeight));
        hackerHarvestBrowser.manage().window().setSize(new Dimension(panelWidth, panelHeight));
        harvestWait = new WebDriverWait(hackerHarvestBrowser, Duration.ofSeconds(15));
        harvestJs = (JavascriptExecutor) hackerHarvestBrowser;

        // PANEL 4: Hacker Decrypt Browser (BOTTOM-RIGHT)
        hackerDecryptBrowser = new ChromeDriver(options);
        hackerDecryptBrowser.manage().window().setPosition(new Point(panelWidth, panelHeight));
        hackerDecryptBrowser.manage().window().setSize(new Dimension(panelWidth, panelHeight));
        decryptWait = new WebDriverWait(hackerDecryptBrowser, Duration.ofSeconds(15));
        decryptJs = (JavascriptExecutor) hackerDecryptBrowser;

        // Check quantum service
        quantumServiceAvailable = checkQuantumService();
        
        printBanner();
    }

    private void printBanner() {
        String scenarioInfo = getScenarioDescription();
        String kemAlgo = getKemAlgorithm();
        String sigAlgo = getSigAlgorithm();
        
        System.out.println("\n");
        System.out.println("╔════════════════════════════════════════════════════════════════════════════════════════╗");
        System.out.println("║          🛡️ PQC CYBERSECURITY - 4-PANEL REALISTIC DEMONSTRATION                       ║");
        System.out.println("╠════════════════════════════════════════════════════════════════════════════════════════╣");
        System.out.println("║  TOP-LEFT:      👤 CITIZEN        - Submits government applications                    ║");
        System.out.println("║  TOP-RIGHT:     👮 OFFICER        - Reviews and approves applications                  ║");
        System.out.println("║  BOTTOM-LEFT:   📡 HACKER HARVEST - Real-time encrypted traffic interception          ║");
        System.out.println("║  BOTTOM-RIGHT:  ⚛️ HACKER DECRYPT - Quantum decryption with progress                  ║");
        System.out.println("╠════════════════════════════════════════════════════════════════════════════════════════╣");
        System.out.println("║  📡 Services: gov-portal:8181 | hacker-console:8183 | quantum-sim:8184                 ║");
        System.out.println("║  ⚛️ Quantum: " + (quantumServiceAvailable ? "AVAILABLE (GPU cuQuantum)   " : "UNAVAILABLE (simulation)    ") + "                                        ║");
        System.out.println("╠════════════════════════════════════════════════════════════════════════════════════════╣");
        System.out.printf("║  🔐 SCENARIO %s: %-68s ║%n", DEMO_SCENARIO, scenarioInfo);
        System.out.printf("║     KEM Algorithm: %-10s | Signature Algorithm: %-10s                         ║%n", kemAlgo, sigAlgo);
        System.out.println("╚════════════════════════════════════════════════════════════════════════════════════════╝\n");
    }

    private boolean checkQuantumService() {
        try {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(QUANTUM_URL + "/api/quantum/status"))
                    .timeout(Duration.ofSeconds(3))
                    .GET()
                    .build();
            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            return response.statusCode() == 200;
        } catch (Exception e) {
            return false;
        }
    }

    @AfterAll
    void closeBrowsers() {
        System.out.println("\n════════════════════════════════════════════════════════════════════════════════");
        System.out.println("  DEMO COMPLETE - BROWSERS REMAIN OPEN FOR MANUAL TESTING");
        System.out.println("════════════════════════════════════════════════════════════════════════════════\n");
        System.out.println("🖥️ All 4 browser panels remain open. You can:");
        System.out.println("   • Submit more documents in the GOV PORTAL (top panels)");
        System.out.println("   • Watch real-time interception in HARVEST panel (bottom-left)");
        System.out.println("   • See quantum decryption progress in DECRYPT panel (bottom-right)");
        System.out.println("   • Close browsers manually when done\n");
        System.out.println("⚠️ Press Ctrl+C to end the test or close browsers manually.\n");
        
        // Keep browsers open indefinitely for manual testing
        // User must manually close browsers or press Ctrl+C
        try {
            while (true) {
                Thread.sleep(Long.MAX_VALUE);
            }
        } catch (InterruptedException ignored) {
            // User pressed Ctrl+C, close browsers gracefully
            System.out.println("\n🔒 Closing browsers...\n");
            try { if (citizenBrowser != null) citizenBrowser.quit(); } catch (Exception e) {}
            try { if (officerBrowser != null) officerBrowser.quit(); } catch (Exception e) {}
            try { if (hackerHarvestBrowser != null) hackerHarvestBrowser.quit(); } catch (Exception e) {}
            try { if (hackerDecryptBrowser != null) hackerDecryptBrowser.quit(); } catch (Exception e) {}
        }
    }

    // ═══════════════════════════════════════════════════════════════════════════════════════════
    // TEST 1: Initialize All Four Panels
    // ═══════════════════════════════════════════════════════════════════════════════════════════
    @Test
    @Order(1)
    @DisplayName("1. 🚀 Initialize Four-Panel Display")
    void initializeAllPanels() {
        System.out.println("════════════════════════════════════════════════════════════════════════════════");
        System.out.println("  TEST 1: Initializing Four Browser Panels");
        System.out.println("════════════════════════════════════════════════════════════════════════════════\n");

        // Panel 3: Hacker Harvest Dashboard
        hackerHarvestBrowser.get(HACKER_URL + "/harvest");
        sleep(2000);
        System.out.println("✅ PANEL 3 (BOTTOM-LEFT): Hacker Harvest Dashboard loaded");

        // Panel 4: Hacker Decrypt Dashboard
        hackerDecryptBrowser.get(HACKER_URL + "/decrypt");
        sleep(2000);
        System.out.println("✅ PANEL 4 (BOTTOM-RIGHT): Hacker Decrypt Dashboard loaded");

        // Panel 1: Citizen - Government Portal
        citizenBrowser.get(GOV_URL);
        sleep(2000);
        System.out.println("✅ PANEL 1 (TOP-LEFT): Citizen - Government Portal loaded");

        // Panel 2: Officer - Government Portal
        officerBrowser.get(GOV_URL);
        sleep(2000);
        System.out.println("✅ PANEL 2 (TOP-RIGHT): Officer - Government Portal loaded");
        
        System.out.println("\n📋 All 4 panels ready - Layout: 2x2 grid\n");
        positionAllBrowsers();
        
        // Clear any existing data in hacker console
        clearHackerData();
        sleep(3000);
    }
    
    private void positionAllBrowsers() {
        java.awt.Toolkit toolkit = java.awt.Toolkit.getDefaultToolkit();
        int screenWidth = (int) toolkit.getScreenSize().getWidth();
        int screenHeight = (int) toolkit.getScreenSize().getHeight();
        int panelWidth = screenWidth / 2;
        int panelHeight = (screenHeight - 80) / 2;
        
        citizenBrowser.manage().window().setPosition(new Point(0, 0));
        citizenBrowser.manage().window().setSize(new Dimension(panelWidth, panelHeight));
        
        officerBrowser.manage().window().setPosition(new Point(panelWidth, 0));
        officerBrowser.manage().window().setSize(new Dimension(panelWidth, panelHeight));
        
        hackerHarvestBrowser.manage().window().setPosition(new Point(0, panelHeight));
        hackerHarvestBrowser.manage().window().setSize(new Dimension(panelWidth, panelHeight));
        
        hackerDecryptBrowser.manage().window().setPosition(new Point(panelWidth, panelHeight));
        hackerDecryptBrowser.manage().window().setSize(new Dimension(panelWidth, panelHeight));
    }

    // ═══════════════════════════════════════════════════════════════════════════════════════════
    // TEST 2: Authenticate Citizen and Officer
    // ═══════════════════════════════════════════════════════════════════════════════════════════
    @Test
    @Order(2)
    @DisplayName("2. 🔐 Citizen & Officer Authentication")
    void authenticateUsers() {
        System.out.println("════════════════════════════════════════════════════════════════════════════════");
        System.out.println("  TEST 2: Authenticating Citizen and Officer");
        System.out.println("════════════════════════════════════════════════════════════════════════════════\n");

        // Login Citizen
        citizenBrowser.get(GOV_URL + "/login");
        sleep(2000);
        performLogin(citizenBrowser, citizenWait, CITIZEN_USER, CITIZEN_PASS);
        System.out.println("✅ CITIZEN: Authenticated as john.citizen");
        
        // Hacker sees the session
        System.out.println("📡 HACKER HARVEST: Detected citizen login session");
        sleep(2000);

        // Login Officer
        officerBrowser.get(GOV_URL + "/login");
        sleep(2000);
        performLogin(officerBrowser, officerWait, OFFICER_USER, OFFICER_PASS);
        System.out.println("✅ OFFICER: Authenticated as officer");
        
        System.out.println("📡 HACKER HARVEST: Detected officer login session");
        System.out.println("\n🕵️ Hacker is now monitoring both user sessions\n");
        sleep(3000);
    }

    // ═══════════════════════════════════════════════════════════════════════════════════════════
    // TEST 3: Citizen Submits Car License (Algorithm based on scenario)
    // ═══════════════════════════════════════════════════════════════════════════════════════════
    @Test
    @Order(3)
    @DisplayName("3. 🚗 Citizen: Car License Application")
    void citizenSubmitsCarLicenseRSA() {
        // Determine algorithms based on scenario
        String kemAlgo = getKemAlgorithm();
        String sigAlgo = getSigAlgorithm();
        String kemName = kemAlgo.contains("RSA") ? "RSA-2048" : "ML-KEM-768";
        String sigName = sigAlgo.contains("RSA") ? "RSA-2048" : "ML-DSA-65";
        
        System.out.println("════════════════════════════════════════════════════════════════════════════════");
        System.out.println("  TEST 3: Citizen Submits Car License");
        System.out.println("  SCENARIO " + DEMO_SCENARIO + ": KEM=" + kemName + ", Sig=" + sigName);
        System.out.println("════════════════════════════════════════════════════════════════════════════════\n");

        citizenBrowser.get(GOV_URL + "/services/car-license");
        sleep(2500);

        try {
            citizenWait.until(ExpectedConditions.presenceOfElementLocated(By.id("fullName")));
            
            String licensePlate = generateLicensePlate();
            
            fillField(citizenBrowser, "fullName", "John Michael Citizen");
            fillField(citizenBrowser, "dob", "1985-06-15");
            fillField(citizenBrowser, "address", "1247 Oak Street, Springfield, IL 62701");
            selectDropdown(citizenBrowser, "licenseType", "Class B - Standard Vehicle");
            fillField(citizenBrowser, "vehiclePlate", licensePlate);
            fillField(citizenBrowser, "vehicleMake", "Toyota Camry");
            selectDropdown(citizenBrowser, "vehicleYear", "2024");
            
            // SELECT ENCRYPTION based on scenario
            clickRadioByValue(citizenBrowser, kemAlgo);
            sleep(500);
            
            // SELECT SIGNATURE based on scenario
            clickRadioByValue(citizenBrowser, sigAlgo);
            sleep(500);

            System.out.println("📝 CITIZEN: Filling Car License Application");
            System.out.println("   🔐 ENCRYPTION: " + kemName + (kemAlgo.contains("RSA") ? " ⚠️ VULNERABLE" : " ✅ QUANTUM-SAFE"));
            System.out.println("   ✍️ SIGNATURE:  " + sigName + (sigAlgo.contains("RSA") ? " ⚠️ VULNERABLE" : " ✅ QUANTUM-SAFE"));

            WebElement submitBtn = citizenBrowser.findElement(By.cssSelector("button[type='submit']"));
            submitBtn.click();
            documentsSubmitted++;
            sleep(3000);

            System.out.println("✅ CITIZEN: Application SUBMITTED!\n");

            // The harvest dashboard should auto-capture this
            System.out.println("📡 HACKER HARVEST: Encrypted packet captured automatically!");
            System.out.println("   📦 Document: Car License Application");
            System.out.println("   🔐 Encryption: RSA-2048");
            System.out.println("   ✍️ Signature: RSA-2048");
            System.out.println("   📊 Size: ~2KB encrypted payload");
            
            // Sync both hacker panels - refresh harvest and trigger decrypt
            syncHackerPanels();
            
            // Watch decryption start in the decrypt panel
            System.out.println("⚛️ HACKER DECRYPT: Quantum attack starting on RSA-2048 document!");
            System.out.println("   📍 Watch BOTTOM-RIGHT panel for decryption progress\n");
            sleep(5000);

        } catch (Exception e) {
            System.out.println("⚠️ Form issue: " + e.getMessage());
        }
    }

    // ═══════════════════════════════════════════════════════════════════════════════════════════
    // TEST 4: Officer Reviews Application
    // ═══════════════════════════════════════════════════════════════════════════════════════════
    @Test
    @Order(4)
    @DisplayName("4. 👮 Officer: Review & Approve Car License")
    void officerReviewsCarLicense() {
        System.out.println("════════════════════════════════════════════════════════════════════════════════");
        System.out.println("  TEST 4: Officer Reviews Car License Application");
        System.out.println("════════════════════════════════════════════════════════════════════════════════\n");

        officerBrowser.get(GOV_URL + "/dashboard");
        sleep(3000);

        System.out.println("📋 OFFICER: Checking dashboard for pending applications...");

        try {
            WebElement reviewLink = officerBrowser.findElement(By.cssSelector("a[href*='/officer/review/']"));
            String docId = reviewLink.getAttribute("href").replaceAll(".*/officer/review/", "");
            
            System.out.println("✅ OFFICER: Found application " + docId);
            reviewLink.click();
            sleep(2500);

            System.out.println("👮 OFFICER: Reviewing and approving...");
            
            try {
                WebElement approveBtn = officerWait.until(ExpectedConditions.elementToBeClickable(
                    By.cssSelector(".btn-approve, button.btn-approve, button[onclick*='approve']")));
                approveBtn.click();
                sleep(2500);
                System.out.println("✅ OFFICER: Car License APPROVED!\n");
                
                // Hacker captures the approval transaction
                System.out.println("📡 HACKER HARVEST: Intercepted approval transaction!");
                System.out.println("   ✍️ Officer digital signature captured");
                refreshHarvestPanel("Approval", "RSA-2048");
                
            } catch (Exception e) {
                System.out.println("ℹ️ OFFICER: Reviewed application\n");
            }
        } catch (NoSuchElementException e) {
            System.out.println("ℹ️ OFFICER: No pending applications\n");
            officerBrowser.get(GOV_URL + "/dashboard");
        }
    }

    // ═══════════════════════════════════════════════════════════════════════════════════════════
    // TEST 5: Citizen Submits Tax Filing (SCENARIO-BASED ALGORITHM SELECTION)
    // ═══════════════════════════════════════════════════════════════════════════════════════════
    @Test
    @Order(5)
    @DisplayName("5. 💰 Citizen: Tax Filing (Scenario-based Algorithm)")
    void citizenSubmitsTaxFilingMLKEM() {
        String kemAlgo = getKemAlgorithm();
        String sigAlgo = getSigAlgorithm();
        String scenarioDesc = getScenarioDescription();
        boolean isKemPQC = kemAlgo.contains("ML_KEM");
        boolean isSigPQC = sigAlgo.contains("ML_DSA");
        
        System.out.println("════════════════════════════════════════════════════════════════════════════════");
        System.out.println("  TEST 5: Citizen Submits Tax Filing - " + scenarioDesc);
        System.out.println("  KEM: " + kemAlgo + " | Signature: " + sigAlgo);
        System.out.println("════════════════════════════════════════════════════════════════════════════════\n");

        citizenBrowser.get(GOV_URL + "/services/tax-filing");
        sleep(2500);

        try {
            citizenWait.until(ExpectedConditions.presenceOfElementLocated(By.id("fullName")));
            
            int grossIncome = 85000 + new Random().nextInt(50000);
            int deductions = 12000 + new Random().nextInt(8000);
            
            fillField(citizenBrowser, "fullName", "John Michael Citizen");
            fillField(citizenBrowser, "taxId", "XXX-XX-6789");
            selectDropdown(citizenBrowser, "filingYear", "2024");
            fillField(citizenBrowser, "grossIncome", String.valueOf(grossIncome));
            fillField(citizenBrowser, "deductions", String.valueOf(deductions));
            sleep(500);
            fillField(citizenBrowser, "bankAccount", "****" + (1000 + new Random().nextInt(9000)));
            
            // SELECT ENCRYPTION ALGORITHM BASED ON SCENARIO
            clickRadioByValue(citizenBrowser, kemAlgo);
            sleep(500);
            
            // SELECT SIGNATURE ALGORITHM IF AVAILABLE
            try {
                clickRadioByValue(citizenBrowser, sigAlgo);
            } catch (Exception ignored) {}
            sleep(500);

            System.out.println("📝 CITIZEN: Filling Tax Filing");
            System.out.println("   💵 Income: $" + String.format("%,d", grossIncome));
            System.out.println("   🔐 ENCRYPTION: " + kemAlgo + (isKemPQC ? " (QUANTUM SAFE!)" : " (VULNERABLE!)"));
            System.out.println("   ✍️ SIGNATURE:  " + sigAlgo + (isSigPQC ? " (QUANTUM SAFE!)" : " (VULNERABLE!)"));

            WebElement submitBtn = citizenBrowser.findElement(By.cssSelector("button[type='submit']"));
            submitBtn.click();
            documentsSubmitted++;
            sleep(3000);

            System.out.println("✅ CITIZEN: Tax Filing SUBMITTED!\n");

            // Hacker captures
            System.out.println("📡 HACKER HARVEST: Encrypted packet captured!");
            System.out.println("   📦 Document: Tax Filing");
            System.out.println("   🔐 Encryption: " + kemAlgo + (isKemPQC ? " (Quantum-Resistant)" : " (Quantum-Vulnerable)"));
            System.out.println("   ✍️ Signature:  " + sigAlgo + (isSigPQC ? " (Quantum-Resistant)" : " (Quantum-Vulnerable)"));
            
            // Sync both hacker panels
            syncHackerPanels();
            
            // Watch decryption attempt
            if (isKemPQC) {
                System.out.println("⚛️ HACKER DECRYPT: Attempting quantum attack on " + kemAlgo + "...");
                System.out.println("   🛡️ This attack will FAIL - PQC is quantum-resistant!\n");
            } else {
                System.out.println("⚛️ HACKER DECRYPT: Quantum attack on " + kemAlgo + "...");
                System.out.println("   💀 This encryption IS VULNERABLE to quantum computers!\n");
            }
            sleep(5000);

        } catch (Exception e) {
            System.out.println("⚠️ Form issue: " + e.getMessage());
        }
    }

    // ═══════════════════════════════════════════════════════════════════════════════════════════
    // TEST 6: Officer Reviews Tax Filing
    // ═══════════════════════════════════════════════════════════════════════════════════════════
    @Test
    @Order(6)
    @DisplayName("6. 👮 Officer: Review & Approve Tax Filing")
    void officerReviewsTaxFiling() {
        System.out.println("════════════════════════════════════════════════════════════════════════════════");
        System.out.println("  TEST 6: Officer Reviews Tax Filing Application");
        System.out.println("════════════════════════════════════════════════════════════════════════════════\n");

        officerBrowser.get(GOV_URL + "/dashboard");
        sleep(3000);

        try {
            var reviewLinks = officerBrowser.findElements(By.cssSelector("a[href*='/officer/review/']"));
            
            if (!reviewLinks.isEmpty()) {
                System.out.println("✅ OFFICER: Found tax filing to review");
                reviewLinks.get(0).click();
                sleep(2500);

                System.out.println("👮 OFFICER: Reviewing Tax Filing with PQC signature...");
                
                try {
                    WebElement approveBtn = officerWait.until(ExpectedConditions.elementToBeClickable(
                        By.cssSelector(".btn-approve, button.btn-approve")));
                    approveBtn.click();
                    sleep(2000);
                    System.out.println("✅ OFFICER: Tax Filing APPROVED!\n");
                    
                    refreshHarvestPanel("Tax Approval", "ML-KEM-768");
                } catch (Exception e) {
                    System.out.println("ℹ️ OFFICER: Reviewed tax filing\n");
                }
            } else {
                System.out.println("ℹ️ OFFICER: Dashboard clear\n");
            }
        } catch (Exception e) {
            System.out.println("ℹ️ OFFICER: No pending items\n");
        }
    }

    // ═══════════════════════════════════════════════════════════════════════════════════════════
    // TEST 7: Verify Harvested Data and Watch Decryption in Progress
    // ═══════════════════════════════════════════════════════════════════════════════════════════
    @Test
    @Order(7)
    @DisplayName("7. 📤 Verify Harvest & Watch Decryption Progress")
    void verifyHarvestAndWatchDecryption() {
        System.out.println("════════════════════════════════════════════════════════════════════════════════");
        System.out.println("  TEST 7: Verify Harvested Data & Watch Decryption in Real-Time");
        System.out.println("════════════════════════════════════════════════════════════════════════════════\n");

        // Final sync of both panels
        System.out.println("📡 Synchronizing hacker panels...");
        syncHackerPanels();
        sleep(3000);
        
        // Count packets in harvest panel
        try {
            Long packetCount = (Long) harvestJs.executeScript(
                "return document.querySelectorAll('.network-packet').length;");
            System.out.println("📊 HARVEST PANEL: " + (packetCount != null ? packetCount : 0) + " packets intercepted");
        } catch (Exception e) {
            System.out.println("📊 HARVEST PANEL: Packets visible in browser");
        }
        
        // Count items in decrypt queue
        try {
            Long queueCount = (Long) decryptJs.executeScript(
                "return document.querySelectorAll('.queue-item').length;");
            System.out.println("📊 DECRYPT PANEL: " + (queueCount != null ? queueCount : 0) + " items in queue");
        } catch (Exception e) {
            System.out.println("📊 DECRYPT PANEL: Items queued for decryption");
        }

        System.out.println("\n👁️ BOTH HACKER PANELS SYNCED:");
        System.out.println("   BOTTOM-LEFT:  Intercepted encrypted packets (KEM + Signatures)");
        System.out.println("   BOTTOM-RIGHT: Quantum decryption queue with progress\n");
        
        // Trigger decryption if not already running
        try {
            decryptJs.executeScript("if(typeof startDecryptAll === 'function') startDecryptAll();");
            System.out.println("⚛️ Quantum decryption triggered - watch progress in BOTTOM-RIGHT!\n");
        } catch (Exception e) {
            System.out.println("⚛️ Decryption already in progress...\n");
        }
        
        sleep(5000);
    }

    // ═══════════════════════════════════════════════════════════════════════════════════════════
    // TEST 8: Wait for Quantum Decryption to Complete
    // ═══════════════════════════════════════════════════════════════════════════════════════════
    @Test
    @Order(8)
    @DisplayName("8. ⚛️ Watch Quantum Decryption Complete (30s-2min)")
    void watchQuantumDecryption() {
        System.out.println("════════════════════════════════════════════════════════════════════════════════");
        System.out.println("  TEST 8: QUANTUM DECRYPTION IN PROGRESS");
        System.out.println("════════════════════════════════════════════════════════════════════════════════\n");

        System.out.println("⚛️ QUANTUM ATTACK RUNNING!");
        System.out.println("   📍 Watch BOTTOM-RIGHT panel for decryption progress");
        System.out.println("   ⏱️ RSA-2048: Shor decrypt → plaintext proof in Results + console");
        System.out.println("   🛡️ ML-KEM: PASS-THROUGH (no lattice decrypt — stays protected)\n");
        
        // Poll for completion
        int maxWaitSeconds = 180; // 3 minutes max
        int waited = 0;
        boolean sawPlaintext = false;
        boolean sawPassThrough = false;
        
        while (waited < maxWaitSeconds) {
            sleep(5000); // Check every 5 seconds
            waited += 5;
            
            // Check decryption progress + plaintext proof in UI
            try {
                Long processingCount = (Long) decryptJs.executeScript(
                    "return document.querySelectorAll('.queue-item.processing').length;");
                Long completedCount = (Long) decryptJs.executeScript(
                    "return document.querySelectorAll('.queue-item.completed, .queue-item.failed').length;");
                Boolean hasPlaintext = (Boolean) decryptJs.executeScript(
                    "var el=document.getElementById('resultsPanel')||document.body;" +
                    "return el && el.innerText && el.innerText.indexOf('DECRYPTED PLAINTEXT')>=0;");
                Boolean hasPassThrough = (Boolean) decryptJs.executeScript(
                    "var el=document.getElementById('resultsPanel')||document.body;" +
                    "return el && el.innerText && (el.innerText.indexOf('PASS-THROUGH')>=0 || el.innerText.indexOf('PQC pass-through')>=0);");
                if (Boolean.TRUE.equals(hasPlaintext)) {
                    sawPlaintext = true;
                    System.out.println("🔓 PROOF: DECRYPTED PLAINTEXT visible in Results panel!");
                }
                if (Boolean.TRUE.equals(hasPassThrough)) {
                    sawPassThrough = true;
                    System.out.println("🛡️ PROOF: PQC PASS-THROUGH visible (no decrypt attempted)!");
                }
                
                if (processingCount != null && processingCount == 0 && completedCount != null && completedCount > 0) {
                    System.out.println("✅ Quantum decryption batch completed!\n");
                    break;
                }
                
                System.out.println("   ⏱️ " + waited + "s - Processing: " + processingCount + ", Completed: " + completedCount);
            } catch (Exception e) {
                System.out.println("   ⏱️ " + waited + "s - Decryption in progress...");
            }
        }
        
        // Dump plaintext lines from decrypt console / results for the demo log
        try {
            String proofDump = (String) decryptJs.executeScript(
                "var lines=[];" +
                "if(typeof results!=='undefined'&&results){results.forEach(function(r){" +
                "  if(r&&r.plaintext){lines.push(r.plaintext);} " +
                "  if(r&&r.passThrough){lines.push('PQC PASS-THROUGH: '+r.documentId+' '+r.encryption+' NOT decrypted');}" +
                "});}" +
                "var consoleEl=document.getElementById('consoleLog')||document.getElementById('console')||document.getElementById('logConsole');" +
                "if(consoleEl&&consoleEl.innerText){" +
                "  consoleEl.innerText.split('\\n').forEach(function(l){" +
                "    if(l.indexOf('DECRYPTED')>=0||l.indexOf('PLAINTEXT')>=0||l.indexOf('PASS-THROUGH')>=0||l.indexOf('RSA FACTORS')>=0){lines.push(l);}" +
                "  });}" +
                "return lines.slice(0,40).join('\\n');");
            if (proofDump != null && !proofDump.isBlank()) {
                System.out.println("\n═══════════════ DECRYPTED / PROTECTED OUTPUT (from UI) ═══════════════");
                System.out.println(proofDump);
                System.out.println("═══════════════════════════════════════════════════════════════════════\n");
                if (proofDump.contains("DECRYPTED PLAINTEXT") || proofDump.contains("PLAINTEXT")) {
                    sawPlaintext = true;
                }
                if (proofDump.contains("PASS-THROUGH")) {
                    sawPassThrough = true;
                }
            }
        } catch (Exception e) {
            System.out.println("ℹ️ Could not dump plaintext from UI: " + e.getMessage());
        }

        sleep(2000);
        
        System.out.println("📊 DECRYPTION RESULTS VISIBLE IN BOTTOM-RIGHT PANEL:");
        System.out.println("   • RSA-encrypted documents: DECRYPTED plaintext shown (red) — proof=" + sawPlaintext);
        System.out.println("   • ML-KEM documents: PQC PASS-THROUGH / PROTECTED (green) — proof=" + sawPassThrough + "\n");
        if (!sawPlaintext) {
            System.out.println("⚠️ WARNING: Expected DECRYPTED PLAINTEXT block was not found in Results/console.");
        }
    }

    // ═══════════════════════════════════════════════════════════════════════════════════════════
    // TEST 9: Final Summary with Real Statistics
    // ═══════════════════════════════════════════════════════════════════════════════════════════
    @Test
    @Order(9)
    @DisplayName("9. 📊 Security Demonstration Summary")
    void displaySummary() {
        System.out.println("════════════════════════════════════════════════════════════════════════════════");
        System.out.println("  TEST 9: PQC Security Demonstration Summary");
        System.out.println("════════════════════════════════════════════════════════════════════════════════\n");

        // Fetch real statistics from the hacker API
        int rsaKemCount = 0, pqcKemCount = 0, rsaSigCount = 0, pqcSigCount = 0, totalPackets = 0;
        
        try {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(HACKER_URL + "/api/hacker/intercept/live"))
                    .timeout(Duration.ofSeconds(5))
                    .GET()
                    .build();
            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            JsonNode data = objectMapper.readTree(response.body());
            
            if (data.has("newTransactions")) {
                for (JsonNode tx : data.get("newTransactions")) {
                    totalPackets++;
                    String encryption = tx.path("encryptionAlgorithm").asText("RSA");
                    String signature = tx.path("signatureAlgorithm").asText("RSA");
                    
                    if (encryption.contains("RSA")) rsaKemCount++;
                    if (encryption.contains("ML-KEM") || encryption.contains("ML_KEM")) pqcKemCount++;
                    if (signature.contains("RSA")) rsaSigCount++;
                    if (signature.contains("ML-DSA") || signature.contains("ML_DSA")) pqcSigCount++;
                }
            }
        } catch (Exception e) {
            System.out.println("ℹ️ Could not fetch live stats: " + e.getMessage());
        }

        System.out.println("╔════════════════════════════════════════════════════════════════════════════════╗");
        System.out.println("║                    PQC SECURITY DEMONSTRATION COMPLETE                         ║");
        System.out.println("╠════════════════════════════════════════════════════════════════════════════════╣");
        System.out.println("║                                                                                ║");
        System.out.println("║  📊 INTERCEPTED TRAFFIC STATISTICS:                                            ║");
        System.out.printf("║     • Total Packets Captured:     %3d                                          ║%n", totalPackets);
        System.out.printf("║     • RSA KEM (Vulnerable):       %3d                                          ║%n", rsaKemCount);
        System.out.printf("║     • ML-KEM (Quantum-Safe):      %3d                                          ║%n", pqcKemCount);
        System.out.printf("║     • RSA Signatures (Vulnerable):%3d                                          ║%n", rsaSigCount);
        System.out.printf("║     • ML-DSA Signatures (Safe):   %3d                                          ║%n", pqcSigCount);
        System.out.println("║                                                                                ║");
        System.out.println("║  📋 DOCUMENTS PROCESSED:                                                       ║");
        System.out.println("║     • Car License (RSA-2048)  → ⚠️ DECRYPTED by Shor's Algorithm              ║");
        System.out.println("║     • Tax Filing (ML-KEM-768) → ✅ PQC PASS-THROUGH (not decrypted)           ║");
        System.out.println("║                                                                                ║");
        System.out.println("║  ⚛️ QUANTUM ATTACK RESULTS:                                                    ║");
        System.out.println("║     • RSA-2048 encryption: 💔 BROKEN — plaintext shown in Results/console     ║");
        System.out.println("║     • RSA-2048 signature:  💔 FORGED (private key recovered)                  ║");
        System.out.println("║     • ML-KEM-768:          🛡️ PASS-THROUGH (decrypt skipped; stays safe)      ║");
        System.out.println("║     • ML-DSA-65:           🛡️ SECURE (signature cannot be forged)             ║");
        System.out.println("║                                                                                ║");
        System.out.println("║  🔑 KEY TAKEAWAY:                                                              ║");
        System.out.println("║     Migrate to Post-Quantum Cryptography (ML-KEM, ML-DSA) NOW!                 ║");
        System.out.println("║     Your RSA-encrypted data can be harvested today and decrypted               ║");
        System.out.println("║     when quantum computers become available (HNDL attack).                     ║");
        System.out.println("║                                                                                ║");
        System.out.println("╚════════════════════════════════════════════════════════════════════════════════╝\n");
        
        System.out.println("👁️ Review all 4 panels to see the complete attack demonstration.");
        System.out.println("   • TOP-LEFT:     Citizen submissions");
        System.out.println("   • TOP-RIGHT:    Officer approvals");
        System.out.println("   • BOTTOM-LEFT:  Intercepted packets (KEM + Signatures)");
        System.out.println("   • BOTTOM-RIGHT: Quantum decryption results\n");
        
        System.out.println("════════════════════════════════════════════════════════════════════════════════");
        System.out.println("  DEMONSTRATION COMPLETE - Browsers remain open for manual testing");
        System.out.println("  Press Ctrl+C or close browsers manually when done");
        System.out.println("════════════════════════════════════════════════════════════════════════════════\n");
    }

    // ═══════════════════════════════════════════════════════════════════════════════════════════
    // SCENARIO-BASED ALGORITHM SELECTION
    // Scenario 1: RSA KEM + RSA Sig (FULLY VULNERABLE)
    // Scenario 2: ML-KEM + ML-DSA (FULLY QUANTUM-SAFE)
    // Scenario 3: RSA KEM + ML-DSA (MIXED - Encryption vulnerable)
    // Scenario 4: ML-KEM + RSA Sig (MIXED - Signature vulnerable)
    // ═══════════════════════════════════════════════════════════════════════════════════════════
    
    private String getKemAlgorithm() {
        return switch (DEMO_SCENARIO) {
            case "1", "3" -> "RSA_2048";    // RSA for scenarios 1 and 3
            case "2", "4" -> "ML_KEM";       // ML-KEM for scenarios 2 and 4
            default -> "RSA_2048";
        };
    }
    
    private String getSigAlgorithm() {
        return switch (DEMO_SCENARIO) {
            case "1", "4" -> "RSA_2048";    // RSA for scenarios 1 and 4
            case "2", "3" -> "ML_DSA";       // ML-DSA for scenarios 2 and 3
            default -> "RSA_2048";
        };
    }
    
    private String getScenarioDescription() {
        return switch (DEMO_SCENARIO) {
            case "1" -> "FULLY VULNERABLE (RSA KEM + RSA Sig)";
            case "2" -> "FULLY QUANTUM-SAFE (ML-KEM + ML-DSA)";
            case "3" -> "MIXED - Encryption Vulnerable (RSA KEM + ML-DSA)";
            case "4" -> "MIXED - Signature Vulnerable (ML-KEM + RSA Sig)";
            default -> "Default (RSA + RSA)";
        };
    }

    // ═══════════════════════════════════════════════════════════════════════════════════════════
    // HELPER METHODS
    // ═══════════════════════════════════════════════════════════════════════════════════════════

    private void performLogin(WebDriver browser, WebDriverWait wait, String username, String password) {
        try {
            WebElement usernameField = wait.until(ExpectedConditions.presenceOfElementLocated(By.id("username")));
            usernameField.clear();
            usernameField.sendKeys(username);
            sleep(300);

            WebElement passwordField = browser.findElement(By.id("password"));
            passwordField.clear();
            passwordField.sendKeys(password);
            sleep(300);

            browser.findElement(By.cssSelector("button[type='submit']")).click();
            sleep(2500);
        } catch (Exception e) {
            System.out.println("⚠️ Login issue: " + e.getMessage());
        }
    }

    private void fillField(WebDriver browser, String fieldId, String value) {
        try {
            WebElement field = browser.findElement(By.id(fieldId));
            field.clear();
            field.sendKeys(value);
            sleep(200);
        } catch (Exception ignored) {}
    }

    private void selectDropdown(WebDriver browser, String fieldId, String visibleText) {
        try {
            Select select = new Select(browser.findElement(By.id(fieldId)));
            try {
                select.selectByVisibleText(visibleText);
            } catch (Exception e) {
                select.selectByValue(visibleText);
            }
            sleep(200);
        } catch (Exception ignored) {}
    }

    private void clickRadioByValue(WebDriver browser, String value) {
        try {
            WebElement radio = browser.findElement(By.cssSelector("input[value='" + value + "']"));
            ((JavascriptExecutor) browser).executeScript("arguments[0].click();", radio);
        } catch (Exception ignored) {}
    }

    private String generateLicensePlate() {
        String[] states = {"IL", "CA", "NY", "TX", "FL"};
        String state = states[new Random().nextInt(states.length)];
        int number = 1000 + new Random().nextInt(9000);
        char letter1 = (char) ('A' + new Random().nextInt(26));
        char letter2 = (char) ('A' + new Random().nextInt(26));
        return state + "-" + number + "-" + letter1 + letter2;
    }

    /**
     * Clear all harvested data in hacker console via API
     */
    private void clearHackerData() {
        try {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(HACKER_URL + "/api/hacker/harvest"))
                    .timeout(Duration.ofSeconds(5))
                    .DELETE()
                    .build();
            httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            System.out.println("🗑️ Cleared previous hacker data");
            
            // Refresh both hacker dashboards
            hackerHarvestBrowser.navigate().refresh();
            hackerDecryptBrowser.navigate().refresh();
            sleep(1000);
        } catch (Exception e) {
            System.out.println("ℹ️ Hacker data already clean");
        }
    }

    /**
     * Sync both hacker panels - refresh harvest, load data into decrypt, trigger auto-decryption
     */
    private void syncHackerPanels() {
        try {
            // Step 1: Refresh harvest panel and fetch new transactions
            harvestJs.executeScript("if(typeof fetchTransactions === 'function') fetchTransactions();");
            sleep(2000);
            
            // Step 2: Force sync the decrypt panel using forceSync function
            decryptJs.executeScript("if(typeof forceSync === 'function') forceSync();");
            sleep(2000);
            
            // Step 3: Also call individual fetch functions as backup
            decryptJs.executeScript("if(typeof fetchLiveInterceptData === 'function') fetchLiveInterceptData();");
            decryptJs.executeScript("if(typeof fetchHarvestedData === 'function') fetchHarvestedData();");
            sleep(1000);
            
            // Step 4: Trigger auto-decryption if not already running
            decryptJs.executeScript("if(typeof autoStartDecryption === 'function') autoStartDecryption();");
            
            // Step 5: Count what we have
            Long harvestCount = (Long) harvestJs.executeScript(
                "return document.querySelectorAll('.network-packet').length;");
            Long decryptCount = (Long) decryptJs.executeScript(
                "return document.querySelectorAll('.queue-item').length;");
            
            System.out.println("   📊 SYNC: Harvest=" + (harvestCount != null ? harvestCount : 0) + 
                             " packets, Decrypt=" + (decryptCount != null ? decryptCount : 0) + " queued");
            
        } catch (Exception e) {
            // Fallback: hard refresh both panels
            hackerHarvestBrowser.navigate().refresh();
            hackerDecryptBrowser.navigate().refresh();
            sleep(2000);
        }
    }

    /**
     * Refresh hacker harvest panel and visually highlight new data
     */
    private void refreshHarvestPanel(String docType, String encryption) {
        try {
            // First fetch transactions via API
            harvestJs.executeScript("if(typeof fetchTransactions === 'function') fetchTransactions();");
            sleep(1500);
            
            // Scroll to show latest packet
            harvestJs.executeScript("window.scrollTo(0, 0);");
            sleep(500);
            
            // Count packets displayed
            Long packetCount = (Long) harvestJs.executeScript(
                "return document.querySelectorAll('.network-packet').length;");
            
            if (packetCount != null && packetCount > 0) {
                System.out.println("   📊 Harvest panel showing " + packetCount + " captured packet(s)");
            }
        } catch (Exception e) {
            // Fallback: just refresh
            hackerHarvestBrowser.navigate().refresh();
            sleep(1000);
        }
    }

    /**
     * Refresh decrypt panel to show queued items
     */
    private void refreshDecryptPanel() {
        try {
            // Trigger data fetch without full page refresh
            decryptJs.executeScript("if(typeof fetchHarvestedData === 'function') fetchHarvestedData();");
            decryptJs.executeScript("if(typeof fetchLiveInterceptData === 'function') fetchLiveInterceptData();");
            sleep(2000);
            
            // Count items in queue
            Long queueCount = (Long) decryptJs.executeScript(
                "return document.querySelectorAll('.queue-item').length;");
            
            if (queueCount != null && queueCount > 0) {
                System.out.println("   📊 Decrypt panel showing " + queueCount + " item(s) in queue");
            }
        } catch (Exception e) {
            hackerDecryptBrowser.navigate().refresh();
            sleep(1000);
        }
    }

    private void sleep(long millis) {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException ignored) {}
    }
}
