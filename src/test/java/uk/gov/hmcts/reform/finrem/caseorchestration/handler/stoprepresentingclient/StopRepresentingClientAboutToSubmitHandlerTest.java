package uk.gov.hmcts.reform.finrem.caseorchestration.handler.stoprepresentingclient;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.finrem.caseorchestration.FinremCallbackRequestFactory;
import uk.gov.hmcts.reform.finrem.caseorchestration.handler.FinremCallbackRequest;
import uk.gov.hmcts.reform.finrem.caseorchestration.mapper.FinremCaseDetailsMapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseData;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.YesOrNo;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.wrapper.SessionWrapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.wrapper.StopRepresentationWrapper;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static uk.gov.hmcts.reform.finrem.caseorchestration.TestConstants.AUTH_TOKEN;
import static uk.gov.hmcts.reform.finrem.caseorchestration.ccd.callback.CallbackType.ABOUT_TO_SUBMIT;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.EventType.STOP_REPRESENTING_CLIENT;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseType.CONSENTED;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseType.CONTESTED;
import static uk.gov.hmcts.reform.finrem.caseorchestration.test.Assertions.assertCanHandle;

@ExtendWith(MockitoExtension.class)
class StopRepresentingClientAboutToSubmitHandlerTest {

    private StopRepresentingClientAboutToSubmitHandler underTest;
    @Mock
    private FinremCaseDetailsMapper finremCaseDetailsMapper;

    @BeforeEach
    public void setup() {
        underTest = new StopRepresentingClientAboutToSubmitHandler(finremCaseDetailsMapper);
    }

    @Test
    void testCanHandle() {
        assertCanHandle(underTest,
            Arguments.of(ABOUT_TO_SUBMIT, CONTESTED, STOP_REPRESENTING_CLIENT),
            Arguments.of(ABOUT_TO_SUBMIT, CONSENTED, STOP_REPRESENTING_CLIENT));
    }

    @ParameterizedTest
    @ValueSource(booleans = {true, false})
    void givenHavingClientConsent_whenHandled_thenWarningsPopulated(boolean loginAsApplicant) {
        FinremCaseData caseData = FinremCaseData.builder()
            .stopRepresentationWrapper(StopRepresentationWrapper.builder()
                .clientConsentOnAppSolStopRep(loginAsApplicant ? YesOrNo.YES : mock(YesOrNo.class))
                .clientConsentOnRespSolStopRep(!loginAsApplicant ? YesOrNo.YES : mock(YesOrNo.class))
                .build())
            .sessionWrapper(SessionWrapper.builder()
                .loginAsApplicantSolicitor(YesOrNo.forValue(loginAsApplicant))
                .loginAsRespondentSolicitor(YesOrNo.forValue(!loginAsApplicant))
                .build())
            .build();

        FinremCallbackRequest request = FinremCallbackRequestFactory.from(caseData);
        assertThat(underTest.handle(request, AUTH_TOKEN).getWarnings()).containsExactly(
            "Are you sure you wish to stop representing your client? "
                + "If you continue your access to this access will be removed"
        );
    }

    @ParameterizedTest
    @ValueSource(booleans = {true, false})
    void givenHavingJudicialApproval_whenHandled_thenWarningsPopulated(boolean loginAsApplicant) {
        FinremCaseData caseData = FinremCaseData.builder()
            .stopRepresentationWrapper(StopRepresentationWrapper.builder()
                .judicialApprovalOnAppSolStopRep(loginAsApplicant ? YesOrNo.YES : mock(YesOrNo.class))
                .judicialApprovalOnRespSolStopRep(!loginAsApplicant ? YesOrNo.YES : mock(YesOrNo.class))
                .build())
            .sessionWrapper(SessionWrapper.builder()
                .loginAsApplicantSolicitor(YesOrNo.forValue(loginAsApplicant))
                .loginAsRespondentSolicitor(YesOrNo.forValue(!loginAsApplicant))
                .build())
            .build();

        FinremCallbackRequest request = FinremCallbackRequestFactory.from(caseData);
        assertThat(underTest.handle(request, AUTH_TOKEN).getWarnings()).containsExactly(
            "Are you sure you wish to stop representing your client? "
                + "If you continue your access to this access will be removed"
        );
    }
}
