package uk.gov.hmcts.reform.finrem.caseorchestration.handler;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.ccd.client.model.CallbackRequest;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.ccd.callback.CallbackType;
import uk.gov.hmcts.reform.finrem.caseorchestration.controllers.GenericAboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.EventType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.ContestedUploadedDocument;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.ContestedUploadedDocumentData;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.FeatureToggleService;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.CONTESTED_UPLOADED_DOCUMENTS;
@Slf4j
@Service
@RequiredArgsConstructor
public class UploadCaseFilesAboutToSubmitHandler implements CallbackHandler<Map<String, Object>> {
    public static final String TRIAL_BUNDLE_SELECTED_ERROR =
        "To upload a hearing bundle please use the Manage hearing "
            + "bundles event which can be found on the drop-down list on the home page";
    public static final String TRIAL_BUNDLE_TYPE = "Trial Bundle";
    private final FeatureToggleService featureToggleService;
    private final ObjectMapper objectMapper;
    @Override
    public boolean canHandle(CallbackType callbackType, CaseType caseType, EventType eventType) {
        return CallbackType.ABOUT_TO_SUBMIT.equals(callbackType)
            && CaseType.CONTESTED.equals(caseType)
            && EventType.UPLOAD_CASE_FILES.equals(eventType);
    }

    @Override
    public GenericAboutToStartOrSubmitCallbackResponse<Map<String, Object>> handle(CallbackRequest callbackRequest, String userAuthorisation) {
        CaseDetails caseDetails = callbackRequest.getCaseDetails();
        Map<String, Object> caseData = caseDetails.getData();
        GenericAboutToStartOrSubmitCallbackResponse<Map<String, Object>> response = getCallBackResponse(caseData);
        setWarningsAndErrors(caseData, response);
        return response;
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

    private List<ContestedUploadedDocumentData> getDocumentCollection(Map<String, Object> caseData) {
        objectMapper.registerModule(new JavaTimeModule());

        List<ContestedUploadedDocumentData> contestedUploadDocuments = objectMapper.convertValue(
            caseData.get(CONTESTED_UPLOADED_DOCUMENTS), new TypeReference<>() {
            });

        return Optional.ofNullable(contestedUploadDocuments).orElse(new ArrayList<>());
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
