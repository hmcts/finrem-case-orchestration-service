package uk.gov.hmcts.reform.finrem.caseorchestration.handler;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.hmcts.reform.finrem.caseorchestration.ccd.callback.CallbackType;
import uk.gov.hmcts.reform.finrem.caseorchestration.mapper.FinremCaseDetailsMapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.EventType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseDocument;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseData;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseDataConsented;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.wrapper.GeneralEmailWrapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.GeneralEmailService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.GenericDocumentService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.NotificationService;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.verify;
import static uk.gov.hmcts.reform.finrem.caseorchestration.TestConstants.AUTH_TOKEN;

@RunWith(MockitoJUnitRunner.class)
public class GeneralEmailAboutToSubmitHandlerTest {

    private GeneralEmailAboutToSubmitHandler handler;

    @Mock
    private GeneralEmailService generalEmailService;

    @Mock
    private NotificationService notificationService;

    @Mock
    private GenericDocumentService genericDocumentService;
    @Mock
    private FinremCaseDetailsMapper finremCaseDetailsMapper;

    @Before
    public void setup() {
        handler = new GeneralEmailAboutToSubmitHandler(finremCaseDetailsMapper,
            notificationService,
            generalEmailService,
            genericDocumentService);
    }

    @Test
    public void givenACcdCallbackGeneralEmailAboutToSubmitHandler_WhenCanHandleCalled_thenHandlerCanHandle() {
        assertThat(handler
                .canHandle(CallbackType.ABOUT_TO_SUBMIT, CaseType.CONTESTED, EventType.CREATE_GENERAL_EMAIL),
            is(true));
    }

    @Test
    public void givenACcdCallbackAboutToSubmit_WhenCanHandleCalled_thenHandlerCanNotHandle() {
        assertThat(handler
                .canHandle(CallbackType.SUBMITTED, CaseType.CONTESTED, EventType.CREATE_GENERAL_EMAIL),
            is(false));
    }

    @Test
    public void givenACcdCallbackCallbackGeneralEmailAboutToSubmitHandler_WhenHandle_thenSendEmail() {
        FinremCallbackRequest callbackRequest = buildFinremCallbackRequest();
        handler.handle(callbackRequest, AUTH_TOKEN);
        verify(notificationService).sendConsentGeneralEmail(any(FinremCaseDetails.class), anyString());
        verify(generalEmailService).storeGeneralEmail(any(FinremCaseDetails.class));
    }

    private FinremCallbackRequest buildFinremCallbackRequest() {
        FinremCaseData caseData = FinremCaseDataConsented.builder()
            .ccdCaseType(CaseType.CONSENTED)
            .generalEmailWrapper(GeneralEmailWrapper.builder()
                .generalEmailRecipient("Test")
                .generalEmailCreatedBy("Test")
                .generalEmailBody("body")
                .generalEmailUploadedDocument(CaseDocument.builder().build())
                .build())
            .build();
        FinremCaseDetails caseDetails = FinremCaseDetails.builder().id(123L).caseType(CaseType.CONSENTED).data(caseData).build();
        return FinremCallbackRequest.builder().eventType(EventType.CREATE_GENERAL_EMAIL).caseDetails(caseDetails).build();
    }

}