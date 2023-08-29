package uk.gov.hmcts.reform.finrem.caseorchestration.handler;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.finrem.caseorchestration.ccd.callback.CallbackType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.EventType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseDataContested;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.IntervenerShareDocumentsService;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseType.CONTESTED;

@ExtendWith(MockitoExtension.class)
class ShareSelectedDocumentsAboutToSubmitHandlerTest {

    public static final String AUTH_TOKEN = "tokien:)";
    @InjectMocks
    private ShareSelectedDocumentsAboutToSubmitHandler handler;

    @Mock
    private IntervenerShareDocumentsService intervenerShareDocumentsService;


    @Test
    void givenContestedCase_whenRequiredEventCallbackIsSubmitted_thenHandlerCanNotHandle() {
        assertThat(handler
                .canHandle(CallbackType.SUBMITTED, CaseType.CONTESTED, EventType.SHARE_SELECTED_DOCUMENTS),
            is(false));
    }

    @Test
    void givenContestedCase_whenRequiredEventCaseTypeIsConsented_thenHandlerCanNotHandle() {
        assertThat(handler
                .canHandle(CallbackType.ABOUT_TO_SUBMIT, CaseType.CONSENTED, EventType.SHARE_SELECTED_DOCUMENTS),
            is(false));
    }

    @Test
    void givenContestedCase_whenRequiredEventTypeIsNotValid_thenHandlerCanNotHandle() {
        assertThat(handler
                .canHandle(CallbackType.ABOUT_TO_SUBMIT, CaseType.CONTESTED, EventType.CLOSE),
            is(false));
    }

    @Test
    void givenContestedCase_whenRequiredConditionSatisfied_thenHandlerCanHandle() {
        assertThat(handler
                .canHandle(CallbackType.ABOUT_TO_SUBMIT, CaseType.CONTESTED, EventType.SHARE_SELECTED_DOCUMENTS),
            is(true));
    }

    @Test
    void givenContestedCase_whenInvokedSharedService_thenHandlerCanHandle() {
        FinremCallbackRequest finremCallbackRequest = buildCallbackRequest();
        handler.handle(finremCallbackRequest, AUTH_TOKEN);
        verify(intervenerShareDocumentsService).shareSelectedDocumentWithOtherSelectedSolicitors(any());
    }

    private FinremCallbackRequest buildCallbackRequest() {
        return FinremCallbackRequest
            .builder()
            .eventType(EventType.SHARE_SELECTED_DOCUMENTS)
            .caseDetails(FinremCaseDetails.builder().id(123L).caseType(CONTESTED)
                .data(new FinremCaseDataContested()).build())
            .build();
    }
}