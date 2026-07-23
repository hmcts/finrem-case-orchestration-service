package uk.gov.hmcts.reform.finrem.caseorchestration.handler.giveallocationdirection;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.EnumSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.finrem.caseorchestration.FinremCallbackRequestFactory;
import uk.gov.hmcts.reform.finrem.caseorchestration.ccd.callback.CallbackType;
import uk.gov.hmcts.reform.finrem.caseorchestration.controllers.GenericAboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.finrem.caseorchestration.handler.FinremCallbackRequest;
import uk.gov.hmcts.reform.finrem.caseorchestration.mapper.CourtDetailsMapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.mapper.FinremCaseDetailsMapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.EventType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.ExpressCaseParticipation;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseData;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.YesOrNo;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.wrapper.AllocatedRegionWrapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.wrapper.ExpressCaseWrapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.wrapper.RegionWrapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.SelectedCourtService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.express.ExpressCaseService;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.finrem.caseorchestration.TestConstants.AUTH_TOKEN;
import static uk.gov.hmcts.reform.finrem.caseorchestration.TestConstants.CASE_ID_IN_LONG;
import static uk.gov.hmcts.reform.finrem.caseorchestration.TestSetUpUtils.verifyTemporaryFieldsWereSanitised;
import static uk.gov.hmcts.reform.finrem.caseorchestration.test.Assertions.assertCanHandle;

@ExtendWith(MockitoExtension.class)
class GiveAllocationDirectionAboutToSubmitHandlerTest {

    @InjectMocks
    private GiveAllocationDirectionAboutToSubmitHandler handler;

    @Mock
    private CourtDetailsMapper courtDetailsMapper;

    @Mock
    private SelectedCourtService selectedCourtService;

    @Mock
    private FinremCaseDetailsMapper finremCaseDetailsMapper;

    @Mock
    private ExpressCaseService expressCaseService;

    @Test
    void testCanHandle() {
        assertCanHandle(handler,
            Arguments.of(CallbackType.ABOUT_TO_SUBMIT, CaseType.CONTESTED, EventType.GIVE_ALLOCATION_DIRECTIONS),
            Arguments.of(CallbackType.ABOUT_TO_SUBMIT, CaseType.CONTESTED, EventType.GIVE_ALLOCATION_DIRECTIONS_V2));
    }

    @Test
    void givenCase_whenHandleAllocationDirection_thenCourtDetailsMapperCalled() {
        FinremCaseData finremCaseData = FinremCaseData.builder().build();
        FinremCaseData finremCaseDataBefore = FinremCaseData.builder().build();

        AllocatedRegionWrapper beforeAllocatedRegionWrapper = finremCaseDataBefore.getRegionWrapper().getAllocatedRegionWrapper();
        AllocatedRegionWrapper allocatedRegionWrapper = finremCaseData.getRegionWrapper().getAllocatedRegionWrapper();
        FinremCallbackRequest callbackRequest = FinremCallbackRequestFactory.from(CASE_ID_IN_LONG,
            finremCaseData, finremCaseDataBefore);

        AllocatedRegionWrapper newAllocatedRegionWrapper = new AllocatedRegionWrapper();
        when(courtDetailsMapper.getLatestAllocatedCourt(beforeAllocatedRegionWrapper, allocatedRegionWrapper, false))
            .thenReturn(newAllocatedRegionWrapper);

        var response = handler.handle(callbackRequest, AUTH_TOKEN);

        assertThat(response.getData())
            .extracting(FinremCaseData::getRegionWrapper)
            .extracting(RegionWrapper::getAllocatedRegionWrapper)
            .isEqualTo(newAllocatedRegionWrapper);
        verify(courtDetailsMapper).getLatestAllocatedCourt(beforeAllocatedRegionWrapper, allocatedRegionWrapper, false);
        verify(selectedCourtService).setSelectedCourtDetailsIfPresent(callbackRequest.getFinremCaseData());
    }

    @Test
    void shouldClearTemporaryFields() {
        verifyTemporaryFieldsWereSanitised(handler,
            finremCaseDetailsMapper, new HashMap<>(Map.of(
                "shouldAllocateToExpressPilot", "Yes",
                "showShouldAllocateToExpressPilot", "Yes")
            )
        );
    }

    @ParameterizedTest
    @EnumSource(YesOrNo.class)
    void givenAllocateToExpressPilotAnswered_whenHandled_shouldSetExpressCaseEnrollmentStatus(YesOrNo shouldAllocateToExpressPilot) {
        ExpressCaseWrapper expressCaseWrapper = ExpressCaseWrapper.builder()
            .shouldAllocateToExpressPilot(shouldAllocateToExpressPilot)
            .build();
        FinremCaseData finremCaseData = FinremCaseData.builder()
            .expressCaseWrapper(expressCaseWrapper)
            .build();

        FinremCallbackRequest callbackRequest = FinremCallbackRequestFactory.from(finremCaseData);

        handler.handle(callbackRequest, AUTH_TOKEN);

        verify(expressCaseService, times(YesOrNo.isYes(shouldAllocateToExpressPilot) ? 1 : 0))
            .setExpressCaseEnrollmentStatus(finremCaseData);
    }

    @ParameterizedTest
    @EnumSource(value = ExpressCaseParticipation.class, names = {"DOES_NOT_QUALIFY", "WITHDRAWN"})
    void givenShouldAllocatedToExpressPilot_whenHandled_thenPopulateWarning(
        ExpressCaseParticipation beforeExpressCaseParticipation
    ) {

        FinremCaseData finremCaseData = FinremCaseData.builder()
            .expressCaseWrapper(ExpressCaseWrapper.builder()
                .shouldAllocateToExpressPilot(YesOrNo.YES)
                .build())
            .build();
        FinremCaseData finremCaseDataBefore = FinremCaseData.builder()
            .expressCaseWrapper(ExpressCaseWrapper.builder()
                .expressCaseParticipation(beforeExpressCaseParticipation)
                .build())
            .build();

        FinremCallbackRequest callbackRequest = FinremCallbackRequestFactory.from(CASE_ID_IN_LONG,
            finremCaseDataBefore, finremCaseData);

        doAnswer(f -> {
            finremCaseData.getExpressCaseWrapper().setExpressCaseParticipation(ExpressCaseParticipation.ENROLLED);
            return null;
        }).when(expressCaseService).setExpressCaseEnrollmentStatus(finremCaseData);

        var response = handler.handle(callbackRequest, AUTH_TOKEN);
        assertThat(response)
            .extracting(GenericAboutToStartOrSubmitCallbackResponse::getWarnings)
            .isEqualTo(List.of("This case will now be enrolled into the Express Pilot."));
    }

    @ParameterizedTest
    @EnumSource(ExpressCaseParticipation.class)
    void givenNoParticipationChange_whenHandled_thenDoNotPopulateWarning(
        ExpressCaseParticipation expressCaseParticipation
    ) {

        FinremCaseData finremCaseData = FinremCaseData.builder()
            .expressCaseWrapper(ExpressCaseWrapper.builder()
                .shouldAllocateToExpressPilot(YesOrNo.YES)
                .build())
            .build();
        FinremCaseData finremCaseDataBefore = FinremCaseData.builder()
            .expressCaseWrapper(ExpressCaseWrapper.builder()
                .expressCaseParticipation(expressCaseParticipation)
                .build())
            .build();

        FinremCallbackRequest callbackRequest = FinremCallbackRequestFactory.from(CASE_ID_IN_LONG,
            finremCaseDataBefore, finremCaseData);

        doAnswer(f -> {
            // guarantee it's no change
            finremCaseData.getExpressCaseWrapper()
                .setExpressCaseParticipation(
                    finremCaseDataBefore.getExpressCaseWrapper().getExpressCaseParticipation()
                );
            return null;
        }).when(expressCaseService).setExpressCaseEnrollmentStatus(finremCaseData);

        var response = handler.handle(callbackRequest, AUTH_TOKEN);
        assertThat(response.getWarnings()).isEmpty();
    }
}
