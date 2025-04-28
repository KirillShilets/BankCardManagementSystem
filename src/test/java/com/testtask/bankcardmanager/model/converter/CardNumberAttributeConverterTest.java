package com.testtask.bankcardmanager.model.converter;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.env.Environment;

import javax.crypto.AEADBadTagException;
import java.util.Base64;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CardNumberAttributeConverterTest {

    @Mock
    private Environment environment;

    private CardNumberAttributeConverter converter;

    private final String TEST_AES_KEY_BASE64 = "MTIzNDU2Nzg5MDEyMzQ1Ng=="; // 16 bytes key, valid

    @BeforeEach
    void setUp() {
        lenient().when(environment.getProperty("encryption.aes.key")).thenReturn(TEST_AES_KEY_BASE64);
        // Use lenient() if the mock is sometimes not used in a test (like constructor tests)
        converter = new CardNumberAttributeConverter(environment);
    }

    @Test
    void convertToDatabaseColumn_Success() {
        String originalCardNumber = "1111222233334444";
        String encryptedData = converter.convertToDatabaseColumn(originalCardNumber);
        assertNotNull(encryptedData);
        assertNotEquals(originalCardNumber, encryptedData);
        assertTrue(encryptedData.length() > originalCardNumber.length());
        try {
            Base64.getDecoder().decode(encryptedData);
        } catch (IllegalArgumentException e) {
            fail("Encrypted data is not valid Base64");
        }
    }

    @Test
    void convertToEntityAttribute_Success() {
        String originalCardNumber = "1111222233334444";
        String encryptedData = converter.convertToDatabaseColumn(originalCardNumber);
        assertNotNull(encryptedData);
        String decryptedCardNumber = converter.convertToEntityAttribute(encryptedData);
        assertEquals(originalCardNumber, decryptedCardNumber);
    }

    @Test
    void convertToDatabaseColumn_NullInput() {
        assertNull(converter.convertToDatabaseColumn(null));
    }

    @Test
    void convertToEntityAttribute_NullInput() {
        assertNull(converter.convertToEntityAttribute(null));
    }

    @Test
    void convertToEntityAttribute_InvalidBase64Data() {
        String invalidData = "Invalid Base64%";
        IllegalStateException exception = assertThrows(IllegalStateException.class, () -> converter.convertToEntityAttribute(invalidData));
        assertTrue(exception.getMessage().contains("Invalid format or Base64"));
    }

    @Test
    void convertToEntityAttribute_TooShortData() {
        byte[] onlyIv = new byte[11]; // Less than IV_LENGTH_BYTE
        java.util.concurrent.ThreadLocalRandom.current().nextBytes(onlyIv);
        String tooShortData = Base64.getEncoder().encodeToString(onlyIv);

        IllegalStateException exception = assertThrows(IllegalStateException.class, () -> converter.convertToEntityAttribute(tooShortData));
        assertTrue(exception.getCause() instanceof IllegalArgumentException);
        assertTrue(exception.getMessage().contains("Invalid format or Base64"));
    }


    @Test
    void convertToEntityAttribute_TamperedData() {
        String originalCardNumber = "1111222233334444";
        String encryptedData = converter.convertToDatabaseColumn(originalCardNumber);
        byte[] encryptedBytes = Base64.getDecoder().decode(encryptedData);

        encryptedBytes[encryptedBytes.length - 1] ^= 0x01; // Tamper with the last byte
        String tamperedData = Base64.getEncoder().encodeToString(encryptedBytes);

        IllegalStateException exception = assertThrows(IllegalStateException.class, () -> converter.convertToEntityAttribute(tamperedData));
        assertTrue(exception.getCause() instanceof AEADBadTagException || exception.getCause() instanceof javax.crypto.BadPaddingException || exception.getCause() instanceof java.security.GeneralSecurityException);
        assertTrue(exception.getMessage().contains("Failed to decrypt data"));
    }


    @Test
    void constructor_KeyNotFound_ThrowsException() {
        // Need a separate environment mock for this test as setUp() sets a valid key
        Environment mockEnv = mock(Environment.class);
        when(mockEnv.getProperty("encryption.aes.key")).thenReturn(null);
        when(mockEnv.getProperty("ENCRYPTION_AES_KEY")).thenReturn(null);

        IllegalStateException exception = assertThrows(IllegalStateException.class, () -> new CardNumberAttributeConverter(mockEnv));
        assertTrue(exception.getMessage().contains("Encryption key not found"));
    }

    @Test
    void constructor_InvalidKeyLength_ThrowsException() {
        Environment mockEnv = mock(Environment.class);
        String invalidKeyBase64 = "dG9vU2hvcnRLZXk="; // 11 bytes key
        when(mockEnv.getProperty("encryption.aes.key")).thenReturn(invalidKeyBase64);

        IllegalStateException exception = assertThrows(IllegalStateException.class, () -> new CardNumberAttributeConverter(mockEnv));
        assertTrue(exception.getCause() instanceof IllegalArgumentException);
        assertTrue(exception.getMessage().contains("Failed to initialize SecretKey"));
        assertTrue(exception.getCause().getMessage().contains("Invalid AES key length"));
    }

    @Test
    void constructor_InvalidBase64Key_ThrowsException() {
        Environment mockEnv = mock(Environment.class);
        String invalidBase64Key = "this is not base64!!";
        when(mockEnv.getProperty("encryption.aes.key")).thenReturn(invalidBase64Key);

        IllegalStateException exception = assertThrows(IllegalStateException.class, () -> new CardNumberAttributeConverter(mockEnv));
        assertTrue(exception.getCause() instanceof IllegalArgumentException);
        assertTrue(exception.getMessage().contains("Failed to initialize SecretKey"));
    }

    @Test
    void convertToDatabaseColumnAndBack_MultipleTimes() {
        String originalCardNumber1 = "1111111111111111";
        String originalCardNumber2 = "2222222222222222";

        String encrypted1 = converter.convertToDatabaseColumn(originalCardNumber1);
        String encrypted2 = converter.convertToDatabaseColumn(originalCardNumber2);

        assertNotNull(encrypted1);
        assertNotNull(encrypted2);
        assertNotEquals(encrypted1, encrypted2);

        assertEquals(originalCardNumber1, converter.convertToEntityAttribute(encrypted1));
        assertEquals(originalCardNumber2, converter.convertToEntityAttribute(encrypted2));
    }
}