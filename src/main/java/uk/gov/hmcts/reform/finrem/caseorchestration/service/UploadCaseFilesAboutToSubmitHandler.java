package uk.gov.hmcts.reform.finrem.caseorchestration.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.finrem.caseorchestration.helper.ContestedUploadDocumentsHelper;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.ContestedUploadedDocument;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.ContestedUploadedDocumentData;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import static org.apache.commons.lang3.ObjectUtils.isNotEmpty;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.CONTESTED_UPLOADED_DOCUMENTS;


@Slf4j
@Service
@RequiredArgsConstructor
public class UploadCaseFilesAboutToSubmitHandler {

    public static final String TRIAL_BUNDLE_SELECTED_ERROR =
        "To upload a hearing bundle please use the Manage hearing "
            + "bundles event which can be found on the drop-down list on the home page";

    private final ContestedUploadDocumentsHelper contestedUploadDocumentsHelper;

    private static final String APPLICANT = "applicant";
    private static final String RESPONDENT = "respondent";
    public static final String TRIAL_BUNDLE_TYPE = "Trial Bundle";

    private final ObjectMapper mapper;
    private final FeatureToggleService featureToggleService;

    public AboutToStartOrSubmitCallbackResponse handle(Map<String, Object> caseData) {

        AboutToStartOrSubmitCallbackResponse response = getCallBackResponse(caseData);

        setWarningsAndErrors(caseData, response);
        if (isNotEmpty(response.getErrors())) {
            return response;
        }

        contestedUploadDocumentsHelper.setUploadedDocumentsToCollections(caseData, CONTESTED_UPLOADED_DOCUMENTS);
        response.setData(caseData);
        return response;
    }

    private void setWarningsAndErrors(Map<String, Object> caseData, AboutToStartOrSubmitCallbackResponse response) {
        if (featureToggleService.isManageBundleEnabled()
            && isTrialBundleSelectedInAnyUploadedFile(caseData)) {
            response.getErrors().add(TRIAL_BUNDLE_SELECTED_ERROR);
        }
    }

    private AboutToStartOrSubmitCallbackResponse getCallBackResponse(Map<String, Object> caseData) {
        return AboutToStartOrSubmitCallbackResponse
            .builder()
            .data(caseData)
            .errors(new ArrayList<>())
            .warnings(new ArrayList<>())
            .build();
    }

    private boolean isTrialBundleSelectedInAnyUploadedFile(Map<String, Object> caseData) {
        return !getTrialBundleUploadedList(getDocumentCollection(caseData, CONTESTED_UPLOADED_DOCUMENTS)).isEmpty();
    }

    private List<ContestedUploadedDocumentData> getDocumentCollection(Map<String, Object> caseData, String collection) {

        if (StringUtils.isEmpty(caseData.get(collection))) {
            return new ArrayList<>();
        }

        return mapper.convertValue(caseData.get(collection), new TypeReference<>() {});
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
