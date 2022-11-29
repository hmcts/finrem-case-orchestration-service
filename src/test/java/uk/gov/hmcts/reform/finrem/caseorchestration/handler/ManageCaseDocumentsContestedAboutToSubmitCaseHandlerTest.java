package uk.gov.hmcts.reform.finrem.caseorchestration.handler;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.hmcts.reform.ccd.client.model.CallbackRequest;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.ccd.callback.CallbackType;
import uk.gov.hmcts.reform.finrem.caseorchestration.helper.UploadedDocumentHelper;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.CaseType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.EventType;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.ManageCaseDocumentsService;

import java.io.InputStream;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static uk.gov.hmcts.reform.finrem.caseorchestration.TestConstants.AUTH_TOKEN;

@RunWith(MockitoJUnitRunner.class)
public class ManageCaseDocumentsContestedAboutToSubmitCaseHandlerTest {

    private final ObjectMapper objectMapper = new ObjectMapper().registerModule(new JavaTimeModule());

    @Mock
    private ManageCaseDocumentsService manageCaseDocumentsService;

    @Mock
    private UploadedDocumentHelper uploadedDocumentHelper = new UploadedDocumentHelper(objectMapper);

    @InjectMocks
    private ManageCaseDocumentsContestedAboutToSubmitCaseHandler manageCaseDocumentsAboutToSubmitCaseHandler;

    @Test
    public void givenACcdCallbackContestedCase_WhenAnAboutToSubmitEventManageCaseDocuments_thenHandlerCanHandle() {
        assertThat(manageCaseDocumentsAboutToSubmitCaseHandler
                .canHandle(CallbackType.ABOUT_TO_SUBMIT, CaseType.CONTESTED, EventType.MANAGE_CASE_DOCUMENTS),
            is(true));
    }

    @Test
    public void givenACcdCallbackConsentedCase_WhenAnAboutToSubmitEventManageCaseDocuments_thenHandlerCanNotHandle() {
        assertThat(manageCaseDocumentsAboutToSubmitCaseHandler
                .canHandle(CallbackType.ABOUT_TO_SUBMIT, CaseType.CONSENTED, EventType.MANAGE_CASE_DOCUMENTS),
            is(false));
    }

    @Test
    public void givenManageCaseDocumentsAboutToSubmitCaseHandlerShouldCallUploadCaseFilesAboutToSubmitHandlerMethod() {
        CallbackRequest callbackRequest =
            CallbackRequest.builder().caseDetails(generalOrderContestedCaseDetails()).caseDetailsBefore(generalOrderContestedCaseDetails()).build();
        manageCaseDocumentsAboutToSubmitCaseHandler.handle(callbackRequest, AUTH_TOKEN);

        verify(manageCaseDocumentsService).manageCaseDocuments(any());
    }

    private CaseDetails generalOrderContestedCaseDetails() {
        try (InputStream resourceAsStream = getClass().getResourceAsStream("/fixtures/general-order-contested.json")) {
            return objectMapper.readValue(resourceAsStream, CallbackRequest.class).getCaseDetails();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}