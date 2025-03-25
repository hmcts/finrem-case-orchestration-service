package uk.gov.hmcts.reform.finrem.caseorchestration;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import uk.gov.hmcts.reform.finrem.caseorchestration.utils.csv.CaseReferenceCsvLoader;

import javax.crypto.SecretKey;
import java.nio.file.Files;
import java.nio.file.Paths;

import static uk.gov.hmcts.reform.finrem.caseorchestration.utils.csv.CaseReferenceCsvLoader.getKeyFromString;

@Slf4j
public class CaseMigrationEncryptionUtil implements CommandLineRunner {

    public static void main(String[] args) throws Exception {
        if (args.length < 4) {
            log.info("Usage: java amendGeneralEmail <encrypt|decrypt> <inputFilePath> <outputFilePath> <secretKey>");
            return;
        }

        String operation = args[0];
        String inputFilePath = args[1];
        String outputFilePath = args[2];
        String secretKeyString = args[3];

        SecretKey key = getKeyFromString(secretKeyString);

        if ("encrypt".equalsIgnoreCase(operation)) {
            encryptFile(inputFilePath, outputFilePath, key);
            log.info("File encrypted successfully.");
        } else if ("decrypt".equalsIgnoreCase(operation)) {
            decryptFile(inputFilePath, outputFilePath, key);
            log.info("File decrypted successfully.");
        } else {
            log.error("Invalid operation. Use 'encrypt' or 'decrypt'.");
        }
    }

    @Override
    public void run(String... args) {
        SpringApplication.run(CaseMigrationEncryptionUtil.class, args);
    }

    public static void encryptFile(String inputFilePath, String outputFilePath, SecretKey key) throws Exception {
        String content = new String(Files.readAllBytes(Paths.get(inputFilePath)));
        String encryptedContent = CaseReferenceCsvLoader.encrypt(content, key);
        Files.write(Paths.get(outputFilePath), encryptedContent.getBytes());
    }

    public static void decryptFile(String inputFilePath, String outputFilePath, SecretKey key) throws Exception {
        String encryptedContent = new String(Files.readAllBytes(Paths.get(inputFilePath)));
        String decryptedContent = CaseReferenceCsvLoader.decrypt(encryptedContent, key);
        Files.write(Paths.get(outputFilePath), decryptedContent.getBytes());
    }
}
