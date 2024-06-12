package uk.gov.hmcts.reform.finrem.caseorchestration.handler;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.hmcts.reform.finrem.caseorchestration.ccd.callback.CallbackType;
import uk.gov.hmcts.reform.finrem.caseorchestration.mapper.FinremCaseDetailsMapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.EventType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseType;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.DocumentUploadServiceV2;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.documentchecker.DocumentCheckerService;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

@RunWith(MockitoJUnitRunner.class)
public class UploadDocumentConsentedAboutToSubmitHandlerTest {

    public static final String AUTH_TOKEN = "token:)";
    public static final String CASE_ID = "1234567890";

    ObjectMapper objectMapper = new ObjectMapper().registerModule(new JavaTimeModule());
    @Mock
    private DocumentCheckerService documentCheckerService;

    private UploadDocumentConsentedAboutToSubmitHandler uploadDocumentConsentedAboutToSubmitHandler;

    @Before
    public void setUpTest() {
        FinremCaseDetailsMapper finremCaseDetailsMapper =
            new FinremCaseDetailsMapper(objectMapper);
        uploadDocumentConsentedAboutToSubmitHandler =
            new UploadDocumentConsentedAboutToSubmitHandler(finremCaseDetailsMapper, documentCheckerService,
                new DocumentUploadServiceV2());
    }

    @Test
    public void givenACcdCallbackContestedCase_WhenAnAboutToSubmitEventUploadGeneralDocument_thenHandlerCanHandle() {
        assertThat(uploadDocumentConsentedAboutToSubmitHandler
                .canHandle(CallbackType.ABOUT_TO_SUBMIT, CaseType.CONTESTED, EventType.UPLOAD_DOCUMENT_CONSENTED),
            is(false));
    }

    @Test
    public void givenACcdCallbackConsentedCase_WhenAnAboutToSubmitEventUploadGeneralDocument_thenHandlerCanHandle() {
        assertThat(uploadDocumentConsentedAboutToSubmitHandler
                .canHandle(CallbackType.ABOUT_TO_SUBMIT, CaseType.CONSENTED, EventType.UPLOAD_DOCUMENT_CONSENTED),
            is(true));
    }
}