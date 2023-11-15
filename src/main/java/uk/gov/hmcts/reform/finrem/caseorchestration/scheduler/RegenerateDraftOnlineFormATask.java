package uk.gov.hmcts.reform.finrem.caseorchestration.scheduler;

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
public class RegenerateDraftOnlineFormATask extends BaseTask {


    private final OnlineFormDocumentService onlineFormDocumentService;

    @Value("${cron.regenerateMiniFormA.enabled:false}")
    private boolean isRegenerateMiniFormATaskEnabled;

    @Autowired
    protected RegenerateDraftOnlineFormATask(CaseReferenceCsvLoader csvLoader,
                                             CcdService ccdService,
                                             SystemUserService systemUserService,
                                             FinremCaseDetailsMapper finremCaseDetailsMapper, OnlineFormDocumentService onlineFormDocumentService) {
        super(csvLoader, ccdService, systemUserService, finremCaseDetailsMapper);
        this.onlineFormDocumentService = onlineFormDocumentService;
    }

    @Override
    protected String getCaseListFileName() {
        return "regenerateDraftFormA.csv";
    }

    @Override
    protected String getTaskName() {
        return "RegenerateDraftOnlineFormATask";
    }

    @Override
    protected boolean isTaskEnabled() {
        return isRegenerateMiniFormATaskEnabled;
    }

    @Override
    protected CaseType getCaseType() {
        return CaseType.CONTESTED;
    }

    @Override
    protected String getSummary() {
        return "Regenerate miniform a -  DFR-2523";
    }

    @Override
    protected void executeTask(FinremCaseDetails finremCaseDetails) {

        finremCaseDetails.getData().setMiniFormA(onlineFormDocumentService.generateDraftContestedMiniFormA(getSystemUserToken(), finremCaseDetails));
    }
}
