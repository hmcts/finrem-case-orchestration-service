package uk.gov.hmcts.reform.finrem.caseorchestration.handler.updatecasedetailssolicitor;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.finrem.caseorchestration.FinremCallbackRequestFactory;
import uk.gov.hmcts.reform.finrem.caseorchestration.controllers.GenericAboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.finrem.caseorchestration.handler.FinremCallbackRequest;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseData;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.YesOrNo;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.wrapper.ContactDetailsWrapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.AssignPartiesAccessService;

import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static uk.gov.hmcts.reform.finrem.caseorchestration.TestConstants.AUTH_TOKEN;
import static uk.gov.hmcts.reform.finrem.caseorchestration.TestConstants.CASE_ID;
import static uk.gov.hmcts.reform.finrem.caseorchestration.ccd.callback.CallbackType.ABOUT_TO_SUBMIT;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.EventType.UPDATE_CASE_DETAILS_SOLICITOR;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseType.CONTESTED;
import static uk.gov.hmcts.reform.finrem.caseorchestration.test.Assertions.assertCanHandle;

@ExtendWith(MockitoExtension.class)
class UpdateCaseDetailsSolicitorAboutToSubmitHandlerTest {

    @Mock
    private AssignPartiesAccessService assignPartiesAccessService;
    @InjectMocks
    private UpdateCaseDetailsSolicitorAboutToSubmitHandler handler;

    @Test
    void testCanHandle() {
        assertCanHandle(handler, ABOUT_TO_SUBMIT, CONTESTED, UPDATE_CASE_DETAILS_SOLICITOR);
    }

    static Stream<Arguments> provideSolicitorEmailChangeScenarios() {
        return Stream.of(
            Arguments.of(
                "newSolicitor@email.com", YesOrNo.YES, "oldSolicitor@email.com", YesOrNo.YES,
                true, true, false // grant, revoke, neverRevoke
            ),
            Arguments.of(
                "unchanged@email.com", YesOrNo.YES, "unchanged@email.com", YesOrNo.YES,
                false, false, true
            ),
            Arguments.of(
                "newSolicitor@email.com", YesOrNo.YES, null, YesOrNo.NO,
                true, false, true
            ),
            Arguments.of(
                null, YesOrNo.NO, "oldSolicitor@email.com", YesOrNo.YES,
                false, true, false
            )
        );
    }

    @ParameterizedTest
    @MethodSource("provideSolicitorEmailChangeScenarios")
    void handleSolicitorEmailChangeScenarios(String applicantSolicitorEmail, YesOrNo applicantRepresented,
                                             String beforeApplicantSolicitorEmail, YesOrNo beforeApplicantRepresented,
                                             boolean shouldGrant, boolean shouldRevoke, boolean neverRevoke) {
        FinremCaseData caseData = FinremCaseData.builder().contactDetailsWrapper(
            ContactDetailsWrapper.builder()
                .applicantSolicitorEmail(applicantSolicitorEmail)
                .applicantRepresented(applicantRepresented)
                .build()).build();

        FinremCaseData caseDataBefore = FinremCaseData.builder().contactDetailsWrapper(
            ContactDetailsWrapper.builder()
                .applicantSolicitorEmail(beforeApplicantSolicitorEmail)
                .applicantRepresented(beforeApplicantRepresented)
                .build()).build();

        FinremCallbackRequest callbackRequest =
            FinremCallbackRequestFactory.from(Long.valueOf(CASE_ID), CONTESTED, UPDATE_CASE_DETAILS_SOLICITOR, caseData, caseDataBefore);

        final GenericAboutToStartOrSubmitCallbackResponse<FinremCaseData> response = handler.handle(callbackRequest, AUTH_TOKEN);
        if (shouldGrant) {
            verify(assignPartiesAccessService).grantApplicantSolicitor(caseData);
        } else {
            verify(assignPartiesAccessService, never()).grantApplicantSolicitor(caseData);
        }
        if (shouldRevoke) {
            verify(assignPartiesAccessService).revokeApplicantSolicitor(CASE_ID, caseDataBefore);
        }
        if (neverRevoke) {
            verify(assignPartiesAccessService, never()).revokeApplicantSolicitor(anyString(), any());
        }
        assertThat(response.getData()).isEqualTo(caseData);
    }
}
