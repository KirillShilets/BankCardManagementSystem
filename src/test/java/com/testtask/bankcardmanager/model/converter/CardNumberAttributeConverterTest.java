package com.testtask.bankcardmanager.model.converter;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.env.Environment;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CardNumberAttributeConverterTest {

    @Mock
    private Environment environment;

    private CardNumberAttributeConverter converter;

    private final String TEST_AES_KEY_BASE64 = "MTIzNDU2Nzg5MDEyMzQ1Ng==";

    @BeforeEach
    void setUp() {
        when(environment.getProperty("encryption.aes.key")).thenReturn(TEST_AES_KEY_BASE64);
        converter = new CardNumberAttributeConverter(environment);
    }

    @Test
    @DisplayName("convertToDatabaseColumn - Успешное шифрование")
    void convertToDatabaseColumn_Success() {
        String originalCardNumber = "1111222233334444";
        String encryptedData = converter.convertToDatabaseColumn(originalCardNumber);
        assertNotNull(encryptedData);
        assertNotEquals(originalCardNumber, encryptedData);
        assertTrue(encryptedData.length() > originalCardNumber.length());
        try {
            java.util.Base64.getDecoder().decode(encryptedData);
        } catch (IllegalArgumentException e) {
            fail("Encrypted data is not valid Base64");
        }
    }

    @Test
    @DisplayName("convertToEntityAttribute - Успешное дешифрование")
    void convertToEntityAttribute_Success() {
        String originalCardNumber = "1111222233334444";
        String encryptedData = converter.convertToDatabaseColumn(originalCardNumber);
        assertNotNull(encryptedData);
        String decryptedCardNumber = converter.convertToEntityAttribute(encryptedData);
        assertEquals(originalCardNumber, decryptedCardNumber);
    }

    @Test
    @DisplayName("convertToDatabaseColumn - Null входные данные")
    void convertToDatabaseColumn_NullInput() {
        assertNull(converter.convertToDatabaseColumn(null));
    }

    @Test
    @DisplayName("convertToEntityAttribute - Null входные данные")
    void convertToEntityAttribute_NullInput() {
        assertNull(converter.convertToEntityAttribute(null));
    }

    @Test
    @DisplayName("convertToEntityAttribute - Невалидные данные (не Base64)")
    void convertToEntityAttribute_InvalidBase64Data() {
        String invalidData = "Invalid Base64%";
        assertThrows(IllegalStateException.class, () -> converter.convertToEntityAttribute(invalidData));
    }

    @Test
    @DisplayName("convertToEntityAttribute - Невалидные данные (слишком короткие)")
    void convertToEntityAttribute_TooShortData() {
        byte[] onlyIv = new byte[12];
        java.util.concurrent.ThreadLocalRandom.current().nextBytes(onlyIv);
        String tooShortData = java.util.Base64.getEncoder().encodeToString(onlyIv);

        assertThrows(IllegalStateException.class, () -> converter.convertToEntityAttribute(tooShortData));
    }


    @Test
    @DisplayName("convertToEntityAttribute - Невалидные данные (случайная строка)")
    void convertToEntityAttribute_RandomStringData() {
        byte[] randomBytes = new byte[32];
        java.util.concurrent.ThreadLocalRandom.current().nextBytes(randomBytes);
        String randomBase64 = java.util.Base64.getEncoder().encodeToString(randomBytes);
        assertThrows(IllegalStateException.class, () -> converter.convertToEntityAttribute(randomBase64));
    }


    @Test
    @DisplayName("Конструктор - Ключ не найден")
    void constructor_KeyNotFound_ThrowsException() {
        reset(environment);
        when(environment.getProperty("encryption.aes.key")).thenReturn(null);
        when(environment.getProperty("ENCRYPTION_AES_KEY")).thenReturn(null);

        IllegalStateException exception = assertThrows(IllegalStateException.class, () -> new CardNumberAttributeConverter(environment));
        assertTrue(exception.getMessage().contains("Encryption key not found"));
    }

    @Test
    @DisplayName("Конструктор - Невалидная длина ключа")
    void constructor_InvalidKeyLength_ThrowsException() {
        String invalidKeyBase64 = "dG9vU2hvcnRLZXk=";
        reset(environment);
        when(environment.getProperty("encryption.aes.key")).thenReturn(invalidKeyBase64);

        IllegalStateException exception = assertThrows(IllegalStateException.class, () -> new CardNumberAttributeConverter(environment));
        assertTrue(exception.getCause() instanceof IllegalArgumentException);
        assertTrue(exception.getMessage().contains("Failed to initialize SecretKey"));
    }
}