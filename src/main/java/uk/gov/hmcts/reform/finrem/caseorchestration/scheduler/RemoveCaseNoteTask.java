package uk.gov.hmcts.reform.finrem.caseorchestration.scheduler;

import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.finrem.caseorchestration.mapper.FinremCaseDetailsMapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseNotesCollection;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseData;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.CcdService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.SystemUserService;
import uk.gov.hmcts.reform.finrem.caseorchestration.utils.csv.CaseReferenceCsvLoader;

import java.util.List;

@Service
@Slf4j
public class RemoveCaseNoteTask extends EncryptedCsvFileProcessingTask {

    @Value("${cron.removeCaseNote.task.enabled:false}")
    private boolean isTaskEnabled;

    @Value("${cron.removeCaseNote.caseTypeId:FinancialRemedyContested}")
    private String caseTypeId;

    @Setter
    @Value("${cron.removeCaseNote.caseListFileName:caserefs-encrypted.csv}")
    private String csvFile;

    protected RemoveCaseNoteTask(CaseReferenceCsvLoader csvLoader, CcdService ccdService, SystemUserService systemUserService,
                                 FinremCaseDetailsMapper finremCaseDetailsMapper) {
        super(csvLoader, ccdService, systemUserService, finremCaseDetailsMapper);
    }

    @Override
    protected String getCaseListFileName() {
        return csvFile;
    }

    @Override
    protected String getTaskName() {
        return "RemoveCaseNoteTask";
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
        return "Remove erroneous case note - DFR-XXXX";
    }

    @Override
    protected void executeTask(FinremCaseDetails finremCaseDetails) {
        FinremCaseData caseData = finremCaseDetails.getData();
        Long caseId = finremCaseDetails.getId();

        List<CaseNotesCollection> caseNotes = caseData.getCaseNotesCollection();
        if (caseNotes == null || caseNotes.isEmpty()) {
            log.info("No case notes found for case id {}", caseId);
            return;
        }

        log.info("caseNotesCollection count before: {} for case id {}", caseNotes.size(), caseId);

        caseNotes.removeLast();
        log.info("Removed last case note for case id {}", caseId);

        log.info("caseNotesCollection count after: {} for case id {}", caseNotes.size(), caseId);
    }
}