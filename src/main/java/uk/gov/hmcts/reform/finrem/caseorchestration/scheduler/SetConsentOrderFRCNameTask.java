package uk.gov.hmcts.reform.finrem.caseorchestration.scheduler;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.finrem.caseorchestration.mapper.FinremCaseDetailsMapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseData;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.CcdService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.SelectedCourtService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.SystemUserService;
import uk.gov.hmcts.reform.finrem.caseorchestration.utils.csv.CaseReferenceCsvLoader;

@Component
@Slf4j
public class SetConsentOrderFRCNameTask extends EncryptedCsvFileProcessingTask {
    private static final String TASK_NAME = "SetConsentOrderFRCNameTask";
    private static final String SUMMARY = "DFR-3693 CT Fix ConsentOrderFRCName";

    @Value("${cron.updateConsentOrderFRCName.enabled:false}")
    private boolean taskEnabled;
    @Value("${cron.updateConsentOrderFRCName.caseTypeId:FinancialRemedyContested}")
    private String caseTypeId;
    @Value("${cron.updateConsentOrderFRCName.caseListFileName:updateConsentOrderFRCName-encrypted.csv}")
    private String csvFile;

    private final SelectedCourtService selectedCourtService;


    public SetConsentOrderFRCNameTask(CaseReferenceCsvLoader csvLoader, CcdService ccdService,
                                      SystemUserService systemUserService,
                                      FinremCaseDetailsMapper finremCaseDetailsMapper,
                                      SelectedCourtService selectedCourtService) {
        super(csvLoader, ccdService, systemUserService, finremCaseDetailsMapper);
        this.selectedCourtService = selectedCourtService;
    }

    @Override
    protected void executeTask(FinremCaseDetails finremCaseDetails) {
        FinremCaseData caseData = finremCaseDetails.getData();

        selectedCourtService.setSelectedCourtDetailsIfPresent(caseData);

        log.info("Case ID: {} - Updated consentOrderFRCName.", finremCaseDetails.getId());


    }

    @Override
    protected String getDescription(FinremCaseDetails finremCaseDetails) {
        return String.format("ConsentOrderFRCName is: %s",
            finremCaseDetails.getData().getConsentOrderWrapper().getConsentOrderFrcName());
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
