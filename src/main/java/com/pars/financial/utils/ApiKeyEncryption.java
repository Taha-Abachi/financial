package com.pars.financial.utils;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.Base64;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class ApiKeyEncryption {

    @Value("${api.key.encryption.secret}")
    private String secretKey;

    @Value("${api.key.encryption.algorithm}")
    private String algorithm;

    private static final int GCM_IV_LENGTH = 12;
    private static final int GCM_TAG_LENGTH = 128;

    public void setSecretKey(String secretKey) {
        this.secretKey = secretKey;
    }

    public void setAlgorithm(String algorithm) {
        this.algorithm = algorithm;
    }

    public String encrypt(String apiKey) {
        try {
            // Generate deterministic IV from API key
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(apiKey.getBytes(StandardCharsets.UTF_8));
            byte[] iv = new byte[GCM_IV_LENGTH];
            System.arraycopy(hash, 0, iv, 0, GCM_IV_LENGTH);

            SecretKey key = new SecretKeySpec(secretKey.getBytes(StandardCharsets.UTF_8), "AES");
            Cipher cipher = Cipher.getInstance(algorithm);
            GCMParameterSpec parameterSpec = new GCMParameterSpec(GCM_TAG_LENGTH, iv);
            cipher.init(Cipher.ENCRYPT_MODE, key, parameterSpec);

            byte[] encryptedData = cipher.doFinal(apiKey.getBytes(StandardCharsets.UTF_8));
            byte[] combined = new byte[iv.length + encryptedData.length];
            System.arraycopy(iv, 0, combined, 0, iv.length);
            System.arraycopy(encryptedData, 0, combined, iv.length, encryptedData.length);

            return Base64.getEncoder().encodeToString(combined);
        } catch (Exception e) {
            throw new RuntimeException("Error encrypting API key", e);
        }
    }

    public String decrypt(String encryptedApiKey) {
        try {
            byte[] decoded = Base64.getDecoder().decode(encryptedApiKey);
            byte[] iv = new byte[GCM_IV_LENGTH];
            System.arraycopy(decoded, 0, iv, 0, iv.length);

            SecretKey key = new SecretKeySpec(secretKey.getBytes(StandardCharsets.UTF_8), "AES");
            Cipher cipher = Cipher.getInstance(algorithm);
            GCMParameterSpec parameterSpec = new GCMParameterSpec(GCM_TAG_LENGTH, iv);
            cipher.init(Cipher.DECRYPT_MODE, key, parameterSpec);

            byte[] decryptedData = cipher.doFinal(decoded, iv.length, decoded.length - iv.length);
            return new String(decryptedData, StandardCharsets.UTF_8);
        } catch (Exception e) {
            throw new RuntimeException("Error decrypting API key", e);
        }
    }
} 