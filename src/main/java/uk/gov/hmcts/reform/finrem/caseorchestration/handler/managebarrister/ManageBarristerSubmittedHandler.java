package uk.gov.hmcts.reform.finrem.caseorchestration.handler.managebarrister;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.ccd.client.model.CallbackRequest;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.ccd.callback.CallbackType;
import uk.gov.hmcts.reform.finrem.caseorchestration.handler.CallbackHandler;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.CaseType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.EventType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.Barrister;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.BarristerData;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.barristers.ManageBarristerService;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class ManageBarristerSubmittedHandler implements CallbackHandler {

    private final ManageBarristerService manageBarristerService;

    @Override
    public boolean canHandle(CallbackType callbackType, CaseType caseType, EventType eventType) {
        return CallbackType.SUBMITTED.equals(callbackType)
            && CaseType.CONTESTED.equals(caseType)
            && EventType.MANAGE_BARRISTER.equals(eventType);
    }

    @Override
    public AboutToStartOrSubmitCallbackResponse handle(CallbackRequest callbackRequest, String userAuthorisation) {
        CaseDetails caseDetails = callbackRequest.getCaseDetails();
        CaseDetails caseDetailsBefore = callbackRequest.getCaseDetailsBefore();

        List<Barrister> barristers = manageBarristerService
            .getBarristersForParty(caseDetails, userAuthorisation).stream()
            .map(BarristerData::getBarrister).toList();

        List<Barrister> barristersBeforeEvent = manageBarristerService
            .getBarristersForParty(caseDetailsBefore, userAuthorisation).stream()
            .map(BarristerData::getBarrister).toList();

        log.info("Current barristers: {}", barristers.toString());
        log.info("Original Barristers: {}", barristersBeforeEvent.toString());


        manageBarristerService.notifyBarristerAccess(caseDetails,
            barristers,
            barristersBeforeEvent);

        return AboutToStartOrSubmitCallbackResponse.builder().data(caseDetails.getData()).build();
    }
}
