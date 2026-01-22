package uk.gov.hmcts.reform.finrem.caseorchestration.service.stoprepresentingclient;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.FinremCaseDetailsBuilderFactory;
import uk.gov.hmcts.reform.finrem.caseorchestration.TestConstants;
import uk.gov.hmcts.reform.finrem.caseorchestration.mapper.FinremCaseDetailsMapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.mapper.notificationrequest.FinremNotificationRequestMapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.BarristerChange;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.Barrister;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.BarristerCollectionItem;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.BarristerParty;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseRole;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.ChangeOrganisationRequest;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseData;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.NoticeOfChangeParty;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.Organisation;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.OrganisationPolicy;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.YesOrNo;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.wrapper.BarristerCollectionWrapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.wrapper.IntervenerFour;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.wrapper.IntervenerOne;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.wrapper.IntervenerTwo;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.notification.NotificationRequest;
import uk.gov.hmcts.reform.finrem.caseorchestration.notifications.domain.EmailTemplateNames;
import uk.gov.hmcts.reform.finrem.caseorchestration.notifications.notifiers.NotificationParty;
import uk.gov.hmcts.reform.finrem.caseorchestration.notifications.notifiers.SendCorrespondenceEvent;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.AssignCaseAccessService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.CaseRoleService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.IdamService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.IntervenerService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.SystemUserService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.barristers.BarristerChangeCaseAccessUpdater;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.barristers.ManageBarristerService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.ccd.CoreCaseDataService;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.finrem.caseorchestration.TestConstants.AUTH_TOKEN;
import static uk.gov.hmcts.reform.finrem.caseorchestration.TestConstants.CASE_ID;
import static uk.gov.hmcts.reform.finrem.caseorchestration.TestConstants.TEST_SYSTEM_TOKEN;
import static uk.gov.hmcts.reform.finrem.caseorchestration.TestConstants.TEST_USER_ID;
import static uk.gov.hmcts.reform.finrem.caseorchestration.TestSetUpUtils.organisation;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.EventType.INTERNAL_CHANGE_UPDATE_CASE;
import static uk.gov.hmcts.reform.finrem.caseorchestration.notifications.domain.EmailTemplateNames.FR_CONSENTED_REPRESENTATIVE_STOP_REPRESENTING_APPLICANT;
import static uk.gov.hmcts.reform.finrem.caseorchestration.notifications.domain.EmailTemplateNames.FR_CONSENTED_REPRESENTATIVE_STOP_REPRESENTING_RESPONDENT;
import static uk.gov.hmcts.reform.finrem.caseorchestration.notifications.domain.EmailTemplateNames.FR_CONTESTED_REPRESENTATIVE_STOP_REPRESENTING_APPLICANT;
import static uk.gov.hmcts.reform.finrem.caseorchestration.notifications.domain.EmailTemplateNames.FR_CONTESTED_REPRESENTATIVE_STOP_REPRESENTING_RESPONDENT;
import static uk.gov.hmcts.reform.finrem.caseorchestration.service.stoprepresentingclient.IntervenerRole.BARRISTER;
import static uk.gov.hmcts.reform.finrem.caseorchestration.service.stoprepresentingclient.IntervenerRole.SOLICITOR;

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

    @Mock
    private IdamService idamService;

    @Mock
    private CaseRoleService caseRoleService;

    private StopRepresentingClientService underTest;

    @Mock
    private FinremNotificationRequestMapper finremNotificationRequestMapper;

    @Mock
    private ApplicationEventPublisher applicationEventPublisher;

    @BeforeEach
    void setup() {
        underTest = spy(new StopRepresentingClientService(assignCaseAccessService, systemUserService, finremCaseDetailsMapper,
            manageBarristerService, barristerChangeCaseAccessUpdater, coreCaseDataService, intervenerService,
            caseRoleService, idamService, finremNotificationRequestMapper, applicationEventPublisher));
        lenient().when(systemUserService.getSysUserToken()).thenReturn(TEST_SYSTEM_TOKEN);
        lenient().when(manageBarristerService
                .getBarristerChange(any(FinremCaseDetails.class), any(FinremCaseData.class), any(BarristerParty.class)))
            .thenReturn(BarristerChange.builder().build());
    }

    @Test
    void shouldSetIntervenerAsUnrepresentedAndPopulateDefaultOrganisationPolicy() {
        IntervenerOne intervenerWrapper = IntervenerOne.builder().build();

        underTest.setIntervenerUnrepresented(intervenerWrapper);

        assertThat(intervenerWrapper.getIntervenerRepresented())
            .isEqualTo(YesOrNo.NO);
        assertThat(intervenerWrapper.getIntervenerOrganisation())
            .isEqualTo(
                OrganisationPolicy.builder()
                    .organisation(Organisation.builder().organisationID(null).organisationName(null).build())
                    .orgPolicyReference(null)
                    .orgPolicyCaseAssignedRole(CaseRole.INTVR_SOLICITOR_1.getCcdCode())
                    .build()
            );
    }

    @ParameterizedTest
    @ValueSource(booleans = {true, false})
    void shouldSetRespondentAsUnrepresentedAndPopulateDefaultOrganisationPolicy(
        boolean isConsentedApplication) {

        FinremCaseData caseData = FinremCaseData.builder()
            .ccdCaseType(isConsentedApplication ? CaseType.CONSENTED : CaseType.CONTESTED)
            .build();

        underTest.setRespondentUnrepresented(caseData);

        if (isConsentedApplication) {
            assertThat(caseData.getContactDetailsWrapper().getConsentedRespondentRepresented())
                .isEqualTo(YesOrNo.NO);
        } else {
            assertThat(caseData.getContactDetailsWrapper().getContestedRespondentRepresented())
                .isEqualTo(YesOrNo.NO);
        }

        assertThat(caseData.getRespondentOrganisationPolicy())
            .isEqualTo(
                OrganisationPolicy.builder()
                    .organisation(Organisation.builder().organisationID(null).organisationName(null).build())
                    .orgPolicyReference(null)
                    .orgPolicyCaseAssignedRole(CaseRole.RESP_SOLICITOR.getCcdCode())
                    .build()
            );
    }

    @Test
    void shouldSetApplicantAsUnrepresentedAndPopulateDefaultOrganisationPolicy() {
        FinremCaseData caseData = FinremCaseData.builder()
            .build();

        underTest.setApplicantUnrepresented(caseData);

        assertThat(caseData.getContactDetailsWrapper().getApplicantRepresented())
            .isEqualTo(YesOrNo.NO);

        assertThat(caseData.getApplicantOrganisationPolicy())
            .isEqualTo(
                OrganisationPolicy.builder()
                    .organisation(Organisation.builder().organisationID(null).organisationName(null).build())
                    .orgPolicyReference(null)
                    .orgPolicyCaseAssignedRole(CaseRole.APP_SOLICITOR.getCcdCode())
                    .build()
            );
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

            StopRepresentingClientInfo info = stopRepresentingClientInfo(caseDetails,
                FinremCaseDetails.builder().data(caseDataBefore).build());

            underTest.revokePartiesAccessAndNotifyParties(info);

            verify(intervenerService).revokeIntervenerSolicitor(Long.parseLong(CASE_ID), intervenerOne);
            verify(intervenerService, never()).revokeIntervenerSolicitor(Long.parseLong(CASE_ID), intervenerTwo);
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

            StopRepresentingClientInfo info = stopRepresentingClientInfo(caseDetails,
                FinremCaseDetails.builder().data(caseDataBefore).build());

            BarristerChange applicantBarristerChange = mock(BarristerChange.class);
            when(manageBarristerService.getBarristerChange(info.getCaseDetails(), caseDataBefore, BarristerParty.APPLICANT))
                .thenReturn(applicantBarristerChange);
            BarristerChange respondentBarristerChange = mock(BarristerChange.class);
            when(manageBarristerService.getBarristerChange(info.getCaseDetails(), caseDataBefore, BarristerParty.RESPONDENT))
                .thenReturn(respondentBarristerChange);
            BarristerChange intv1BarristerChange = mock(BarristerChange.class);
            when(manageBarristerService.getBarristerChange(info.getCaseDetails(), caseDataBefore, BarristerParty.INTERVENER1))
                .thenReturn(intv1BarristerChange);
            BarristerChange intv2BarristerChange = mock(BarristerChange.class);
            when(manageBarristerService.getBarristerChange(info.getCaseDetails(), caseDataBefore, BarristerParty.INTERVENER2))
                .thenReturn(intv2BarristerChange);
            BarristerChange intv3BarristerChange = mock(BarristerChange.class);
            when(manageBarristerService.getBarristerChange(info.getCaseDetails(), caseDataBefore, BarristerParty.INTERVENER3))
                .thenReturn(intv3BarristerChange);
            BarristerChange intv4BarristerChange = mock(BarristerChange.class);
            when(manageBarristerService.getBarristerChange(info.getCaseDetails(), caseDataBefore, BarristerParty.INTERVENER4))
                .thenReturn(intv4BarristerChange);

            underTest.revokePartiesAccessAndNotifyParties(info);

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
        void shouldExecuteAllBarristerChanges() {
            FinremCaseData caseData = spy(FinremCaseData.class);
            caseData.getContactDetailsWrapper().setNocParty(mock(NoticeOfChangeParty.class));
            FinremCaseData caseDataBefore = mock(FinremCaseData.class);
            FinremCaseDetails caseDetails = FinremCaseDetailsBuilderFactory.from(Long.valueOf(CASE_ID), mock(CaseType.class), caseData)
                .build();

            StopRepresentingClientInfo info = stopRepresentingClientInfo(caseDetails,
                FinremCaseDetails.builder().data(caseDataBefore).build());

            BarristerChange applicantBarristerChange = mock(BarristerChange.class);
            when(manageBarristerService.getBarristerChange(info.getCaseDetails(), caseDataBefore, BarristerParty.APPLICANT))
                .thenReturn(applicantBarristerChange);
            BarristerChange respondentBarristerChange = mock(BarristerChange.class);
            when(manageBarristerService.getBarristerChange(info.getCaseDetails(), caseDataBefore, BarristerParty.RESPONDENT))
                .thenReturn(respondentBarristerChange);
            BarristerChange intv1BarristerChange = mock(BarristerChange.class);
            when(manageBarristerService.getBarristerChange(info.getCaseDetails(), caseDataBefore, BarristerParty.INTERVENER1))
                .thenReturn(intv1BarristerChange);
            BarristerChange intv2BarristerChange = mock(BarristerChange.class);
            when(manageBarristerService.getBarristerChange(info.getCaseDetails(), caseDataBefore, BarristerParty.INTERVENER2))
                .thenReturn(intv2BarristerChange);
            BarristerChange intv3BarristerChange = mock(BarristerChange.class);
            when(manageBarristerService.getBarristerChange(info.getCaseDetails(), caseDataBefore, BarristerParty.INTERVENER3))
                .thenReturn(intv3BarristerChange);
            BarristerChange intv4BarristerChange = mock(BarristerChange.class);
            when(manageBarristerService.getBarristerChange(info.getCaseDetails(), caseDataBefore, BarristerParty.INTERVENER4))
                .thenReturn(intv4BarristerChange);

            underTest.revokePartiesAccessAndNotifyParties(info);

            verify(barristerChangeCaseAccessUpdater).executeBarristerChange(Long.parseLong(CASE_ID), applicantBarristerChange);
            verify(barristerChangeCaseAccessUpdater).executeBarristerChange(Long.parseLong(CASE_ID), respondentBarristerChange);
            verify(barristerChangeCaseAccessUpdater).executeBarristerChange(Long.parseLong(CASE_ID), intv1BarristerChange);
            verify(barristerChangeCaseAccessUpdater).executeBarristerChange(Long.parseLong(CASE_ID), intv2BarristerChange);
            verify(barristerChangeCaseAccessUpdater).executeBarristerChange(Long.parseLong(CASE_ID), intv3BarristerChange);
            verify(barristerChangeCaseAccessUpdater).executeBarristerChange(Long.parseLong(CASE_ID), intv4BarristerChange);
        }

        @ParameterizedTest
        @ValueSource(booleans = {true, false})
        void shouldApplyDecisionWithRevertedCaseDetails(boolean isApplicant) {
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

            final StopRepresentingClientInfo info = stopRepresentingClientInfo(caseDetails,
                FinremCaseDetails.builder().data(caseDataBefore).build());

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

            if (isApplicant) {
                when(finremNotificationRequestMapper.getNotificationRequestForStopRepresentingClientEmail(any(FinremCaseDetails.class),
                    eq(CaseRole.APP_SOLICITOR))).thenReturn(NotificationRequest.builder().build());
            }

            // Act
            underTest.revokePartiesAccessAndNotifyParties(info);

            verify(assignCaseAccessService).applyDecision(TEST_SYSTEM_TOKEN, mockValidCaseDetails);
            verify(assignCaseAccessService, never()).applyDecision(TEST_SYSTEM_TOKEN, mockInvalidCaseDetails);
            verifyNoInteractions(intervenerService);
        }

        @ParameterizedTest
        @ValueSource(booleans = {true, false})
        void shouldNotApplyDecisionIfChangeOrganisationRequestFieldNotPopulated(boolean isApplicant) {
            FinremCaseData caseData = spy(FinremCaseData.class);
            caseData.getContactDetailsWrapper().setNocParty(isApplicant ? NoticeOfChangeParty.APPLICANT : NoticeOfChangeParty.RESPONDENT);
            caseData.setChangeOrganisationRequestField(null);

            // Setting original org policy
            FinremCaseDetails caseDetails = FinremCaseDetailsBuilderFactory.from(
                    Long.valueOf(CASE_ID), mock(CaseType.class), caseData)
                .build();

            StopRepresentingClientInfo info = stopRepresentingClientInfo(caseDetails,
                FinremCaseDetails.builder().data(mock(FinremCaseData.class)).build());

            underTest.revokePartiesAccessAndNotifyParties(info);

            verify(assignCaseAccessService, never()).applyDecision(eq(TEST_SYSTEM_TOKEN), any(CaseDetails.class));
            verifyNoInteractions(intervenerService);
        }

        @ParameterizedTest
        @ValueSource(booleans = {true, false})
        void shouldResetChangeOrganisationRequestFieldAfterApplyDecision(boolean isApplicant) {
            CaseType caseType = mock(CaseType.class);
            FinremCaseData caseData = spy(FinremCaseData.class);
            caseData.getContactDetailsWrapper().setNocParty(isApplicant ? NoticeOfChangeParty.APPLICANT : NoticeOfChangeParty.RESPONDENT);
            caseData.setChangeOrganisationRequestField(mock(ChangeOrganisationRequest.class));

            FinremCaseData caseDataBefore = mock(FinremCaseData.class);
            FinremCaseDetails caseDetails = FinremCaseDetailsBuilderFactory.from(Long.valueOf(CASE_ID), caseType, caseData)
                .build();

            final StopRepresentingClientInfo info = stopRepresentingClientInfo(caseDetails,
                FinremCaseDetails.builder().data(caseDataBefore).build());

            when(finremCaseDetailsMapper.mapToCaseDetails(caseDetails)).thenReturn(mock(CaseDetails.class));
            if (isApplicant) {
                when(finremNotificationRequestMapper.getNotificationRequestForStopRepresentingClientEmail(any(FinremCaseDetails.class),
                    eq(CaseRole.APP_SOLICITOR))).thenReturn(NotificationRequest.builder().build());
            }

            underTest.revokePartiesAccessAndNotifyParties(info);

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

        @ParameterizedTest
        @EnumSource(value = CaseType.class, names = {"CONSENTED", "CONTESTED"})
        void shouldNotifyApplicantSolicitor(CaseType caseType) {
            FinremCaseData caseData = spy(FinremCaseData.class);
            caseData.getContactDetailsWrapper().setNocParty(NoticeOfChangeParty.APPLICANT);
            caseData.setChangeOrganisationRequestField(mock(ChangeOrganisationRequest.class));

            FinremCaseData caseDataBefore = mock(FinremCaseData.class);
            FinremCaseDetails caseDetails = FinremCaseDetailsBuilderFactory.from(Long.valueOf(CASE_ID), caseType, caseData)
                .build();
            FinremCaseDetails caseDetailsBefore = FinremCaseDetails.builder().data(caseDataBefore).build();

            StopRepresentingClientInfo info = stopRepresentingClientInfo(caseDetails, caseDetailsBefore);

            NotificationRequest notificationRequest = NotificationRequest.builder().build();
            when(finremNotificationRequestMapper.getNotificationRequestForStopRepresentingClientEmail(caseDetailsBefore,
                CaseRole.APP_SOLICITOR)).thenReturn(notificationRequest);

            underTest.revokePartiesAccessAndNotifyParties(info);

            ArgumentCaptor<SendCorrespondenceEvent> captor = ArgumentCaptor.forClass(SendCorrespondenceEvent.class);
            verify(applicationEventPublisher).publishEvent(captor.capture());
            verify(finremNotificationRequestMapper).getNotificationRequestForStopRepresentingClientEmail(caseDetailsBefore,
                CaseRole.APP_SOLICITOR);

            verifySendCorrespondenceEvent(captor.getAllValues().getFirst(),
                NotificationParty.FORMER_APPLICANT_SOLICITOR_ONLY,
                applicantExpectedTemplateNames(caseType), caseDetails, caseDetailsBefore, notificationRequest);
            verifyNoMoreInteractions(applicationEventPublisher, finremNotificationRequestMapper);
        }

        @ParameterizedTest
        @EnumSource(value = CaseType.class, names = {"CONSENTED", "CONTESTED"})
        void shouldNotifyApplicantBarrister(CaseType caseType) {
            FinremCaseData caseData = spy(FinremCaseData.class);
            caseData.getContactDetailsWrapper().setNocParty(mock(NoticeOfChangeParty.class));
            FinremCaseData caseDataBefore = mock(FinremCaseData.class);
            FinremCaseDetails caseDetails = FinremCaseDetailsBuilderFactory.from(Long.valueOf(CASE_ID), caseType, caseData)
                .build();
            FinremCaseDetails caseDetailsBefore = FinremCaseDetails.builder().data(caseDataBefore).build();

            StopRepresentingClientInfo info = stopRepresentingClientInfo(caseDetails, caseDetailsBefore);
            Barrister applicantBarrister = mock(Barrister.class);
            mockApplicantBarristersChangeOnly(info, caseDataBefore, applicantBarrister);

            NotificationRequest notificationRequest = NotificationRequest.builder().build();
            when(finremNotificationRequestMapper
                .getNotificationRequestForStopRepresentingClientEmail(caseDetailsBefore, applicantBarrister))
                .thenReturn(notificationRequest);

            underTest.revokePartiesAccessAndNotifyParties(info);

            ArgumentCaptor<SendCorrespondenceEvent> captor = ArgumentCaptor.forClass(SendCorrespondenceEvent.class);
            verify(applicationEventPublisher).publishEvent(captor.capture());
            verify(finremNotificationRequestMapper)
                .getNotificationRequestForStopRepresentingClientEmail(caseDetailsBefore, applicantBarrister);

            verifySendCorrespondenceEvent(captor.getAllValues().getLast(),
                NotificationParty.FORMER_APPLICANT_BARRISTER_ONLY,
                applicantExpectedTemplateNames(caseType), caseDetails, caseDetailsBefore, notificationRequest);
            verifyNoMoreInteractions(applicationEventPublisher, finremNotificationRequestMapper);
        }

        @ParameterizedTest
        @EnumSource(value = CaseType.class, names = {"CONSENTED", "CONTESTED"})
        void shouldNotifyApplicantSolicitorAndBarrister(CaseType caseType) {
            FinremCaseData caseData = spy(FinremCaseData.class);
            caseData.getContactDetailsWrapper().setNocParty(NoticeOfChangeParty.APPLICANT);
            caseData.setChangeOrganisationRequestField(mock(ChangeOrganisationRequest.class));

            FinremCaseData caseDataBefore = mock(FinremCaseData.class);
            FinremCaseDetails caseDetails = FinremCaseDetailsBuilderFactory.from(Long.valueOf(CASE_ID), caseType, caseData)
                .build();
            FinremCaseDetails caseDetailsBefore = FinremCaseDetails.builder().data(caseDataBefore).build();

            StopRepresentingClientInfo info = stopRepresentingClientInfo(caseDetails, caseDetailsBefore);
            Barrister applicantBarrister = mock(Barrister.class);
            mockApplicantBarristersChangeOnly(info, caseDataBefore, applicantBarrister);

            NotificationRequest notificationRequest1 = NotificationRequest.builder().build();
            when(finremNotificationRequestMapper.getNotificationRequestForStopRepresentingClientEmail(
                caseDetailsBefore, CaseRole.APP_SOLICITOR)).thenReturn(notificationRequest1);
            NotificationRequest notificationRequest2 = NotificationRequest.builder().build();
            when(finremNotificationRequestMapper.getNotificationRequestForStopRepresentingClientEmail(caseDetailsBefore, applicantBarrister))
                .thenReturn(notificationRequest2);

            underTest.revokePartiesAccessAndNotifyParties(info);

            ArgumentCaptor<SendCorrespondenceEvent> captor = ArgumentCaptor.forClass(SendCorrespondenceEvent.class);
            verify(applicationEventPublisher, times(2)).publishEvent(captor.capture());
            verify(finremNotificationRequestMapper)
                .getNotificationRequestForStopRepresentingClientEmail(caseDetailsBefore, applicantBarrister);

            verifySendCorrespondenceEvent(captor.getAllValues().getFirst(),
                NotificationParty.FORMER_APPLICANT_SOLICITOR_ONLY,
                applicantExpectedTemplateNames(caseType), caseDetails, caseDetailsBefore, notificationRequest1);

            verifySendCorrespondenceEvent(captor.getAllValues().getLast(),
                NotificationParty.FORMER_APPLICANT_BARRISTER_ONLY,
                applicantExpectedTemplateNames(caseType), caseDetails, caseDetailsBefore, notificationRequest2);
            verifyNoMoreInteractions(applicationEventPublisher, finremNotificationRequestMapper);
        }

        @ParameterizedTest
        @EnumSource(value = CaseType.class, names = {"CONSENTED", "CONTESTED"})
        void shouldNotifyRespondentSolicitor(CaseType caseType) {
            FinremCaseData caseData = spy(FinremCaseData.class);
            caseData.getContactDetailsWrapper().setNocParty(NoticeOfChangeParty.RESPONDENT);
            caseData.setChangeOrganisationRequestField(mock(ChangeOrganisationRequest.class));

            FinremCaseData caseDataBefore = mock(FinremCaseData.class);
            FinremCaseDetails caseDetails = FinremCaseDetailsBuilderFactory.from(Long.valueOf(CASE_ID), caseType, caseData)
                .build();
            FinremCaseDetails caseDetailsBefore = FinremCaseDetails.builder().data(caseDataBefore).build();

            StopRepresentingClientInfo info = stopRepresentingClientInfo(caseDetails, caseDetailsBefore);

            NotificationRequest notificationRequest = NotificationRequest.builder().build();
            when(finremNotificationRequestMapper.getNotificationRequestForStopRepresentingClientEmail(caseDetailsBefore,
                CaseRole.RESP_SOLICITOR)).thenReturn(notificationRequest);

            // Act
            underTest.revokePartiesAccessAndNotifyParties(info);

            // verify
            ArgumentCaptor<SendCorrespondenceEvent> captor = ArgumentCaptor.forClass(SendCorrespondenceEvent.class);
            verify(applicationEventPublisher).publishEvent(captor.capture());
            verify(finremNotificationRequestMapper).getNotificationRequestForStopRepresentingClientEmail(caseDetailsBefore,
                CaseRole.RESP_SOLICITOR);

            verifySendCorrespondenceEvent(captor.getAllValues().getFirst(),
                NotificationParty.FORMER_RESPONDENT_SOLICITOR_ONLY,
                respondentExpectedTemplateNames(caseType), caseDetails, caseDetailsBefore, notificationRequest);
            verifyNoMoreInteractions(applicationEventPublisher, finremNotificationRequestMapper);
        }

        @ParameterizedTest
        @EnumSource(value = CaseType.class, names = {"CONSENTED", "CONTESTED"})
        void shouldNotifyRespondentBarrister(CaseType caseType) {
            FinremCaseData caseData = spy(FinremCaseData.class);
            caseData.getContactDetailsWrapper().setNocParty(mock(NoticeOfChangeParty.class));
            FinremCaseData caseDataBefore = mock(FinremCaseData.class);
            FinremCaseDetails caseDetails = FinremCaseDetailsBuilderFactory.from(Long.valueOf(CASE_ID), caseType, caseData)
                .build();
            FinremCaseDetails caseDetailsBefore = FinremCaseDetails.builder().data(caseDataBefore).build();

            StopRepresentingClientInfo info = stopRepresentingClientInfo(caseDetails, caseDetailsBefore);
            Barrister respondentBarrister = mock(Barrister.class);
            mockRespondentBarristersChangeOnly(info, caseDataBefore, respondentBarrister);

            NotificationRequest notificationRequest = NotificationRequest.builder().build();
            when(finremNotificationRequestMapper
                .getNotificationRequestForStopRepresentingClientEmail(caseDetailsBefore, respondentBarrister))
                .thenReturn(notificationRequest);

            underTest.revokePartiesAccessAndNotifyParties(info);

            ArgumentCaptor<SendCorrespondenceEvent> captor = ArgumentCaptor.forClass(SendCorrespondenceEvent.class);
            verify(applicationEventPublisher).publishEvent(captor.capture());
            verify(finremNotificationRequestMapper)
                .getNotificationRequestForStopRepresentingClientEmail(caseDetailsBefore, respondentBarrister);

            verifySendCorrespondenceEvent(captor.getAllValues().getLast(),
                NotificationParty.FORMER_RESPONDENT_BARRISTER_ONLY,
                respondentExpectedTemplateNames(caseType), caseDetails, caseDetailsBefore, notificationRequest);
            verifyNoMoreInteractions(applicationEventPublisher, finremNotificationRequestMapper);
        }

        @ParameterizedTest
        @EnumSource(value = CaseType.class, names = {"CONSENTED", "CONTESTED"})
        void shouldNotifyRespondentSolicitorAndBarrister(CaseType caseType) {
            FinremCaseData caseData = spy(FinremCaseData.class);
            caseData.getContactDetailsWrapper().setNocParty(NoticeOfChangeParty.RESPONDENT);
            caseData.setChangeOrganisationRequestField(mock(ChangeOrganisationRequest.class));

            FinremCaseData caseDataBefore = mock(FinremCaseData.class);
            FinremCaseDetails caseDetails = FinremCaseDetailsBuilderFactory.from(Long.valueOf(CASE_ID), caseType, caseData)
                .build();
            FinremCaseDetails caseDetailsBefore = FinremCaseDetails.builder().data(caseDataBefore).build();

            StopRepresentingClientInfo info = stopRepresentingClientInfo(caseDetails, caseDetailsBefore);
            Barrister applicantBarrister = mock(Barrister.class);
            mockRespondentBarristersChangeOnly(info, caseDataBefore, applicantBarrister);

            NotificationRequest notificationRequest = NotificationRequest.builder().build();
            when(finremNotificationRequestMapper.getNotificationRequestForStopRepresentingClientEmail(caseDetailsBefore,
                CaseRole.RESP_SOLICITOR)).thenReturn(notificationRequest);
            when(finremNotificationRequestMapper.getNotificationRequestForStopRepresentingClientEmail(caseDetailsBefore, applicantBarrister))
                .thenReturn(notificationRequest);

            underTest.revokePartiesAccessAndNotifyParties(info);

            ArgumentCaptor<SendCorrespondenceEvent> captor = ArgumentCaptor.forClass(SendCorrespondenceEvent.class);
            verify(applicationEventPublisher, times(2)).publishEvent(captor.capture());
            verify(finremNotificationRequestMapper)
                .getNotificationRequestForStopRepresentingClientEmail(caseDetailsBefore, applicantBarrister);

            verifySendCorrespondenceEvent(captor.getAllValues().getFirst(),
                NotificationParty.FORMER_RESPONDENT_SOLICITOR_ONLY,
                respondentExpectedTemplateNames(caseType), caseDetails, caseDetailsBefore, notificationRequest);

            verifySendCorrespondenceEvent(captor.getAllValues().getLast(),
                NotificationParty.FORMER_RESPONDENT_BARRISTER_ONLY,
                respondentExpectedTemplateNames(caseType), caseDetails, caseDetailsBefore, notificationRequest);
            verifyNoMoreInteractions(applicationEventPublisher, finremNotificationRequestMapper);
        }

        private static EmailTemplateNames applicantExpectedTemplateNames(CaseType caseType) {
            return CaseType.CONTESTED.equals(caseType)
                ? FR_CONTESTED_REPRESENTATIVE_STOP_REPRESENTING_APPLICANT
                : FR_CONSENTED_REPRESENTATIVE_STOP_REPRESENTING_APPLICANT;
        }

        private static EmailTemplateNames respondentExpectedTemplateNames(CaseType caseType) {
            return CaseType.CONTESTED.equals(caseType)
                ? FR_CONTESTED_REPRESENTATIVE_STOP_REPRESENTING_RESPONDENT
                : FR_CONSENTED_REPRESENTATIVE_STOP_REPRESENTING_RESPONDENT;
        }

        private void verifySendCorrespondenceEvent(SendCorrespondenceEvent event, NotificationParty party, EmailTemplateNames template,
                                                   FinremCaseDetails caseDetails, FinremCaseDetails caseDetailsBefore,
                                                   NotificationRequest notificationRequest) {
            assertThat(event)
                .extracting(
                    SendCorrespondenceEvent::getNotificationParties,
                    SendCorrespondenceEvent::getEmailTemplate,
                    SendCorrespondenceEvent::getCaseDetails,
                    SendCorrespondenceEvent::getCaseDetailsBefore,
                    SendCorrespondenceEvent::getAuthToken,
                    SendCorrespondenceEvent::getEmailNotificationRequest
                )
                .contains(
                    List.of(party),
                    template,
                    caseDetails,
                    caseDetailsBefore,
                    AUTH_TOKEN,
                    notificationRequest
                );
        }
    }

    @Nested
    class BuildRepresentationTests {

        @Test
        void testApplicantSolicitorRepresented() {
            FinremCaseData caseData = spy(FinremCaseData.class);

            boolean isApplicantRepresentative = true;
            when(caseRoleService.isApplicantRepresentative(caseData, AUTH_TOKEN)).thenReturn(isApplicantRepresentative);
            boolean isRespondentRepresentative = false;
            when(caseRoleService.isRespondentRepresentative(caseData, AUTH_TOKEN)).thenReturn(isRespondentRepresentative);
            boolean isIntervenerRepresentative = false;
            when(caseRoleService.isIntervenerRepresentative(caseData, AUTH_TOKEN)).thenReturn(isIntervenerRepresentative);

            when(idamService.getIdamUserId(AUTH_TOKEN)).thenReturn(TestConstants.TEST_USER_ID);

            RepresentativeInContext representativeInContext = underTest.buildRepresentation(caseData, AUTH_TOKEN);
            assertThat(representativeInContext.userId()).isEqualTo(TestConstants.TEST_USER_ID);
            assertThat(representativeInContext.isApplicantRepresentative()).isTrue();
            assertThat(representativeInContext.isRespondentRepresentative()).isFalse();
            assertThat(representativeInContext.intervenerIndex()).isNull();
            assertThat(representativeInContext.intervenerRole()).isNull();
            assertThat(representativeInContext.isIntervenerRepresentative()).isFalse();
            assertThat(representativeInContext.isIntervenerBarrister()).isFalse();
        }

        @Test
        void testRespondentSolicitorRepresented() {
            FinremCaseData caseData = spy(FinremCaseData.class);

            boolean isApplicantRepresentative = false;
            when(caseRoleService.isApplicantRepresentative(caseData, AUTH_TOKEN)).thenReturn(isApplicantRepresentative);
            boolean isRespondentRepresentative = true;
            when(caseRoleService.isRespondentRepresentative(caseData, AUTH_TOKEN)).thenReturn(isRespondentRepresentative);
            boolean isIntervenerRepresentative = false;
            when(caseRoleService.isIntervenerRepresentative(caseData, AUTH_TOKEN)).thenReturn(isIntervenerRepresentative);

            when(idamService.getIdamUserId(AUTH_TOKEN)).thenReturn(TestConstants.TEST_USER_ID);

            RepresentativeInContext representativeInContext = underTest.buildRepresentation(caseData, AUTH_TOKEN);
            assertThat(representativeInContext.userId()).isEqualTo(TestConstants.TEST_USER_ID);
            assertThat(representativeInContext.isApplicantRepresentative()).isFalse();
            assertThat(representativeInContext.isRespondentRepresentative()).isTrue();
            assertThat(representativeInContext.intervenerIndex()).isNull();
            assertThat(representativeInContext.intervenerRole()).isNull();
            assertThat(representativeInContext.isIntervenerRepresentative()).isFalse();
            assertThat(representativeInContext.isIntervenerBarrister()).isFalse();
        }

        @Test
        void testIntervenerOneBarristerRepresented() {
            FinremCaseData caseData = spy(FinremCaseData.class);

            boolean isApplicantRepresentative = false;
            when(caseRoleService.isApplicantRepresentative(caseData, AUTH_TOKEN)).thenReturn(isApplicantRepresentative);
            boolean isRespondentRepresentative = false;
            when(caseRoleService.isRespondentRepresentative(caseData, AUTH_TOKEN)).thenReturn(isRespondentRepresentative);
            boolean isIntervenerRepresentative = true;
            when(caseRoleService.isIntervenerRepresentative(caseData, AUTH_TOKEN)).thenReturn(isIntervenerRepresentative);

            when(idamService.getIdamUserId(AUTH_TOKEN)).thenReturn(TestConstants.TEST_USER_ID);
            when(caseRoleService.getIntervenerIndex(caseData, AUTH_TOKEN)).thenReturn(Optional.of(1));
            when(caseRoleService.getIntervenerSolicitorIndex(caseData, AUTH_TOKEN)).thenReturn(Optional.empty());

            RepresentativeInContext representativeInContext = underTest.buildRepresentation(caseData, AUTH_TOKEN);
            assertThat(representativeInContext.userId()).isEqualTo(TestConstants.TEST_USER_ID);
            assertThat(representativeInContext.isApplicantRepresentative()).isFalse();
            assertThat(representativeInContext.isRespondentRepresentative()).isFalse();
            assertThat(representativeInContext.intervenerIndex()).isEqualTo(1);
            assertThat(representativeInContext.intervenerRole()).isEqualTo(BARRISTER);
            assertThat(representativeInContext.isIntervenerRepresentative()).isTrue();
            assertThat(representativeInContext.isIntervenerBarrister()).isTrue();
        }

        @Test
        void testIntervenerTwoSolicitorRepresented() {
            FinremCaseData caseData = spy(FinremCaseData.class);

            boolean isApplicantRepresentative = false;
            when(caseRoleService.isApplicantRepresentative(caseData, AUTH_TOKEN)).thenReturn(isApplicantRepresentative);
            boolean isRespondentRepresentative = false;
            when(caseRoleService.isRespondentRepresentative(caseData, AUTH_TOKEN)).thenReturn(isRespondentRepresentative);
            boolean isIntervenerRepresentative = true;
            when(caseRoleService.isIntervenerRepresentative(caseData, AUTH_TOKEN)).thenReturn(isIntervenerRepresentative);

            when(idamService.getIdamUserId(AUTH_TOKEN)).thenReturn(TestConstants.TEST_USER_ID);
            when(caseRoleService.getIntervenerIndex(caseData, AUTH_TOKEN)).thenReturn(Optional.of(2));
            when(caseRoleService.getIntervenerSolicitorIndex(caseData, AUTH_TOKEN)).thenReturn(Optional.of(2));

            RepresentativeInContext representativeInContext = underTest.buildRepresentation(caseData, AUTH_TOKEN);
            assertThat(representativeInContext.userId()).isEqualTo(TestConstants.TEST_USER_ID);
            assertThat(representativeInContext.isApplicantRepresentative()).isFalse();
            assertThat(representativeInContext.isRespondentRepresentative()).isFalse();
            assertThat(representativeInContext.intervenerIndex()).isEqualTo(2);
            assertThat(representativeInContext.intervenerRole()).isEqualTo(SOLICITOR);
            assertThat(representativeInContext.isIntervenerRepresentative()).isTrue();
            assertThat(representativeInContext.isIntervenerBarrister()).isFalse();
        }
    }

    @Nested
    class IsIntervenerBarristerFromSameOrganisationAsSolicitorTests {

        @Test
        void givenIntvSolicitorRepresented_whenCalled_thenReturnFalse() {
            RepresentativeInContext representativeInContext = mock(RepresentativeInContext.class);
            when(representativeInContext.isIntervenerBarrister()).thenReturn(false);

            assertFalse(underTest.isIntervenerBarristerFromSameOrganisationAsSolicitor(mock(FinremCaseData.class), representativeInContext));
        }

        @Test
        void givenIntvOneBarristerRepresented_whenIntvOneSolOrganisationIsTheSame_thenReturnTrue() {
            RepresentativeInContext representativeInContext = mock(RepresentativeInContext.class);
            when(representativeInContext.userId()).thenReturn(TEST_USER_ID);
            when(representativeInContext.intervenerIndex()).thenReturn(1);
            when(representativeInContext.isIntervenerBarrister()).thenReturn(true);

            FinremCaseData caseData = FinremCaseData.builder()
                .intervenerOne(IntervenerOne.builder()
                    .intervenerOrganisation(OrganisationPolicy.builder()
                        .organisation(organisation("FinremOrg-1"))
                        .build())
                    .build())
                .barristerCollectionWrapper(BarristerCollectionWrapper.builder()
                    .intvr1Barristers(List.of(
                        buildBarristerCollectionItem("another-user-id", "FinremOrg-2"),
                        buildBarristerCollectionItem(TEST_USER_ID, "FinremOrg-1")
                    ))
                    .build())
                .build();

            assertTrue(underTest.isIntervenerBarristerFromSameOrganisationAsSolicitor(caseData, representativeInContext));
        }

        @Test
        void givenIntvOneBarristerRepresented_whenIntvOneSolOrganisationIsNotTheSame_thenReturnFalse() {
            RepresentativeInContext representativeInContext = mock(RepresentativeInContext.class);
            when(representativeInContext.userId()).thenReturn(TEST_USER_ID);
            when(representativeInContext.intervenerIndex()).thenReturn(1);
            when(representativeInContext.isIntervenerBarrister()).thenReturn(true);

            FinremCaseData caseData = FinremCaseData.builder()
                .intervenerOne(IntervenerOne.builder()
                    .intervenerOrganisation(OrganisationPolicy.builder()
                        .organisation(organisation("FinremOrg-3"))
                        .build())
                    .build())
                .barristerCollectionWrapper(BarristerCollectionWrapper.builder()
                    .intvr1Barristers(List.of(
                        buildBarristerCollectionItem("another-user-id", "FinremOrg-3"),
                        buildBarristerCollectionItem(TEST_USER_ID, "FinremOrg-1")
                    ))
                    .build())
                .build();

            assertFalse(underTest.isIntervenerBarristerFromSameOrganisationAsSolicitor(caseData, representativeInContext));
        }

        @Test
        void givenIntvOneBarristerRepresented_whenIntvOneSolOrganisationIsMissing_thenReturnFalse() {
            RepresentativeInContext representativeInContext = mock(RepresentativeInContext.class);
            when(representativeInContext.userId()).thenReturn(TEST_USER_ID);
            when(representativeInContext.intervenerIndex()).thenReturn(1);
            when(representativeInContext.isIntervenerBarrister()).thenReturn(true);

            FinremCaseData caseData = FinremCaseData.builder()
                .intervenerOne(IntervenerOne.builder()
                    .intervenerOrganisation(null)
                    .build())
                .barristerCollectionWrapper(BarristerCollectionWrapper.builder()
                    .intvr1Barristers(List.of(
                        buildBarristerCollectionItem("another-user-id", "FinremOrg-3"),
                        buildBarristerCollectionItem(TEST_USER_ID, "FinremOrg-1")
                    ))
                    .build())
                .build();

            assertFalse(underTest.isIntervenerBarristerFromSameOrganisationAsSolicitor(caseData, representativeInContext));
        }

        @Test
        void givenIntvFourBarristerRepresented_whenIntvFourSolOrganisationIsTheSame_thenReturnTrue() {
            RepresentativeInContext representativeInContext = mock(RepresentativeInContext.class);
            when(representativeInContext.userId()).thenReturn(TEST_USER_ID);
            when(representativeInContext.intervenerIndex()).thenReturn(4);
            when(representativeInContext.isIntervenerBarrister()).thenReturn(true);

            FinremCaseData caseData = FinremCaseData.builder()
                .intervenerFour(IntervenerFour.builder()
                    .intervenerOrganisation(OrganisationPolicy.builder()
                        .organisation(organisation("FinremOrg-1"))
                        .build())
                    .build())
                .barristerCollectionWrapper(BarristerCollectionWrapper.builder()
                    .intvr4Barristers(List.of(
                        buildBarristerCollectionItem("another-user-id", "FinremOrg-2"),
                        buildBarristerCollectionItem(TEST_USER_ID, "FinremOrg-1")
                    ))
                    .build())
                .build();

            assertTrue(underTest.isIntervenerBarristerFromSameOrganisationAsSolicitor(caseData, representativeInContext));
        }

        @Test
        void givenIntvFourBarristerRepresented_whenIntvFourSolOrganisationIsNotTheSame_thenReturnFalse() {
            RepresentativeInContext representativeInContext = mock(RepresentativeInContext.class);
            when(representativeInContext.userId()).thenReturn(TEST_USER_ID);
            when(representativeInContext.intervenerIndex()).thenReturn(4);
            when(representativeInContext.isIntervenerBarrister()).thenReturn(true);

            FinremCaseData caseData = FinremCaseData.builder()
                .intervenerFour(IntervenerFour.builder()
                    .intervenerOrganisation(OrganisationPolicy.builder()
                        .organisation(organisation("FinremOrg-3"))
                        .build())
                    .build())
                .barristerCollectionWrapper(BarristerCollectionWrapper.builder()
                    .intvr4Barristers(List.of(
                        buildBarristerCollectionItem("another-user-id", "FinremOrg-3"),
                        buildBarristerCollectionItem(TEST_USER_ID, "FinremOrg-1")
                    ))
                    .build())
                .build();

            assertFalse(underTest.isIntervenerBarristerFromSameOrganisationAsSolicitor(caseData, representativeInContext));
        }
    }

    private void mockBarristerChange(StopRepresentingClientInfo info, FinremCaseData caseDataBefore) {
        BarristerChange applicantBarristerChange = mock(BarristerChange.class);
        when(manageBarristerService.getBarristerChange(info.getCaseDetails(), caseDataBefore, BarristerParty.APPLICANT))
            .thenReturn(applicantBarristerChange);
        BarristerChange respondentBarristerChange = mock(BarristerChange.class);
        when(manageBarristerService.getBarristerChange(info.getCaseDetails(), caseDataBefore, BarristerParty.RESPONDENT))
            .thenReturn(respondentBarristerChange);
        BarristerChange intv1BarristerChange = mock(BarristerChange.class);
        when(manageBarristerService.getBarristerChange(info.getCaseDetails(), caseDataBefore, BarristerParty.INTERVENER1))
            .thenReturn(intv1BarristerChange);
        BarristerChange intv2BarristerChange = mock(BarristerChange.class);
        when(manageBarristerService.getBarristerChange(info.getCaseDetails(), caseDataBefore, BarristerParty.INTERVENER2))
            .thenReturn(intv2BarristerChange);
        BarristerChange intv3BarristerChange = mock(BarristerChange.class);
        when(manageBarristerService.getBarristerChange(info.getCaseDetails(), caseDataBefore, BarristerParty.INTERVENER3))
            .thenReturn(intv3BarristerChange);
        BarristerChange intv4BarristerChange = mock(BarristerChange.class);
        when(manageBarristerService.getBarristerChange(info.getCaseDetails(), caseDataBefore, BarristerParty.INTERVENER4))
            .thenReturn(intv4BarristerChange);
    }

    private void mockRespondentBarristersChangeOnly(StopRepresentingClientInfo info, FinremCaseData caseDataBefore,
                                                    Barrister... respondentBarrister) {
        mockBarristerChange(info, caseDataBefore);
        // override respondent barrister change
        BarristerChange respondentBarristerChange = mock(BarristerChange.class);
        when(manageBarristerService.getBarristerChange(info.getCaseDetails(), caseDataBefore, BarristerParty.RESPONDENT))
            .thenReturn(respondentBarristerChange);
        when(respondentBarristerChange.getRemoved()).thenReturn(Set.of(respondentBarrister));
    }

    private void mockApplicantBarristersChangeOnly(StopRepresentingClientInfo info, FinremCaseData caseDataBefore,
                                                   Barrister... applicantBarristers) {
        mockBarristerChange(info, caseDataBefore);
        // override applicant barrister change
        BarristerChange applicantBarristerChange = mock(BarristerChange.class);
        when(manageBarristerService.getBarristerChange(info.getCaseDetails(), caseDataBefore, BarristerParty.APPLICANT))
            .thenReturn(applicantBarristerChange);
        when(applicantBarristerChange.getRemoved()).thenReturn(Set.of(applicantBarristers));
    }

    private static BarristerCollectionItem buildBarristerCollectionItem(String userId, String orgId) {
        return BarristerCollectionItem.builder()
            .value(Barrister.builder().userId(userId).organisation(organisation(orgId)).build())
            .build();
    }

    private static OrganisationPolicy getOrganisationPolicy(FinremCaseData caseData, boolean isApplicant) {
        return isApplicant ? caseData.getApplicantOrganisationPolicy() :
            caseData.getRespondentOrganisationPolicy();
    }

    private static StopRepresentingClientInfo stopRepresentingClientInfo(FinremCaseDetails caseDetails,
                                                                         FinremCaseDetails caseDetailsBefore) {
        return StopRepresentingClientInfo.builder()
            .caseDetails(caseDetails)
            .caseDetailsBefore(caseDetailsBefore)
            .userAuthorisation(AUTH_TOKEN)
            .build();
    }
}
