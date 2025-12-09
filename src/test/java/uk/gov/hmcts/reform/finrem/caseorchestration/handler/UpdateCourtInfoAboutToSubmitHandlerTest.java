package uk.gov.hmcts.reform.finrem.caseorchestration.handler;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
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

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static uk.gov.hmcts.reform.finrem.caseorchestration.TestConstants.AUTH_TOKEN;
import static uk.gov.hmcts.reform.finrem.caseorchestration.TestConstants.CASE_ID;
import static uk.gov.hmcts.reform.finrem.caseorchestration.test.Assertions.assertCanHandle;

@ExtendWith(MockitoExtension.class)
class UpdateCourtInfoAboutToSubmitHandlerTest {

    @InjectMocks
    private UpdateCourtInfoAboutToSubmitHandler handler;

    @Mock
    private CourtDetailsMapper courtDetailsMapper;

    @Test
    void testCanHandle() {
        assertCanHandle(handler, CallbackType.ABOUT_TO_SUBMIT, CaseType.CONSENTED, EventType.UPDATE_COURT_INFO);
    }

    @Test
    void givenCase_whenHandleAllocationDirection_thenCourtDetailsMapperCalled() {
        AllocatedRegionWrapper mockedAllocatedRegionWrapper = mock(AllocatedRegionWrapper.class);
        FinremCaseData caseData = FinremCaseData.builder()
            .regionWrapper(RegionWrapper.builder()
                .allocatedRegionWrapper(mockedAllocatedRegionWrapper)
                .build())
            .build();

        AllocatedRegionWrapper mockedAllocatedRegionWrapperBefore = mock(AllocatedRegionWrapper.class);
        FinremCaseData caseDataBefore = FinremCaseData.builder()
            .regionWrapper(RegionWrapper.builder()
                .allocatedRegionWrapper(mockedAllocatedRegionWrapperBefore)
                .build())
            .build();

        FinremCallbackRequest callbackRequest = FinremCallbackRequestFactory.from(Long.valueOf(CASE_ID),
            caseDataBefore, caseData);
        handler.handle(callbackRequest, AUTH_TOKEN);

        verify(courtDetailsMapper).getLatestAllocatedCourt(mockedAllocatedRegionWrapperBefore, mockedAllocatedRegionWrapper,
            true);
    }
}
