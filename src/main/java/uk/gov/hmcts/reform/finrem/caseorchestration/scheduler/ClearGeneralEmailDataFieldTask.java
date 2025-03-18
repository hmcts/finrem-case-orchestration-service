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
import uk.gov.hmcts.reform.finrem.caseorchestration.utils.csv.CaseReference;
import uk.gov.hmcts.reform.finrem.caseorchestration.utils.csv.CaseReferenceCsvLoader;

import java.util.List;

/**
 * Scheduled task to find cases where GeneralEmailDataField is not empty and clear the field.
 * To enable the task to execute set environment variables:
 * <ul>
 *     <li>CRON_NULL_CASEROLEIDS_WHERE_EMPTY_ENABLED=true</li>
 *     <li>TASK_NAME=ClearGeneralEmailDataFieldTask</li>
 *     <li>CRON_NULL_CASEROLEIDS_WHERE_EMPTY_CASE_TYPE_ID=FinancialRemedyContested | FinancialRemedyMVP2</li>
 *     <li>CRON_NULL_CASEROLEIDS_WHERE_EMPTY_BATCH_SIZE=number of cases to search for</li>
 * </ul>
 */
@Component
@Slf4j
public class ClearGeneralEmailDataFieldTask extends CsvFileProcessingTask {

    @Value("${cron.clearGeneralEmailDataFieldTask.csvfile:caserefs-for-dfr-3639.csv")
    private String csvfile;

    @Value("${cron.clearGeneralEmailDataFieldTask.secret:DUMMY_SECRET}")
    private String secret;

    private static final String TASK_NAME = "ClearGeneralEmailDataFieldTask";
    private static final String SUMMARY = "DFR-3639";
    @Value("${cron.clearGeneralEmailDataFieldTask.enabled:true}")
    private boolean taskEnabled;
    @Value("${cron.clearGeneralEmailDataFieldTask.caseTypeId:FinancialRemedyContested}")
    private String caseTypeId;
    @Value("${cron.clearGeneralEmailDataFieldTask.batchSize:500}")
    private int batchSize;

    protected ClearGeneralEmailDataFieldTask(CaseReferenceCsvLoader csvLoader, CcdService ccdService, SystemUserService systemUserService,
                                             FinremCaseDetailsMapper finremCaseDetailsMapper) {
        super(csvLoader, ccdService, systemUserService, finremCaseDetailsMapper);
    }

    @Override
    protected List<CaseReference> getCaseReferences() {
        log.info("Getting case references for GeneralEmailDataFieldTask migration");
        String caseListFileName = getCaseListFileName();

        CaseReferenceCsvLoader csvLoader = new CaseReferenceCsvLoader();
        List<CaseReference> caseReferences;
        try {
            caseReferences = csvLoader.loadCaseReferenceList(caseListFileName, secret);
        } catch (Exception e) {
            log.error("Error decrypting and loading case references from {} Exception: {}", caseListFileName, e);
            throw new RuntimeException(e);
        }

        log.info("CaseReferences has {} cases.", caseReferences.size());
        return caseReferences;
    }

    @Override
    protected String getCaseListFileName() {
        return csvfile;
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

        if (caseData.getGeneralEmailWrapper() != null
            && caseData.getGeneralEmailWrapper().getGeneralEmailUploadedDocument() != null) {
            log.info("Case {} will have generalEmailUploadedDocument set to null", finremCaseDetails.getId());
            caseData.getGeneralEmailWrapper().setGeneralEmailUploadedDocument(null);
        } else {
            log.info("Case {} has empty generalEmailUploadedDocument field", finremCaseDetails.getId());
        }
    }

    void setTaskEnabled(boolean taskEnabled) {
        this.taskEnabled = taskEnabled;
    }

    void setCaseTypeContested() {
        this.caseTypeId = CaseType.CONTESTED.getCcdType();
    }
}
