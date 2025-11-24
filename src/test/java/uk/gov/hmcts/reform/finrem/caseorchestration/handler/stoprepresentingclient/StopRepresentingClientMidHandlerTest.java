package uk.gov.hmcts.reform.finrem.caseorchestration.handler.stoprepresentingclient;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.provider.Arguments;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.finrem.caseorchestration.mapper.FinremCaseDetailsMapper;

import static uk.gov.hmcts.reform.finrem.caseorchestration.ccd.callback.CallbackType.MID_EVENT;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.EventType.STOP_REPRESENTING_CLIENT;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseType.CONSENTED;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseType.CONTESTED;
import static uk.gov.hmcts.reform.finrem.caseorchestration.test.Assertions.assertCanHandle;

@ExtendWith(MockitoExtension.class)
class StopRepresentingClientMidHandlerTest {

    private StopRepresentingClientMidHandler underTest;
    @Mock
    private FinremCaseDetailsMapper finremCaseDetailsMapper;

    @BeforeEach
    public void setup() {
        underTest = new StopRepresentingClientMidHandler(finremCaseDetailsMapper);
    }

    @Test
    void testCanHandle() {
        assertCanHandle(underTest,
            Arguments.of(MID_EVENT, CONSENTED, STOP_REPRESENTING_CLIENT),
            Arguments.of(MID_EVENT, CONTESTED, STOP_REPRESENTING_CLIENT));
    }

//    @ParameterizedTest
//    @ValueSource(booleans = {true, false})
//    void givenIsHavingClientConsent_whenHandled_thenErrorPopulated(boolean loginAsApplicant) {
//        FinremCaseData caseData = FinremCaseData.builder()
//            .stopRepresentationWrapper(StopRepresentationWrapper.builder()
//                .clientConsentOnAppSolStopRep(loginAsApplicant ? YesOrNo.YES : mock(YesOrNo.class))
//                .clientConsentOnRespSolStopRep(!loginAsApplicant ? YesOrNo.YES : mock(YesOrNo.class))
//                .build())
//            .sessionWrapper(SessionWrapper.builder()
//                .loginAsApplicantSolicitor(YesOrNo.forValue(loginAsApplicant))
//                .loginAsRespondentSolicitor(YesOrNo.forValue(!loginAsApplicant))
//                .build())
//            .build();
//
//        FinremCallbackRequest request = FinremCallbackRequestFactory.from(caseData);
//        assertThat(underTest.handle(request, AUTH_TOKEN).getErrors()).isEmpty();
//    }
}
