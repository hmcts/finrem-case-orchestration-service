package uk.gov.hmcts.reform.finrem.caseorchestration.service.stoprepresentingclient;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.finrem.caseorchestration.TestConstants;
import uk.gov.hmcts.reform.finrem.caseorchestration.mapper.FinremCaseDetailsMapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.mapper.notificationrequest.FinremNotificationRequestMapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.BarristerChange;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.Barrister;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.BarristerCollectionItem;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.BarristerParty;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseDocument;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseRole;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseData;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.OrganisationPolicy;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.YesOrNo;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.wrapper.BarristerCollectionWrapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.wrapper.ContactDetailsWrapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.wrapper.IntervenerFour;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.wrapper.IntervenerOne;
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
import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.finrem.caseorchestration.TestConstants.AUTH_TOKEN;
import static uk.gov.hmcts.reform.finrem.caseorchestration.TestConstants.TEST_SYSTEM_TOKEN;
import static uk.gov.hmcts.reform.finrem.caseorchestration.TestConstants.TEST_USER_ID;
import static uk.gov.hmcts.reform.finrem.caseorchestration.TestSetUpUtils.organisation;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseType.CONTESTED;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.intervener.IntervenerType.INTERVENER_FOUR;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.intervener.IntervenerType.INTERVENER_ONE;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.intervener.IntervenerType.INTERVENER_TWO;
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
    private StopRepresentingClientLetterService stopRepresentingClientLetterService;

    @BeforeEach
    void setup() {
        underTest = spy(new StopRepresentingClientService(assignCaseAccessService, systemUserService, finremCaseDetailsMapper,
            manageBarristerService, barristerChangeCaseAccessUpdater, coreCaseDataService, intervenerService,
            caseRoleService, idamService, finremNotificationRequestMapper,
            stopRepresentingClientLetterService));
        lenient().when(systemUserService.getSysUserToken()).thenReturn(TEST_SYSTEM_TOKEN);
        lenient().when(manageBarristerService
                .getBarristerChange(any(FinremCaseDetails.class), any(FinremCaseData.class), any(BarristerParty.class)))
            .thenReturn(BarristerChange.builder().build());
    }

    @Test
    void shouldSetIntervenerAsUnrepresentedAndPopulateDefaultOrganisationPolicy() {
        IntervenerOne intervener = spy(IntervenerOne.builder().build());

        underTest.setIntervenerUnrepresented(intervener);

        OrganisationPolicy expectedPolicy =
            OrganisationPolicy.getDefaultOrganisationPolicy(CaseRole.INTVR_SOLICITOR_1);
        assertAll(
            () -> assertThat(intervener.getIntervenerRepresented())
                .isEqualTo(YesOrNo.NO),

            () -> assertThat(intervener.getIntervenerOrganisation())
                .usingRecursiveComparison()
                .isEqualTo(expectedPolicy),

            () -> verify(intervener).clearIntervenerSolicitorFields()
        );
    }

    @ParameterizedTest
    @ValueSource(booleans = {true, false})
    void shouldSetRespondentAsUnrepresentedAndPopulateDefaultOrganisationPolicy(
        boolean isConsentedApplication) {

        ContactDetailsWrapper contactDetails = spy(new ContactDetailsWrapper());
        FinremCaseData caseData = FinremCaseData.builder()
            .contactDetailsWrapper(contactDetails)
            .ccdCaseType(isConsentedApplication ? CaseType.CONSENTED : CONTESTED)
            .build();

        underTest.setRespondentUnrepresented(caseData);

        OrganisationPolicy expectedPolicy =
            OrganisationPolicy.getDefaultOrganisationPolicy(CaseRole.RESP_SOLICITOR);

        assertAll(
            () -> {
                if (isConsentedApplication) {
                    assertThat(contactDetails.getConsentedRespondentRepresented())
                        .isEqualTo(YesOrNo.NO);
                } else {
                    assertThat(contactDetails.getContestedRespondentRepresented())
                        .isEqualTo(YesOrNo.NO);
                }
            },
            () -> verify(contactDetails).clearRespondentSolicitorFields(),
            () -> assertThat(caseData.getRespondentOrganisationPolicy())
                .isEqualTo(expectedPolicy)
        );
    }

    @Test
    void shouldSetApplicantAsUnrepresentedAndPopulateDefaultOrganisationPolicy() {
        ContactDetailsWrapper contactDetails = spy(new ContactDetailsWrapper());
        FinremCaseData caseData = FinremCaseData.builder()
            .contactDetailsWrapper(contactDetails)
            .build();

        underTest.setApplicantUnrepresented(caseData);

        OrganisationPolicy expectedPolicy =
            OrganisationPolicy.getDefaultOrganisationPolicy(CaseRole.APP_SOLICITOR);

        assertAll(
            () -> assertThat(contactDetails.getApplicantRepresented())
                .isEqualTo(YesOrNo.NO),
            () -> verify(contactDetails).clearApplicantSolicitorFields(),
            () -> assertThat(caseData.getApplicantOrganisationPolicy())
                .usingRecursiveComparison()
                .isEqualTo(expectedPolicy)
        );
    }

    @Nested
    class BuildRepresentationTests {

        @Test
        void testApplicantSolicitorRepresented() {
            // Arrange
            FinremCaseData caseData = mock(FinremCaseData.class);

            when(caseRoleService.isApplicantRepresentative(caseData, AUTH_TOKEN)).thenReturn(true);
            when(caseRoleService.isRespondentRepresentative(caseData, AUTH_TOKEN)).thenReturn(false);
            when(caseRoleService.isIntervenerRepresentative(caseData, AUTH_TOKEN)).thenReturn(false);

            when(idamService.getIdamUserId(AUTH_TOKEN)).thenReturn(TestConstants.TEST_USER_ID);

            // Act
            RepresentativeInContext representativeInContext = underTest.buildRepresentation(caseData, AUTH_TOKEN);

            // Assert
            assertAll(
                "Verify applicant representative context",
                () -> assertThat(representativeInContext.userId())
                    .isEqualTo(TestConstants.TEST_USER_ID),
                () -> assertThat(representativeInContext.isApplicantRepresentative()).isTrue(),
                () -> assertThat(representativeInContext.isRespondentRepresentative()).isFalse(),
                () -> assertThat(representativeInContext.isIntervenerRepresentative()).isFalse(),
                () -> assertThat(representativeInContext.intervenerType()).isNull(),
                () -> assertThat(representativeInContext.intervenerRole()).isNull(),
                () -> assertThat(representativeInContext.isIntervenerBarrister()).isFalse()
            );
        }

        @Test
        void testRespondentSolicitorRepresented() {
            // Arrange
            FinremCaseData caseData = mock(FinremCaseData.class);

            when(caseRoleService.isApplicantRepresentative(caseData, AUTH_TOKEN)).thenReturn(false);
            when(caseRoleService.isRespondentRepresentative(caseData, AUTH_TOKEN)).thenReturn(true);
            when(caseRoleService.isIntervenerRepresentative(caseData, AUTH_TOKEN)).thenReturn(false);

            when(idamService.getIdamUserId(AUTH_TOKEN)).thenReturn(TestConstants.TEST_USER_ID);

            // Act
            RepresentativeInContext representativeInContext = underTest.buildRepresentation(caseData, AUTH_TOKEN);

            // Assert
            assertAll(
                "Verify respondent representative context",
                () -> assertThat(representativeInContext.userId())
                    .isEqualTo(TestConstants.TEST_USER_ID),
                () -> assertThat(representativeInContext.isApplicantRepresentative()).isFalse(),
                () -> assertThat(representativeInContext.isRespondentRepresentative()).isTrue(),
                () -> assertThat(representativeInContext.isIntervenerRepresentative()).isFalse(),
                () -> assertThat(representativeInContext.intervenerType()).isNull(),
                () -> assertThat(representativeInContext.intervenerRole()).isNull(),
                () -> assertThat(representativeInContext.isIntervenerBarrister()).isFalse()
            );
        }

        @Test
        void testIntervenerOneBarristerRepresented() {
            // Arrange
            FinremCaseData caseData = mock(FinremCaseData.class);

            when(caseRoleService.isApplicantRepresentative(caseData, AUTH_TOKEN)).thenReturn(false);
            when(caseRoleService.isRespondentRepresentative(caseData, AUTH_TOKEN)).thenReturn(false);
            when(caseRoleService.isIntervenerRepresentative(caseData, AUTH_TOKEN)).thenReturn(true);

            when(caseRoleService.getIntervenerType(caseData, AUTH_TOKEN)).thenReturn(Optional.of(INTERVENER_ONE));
            when(caseRoleService.getIntervenerSolicitorIndex(caseData, AUTH_TOKEN)).thenReturn(Optional.empty());

            when(idamService.getIdamUserId(AUTH_TOKEN)).thenReturn(TestConstants.TEST_USER_ID);

            // Act
            RepresentativeInContext representativeInContext = underTest.buildRepresentation(caseData, AUTH_TOKEN);

            // Assert
            assertAll(
                "Verify intervener 1 barrister representative context",
                () -> assertThat(representativeInContext.userId())
                    .isEqualTo(TestConstants.TEST_USER_ID),
                () -> assertThat(representativeInContext.isApplicantRepresentative()).isFalse(),
                () -> assertThat(representativeInContext.isRespondentRepresentative()).isFalse(),
                () -> assertThat(representativeInContext.isIntervenerRepresentative()).isTrue(),
                () -> assertThat(representativeInContext.intervenerType()).isEqualTo(INTERVENER_ONE),
                () -> assertThat(representativeInContext.intervenerRole()).isEqualTo(BARRISTER),
                () -> assertThat(representativeInContext.isIntervenerBarrister()).isTrue()
            );
        }

        @Test
        void testIntervenerTwoSolicitorRepresented() {
            // Arrange
            FinremCaseData caseData = mock(FinremCaseData.class);

            when(caseRoleService.isApplicantRepresentative(caseData, AUTH_TOKEN)).thenReturn(false);
            when(caseRoleService.isRespondentRepresentative(caseData, AUTH_TOKEN)).thenReturn(false);
            when(caseRoleService.isIntervenerRepresentative(caseData, AUTH_TOKEN)).thenReturn(true);

            when(caseRoleService.getIntervenerType(caseData, AUTH_TOKEN)).thenReturn(Optional.of(INTERVENER_ONE));
            when(caseRoleService.getIntervenerSolicitorIndex(caseData, AUTH_TOKEN)).thenReturn(Optional.of(2));

            when(idamService.getIdamUserId(AUTH_TOKEN)).thenReturn(TestConstants.TEST_USER_ID);

            // Act
            RepresentativeInContext representativeInContext = underTest.buildRepresentation(caseData, AUTH_TOKEN);

            // Assert
            assertAll(
                "Verify intervener 2 solicitor representative context",
                () -> assertThat(representativeInContext.userId())
                    .isEqualTo(TestConstants.TEST_USER_ID),
                () -> assertThat(representativeInContext.isApplicantRepresentative()).isFalse(),
                () -> assertThat(representativeInContext.isRespondentRepresentative()).isFalse(),
                () -> assertThat(representativeInContext.isIntervenerRepresentative()).isTrue(),
                () -> assertThat(representativeInContext.intervenerType()).isEqualTo(INTERVENER_TWO),
                () -> assertThat(representativeInContext.intervenerRole()).isEqualTo(SOLICITOR),
                () -> assertThat(representativeInContext.isIntervenerBarrister()).isFalse()
            );
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
            when(representativeInContext.intervenerType()).thenReturn(INTERVENER_ONE);
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
            when(representativeInContext.intervenerType()).thenReturn(INTERVENER_ONE);
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
            when(representativeInContext.intervenerType()).thenReturn(INTERVENER_ONE);
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
            when(representativeInContext.intervenerType()).thenReturn(INTERVENER_FOUR);
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
            when(representativeInContext.intervenerType()).thenReturn(INTERVENER_FOUR);
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

    private void mockIntervenerOneBarristersChangeOnly(StopRepresentingClientInfo info, FinremCaseData caseDataBefore,
                                                       Barrister... intervenerBarristers) {
        mockBarristerChange(info, caseDataBefore);
        // override respondent barrister change
        BarristerChange bc = mock(BarristerChange.class);
        when(manageBarristerService.getBarristerChange(info.getCaseDetails(), caseDataBefore, BarristerParty.INTERVENER1))
            .thenReturn(bc);
        when(bc.getRemoved()).thenReturn(Set.of(intervenerBarristers));
    }

    private void mockRespondentBarristersChangeOnly(StopRepresentingClientInfo info, FinremCaseData caseDataBefore,
                                                    Barrister... respondentBarristers) {
        mockBarristerChange(info, caseDataBefore);
        // override respondent barrister change
        BarristerChange bc = mock(BarristerChange.class);
        when(manageBarristerService.getBarristerChange(info.getCaseDetails(), caseDataBefore, BarristerParty.RESPONDENT))
            .thenReturn(bc);
        when(bc.getRemoved()).thenReturn(Set.of(respondentBarristers));
    }

    private void mockApplicantBarristersChangeOnly(StopRepresentingClientInfo info, FinremCaseData caseDataBefore,
                                                   Barrister... applicantBarristers) {
        mockBarristerChange(info, caseDataBefore);
        // override applicant barrister change
        BarristerChange bc = mock(BarristerChange.class);
        when(manageBarristerService.getBarristerChange(info.getCaseDetails(), caseDataBefore, BarristerParty.APPLICANT))
            .thenReturn(bc);
        when(bc.getRemoved()).thenReturn(Set.of(applicantBarristers));
    }

    private static BarristerCollectionItem buildBarristerCollectionItem(String userId, String orgId) {
        return buildBarristerCollectionItem(userId, orgId, null);
    }

    private static BarristerCollectionItem buildBarristerCollectionItem(String userId, String orgId, String email) {
        return BarristerCollectionItem.builder()
            .value(Barrister.builder()
                .email(email)
                .userId(userId)
                .organisation(organisation(orgId))
                .build())
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

    private void verifySendCorrespondenceEventForLetter(SendCorrespondenceEvent event, NotificationParty party,
                                                        FinremCaseDetails caseDetails, FinremCaseDetails caseDetailsBefore,
                                                        List<CaseDocument> documentsToPost) {
        assertThat(event)
            .extracting(
                SendCorrespondenceEvent::isLetterNotificationOnly,
                SendCorrespondenceEvent::getNotificationParties,
                SendCorrespondenceEvent::getCaseDetails,
                SendCorrespondenceEvent::getCaseDetailsBefore,
                SendCorrespondenceEvent::getAuthToken,
                SendCorrespondenceEvent::getDocumentsToPost
            )
            .contains(true, List.of(party), caseDetails, caseDetailsBefore, AUTH_TOKEN, documentsToPost);
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
