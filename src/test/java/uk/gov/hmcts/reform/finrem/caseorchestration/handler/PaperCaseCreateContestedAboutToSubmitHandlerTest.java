package uk.gov.hmcts.reform.finrem.caseorchestration.handler;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.InOrder;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.FinremCallbackRequestFactory;
import uk.gov.hmcts.reform.finrem.caseorchestration.ccd.callback.CallbackType;
import uk.gov.hmcts.reform.finrem.caseorchestration.controllers.GenericAboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.finrem.caseorchestration.handler.solicitorcreatecase.mandatorydatavalidation.ApplicantSolicitorDetailsValidator;
import uk.gov.hmcts.reform.finrem.caseorchestration.mapper.FinremCaseDetailsMapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.EventType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseData;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.Organisation;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.OrganisationPolicy;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.State;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.YesOrNo;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.wrapper.ContactDetailsWrapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.CaseDataService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.CaseFlagsService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.IdamService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.express.ExpressCaseService;
import uk.gov.hmcts.reform.finrem.caseorchestration.utils.refuge.RefugeWrapperUtils;

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
import static uk.gov.hmcts.reform.finrem.caseorchestration.TestConstants.TEST_ORG2_ID;
import static uk.gov.hmcts.reform.finrem.caseorchestration.TestConstants.TEST_ORG_ID;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.APP_SOLICITOR_POLICY;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.RESP_SOLICITOR_POLICY;
import static uk.gov.hmcts.reform.finrem.caseorchestration.service.IntervenerServiceTest.CASE_ID;
import static uk.gov.hmcts.reform.finrem.caseorchestration.test.Assertions.assertCanHandle;

@ExtendWith(MockitoExtension.class)
class PaperCaseCreateContestedAboutToSubmitHandlerTest extends BaseHandlerTestSetup {

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

    @Spy
    private final FinremCaseDetailsMapper finremCaseDetailsMapper = new FinremCaseDetailsMapper(
        new ObjectMapper().registerModule(new JavaTimeModule())
    );

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
        FinremCallbackRequest callbackRequest = FinremCallbackRequestFactory.from(CASE_ID, CaseType.CONTESTED,
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

        CaseDetails oldCaseDetails = mock(CaseDetails.class);
        when(finremCaseDetailsMapper.mapToCaseDetails(callbackRequest.getCaseDetails())).thenReturn(oldCaseDetails);

        handler.handle(callbackRequest, AUTH_TOKEN);

        InOrder inOrder = Mockito.inOrder(finremCaseDetailsMapper, caseDataService);
        inOrder.verify(finremCaseDetailsMapper).mapToCaseDetails(callbackRequest.getCaseDetails());
        inOrder.verify(caseDataService).setFinancialRemediesCourtDetails(oldCaseDetails);
    }

    @Test
    void givenAnyCase_whenHandle_thenExpressCaseEnrollmentStatusSet() {
        FinremCallbackRequest callbackRequest = buildFinremCallbackRequest();

        CaseDetails oldCaseDetails = mock(CaseDetails.class);
        when(oldCaseDetails.getData()).thenReturn(Map.of());
        FinremCaseData caseData = mock(FinremCaseData.class);

        when(finremCaseDetailsMapper.mapToCaseDetails(callbackRequest.getCaseDetails())).thenReturn(oldCaseDetails);
        when(finremCaseDetailsMapper.mapToFinremCaseData(oldCaseDetails.getData())).thenReturn(caseData);

        handler.handle(callbackRequest, AUTH_TOKEN);

        InOrder inOrder = Mockito.inOrder(finremCaseDetailsMapper, expressCaseService);
        inOrder.verify(finremCaseDetailsMapper).mapToCaseDetails(callbackRequest.getCaseDetails());
        inOrder.verify(finremCaseDetailsMapper).mapToFinremCaseData(oldCaseDetails.getData());
        inOrder.verify(expressCaseService).setExpressCaseEnrollmentStatus(caseData);
    }

    @Test
    void givenAnyCase_whenHandle_thenValidateCaseDataAndPopulateErrors() {
        FinremCallbackRequest callbackRequest = buildFinremCallbackRequest();

        CaseDetails oldCaseDetails = mock(CaseDetails.class);
        when(oldCaseDetails.getData()).thenReturn(Map.of());
        FinremCaseData caseData = mock(FinremCaseData.class);

        when(finremCaseDetailsMapper.mapToCaseDetails(callbackRequest.getCaseDetails())).thenReturn(oldCaseDetails);
        when(finremCaseDetailsMapper.mapToFinremCaseData(oldCaseDetails.getData())).thenReturn(caseData);
        when(applicantSolicitorDetailsValidator.validate(caseData))
            .thenReturn(List.of("error1"));

        GenericAboutToStartOrSubmitCallbackResponse<FinremCaseData> handle = handler.handle(callbackRequest, AUTH_TOKEN);

        assertThat(handle.getErrors()).containsExactly("error1");

        InOrder inOrder = Mockito.inOrder(finremCaseDetailsMapper, applicantSolicitorDetailsValidator);
        inOrder.verify(finremCaseDetailsMapper).mapToCaseDetails(callbackRequest.getCaseDetails());
        inOrder.verify(finremCaseDetailsMapper).mapToFinremCaseData(oldCaseDetails.getData());
        inOrder.verify(applicantSolicitorDetailsValidator).validate(caseData);
    }

    private FinremCallbackRequest buildFinremCallbackRequest() {
        return buildFinremCallbackRequest(FinremCaseData.builder().build());
    }

    private FinremCallbackRequest buildFinremCallbackRequest(FinremCaseData finremCaseData) {
        return FinremCallbackRequestFactory.from(CASE_ID, CaseType.CONTESTED,
            finremCaseData, mock(State.class));
    }

    @ParameterizedTest
    @ValueSource(booleans = {true, false})
    void givenContestedCase_whenApplicantAndRespondentOrganisationPolicyAreTheSame_thenShowError(boolean happyPath) {
        FinremCallbackRequest callbackRequest = mock(FinremCallbackRequest.class);
        FinremCaseDetails finremCaseDetails = mock(FinremCaseDetails.class);
        when(callbackRequest.getCaseDetails()).thenReturn(finremCaseDetails);
        FinremCaseData finremCaseData = spy(FinremCaseData.class);
        when(finremCaseDetails.getState()).thenReturn(State.APPLICATION_SUBMITTED);
        when(finremCaseDetails.getData()).thenReturn(finremCaseData);
        when(finremCaseDetails.getCaseType()).thenReturn(CaseType.CONTESTED);

        when(finremCaseData.getApplicantOrganisationPolicy()).thenReturn(OrganisationPolicy
            .builder().organisation(Organisation.builder().organisationID(TEST_ORG_ID).build())
            .build());
        when(finremCaseData.getRespondentOrganisationPolicy()).thenReturn(OrganisationPolicy
            .builder().organisation(Organisation.builder().organisationID(happyPath ? TEST_ORG2_ID : TEST_ORG_ID).build())
            .build());

        GenericAboutToStartOrSubmitCallbackResponse<FinremCaseData> response = handler.handle(callbackRequest, AUTH_TOKEN);
        if (happyPath) {
            assertThat(response.getErrors()).isEmpty();
        } else {
            assertThat(response.getErrors()).contains("Solicitor can only represent one party.");
        }
    }
}
