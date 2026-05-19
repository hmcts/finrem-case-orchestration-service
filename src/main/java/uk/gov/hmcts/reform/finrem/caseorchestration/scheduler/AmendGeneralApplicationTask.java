package uk.gov.hmcts.reform.finrem.caseorchestration.scheduler;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.finrem.caseorchestration.mapper.FinremCaseDetailsMapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseData;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.wrapper.GeneralApplicationWrapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.CcdService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.SystemUserService;
import uk.gov.hmcts.reform.finrem.caseorchestration.utils.csv.CaseReference;
import uk.gov.hmcts.reform.finrem.caseorchestration.utils.csv.CaseReferenceCsvLoader;

import java.util.List;

import static java.util.Objects.isNull;

/**
 * This task is responsible for clearing the GeneralApplicationReferToJudgeEmail field in the GeneralApplicationWrapper.
 */
@Component
@Slf4j
public class AmendGeneralApplicationTask extends CsvFileProcessingTask {

    @Value("${cron.csvFile.decrypt.key:DUMMY_SECRET}")
    private String secret;
    private static final String TASK_NAME = "AmendGeneralApplicationTask";
    private static final String SUMMARY = "DFR-5005";
    @Value("${cron.amendGeneralApplication.task.enabled:false}")
    private boolean taskEnabled;
    @Value("${cron.amendGeneralApplication.caseTypeId:FinancialRemedyContested}")
    private String caseTypeId;
    @Value("${cron.amendGeneralApplication.batchSize:1}")
    private int batchSize;
    @Value("${cron.amendGeneralApplication.caseListFileName:caserefs-encrypted.csv}")
    private String csvFile;

    protected AmendGeneralApplicationTask(CaseReferenceCsvLoader csvLoader, CcdService ccdService, SystemUserService systemUserService,
                                          FinremCaseDetailsMapper finremCaseDetailsMapper) {
        super(csvLoader, ccdService, systemUserService, finremCaseDetailsMapper);
    }

    @Override
    protected List<CaseReference> getCaseReferences() {
        log.info("Starting AmendGeneralApplicationTask Cron....\n"
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
        log.info("Getting case references for AmendGeneralApplicationTask migration from csv file {}", caseListFileName);

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

    @Override
    protected void executeTask(FinremCaseDetails finremCaseDetails) {
        FinremCaseData caseData = finremCaseDetails.getData();
        GeneralApplicationWrapper wrapper = caseData.getGeneralApplicationWrapper();
        String referToJudgeEmail = wrapper.getGeneralApplicationReferToJudgeEmail();
        String caseId = String.valueOf(finremCaseDetails.getId());

        if (!isNull(referToJudgeEmail) && !referToJudgeEmail.isEmpty()) {
            log.info("Case {}: Clearing GeneralApplicationReferToJudgeEmail", finremCaseDetails.getId());
            wrapper.setGeneralApplicationReferToJudgeEmail("deprecatedby@amendcron.com");
            log.info("Case {}: Cleared GeneralApplicationReferToJudgeEmail successfully", caseId);
        } else {
            log.info("Case {}: Has empty GeneralApplicationReferToJudgeEmail field", caseId);
        }
    }
}
