package uk.gov.hmcts.reform.finrem.caseorchestration.handler;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import uk.gov.hmcts.reform.ccd.client.model.CallbackRequest;
import uk.gov.hmcts.reform.finrem.caseorchestration.ccd.callback.CallbackType;
import uk.gov.hmcts.reform.finrem.caseorchestration.controllers.GenericAboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.finrem.caseorchestration.helper.UploadedDocumentHelper;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.EventType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.ContestedUploadedDocument;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.ContestedUploadedDocumentData;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.FeatureToggleService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.casedocuments.CaseDocumentHandler;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.CONTESTED_UPLOADED_DOCUMENTS;

@Slf4j
@Service
@RequiredArgsConstructor
public class UploadContestedCaseDocumentsAboutToSubmitHandler
    implements CallbackHandler<Map<String, Object>> {

    public static final String TRIAL_BUNDLE_SELECTED_ERROR =
        "To upload a hearing bundle please use the Manage hearing "
            + "bundles event which can be found on the drop-down list on the home page";
    public static final String TRIAL_BUNDLE_TYPE = "Trial Bundle";

    private final FeatureToggleService featureToggleService;
    private final List<CaseDocumentHandler> caseDocumentHandlers;
    private final ObjectMapper objectMapper;
    private final UploadedDocumentHelper uploadedDocumentHelper;

    @Override
    public boolean canHandle(CallbackType callbackType, CaseType caseType, EventType eventType) {
        return CallbackType.ABOUT_TO_SUBMIT.equals(callbackType)
            && CaseType.CONTESTED.equals(caseType)
            && EventType.UPLOAD_CASE_FILES.equals(eventType);
    }

    @Override
    public GenericAboutToStartOrSubmitCallbackResponse<Map<String, Object>> handle(CallbackRequest callbackRequest,
                                                                                   String userAuthorisation) {

        GenericAboutToStartOrSubmitCallbackResponse<Map<String, Object>>
            response = validateUploadedDocuments(callbackRequest.getCaseDetails().getData());
        if (!CollectionUtils.isEmpty(response.getErrors())) {
            return response;
        }

        Map<String, Object> caseData = uploadedDocumentHelper.addUploadDateToNewDocuments(
            callbackRequest.getCaseDetails().getData(),
            callbackRequest.getCaseDetailsBefore().getData(), CONTESTED_UPLOADED_DOCUMENTS);

        List<ContestedUploadedDocumentData> uploadedDocuments = (List<ContestedUploadedDocumentData>) caseData.get(CONTESTED_UPLOADED_DOCUMENTS);
        caseDocumentHandlers.stream().forEach(h -> h.handle(uploadedDocuments, caseData));
        uploadedDocuments.sort(Comparator.comparing(
            ContestedUploadedDocumentData::getUploadedCaseDocument, Comparator.comparing(
                ContestedUploadedDocument::getCaseDocumentUploadDateTime, Comparator.nullsLast(
                    Comparator.reverseOrder()))));
        caseData.put(CONTESTED_UPLOADED_DOCUMENTS, uploadedDocuments);
        return response;
    }

    private GenericAboutToStartOrSubmitCallbackResponse<Map<String, Object>> validateUploadedDocuments(
        Map<String, Object> caseData) {
        GenericAboutToStartOrSubmitCallbackResponse<Map<String, Object>> response = getCallBackResponse(caseData);
        setWarningsAndErrors(caseData, response);
        return response;
    }

    private List<ContestedUploadedDocumentData> getDocumentCollection(Map<String, Object> caseData) {
        objectMapper.registerModule(new JavaTimeModule());
        List<ContestedUploadedDocumentData> contestedUploadDocuments = objectMapper.convertValue(
            caseData.get(CONTESTED_UPLOADED_DOCUMENTS), new TypeReference<>() {
            });

        return Optional.ofNullable(contestedUploadDocuments).orElse(new ArrayList<>());
    }

    private GenericAboutToStartOrSubmitCallbackResponse<Map<String, Object>> getCallBackResponse(Map<String, Object> caseData) {
        return GenericAboutToStartOrSubmitCallbackResponse.<Map<String, Object>>builder()
            .data(caseData)
            .errors(new ArrayList<>())
            .warnings(new ArrayList<>())
            .build();
    }

    private void setWarningsAndErrors(Map<String, Object> caseData, GenericAboutToStartOrSubmitCallbackResponse<Map<String, Object>> response) {
        if (featureToggleService.isManageBundleEnabled()
            && isTrialBundleSelectedInAnyUploadedFile(caseData)) {
            response.getErrors().add(TRIAL_BUNDLE_SELECTED_ERROR);
        }

    }

    private boolean isTrialBundleSelectedInAnyUploadedFile(Map<String, Object> caseData) {
        return !getTrialBundleUploadedList(getDocumentCollection(caseData)).isEmpty();
    }

    private List<ContestedUploadedDocumentData> getTrialBundleUploadedList(List<ContestedUploadedDocumentData> uploadedDocuments) {

        return uploadedDocuments.stream()
            .filter(d -> isTrialBundle(d.getUploadedCaseDocument()))
            .collect(Collectors.toList());
    }

    private boolean isTrialBundle(ContestedUploadedDocument uploadedCaseDocument) {
        return Optional.ofNullable(uploadedCaseDocument)
            .map(ContestedUploadedDocument::getCaseDocumentType)
            .filter(type -> type.equals(TRIAL_BUNDLE_TYPE))
            .isPresent();
    }

}
