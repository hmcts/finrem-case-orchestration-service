package uk.gov.hmcts.reform.finrem.caseorchestration.handler;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.ccd.client.model.CallbackRequest;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.ccd.callback.CallbackType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.CaseType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.EventType;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.ManageCaseDocumentsService;

@Slf4j
@Service
@RequiredArgsConstructor
public class AmendApplicationAboutToStartCaseHandler implements CallbackHandler {

    private final ManageCaseDocumentsService manageCaseDocumentsService;

    @Override
    public boolean canHandle(CallbackType callbackType, CaseType caseType, EventType eventType) {
        return CallbackType.ABOUT_TO_START.equals(callbackType)
            && CaseType.CONTESTED.equals(caseType)
            && EventType.FR_NEW_PAPER_CASE_EVENT.equals(eventType);
    }

    @Override
    public AboutToStartOrSubmitCallbackResponse handle(CallbackRequest callbackRequest, String userAuthorisation) {

        CaseDetails caseDetails = callbackRequest.getCaseDetails();

        return AboutToStartOrSubmitCallbackResponse.builder().data(
            manageCaseDocumentsService.setApplicantAndRespondentDocumentsCollection(caseDetails)).build();

    }
}
