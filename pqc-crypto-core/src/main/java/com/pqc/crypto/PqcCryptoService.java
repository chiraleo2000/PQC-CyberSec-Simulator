package com.pqc.crypto;

import com.pqc.model.*;
import lombok.extern.slf4j.Slf4j;
import org.bouncycastle.jcajce.SecretKeyWithEncapsulation;
import org.bouncycastle.jcajce.spec.KEMExtractSpec;
import org.bouncycastle.jcajce.spec.KEMGenerateSpec;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.bouncycastle.pqc.jcajce.provider.BouncyCastlePQCProvider;
import org.bouncycastle.pqc.jcajce.spec.DilithiumParameterSpec;
import org.bouncycastle.pqc.jcajce.spec.KyberParameterSpec;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.security.*;
import java.util.Base64;

/**
 * Core Cryptographic Service providing PQC and Classical algorithms.
 * 
 * POST-QUANTUM ALGORITHMS (Quantum-Resistant):
 * - ML-DSA (CRYSTALS-Dilithium) - Digital Signatures (FIPS 204)
 * - ML-KEM (CRYSTALS-Kyber) - Key Encapsulation (FIPS 203)
 * 
 * CLASSICAL ALGORITHMS (Quantum-Vulnerable, for fallback/demo):
 * - RSA-2048 - Digital Signatures
 * - AES-128/256-GCM - Symmetric Encryption
 * 
 * EXTENSIBILITY:
 * - Add new algorithms by:
 * 1. Adding to CryptoAlgorithm enum
 * 2. Implementing generate/sign/verify/encrypt/decrypt methods
 * 3. Updating the hybrid methods to include the new algorithm
 * 
 * THREAD SAFETY:
 * - This class is stateless and thread-safe
 * - Each operation uses fresh Cipher/Signature instances
 */
@Slf4j
public class PqcCryptoService {

    private static final String BC_PROVIDER = "BC";
    private static final String BCPQC_PROVIDER = "BCPQC";
    private static final int GCM_IV_LENGTH = 12;
    private static final int GCM_TAG_LENGTH = 128;

    private static volatile boolean providersRegistered = false;

    /**
     * Initialize the crypto service and register Bouncy Castle providers.
     * This is called automatically on first use.
     */
    public static synchronized void initialize() {
        if (!providersRegistered) {
            if (Security.getProvider("BC") == null) {
                Security.addProvider(new BouncyCastleProvider());
            }
            if (Security.getProvider("BCPQC") == null) {
                Security.addProvider(new BouncyCastlePQCProvider());
            }
            providersRegistered = true;
            log.info("Bouncy Castle providers registered for PQC cryptography");
        }
    }

    /**
     * Create a new PqcCryptoService instance.
     * Automatically initializes Bouncy Castle providers.
     */
    public PqcCryptoService() {
        initialize();
    }

    // ==================== ML-DSA (Dilithium) - Digital Signatures
    // ====================

    /**
     * Generate ML-DSA (Dilithium) key pair.
     * Uses Dilithium3 (ML-DSA-65) for 128-bit post-quantum security.
     * 
     * @return KeyPairResult containing public and private keys
     */
    public KeyPairResult generateMLDSAKeyPair() throws GeneralSecurityException {
        log.debug("Generating ML-DSA (Dilithium3) key pair...");
        KeyPairGenerator kpg = KeyPairGenerator.getInstance("Dilithium", BCPQC_PROVIDER);
        kpg.initialize(DilithiumParameterSpec.dilithium3, new SecureRandom());
        KeyPair keyPair = kpg.generateKeyPair();

        byte[] publicKey = keyPair.getPublic().getEncoded();
        byte[] privateKey = keyPair.getPrivate().getEncoded();

        log.info("ML-DSA key pair generated - Public: {} bytes, Private: {} bytes",
                publicKey.length, privateKey.length);

        return KeyPairResult.of(publicKey, privateKey, CryptoAlgorithm.ML_DSA);
    }

    /**
     * Sign data using ML-DSA (Dilithium).
     * 
     * @param data       Data to sign
     * @param privateKey Encoded private key
     * @return SignatureResult containing the signature
     */
    public SignatureResult signWithMLDSA(byte[] data, PrivateKey privateKey) throws GeneralSecurityException {
        long start = System.nanoTime();

        Signature signer = Signature.getInstance("Dilithium", BCPQC_PROVIDER);
        signer.initSign(privateKey, new SecureRandom());
        signer.update(data);
        byte[] signature = signer.sign();

        long elapsed = System.nanoTime() - start;
        log.debug("ML-DSA signature created - Size: {} bytes, Time: {} µs",
                signature.length, elapsed / 1000);

        return SignatureResult.of(signature, CryptoAlgorithm.ML_DSA, elapsed);
    }

    /**
     * Verify ML-DSA (Dilithium) signature.
     * 
     * @param data      Original data
     * @param signature Signature to verify
     * @param publicKey Public key for verification
     * @return true if signature is valid
     */
    public boolean verifyMLDSASignature(byte[] data, byte[] signature, PublicKey publicKey)
            throws GeneralSecurityException {
        Signature verifier = Signature.getInstance("Dilithium", BCPQC_PROVIDER);
        verifier.initVerify(publicKey);
        verifier.update(data);
        boolean valid = verifier.verify(signature);
        log.debug("ML-DSA signature verification: {}", valid ? "VALID" : "INVALID");
        return valid;
    }

    // ==================== ML-KEM (Kyber) - Key Encapsulation ====================

    /**
     * Generate ML-KEM (Kyber) key pair.
     * Uses Kyber768 (ML-KEM-768) for 192-bit post-quantum security.
     * 
     * @return KeyPairResult containing public and private keys
     */
    public KeyPairResult generateMLKEMKeyPair() throws GeneralSecurityException {
        log.debug("Generating ML-KEM (Kyber768) key pair...");
        KeyPairGenerator kpg = KeyPairGenerator.getInstance("Kyber", BCPQC_PROVIDER);
        kpg.initialize(KyberParameterSpec.kyber768, new SecureRandom());
        KeyPair keyPair = kpg.generateKeyPair();

        byte[] publicKey = keyPair.getPublic().getEncoded();
        byte[] privateKey = keyPair.getPrivate().getEncoded();

        log.info("ML-KEM key pair generated - Public: {} bytes, Private: {} bytes",
                publicKey.length, privateKey.length);

        return KeyPairResult.of(publicKey, privateKey, CryptoAlgorithm.ML_KEM);
    }

    /**
     * Encapsulate a shared secret using ML-KEM (Kyber).
     * Used by the sender to create a shared secret for symmetric encryption.
     * 
     * @param publicKey Recipient's public key
     * @return EncapsulationResult with encapsulated key and shared secret
     */
    public EncapsulationResult encapsulateMLKEM(PublicKey publicKey) throws GeneralSecurityException {
        log.debug("Encapsulating shared secret with ML-KEM (Kyber)...");

        KeyGenerator keyGen = KeyGenerator.getInstance("Kyber", BCPQC_PROVIDER);
        keyGen.init(new KEMGenerateSpec(publicKey, "AES"), new SecureRandom());
        SecretKeyWithEncapsulation secretKey = (SecretKeyWithEncapsulation) keyGen.generateKey();

        byte[] encapsulation = secretKey.getEncapsulation();
        byte[] sharedSecret = secretKey.getEncoded();

        log.info("ML-KEM encapsulation complete - Encapsulated: {} bytes, Secret: {} bytes",
                encapsulation.length, sharedSecret.length);

        return EncapsulationResult.of(encapsulation, sharedSecret, CryptoAlgorithm.ML_KEM);
    }

    /**
     * Decapsulate a shared secret using ML-KEM (Kyber).
     * Used by the recipient to recover the shared secret.
     * 
     * @param encapsulation Encapsulated key from sender
     * @param privateKey    Recipient's private key
     * @return Shared secret bytes
     */
    public byte[] decapsulateMLKEM(byte[] encapsulation, PrivateKey privateKey)
            throws GeneralSecurityException {
        log.debug("Decapsulating shared secret with ML-KEM (Kyber)...");

        KeyGenerator keyGen = KeyGenerator.getInstance("Kyber", BCPQC_PROVIDER);
        keyGen.init(new KEMExtractSpec(privateKey, encapsulation, "AES"), new SecureRandom());
        SecretKey sharedSecret = keyGen.generateKey();

        log.info("ML-KEM decapsulation complete - Secret: {} bytes", sharedSecret.getEncoded().length);
        return sharedSecret.getEncoded();
    }

    // ==================== RSA-2048 - Classical Signatures ====================

    /**
     * Generate RSA-2048 key pair (quantum-vulnerable).
     * 
     * ⚠️ WARNING: RSA is vulnerable to Shor's algorithm on quantum computers!
     * Use ML-DSA for quantum-resistant signatures.
     */
    public KeyPairResult generateRSAKeyPair() throws GeneralSecurityException {
        log.warn("Generating RSA-2048 key pair - QUANTUM VULNERABLE!");
        KeyPairGenerator kpg = KeyPairGenerator.getInstance("RSA", BC_PROVIDER);
        kpg.initialize(2048, new SecureRandom());
        KeyPair keyPair = kpg.generateKeyPair();

        byte[] publicKey = keyPair.getPublic().getEncoded();
        byte[] privateKey = keyPair.getPrivate().getEncoded();

        return KeyPairResult.of(publicKey, privateKey, CryptoAlgorithm.RSA_2048);
    }

    /**
     * Sign data using RSA-2048 with SHA-256.
     * 
     * ⚠️ WARNING: RSA signatures are quantum-vulnerable!
     */
    public SignatureResult signWithRSA(byte[] data, PrivateKey privateKey) throws GeneralSecurityException {
        long start = System.nanoTime();

        Signature signer = Signature.getInstance("SHA256withRSA", BC_PROVIDER);
        signer.initSign(privateKey, new SecureRandom());
        signer.update(data);
        byte[] signature = signer.sign();

        long elapsed = System.nanoTime() - start;
        log.warn("RSA signature created (QUANTUM VULNERABLE) - Size: {} bytes", signature.length);

        return SignatureResult.of(signature, CryptoAlgorithm.RSA_2048, elapsed);
    }

    /**
     * Verify RSA-2048 signature.
     */
    public boolean verifyRSASignature(byte[] data, byte[] signature, PublicKey publicKey)
            throws GeneralSecurityException {
        Signature verifier = Signature.getInstance("SHA256withRSA", BC_PROVIDER);
        verifier.initVerify(publicKey);
        verifier.update(data);
        return verifier.verify(signature);
    }

    // ==================== AES-GCM - Symmetric Encryption ====================

    /**
     * Generate AES key.
     * 
     * @param keySize Key size in bits (128 or 256)
     * @return Secret key
     */
    public SecretKey generateAESKey(int keySize) throws GeneralSecurityException {
        KeyGenerator keyGen = KeyGenerator.getInstance("AES", BC_PROVIDER);
        keyGen.init(keySize, new SecureRandom());
        return keyGen.generateKey();
    }

    /**
     * Encrypt data using AES-GCM.
     * 
     * @param plaintext Data to encrypt
     * @param key       AES key (128 or 256 bits)
     * @return EncryptionResult with ciphertext and IV
     */
    public EncryptionResult encryptAES(byte[] plaintext, SecretKey key) throws GeneralSecurityException {
        byte[] iv = new byte[GCM_IV_LENGTH];
        new SecureRandom().nextBytes(iv);

        Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding", BC_PROVIDER);
        GCMParameterSpec gcmSpec = new GCMParameterSpec(GCM_TAG_LENGTH, iv);
        cipher.init(Cipher.ENCRYPT_MODE, key, gcmSpec);

        byte[] ciphertext = cipher.doFinal(plaintext);

        CryptoAlgorithm algo = key.getEncoded().length == 16 ? CryptoAlgorithm.AES_128 : CryptoAlgorithm.AES_256;

        return EncryptionResult.of(ciphertext, iv, algo);
    }

    /**
     * Decrypt data using AES-GCM.
     * 
     * @param ciphertext Encrypted data
     * @param iv         Initialization vector
     * @param key        AES key
     * @return Decrypted plaintext
     */
    public byte[] decryptAES(byte[] ciphertext, byte[] iv, SecretKey key) throws GeneralSecurityException {
        Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding", BC_PROVIDER);
        GCMParameterSpec gcmSpec = new GCMParameterSpec(GCM_TAG_LENGTH, iv);
        cipher.init(Cipher.DECRYPT_MODE, key, gcmSpec);
        return cipher.doFinal(ciphertext);
    }

    /**
     * Encrypt using AES with a shared secret (from KEM).
     */
    public EncryptionResult encryptWithSharedSecret(byte[] plaintext, byte[] sharedSecret, int keySize)
            throws GeneralSecurityException {
        SecretKey key = new SecretKeySpec(sharedSecret, 0, keySize / 8, "AES");
        return encryptAES(plaintext, key);
    }

    /**
     * Decrypt using AES with a shared secret (from KEM).
     */
    public byte[] decryptWithSharedSecret(byte[] ciphertext, byte[] iv, byte[] sharedSecret, int keySize)
            throws GeneralSecurityException {
        SecretKey key = new SecretKeySpec(sharedSecret, 0, keySize / 8, "AES");
        return decryptAES(ciphertext, iv, key);
    }

    // ==================== SHA-384 Hashing ====================

    /**
     * Generate SHA-384 hash.
     * 
     * @param data Data to hash
     * @return Hash bytes
     */
    public byte[] hashSHA384(byte[] data) throws GeneralSecurityException {
        MessageDigest digest = MessageDigest.getInstance("SHA-384", BC_PROVIDER);
        return digest.digest(data);
    }

    /**
     * Generate SHA-384 hash as Base64 URL-safe string.
     * Useful for generating user/document IDs.
     */
    public String hashSHA384AsString(String input) throws GeneralSecurityException {
        byte[] hash = hashSHA384(input.getBytes());
        return Base64.getUrlEncoder().withoutPadding().encodeToString(hash);
    }

    // ==================== Key Conversion Utilities ====================

    /**
     * Convert encoded key bytes to KeyPair.
     * Useful for loading keys from storage.
     */
    public KeyPair loadMLDSAKeyPair(byte[] publicKeyBytes, byte[] privateKeyBytes)
            throws GeneralSecurityException {
        KeyFactory keyFactory = KeyFactory.getInstance("Dilithium", BCPQC_PROVIDER);
        PublicKey publicKey = keyFactory.generatePublic(
                new java.security.spec.X509EncodedKeySpec(publicKeyBytes));
        PrivateKey privateKey = keyFactory.generatePrivate(
                new java.security.spec.PKCS8EncodedKeySpec(privateKeyBytes));
        return new KeyPair(publicKey, privateKey);
    }

    /**
     * Convert encoded key bytes to ML-KEM KeyPair.
     */
    public KeyPair loadMLKEMKeyPair(byte[] publicKeyBytes, byte[] privateKeyBytes)
            throws GeneralSecurityException {
        KeyFactory keyFactory = KeyFactory.getInstance("Kyber", BCPQC_PROVIDER);
        PublicKey publicKey = keyFactory.generatePublic(
                new java.security.spec.X509EncodedKeySpec(publicKeyBytes));
        PrivateKey privateKey = keyFactory.generatePrivate(
                new java.security.spec.PKCS8EncodedKeySpec(privateKeyBytes));
        return new KeyPair(publicKey, privateKey);
    }

    /**
     * Convert encoded key bytes to RSA KeyPair.
     */
    public KeyPair loadRSAKeyPair(byte[] publicKeyBytes, byte[] privateKeyBytes)
            throws GeneralSecurityException {
        KeyFactory keyFactory = KeyFactory.getInstance("RSA", BC_PROVIDER);
        PublicKey publicKey = keyFactory.generatePublic(
                new java.security.spec.X509EncodedKeySpec(publicKeyBytes));
        PrivateKey privateKey = keyFactory.generatePrivate(
                new java.security.spec.PKCS8EncodedKeySpec(privateKeyBytes));
        return new KeyPair(publicKey, privateKey);
    }
}
