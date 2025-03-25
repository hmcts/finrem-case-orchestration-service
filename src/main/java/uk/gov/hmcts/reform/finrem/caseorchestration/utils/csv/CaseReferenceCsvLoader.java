package uk.gov.hmcts.reform.finrem.caseorchestration.utils.csv;

import com.fasterxml.jackson.dataformat.csv.CsvMapper;
import com.fasterxml.jackson.dataformat.csv.CsvSchema;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.util.Arrays;
import java.util.Base64;
import java.util.Collections;
import java.util.List;

@Slf4j
@NoArgsConstructor
@Component
public class CaseReferenceCsvLoader {

    private static final String ALGORITHM = "AES";
    private static final String TRANSFORMATION = "AES/CBC/PKCS5Padding";
    private static final int IV_SIZE = 16;

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

    public List<CaseReference> loadCaseReferenceList(String fileName, String secret) {
        try {
            String encryptedContent = new String(Files.readAllBytes(Paths.get(getClass().getClassLoader().getResource(fileName).toURI())));
            SecretKey key = getKeyFromString(secret);
            String decryptedContent = decrypt(encryptedContent, key);

            CsvMapper csvMapper = new CsvMapper();
            CsvSchema csvSchema = csvMapper.typedSchemaFor(CaseReference.class).withHeader();
            List list = new CsvMapper().readerFor(CaseReference.class)
                    .with(csvSchema)
                    .readValues(decryptedContent)
                    .readAll();
            return list;
        } catch (Exception e) {
            log.error("Error occurred while loading object list from file " + fileName, e);
            return Collections.emptyList();
        }
    }

    public static SecretKey getKeyFromString(String key) throws Exception {
        SecretKeySpec secretKey = generateKey(key);
        return secretKey;
    }

    public static String encrypt(String data, SecretKey key) throws Exception {
        Cipher cipher = Cipher.getInstance(TRANSFORMATION);
        IvParameterSpec iv = generateIv();
        cipher.init(Cipher.ENCRYPT_MODE, key, iv);
        byte[] encryptedBytes = cipher.doFinal(data.getBytes());
        return Base64.getEncoder().encodeToString(iv.getIV()) + ":" + Base64.getEncoder().encodeToString(encryptedBytes);
    }

    public static String decrypt(String encryptedData, SecretKey key) throws Exception {
        String[] parts = encryptedData.split(":");
        IvParameterSpec iv = new IvParameterSpec(Base64.getDecoder().decode(parts[0]));
        Cipher cipher = Cipher.getInstance(TRANSFORMATION);
        cipher.init(Cipher.DECRYPT_MODE, key, iv);
        byte[] decodedBytes = Base64.getDecoder().decode(parts[1]);
        byte[] decryptedBytes = cipher.doFinal(decodedBytes);
        return new String(decryptedBytes);
    }

    private static SecretKeySpec generateKey(String key) throws Exception {
        MessageDigest sha = MessageDigest.getInstance("SHA-256");
        byte[] keyBytes = key.getBytes("UTF-8");
        keyBytes = sha.digest(keyBytes);
        keyBytes = Arrays.copyOf(keyBytes, 16); // Use first 16 bytes for AES-128
        return new SecretKeySpec(keyBytes, ALGORITHM);
    }

    private static IvParameterSpec generateIv() {
        byte[] iv = new byte[IV_SIZE];
        new SecureRandom().nextBytes(iv);
        return new IvParameterSpec(iv);
    }
}
