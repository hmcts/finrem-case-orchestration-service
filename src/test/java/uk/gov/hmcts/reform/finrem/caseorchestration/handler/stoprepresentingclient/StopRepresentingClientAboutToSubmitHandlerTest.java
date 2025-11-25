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
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.wrapper.StopRepresentationWrapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.CaseRoleService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.UpdateContactDetailsService;
import uk.gov.hmcts.reform.finrem.caseorchestration.util.TestLogger;
import uk.gov.hmcts.reform.finrem.caseorchestration.util.TestLogs;

import static java.lang.String.format;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.finrem.caseorchestration.TestConstants.AUTH_TOKEN;
import static uk.gov.hmcts.reform.finrem.caseorchestration.TestConstants.CASE_ID;
import static uk.gov.hmcts.reform.finrem.caseorchestration.ccd.callback.CallbackType.ABOUT_TO_SUBMIT;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.EventType.STOP_REPRESENTING_CLIENT;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseType.CONSENTED;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseType.CONTESTED;
import static uk.gov.hmcts.reform.finrem.caseorchestration.test.Assertions.assertCanHandle;

@ExtendWith(MockitoExtension.class)
class StopRepresentingClientAboutToSubmitHandlerTest {

    @TestLogs
    private final TestLogger logs = new TestLogger(StopRepresentingClientAboutToSubmitHandler.class);

    private StopRepresentingClientAboutToSubmitHandler underTest;

    @Mock
    private FinremCaseDetailsMapper finremCaseDetailsMapper;

    @Mock
    private UpdateContactDetailsService updateContactDetailsService;

    @Mock
    private CaseRoleService caseRoleService;

    @BeforeEach
    public void setup() {
        underTest = new StopRepresentingClientAboutToSubmitHandler(finremCaseDetailsMapper, updateContactDetailsService,
            caseRoleService);
    }

    @Test
    void testCanHandle() {
        assertCanHandle(underTest,
            Arguments.of(ABOUT_TO_SUBMIT, CONTESTED, STOP_REPRESENTING_CLIENT),
            Arguments.of(ABOUT_TO_SUBMIT, CONSENTED, STOP_REPRESENTING_CLIENT));
    }

    @ParameterizedTest
    @ValueSource(booleans = {true, false})
    void givenHavingClientConsent_whenHandled_thenWarningsPopulated(boolean isApplicantSolicitor) {
        when(caseRoleService.isLoginWithApplicantSolicitor(any(FinremCaseData.class), eq(AUTH_TOKEN))).thenReturn(isApplicantSolicitor);
        FinremCaseData caseData = FinremCaseData.builder()
            .stopRepresentationWrapper(StopRepresentationWrapper.builder()
                .stopRepClientConsent(YesOrNo.YES)
                .build())
            .build();

        FinremCallbackRequest request = FinremCallbackRequestFactory.from(Long.valueOf(CASE_ID), caseData);
        assertThat(underTest.handle(request, AUTH_TOKEN).getWarnings()).containsExactly(
            "Are you sure you wish to stop representing your client? "
                + "If you continue your access to this access will be removed"
        );
        assertThat(logs.getInfos()).hasSize(2).contains(format(
            format("%s - %s solicitor stops representing a client with a client consent", CASE_ID,
                isApplicantSolicitor ? "applicant" : "respondent")));
    }

    @ParameterizedTest
    @ValueSource(booleans = {true, false})
    void givenHavingJudicialApproval_whenHandled_thenWarningsPopulated(boolean isApplicantSolicitor) {
        when(caseRoleService.isLoginWithApplicantSolicitor(any(FinremCaseData.class), eq(AUTH_TOKEN))).thenReturn(isApplicantSolicitor);
        FinremCaseData caseData = FinremCaseData.builder()
            .stopRepresentationWrapper(StopRepresentationWrapper.builder()
                .stopRepJudicialApproval(YesOrNo.YES)
                .build())
            .build();

        FinremCallbackRequest request = FinremCallbackRequestFactory.from(Long.valueOf(CASE_ID), caseData);
        assertThat(underTest.handle(request, AUTH_TOKEN).getWarnings()).containsExactly(
            "Are you sure you wish to stop representing your client? "
                + "If you continue your access to this access will be removed"
        );
        assertThat(logs.getInfos()).hasSize(2).contains(format(
            format("%s - %s solicitor stops representing a client with a judicial approval", CASE_ID,
                isApplicantSolicitor ? "applicant" : "respondent")));
    }

    @ParameterizedTest
    @ValueSource(booleans = {true, false})
    void givenNoJudicialApprovalOrClientConsent_whenHandled_thenThrowIllegalStateException(boolean isApplicantSolicitor) {
        lenient().when(caseRoleService.isLoginWithApplicantSolicitor(any(FinremCaseData.class), eq(AUTH_TOKEN))).thenReturn(isApplicantSolicitor);
        FinremCaseData caseData = FinremCaseData.builder()
            .stopRepresentationWrapper(StopRepresentationWrapper.builder().build())
            .build();

        FinremCallbackRequest request = FinremCallbackRequestFactory.from(Long.valueOf(CASE_ID), caseData);
        assertThatThrownBy(() -> underTest.handle(request, AUTH_TOKEN).getWarnings())
            .hasMessage("Client consent or judicial approval is required but missing.");
        assertThat(logs.getInfos()).doesNotContain(
            format("%s - applicant solicitor stops representing a client with a judicial approval", CASE_ID),
            format("%s - respondent solicitor stops representing a client with a judicial approval", CASE_ID),
            format("%s - applicant solicitor stops representing a client with a client consent", CASE_ID),
            format("%s - respondent solicitor stops representing a client with a client consent", CASE_ID)
        );
    }
}
