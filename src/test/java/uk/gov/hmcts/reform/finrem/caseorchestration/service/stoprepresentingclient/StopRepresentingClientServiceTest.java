package uk.gov.hmcts.reform.finrem.caseorchestration.service.stoprepresentingclient;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.FinremCaseDetailsBuilderFactory;
import uk.gov.hmcts.reform.finrem.caseorchestration.mapper.FinremCaseDetailsMapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.BarristerChange;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.BarristerParty;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.ChangeOrganisationRequest;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseData;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.NoticeOfChangeParty;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.OrganisationPolicy;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.wrapper.IntervenerOne;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.wrapper.IntervenerTwo;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.AssignCaseAccessService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.IntervenerService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.SystemUserService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.barristers.BarristerChangeCaseAccessUpdater;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.barristers.ManageBarristerService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.ccd.CoreCaseDataService;

import java.util.Map;
import java.util.function.Function;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.finrem.caseorchestration.TestConstants.AUTH_TOKEN;
import static uk.gov.hmcts.reform.finrem.caseorchestration.TestConstants.CASE_ID;
import static uk.gov.hmcts.reform.finrem.caseorchestration.TestConstants.TEST_SYSTEM_TOKEN;
import static uk.gov.hmcts.reform.finrem.caseorchestration.TestSetUpUtils.organisation;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.EventType.INTERNAL_CHANGE_UPDATE_CASE;

@ExtendWith(MockitoExtension.class)
class StopRepresentingClientServiceTest {

    @Mock
    private AssignCaseAccessService assignCaseAccessService;

    @Mock
    private SystemUserService systemUserService;

    @Mock
    private FinremCaseDetailsMapper finremCaseDetailsMapper;

    @Mock
    private ManageBarristerService manageBarristerService;

    @Mock
    private BarristerChangeCaseAccessUpdater barristerChangeCaseAccessUpdater;

    @Mock
    private CoreCaseDataService coreCaseDataService;

    @Mock
    private IntervenerService intervenerService;

    private StopRepresentingClientService underTest;

    @BeforeEach
    void setup() {
        underTest = new StopRepresentingClientService(assignCaseAccessService, systemUserService, finremCaseDetailsMapper,
            manageBarristerService, barristerChangeCaseAccessUpdater, coreCaseDataService, intervenerService);
        lenient().when(systemUserService.getSysUserToken()).thenReturn(TEST_SYSTEM_TOKEN);
    }

    @Nested
    class IntervenerRepresentativeRequestTests {

        @Test
        void givenIntervenerOrganisationChange_whenHandled_thenShouldSendRequestToCaseAssigment() {
            FinremCaseData caseData = FinremCaseData.builder()
                .intervenerOne(IntervenerOne.builder()
                    .intervenerOrganisation(OrganisationPolicy.builder()
                        .organisation(organisation(null))
                        .build())
                    .build())
                .intervenerTwo(IntervenerTwo.builder()
                    .intervenerOrganisation(OrganisationPolicy.builder()
                        .organisation(organisation("BBB"))
                        .build())
                    .build())
                .build();

            // Setting original org policy
            IntervenerOne intervenerOne = IntervenerOne.builder()
                .intervenerOrganisation(OrganisationPolicy.builder()
                    .organisation(organisation("AAA"))
                    .build())
                .build();
            IntervenerTwo intervenerTwo = IntervenerTwo.builder()
                .intervenerOrganisation(OrganisationPolicy.builder()
                    .organisation(organisation("BBB"))
                    .build())
                .build();
            FinremCaseData caseDataBefore = FinremCaseData.builder()
                .intervenerOne(intervenerOne)
                .intervenerTwo(intervenerTwo)
                .build();

            FinremCaseDetails caseDetails = FinremCaseDetailsBuilderFactory.from(
                Long.valueOf(CASE_ID), mock(CaseType.class), caseData)
                .build();

            StopRepresentingClientInfo event = StopRepresentingClientInfo.builder()
                .invokedByIntervener(true)
                .caseDetails(caseDetails)
                .caseDetailsBefore(FinremCaseDetails.builder().data(caseDataBefore).build())
                .userAuthorisation(AUTH_TOKEN)
                .build();

            underTest.applyCaseAssignment(event);

            verify(intervenerService).revokeIntervener(Long.parseLong(CASE_ID), intervenerOne);
            verify(intervenerService, never()).revokeIntervener(Long.parseLong(CASE_ID), intervenerTwo);
            verifyNoMoreInteractions(intervenerService);
            verify(assignCaseAccessService, never()).applyDecision(eq(TEST_SYSTEM_TOKEN), any(CaseDetails.class));
        }

        @Test
        void givenAnyBarristerChange_whenHandled_thenUpdateBarristerChangeCaseAccess() {
            FinremCaseData caseData = spy(FinremCaseData.class);
            caseData.getContactDetailsWrapper().setNocParty(null);
            FinremCaseData caseDataBefore = mock(FinremCaseData.class);
            FinremCaseDetails caseDetails = FinremCaseDetailsBuilderFactory.from(Long.valueOf(CASE_ID), mock(CaseType.class), caseData)
                .build();

            StopRepresentingClientInfo event = StopRepresentingClientInfo.builder()
                .invokedByIntervener(true)
                .caseDetails(caseDetails)
                .caseDetailsBefore(FinremCaseDetails.builder().data(caseDataBefore).build())
                .userAuthorisation(AUTH_TOKEN)
                .build();
            BarristerChange applicantBarristerChange = mock(BarristerChange.class);
            when(manageBarristerService.getBarristerChange(event.getCaseDetails(), caseDataBefore, BarristerParty.APPLICANT))
                .thenReturn(applicantBarristerChange);
            BarristerChange respondentBarristerChange = mock(BarristerChange.class);
            when(manageBarristerService.getBarristerChange(event.getCaseDetails(), caseDataBefore, BarristerParty.RESPONDENT))
                .thenReturn(respondentBarristerChange);
            BarristerChange intv1BarristerChange = mock(BarristerChange.class);
            when(manageBarristerService.getBarristerChange(event.getCaseDetails(), caseDataBefore, BarristerParty.INTERVENER1))
                .thenReturn(intv1BarristerChange);
            BarristerChange intv2BarristerChange = mock(BarristerChange.class);
            when(manageBarristerService.getBarristerChange(event.getCaseDetails(), caseDataBefore, BarristerParty.INTERVENER2))
                .thenReturn(intv2BarristerChange);
            BarristerChange intv3BarristerChange = mock(BarristerChange.class);
            when(manageBarristerService.getBarristerChange(event.getCaseDetails(), caseDataBefore, BarristerParty.INTERVENER3))
                .thenReturn(intv3BarristerChange);
            BarristerChange intv4BarristerChange = mock(BarristerChange.class);
            when(manageBarristerService.getBarristerChange(event.getCaseDetails(), caseDataBefore, BarristerParty.INTERVENER4))
                .thenReturn(intv4BarristerChange);

            underTest.applyCaseAssignment(event);

            verify(barristerChangeCaseAccessUpdater).executeBarristerChange(Long.parseLong(CASE_ID), applicantBarristerChange);
            verify(barristerChangeCaseAccessUpdater).executeBarristerChange(Long.parseLong(CASE_ID), respondentBarristerChange);
            verify(barristerChangeCaseAccessUpdater).executeBarristerChange(Long.parseLong(CASE_ID), intv1BarristerChange);
            verify(barristerChangeCaseAccessUpdater).executeBarristerChange(Long.parseLong(CASE_ID), intv2BarristerChange);
            verify(barristerChangeCaseAccessUpdater).executeBarristerChange(Long.parseLong(CASE_ID), intv3BarristerChange);
            verify(barristerChangeCaseAccessUpdater).executeBarristerChange(Long.parseLong(CASE_ID), intv4BarristerChange);
        }
    }

    @Nested
    class ApplicantOrRespondentRepresentativeRequestTests {

        @Test
        void givenAnyBarristerChange_whenHandled_thenUpdateBarristerChangeCaseAccess() {
            FinremCaseData caseData = spy(FinremCaseData.class);
            caseData.getContactDetailsWrapper().setNocParty(mock(NoticeOfChangeParty.class));
            FinremCaseData caseDataBefore = mock(FinremCaseData.class);
            FinremCaseDetails caseDetails = FinremCaseDetailsBuilderFactory.from(Long.valueOf(CASE_ID), mock(CaseType.class), caseData)
                .build();

            StopRepresentingClientInfo event = StopRepresentingClientInfo.builder()
                .caseDetails(caseDetails)
                .caseDetailsBefore(FinremCaseDetails.builder().data(caseDataBefore).build())
                .userAuthorisation(AUTH_TOKEN)
                .build();
            BarristerChange applicantBarristerChange = mock(BarristerChange.class);
            when(manageBarristerService.getBarristerChange(event.getCaseDetails(), caseDataBefore, BarristerParty.APPLICANT))
                .thenReturn(applicantBarristerChange);
            BarristerChange respondentBarristerChange = mock(BarristerChange.class);
            when(manageBarristerService.getBarristerChange(event.getCaseDetails(), caseDataBefore, BarristerParty.RESPONDENT))
                .thenReturn(respondentBarristerChange);
            BarristerChange intv1BarristerChange = mock(BarristerChange.class);
            when(manageBarristerService.getBarristerChange(event.getCaseDetails(), caseDataBefore, BarristerParty.INTERVENER1))
                .thenReturn(intv1BarristerChange);
            BarristerChange intv2BarristerChange = mock(BarristerChange.class);
            when(manageBarristerService.getBarristerChange(event.getCaseDetails(), caseDataBefore, BarristerParty.INTERVENER2))
                .thenReturn(intv2BarristerChange);
            BarristerChange intv3BarristerChange = mock(BarristerChange.class);
            when(manageBarristerService.getBarristerChange(event.getCaseDetails(), caseDataBefore, BarristerParty.INTERVENER3))
                .thenReturn(intv3BarristerChange);
            BarristerChange intv4BarristerChange = mock(BarristerChange.class);
            when(manageBarristerService.getBarristerChange(event.getCaseDetails(), caseDataBefore, BarristerParty.INTERVENER4))
                .thenReturn(intv4BarristerChange);

            underTest.applyCaseAssignment(event);

            verify(barristerChangeCaseAccessUpdater).executeBarristerChange(Long.parseLong(CASE_ID), applicantBarristerChange);
            verify(barristerChangeCaseAccessUpdater).executeBarristerChange(Long.parseLong(CASE_ID), respondentBarristerChange);
            verify(barristerChangeCaseAccessUpdater).executeBarristerChange(Long.parseLong(CASE_ID), intv1BarristerChange);
            verify(barristerChangeCaseAccessUpdater).executeBarristerChange(Long.parseLong(CASE_ID), intv2BarristerChange);
            verify(barristerChangeCaseAccessUpdater).executeBarristerChange(Long.parseLong(CASE_ID), intv3BarristerChange);
            verify(barristerChangeCaseAccessUpdater).executeBarristerChange(Long.parseLong(CASE_ID), intv4BarristerChange);
        }

        @ParameterizedTest
        @ValueSource(booleans = {true, false})
        void givenChangeOrganisationRequestFieldSet_whenHandled_thenShouldSendRevertedCaseDetailsToAssignCaseAccessService(boolean isApplicant) {
            FinremCaseData caseData = spy(FinremCaseData.class);
            caseData.getContactDetailsWrapper().setNocParty(isApplicant ? NoticeOfChangeParty.APPLICANT : NoticeOfChangeParty.RESPONDENT);
            caseData.setChangeOrganisationRequestField(spy(ChangeOrganisationRequest.class));

            // Setting original org policy
            FinremCaseData caseDataBefore = mock(FinremCaseData.class);
            OrganisationPolicy originalOrgPolicy = mock(OrganisationPolicy.class);
            if (isApplicant) {
                when(caseDataBefore.getApplicantOrganisationPolicy()).thenReturn(originalOrgPolicy);
            } else {
                when(caseDataBefore.getRespondentOrganisationPolicy()).thenReturn(originalOrgPolicy);
            }

            FinremCaseDetails caseDetails = FinremCaseDetailsBuilderFactory.from(
                    Long.valueOf(CASE_ID), mock(CaseType.class), caseData)
                .build();

            StopRepresentingClientInfo event = StopRepresentingClientInfo.builder()
                .invokedByIntervener(false)
                .caseDetails(caseDetails)
                .caseDetailsBefore(FinremCaseDetails.builder().data(caseDataBefore).build())
                .userAuthorisation(AUTH_TOKEN)
                .build();

            // Setting up invalid case details
            CaseDetails mockInvalidCaseDetails = mock(CaseDetails.class);
            lenient().when(finremCaseDetailsMapper.mapToCaseDetails(any(FinremCaseDetails.class)))
                .thenReturn(mockInvalidCaseDetails);

            // Setting up valid case details
            // verifying original applicant/respondent org policy should be set to finremCaseData
            CaseDetails mockValidCaseDetails = mock(CaseDetails.class);
            when(finremCaseDetailsMapper.mapToCaseDetails(argThat(cd
                -> getOrganisationPolicy(cd.getData(), isApplicant).equals(originalOrgPolicy)
            ))).thenReturn(mockValidCaseDetails);

            underTest.applyCaseAssignment(event);

            verify(assignCaseAccessService).applyDecision(TEST_SYSTEM_TOKEN, mockValidCaseDetails);
            verify(assignCaseAccessService, never()).applyDecision(TEST_SYSTEM_TOKEN, mockInvalidCaseDetails);
            verifyNoInteractions(intervenerService);
        }

        @ParameterizedTest
        @ValueSource(booleans = {true, false})
        void givenChangeOrganisationRequestFieldNotPopulated_whenHandled_thenShouldNotInteractWithAssignCaseAccessService(boolean isApplicant) {
            FinremCaseData caseData = spy(FinremCaseData.class);
            caseData.getContactDetailsWrapper().setNocParty(isApplicant ? NoticeOfChangeParty.APPLICANT : NoticeOfChangeParty.RESPONDENT);
            caseData.setChangeOrganisationRequestField(null);

            // Setting original org policy
            FinremCaseDetails caseDetails = FinremCaseDetailsBuilderFactory.from(
                    Long.valueOf(CASE_ID), mock(CaseType.class), caseData)
                .build();

            StopRepresentingClientInfo event = StopRepresentingClientInfo.builder()
                .invokedByIntervener(false)
                .caseDetails(caseDetails)
                .caseDetailsBefore(FinremCaseDetails.builder().data(mock(FinremCaseData.class)).build())
                .userAuthorisation(AUTH_TOKEN)
                .build();

            underTest.applyCaseAssignment(event);

            verify(assignCaseAccessService, never()).applyDecision(eq(TEST_SYSTEM_TOKEN), any(CaseDetails.class));
            verifyNoInteractions(intervenerService);
        }

        @ParameterizedTest
        @ValueSource(booleans = {true, false})
        void givenNocRequestSent_whenHandled_thenShouldResetChangeOrganisationField(boolean isApplicant) {
            CaseType caseType = mock(CaseType.class);
            FinremCaseData caseData = spy(FinremCaseData.class);
            caseData.getContactDetailsWrapper().setNocParty(isApplicant ? NoticeOfChangeParty.APPLICANT : NoticeOfChangeParty.RESPONDENT);
            caseData.setChangeOrganisationRequestField(mock(ChangeOrganisationRequest.class));

            FinremCaseData caseDataBefore = mock(FinremCaseData.class);
            FinremCaseDetails caseDetails = FinremCaseDetailsBuilderFactory.from(Long.valueOf(CASE_ID), caseType, caseData)
                .build();

            StopRepresentingClientInfo event = StopRepresentingClientInfo.builder()
                .invokedByIntervener(false)
                .caseDetails(caseDetails)
                .caseDetailsBefore(FinremCaseDetails.builder().data(caseDataBefore).build())
                .userAuthorisation(AUTH_TOKEN)
                .build();

            when(finremCaseDetailsMapper.mapToCaseDetails(caseDetails)).thenReturn(mock(CaseDetails.class));

            underTest.applyCaseAssignment(event);

            ArgumentCaptor<Function<CaseDetails, Map<String, Object>>> captor = ArgumentCaptor.forClass(Function.class);
            verify(coreCaseDataService).performPostSubmitCallback(eq(caseType), eq(Long.valueOf(CASE_ID)),
                eq(INTERNAL_CHANGE_UPDATE_CASE.getCcdType()), captor.capture());
            verify(assignCaseAccessService).applyDecision(eq(TEST_SYSTEM_TOKEN), any(CaseDetails.class));

            Function<CaseDetails, Map<String, Object>> fn = captor.getValue();
            CaseDetails ccdCaseDetails = mock(CaseDetails.class);
            assertThat(fn.apply(ccdCaseDetails))
                .containsKey("changeOrganisationRequestField")
                .extractingByKey("changeOrganisationRequestField")
                .isNull();
            verifyNoInteractions(intervenerService);
        }
    }

    private OrganisationPolicy getOrganisationPolicy(FinremCaseData caseData, boolean isApplicant) {
        return isApplicant ? caseData.getApplicantOrganisationPolicy() :
            caseData.getRespondentOrganisationPolicy();
    }
}
