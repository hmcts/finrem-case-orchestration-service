package uk.gov.hmcts.reform.finrem.caseorchestration.scheduler;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import uk.gov.hmcts.reform.finrem.caseorchestration.mapper.FinremCaseDetailsMapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseData;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.UploadCaseDocument;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.UploadCaseDocumentCollection;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.CcdService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.SystemUserService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.evidencemanagement.EvidenceManagementDownloadService;
import uk.gov.hmcts.reform.finrem.caseorchestration.utils.csv.CaseReferenceCsvLoader;

import java.util.Objects;

@Component
@Slf4j
public class FindCasesWithMissingDocsTask extends EncryptedCsvFileProcessingTask {
    private static final String TASK_NAME = "FindCasesWithMissingDocsTask";
    private static final String SUMMARY = "FindCasesWithMissingDocsTask";

    @Value("${cron.findCasesWithMissingDocs.enabled:false}")
    private boolean taskEnabled;
    @Value("${cron.findCasesWithMissingDocs.caseTypeId:FinancialRemedyContested}")
    private String caseTypeId;
    @Value("${cron.findCasesWithMissingDocs.caseListFileName:findCasesWithMissingDocs-encrypted.csv}")
    private String csvFile;

    private final EvidenceManagementDownloadService evidenceManagementDownloadService;

    private final SystemUserService systemUserService;

    public FindCasesWithMissingDocsTask(CaseReferenceCsvLoader csvLoader, CcdService ccdService,
                                        SystemUserService systemUserService,
                                        FinremCaseDetailsMapper finremCaseDetailsMapper,
                                        EvidenceManagementDownloadService evidenceManagementDownloadService) {
        super(csvLoader, ccdService, systemUserService, finremCaseDetailsMapper);
        this.evidenceManagementDownloadService = evidenceManagementDownloadService;
        this.systemUserService = systemUserService;
    }

    @Override
    protected void executeTask(FinremCaseDetails finremCaseDetails) {
        FinremCaseData caseData = finremCaseDetails.getData();
        String caseId = finremCaseDetails.getCaseIdAsString();

        caseData.getUploadCaseDocumentWrapper()
            .getAllManageableCollections()
            .stream()
            .map(UploadCaseDocumentCollection::getUploadCaseDocument)
            .filter(Objects::nonNull)
            .forEach(uploadCaseDocument -> checkAndLogMissingDocument(caseId, caseData, uploadCaseDocument));

        log.info("Completed missing document scan for caseId={}", caseId);
    }

    /**
     * Checks if the document exists in the document management store and logs an error if it is missing.
     * <p>
     * This method attempts to download the document using its binary file URL. If the download fails,
     * an error is logged with details about the case, document, and the exception.
     * </p>
     *
     * @param caseId             the unique identifier of the case
     * @param caseData           the data associated with the case, containing state and document details
     * @param uploadCaseDocument the document to be checked for existence, including its metadata and URLs
     */
    private void checkAndLogMissingDocument(String caseId, FinremCaseData caseData, UploadCaseDocument uploadCaseDocument) {
        String caseState = caseData.getState();
        String documentUrl = uploadCaseDocument.getCaseDocuments().getDocumentUrl();
        String documentFilename = uploadCaseDocument.getCaseDocuments().getDocumentFilename();
        String binaryFileUrl = uploadCaseDocument.getCaseDocuments().getDocumentBinaryUrl();

        String collectionName = uploadCaseDocument.getCaseDocumentType() != null
            ? uploadCaseDocument.getCaseDocumentType().name()
            : "unknownType";

        try {
            evidenceManagementDownloadService.download(binaryFileUrl, systemUserService.getSysUserToken());
        } catch (HttpClientErrorException ex) {
            log.error("Error checking document existence: caseId={}, state={}, collection={}, filename={}, url={}, binaryUrl={}",
                caseId, caseState, collectionName, documentFilename, documentUrl, binaryFileUrl, ex);
        }
    }

    @Override
    protected String getDescription(FinremCaseDetails finremCaseDetails) {
        return String.format("Check for dm store: %s",
            finremCaseDetails.getData().getCcdCaseId());
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
