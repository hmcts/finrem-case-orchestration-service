package uk.gov.hmcts.reform.finrem.caseorchestration;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.nio.file.Files;
import java.nio.file.Paths;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CaseMigrationEncryptionUtilTest {

    private static final String SECRET_KEY = "mySecretKey";
    private static final String INPUT_FILE_PATH = "src/test/resources/input.txt";
    private static final String EMPTY_FILE_PATH = "src/test/resources/empty.txt";
    private static final String ENCRYPTED_FILE_PATH = "src/test/resources/encrypted.txt";
    private static final String DECRYPTED_FILE_PATH = "src/test/resources/decrypted.txt";

    @BeforeEach
    void setUp() throws Exception {
        Files.write(Paths.get(INPUT_FILE_PATH), "This is a test string".getBytes());
        Files.write(Paths.get(EMPTY_FILE_PATH), new byte[0]);
    }

    @AfterEach
    void tearDown() throws Exception {
        Files.deleteIfExists(Paths.get(INPUT_FILE_PATH));
        Files.deleteIfExists(Paths.get(EMPTY_FILE_PATH));
        Files.deleteIfExists(Paths.get(ENCRYPTED_FILE_PATH));
        Files.deleteIfExists(Paths.get(DECRYPTED_FILE_PATH));
    }

    @Test
    void testProcessFileEncrypt() throws Exception {
        CaseMigrationEncryptionUtil.processFile(SECRET_KEY, "encrypt", INPUT_FILE_PATH, ENCRYPTED_FILE_PATH);

        String encryptedContent = new String(Files.readAllBytes(Paths.get(ENCRYPTED_FILE_PATH)));
        assertNotNull(encryptedContent);
        assertNotEquals("This is a test string", encryptedContent);
    }

    @Test
    void testProcessFileDecrypt() throws Exception {
        CaseMigrationEncryptionUtil.processFile(SECRET_KEY, "encrypt", INPUT_FILE_PATH, ENCRYPTED_FILE_PATH);
        CaseMigrationEncryptionUtil.processFile(SECRET_KEY, "decrypt", ENCRYPTED_FILE_PATH, DECRYPTED_FILE_PATH);

        String decryptedContent = new String(Files.readAllBytes(Paths.get(DECRYPTED_FILE_PATH)));
        assertEquals("This is a test string", decryptedContent);
    }

    @Test
    void testProcessFileInvalidOperation() {
        Exception exception = assertThrows(Exception.class, () -> {
            CaseMigrationEncryptionUtil.processFile(SECRET_KEY, "invalid", INPUT_FILE_PATH, ENCRYPTED_FILE_PATH);
        });

        String expectedMessage = "Invalid operation. Use 'encrypt' or 'decrypt'.";
        String actualMessage = exception.getMessage();

        assertTrue(actualMessage.contains(expectedMessage));
    }
}
