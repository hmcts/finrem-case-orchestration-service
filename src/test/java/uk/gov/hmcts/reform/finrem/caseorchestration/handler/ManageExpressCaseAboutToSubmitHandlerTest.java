package uk.gov.hmcts.reform.finrem.caseorchestration.handler;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.NullSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.DynamicMultiSelectList;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.DynamicMultiSelectListElement;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.ExpressCaseParticipation;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseData;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.YesOrNo;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.wrapper.ExpressCaseWrapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.express.ExpressCaseService;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.finrem.caseorchestration.TestConstants.AUTH_TOKEN;
import static uk.gov.hmcts.reform.finrem.caseorchestration.ccd.callback.CallbackType.ABOUT_TO_SUBMIT;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.EventType.MANAGE_EXPRESS_CASE;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseType.CONTESTED;
import static uk.gov.hmcts.reform.finrem.caseorchestration.test.Assertions.assertCanHandle;

@ExtendWith(MockitoExtension.class)
class ManageExpressCaseAboutToSubmitHandlerTest {

    @Mock
    private ExpressCaseService expressCaseService;

    @InjectMocks
    private ManageExpressCaseAboutToSubmitHandler underTest;

    private FinremCallbackRequest callbackRequest;
    private FinremCaseData caseData;
    private ExpressCaseWrapper expressCaseWrapper;

    @BeforeEach
    void setUp() {
        callbackRequest = mock(FinremCallbackRequest.class);
        FinremCaseDetails caseDetails = mock(FinremCaseDetails.class);
        caseData = mock(FinremCaseData.class);
        expressCaseWrapper = mock(ExpressCaseWrapper.class);

        lenient().when(callbackRequest.getCaseDetails()).thenReturn(caseDetails);
        lenient().when(caseDetails.getData()).thenReturn(caseData);
        lenient().when(caseData.getExpressCaseWrapper()).thenReturn(expressCaseWrapper);
    }

    @Test
    void testHandlerCanHandle() {
        assertCanHandle(underTest, ABOUT_TO_SUBMIT, CONTESTED, MANAGE_EXPRESS_CASE);
    }

    @Test
    void shouldWithdrawExpressCase_whenEnrolledAndUserConfirmed() {
        when(caseData.getExpressCaseParticipation()).thenReturn(ExpressCaseParticipation.ENROLLED);
        when(expressCaseWrapper.getExpressPilotQuestion()).thenReturn(YesOrNo.NO);
        when(expressCaseWrapper.getConfirmRemoveCaseFromExpressPilot())
            .thenReturn(
                DynamicMultiSelectList.builder().value(List.of(DynamicMultiSelectListElement.builder().code(YesOrNo.YES.name()).build())).build()
            );

        underTest.handle(callbackRequest, AUTH_TOKEN);

        verify(expressCaseService).setExpressCaseEnrollmentStatusToWithdrawn(caseData);
    }

    @ParameterizedTest
    @EnumSource(value = ExpressCaseParticipation.class, names = {"DOES_NOT_QUALIFY", "WITHDRAWN"})
    @NullSource
    void shouldNotWithdrawExpressCase_whenNotEnrolled(ExpressCaseParticipation notEnrolledEnum) {
        when(caseData.getExpressCaseParticipation()).thenReturn(notEnrolledEnum);

        underTest.handle(callbackRequest, AUTH_TOKEN);

        verify(expressCaseService, never()).setExpressCaseEnrollmentStatusToWithdrawn(caseData);
    }

    @Test
    void shouldNotWithdrawExpressCase_whenExpressPilotQuestionIsYes() {
        when(caseData.getExpressCaseParticipation()).thenReturn(ExpressCaseParticipation.ENROLLED);
        when(expressCaseWrapper.getExpressPilotQuestion()).thenReturn(YesOrNo.YES);

        underTest.handle(callbackRequest, AUTH_TOKEN);

        verify(expressCaseService, never()).setExpressCaseEnrollmentStatusToWithdrawn(any());
    }

    @Test
    void shouldNotWithdrawExpressCase_whenUserDidNotConfirm() {
        when(caseData.getExpressCaseParticipation()).thenReturn(ExpressCaseParticipation.ENROLLED);
        when(expressCaseWrapper.getExpressPilotQuestion()).thenReturn(YesOrNo.NO);
        when(expressCaseWrapper.getConfirmRemoveCaseFromExpressPilot())
            .thenReturn(
                DynamicMultiSelectList.builder().value(
                    List.of(
                        DynamicMultiSelectListElement.builder().code(YesOrNo.NO.name()).build()
                    )
                ).build()
            );

        underTest.handle(callbackRequest, AUTH_TOKEN);

        verify(expressCaseService, never()).setExpressCaseEnrollmentStatusToWithdrawn(any());
    }

    @Test
    void shouldNotWithdrawExpressCase_whenUserConfirmationListIsEmpty() {
        when(caseData.getExpressCaseParticipation()).thenReturn(ExpressCaseParticipation.ENROLLED);
        when(expressCaseWrapper.getExpressPilotQuestion()).thenReturn(YesOrNo.NO);
        when(expressCaseWrapper.getConfirmRemoveCaseFromExpressPilot())
            .thenReturn(
                DynamicMultiSelectList.builder().value(List.of()).build()
            );

        underTest.handle(callbackRequest, AUTH_TOKEN);

        verify(expressCaseService, never()).setExpressCaseEnrollmentStatusToWithdrawn(any());
    }
}
