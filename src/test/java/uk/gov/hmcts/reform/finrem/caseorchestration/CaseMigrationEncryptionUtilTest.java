package uk.gov.hmcts.reform.finrem.caseorchestration;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import javax.crypto.SecretKey;
import java.nio.file.Files;
import java.nio.file.Paths;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static uk.gov.hmcts.reform.finrem.caseorchestration.utils.csv.CaseReferenceCsvLoader.getKeyFromString;

class CaseMigrationEncryptionUtilTest {

    private static final String SECRET_KEY = "mySecretKey";
    private static final String INPUT_FILE_PATH = "src/test/resources/input.txt";
    private static final String ENCRYPTED_FILE_PATH = "src/test/resources/encrypted.txt";
    private static final String DECRYPTED_FILE_PATH = "src/test/resources/decrypted.txt";

    @BeforeEach
    void setUp() throws Exception {
        Files.write(Paths.get(INPUT_FILE_PATH), "This is a test string".getBytes());
    }

    @AfterEach
    void tearDown() throws Exception {
        Files.deleteIfExists(Paths.get(INPUT_FILE_PATH));
        Files.deleteIfExists(Paths.get(ENCRYPTED_FILE_PATH));
        Files.deleteIfExists(Paths.get(DECRYPTED_FILE_PATH));
    }

    @Test
    void encryptFile() throws Exception {
        SecretKey key = getKeyFromString(SECRET_KEY);
        CaseMigrationEncryptionUtil.encryptFile(INPUT_FILE_PATH, ENCRYPTED_FILE_PATH, key);

        String encryptedContent = new String(Files.readAllBytes(Paths.get(ENCRYPTED_FILE_PATH)));
        assertNotNull(encryptedContent);
        assertNotEquals("This is a test string", encryptedContent);
    }

    @Test
    void decryptFile() throws Exception {
        SecretKey key = getKeyFromString(SECRET_KEY);
        CaseMigrationEncryptionUtil.encryptFile(INPUT_FILE_PATH, ENCRYPTED_FILE_PATH, key);
        CaseMigrationEncryptionUtil.decryptFile(ENCRYPTED_FILE_PATH, DECRYPTED_FILE_PATH, key);

        String decryptedContent = new String(Files.readAllBytes(Paths.get(DECRYPTED_FILE_PATH)));
        assertEquals("This is a test string", decryptedContent);
    }
}