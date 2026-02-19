package uk.gov.hmcts.reform.finrem.caseorchestration.handler;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.finrem.caseorchestration.ccd.callback.CallbackType;
import uk.gov.hmcts.reform.finrem.caseorchestration.config.DefaultsConfiguration;
import uk.gov.hmcts.reform.finrem.caseorchestration.controllers.GenericAboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.finrem.caseorchestration.mapper.FinremCaseDetailsMapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.EventType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseData;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.OnlineFormDocumentService;

import java.time.LocalDate;

import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.AssignToJudgeReason.DRAFT_CONSENT_ORDER;

@Slf4j
@Service
public class IssueApplicationConsentedAboutToSubmitHandler extends FinremCallbackHandler {

    private final OnlineFormDocumentService onlineFormDocumentService;

    private final DefaultsConfiguration defaultsConfiguration;

    public IssueApplicationConsentedAboutToSubmitHandler(FinremCaseDetailsMapper finremCaseDetailsMapper,
                                                         OnlineFormDocumentService onlineFormDocumentService,
                                                         DefaultsConfiguration defaultsConfiguration) {
        super(finremCaseDetailsMapper);
        this.onlineFormDocumentService = onlineFormDocumentService;
        this.defaultsConfiguration = defaultsConfiguration;
    }

    @Override
    public boolean canHandle(CallbackType callbackType, CaseType caseType, EventType eventType) {
        return CallbackType.ABOUT_TO_SUBMIT.equals(callbackType)
            && CaseType.CONSENTED.equals(caseType)
            && EventType.ISSUE_APPLICATION.equals(eventType);
    }

    @Override
    public GenericAboutToStartOrSubmitCallbackResponse<FinremCaseData> handle(FinremCallbackRequest callbackRequest,
                                                                              String userAuthorisation) {
        log.info(CallbackHandlerLogger.aboutToSubmit(callbackRequest));

        FinremCaseDetails caseDetails = callbackRequest.getCaseDetails();
        FinremCaseData caseData = caseDetails.getData();

        caseData.setMiniFormA(onlineFormDocumentService.generateMiniFormA(userAuthorisation, caseDetails));
        populateAssignToJudgeFields(caseData);

        return GenericAboutToStartOrSubmitCallbackResponse.<FinremCaseData>builder()
            .data(caseData).build();
    }

    private void populateAssignToJudgeFields(FinremCaseData caseData) {
        caseData.setAssignedToJudge(defaultsConfiguration.getAssignedToJudgeDefault());
        caseData.setAssignedToJudgeReason(DRAFT_CONSENT_ORDER);
        caseData.getReferToJudgeWrapper().setReferToJudgeDate(LocalDate.now());
        caseData.getReferToJudgeWrapper().setReferToJudgeText("consent for approval");
    }
}
