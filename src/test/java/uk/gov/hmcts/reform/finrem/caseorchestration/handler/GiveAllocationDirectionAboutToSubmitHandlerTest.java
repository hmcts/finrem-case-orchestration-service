package uk.gov.hmcts.reform.finrem.caseorchestration.handler;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.provider.Arguments;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.finrem.caseorchestration.FinremCallbackRequestFactory;
import uk.gov.hmcts.reform.finrem.caseorchestration.ccd.callback.CallbackType;
import uk.gov.hmcts.reform.finrem.caseorchestration.mapper.CourtDetailsMapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.EventType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseData;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.wrapper.AllocatedRegionWrapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.wrapper.RegionWrapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.SelectedCourtService;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.finrem.caseorchestration.TestConstants.AUTH_TOKEN;
import static uk.gov.hmcts.reform.finrem.caseorchestration.TestConstants.CASE_ID_IN_LONG;
import static uk.gov.hmcts.reform.finrem.caseorchestration.test.Assertions.assertCanHandle;

@ExtendWith(MockitoExtension.class)
class GiveAllocationDirectionAboutToSubmitHandlerTest {

    @InjectMocks
    private GiveAllocationDirectionAboutToSubmitHandler handler;

    @Mock
    private CourtDetailsMapper courtDetailsMapper;

    @Mock
    private SelectedCourtService selectedCourtService;

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
}
