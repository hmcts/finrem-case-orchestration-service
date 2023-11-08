package uk.gov.hmcts.reform.finrem.caseorchestration.handler;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import uk.gov.hmcts.reform.finrem.caseorchestration.ccd.callback.CallbackType;
import uk.gov.hmcts.reform.finrem.caseorchestration.mapper.FinremCaseDetailsMapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.EventType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseData;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.documentcatergory.DraftOrderDocumentCategoriser;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static uk.gov.hmcts.reform.finrem.caseorchestration.TestConstants.AUTH_TOKEN;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseType.CONTESTED;

@ExtendWith(SpringExtension.class)
class SolicitorUploadDraftOrderAboutToSubmitHandlerTest {

    private SolicitorUploadDraftOrderAboutToSubmitHandler solicitorUploadDraftOrderAboutToSubmitHandler;

    private FinremCaseDetailsMapper finremCaseDetailsMapper;

    @MockBean
    private DraftOrderDocumentCategoriser draftOrderDocumentCategoriser;

    @BeforeEach
    public void setUpTest() {
        solicitorUploadDraftOrderAboutToSubmitHandler = new SolicitorUploadDraftOrderAboutToSubmitHandler(
            finremCaseDetailsMapper, draftOrderDocumentCategoriser);
    }

    @Test
    void givenACcdCallbackContestedCase_WhenAnAboutToSubmitEventUploadDraftOrder_thenHandlerCanHandle() {
        assertThat(solicitorUploadDraftOrderAboutToSubmitHandler
                .canHandle(CallbackType.ABOUT_TO_SUBMIT, CaseType.CONTESTED, EventType.SOLICITOR_CW_DRAFT_ORDER),
            is(true));
    }

    @Test
    void givenACcdCallbackContestedCase_WhenAnAboutToSubmitEventUploadDraftOrderAndIncorrectCallBackType_thenHandlerCanNotHandle() {
        assertThat(solicitorUploadDraftOrderAboutToSubmitHandler
                .canHandle(CallbackType.ABOUT_TO_START, CaseType.CONTESTED, EventType.SOLICITOR_CW_DRAFT_ORDER),
            is(false));
    }

    @Test
    void givenACcdCallbackContestedCase_WhenAnAboutToSubmitEventUploadDraftOrderAndIncorrectCaseType_thenHandlerCanNotHandle() {
        assertThat(solicitorUploadDraftOrderAboutToSubmitHandler
                .canHandle(CallbackType.ABOUT_TO_SUBMIT, CaseType.CONSENTED, EventType.SOLICITOR_CW_DRAFT_ORDER),
            is(false));
    }

    @Test
    void givenACcdCallbackContestedCase_WhenAnAboutToSubmitEventAndIncorrectEventType_thenHandlerCanNotHandle() {
        assertThat(solicitorUploadDraftOrderAboutToSubmitHandler
                .canHandle(CallbackType.ABOUT_TO_SUBMIT, CaseType.CONTESTED, EventType.SEND_ORDER),
            is(false));
    }

    @Test
    void givenACcdCallbackContestedCase_WhenHandledThenHandlesAndCategorisesSuccessfully() {
        FinremCallbackRequest callbackRequest = buildCallbackRequest();

        solicitorUploadDraftOrderAboutToSubmitHandler.handle(callbackRequest, AUTH_TOKEN);

        verify(draftOrderDocumentCategoriser).categorise(any(FinremCaseData.class));

    }

    private FinremCallbackRequest buildCallbackRequest() {
        return FinremCallbackRequest
            .builder()
            .eventType(EventType.SOLICITOR_CW_DRAFT_ORDER)
            .caseDetails(FinremCaseDetails.builder().id(123L).caseType(CONTESTED)
                .data(new FinremCaseData()).build())
            .build();
    }
}
