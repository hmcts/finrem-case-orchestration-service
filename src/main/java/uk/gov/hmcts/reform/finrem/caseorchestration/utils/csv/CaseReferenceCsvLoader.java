package uk.gov.hmcts.reform.finrem.caseorchestration.utils.csv;

import com.fasterxml.jackson.dataformat.csv.CsvMapper;
import com.fasterxml.jackson.dataformat.csv.CsvSchema;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;

import java.io.File;
import java.nio.file.Files;
import java.security.Key;
import java.util.Collections;
import java.util.List;

@Slf4j
@NoArgsConstructor
@Component
public class CaseReferenceCsvLoader {

    private static final String ALGORITHM = "AES";
    private static final String TRANSFORMATION = "AES";

    @SuppressWarnings({"java:S3740", "java:S1488"})
    public List<CaseReference> loadCaseReferenceList(String fileName) {
        try {
            CsvMapper csvMapper = new CsvMapper();
            CsvSchema csvSchema = csvMapper.typedSchemaFor(CaseReference.class).withHeader();
            List list = new CsvMapper().readerFor(CaseReference.class)
                .with(csvSchema)
                .readValues(getClass().getClassLoader().getResource(fileName))
                .readAll();

            return list;
        } catch (Exception e) {
            log.error("Error occurred while loading object list from file " + fileName, e);
            return Collections.emptyList();
        }
    }

    public List<CaseReference> loadCaseReferenceList(String fileName, String secret) throws Exception {
        File encryptedFile = new File(fileName);
        File decryptedFile = new File("decrypted-" + fileName);

        decryptFile(secret, encryptedFile, decryptedFile);

        return loadCaseReferenceList(decryptedFile.getPath());
    }

    private void decryptFile(String key, File inputFile, File outputFile) throws Exception {
        Key secretKey = new SecretKeySpec(key.getBytes(), ALGORITHM);
        Cipher cipher = Cipher.getInstance(TRANSFORMATION);
        cipher.init(Cipher.DECRYPT_MODE, secretKey);

        byte[] inputBytes = Files.readAllBytes(inputFile.toPath());
        byte[] outputBytes = cipher.doFinal(inputBytes);

        Files.write(outputFile.toPath(), outputBytes);
    }
}
