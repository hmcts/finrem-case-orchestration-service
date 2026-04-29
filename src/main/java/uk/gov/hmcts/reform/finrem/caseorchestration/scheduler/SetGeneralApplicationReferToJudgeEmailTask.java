package uk.gov.hmcts.reform.finrem.caseorchestration.scheduler;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.finrem.caseorchestration.mapper.FinremCaseDetailsMapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseData;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.CcdService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.SystemUserService;
import uk.gov.hmcts.reform.finrem.caseorchestration.utils.csv.CaseReferenceCsvLoader;

@Component
@Slf4j
public class SetGeneralApplicationReferToJudgeEmailTask extends EncryptedCsvFileProcessingTask {
    private static final String TASK_NAME = "SetGeneralApplicationReferToJudgeEmailTask";
    private static final String SUMMARY = "DFR-4922 CT Fix generalApplicationReferToJudgeEmail";

    @Value("${cron.generalApplicationReferToJudgeEmail.enabled:false}")
    private boolean taskEnabled;
    @Value("${cron.generalApplicationReferToJudgeEmail.caseTypeId:FinancialRemedyContested}")
    private String caseTypeId;
    @Value("${cron.generalApplicationReferToJudgeEmail.caseListFileName:updateGeneralApplicationReferToJudgeEmail-encrypted.csv}")
    private String csvFile;

    public SetGeneralApplicationReferToJudgeEmailTask(CaseReferenceCsvLoader csvLoader, CcdService ccdService,
                                                      SystemUserService systemUserService,
                                                      FinremCaseDetailsMapper finremCaseDetailsMapper) {
        super(csvLoader, ccdService, systemUserService, finremCaseDetailsMapper);
    }

    /**
     * Updates the generalApplicationReferToJudgeEmail field if it matches an incorrect value.
     *
     * @param finremCaseDetails the case details to process
     */
    @Override
    protected void executeTask(FinremCaseDetails finremCaseDetails) {
        FinremCaseData caseData = finremCaseDetails.getData();
        String currentEmail = caseData.getGeneralApplicationWrapper().getGeneralApplicationReferToJudgeEmail();

        if ("watford.@j.com".equals(currentEmail)) {
            caseData.getGeneralApplicationWrapper().setGeneralApplicationReferToJudgeEmail("watford@j.com");
            log.info("Updated generalApplicationReferToJudgeEmail for case id {}",
                finremCaseDetails.getId());
        } else {
            log.info("No update required for generalApplicationReferToJudgeEmail for case id {})",
                finremCaseDetails.getId());
        }
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
