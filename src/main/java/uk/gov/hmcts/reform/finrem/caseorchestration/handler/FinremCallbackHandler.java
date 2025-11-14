package uk.gov.hmcts.reform.finrem.caseorchestration.handler;

import lombok.RequiredArgsConstructor;
import uk.gov.hmcts.reform.ccd.client.model.CallbackRequest;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.controllers.GenericAboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.finrem.caseorchestration.error.InvalidCaseDataException;
import uk.gov.hmcts.reform.finrem.caseorchestration.mapper.FinremCaseDetailsMapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.EventType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.TemporaryField;
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

        return handle(
            removeTemporaryFields(callbackRequestWithFinremCaseDetails),
            userAuthorisation);
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

    protected boolean shouldClearTemporaryFields() {
        return false;
    }

    /**
     * Removes fields marked with {@link TemporaryField} from the case data when required.
     *
     * <p>
     * If {@link #shouldClearTemporaryFields()} returns {@code true}, this method finds all classes
     * that contain fields annotated with {@link TemporaryField} and removes those fields from
     * the case data map. The updated data is then mapped back into a new {@link FinremCaseDetails}
     * object and returned within a rebuilt {@link FinremCallbackRequest}.
     *
     * <p>
     * If temporary fields should not be cleared, the original {@code callbackRequest} is returned.
     *
     * @param callbackRequest the incoming callback request containing case details
     * @return a new {@link FinremCallbackRequest} with temporary fields removed, or the original
     *         request if no removal is required
     */
    protected FinremCallbackRequest removeTemporaryFields(FinremCallbackRequest callbackRequest) {
        if (!shouldClearTemporaryFields()) {
            return callbackRequest;
        }

        FinremCaseDetails finremCaseDetails = callbackRequest.getCaseDetails();
        CaseDetails caseDetails = finremCaseDetailsMapper.mapToCaseDetails(finremCaseDetails);

        getClassesWithTemporaryFieldAnnotation().forEach(clazz ->
            getFieldsListWithAnnotation(clazz, TemporaryField.class).stream()
                .map(Field::getName)
                .forEach(caseDetails.getData()::remove));

        return callbackRequest.toBuilder().caseDetails(
            finremCaseDetailsMapper.mapToFinremCaseDetails(caseDetails)
        ).build();
    }

    private static List<Class> getClassesWithTemporaryFieldAnnotation() {
        return Arrays.asList(SessionWrapper.class);
    }
}
