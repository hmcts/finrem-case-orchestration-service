package uk.gov.hmcts.reform.finrem.caseorchestration.scheduler;

import lombok.Setter;
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

import java.time.LocalDate;
import java.util.List;

/**
 * Scheduled task to find cases where GeneralEmailDataField is not empty and clear the field.
 * To enable the task to execute set environment variables:
 * <ul>
 *     <li>CRON_AMEND_NOTE_TASK_ENABLED=true</li>
 *     <li>TASK_NAME=AmendGeneralEmailTask</li>
 *     <li>CRON_AMEND_NOTE_TASK_CASE_TYPE_ID=FinancialRemedyContested | FinancialRemedyMVP2</li>
 *     <li>CRON_AMEND_NOTE_TASK_BATCH_SIZE=number of cases to search for</li>
 *     <li>CRON_CSV_FILE_DECRYPT_KEY=secret key to decrypt the csv file</li>
 * </ul>
 */
@Component
@Slf4j
public class AmendNoteTask extends EncryptedCsvFileProcessingTask {

    private static final String TASK_NAME = "AmendNoteTask";
    private static final String SUMMARY = "DFR-4570";
    @Value("${cron.amendNoteTask.enabled:false}")
    private boolean taskEnabled;
    @Value("${cron.amendNoteTask.caseTypeId:FinancialRemedyContested}")
    private String caseTypeId;
    @Value("${cron.amendNoteTask.batchSize:100}")
    private int batchSize;
    @Setter
    @Value("${cron.amendNoteTask.caseListFileName:caserefs-encrypted.csv}")
    private String csvFile;

    protected AmendNoteTask(CaseReferenceCsvLoader csvLoader, CcdService ccdService, SystemUserService systemUserService,
                            FinremCaseDetailsMapper finremCaseDetailsMapper) {
        super(csvLoader, ccdService, systemUserService, finremCaseDetailsMapper);
    }

    @Override
    protected List<CaseReference> getCaseReferences() {
        log.info("Starting Task Cron....\n"
                + "TASK_NAME: {}\n"
                + "SUMMARY: {}\n"
                + "TASK_ENABLED: {}\n"
                + "BATCH_SIZE: {}\n"
                + "CASE_TYPE_ID: {}\n"
                + "CSV_FILE: {}\n"
                + "SECRET KEY EXIST: {}",
            getTaskName(),
            getSummary(),
            taskEnabled,
            batchSize,
            caseTypeId,
            getCaseListFileName(),
            secret != null && !secret.isEmpty());

        if (secret.isEmpty()) {
            log.error("Secret key is empty. Unable to decrypt the csv file. "
                + "Please configure Azure Key Vault or set the secret key [cron-csv-file-decrypt-key].");
            return List.of();
        }

        String caseListFileName = getCaseListFileName();
        log.info("Getting case references for Task migration from csv file {}", caseListFileName);

        CaseReferenceCsvLoader csvLoader = new CaseReferenceCsvLoader();
        List<CaseReference> caseReferences = csvLoader.loadCaseReferenceList(caseListFileName, secret);

        log.info("CaseReferences has {} cases.", caseReferences.size());
        return caseReferences;
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

    @Override
    protected void executeTask(FinremCaseDetails finremCaseDetails) {
        FinremCaseData caseData = finremCaseDetails.getData();
        LocalDate targetDate = LocalDate.of(2026, 2, 2);

        if (caseData.getCaseNotesCollection() != null && !caseData.getCaseNotesCollection().isEmpty()) {
            log.info("Before Removal CaseNotesCollection: {}", caseData.getCaseNotesCollection());
            boolean removed = caseData.getCaseNotesCollection().removeIf(
                note -> targetDate.equals(note.getValue().getCaseNoteDate())
            );
            log.info("After Removal CaseNotesCollection: {}", caseData.getCaseNotesCollection());

            if (removed) {
                log.info("Case {}: Removed case notes with date {}", finremCaseDetails.getId(), targetDate);
            } else {
                log.info("Case {}: No case notes with date {} found.", finremCaseDetails.getId(), targetDate);
            }
        } else {
            log.info("Case {} has empty getCaseNotesCollection field", finremCaseDetails.getId());
        }
    }
}
