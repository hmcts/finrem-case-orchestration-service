package uk.gov.hmcts.reform.finrem.caseorchestration.handler.creategeneralemail;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.finrem.caseorchestration.FinremCallbackRequestFactory;
import uk.gov.hmcts.reform.finrem.caseorchestration.handler.FinremCallbackRequest;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.EventType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseData;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.wrapper.GeneralEmailWrapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.GeneralEmailService;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.verify;
import static uk.gov.hmcts.reform.finrem.caseorchestration.TestConstants.AUTH_TOKEN;
import static uk.gov.hmcts.reform.finrem.caseorchestration.TestConstants.CASE_ID_IN_LONG;
import static uk.gov.hmcts.reform.finrem.caseorchestration.ccd.callback.CallbackType.MID_EVENT;
import static uk.gov.hmcts.reform.finrem.caseorchestration.test.Assertions.assertCanHandleAnyCaseType;

@ExtendWith(MockitoExtension.class)
class GeneralEmailMidHandlerTest {

    @InjectMocks
    private GeneralEmailMidHandler handler;

    @Mock
    private GeneralEmailService generalEmailService;

    @Test
    void shouldHandleAllCaseTypes() {
        assertCanHandleAnyCaseType(handler, MID_EVENT, EventType.CREATE_GENERAL_EMAIL);
    }

    @Test
    void givenMidEvent_whenHandle_thenShouldValidateUploadedDocuments() {
        FinremCaseData finremCaseData = FinremCaseData.builder()
            .generalEmailWrapper(GeneralEmailWrapper.builder().build())
            .build();
        FinremCallbackRequest request = FinremCallbackRequestFactory.from(CASE_ID_IN_LONG, finremCaseData);

        handler.handle(request, AUTH_TOKEN);

        verify(generalEmailService).validateUploadedDocuments(eq(finremCaseData), eq(AUTH_TOKEN), anyList());
    }

    @Test
    void givenValidationErrors_whenHandle_thenPopulateErrors() {
        FinremCaseData finremCaseData = FinremCaseData.builder()
            .generalEmailWrapper(GeneralEmailWrapper.builder().build())
            .build();
        FinremCallbackRequest request = FinremCallbackRequestFactory.from(CASE_ID_IN_LONG, finremCaseData);

        doAnswer(invocation -> {
            ((List<String>) invocation.getArguments()[2]).add("Only Word and PDF documents are permitted");
            return null;
        }).when(generalEmailService).validateUploadedDocuments(eq(finremCaseData), eq(AUTH_TOKEN), anyList());

        var response = handler.handle(request, AUTH_TOKEN);

        assertAll(
            () -> verify(generalEmailService).validateUploadedDocuments(eq(finremCaseData), eq(AUTH_TOKEN), anyList()),
            () -> assertThat(response.getErrors()).containsOnly("Only Word and PDF documents are permitted")
        );
    }
}
