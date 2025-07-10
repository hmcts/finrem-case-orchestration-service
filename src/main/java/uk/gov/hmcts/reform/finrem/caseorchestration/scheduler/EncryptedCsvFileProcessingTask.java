package uk.gov.hmcts.reform.finrem.caseorchestration.scheduler;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import uk.gov.hmcts.reform.finrem.caseorchestration.mapper.FinremCaseDetailsMapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.CcdService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.SystemUserService;
import uk.gov.hmcts.reform.finrem.caseorchestration.utils.csv.CaseReference;
import uk.gov.hmcts.reform.finrem.caseorchestration.utils.csv.CaseReferenceCsvLoader;

import java.util.List;

@Slf4j
public abstract class EncryptedCsvFileProcessingTask extends CsvFileProcessingTask {

    @Value("${cron.csvFile.decrypt.key:DUMMY_SECRET}")
    protected String secret;

    public EncryptedCsvFileProcessingTask(CaseReferenceCsvLoader csvLoader, CcdService ccdService,
                                          SystemUserService systemUserService,
                                          FinremCaseDetailsMapper finremCaseDetailsMapper) {
        super(csvLoader, ccdService, systemUserService, finremCaseDetailsMapper);
    }

    @Override
    protected List<CaseReference> getCaseReferences() {
        String caseListFileName = getCaseListFileName();
        log.info("Getting case references from {}", caseListFileName);

        log.info("""
                Starting Cron....
                TASK_NAME: {}
                SUMMARY: {}
                CSV_FILE: {}
                SECRET KEY EXIST: {}""",
            getTaskName(),
            getSummary(),
            getCaseListFileName(),
            secret != null && !secret.isEmpty());

        List<CaseReference> caseReferences = csvLoader.loadCaseReferenceList(caseListFileName, secret);

        log.info("{} cases read from {}", caseReferences.size(), caseListFileName);
        return caseReferences;
    }
}
