package uk.gov.hmcts.reform.finrem.caseorchestration.handler;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.casedocuments.CaseDocumentHandlerTest;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.casedocuments.applicant.ApplicantCaseSummariesHandler;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.casedocuments.applicant.ApplicantChronologiesStatementHandler;
import uk.gov.hmcts.reform.finrem.ccd.callback.CallbackRequest;
import uk.gov.hmcts.reform.finrem.ccd.callback.CallbackType;
import uk.gov.hmcts.reform.finrem.ccd.domain.CaseType;
import uk.gov.hmcts.reform.finrem.ccd.domain.EventType;
import uk.gov.hmcts.reform.finrem.ccd.domain.FinremCaseData;
import uk.gov.hmcts.reform.finrem.ccd.domain.FinremCaseDetails;
import uk.gov.hmcts.reform.finrem.ccd.domain.UploadCaseDocumentCollection;
import uk.gov.hmcts.reform.finrem.ccd.domain.YesOrNo;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.verify;

@RunWith(MockitoJUnitRunner.class)
public class UploadContestedCaseDocumentsHandlerTest extends CaseDocumentHandlerTest {

    public static final String AUTH_TOKEN = "tokien:)";

    @Mock
    ApplicantCaseSummariesHandler applicantCaseSummariesHandler;

    @Mock
    ApplicantChronologiesStatementHandler applicantChronologiesStatementHandler;

    ObjectMapper objectMapper = new ObjectMapper();
    private UploadContestedCaseDocumentsAboutToSubmitHandler uploadContestedCaseDocumentsHandler;

    private final List<UploadCaseDocumentCollection> uploadDocumentList = new ArrayList<>();

    @Before
    public void setUpTest() {
        uploadContestedCaseDocumentsHandler = new UploadContestedCaseDocumentsAboutToSubmitHandler(
            Arrays.asList(applicantCaseSummariesHandler, applicantChronologiesStatementHandler));
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
        FinremCaseDetails caseDetails = callbackRequest.getCaseDetails();
        uploadDocumentList.add(createContestedUploadDocumentItem("Other", "applicant", YesOrNo.YES, YesOrNo.NO, "Other Example"));
        caseDetails.getCaseData().getUploadCaseDocumentWrapper().setUploadCaseDocument(uploadDocumentList);
        uploadContestedCaseDocumentsHandler.handle(callbackRequest, AUTH_TOKEN);


        verify(applicantCaseSummariesHandler).handle(uploadDocumentList, caseDetails.getCaseData());
        verify(applicantChronologiesStatementHandler).handle(uploadDocumentList, caseDetails.getCaseData());
    }

    private CallbackRequest buildCallbackRequest() {
        FinremCaseData caseData = new FinremCaseData();
        FinremCaseDetails caseDetails = FinremCaseDetails.builder().id(123L).caseData(caseData).build();
        return CallbackRequest.builder().eventType(EventType.UPLOAD_CASE_FILES).caseDetails(caseDetails).build();
    }
}