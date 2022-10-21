package uk.gov.hmcts.reform.finrem.caseorchestration.handler;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import uk.gov.hmcts.reform.ccd.client.model.CallbackRequest;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.ccd.domain.CaseType;
import uk.gov.hmcts.reform.finrem.caseorchestration.ccd.domain.FinremCaseData;
import uk.gov.hmcts.reform.finrem.caseorchestration.ccd.domain.FinremCaseDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.ccd.domain.State;
import uk.gov.hmcts.reform.finrem.caseorchestration.controllers.GenericAboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.EventType;

@RequiredArgsConstructor
public abstract class FinremCallbackHandler implements CallbackHandler<FinremCaseData> {

    private final ObjectMapper mapper;

    @Override
    public GenericAboutToStartOrSubmitCallbackResponse<FinremCaseData> handle(CallbackRequest callbackRequest, String userAuthorisation) {

        FinremCaseDetails finremCaseDetails = buildFinremCaseDetails(callbackRequest.getCaseDetails());
        FinremCaseDetails finremCaseDetailsBefore = null;
        if (callbackRequest.getCaseDetailsBefore() != null) {
            finremCaseDetailsBefore = buildFinremCaseDetails(callbackRequest.getCaseDetailsBefore());
        }
        FinremCallbackRequest callbackRequestWithFinremCaseDetails = FinremCallbackRequest.builder()
            .caseDetails(finremCaseDetails)
            .caseDetailsBefore(finremCaseDetailsBefore)
            .eventType(EventType.getEventType(callbackRequest.getEventId()))
            .build();

        return handle(callbackRequestWithFinremCaseDetails, userAuthorisation);
    }

    public abstract GenericAboutToStartOrSubmitCallbackResponse<FinremCaseData> handle(FinremCallbackRequest callbackRequestWithFinremCaseDetails,
                                                                                       String userAuthorisation);

    private FinremCaseDetails buildFinremCaseDetails(CaseDetails caseDetails) {

        return FinremCaseDetails.builder()
            .caseType(CaseType.forValue(caseDetails.getCaseTypeId()))
            .id(caseDetails.getId())
            .jurisdiction(caseDetails.getJurisdiction())
            .state(State.forValue(caseDetails.getState()))
            .createdDate(caseDetails.getCreatedDate())
            .securityLevel(caseDetails.getSecurityLevel())
            .callbackResponseStatus(caseDetails.getCallbackResponseStatus())
            .lastModified(caseDetails.getLastModified())
            .securityClassification(caseDetails.getSecurityClassification())
            .data(mapper.convertValue(caseDetails.getData(), FinremCaseData.class))
            .build();
    }

}
