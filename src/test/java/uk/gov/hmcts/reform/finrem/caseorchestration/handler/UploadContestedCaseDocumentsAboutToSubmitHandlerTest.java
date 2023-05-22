package uk.gov.hmcts.reform.finrem.caseorchestration.handler;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.hmcts.reform.ccd.client.model.CallbackRequest;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.ccd.callback.CallbackType;
import uk.gov.hmcts.reform.finrem.caseorchestration.controllers.GenericAboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.finrem.caseorchestration.helper.UploadedDocumentHelper;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.EventType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.ContestedUploadedDocumentData;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.AssignCaseAccessService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.FeatureToggleService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.casedocuments.CaseDocumentHandlerTest;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.casedocuments.applicant.ApplicantCaseSummariesHandler;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.casedocuments.applicant.ApplicantChronologiesStatementHandler;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.CASE_LEVEL_ROLE;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.CONTESTED_UPLOADED_DOCUMENTS;

@RunWith(MockitoJUnitRunner.class)
public class UploadContestedCaseDocumentsAboutToSubmitHandlerTest extends CaseDocumentHandlerTest {

    public static final String AUTH_TOKEN = "tokien:)";

    @Mock
    ApplicantCaseSummariesHandler applicantCaseSummariesHandler;

    @Mock
    ApplicantChronologiesStatementHandler applicantChronologiesStatementHandler;
    @Mock
    private AssignCaseAccessService accessService;

    @Mock
    FeatureToggleService featureToggleService;

    ObjectMapper objectMapper = new ObjectMapper().registerModule(new JavaTimeModule());
    private UploadContestedCaseDocumentsAboutToSubmitHandler uploadContestedCaseDocumentsHandler;

    private final List<ContestedUploadedDocumentData> uploadDocumentList = new ArrayList<>();
    private final List<ContestedUploadedDocumentData> existingDocumentList = new ArrayList<>();
    private final List<String> expectedDocumentIdList = new ArrayList<>();
    List<ContestedUploadedDocumentData> handledDocumentList = new ArrayList<>();
    List<String> handledDocumentIdList = new ArrayList<>();

    private final UploadedDocumentHelper uploadedDocumentHelper = new UploadedDocumentHelper(objectMapper);

    @Before
    public void setUpTest() {
        when(featureToggleService.isManageBundleEnabled()).thenReturn(false);
        uploadContestedCaseDocumentsHandler = new UploadContestedCaseDocumentsAboutToSubmitHandler(featureToggleService,
            Arrays.asList(applicantCaseSummariesHandler, applicantChronologiesStatementHandler),
            objectMapper, uploadedDocumentHelper, accessService);
    }

    @Test
    public void givenACcdCallbackContestedCase_WhenAnAboutToSubmitEventUploadCaseDocument_thenHandlerCanHandle() {
        assertThat(uploadContestedCaseDocumentsHandler
                .canHandle(CallbackType.ABOUT_TO_SUBMIT, CaseType.CONTESTED, EventType.UPLOAD_CASE_FILES),
            is(true));
    }

    @Test
    public void givenACcdCallbackConsentedCase_WhenAnAboutToSubmitEventUploadCaseDocument_thenHandlerCanNotHandle() {
        assertThat(uploadContestedCaseDocumentsHandler
                .canHandle(CallbackType.ABOUT_TO_SUBMIT, CaseType.CONSENTED, EventType.UPLOAD_CASE_FILES),
            is(false));
    }


    @Test
    public void givenUploadCaseDocument_When_IsValid_ThenExecuteHandlers() {
        CallbackRequest callbackRequest = buildCallbackRequest();
        CaseDetails caseDetails = callbackRequest.getCaseDetails();

        when(accessService.getActiveUserCaseRole(caseDetails.getId().toString(), AUTH_TOKEN)).thenReturn("[APPSOLICITOR]");

        uploadDocumentList.add(createContestedUploadDocumentItem("Other", "", "yes", "no", "Other Example"));
        caseDetails.getData().put(CONTESTED_UPLOADED_DOCUMENTS, uploadDocumentList);
        CaseDetails caseDetailsBefore = callbackRequest.getCaseDetailsBefore();
        caseDetailsBefore.getData().put(CONTESTED_UPLOADED_DOCUMENTS, uploadDocumentList);
        uploadContestedCaseDocumentsHandler.handle(callbackRequest, AUTH_TOKEN);

        List<ContestedUploadedDocumentData> uploadedDocumentPost
            = convertToUploadDocList(caseDetails.getData().get(CONTESTED_UPLOADED_DOCUMENTS));

        verify(applicantCaseSummariesHandler).handle(uploadedDocumentPost, caseDetails.getData());
        verify(applicantChronologiesStatementHandler).handle(uploadedDocumentPost, caseDetails.getData());
    }

    @Test
    public void givenUploadCaseDocument_whenDocIsValidAndUploadedByCaseWorkerAndPartyChoosenApplicant_thenExecuteHandlers() {
        List<String> roles = List.of(CASE_LEVEL_ROLE);

        for (String activeRole : roles) {
            CallbackRequest callbackRequest = buildCallbackRequest();
            CaseDetails caseDetails = callbackRequest.getCaseDetails();
            when(accessService.getActiveUserCaseRole(caseDetails.getId().toString(), AUTH_TOKEN)).thenReturn(activeRole);
            uploadDocumentList.add(createContestedUploadDocumentItem("Other", "applicant", "yes", "no", "Other Example"));
            caseDetails.getData().put(CONTESTED_UPLOADED_DOCUMENTS, uploadDocumentList);
            CaseDetails caseDetailsBefore = callbackRequest.getCaseDetailsBefore();
            caseDetailsBefore.getData().put(CONTESTED_UPLOADED_DOCUMENTS, uploadDocumentList);
            uploadContestedCaseDocumentsHandler.handle(callbackRequest, AUTH_TOKEN);

            List<ContestedUploadedDocumentData> uploadedDocumentPost
                = convertToUploadDocList(caseDetails.getData().get(CONTESTED_UPLOADED_DOCUMENTS));

            verify(applicantCaseSummariesHandler).handle(uploadedDocumentPost, caseDetails.getData());
            verify(applicantChronologiesStatementHandler).handle(uploadedDocumentPost, caseDetails.getData());
        }
    }

    @Test
    public void givenUploadCaseDocument_whenDocIsValidAndUploadedByInterveners_thenExecuteHandlers() {
        List<String> roles = List.of("[INTVRSOLICITOR1]", "[INTVRSOLICITOR2]", "[INTVRSOLICITOR3]", "[INTVRSOLICITOR4]",
            "[[INTVRBARRISTER1]]", "[[INTVRBARRISTER2]]", "[[INTVRBARRISTER3]]", "[[INTVRBARRISTER4]]", "[RESPSOLICITOR]", "");

        for (String activeRole : roles) {
            CallbackRequest callbackRequest = buildCallbackRequest();
            CaseDetails caseDetails = callbackRequest.getCaseDetails();
            when(accessService.getActiveUserCaseRole(caseDetails.getId().toString(), AUTH_TOKEN)).thenReturn(activeRole);
            uploadDocumentList.add(createContestedUploadDocumentItem("Other", "", "yes", "no", "Other Example"));
            caseDetails.getData().put(CONTESTED_UPLOADED_DOCUMENTS, uploadDocumentList);
            CaseDetails caseDetailsBefore = callbackRequest.getCaseDetailsBefore();
            caseDetailsBefore.getData().put(CONTESTED_UPLOADED_DOCUMENTS, uploadDocumentList);
            uploadContestedCaseDocumentsHandler.handle(callbackRequest, AUTH_TOKEN);

            List<ContestedUploadedDocumentData> uploadedDocumentPost
                = convertToUploadDocList(caseDetails.getData().get(CONTESTED_UPLOADED_DOCUMENTS));

            verify(applicantCaseSummariesHandler).handle(uploadedDocumentPost, caseDetails.getData());
            verify(applicantChronologiesStatementHandler).handle(uploadedDocumentPost, caseDetails.getData());
        }
    }

    @Test
    public void givenUploadCaseDocument_When_IsValid_ThenExecuteHandler_And_ValidateDocumentOrder() {
        CallbackRequest callbackRequest = buildCallbackRequest();
        when(accessService.getActiveUserCaseRole(caseDetails.getId().toString(), AUTH_TOKEN)).thenReturn("[APPSOLICITOR]");
        CaseDetails caseDetailsBefore = callbackRequest.getCaseDetailsBefore();
        ContestedUploadedDocumentData oldDoc = createContestedUploadDocumentItem("Other", "", "yes", "no", "Old Document Example");
        existingDocumentList.add(oldDoc);
        caseDetailsBefore.getData().put(CONTESTED_UPLOADED_DOCUMENTS, existingDocumentList);

        CaseDetails caseDetails = callbackRequest.getCaseDetails();
        ContestedUploadedDocumentData newDoc = createContestedUploadDocumentItem("Other", "", "yes", "no", "New Document Example");
        uploadDocumentList.addAll(List.of(newDoc, oldDoc));
        caseDetails.getData().put(CONTESTED_UPLOADED_DOCUMENTS, uploadDocumentList);

        expectedDocumentIdList.add(newDoc.getId());
        expectedDocumentIdList.add(oldDoc.getId());

        handledDocumentList.addAll(
            (List<ContestedUploadedDocumentData>) uploadContestedCaseDocumentsHandler.handle(
                callbackRequest, AUTH_TOKEN).getData().get(CONTESTED_UPLOADED_DOCUMENTS));

        handledDocumentList.forEach(doc -> handledDocumentIdList.add(doc.getId()));

        assertThat(handledDocumentIdList.equals(expectedDocumentIdList), is(true));
    }

    @Test
    public void givenUploadFileTrialBundleSelectedWhenAboutToSubmitThenShowTrialBundleDeprecatedErrorMessage() {

        when(featureToggleService.isManageBundleEnabled()).thenReturn(true);

        uploadDocumentList.add(createContestedUploadDocumentItem("Trial Bundle", "", "yes", "no","Other Example"));
        CallbackRequest callbackRequest = buildCallbackRequest();
        callbackRequest.getCaseDetails().getData().put(CONTESTED_UPLOADED_DOCUMENTS, uploadDocumentList);

        GenericAboutToStartOrSubmitCallbackResponse<Map<String, Object>>
            response = uploadContestedCaseDocumentsHandler.handle(callbackRequest, AUTH_TOKEN);

        assertThat(response.getErrors().size(), is(1));
        assertThat(response.getErrors().iterator().next(), is(UploadContestedCaseDocumentsAboutToSubmitHandler.TRIAL_BUNDLE_SELECTED_ERROR));
    }

    @Test
    public void givenUploadFileWithoutTrialBundleWhenAboutToSubmitThenNoErrors() {

        when(featureToggleService.isManageBundleEnabled()).thenReturn(true);
        when(accessService.getActiveUserCaseRole(caseDetails.getId().toString(), AUTH_TOKEN)).thenReturn("[APPSOLICITOR]");
        uploadDocumentList.add(createContestedUploadDocumentItem("Letter from Applicant", "", "yes", "no","Other Example"));
        CallbackRequest callbackRequest = buildCallbackRequest();
        callbackRequest.getCaseDetails().getData().put(CONTESTED_UPLOADED_DOCUMENTS, uploadDocumentList);

        GenericAboutToStartOrSubmitCallbackResponse<Map<String, Object>>
            response = uploadContestedCaseDocumentsHandler.handle(callbackRequest, AUTH_TOKEN);

        assertThat(response.getErrors().size(), is(0));
    }

    private CallbackRequest buildCallbackRequest() {
        Map<String, Object> caseData = new HashMap<>();
        Map<String, Object> caseDataBefore = new HashMap<>();
        CaseDetails caseDetails = CaseDetails.builder().id(123L).build();
        caseDetails.setData(caseData);
        CaseDetails caseDetailsBefore = CaseDetails.builder().id(123L).build();
        caseDetailsBefore.setData(caseDataBefore);
        return CallbackRequest.builder().eventId(EventType.UPLOAD_CASE_FILES.getCcdType())
            .caseDetails(caseDetails).caseDetailsBefore(caseDetailsBefore).build();
    }

    public List<ContestedUploadedDocumentData> convertToUploadDocList(Object object) {
        return objectMapper.convertValue(object, new TypeReference<>() {
        });
    }
}