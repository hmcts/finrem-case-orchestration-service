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
import uk.gov.hmcts.reform.finrem.caseorchestration.service.managehearings.ManageHearingsMigrationService;
import uk.gov.hmcts.reform.finrem.caseorchestration.utils.csv.CaseReferenceCsvLoader;

@Component
@Slf4j
public class ManageHearingsMigrationTask extends EncryptedCsvFileProcessingTask {

    private static final String TASK_NAME = "ManageHearingsMigrationTask";

    private static final String SUMMARY = "Manage Hearings migration";

    @Value("${cron.manageHearingsMigration.enabled:false}")
    private boolean taskEnabled;

    @Value("${cron.manageHearingsMigration.caseListFileName:updateConsentOrderFRCName-encrypted.csv}")
    private String csvFile;

    @Value("${cron.manageHearingsMigration.mhMigrationVersion:1}")
    private String mhMigrationVersion;

    private final ManageHearingsMigrationService manageHearingsMigrationService;

    public ManageHearingsMigrationTask(CaseReferenceCsvLoader csvLoader, CcdService ccdService,
                                       SystemUserService systemUserService, FinremCaseDetailsMapper finremCaseDetailsMapper,
                                       ManageHearingsMigrationService manageHearingsMigrationService) {
        super(csvLoader, ccdService, systemUserService, finremCaseDetailsMapper);
        this.manageHearingsMigrationService = manageHearingsMigrationService;
    }

    @Override
    protected void executeTask(FinremCaseDetails finremCaseDetails) {
        FinremCaseData caseData = finremCaseDetails.getData();
        if (!manageHearingsMigrationService.wasMigrated(caseData)) {
            manageHearingsMigrationService.populateListForHearingWrapper(caseData);
            manageHearingsMigrationService.populateListForInterimHearingWrapper(caseData);
            manageHearingsMigrationService.populateGeneralApplicationWrapper(caseData);
            manageHearingsMigrationService.populateDirectionDetailsCollection(caseData);
            manageHearingsMigrationService.markCaseDataMigrated(caseData, mhMigrationVersion);
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
        return CaseType.CONTESTED;
    }

    @Override
    protected String getSummary() {
        return SUMMARY;
    }
}
