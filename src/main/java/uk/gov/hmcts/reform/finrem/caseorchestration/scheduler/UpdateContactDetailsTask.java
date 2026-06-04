package uk.gov.hmcts.reform.finrem.caseorchestration.scheduler;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.finrem.caseorchestration.mapper.FinremCaseDetailsMapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.ChangedRepresentative;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseData;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.RepresentationUpdate;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.RepresentationUpdateHistoryCollection;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.CcdService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.SystemUserService;
import uk.gov.hmcts.reform.finrem.caseorchestration.utils.EmailUtils;
import uk.gov.hmcts.reform.finrem.caseorchestration.utils.csv.CaseReference;
import uk.gov.hmcts.reform.finrem.caseorchestration.utils.csv.CaseReferenceCsvLoader;

import java.util.List;

import static java.util.Objects.isNull;
import static uk.gov.hmcts.reform.finrem.caseorchestration.helper.MaskHelper.maskEmail;

/**
 *  Cron task to update contact details for cases with invalid email addresses in the RespondentSolicitorEmail field
 *  and RepresentationUpdateHistory field. The task will update the invalid email addresses to a valid email address.
 */
@Component
@Slf4j
public class UpdateContactDetailsTask extends CsvFileProcessingTask {

    @Value("${cron.csvFile.decrypt.key:DUMMY_SECRET}")
    private String secret;
    private static final String TASK_NAME = "UpdateContactDetailsTask";
    private static final String SUMMARY = "DFR-5006";
    @Value("${cron.updateContactDetails.task.enabled:false}")
    private boolean taskEnabled;
    @Value("${cron.updateContactDetails.caseTypeId:FinancialRemedyContested}")
    private String caseTypeId;
    @Value("${cron.updateContactDetails.batchSize:1}")
    private int batchSize;
    @Value("${cron.updateContactDetails.caseListFileName:caserefs-encrypted.csv}")
    private String csvFile;

    protected UpdateContactDetailsTask(CaseReferenceCsvLoader csvLoader, CcdService ccdService, SystemUserService systemUserService,
                                       FinremCaseDetailsMapper finremCaseDetailsMapper) {
        super(csvLoader, ccdService, systemUserService, finremCaseDetailsMapper);
    }

    @Override
    protected List<CaseReference> getCaseReferences() {
        log.info("Starting UpdateContactDetailsTask Cron....\n"
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
        log.info("Getting case references for UpdateContactDetailsTask migration from csv file {}", caseListFileName);

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

    private static final String UPDATED_EMAIL = "updated@amendedbycron.com";

    @Override
    protected void executeTask(FinremCaseDetails finremCaseDetails) {
        FinremCaseData caseData = finremCaseDetails.getData();
        log.info("Case {} Running UpdateContactDetailsTask for this case", finremCaseDetails.getId());

        if (!isNull(caseData.getContactDetailsWrapper().getRespondentSolicitorEmail())) {
            if (!EmailUtils.isValidEmailAddress(caseData.getContactDetailsWrapper().getRespondentSolicitorEmail())) {
                // Check RepresentationUpdateHistory
                amendRepresentationHistory(finremCaseDetails, caseData);

                // Update the RespondentSolicitorEmail field to a valid email address
                log.info("Case {} Updating invalid RespondentSolicitorEmail: {}", finremCaseDetails.getId(),
                    maskEmail(caseData.getContactDetailsWrapper().getRespondentSolicitorEmail()));
                caseData.getContactDetailsWrapper().setRespondentSolicitorEmail(UPDATED_EMAIL);
                log.info("Case {} RespondentSolicitorEmail field amended successfully to: {}",
                    finremCaseDetails.getId(), UPDATED_EMAIL);
            } else {
                log.info("Case {} Nothing updated. Already has valid RespondentSolicitorEmail field", finremCaseDetails.getId());
            }
        } else {
            log.info("Case {} has empty RespondentSolicitorEmail field", finremCaseDetails.getId());
        }
    }

    /**
     *  Check if RepresentationUpdateHistory field contains invalid email addresses and update them to a valid email
     *  address before updating the RespondentSolicitorEmail field.
     */
    private void amendRepresentationHistory(FinremCaseDetails finremCaseDetails, FinremCaseData caseData) {
        if (!isNull(caseData.getRepresentationUpdateHistory()) && !caseData.getRepresentationUpdateHistory().isEmpty()) {
            log.info("Case {} Checking RepresentationUpdateHistory for invalid email addresses for case", finremCaseDetails.getId());

            List<RepresentationUpdateHistoryCollection> history = caseData.getRepresentationUpdateHistory();
            history.stream()
                .map(RepresentationUpdateHistoryCollection::getValue)
                .filter(java.util.Objects::nonNull)
                .forEach(representationUpdate -> {
                    updateInvalidAddedEmail(representationUpdate, finremCaseDetails.getId());
                    updateInvalidRemovedEmail(representationUpdate, finremCaseDetails.getId());
                });
        }
    }

    private void updateInvalidAddedEmail(RepresentationUpdate representationUpdate, Long caseId) {
        ChangedRepresentative added = representationUpdate.getAdded();
        if (isNull(added) || isNull(added.getEmail()) || added.getEmail().isBlank()) {
            return;
        }

        if (!EmailUtils.isValidEmailAddress(added.getEmail())) {
            log.info("Case {} Found invalid added email in RepresentationUpdateHistory: {}", caseId, maskEmail(added.getEmail()));
            added.setEmail(UPDATED_EMAIL);
            log.info("Case {} added EMAIL UPDATED TO: {}", caseId, UPDATED_EMAIL);
        }
    }

    private void updateInvalidRemovedEmail(RepresentationUpdate representationUpdate, Long caseId) {
        ChangedRepresentative removed = representationUpdate.getRemoved();
        if (isNull(removed) || isNull(removed.getEmail()) || removed.getEmail().isBlank()) {
            return;
        }

        if (!EmailUtils.isValidEmailAddress(removed.getEmail())) {
            log.info("Case {} Found invalid removed email in RepresentationUpdateHistory: {}", caseId, maskEmail(removed.getEmail()));
            removed.setEmail(UPDATED_EMAIL);
            log.info("Case {} removed EMAIL UPDATED TO: {}", caseId, UPDATED_EMAIL);
        }
    }

    void setSecret(String secret) {
        this.secret = secret;
    }

    void setTaskEnabled(boolean taskEnabled) {
        this.taskEnabled = taskEnabled;
    }

    void setCaseTypeContested(String caseTypeId) {
        this.caseTypeId = caseTypeId;
    }

    public String getCsvFile() {
        return csvFile;
    }

    public void setCsvFile(String csvFile) {
        this.csvFile = csvFile;
    }
}
