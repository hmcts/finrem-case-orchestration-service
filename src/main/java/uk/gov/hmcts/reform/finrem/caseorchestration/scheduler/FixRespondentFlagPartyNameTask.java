package uk.gov.hmcts.reform.finrem.caseorchestration.scheduler;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.finrem.caseorchestration.mapper.FinremCaseDetailsMapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseData;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.caseflag.caseflag.CaseFlag;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.wrapper.CaseFlagsWrapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.CcdService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.SystemUserService;
import uk.gov.hmcts.reform.finrem.caseorchestration.utils.csv.CaseReferenceCsvLoader;

@Component
@Slf4j
public class FixRespondentFlagPartyNameTask extends EncryptedCsvFileProcessingTask {
    private static final String TASK_NAME = "FixRespondentFlagPartyNameTask";
    private static final String SUMMARY = "DFR-5941 Fix respondent case flag party name";

    @Value("${cron.fixRespondentFlagPartyName.task.enabled:false}")
    private boolean taskEnabled;
    @Value("${cron.fixRespondentFlagPartyName.caseTypeId:FinancialRemedyContested}")
    private String caseTypeId;
    @Value("${cron.fixRespondentFlagPartyName.caseListFileName:fixRespondentFlagPartyName-encrypted.csv}")
    private String csvFile;

    public FixRespondentFlagPartyNameTask(CaseReferenceCsvLoader csvLoader, CcdService ccdService,
                                          SystemUserService systemUserService,
                                          FinremCaseDetailsMapper finremCaseDetailsMapper) {
        super(csvLoader, ccdService, systemUserService, finremCaseDetailsMapper);
    }

    @Override
    protected void executeTask(FinremCaseDetails finremCaseDetails) {
        Long caseId = finremCaseDetails.getId();
        FinremCaseData caseData = finremCaseDetails.getData();
        CaseFlagsWrapper flagsWrapper = caseData.getCaseFlagsWrapper();

        CaseFlag respondentFlags = flagsWrapper.getRespondentFlags();
        String respondentName = caseData.getRespondentFullName();

        respondentFlags.setPartyName(respondentName);

        log.info("Case ID: {} - Respondent flag party name updated.", caseId);
    }

    @Override
    protected String getDescription(FinremCaseDetails finremCaseDetails) {
        return "Update respondent case flag party name to match respondent's current name";
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
