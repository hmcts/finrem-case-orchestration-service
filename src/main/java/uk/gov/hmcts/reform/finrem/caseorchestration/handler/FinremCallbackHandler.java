package uk.gov.hmcts.reform.finrem.caseorchestration.handler;

import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.text.StringEscapeUtils;
import uk.gov.hmcts.reform.ccd.client.model.CallbackRequest;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.controllers.GenericAboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.finrem.caseorchestration.error.InvalidCaseDataException;
import uk.gov.hmcts.reform.finrem.caseorchestration.mapper.FinremCaseDetailsMapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.EventType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.TemporaryField;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseDocument;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseData;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.wrapper.Bin;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.wrapper.CaseDataMetricsWrapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.wrapper.ContactDetailsWrapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.wrapper.GeneralApplicationWrapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.wrapper.ManageCaseDocumentsWrapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.wrapper.StopRepresentationWrapper;

import java.lang.reflect.Field;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;
import static java.util.Optional.ofNullable;
import static org.apache.commons.lang3.reflect.FieldUtils.getFieldsListWithAnnotation;
import static org.springframework.http.HttpStatus.BAD_REQUEST;

@RequiredArgsConstructor
public abstract class FinremCallbackHandler implements CallbackHandler<FinremCaseData> {

    protected static final List<String> NO_WARNINGS = null;

    protected final FinremCaseDetailsMapper finremCaseDetailsMapper;

    @Override
    public GenericAboutToStartOrSubmitCallbackResponse<FinremCaseData> handle(CallbackRequest callbackRequest,
                                                                              String userAuthorisation) {
        FinremCallbackRequest finremCallbackRequest = mapToFinremCallbackRequest(callbackRequest);
        FinremCaseData finremCaseData = finremCallbackRequest.getFinremCaseData();
        Bin bin = finremCaseData.getBin();
        if (shouldClearBinBeforeHandle()) {
            bin.clearBin();
        }
        GenericAboutToStartOrSubmitCallbackResponse<FinremCaseData> response = handle(finremCallbackRequest, userAuthorisation);
        return postHandle(response, finremCaseData, userAuthorisation);
    }

    public abstract GenericAboutToStartOrSubmitCallbackResponse<FinremCaseData> handle(FinremCallbackRequest callbackRequestWithFinremCaseDetails,
                                                                                       String userAuthorisation);

    protected GenericAboutToStartOrSubmitCallbackResponse<FinremCaseData> postHandle(
        GenericAboutToStartOrSubmitCallbackResponse<FinremCaseData> response,
        FinremCaseData finremCaseData, String userAuthorisation) {

        if (shouldClearTemporaryFieldsAfterHandle()) {
            return removeTemporaryFieldsAfterHandled(response);
        }
        return response;
    }

    protected void validateCaseData(FinremCallbackRequest callbackRequest) {
        if (isNull(callbackRequest)
            || isNull(callbackRequest.getCaseDetails())
            || isNull(callbackRequest.getCaseDetails().getData())) {
            throw new InvalidCaseDataException(BAD_REQUEST.value(), "Missing data from callbackRequest.");
        }
    }

    protected boolean shouldClearBinBeforeHandle() {
        return true;
    }

    protected boolean shouldClearTemporaryFieldsAfterHandle() {
        return false;
    }

    /**
     * Removes all fields marked with {@link TemporaryField} from the case data
     * in the given callback response.
     *
     * <p>
     * If clearing is required, the method maps the response data into
     * {@link FinremCaseDetails}, converts it into a CCD {@link CaseDetails} object,
     * finds all fields annotated with {@link TemporaryField}, and removes those field
     * names from the case data. It then maps the cleaned data back into
     * {@link FinremCaseData} and returns a new response containing the updated data.
     *
     * @param response the callback response containing case data to clean
     * @return a response with temporary fields removed, or the original response if
     *         clearing is not needed
     */
    protected GenericAboutToStartOrSubmitCallbackResponse<FinremCaseData> removeTemporaryFieldsAfterHandled(
        GenericAboutToStartOrSubmitCallbackResponse<FinremCaseData> response) {
        FinremCaseData finremCaseData = response.getData();

        Map<String, Object> toBeSanitisedMap = finremCaseDetailsMapper.finremCaseDataToMap(finremCaseData);

        Bin bin = finremCaseData.getBin();
        sanitise(toBeSanitisedMap, bin);

        FinremCaseData sanitisedFinremCaseData = finremCaseDetailsMapper.mapToFinremCaseData(toBeSanitisedMap);
        sanitisedFinremCaseData.setBin(bin);
        return response.toBuilder().data(sanitisedFinremCaseData).build();
    }

    protected GenericAboutToStartOrSubmitCallbackResponse<FinremCaseData> submittedResponse() {
        return GenericAboutToStartOrSubmitCallbackResponse.<FinremCaseData>builder().build();
    }

    protected GenericAboutToStartOrSubmitCallbackResponse<FinremCaseData> submittedResponse(String confirmationHeader,
                                                                                            String confirmationBody) {
        return GenericAboutToStartOrSubmitCallbackResponse.<FinremCaseData>builder()
            .confirmationHeader(confirmationHeader)
            .confirmationBody(confirmationBody)
            .build();
    }

    protected GenericAboutToStartOrSubmitCallbackResponse<FinremCaseData> responseWithoutWarnings(FinremCaseData finremCaseData,
                                                                                                  List<String> errors) {
        return responseWithoutWarnings(finremCaseData, errors, null);
    }

    protected GenericAboutToStartOrSubmitCallbackResponse<FinremCaseData> responseWithoutWarnings(FinremCaseData finremCaseData,
                                                                                                  List<String> errors,
                                                                                                  String postState) {
        return response(finremCaseData, null, errors, postState);
    }

    protected GenericAboutToStartOrSubmitCallbackResponse<FinremCaseData> response(FinremCaseData finremCaseData) {
        return response(finremCaseData, null, null);
    }

    protected GenericAboutToStartOrSubmitCallbackResponse<FinremCaseData> response(FinremCaseData finremCaseData,
                                                                                   List<String> warnings, List<String> errors) {
        return response(finremCaseData, warnings, errors, null);
    }

    protected GenericAboutToStartOrSubmitCallbackResponse<FinremCaseData> response(FinremCaseData finremCaseData,
                                                                                   List<String> warnings, List<String> errors,
                                                                                   String postState) {
        var builder = GenericAboutToStartOrSubmitCallbackResponse.<FinremCaseData>builder();
        builder.data(finremCaseData);
        ofNullable(errors)
            .filter(e -> !e.isEmpty())
            .ifPresent(builder::errors);
        ofNullable(warnings)
            .filter(w -> !w.isEmpty())
            .ifPresent(builder::warnings);
        ofNullable(postState)
            .filter(StringUtils::isNotBlank)
            .ifPresent(builder::state);
        return builder.build();
    }

    protected String toConfirmationHeader(String messages) {
        return "# %s".formatted(messages);
    }

    protected String toConfirmationBody(String... messages) {
        StringBuilder body = new StringBuilder("<ul>");

        if (nonNull(messages)) {
            for (String error : messages) {
                if (error != null && !error.isBlank()) {
                    body.append("<li><h2>%s</h2></li>".formatted(StringEscapeUtils.escapeHtml4(error)));
                }
            }
        }

        body.append("</ul>");
        return body.toString();
    }

    private void sanitise(Map<String, Object> toBeSanitisedMap, Bin bin) {
        if (isNull(toBeSanitisedMap)) {
            return;
        }

        Map<String, Object> nonTemporaryDataMap = getNonTemporaryDataMap(toBeSanitisedMap);

        getClassesWithTemporaryFieldAnnotation().forEach(clazz ->
            getFieldsListWithAnnotation(clazz, TemporaryField.class)
                .forEach(field -> {
                    Object value = toBeSanitisedMap.remove(field.getName());

                    if (CaseDocument.class.isAssignableFrom(field.getType())
                        && value != null
                        && !nonTemporaryDataMap.containsValue(value)) {

                        binCaseDocumentIfTemporaryField(bin, (CaseDocument) value);
                    }
                })
        );
    }

    private void binCaseDocumentIfTemporaryField(Bin bin, CaseDocument caseDocument) {
        if (nonNull(caseDocument)) {
            bin.binCaseDocument(caseDocument);
        }
    }

    private FinremCallbackRequest mapToFinremCallbackRequest(CallbackRequest callbackRequest) {
        FinremCaseDetails finremCaseDetails = finremCaseDetailsMapper.mapToFinremCaseDetails(callbackRequest.getCaseDetails());
        FinremCaseDetails finremCaseDetailsBefore = null;
        String caseId = finremCaseDetails.getCaseIdAsString();
        if (nonNull(callbackRequest.getCaseDetailsBefore())) {
            finremCaseDetailsBefore = finremCaseDetailsMapper.mapToFinremCaseDetails(callbackRequest.getCaseDetailsBefore());
            if (nonNull(finremCaseDetailsBefore.getData())
                && isNull(finremCaseDetailsBefore.getData().getCcdCaseId())) {
                finremCaseDetailsBefore.getData().setCcdCaseId(caseId);
            }
        }
        finremCaseDetails.getData().setCcdCaseId(caseId);

        return FinremCallbackRequest.builder()
            .caseDetails(finremCaseDetails)
            .caseDetailsBefore(finremCaseDetailsBefore)
            .eventType(EventType.getEventType(callbackRequest.getEventId()))
            .build();
    }

    private Map<String, Object> getNonTemporaryDataMap(Map<String, Object> toBeSanitisedMap) {
        return toBeSanitisedMap.entrySet().stream()
            .filter(entry -> !getTemporaryFieldNamesWithCaseDocumentType().contains(entry.getKey()))
            .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
    }

    private static Set<String> getTemporaryFieldNamesWithCaseDocumentType() {
        return getClassesWithTemporaryFieldAnnotation().stream()
            .flatMap(clazz -> getFieldsListWithAnnotation(clazz, TemporaryField.class).stream())
            .filter(field -> CaseDocument.class.isAssignableFrom(field.getType()))
            .map(Field::getName)
            .collect(Collectors.toSet());
    }

    /**
     * Returns the list of classes that contain fields annotated with {@link TemporaryField}
     * and should have those temporary fields cleared during sanitisation.
     *
     * <p><strong>Developer note:</strong> If you introduce a new class that uses
     * {@code @TemporaryField}, you must add it to this list so that its temporary
     * fields are removed correctly. In addition, make sure shouldClearTemporaryFields returns true</p>
     *
     * @return a list of classes containing {@code @TemporaryField}-annotated fields
     */
    private static List<Class<?>> getClassesWithTemporaryFieldAnnotation() {
        return List.of(
            StopRepresentationWrapper.class,
            CaseDataMetricsWrapper.class,
            ContactDetailsWrapper.class,
            ManageCaseDocumentsWrapper.class,
            GeneralApplicationWrapper.class
        );
    }
}
