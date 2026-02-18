package uk.gov.hmcts.reform.finrem.caseorchestration.scheduler;

import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.finrem.caseorchestration.mapper.FinremCaseDetailsMapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseData;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.CcdService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.SystemUserService;
import uk.gov.hmcts.reform.finrem.caseorchestration.utils.csv.CaseReferenceCsvLoader;

@Service
@Slf4j
public class RemoveVacatedHearing extends EncryptedCsvFileProcessingTask {

    @Value("${cron.removeVacatedHearing.task.enabled:false}")
    private boolean isTaskEnabled;

    @Value("${cron.removeVacatedHearing.caseTypeId:FinancialRemedyContested}")
    private String caseTypeId;

    @Setter
    @Value("${cron.removeVacatedHearing.caseListFileName:caserefs-encrypted.csv}")
    private String csvFile;

    protected RemoveVacatedHearing(CaseReferenceCsvLoader csvLoader, CcdService ccdService, SystemUserService systemUserService,
                                   FinremCaseDetailsMapper finremCaseDetailsMapper) {
        super(csvLoader, ccdService, systemUserService, finremCaseDetailsMapper);
    }

    @Override
    protected String getCaseListFileName() {
        return csvFile;
    }

    @Override
    protected String getTaskName() {
        return "ClearHearingDataTask";
    }

    @Override
    protected boolean isTaskEnabled() {
        return isTaskEnabled;
    }

    @Override
    protected CaseType getCaseType() {
        return CaseType.forValue(caseTypeId);
    }

    @Override
    protected String getSummary() {
        return "Clear vacated/adjourned hearing data - DFR-4584";
    }

    @Override
    protected void executeTask(FinremCaseDetails finremCaseDetails) {
        FinremCaseData caseData = finremCaseDetails.getData();
        log.info("Clearing vacatedOrAdjournedHearings for case id {}", finremCaseDetails.getId());

        log.info("vacatedOrAdjournedHearings count: {}", caseData.getManageHearingsWrapper().getVacatedOrAdjournedHearings().size());
        log.info("vacatedOrAdjournedHearingTabItems count: {}", caseData.getManageHearingsWrapper()
            .getVacatedOrAdjournedHearingTabItems().size());

        if (caseData.getManageHearingsWrapper().getVacatedOrAdjournedHearings().size() == 1) {
            caseData.getManageHearingsWrapper().getVacatedOrAdjournedHearings().clear();
            caseData.getManageHearingsWrapper().getVacatedOrAdjournedHearingTabItems().clear();
            log.info("Cleared vacatedOrAdjournedHearings for case id {}", finremCaseDetails.getId());
        }

        log.info("vacatedOrAdjournedHearings count after: {}", caseData.getManageHearingsWrapper()
            .getVacatedOrAdjournedHearings().size());
        log.info("vacatedOrAdjournedHearingTabItems count after: {}", caseData.getManageHearingsWrapper()
            .getVacatedOrAdjournedHearingTabItems().size());

    }
}
