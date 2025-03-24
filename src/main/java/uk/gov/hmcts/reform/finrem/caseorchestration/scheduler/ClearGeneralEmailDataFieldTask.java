package uk.gov.hmcts.reform.finrem.caseorchestration.scheduler;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.finrem.caseorchestration.mapper.FinremCaseDetailsMapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseData;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.CcdService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.SystemUserService;
import uk.gov.hmcts.reform.finrem.caseorchestration.utils.csv.CaseReference;
import uk.gov.hmcts.reform.finrem.caseorchestration.utils.csv.CaseReferenceCsvLoader;

import javax.crypto.SecretKey;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;

import static uk.gov.hmcts.reform.finrem.caseorchestration.utils.csv.CaseReferenceCsvLoader.getKeyFromString;

/**
 * Scheduled task to find cases where GeneralEmailDataField is not empty and clear the field.
 * To enable the task to execute set environment variables:
 * <ul>
 *     <li>CRON_NULL_CASEROLEIDS_WHERE_EMPTY_ENABLED=true</li>
 *     <li>TASK_NAME=ClearGeneralEmailDataFieldTask</li>
 *     <li>CRON_NULL_CASEROLEIDS_WHERE_EMPTY_CASE_TYPE_ID=FinancialRemedyContested | FinancialRemedyMVP2</li>
 *     <li>CRON_NULL_CASEROLEIDS_WHERE_EMPTY_BATCH_SIZE=number of cases to search for</li>
 * </ul>
 */
@Component
@Slf4j
public class ClearGeneralEmailDataFieldTask extends CsvFileProcessingTask {

    @Value("${cron.clearGeneralEmailDataFieldTask.secret:DUMMY_SECRET}")
    private String secret;

    private static final String TASK_NAME = "AmendGeneralEmailCron";
    private static final String SUMMARY = "DFR-3639";
    @Value("${cron.clearGeneralEmailDataFieldTask.enabled:true}")
    private boolean taskEnabled;
    @Value("${cron.clearGeneralEmailDataFieldTask.caseTypeId:FinancialRemedyContested}")
    private String caseTypeId;
    @Value("${cron.clearGeneralEmailDataFieldTask.batchSize:500}")
    private int batchSize;

    protected ClearGeneralEmailDataFieldTask(CaseReferenceCsvLoader csvLoader, CcdService ccdService, SystemUserService systemUserService,
                                             FinremCaseDetailsMapper finremCaseDetailsMapper) {
        super(csvLoader, ccdService, systemUserService, finremCaseDetailsMapper);
    }

    @Override
    protected List<CaseReference> getCaseReferences() {
        log.info("Getting case references for GeneralEmailDataFieldTask migration");
        String caseListFileName = getCaseListFileName();

        CaseReferenceCsvLoader csvLoader = new CaseReferenceCsvLoader();
        List<CaseReference> caseReferences = csvLoader.loadCaseReferenceList(caseListFileName, secret);

        log.info("CaseReferences has {} cases.", caseReferences.size());
        return caseReferences;
    }

    @Override
    protected String getCaseListFileName() {
        return "caserefs-for-dfr-3639-encrypted.csv";
    }

    @Override
    protected String getTaskName() {
        return TASK_NAME;
    }

    @Override
    protected boolean isTaskEnabled() {
        return taskEnabled;
    }

    @Override
    protected CaseType getCaseType() {
        return CaseType.forValue(caseTypeId);
    }

    @Override
    protected String getSummary() {
        return SUMMARY;
    }

    @Override
    protected void executeTask(FinremCaseDetails finremCaseDetails) {
        FinremCaseData caseData = finremCaseDetails.getData();
        ObjectMapper mapper = new ObjectMapper();

        if (caseData.getGeneralEmailWrapper() != null
            && caseData.getGeneralEmailWrapper().getGeneralEmailUploadedDocument() != null) {
            try {
                log.info("Case {} GeneralEmailUploadedDocument: {}", finremCaseDetails.getId(), mapper
                        .writerWithDefaultPrettyPrinter()
                        .writeValueAsString(caseData.getGeneralEmailWrapper().getGeneralEmailUploadedDocument()));
            } catch (JsonProcessingException e) {
                throw new RuntimeException(e);
            }
            log.info("Case {} generalEmailUploadedDocument set to null", finremCaseDetails.getId());
            caseData.getGeneralEmailWrapper().setGeneralEmailUploadedDocument(null);
            try {
                log.info("Case {} GeneralEmailUploadedDocument: {}", finremCaseDetails.getId(), mapper
                        .writerWithDefaultPrettyPrinter()
                        .writeValueAsString(caseData.getGeneralEmailWrapper().getGeneralEmailUploadedDocument()));
            } catch (JsonProcessingException e) {
                throw new RuntimeException(e);
            }
        } else {
            log.info("Case {} has empty generalEmailUploadedDocument field", finremCaseDetails.getId());
        }
    }

    void setSecret(String secret) {
        this.secret = secret;
    }

    void setTaskEnabled(boolean taskEnabled) {
        this.taskEnabled = taskEnabled;
    }

    void setCaseTypeContested() {
        this.caseTypeId = CaseType.CONTESTED.getCcdType();
    }

    public static void encryptFile(String inputFilePath, String outputFilePath, SecretKey key) throws Exception {
        String content = new String(Files.readAllBytes(Paths.get(inputFilePath)));
        CaseReferenceCsvLoader csvLoader = new CaseReferenceCsvLoader();
        String encryptedContent = csvLoader.encrypt(content, key);
        Files.write(Paths.get(outputFilePath), encryptedContent.getBytes());
    }

    public static void decryptFile(String inputFilePath, String outputFilePath, SecretKey key) throws Exception {
        String encryptedContent = new String(Files.readAllBytes(Paths.get(inputFilePath)));
        CaseReferenceCsvLoader csvLoader = new CaseReferenceCsvLoader();
        String decryptedContent = csvLoader.decrypt(encryptedContent, key);
        Files.write(Paths.get(outputFilePath), decryptedContent.getBytes());
    }

    public static void main(String[] args) throws Exception {
        if (args.length < 4) {
            System.out.println("Usage: java ClearGeneralEmailDataFieldTask <encrypt|decrypt> <inputFilePath> <outputFilePath> <secretKey>");
            return;
        }

        String operation = args[0];
        String inputFilePath = args[1];
        String outputFilePath = args[2];
        String secretKeyString = args[3];

        SecretKey key = getKeyFromString(secretKeyString);

        if ("encrypt".equalsIgnoreCase(operation)) {
            encryptFile(inputFilePath, outputFilePath, key);
            System.out.println("File encrypted successfully.");
        } else if ("decrypt".equalsIgnoreCase(operation)) {
            decryptFile(inputFilePath, outputFilePath, key);
            System.out.println("File decrypted successfully.");
        } else {
            System.out.println("Invalid operation. Use 'encrypt' or 'decrypt'.");
        }
    }
}
