package uk.gov.hmcts.reform.finrem.caseorchestration.handler.managebarrister;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.ccd.client.model.CallbackRequest;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.ccd.callback.CallbackType;
import uk.gov.hmcts.reform.finrem.caseorchestration.ccd.domain.EventType;
import uk.gov.hmcts.reform.finrem.caseorchestration.controllers.GenericAboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.finrem.caseorchestration.handler.CallbackHandler;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.CaseType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.Barrister;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.BarristerData;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.barristers.ManageBarristerService;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class ManageBarristerSubmittedHandler implements CallbackHandler<Map<String, Object>> {

    private final ManageBarristerService manageBarristerService;

    @Override
    public boolean canHandle(CallbackType callbackType, CaseType caseType, EventType eventType) {
        return CallbackType.SUBMITTED.equals(callbackType)
            && CaseType.CONTESTED.equals(caseType)
            && EventType.MANAGE_BARRISTER.equals(eventType);
    }

    @Override
    public GenericAboutToStartOrSubmitCallbackResponse<Map<String, Object>> handle(CallbackRequest callbackRequest, String userAuthorisation) {
        CaseDetails caseDetails = callbackRequest.getCaseDetails();
        CaseDetails caseDetailsBefore = callbackRequest.getCaseDetailsBefore();

        List<Barrister> barristers = manageBarristerService
            .getBarristersForParty(caseDetails, userAuthorisation).stream()
            .map(BarristerData::getBarrister).collect(Collectors.toList());

        List<Barrister> barristersBeforeEvent = manageBarristerService
            .getBarristersForParty(caseDetailsBefore, userAuthorisation).stream()
            .map(BarristerData::getBarrister).collect(Collectors.toList());

        log.info("Current barristers: {}", barristers);
        log.info("Original Barristers: {}", barristersBeforeEvent);

        manageBarristerService.notifyBarristerAccess(caseDetails,
            barristers,
            barristersBeforeEvent,
            userAuthorisation);

        return GenericAboutToStartOrSubmitCallbackResponse.<Map<String, Object>>builder().data(caseDetails.getData()).build();
    }
}
