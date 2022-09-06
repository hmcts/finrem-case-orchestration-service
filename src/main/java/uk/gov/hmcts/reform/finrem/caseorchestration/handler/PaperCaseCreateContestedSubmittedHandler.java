package uk.gov.hmcts.reform.finrem.caseorchestration.handler;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.ccd.client.model.CallbackRequest;
import uk.gov.hmcts.reform.finrem.caseorchestration.ccd.callback.CallbackType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.CaseType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.EventType;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.CreateCaseService;

import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class PaperCaseCreateContestedSubmittedHandler implements CallbackHandler {


    private final CreateCaseService createCaseService;

    @Override
    public boolean canHandle(CallbackType callbackType, CaseType caseType, EventType eventType) {
        return CallbackType.SUBMITTED.equals(callbackType)
            && CaseType.CONTESTED.equals(caseType)
            && (EventType.NEW_PAPER_CASE.equals(eventType));
    }

    @Override
    public AboutToStartOrSubmitCallbackResponse handle(CallbackRequest callbackRequest,
                                                       String userAuthorisation) {
        log.info("Processing Submitted callback for event {} with Case ID : {}",
            EventType.NEW_PAPER_CASE, callbackRequest.getCaseDetails().getId());

        Map<String, Object> caseData = callbackRequest.getCaseDetails().getData();

        createCaseService.setSupplementaryData(callbackRequest, userAuthorisation);

        return AboutToStartOrSubmitCallbackResponse.builder().data(caseData).build();
    }
}
