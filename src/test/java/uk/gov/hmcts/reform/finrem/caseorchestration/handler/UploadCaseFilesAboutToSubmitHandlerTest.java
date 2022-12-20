package uk.gov.hmcts.reform.finrem.caseorchestration.handler;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.hmcts.reform.ccd.client.model.CallbackRequest;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.ccd.callback.CallbackType;
import uk.gov.hmcts.reform.finrem.caseorchestration.controllers.GenericAboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.EventType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseDocument;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.ContestedUploadedDocument;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.ContestedUploadedDocumentData;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.FeatureToggleService;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.CONTESTED_UPLOADED_DOCUMENTS;

@RunWith(MockitoJUnitRunner.class)
public class UploadCaseFilesAboutToSubmitHandlerTest {

    public static final String AUTH_TOKEN = "tokien:)";

    public static final String CASE_ID = "1234567890";
    private FeatureToggleService featureToggleService;
    private ObjectMapper mapper;

    private final List<ContestedUploadedDocumentData> uploadDocumentList = new ArrayList<>();
    private UploadCaseFilesAboutToSubmitHandler uploadCaseFilesAboutToSubmitHandler;

    private CallbackRequest callbackRequest;

    @Before
    public void setUp() {
        mapper = new ObjectMapper();
        featureToggleService = mock(FeatureToggleService.class);
        uploadCaseFilesAboutToSubmitHandler = new UploadCaseFilesAboutToSubmitHandler(featureToggleService, mapper);
        when(featureToggleService.isManageBundleEnabled()).thenReturn(false);
        Map<String, Object> caseData = new HashMap<>();
        Map<String, Object> caseDataBefore = new HashMap<>();
        callbackRequest = CallbackRequest.builder()
            .caseDetails(CaseDetails.builder().id(Long.parseLong(CASE_ID)).data(caseData).build())
            .caseDetailsBefore(CaseDetails.builder().id(Long.parseLong(CASE_ID)).data(caseDataBefore).build())
            .build();
    }

    @Test
    public void givenHandlerCanHandleCallback_whenCanHandle_thenReturnTrue() {
        assertThat(uploadCaseFilesAboutToSubmitHandler.canHandle(
                CallbackType.ABOUT_TO_SUBMIT,
                CaseType.CONTESTED,
                EventType.UPLOAD_CASE_FILES),
            is(true));
    }

    @Test
    public void givenInvalidCallbackType_whenCanHandle_thenReturnFalse() {
        assertThat(uploadCaseFilesAboutToSubmitHandler.canHandle(
                CallbackType.SUBMITTED,
                CaseType.CONTESTED,
                EventType.UPLOAD_CASE_FILES),
            is(false));
    }

    @Test
    public void givenInvalidCaseType_whenCanHandle_thenReturnFalse() {
        assertThat(uploadCaseFilesAboutToSubmitHandler.canHandle(
                CallbackType.ABOUT_TO_SUBMIT,
                CaseType.CONSENTED,
                EventType.UPLOAD_CASE_FILES),
            is(false));
    }

    @Test
    public void givenInvalidEventType_whenCanHandle_thenReturnFalse() {
        assertThat(uploadCaseFilesAboutToSubmitHandler.canHandle(
                CallbackType.ABOUT_TO_SUBMIT,
                CaseType.CONTESTED,
                EventType.UPLOAD_APPROVED_ORDER),
            is(false));
    }

    @Test
    public void givenUploadFileTrialBundleSelectedWhenAboutToSubmitThenShowTrialBundleDeprecatedErrorMessage() {
        when(featureToggleService.isManageBundleEnabled()).thenReturn(true);

        uploadDocumentList.add(createContestedUploadDocumentItem("Trial Bundle", "applicant", "yes", "Other Example"));
        uploadDocumentList.add(createContestedUploadDocumentItem("Other", "respondent", "yes", "Other Example"));
        callbackRequest.getCaseDetails().getData().put(CONTESTED_UPLOADED_DOCUMENTS, uploadDocumentList);

        GenericAboutToStartOrSubmitCallbackResponse<Map<String, Object>>
            response = uploadCaseFilesAboutToSubmitHandler.handle(callbackRequest, AUTH_TOKEN);

        assertThat(response.getErrors().size(), is(1));
        assertThat(response.getErrors().iterator().next(), is(UploadCaseFilesAboutToSubmitHandler.TRIAL_BUNDLE_SELECTED_ERROR));
    }

    @Test
    public void givenUploadFileWithoutTrialBundleWhenAboutToSubmitThenNoErrors() {
        when(featureToggleService.isManageBundleEnabled()).thenReturn(true);

        uploadDocumentList.add(createContestedUploadDocumentItem("Letter from Applicant", "applicant", "yes", "Other Example"));
        uploadDocumentList.add(createContestedUploadDocumentItem("Other", "respondent", "yes", "Other Example"));
        callbackRequest.getCaseDetails().getData().put(CONTESTED_UPLOADED_DOCUMENTS, uploadDocumentList);

        GenericAboutToStartOrSubmitCallbackResponse<Map<String, Object>>
            response = uploadCaseFilesAboutToSubmitHandler.handle(callbackRequest, AUTH_TOKEN);

        assertThat(response.getErrors().size(), is(0));
    }

    @Test
    public void givenNoUploadFileWhenAboutToSubmitThenNoErrors() {
        when(featureToggleService.isManageBundleEnabled()).thenReturn(true);

        callbackRequest.getCaseDetails().getData().put(CONTESTED_UPLOADED_DOCUMENTS, null);

        GenericAboutToStartOrSubmitCallbackResponse<Map<String, Object>>
            response = uploadCaseFilesAboutToSubmitHandler.handle(callbackRequest, AUTH_TOKEN);

        assertThat(response.getErrors().size(), is(0));
    }


    private ContestedUploadedDocumentData createContestedUploadDocumentItem(String type, String party,
                                                                            String isConfidential, String other) {

        return ContestedUploadedDocumentData.builder()
            .uploadedCaseDocument(ContestedUploadedDocument
                .builder()
                .caseDocuments(new CaseDocument())
                .caseDocumentType(type)
                .caseDocumentParty(party)
                .caseDocumentConfidential(isConfidential)
                .caseDocumentOther(other)
                .hearingDetails(null)
                .build())
            .build();
    }
}