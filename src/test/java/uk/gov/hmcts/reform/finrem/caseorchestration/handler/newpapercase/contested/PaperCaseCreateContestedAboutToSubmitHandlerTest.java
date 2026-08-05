package uk.gov.hmcts.reform.finrem.caseorchestration.handler.newpapercase.contested;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.finrem.caseorchestration.FinremCallbackRequestFactory;
import uk.gov.hmcts.reform.finrem.caseorchestration.ccd.callback.CallbackType;
import uk.gov.hmcts.reform.finrem.caseorchestration.controllers.GenericAboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.finrem.caseorchestration.handler.FinremCallbackRequest;
import uk.gov.hmcts.reform.finrem.caseorchestration.handler.solicitorcreatecase.mandatorydatavalidation.ApplicantSolicitorDetailsValidator;
import uk.gov.hmcts.reform.finrem.caseorchestration.helper.ContactDetailsValidator;
import uk.gov.hmcts.reform.finrem.caseorchestration.mapper.FinremCaseDetailsMapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.EstimatedAssetsChecklistVersion;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.EventType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseData;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.OrganisationPolicy;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.State;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.YesOrNo;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.wrapper.ContactDetailsWrapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.CaseDataService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.CaseFlagsService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.IdamService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.express.ExpressCaseService;
import uk.gov.hmcts.reform.finrem.caseorchestration.utils.refuge.RefugeWrapperUtils;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.finrem.caseorchestration.OrchestrationConstants.NO_VALUE;
import static uk.gov.hmcts.reform.finrem.caseorchestration.OrchestrationConstants.YES_VALUE;
import static uk.gov.hmcts.reform.finrem.caseorchestration.TestConstants.AUTH_TOKEN;
import static uk.gov.hmcts.reform.finrem.caseorchestration.TestConstants.CASE_ID_IN_LONG;
import static uk.gov.hmcts.reform.finrem.caseorchestration.TestSetUpUtils.verifyTemporaryFieldsWereSanitised;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.APP_SOLICITOR_POLICY;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.RESP_SOLICITOR_POLICY;
import static uk.gov.hmcts.reform.finrem.caseorchestration.test.Assertions.assertCanHandle;

@ExtendWith(MockitoExtension.class)
class PaperCaseCreateContestedAboutToSubmitHandlerTest {

    @Mock
    private IdamService idamService;
    @Mock
    private CaseFlagsService caseFlagsService;
    @Mock
    private CaseDataService caseDataService;
    @Mock
    private ExpressCaseService expressCaseService;
    @Mock
    private ApplicantSolicitorDetailsValidator applicantSolicitorDetailsValidator;

    @Mock
    private FinremCaseDetailsMapper finremCaseDetailsMapper;

    @InjectMocks
    private PaperCaseCreateContestedAboutToSubmitHandler handler;

    @Test
    void canHandle() {
        assertCanHandle(handler, CallbackType.ABOUT_TO_SUBMIT, CaseType.CONTESTED, EventType.NEW_PAPER_CASE);
    }

    @Test
    void givenAnyCase_whenHandle_thenShouldSetCaseFlagInformation() {
        FinremCallbackRequest callbackRequest = buildFinremCallbackRequest();
        handler.handle(callbackRequest, AUTH_TOKEN);
        verify(caseFlagsService).setCaseFlagInformation(callbackRequest.getCaseDetails());
    }

    @Test
    void givenAnyCase_whenHandle_thenShouldSetPaperApplicationYes() {
        FinremCallbackRequest callbackRequest = FinremCallbackRequestFactory.from(CASE_ID_IN_LONG, CaseType.CONTESTED,
            FinremCaseData.builder().build(), mock(State.class));

        assertThat(handler.handle(callbackRequest, AUTH_TOKEN).getData().getPaperApplication())
            .isEqualTo(YesOrNo.YES);
    }

    @Test
    void givenUserIsAdmin_whenHandle_thenSetIsAdminYes() {
        FinremCallbackRequest callbackRequest = buildFinremCallbackRequest();

        when(idamService.isUserRoleAdmin(AUTH_TOKEN)).thenReturn(true);

        GenericAboutToStartOrSubmitCallbackResponse<FinremCaseData> handle = handler.handle(callbackRequest, AUTH_TOKEN);

        assertThat(handle.getData().getContactDetailsWrapper()).extracting(ContactDetailsWrapper::getIsAdmin)
            .isEqualTo(YES_VALUE);
        verify(idamService).isUserRoleAdmin(AUTH_TOKEN);
    }

    @Test
    void givenUserIsNotAdmin_whenHandle_thenSetIsAdminNo() {
        FinremCallbackRequest callbackRequest = buildFinremCallbackRequest();

        when(idamService.isUserRoleAdmin(AUTH_TOKEN)).thenReturn(false);

        GenericAboutToStartOrSubmitCallbackResponse<FinremCaseData> handle = handler.handle(callbackRequest, AUTH_TOKEN);

        assertThat(handle.getData().getContactDetailsWrapper())
            .extracting(ContactDetailsWrapper::getIsAdmin, ContactDetailsWrapper::getApplicantRepresented)
            .containsExactly(NO_VALUE, YesOrNo.YES);
        verify(idamService).isUserRoleAdmin(AUTH_TOKEN);
    }

    @Test
    void givenFastTrackDecisionIsNotSet_whenHandle_thenSetFastTrackDecisionNo() {
        FinremCallbackRequest callbackRequest = buildFinremCallbackRequest();

        GenericAboutToStartOrSubmitCallbackResponse<FinremCaseData> handle = handler.handle(callbackRequest, AUTH_TOKEN);

        assertThat(handle.getData().getFastTrackDecision())
            .isEqualTo(YesOrNo.NO);
    }

    @ParameterizedTest
    @EnumSource(value = YesOrNo.class)
    void givenFastTrackDecisionSet_whenHandle_thenFastTrackDecisionIsKept(YesOrNo fastTrackDecision) {
        FinremCallbackRequest callbackRequest = buildFinremCallbackRequest(FinremCaseData.builder()
            .fastTrackDecision(fastTrackDecision)
            .build());

        GenericAboutToStartOrSubmitCallbackResponse<FinremCaseData> handle = handler.handle(callbackRequest, AUTH_TOKEN);

        assertThat(handle.getData().getFastTrackDecision())
            .isEqualTo(fastTrackDecision);
    }

    @Test
    void givenApplicantNotRepresentedByASolicitor_whenHandle_thenApplicantOrganisationPolicySet() {
        FinremCaseData spiedFinremCaseData = spy(FinremCaseData.class);
        when(spiedFinremCaseData.isApplicantRepresentedByASolicitor()).thenReturn(false);

        FinremCallbackRequest callbackRequest = buildFinremCallbackRequest(spiedFinremCaseData);

        GenericAboutToStartOrSubmitCallbackResponse<FinremCaseData> handle = handler.handle(callbackRequest, AUTH_TOKEN);

        assertThat(handle.getData().getApplicantOrganisationPolicy())
            .isEqualTo(OrganisationPolicy.builder()
                .orgPolicyCaseAssignedRole(APP_SOLICITOR_POLICY)
                .build());
    }

    @Test
    void givenApplicantRepresentedByASolicitor_whenHandle_thenApplicantOrganisationPolicyIsNotSet() {
        FinremCaseData spiedFinremCaseData = spy(FinremCaseData.class);
        when(spiedFinremCaseData.isApplicantRepresentedByASolicitor()).thenReturn(true);

        FinremCallbackRequest callbackRequest = buildFinremCallbackRequest(spiedFinremCaseData);

        GenericAboutToStartOrSubmitCallbackResponse<FinremCaseData> handle = handler.handle(callbackRequest, AUTH_TOKEN);

        assertThat(handle.getData().getApplicantOrganisationPolicy())
            .isNull();
    }

    @Test
    void givenRespondentNotRepresentedByASolicitor_whenHandle_thenRespondentOrganisationPolicySet() {
        FinremCaseData spiedFinremCaseData = spy(FinremCaseData.class);
        when(spiedFinremCaseData.isRespondentRepresentedByASolicitor()).thenReturn(false);

        FinremCallbackRequest callbackRequest = buildFinremCallbackRequest(spiedFinremCaseData);

        GenericAboutToStartOrSubmitCallbackResponse<FinremCaseData> handle = handler.handle(callbackRequest, AUTH_TOKEN);

        assertThat(handle.getData().getRespondentOrganisationPolicy())
            .isEqualTo(OrganisationPolicy.builder()
                .orgPolicyCaseAssignedRole(RESP_SOLICITOR_POLICY)
                .build());
    }

    @Test
    void givenRespondentRepresentedByASolicitor_whenHandle_thenRespondentOrganisationPolicyIsNotSet() {
        FinremCaseData spiedFinremCaseData = spy(FinremCaseData.class);
        when(spiedFinremCaseData.isRespondentRepresentedByASolicitor()).thenReturn(true);

        FinremCallbackRequest callbackRequest = buildFinremCallbackRequest(spiedFinremCaseData);

        GenericAboutToStartOrSubmitCallbackResponse<FinremCaseData> handle = handler.handle(callbackRequest, AUTH_TOKEN);

        assertThat(handle.getData().getRespondentOrganisationPolicy())
            .isNull();
    }

    @Test
    void givenAnyCase_whenHandle_thenUpdateRefugeTabs() {
        FinremCallbackRequest callbackRequest = buildFinremCallbackRequest();

        try (MockedStatic<RefugeWrapperUtils> mockedStatic = mockStatic(RefugeWrapperUtils.class)) {
            handler.handle(callbackRequest, AUTH_TOKEN);
            mockedStatic.verify(() -> RefugeWrapperUtils.updateRespondentInRefugeTab(callbackRequest.getCaseDetails()));
            mockedStatic.verify(() -> RefugeWrapperUtils.updateApplicantInRefugeTab(callbackRequest.getCaseDetails()));
        }
    }

    @Test
    void givenAnyCase_whenHandle_thenFinancialRemediesCourtDetailsSet() {
        FinremCallbackRequest callbackRequest = buildFinremCallbackRequest();

        handler.handle(callbackRequest, AUTH_TOKEN);

        verify(caseDataService).setFinancialRemediesCourtDetails(callbackRequest.getCaseDetails());
    }

    @Test
    void givenAnyCase_whenHandle_thenExpressCaseEnrollmentStatusSet() {
        FinremCallbackRequest callbackRequest = buildFinremCallbackRequest();

        handler.handle(callbackRequest, AUTH_TOKEN);

        verify(expressCaseService).setExpressCaseEnrollmentStatus(callbackRequest.getFinremCaseData());
    }

    @Test
    void givenAnyCase_whenHandle_thenValidateCaseDataAndPopulateErrors() {
        FinremCallbackRequest callbackRequest = buildFinremCallbackRequest();

        when(applicantSolicitorDetailsValidator.validate(callbackRequest.getFinremCaseData()))
            .thenReturn(List.of("error1"));

        var handle = handler.handle(callbackRequest, AUTH_TOKEN);

        assertThat(handle.getErrors()).containsExactly("error1");

        verify(applicantSolicitorDetailsValidator).validate(callbackRequest.getFinremCaseData());
    }

    private FinremCallbackRequest buildFinremCallbackRequest() {
        return buildFinremCallbackRequest(FinremCaseData.builder().build());
    }

    private FinremCallbackRequest buildFinremCallbackRequest(FinremCaseData finremCaseData) {
        return FinremCallbackRequestFactory.from(CASE_ID_IN_LONG, CaseType.CONTESTED,
            finremCaseData, mock(State.class));
    }

    @ParameterizedTest
    @ValueSource(booleans = {true, false})
    void givenContestedCase_whenApplicantAndRespondentOrganisationPolicyAreTheSame_thenShowError(boolean happyPath) {
        FinremCallbackRequest callbackRequest = FinremCallbackRequestFactory.from();

        try (MockedStatic<ContactDetailsValidator> mockedStatic = mockStatic(ContactDetailsValidator.class)) {
            mockedStatic.when(() -> ContactDetailsValidator.validateOrganisationPolicy(callbackRequest.getFinremCaseData()))
                .thenReturn(happyPath ? List.of() : List.of("Solicitor can only represent one party."));

            var response = handler.handle(callbackRequest, AUTH_TOKEN);
            if (happyPath) {
                assertThat(response.getErrors()).isEmpty();
            } else {
                assertThat(response.getErrors()).contains("Solicitor can only represent one party.");
            }
        }
    }

    /*
     * Any value can be used in place of listVersion, at the time of writing.
     * Just preferred to use correct enums, to cover logic changing later.
     */
    @ParameterizedTest
    @EnumSource(value = EstimatedAssetsChecklistVersion.class)
    void givenCaseDataWithTemporaryEstimatedAssetsChecklistVersion_whenHandle_thenTemporaryFieldSanitised(
        EstimatedAssetsChecklistVersion listVersion) {
        verifyTemporaryFieldsWereSanitised(handler,
            finremCaseDetailsMapper, new HashMap<>(Map.of(
                "estimatedAssetsChecklistVersion", listVersion
            ))
        );
    }
}
