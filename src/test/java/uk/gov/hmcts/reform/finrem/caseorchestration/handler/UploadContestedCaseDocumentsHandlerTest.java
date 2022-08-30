package uk.gov.hmcts.reform.finrem.caseorchestration.handler;

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
import uk.gov.hmcts.reform.finrem.caseorchestration.helper.UploadedDocumentHelper;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.CaseType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.EventType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.ContestedUploadedDocumentData;
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
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.CONTESTED_UPLOADED_DOCUMENTS;

@RunWith(MockitoJUnitRunner.class)
public class UploadContestedCaseDocumentsHandlerTest extends CaseDocumentHandlerTest {

    public static final String AUTH_TOKEN = "tokien:)";

    @Mock
    ApplicantCaseSummariesHandler applicantCaseSummariesHandler;

    @Mock
    ApplicantChronologiesStatementHandler applicantChronologiesStatementHandler;

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
        uploadContestedCaseDocumentsHandler = new UploadContestedCaseDocumentsAboutToSubmitHandler(
            Arrays.asList(applicantCaseSummariesHandler, applicantChronologiesStatementHandler), objectMapper, uploadedDocumentHelper);
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
        CaseDetails caseDetailsBefore = callbackRequest.getCaseDetailsBefore();
        uploadDocumentList.add(createContestedUploadDocumentItem("Other", "applicant", "yes", "no", "Other Example"));
        caseDetails.getData().put(CONTESTED_UPLOADED_DOCUMENTS, uploadDocumentList);
        caseDetailsBefore.getData().put(CONTESTED_UPLOADED_DOCUMENTS, uploadDocumentList);
        uploadContestedCaseDocumentsHandler.handle(callbackRequest, AUTH_TOKEN);


        verify(applicantCaseSummariesHandler).handle(uploadDocumentList, caseDetails.getData());
        verify(applicantChronologiesStatementHandler).handle(uploadDocumentList, caseDetails.getData());
    }

    @Test
    public void givenUploadCaseDocument_When_IsValid_ThenExecuteHandler_And_ValidateDocumentOrder() {
        CallbackRequest callbackRequest = buildCallbackRequest();

        CaseDetails caseDetailsBefore = callbackRequest.getCaseDetailsBefore();
        ContestedUploadedDocumentData oldDoc = createContestedUploadDocumentItem("Other", "applicant", "yes", "no", "Old Document Example");
        existingDocumentList.add(oldDoc);
        caseDetailsBefore.getData().put(CONTESTED_UPLOADED_DOCUMENTS, existingDocumentList);

        CaseDetails caseDetails = callbackRequest.getCaseDetails();
        ContestedUploadedDocumentData newDoc = createContestedUploadDocumentItem("Other", "applicant", "yes", "no", "New Document Example");
        uploadDocumentList.add(newDoc);
        caseDetails.getData().put(CONTESTED_UPLOADED_DOCUMENTS, uploadDocumentList);

        expectedDocumentIdList.add(newDoc.getId());
        expectedDocumentIdList.add(oldDoc.getId());

        handledDocumentList.addAll(
            (List<ContestedUploadedDocumentData>) uploadContestedCaseDocumentsHandler.handle(
                callbackRequest, AUTH_TOKEN).getData().get(CONTESTED_UPLOADED_DOCUMENTS));

        handledDocumentList.forEach(doc -> handledDocumentIdList.add(doc.getId()));

        assertThat(handledDocumentIdList.equals(expectedDocumentIdList), is(true));
    }

    private CallbackRequest buildCallbackRequest() {
        Map<String, Object> caseData = new HashMap<>();
        Map<String, Object> caseDataBefore = new HashMap<>();
        CaseDetails caseDetails = CaseDetails.builder().id(123L).data(caseData).build();
        CaseDetails caseDetailsBefore = CaseDetails.builder().id(123L).data(caseDataBefore).build();
        return CallbackRequest.builder().eventId(EventType.UPLOAD_CASE_FILES.getCcdType())
            .caseDetails(caseDetails).caseDetailsBefore(caseDetailsBefore).build();
    }
}