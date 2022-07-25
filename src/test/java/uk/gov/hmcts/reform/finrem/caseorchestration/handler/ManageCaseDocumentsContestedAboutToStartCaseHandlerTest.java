package uk.gov.hmcts.reform.finrem.caseorchestration.handler;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.ManageCaseDocumentsService;
import uk.gov.hmcts.reform.finrem.ccd.callback.CallbackRequest;
import uk.gov.hmcts.reform.finrem.ccd.callback.CallbackType;
import uk.gov.hmcts.reform.finrem.ccd.domain.CaseType;
import uk.gov.hmcts.reform.finrem.ccd.domain.EventType;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static uk.gov.hmcts.reform.finrem.caseorchestration.TestConstants.AUTH_TOKEN;

@RunWith(MockitoJUnitRunner.class)
public class ManageCaseDocumentsContestedAboutToStartCaseHandlerTest extends BaseHandlerTest {

    public static final String GENERAL_ORDER_CONTESTED_JSON = "/fixtures/general-order-contested.json";
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

        CallbackRequest callbackRequest = getCallbackRequestFromResource(GENERAL_ORDER_CONTESTED_JSON);
        manageCaseDocumentsAboutToStartCaseHandler.handle(callbackRequest, AUTH_TOKEN);

        verify(manageCaseDocumentsService).setApplicantAndRespondentDocumentsCollection(any());
    }
}