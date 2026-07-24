package uk.gov.hmcts.reform.finrem.caseorchestration.handler;

import org.assertj.core.api.InstanceOfAssertFactories;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.NullSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.finrem.caseorchestration.FinremCallbackRequestFactory;
import uk.gov.hmcts.reform.finrem.caseorchestration.controllers.GenericAboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.EventType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.DynamicMultiSelectList;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.DynamicMultiSelectListElement;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.ExpressCaseParticipation;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseData;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.YesOrNo;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.wrapper.ExpressCaseWrapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.express.ExpressCaseService;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.finrem.caseorchestration.TestConstants.AUTH_TOKEN;
import static uk.gov.hmcts.reform.finrem.caseorchestration.ccd.callback.CallbackType.ABOUT_TO_START;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.EventType.MANAGE_EXPRESS_CASE;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.EventType.MANAGE_EXPRESS_CASE_V2;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseType.CONTESTED;
import static uk.gov.hmcts.reform.finrem.caseorchestration.test.Assertions.assertCanHandle;

@ExtendWith(MockitoExtension.class)
class ManageExpressCaseAboutToStartHandlerTest {

    @InjectMocks
    private ManageExpressCaseAboutToStartHandler underTest;

    @Mock
    private ExpressCaseService expressCaseService;

    @Test
    void testHandlerCanHandle() {
        assertCanHandle(underTest,
            Arguments.of(ABOUT_TO_START, CONTESTED, MANAGE_EXPRESS_CASE),
            Arguments.of(ABOUT_TO_START, CONTESTED, MANAGE_EXPRESS_CASE_V2)
        );
    }

    @ParameterizedTest
    @EnumSource(value = EventType.class, names = {"MANAGE_EXPRESS_CASE", "MANAGE_EXPRESS_CASE_V2"})
    void shouldBuildConfirmRemoveCaseFromExpressPilotEntry(EventType eventType) {
        FinremCaseData caseData = FinremCaseData.builder().build();

        var response = underTest.handle(FinremCallbackRequestFactory.from(caseData)
            .toBuilder().eventType(eventType).build(), AUTH_TOKEN);

        assertThat(response)
            .isNotNull()
            .extracting(GenericAboutToStartOrSubmitCallbackResponse::getData)
            .extracting(FinremCaseData::getExpressCaseWrapper)
            .extracting(ExpressCaseWrapper::getConfirmRemoveCaseFromExpressPilot)
            .extracting(DynamicMultiSelectList::getListItems)
            .asInstanceOf(InstanceOfAssertFactories.list(DynamicMultiSelectListElement.class))
            .containsExactly(DynamicMultiSelectListElement.builder()
                .code(YesOrNo.YES.getYesOrNo())
                .label("Confirm that this case should no longer be in the Express Financial Remedy Pilot")
                .build());
    }

    @Test
    void shouldSetDefaultAnswerForExpressPilotQuestionToYes() {
        FinremCaseData caseData = FinremCaseData.builder().build();
        caseData.getExpressCaseWrapper().setExpressCaseParticipation(ExpressCaseParticipation.ENROLLED);

        var response = underTest.handle(FinremCallbackRequestFactory.from(caseData)
            .toBuilder().eventType(MANAGE_EXPRESS_CASE).build(), AUTH_TOKEN);

        assertNotNull(response);
        assertNotNull(response.getData());
        assertEquals(YesOrNo.YES, response.getData().getExpressCaseWrapper().getExpressPilotQuestion());
        verifyNoInteractions(expressCaseService);
    }

    @ParameterizedTest
    @EnumSource(value = ExpressCaseParticipation.class, names = {"DOES_NOT_QUALIFY", "WITHDRAWN"})
    @NullSource
    void shouldSetDefaultAnswerForExpressPilotQuestionToNo(ExpressCaseParticipation expressCaseParticipation) {
        FinremCaseData caseData = FinremCaseData.builder().build();
        caseData.getExpressCaseWrapper().setExpressCaseParticipation(expressCaseParticipation);

        var response = underTest.handle(FinremCallbackRequestFactory.from(caseData)
            .toBuilder().eventType(MANAGE_EXPRESS_CASE).build(), AUTH_TOKEN);

        assertNotNull(response);
        assertNotNull(response.getData());
        assertEquals(YesOrNo.NO, response.getData().getExpressCaseWrapper().getExpressPilotQuestion());
        verifyNoInteractions(expressCaseService);
    }

    @Test
    void givenManageExpressCaseV2Event_shouldNotSetExpressPilotQuestion() {
        FinremCaseData caseData = mock(FinremCaseData.class);
        ExpressCaseWrapper expressCaseWrapper = mock(ExpressCaseWrapper.class);
        when(caseData.getExpressCaseWrapper()).thenReturn(expressCaseWrapper);

        underTest.handle(FinremCallbackRequestFactory.from(caseData)
            .toBuilder().eventType(MANAGE_EXPRESS_CASE_V2).build(), AUTH_TOKEN);

        verify(expressCaseWrapper, never()).setExpressPilotQuestion(any(YesOrNo.class));
    }

    @Test
    void givenManageExpressCaseV2Event_whenCannotSetExpressPilotStatus_thenPopulateError() {
        FinremCaseData caseData = FinremCaseData.builder().build();

        when(expressCaseService.canSetExpressPilotStatus(caseData, false)).thenReturn(false);

        var response = underTest.handle(FinremCallbackRequestFactory.from(caseData)
            .toBuilder().eventType(MANAGE_EXPRESS_CASE_V2).build(), AUTH_TOKEN);

        assertThat(response)
            .extracting(GenericAboutToStartOrSubmitCallbackResponse::getErrors)
            .isEqualTo(List.of("This case is not enrolled in the Express Financial Remedy Pilot and does meet the criteria to be enrolled"));
        verify(expressCaseService).canSetExpressPilotStatus(caseData, false);
    }

    @Test
    void givenManageExpressCaseV2Event_whenCanSetExpressPilotStatus_thenDoNotPopulateError() {
        FinremCaseData caseData = FinremCaseData.builder().build();

        when(expressCaseService.canSetExpressPilotStatus(caseData, false)).thenReturn(true);

        var response = underTest.handle(FinremCallbackRequestFactory.from(caseData)
            .toBuilder().eventType(MANAGE_EXPRESS_CASE_V2).build(), AUTH_TOKEN);

        assertThat(response.getErrors()).isEmpty();
        verify(expressCaseService).canSetExpressPilotStatus(caseData, false);
    }
}
