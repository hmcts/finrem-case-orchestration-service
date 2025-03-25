package uk.gov.hmcts.reform.finrem.caseorchestration.utils.csv;

import org.junit.jupiter.api.Test;

import javax.crypto.SecretKey;
import java.util.List;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

public class CaseReferenceCsvLoaderTest {

    CaseReferenceCsvLoader caseReferenceCsvLoader = new CaseReferenceCsvLoader();

    @Test
    void shouldLoadCaseRefsCsvFile() {
        List<CaseReference> caseReferences = caseReferenceCsvLoader.loadCaseReferenceList("caserefs-test.csv");

        assertThat(caseReferences.isEmpty(), equalTo(Boolean.FALSE));
        assertThat(caseReferences.size(), equalTo(4));
    }

    @Test
    void shouldReturnEmptyCollectionWhenFileNotFound() {
        List<CaseReference> caseReferences = caseReferenceCsvLoader.loadCaseReferenceList("caserefs-test-file-does-not-exist.csv");

        assertThat(caseReferences.isEmpty(), equalTo(Boolean.TRUE));
    }

    @Test
    void testEncryptDecrypt() throws Exception {
        String originalString = "This is a test string";
        String secret = "mySecretKey";

        SecretKey key = CaseReferenceCsvLoader.getKeyFromString(secret);
        String encryptedString = CaseReferenceCsvLoader.encrypt(originalString, key);
        String decryptedString = CaseReferenceCsvLoader.decrypt(encryptedString, key);

        assertEquals(originalString, decryptedString);
    }

    @Test
    void shouldLoadEncryptedCaseRefsCsvFile() {
        String secret = "DUMMY_SECRET";
        String fileName = "caserefs-test-encrypted.csv";

        List<CaseReference> caseReferences = caseReferenceCsvLoader.loadCaseReferenceList(fileName, secret);

        assertThat(caseReferences.isEmpty(), equalTo(Boolean.FALSE));
        assertThat(caseReferences.size() > 0, equalTo(Boolean.TRUE));
    }

    @Test
    void testGetKeyFromString() throws Exception {
        String secret = "mySecretKey";
        SecretKey key = CaseReferenceCsvLoader.getKeyFromString(secret);

        assertNotNull(key);
        assertArrayEquals(CaseReferenceCsvLoader.getKeyFromString(secret).getEncoded(), key.getEncoded());
    }
}