package uk.gov.hmcts.reform.finrem.caseorchestration.handler;

import lombok.RequiredArgsConstructor;
import uk.gov.hmcts.reform.ccd.client.model.CallbackRequest;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.controllers.GenericAboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.finrem.caseorchestration.error.InvalidCaseDataException;
import uk.gov.hmcts.reform.finrem.caseorchestration.mapper.FinremCaseDetailsMapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.EventType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.Temp;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseData;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.wrapper.SessionWrapper;

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.List;

import static org.apache.commons.lang3.reflect.FieldUtils.getFieldsListWithAnnotation;
import static org.springframework.http.HttpStatus.BAD_REQUEST;

@RequiredArgsConstructor
public abstract class FinremCallbackHandler implements CallbackHandler<FinremCaseData> {

    protected final FinremCaseDetailsMapper finremCaseDetailsMapper;

    @Override
    public GenericAboutToStartOrSubmitCallbackResponse<FinremCaseData> handle(CallbackRequest callbackRequest,
                                                                              String userAuthorisation) {

        FinremCallbackRequest callbackRequestWithFinremCaseDetails =
            mapToFinremCallbackRequest(callbackRequest);

        return handle(callbackRequestWithFinremCaseDetails, userAuthorisation);
    }

    public abstract GenericAboutToStartOrSubmitCallbackResponse<FinremCaseData> handle(FinremCallbackRequest callbackRequestWithFinremCaseDetails,
                                                                                       String userAuthorisation);

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

    protected void validateCaseData(FinremCallbackRequest callbackRequest) {
        if (callbackRequest == null
            || callbackRequest.getCaseDetails() == null
            || callbackRequest.getCaseDetails().getData() == null) {
            throw new InvalidCaseDataException(BAD_REQUEST.value(), "Missing data from callbackRequest.");
        }
    }

    protected FinremCaseDetails removeTemporaryFields(FinremCallbackRequest callbackRequest) {
        FinremCaseDetails finremCaseDetails = callbackRequest.getCaseDetails();
        CaseDetails caseDetails = finremCaseDetailsMapper.mapToCaseDetails(finremCaseDetails);

        getTemporaryWrapperClasses().forEach(temporaryWrapperClasss ->
            getFieldsListWithAnnotation(temporaryWrapperClasss, Temp.class).stream()
                .map(Field::getName)
                .forEach(caseDetails.getData()::remove));

        return finremCaseDetailsMapper.mapToFinremCaseDetails(caseDetails);
    }

    private static List<Class> getTemporaryWrapperClasses() {
        return Arrays.asList(SessionWrapper.class);
    }
}
