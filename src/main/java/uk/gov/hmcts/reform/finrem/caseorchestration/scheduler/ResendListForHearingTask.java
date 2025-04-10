package uk.gov.hmcts.reform.finrem.caseorchestration.scheduler;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.mapper.FinremCaseDetailsMapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseData;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.CcdService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.HearingDocumentService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.NotificationService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.SystemUserService;
import uk.gov.hmcts.reform.finrem.caseorchestration.utils.csv.CaseReferenceCsvLoader;

@Component
@Slf4j
public class ResendListForHearingTask extends EncryptedCsvFileProcessingTask {
    private static final String TASK_NAME = "ResendListForHearingTask";
    private static final String SUMMARY = "DFR-3710 - Resend List for Hearing";

    @Value("${cron.resendListForHearing.enabled:false}")
    private boolean taskEnabled;
    @Value("${cron.resendListForHearing.caseTypeId:FinancialRemedyContested}")
    private String caseTypeId;
    @Value("${cron.resendListForHearing.caseListFileName:resendlistforhearing-encrypted.csv}")
    private String csvFile;

    private final NotificationService notificationService;
    private final HearingDocumentService hearingDocumentService;

    public ResendListForHearingTask(CaseReferenceCsvLoader csvLoader, CcdService ccdService,
                                    SystemUserService systemUserService,
                                    FinremCaseDetailsMapper finremCaseDetailsMapper,
                                    NotificationService notificationService,
                                    HearingDocumentService hearingDocumentService) {
        super(csvLoader, ccdService, systemUserService, finremCaseDetailsMapper);
        this.notificationService = notificationService;
        this.hearingDocumentService = hearingDocumentService;
    }

    @Override
    protected boolean isUpdatedRequired(CaseDetails caseDetails) {
        FinremCaseDetails finremCaseDetails = finremCaseDetailsMapper.mapToFinremCaseDetails(caseDetails);

        // Sanity check that a Form C exists on the case
        if (finremCaseDetails.getData().getListForHearingWrapper().getFormC() == null) {
            log.warn("Case ID: {} does not have a Form C", finremCaseDetails.getId());
            return false;
        }

        return isApplicantCorrespondenceRequired(finremCaseDetails)
            || isRespondentCorrespondenceRequired(finremCaseDetails);
    }

    @Override
    protected void executeTask(FinremCaseDetails finremCaseDetails) {
        FinremCaseData caseData = finremCaseDetails.getData();

        caseData.setApplicantCorrespondenceEnabled(isApplicantCorrespondenceRequired(finremCaseDetails));
        caseData.setRespondentCorrespondenceEnabled(isRespondentCorrespondenceRequired(finremCaseDetails));

        String systemUserToken = getSystemUserToken();

        log.info("Case ID: {} Sending initial hearing correspondence for applicant {} and respondent {}",
            finremCaseDetails.getId(),
            caseData.isApplicantCorrespondenceEnabled(),
            caseData.isRespondentCorrespondenceEnabled());

        hearingDocumentService.sendInitialHearingCorrespondence(finremCaseDetails, systemUserToken);
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
    protected String getDescription(FinremCaseDetails finremCaseDetails) {
        return String.format("Applicant correspondence sent: %s. Respondent correspondence sent: %s",
            finremCaseDetails.getData().isApplicantCorrespondenceEnabled(),
            finremCaseDetails.getData().isRespondentCorrespondenceEnabled());
    }

    private boolean isApplicantCorrespondenceRequired(FinremCaseDetails caseDetails) {
        if (!caseDetails.getData().isApplicantRepresentedByASolicitor()) {
            return true;
        } else {
            return !notificationService.isApplicantSolicitorDigitalAndEmailPopulated(caseDetails);
        }
    }

    private boolean isRespondentCorrespondenceRequired(FinremCaseDetails caseDetails) {
        if (!caseDetails.getData().isRespondentRepresentedByASolicitor()) {
            return true;
        } else {
            return !notificationService.isRespondentSolicitorDigitalAndEmailPopulated(caseDetails);
        }
    }
}
