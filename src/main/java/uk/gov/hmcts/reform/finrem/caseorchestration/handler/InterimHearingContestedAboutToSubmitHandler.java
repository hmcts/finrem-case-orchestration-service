package uk.gov.hmcts.reform.finrem.caseorchestration.handler;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.ccd.client.model.CallbackRequest;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.ccd.callback.CallbackType;
import uk.gov.hmcts.reform.finrem.caseorchestration.controllers.GenericAboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.EventType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseType;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.InterimHearingService;

import java.util.List;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class InterimHearingContestedAboutToSubmitHandler
    implements CallbackHandler<Map<String, Object>> {

    private final InterimHearingService interimHearingService;

    @Override
    public boolean canHandle(CallbackType callbackType, CaseType caseType, EventType eventType) {
        return CallbackType.ABOUT_TO_SUBMIT.equals(callbackType)
            && CaseType.CONTESTED.equals(caseType)
            && EventType.INTERIM_HEARING.equals(eventType);
    }

    @Override
    public GenericAboutToStartOrSubmitCallbackResponse<Map<String, Object>> handle(
        CallbackRequest callbackRequest,
        String userAuthorisation) {
        CaseDetails caseDetails = callbackRequest.getCaseDetails();
        CaseDetails caseDetailsBefore = callbackRequest.getCaseDetailsBefore();
        log.info("About to submit Interim hearing for case id {}", caseDetails.getId());
        List<String> errors = interimHearingService.submitInterimHearing(caseDetails, caseDetailsBefore, userAuthorisation);
        if (!errors.isEmpty()) {
            return GenericAboutToStartOrSubmitCallbackResponse.<Map<String, Object>>builder().data(caseDetails.getData()).errors(errors).build();
        }
        return GenericAboutToStartOrSubmitCallbackResponse.<Map<String, Object>>builder().data(caseDetails.getData()).build();
    }
}
