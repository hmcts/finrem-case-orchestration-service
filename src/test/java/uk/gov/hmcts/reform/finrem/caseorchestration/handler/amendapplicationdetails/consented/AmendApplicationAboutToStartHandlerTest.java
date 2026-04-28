package uk.gov.hmcts.reform.finrem.caseorchestration.handler.amendapplicationdetails.consented;

import com.google.common.collect.Lists;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.finrem.caseorchestration.FinremCallbackRequestFactory;
import uk.gov.hmcts.reform.finrem.caseorchestration.ccd.callback.CallbackType;
import uk.gov.hmcts.reform.finrem.caseorchestration.handler.FinremCallbackRequest;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.EventType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseData;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.Intention;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.NatureApplication;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.YesOrNo;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.mockito.Mockito.mock;
import static uk.gov.hmcts.reform.finrem.caseorchestration.TestConstants.AUTH_TOKEN;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseType.CONSENTED;
import static uk.gov.hmcts.reform.finrem.caseorchestration.test.Assertions.assertCanHandle;

@ExtendWith(MockitoExtension.class)
class AmendApplicationAboutToStartHandlerTest {

    @InjectMocks
    private AmendApplicationAboutToStartHandler handler;

    @Test
    void testCanHandle() {
        assertCanHandle(handler, CallbackType.ABOUT_TO_START, CONSENTED, EventType.AMEND_APP_DETAILS);
    }

    @Test
    void givenCase_whenIntendsToIsApplyToVary_thenShouldAddToNatureList() {
        FinremCallbackRequest callbackRequest = FinremCallbackRequestFactory.from();

        FinremCaseData data = callbackRequest.getCaseDetails().getData();
        data.getNatureApplicationWrapper().setNatureOfApplication2(Lists.newArrayList(
            NatureApplication.PENSION_SHARING_ORDER,
            NatureApplication.LUMP_SUM_ORDER));
        data.setApplicantIntendsTo(Intention.APPLY_TO_VARY);

        handler.handle(callbackRequest, AUTH_TOKEN);

        assertAll(
            () -> assertThat(data.getNatureApplicationWrapper().getNatureOfApplication2())
                .contains(
                    NatureApplication.VARIATION_ORDER,
                    NatureApplication.LUMP_SUM_ORDER,
                    NatureApplication.PENSION_SHARING_ORDER
                ),
            () -> assertThat(data).extracting(FinremCaseData::getCivilPartnership)
                .isEqualTo(YesOrNo.NO)
        );
    }

    @Test
    void givenCase_whenIntendsToIsNotApplyToVary_thenShouldNotDoAnything() {
        FinremCallbackRequest callbackRequest = FinremCallbackRequestFactory.from();

        FinremCaseData data = callbackRequest.getCaseDetails().getData();
        data.getNatureApplicationWrapper().setNatureOfApplication2(Lists.newArrayList(
            NatureApplication.PENSION_SHARING_ORDER,
            NatureApplication.LUMP_SUM_ORDER));
        data.setApplicantIntendsTo(Intention.APPLY_TO_COURT_FOR);

        handler.handle(callbackRequest, AUTH_TOKEN);

        assertAll(
            () -> assertThat(data.getNatureApplicationWrapper().getNatureOfApplication2())
                .containsOnly(
                    NatureApplication.LUMP_SUM_ORDER,
                    NatureApplication.PENSION_SHARING_ORDER
                ),
            () -> assertThat(data).extracting(FinremCaseData::getCivilPartnership)
                .isEqualTo(YesOrNo.NO)
        );
    }

    @Test
    void givenCase_whenNatureListIsEmptyAndIntendsToIsApplyToVary_thenShouldAddToNatureList() {
        FinremCallbackRequest callbackRequest = FinremCallbackRequestFactory.from();

        FinremCaseData data = callbackRequest.getCaseDetails().getData();
        data.setApplicantIntendsTo(Intention.APPLY_TO_VARY);

        handler.handle(callbackRequest, AUTH_TOKEN);

        assertAll(
            () -> assertThat(data.getNatureApplicationWrapper().getNatureOfApplication2())
                .containsOnly(
                    NatureApplication.VARIATION_ORDER
                ),
            () -> assertThat(data).extracting(FinremCaseData::getCivilPartnership)
                .isEqualTo(YesOrNo.NO)
        );
    }

    @Test
    void givenCase_whenCivilPartnershipAlreadyExists_thenShouldNotPrepopulateCivilPartnership() {
        FinremCallbackRequest callbackRequest = FinremCallbackRequestFactory.from();

        FinremCaseData data = callbackRequest.getCaseDetails().getData();
        YesOrNo mockedYerOrNo = mock(YesOrNo.class);
        data.setCivilPartnership(mockedYerOrNo);

        handler.handle(callbackRequest, AUTH_TOKEN);

        assertThat(data).extracting(FinremCaseData::getCivilPartnership)
            .isEqualTo(mockedYerOrNo);
    }
}
