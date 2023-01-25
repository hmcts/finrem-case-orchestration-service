package uk.gov.hmcts.reform.finrem.caseorchestration.handler;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.ccd.client.model.CallbackRequest;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.ccd.callback.CallbackType;
import uk.gov.hmcts.reform.finrem.caseorchestration.controllers.GenericAboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.finrem.caseorchestration.mapper.FinremCaseDetailsMapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.EventType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseData;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.ConsentHearingService;

import java.util.Map;

@Slf4j
@Service
public class HearingConsentSubmittedHandler extends FinremCallbackHandler {

    private final ConsentHearingService service;

    public HearingConsentSubmittedHandler(FinremCaseDetailsMapper finremCaseDetailsMapper,
                                          ConsentHearingService service) {
        super(finremCaseDetailsMapper);
        this.service = service;
    }

    @Override
    public boolean canHandle(CallbackType callbackType, CaseType caseType, EventType eventType) {
        return CallbackType.SUBMITTED.equals(callbackType)
            && CaseType.CONSENTED.equals(caseType)
            && EventType.LIST_FOR_HEARING_CONSENTED.equals(eventType);
    }

    @Override
    public GenericAboutToStartOrSubmitCallbackResponse<FinremCaseData> handle(FinremCallbackRequest finremCallbackRequest,
                                                                              String userAuthorisation) {
        FinremCaseDetails caseDetails = finremCallbackRequest.getCaseDetails();
        FinremCaseDetails caseDetailsBefore = finremCallbackRequest.getCaseDetailsBefore();

        service.sendNotification(caseDetails, caseDetailsBefore);

        return GenericAboutToStartOrSubmitCallbackResponse.<FinremCaseData>builder()
            .data(finremCallbackRequest.getCaseDetails().getData()).build();
    }
}
