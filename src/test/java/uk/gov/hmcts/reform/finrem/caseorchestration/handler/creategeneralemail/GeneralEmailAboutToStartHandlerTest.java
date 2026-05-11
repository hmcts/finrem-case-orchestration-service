package uk.gov.hmcts.reform.finrem.caseorchestration.handler.creategeneralemail;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.finrem.caseorchestration.FinremCallbackRequestFactory;
import uk.gov.hmcts.reform.finrem.caseorchestration.ccd.callback.CallbackType;
import uk.gov.hmcts.reform.finrem.caseorchestration.handler.FinremCallbackRequest;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.EventType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseData;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.wrapper.GeneralEmailWrapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.IdamService;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.finrem.caseorchestration.TestConstants.AUTH_TOKEN;
import static uk.gov.hmcts.reform.finrem.caseorchestration.test.Assertions.assertCanHandleAnyCaseType;

@ExtendWith(MockitoExtension.class)
class GeneralEmailAboutToStartHandlerTest {

    @InjectMocks
    private GeneralEmailAboutToStartHandler handler;

    @Mock
    private IdamService idamService;

    @Test
    void testCanHandle() {
        assertCanHandleAnyCaseType(handler, CallbackType.ABOUT_TO_START, EventType.CREATE_GENERAL_EMAIL);
    }

    @Test
    void givenCase_whenHandled_shouldClearPropertiesAndCreatedByPopulated() {
        when(idamService.getIdamFullName(AUTH_TOKEN)).thenReturn("UserName");

        FinremCaseData finremCaseData = FinremCaseData.builder().build();

        FinremCallbackRequest request = FinremCallbackRequestFactory.from(finremCaseData);
        handler.handle(request, AUTH_TOKEN);

        assertAll(
            () -> assertThat(finremCaseData.getGeneralEmailWrapper())
                .extracting(
                    GeneralEmailWrapper::getGeneralEmailRecipient,
                    GeneralEmailWrapper::getGeneralEmailUploadedDocument,
                    GeneralEmailWrapper::getGeneralEmailBody
                )
                .containsOnlyNulls(),
            () -> assertThat(finremCaseData.getGeneralEmailWrapper().getGeneralEmailCreatedBy()).isEqualTo("UserName"),
            () -> verify(idamService).getIdamFullName(AUTH_TOKEN)
        );
    }
}
