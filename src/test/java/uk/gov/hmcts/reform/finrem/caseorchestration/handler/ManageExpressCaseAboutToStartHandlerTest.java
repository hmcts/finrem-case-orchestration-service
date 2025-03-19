package uk.gov.hmcts.reform.finrem.caseorchestration.handler;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.NullSource;
import org.mockito.InjectMocks;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.finrem.caseorchestration.FinremCallbackRequestFactory;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.ExpressCaseParticipation;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseData;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.YesOrNo;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static uk.gov.hmcts.reform.finrem.caseorchestration.TestConstants.AUTH_TOKEN;
import static uk.gov.hmcts.reform.finrem.caseorchestration.ccd.callback.CallbackType.ABOUT_TO_START;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.EventType.MANAGE_EXPRESS_CASE;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseType.CONTESTED;
import static uk.gov.hmcts.reform.finrem.caseorchestration.test.Assertions.assertCanHandle;

@ExtendWith(MockitoExtension.class)
class ManageExpressCaseAboutToStartHandlerTest {

    @InjectMocks
    private ManageExpressCaseAboutToStartHandler underTest;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testHandlerCanHandle() {
        assertCanHandle(underTest, ABOUT_TO_START, CONTESTED, MANAGE_EXPRESS_CASE);
    }

    @Test
    void shouldHandleSuccessfully() {
        FinremCaseData caseData = FinremCaseData.builder().build();
        caseData.setExpressCaseParticipation(ExpressCaseParticipation.ENROLLED);

        var response = underTest.handle(FinremCallbackRequestFactory.from(caseData), AUTH_TOKEN);

        assertNotNull(response);
        assertNotNull(response.getData());
        assertEquals(YesOrNo.YES, response.getData().getExpressCaseWrapper().getExpressPilotQuestion());
        assertNotNull(response.getData().getExpressCaseWrapper().getConfirmRemoveCaseFromExpressPilot());
    }

    @ParameterizedTest
    @EnumSource(value = ExpressCaseParticipation.class, names = {"DOES_NOT_QUALIFY", "WITHDRAWN"})
    @NullSource
    void shouldSetDefaultAnswerForExpressPilotQuestionToNo(ExpressCaseParticipation expressCaseParticipation) {
        FinremCaseData caseData = FinremCaseData.builder().build();
        caseData.setExpressCaseParticipation(expressCaseParticipation);

        var response = underTest.handle(FinremCallbackRequestFactory.from(caseData), AUTH_TOKEN);

        assertNotNull(response);
        assertNotNull(response.getData());
        assertEquals(YesOrNo.NO, response.getData().getExpressCaseWrapper().getExpressPilotQuestion());
    }
}
