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

    @ParameterizedTest
    @ValueSource(booleans = {true, false})
    void givenNotHavingJudicialApproval_whenHandled_thenErrorPopulated(boolean loginAsApplicant) {
        FinremCaseData caseData = FinremCaseData.builder()
            .stopRepresentationWrapper(StopRepresentationWrapper.builder()
                .judicialApprovalOnAppSolStopRep(loginAsApplicant ? YesOrNo.NO : mock(YesOrNo.class))
                .judicialApprovalOnRespSolStopRep(!loginAsApplicant ? YesOrNo.NO : mock(YesOrNo.class))
                .build())
            .sessionWrapper(SessionWrapper.builder()
                .loginAsApplicantSolicitor(YesOrNo.forValue(loginAsApplicant))
                .loginAsRespondentSolicitor(YesOrNo.forValue(!loginAsApplicant))
                .build())
            .build();

        FinremCallbackRequest request = FinremCallbackRequestFactory.from(caseData);
        assertThat(underTest.handle(request, AUTH_TOKEN).getErrors()).containsExactly(
            "You cannot stop representing your client without either client consent or judicial approval. "
                + "You will need to make a general application to apply to come off record using the next step event 'general application"
        );
    }
}
