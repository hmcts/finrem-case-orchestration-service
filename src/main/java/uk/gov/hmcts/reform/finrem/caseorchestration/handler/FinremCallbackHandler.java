package uk.gov.hmcts.reform.finrem.caseorchestration.handler;

import lombok.RequiredArgsConstructor;
import uk.gov.hmcts.reform.ccd.client.model.CallbackRequest;
import uk.gov.hmcts.reform.finrem.caseorchestration.controllers.GenericAboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.finrem.caseorchestration.mapper.FinremCaseDetailsMapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.EventType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseData;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseDetails;

@RequiredArgsConstructor
public abstract class FinremCallbackHandler implements CallbackHandler<FinremCaseData> {

    private final FinremCaseDetailsMapper finremCaseDetailsMapper;

    @Override
    public GenericAboutToStartOrSubmitCallbackResponse<FinremCaseData> handle(CallbackRequest callbackRequest, String userAuthorisation) {

        FinremCallbackRequest callbackRequestWithFinremCaseDetails =
            mapToFinremCallbackRequest(callbackRequest);

        return handle(callbackRequestWithFinremCaseDetails, userAuthorisation);
    }

    private FinremCallbackRequest mapToFinremCallbackRequest(CallbackRequest callbackRequest) {
        FinremCaseDetails finremCaseDetails = finremCaseDetailsMapper.mapToFinremCaseDetails(callbackRequest.getCaseDetails());
        FinremCaseDetails finremCaseDetailsBefore = null;
        if (callbackRequest.getCaseDetailsBefore() != null) {
            finremCaseDetailsBefore = finremCaseDetailsMapper.mapToFinremCaseDetails(callbackRequest.getCaseDetailsBefore());
        }
        FinremCallbackRequest callbackRequestWithFinremCaseDetails = FinremCallbackRequest.builder()
            .caseDetails(finremCaseDetails)
            .caseDetailsBefore(finremCaseDetailsBefore)
            .eventType(EventType.getEventType(callbackRequest.getEventId()))
            .build();
        return callbackRequestWithFinremCaseDetails;
    }

    public abstract GenericAboutToStartOrSubmitCallbackResponse<FinremCaseData> handle(FinremCallbackRequest callbackRequestWithFinremCaseDetails,
                                                                                       String userAuthorisation);


}
