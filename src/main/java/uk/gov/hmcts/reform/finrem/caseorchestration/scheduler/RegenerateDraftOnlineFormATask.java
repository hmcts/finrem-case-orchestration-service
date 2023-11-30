package uk.gov.hmcts.reform.finrem.caseorchestration.scheduler;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.finrem.caseorchestration.mapper.FinremCaseDetailsMapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.CcdService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.OnlineFormDocumentService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.SystemUserService;
import uk.gov.hmcts.reform.finrem.caseorchestration.utils.csv.CaseReferenceCsvLoader;

@Component
@Slf4j
@RequiredArgsConstructor
public class RegenerateDraftOnlineFormATask implements Task {


    private final OnlineFormDocumentService onlineFormDocumentService;

    @Value("${cron.regenerateMiniFormA.enabled:false}")
    private boolean isRegenerateMiniFormATaskEnabled;
    

    @Override
    public String getCaseListFileName() {
        return "regenerateDraftFormA.csv";
    }

    @Override
    public String getTaskName() {
        return "RegenerateDraftOnlineFormATask";
    }

    @Override
    public boolean isTaskEnabled() {
        return isRegenerateMiniFormATaskEnabled;
    }

    @Override
    public CaseType getCaseType() {
        return CaseType.CONTESTED;
    }

    @Override
    public String getSummary() {
        return "Regenerate miniform a -  DFR-2523";
    }

    @Override
    public void executeTask(FinremCaseDetails finremCaseDetails, String authToken) {

        log.info("RegenerateDraftOnlineFormATask started for case id {}", finremCaseDetails.getId());
        finremCaseDetails.getData().setMiniFormA(onlineFormDocumentService.generateDraftContestedMiniFormA(authToken, finremCaseDetails));
    }
}
