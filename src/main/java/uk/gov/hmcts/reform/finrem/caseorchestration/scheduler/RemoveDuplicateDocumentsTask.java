package uk.gov.hmcts.reform.finrem.caseorchestration.scheduler;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.finrem.caseorchestration.mapper.FinremCaseDetailsMapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseDocument;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseData;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.UploadAdditionalDocumentCollection;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.CcdService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.SystemUserService;
import uk.gov.hmcts.reform.finrem.caseorchestration.utils.csv.CaseReferenceCsvLoader;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Component
@Slf4j
public class RemoveDuplicateDocumentsTask extends EncryptedCsvFileProcessingTask {
    private static final String TASK_NAME = "RemoveDuplicateDocumentsTask";
    private static final String SUMMARY = "DFR-3693 CT RemoveDuplicateDocumentsTask";

    @Value("${cron.removeDuplicateDocuments.enabled:false}")
    private boolean taskEnabled;
    @Value("${cron.removeDuplicateDocuments.caseTypeId:FinancialRemedyContested}")
    private String caseTypeId;
    @Value("${cron.removeDuplicateDocuments.caseListFileName:removeDuplicateDocuments-encrypted.csv}")
    private String csvFile;

    public RemoveDuplicateDocumentsTask(CaseReferenceCsvLoader csvLoader, CcdService ccdService,
                                        SystemUserService systemUserService,
                                        FinremCaseDetailsMapper finremCaseDetailsMapper) {
        super(csvLoader, ccdService, systemUserService, finremCaseDetailsMapper);
    }

    @Override
    protected void executeTask(FinremCaseDetails finremCaseDetails) {
        FinremCaseData caseData = finremCaseDetails.getData();
        List<UploadAdditionalDocumentCollection> additionalDocs =
            caseData.getUploadAdditionalDocument();

        //Initial total
        int originalCount = additionalDocs.size();

        log.info(
            "Case ID: {} - Starting duplicate additional document cleanup. Current document count: {}",
            finremCaseDetails.getId(),
            originalCount
        );

        //Tracks documents already encountered
        Set<List<String>> seenDocuments = new HashSet<>();

        additionalDocs.removeIf(doc -> {
            CaseDocument document = doc.getValue().getAdditionalDocuments();

            List<String> documentKey = Arrays.asList(
                document.getDocumentUrl(),
                document.getDocumentFilename(),
                document.getDocumentBinaryUrl()
            );

            //HashSet add returns false when the same document key has already been seen.
            boolean duplicate = !seenDocuments.add(documentKey);

            if (duplicate) {
                log.info(
                    "Case ID: {} - Removing duplicate additional document: {}",
                    finremCaseDetails.getId(),
                    document.getDocumentFilename()
                );
            }

            return duplicate;
        });

        int updatedCount = additionalDocs.size();
        int duplicatesRemoved = originalCount - updatedCount;

        log.info(
            "Case ID: {} - Duplicate additional document cleanup complete. "
                + "Original count: {}, duplicates removed: {}, updated count: {}",
            finremCaseDetails.getId(),
            originalCount,
            duplicatesRemoved,
            updatedCount
        );
    }

    @Override
    protected String getCaseListFileName() {
        return csvFile;
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
}
