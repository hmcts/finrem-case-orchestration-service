package uk.gov.hmcts.reform.finrem.caseorchestration.handler;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.hmcts.reform.ccd.client.model.CallbackRequest;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.ccd.callback.CallbackType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.EventType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseType;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.ManageCaseDocumentsService;

import java.io.InputStream;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static uk.gov.hmcts.reform.finrem.caseorchestration.TestConstants.AUTH_TOKEN;

@RunWith(MockitoJUnitRunner.class)
public class ManageCaseDocumentsContestedAboutToStartCaseHandlerTest {

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Mock
    private ManageCaseDocumentsService manageCaseDocumentsService;

    @InjectMocks
    private ManageCaseDocumentsContestedAboutToStartCaseHandler manageCaseDocumentsAboutToStartCaseHandler;

    @Test
    public void givenACcdCallbackContestedCase_WhenAnAboutToStartEventManageCaseDocuments_thenHandlerCanHandle() {
        assertThat(manageCaseDocumentsAboutToStartCaseHandler
                .canHandle(CallbackType.ABOUT_TO_START, CaseType.CONTESTED, EventType.MANAGE_CASE_DOCUMENTS),
            is(true));
    }

    @Test
    public void givenACcdCallbackConsentedCase_WhenAnAboutToStartEventManageCaseDocuments_thenHandlerCanNotHandle() {
        assertThat(manageCaseDocumentsAboutToStartCaseHandler
                .canHandle(CallbackType.ABOUT_TO_START, CaseType.CONSENTED, EventType.MANAGE_CASE_DOCUMENTS),
            is(false));
    }

    @Test
    public void givenManageCaseDocumentsAboutToStartCaseHandlerShouldCallUploadCaseFilesAboutToSubmitHandlerMethod() {

        CallbackRequest callbackRequest =
            CallbackRequest.builder().caseDetails(generalOrderContestedCaseDetails()).build();
        manageCaseDocumentsAboutToStartCaseHandler.handle(callbackRequest, AUTH_TOKEN);

        verify(manageCaseDocumentsService).setApplicantAndRespondentDocumentsCollection(any());
    }

    private CaseDetails generalOrderContestedCaseDetails() {
        try (InputStream resourceAsStream = getClass().getResourceAsStream("/fixtures/general-order-contested.json")) {
            return objectMapper.readValue(resourceAsStream, CallbackRequest.class).getCaseDetails();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}