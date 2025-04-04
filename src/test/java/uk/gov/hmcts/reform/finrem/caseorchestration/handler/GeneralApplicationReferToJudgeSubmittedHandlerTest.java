package uk.gov.hmcts.reform.finrem.caseorchestration.handler;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.finrem.caseorchestration.FinremCallbackRequestFactory;
import uk.gov.hmcts.reform.finrem.caseorchestration.TestConstants;
import uk.gov.hmcts.reform.finrem.caseorchestration.ccd.callback.CallbackType;
import uk.gov.hmcts.reform.finrem.caseorchestration.mapper.FinremCaseDetailsMapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.EventType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseData;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.wrapper.GeneralApplicationWrapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.NotificationService;
import uk.gov.hmcts.reform.finrem.caseorchestration.test.Assertions;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class GeneralApplicationReferToJudgeSubmittedHandlerTest {

    @InjectMocks
    private GeneralApplicationReferToJudgeSubmittedHandler handler;
    @Mock
    private NotificationService notificationService;
    @Mock
    private FinremCaseDetailsMapper finremCaseDetailsMapper;

    @Test
    void testCanHandle() {
        Assertions.assertCanHandle(handler, CallbackType.SUBMITTED, CaseType.CONTESTED,
            EventType.GENERAL_APPLICATION_REFER_TO_JUDGE);
    }

    @Test
    void givenJudgeEmailPresent_whenHandle_thenSendsEmail() {
        FinremCaseDetails caseDetails = createCaseDetails("judge@judge.com");
        FinremCallbackRequest request = FinremCallbackRequestFactory.from(caseDetails);

        var response = handler.handle(request, TestConstants.AUTH_TOKEN);

        assertThat(response).isNotNull();
        verify(notificationService).sendContestedGeneralApplicationReferToJudgeEmail(caseDetails);
    }

    @Test
    void givenJudgeEmailNotPresent_whenHandle_thenDoesNotSendEmail() {
        FinremCaseDetails caseDetails = createCaseDetails(null);
        FinremCallbackRequest request = FinremCallbackRequestFactory.from(caseDetails);

        var response = handler.handle(request, TestConstants.AUTH_TOKEN);

        assertThat(response).isNotNull();
        verify(notificationService, never()).sendContestedGeneralApplicationReferToJudgeEmail(caseDetails);
    }

    private FinremCaseDetails createCaseDetails(String judgeEmail) {
        return FinremCaseDetails.builder()
            .data(FinremCaseData.builder()
                .generalApplicationWrapper(GeneralApplicationWrapper.builder()
                    .generalApplicationReferToJudgeEmail(judgeEmail)
                    .build())
                .build())
            .build();
    }
}
