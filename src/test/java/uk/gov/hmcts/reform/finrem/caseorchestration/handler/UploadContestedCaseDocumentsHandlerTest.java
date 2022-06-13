package uk.gov.hmcts.reform.finrem.caseorchestration.handler;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.hmcts.reform.ccd.client.model.CallbackRequest;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.ccd.callback.CallbackType;
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

    ObjectMapper objectMapper = new ObjectMapper();
    private UploadContestedCaseDocumentsHandler uploadContestedCaseDocumentsHandler;

    private final List<ContestedUploadedDocumentData> uploadDocumentList = new ArrayList<>();

    @Before
    public void setUpTest() {
        uploadContestedCaseDocumentsHandler = new UploadContestedCaseDocumentsHandler(
            Arrays.asList(applicantCaseSummariesHandler, applicantChronologiesStatementHandler), objectMapper);
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
        uploadDocumentList.add(createContestedUploadDocumentItem("Other", "applicant", "yes", "no", "Other Example"));
        caseDetails.getData().put(CONTESTED_UPLOADED_DOCUMENTS, uploadDocumentList);
        uploadContestedCaseDocumentsHandler.handle(callbackRequest, AUTH_TOKEN);


        verify(applicantCaseSummariesHandler).handle(uploadDocumentList, caseDetails.getData());
        verify(applicantChronologiesStatementHandler).handle(uploadDocumentList, caseDetails.getData());
    }

    private CallbackRequest buildCallbackRequest() {
        Map<String, Object> caseData = new HashMap<>();
        CaseDetails caseDetails = CaseDetails.builder().id(123L).data(caseData).build();
        return CallbackRequest.builder().eventId(EventType.UPLOAD_CASE_FILES.getCcdType()).caseDetails(caseDetails).build();
    }
}