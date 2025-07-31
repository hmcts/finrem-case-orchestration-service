package uk.gov.hmcts.reform.finrem.caseorchestration.scheduler;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.finrem.caseorchestration.helper.DocumentHelper;
import uk.gov.hmcts.reform.finrem.caseorchestration.mapper.FinremCaseDetailsMapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseData;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.wrapper.ManageHearingsWrapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.wrapper.MhMigrationWrapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.CcdService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.SystemUserService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.managehearings.ManageHearingsMigrationService;
import uk.gov.hmcts.reform.finrem.caseorchestration.utils.csv.CaseReferenceCsvLoader;

@Component
@Slf4j
public class ManageHearingsMigrationTask extends EncryptedCsvFileProcessingTask {

    @Value("${cron.manageHearingsMigration.enabled:false}")
    private boolean taskEnabled;

    @Value("${cron.manageHearingsMigration.rollback:false}")
    private boolean rollback;

    @Value("${cron.manageHearingsMigration.caseListFileName:manageHearingsMigration-encrypted.csv}")
    private String csvFile;

    @Value("${cron.manageHearingsMigration.mhMigrationVersion:1}")
    private String mhMigrationVersion;

    @Value("${cron.manageHearingsMigration.dryRun:true}")
    private boolean dryRun;

    private final ManageHearingsMigrationService manageHearingsMigrationService;

    private final DocumentHelper documentHelper;

    public ManageHearingsMigrationTask(CaseReferenceCsvLoader csvLoader, CcdService ccdService,
                                       SystemUserService systemUserService, FinremCaseDetailsMapper finremCaseDetailsMapper,
                                       ManageHearingsMigrationService manageHearingsMigrationService,
                                       DocumentHelper documentHelper) {
        super(csvLoader, ccdService, systemUserService, finremCaseDetailsMapper);
        this.manageHearingsMigrationService = manageHearingsMigrationService;
        this.documentHelper = documentHelper;
    }

    @Override
    protected Class[] classesToOverrideJsonInclude() {
        return new Class[] {MhMigrationWrapper.class, ManageHearingsWrapper.class};
    }

    @Override
    protected void executeTask(FinremCaseDetails finremCaseDetails) {
        FinremCaseDetails workingFinremCaseDetails = finremCaseDetails;
        if (dryRun) {
            workingFinremCaseDetails = documentHelper.deepCopy(finremCaseDetails, FinremCaseDetails.class);
        }

        FinremCaseData caseData = workingFinremCaseDetails.getData();
        // It's weird caseData.ccdCaseId is null
        caseData.setCcdCaseId(workingFinremCaseDetails.getId().toString());

        if (rollback) {
            if (manageHearingsMigrationService.wasMigrated(caseData)) {
                log.info("{} - Rolling back Manage Hearings migration.", caseData.getCcdCaseId());
                manageHearingsMigrationService.revertManageHearingMigration(caseData);
            } else {
                log.info("{} - Manage Hearings migration not detected. Rollback skipped.", caseData.getCcdCaseId());
            }
        } else {
            if (!manageHearingsMigrationService.wasMigrated(caseData)) {
                log.info("{} - Starting Manage Hearings migration.", caseData.getCcdCaseId());
                manageHearingsMigrationService.runManageHearingMigration(caseData, mhMigrationVersion);
            } else {
                log.info("{} - Manage Hearings migration already applied. Skipping.", caseData.getCcdCaseId());
            }
        }
    }

    @Override
    protected String getCaseListFileName() {
        return csvFile;
    }

    @Override
    protected String getTaskName() {
        return "ManageHearingsMigrationTask";
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
        return "Manage Hearings migration";
    }
}
