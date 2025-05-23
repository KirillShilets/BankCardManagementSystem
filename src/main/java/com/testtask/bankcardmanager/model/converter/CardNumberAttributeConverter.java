package com.testtask.bankcardmanager.model.converter;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Base64;
import java.util.Objects;

@Converter
@Component
public class CardNumberAttributeConverter implements AttributeConverter<String, String> {

    private static final String ALGORITHM = "AES/GCM/NoPadding";
    private static final int IV_LENGTH_BYTE = 12;
    private static final int TAG_LENGTH_BIT = 128;
    private static final String PROPERTY_NAME_DOT = "encryption.aes.key";
    private static final String PROPERTY_NAME_ENV = "ENCRYPTION_AES_KEY";

    private final SecretKey secretKey;
    private final SecureRandom secureRandom = new SecureRandom();

    @Autowired
    public CardNumberAttributeConverter(Environment environment) {
        Objects.requireNonNull(environment, "Spring Environment cannot be null");

        String base64Key = environment.getProperty(PROPERTY_NAME_DOT);
        if (base64Key == null || base64Key.isEmpty()) {
            base64Key = environment.getProperty(PROPERTY_NAME_ENV);
        }

        if (base64Key == null || base64Key.isEmpty()) {
            String errorMessage = String.format(
                    "Encryption key not found using properties '%s' or '%s'. " +
                            "Ensure it's configured in a property source (like .env via initializer, application.properties, or environment variables).",
                    PROPERTY_NAME_DOT, PROPERTY_NAME_ENV);
            throw new IllegalStateException(errorMessage);
        }

        try {
            byte[] keyBytes = Base64.getDecoder().decode(base64Key);
            if (keyBytes.length != 16 && keyBytes.length != 24 && keyBytes.length != 32) {
                throw new IllegalArgumentException("Invalid AES key length: " + keyBytes.length * 8 + " bits. Must be 128, 192, or 256 bits.");
            }
            this.secretKey = new SecretKeySpec(keyBytes, "AES");
            Cipher.getInstance(ALGORITHM);
        } catch (IllegalArgumentException | NoSuchAlgorithmException e) {
            throw new IllegalStateException("Failed to initialize SecretKey", e);
        } catch (Exception e) {
            throw new IllegalStateException("Failed to initialize Cipher for algorithm " + ALGORITHM, e);
        }
    }

    @Override
    public String convertToDatabaseColumn(String attribute) {
        if (attribute == null) {
            return null;
        }

        try {
            byte[] iv = new byte[IV_LENGTH_BYTE];
            secureRandom.nextBytes(iv);

            Cipher cipher = Cipher.getInstance(ALGORITHM);
            GCMParameterSpec parameterSpec = new GCMParameterSpec(TAG_LENGTH_BIT, iv);
            cipher.init(Cipher.ENCRYPT_MODE, this.secretKey, parameterSpec);

            byte[] plainTextBytes = attribute.getBytes(StandardCharsets.UTF_8);
            byte[] cipherTextBytes = cipher.doFinal(plainTextBytes);

            ByteBuffer byteBuffer = ByteBuffer.allocate(iv.length + cipherTextBytes.length);
            byteBuffer.put(iv);
            byteBuffer.put(cipherTextBytes);
            byte[] ivAndCipherText = byteBuffer.array();

            return Base64.getEncoder().encodeToString(ivAndCipherText);

        } catch (GeneralSecurityException e) {
            throw new IllegalStateException("Failed to encrypt data", e);
        }
    }

    @Override
    public String convertToEntityAttribute(String dbData) {
        if (dbData == null) {
            return null;
        }

        try {
            byte[] ivAndCipherText = Base64.getDecoder().decode(dbData);
            if (ivAndCipherText.length <= IV_LENGTH_BYTE) {
                throw new IllegalArgumentException("Invalid encrypted data length received.");
            }

            ByteBuffer byteBuffer = ByteBuffer.wrap(ivAndCipherText);
            byte[] iv = new byte[IV_LENGTH_BYTE];
            byteBuffer.get(iv);
            byte[] cipherTextBytes = new byte[byteBuffer.remaining()];
            byteBuffer.get(cipherTextBytes);

            if (cipherTextBytes.length == 0) {
                throw new IllegalArgumentException("Encrypted data contains only IV, no ciphertext.");
            }


            Cipher cipher = Cipher.getInstance(ALGORITHM);
            GCMParameterSpec parameterSpec = new GCMParameterSpec(TAG_LENGTH_BIT, iv);
            cipher.init(Cipher.DECRYPT_MODE, this.secretKey, parameterSpec);

            byte[] plainTextBytes = cipher.doFinal(cipherTextBytes);

            return new String(plainTextBytes, StandardCharsets.UTF_8);

        } catch (IllegalArgumentException e) {
            throw new IllegalStateException("Failed to decrypt data: Invalid format or Base64", e);
        } catch (RuntimeException e) {
            throw new IllegalStateException("Failed to decrypt data due to an unexpected runtime error", e);
        } catch (GeneralSecurityException e) {
            throw new IllegalStateException("Failed to decrypt data", e);
        }
    }
}