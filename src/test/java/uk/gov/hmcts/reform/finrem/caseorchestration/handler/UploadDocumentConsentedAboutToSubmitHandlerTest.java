package uk.gov.hmcts.reform.finrem.caseorchestration.handler;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import uk.gov.hmcts.reform.finrem.caseorchestration.ccd.callback.CallbackType;
import uk.gov.hmcts.reform.finrem.caseorchestration.mapper.FinremCaseDetailsMapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.EventType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseType;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.DocumentUploadServiceV2;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.documentchecker.DocumentCheckerService;
import uk.gov.hmcts.reform.finrem.caseorchestration.util.TestLogger;
import uk.gov.hmcts.reform.finrem.caseorchestration.util.TestLogs;

@ExtendWith(SpringExtension.class)
class UploadDocumentConsentedAboutToSubmitHandlerTest {

    @TestLogs
    private final TestLogger logs = new TestLogger(UploadDocumentConsentedAboutToSubmitHandler.class);

    @Mock
    private DocumentCheckerService documentCheckerService;

    @Mock
    private DocumentUploadServiceV2 documentUploadService;

    private UploadDocumentConsentedAboutToSubmitHandler underTest;

    @BeforeEach
    public void setUpTest() {
        FinremCaseDetailsMapper finremCaseDetailsMapper = new FinremCaseDetailsMapper(new ObjectMapper().registerModule(new JavaTimeModule()));
        underTest = new UploadDocumentConsentedAboutToSubmitHandler(finremCaseDetailsMapper, documentCheckerService, documentUploadService);
    }

    @Test
    void givenACcdCallbackContestedCase_WhenAnAboutToSubmitEventUploadDocumentConsented_thenHandlerCanHandleReturnTrue() {
        Assertions.assertThat(underTest.canHandle(CallbackType.ABOUT_TO_SUBMIT, CaseType.CONSENTED, EventType.UPLOAD_DOCUMENT_CONSENTED)).isTrue();
    }

    @Test
    void givenACcdCallbackConsentedCase_WhenAnAboutToSubmitEventUploadDocumentConsented_thenHandlerCanHandleReturnFalse() {
        Assertions.assertThat(underTest.canHandle(CallbackType.ABOUT_TO_SUBMIT, CaseType.CONTESTED, EventType.UPLOAD_DOCUMENT_CONSENTED)).isFalse();
    }
}
