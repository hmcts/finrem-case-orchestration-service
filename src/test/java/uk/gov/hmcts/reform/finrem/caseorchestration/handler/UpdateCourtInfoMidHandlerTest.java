package uk.gov.hmcts.reform.finrem.caseorchestration.handler;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.finrem.caseorchestration.FinremCallbackRequestFactory;
import uk.gov.hmcts.reform.finrem.caseorchestration.ccd.callback.CallbackType;
import uk.gov.hmcts.reform.finrem.caseorchestration.controllers.GenericAboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.finrem.caseorchestration.helper.ConsentedApplicationHelper;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.EventType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseData;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.Region;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.wrapper.AllocatedRegionWrapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.wrapper.RegionWrapper;

import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.hmcts.reform.finrem.caseorchestration.TestConstants.AUTH_TOKEN;
import static uk.gov.hmcts.reform.finrem.caseorchestration.test.Assertions.assertCanHandle;

@ExtendWith(MockitoExtension.class)
class UpdateCourtInfoMidHandlerTest {

    @InjectMocks
    private UpdateCourtInfoMidHandler underTest;

    @Spy
    private ConsentedApplicationHelper consentedApplicationHelper;

    @Test
    void testCanHandle() {
        assertCanHandle(underTest, CallbackType.MID_EVENT, CaseType.CONSENTED, EventType.UPDATE_COURT_INFO);
    }

    @Test
    void givenHighCourtSelected_whenHandled_thenPopulateAnErrorMessage() {
        FinremCaseData caseData = FinremCaseData.builder()
            .regionWrapper(RegionWrapper.builder()
                .allocatedRegionWrapper(AllocatedRegionWrapper.builder()
                    .regionList(Region.HIGHCOURT)
                    .build())
                .build())
            .build();

        GenericAboutToStartOrSubmitCallbackResponse<FinremCaseData> response =
            underTest.handle(FinremCallbackRequestFactory.from(caseData), AUTH_TOKEN);
        assertThat(response.getErrors()).containsExactly("You cannot select the High Court for a consent application.");
    }
}
