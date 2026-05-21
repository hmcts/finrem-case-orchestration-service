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

import java.time.LocalDate;

@Component
@Slf4j
public class SetDateOfMarriageTask extends EncryptedCsvFileProcessingTask {
    private static final String TASK_NAME = "SetDateOfMarriageTask";
    private static final String SUMMARY = "DFR-5060 CT Fix Date of Marriage";

    @Value("${cron.dateOfMarriage.enabled:false}")
    private boolean taskEnabled;
    @Value("${cron.dateOfMarriage.caseTypeId:FinancialRemedyContested}")
    private String caseTypeId;
    @Value("${cron.dateOfMarriage.caseListFileName:updateDateOfMarriage-encrypted.csv}")
    private String csvFile;

    public SetDateOfMarriageTask(CaseReferenceCsvLoader csvLoader, CcdService ccdService,
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
        LocalDate dateOfMarriage = caseData.getDateOfMarriage();

        if (dateOfMarriage.equals(LocalDate.of(2005, 3, 25))) {
            log.info("Case ID: {} - Date of Marriage found. Updating...",
                finremCaseDetails.getId());
            caseData.setDateOfMarriage(LocalDate.of(2005, 3, 29));
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
