package uk.gov.hmcts.reform.finrem.caseorchestration.service.stoprepresentingclient;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.NullSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.TestConstants;
import uk.gov.hmcts.reform.finrem.caseorchestration.mapper.FinremCaseDetailsMapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.Barrister;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.BarristerCollectionItem;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseRole;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.ChangeOrganisationRequest;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseData;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.NoticeOfChangeParty;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.OrganisationPolicy;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.YesOrNo;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.wrapper.BarristerCollectionWrapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.wrapper.ContactDetailsWrapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.wrapper.IntervenerFour;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.wrapper.IntervenerOne;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.wrapper.IntervenerTwo;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.wrapper.intevener.IntervenerWrapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.AssignCaseAccessService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.CaseRoleService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.IdamService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.SystemUserService;

import java.util.HashMap;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.finrem.caseorchestration.TestConstants.AUTH_TOKEN;
import static uk.gov.hmcts.reform.finrem.caseorchestration.TestConstants.TEST_ORG2_ID;
import static uk.gov.hmcts.reform.finrem.caseorchestration.TestConstants.TEST_ORG_ID;
import static uk.gov.hmcts.reform.finrem.caseorchestration.TestConstants.TEST_SYSTEM_TOKEN;
import static uk.gov.hmcts.reform.finrem.caseorchestration.TestConstants.TEST_USER_ID;
import static uk.gov.hmcts.reform.finrem.caseorchestration.TestSetUpUtils.organisation;
import static uk.gov.hmcts.reform.finrem.caseorchestration.TestSetUpUtils.organisationPolicy;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseRole.RESP_SOLICITOR;
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
    private IdamService idamService;

    @Mock
    private CaseRoleService caseRoleService;

    private StopRepresentingClientService underTest;

    @BeforeEach
    void setup() {
        underTest = spy(new StopRepresentingClientService(assignCaseAccessService, systemUserService, finremCaseDetailsMapper,
            caseRoleService, idamService));
        lenient().when(systemUserService.getSysUserToken()).thenReturn(TEST_SYSTEM_TOKEN);
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
            OrganisationPolicy.getDefaultOrganisationPolicy(RESP_SOLICITOR);

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

    @Test
    void shouldReturnIntervener_whenIntervenerSolicitorOrgChange() {
        FinremCaseData caseData = FinremCaseData.builder()
            .intervenerOne(IntervenerOne.builder().build())
            .build();
        IntervenerOne originalWrapper = null;
        FinremCaseData caseDataBefore = FinremCaseData.builder()
            .intervenerOne(originalWrapper = IntervenerOne.builder()
                .intervenerOrganisation(organisationPolicy(TEST_ORG2_ID))
                .build())
            .build();

        FinremCaseDetails caseDetails = FinremCaseDetails.builder().data(caseData).build();
        FinremCaseDetails caseDetailsBefore = FinremCaseDetails.builder().data(caseDataBefore).build();

        StopRepresentingClientInfo info = StopRepresentingClientInfo.builder()
            .caseDetails(caseDetails)
            .caseDetailsBefore(caseDetailsBefore)
            .build();

        List<IntervenerWrapper> actual = underTest.getToBeRevokedIntervenerSolicitors(info);
        assertThat(actual).containsExactly(originalWrapper);
    }

    @Test
    void shouldReturnMultipleInterveners_whenIntervenerSolicitorOrgChange() {
        FinremCaseData caseData = FinremCaseData.builder()
            .intervenerOne(IntervenerOne.builder().build())
            .intervenerTwo(IntervenerTwo.builder().build())
            .build();
        IntervenerOne originalWrapper = null;
        IntervenerTwo originalTwoWrapper = null;
        FinremCaseData caseDataBefore = FinremCaseData.builder()
            .intervenerTwo(originalTwoWrapper = IntervenerTwo.builder()
                .intervenerOrganisation(organisationPolicy(TEST_ORG2_ID))
                .build())
            .intervenerOne(originalWrapper = IntervenerOne.builder()
                .intervenerOrganisation(organisationPolicy(TEST_ORG2_ID))
                .build())
            .build();

        FinremCaseDetails caseDetails = FinremCaseDetails.builder().data(caseData).build();
        FinremCaseDetails caseDetailsBefore = FinremCaseDetails.builder().data(caseDataBefore).build();

        StopRepresentingClientInfo info = StopRepresentingClientInfo.builder()
            .caseDetails(caseDetails)
            .caseDetailsBefore(caseDetailsBefore)
            .build();

        List<IntervenerWrapper> actual = underTest.getToBeRevokedIntervenerSolicitors(info);
        assertThat(actual).containsExactly(originalWrapper, originalTwoWrapper);
    }

    @Nested
    class RevokeApplicantSolicitorOrRespondentSolicitorTests {

        CaseDetails mockClonnedCaseDetails(FinremCaseDetails finremCaseDetails) {
            CaseDetails clonnedCaseDetails = CaseDetails.builder().data(new HashMap<>()).build();
            when(finremCaseDetailsMapper.mapToCaseDetails(finremCaseDetails)).thenReturn(clonnedCaseDetails);
            return clonnedCaseDetails;
        }

        NoticeOfChangeParty resolveNoticeOfChangeParty(boolean isApplicant) {
            return isApplicant ? NoticeOfChangeParty.APPLICANT : NoticeOfChangeParty.RESPONDENT;
        }

        static Stream<ChangeOrganisationRequest> shouldReturnNoNocInvolved() {
            ChangeOrganisationRequest mockedChangeOrganisationRequest = mock(ChangeOrganisationRequest.class);
            when(mockedChangeOrganisationRequest.isNoOrganisationsToAddOrRemove()).thenReturn(true);

            return Stream.of(mockedChangeOrganisationRequest);
        }

        @ParameterizedTest
        @NullSource
        @MethodSource
        void shouldReturnNoNocInvolved(ChangeOrganisationRequest changeOrganisationRequest) {
            // given
            FinremCaseData finremCaseData = mock(FinremCaseData.class);
            FinremCaseData originalFinremCaseData = mock(FinremCaseData.class);
            StopRepresentingClientInfo info = mock(StopRepresentingClientInfo.class);
            when(info.getFinremCaseData()).thenReturn(finremCaseData);
            when(info.getFinremCaseDataBefore()).thenReturn(originalFinremCaseData);

            when(finremCaseData.getChangeOrganisationRequestField()).thenReturn(changeOrganisationRequest);

            // when
            LitigantRevocation result =
                underTest.revokeApplicantSolicitorOrRespondentSolicitor(info);

            // then
            assertFalse(result.applicantSolicitorRevoked());
            assertFalse(result.respondentSolicitorRevoked());
        }

        @ParameterizedTest
        @ValueSource(booleans = {true, false})
        void shouldPerformNocWorkflow_whenChangeOrganisationRequestIsSet(boolean revokingApplicant) {

            // given
            FinremCaseData finremCaseData = spy(FinremCaseData.builder()
                .contactDetailsWrapper(ContactDetailsWrapper.builder()
                    .nocParty(resolveNoticeOfChangeParty(revokingApplicant))
                    .build())
                .build());
            FinremCaseData originalFinremCaseData = mock(FinremCaseData.class);
            lenient().when(originalFinremCaseData.getApplicantOrganisationPolicy()).thenReturn(organisationPolicy(TEST_ORG_ID));
            lenient().when(originalFinremCaseData.getRespondentOrganisationPolicy()).thenReturn(organisationPolicy(TEST_ORG2_ID));
            StopRepresentingClientInfo info = mock(StopRepresentingClientInfo.class);
            when(info.getFinremCaseData()).thenReturn(finremCaseData);
            when(info.getFinremCaseDataBefore()).thenReturn(originalFinremCaseData);

            ChangeOrganisationRequest changeOrganisationRequest = mock(ChangeOrganisationRequest.class);
            when(changeOrganisationRequest.isNoOrganisationsToAddOrRemove()).thenReturn(false);
            when(finremCaseData.getChangeOrganisationRequestField()).thenReturn(changeOrganisationRequest);

            CaseDetails cloned = mockClonnedCaseDetails(info.getCaseDetails());

            when(systemUserService.getSysUserToken()).thenReturn(TEST_SYSTEM_TOKEN);

            // when
            final LitigantRevocation result =
                underTest.revokeApplicantSolicitorOrRespondentSolicitor(info);

            // then
            verify(assignCaseAccessService).applyDecision(TEST_SYSTEM_TOKEN, cloned);
            verify(finremCaseDetailsMapper).mapToCaseDetails(info.getCaseDetails());
            verify(systemUserService).getSysUserToken();

            String expectedKey = revokingApplicant
                ? "ApplicantOrganisationPolicy"
                : "RespondentOrganisationPolicy";

            Object expectedValue = revokingApplicant
                ? organisationPolicy(TEST_ORG_ID)
                : organisationPolicy(TEST_ORG2_ID);

            assertAll(
                () -> assertThat(cloned.getData()).containsEntry(expectedKey, expectedValue),
                () -> assertThat(result.applicantSolicitorRevoked()).isEqualTo(revokingApplicant),
                () -> assertThat(result.respondentSolicitorRevoked()).isEqualTo(!revokingApplicant)
            );
        }

        static Stream<NoticeOfChangeParty> shouldThrowIllegalStateException_whenUnrecognizedNocPartyIsProvided() {
            return Stream.of(mock(NoticeOfChangeParty.class));
        }

        @ParameterizedTest
        @NullSource
        @MethodSource
        void shouldThrowIllegalStateException_whenUnrecognizedNocPartyIsProvided(NoticeOfChangeParty nocParty) {

            // given
            FinremCaseData finremCaseData = spy(FinremCaseData.builder()
                .contactDetailsWrapper(ContactDetailsWrapper.builder()
                    .nocParty(nocParty)
                    .build())
                .build());
            FinremCaseData originalFinremCaseData = mock(FinremCaseData.class);
            StopRepresentingClientInfo info = mock(StopRepresentingClientInfo.class);
            when(info.getFinremCaseData()).thenReturn(finremCaseData);
            when(info.getFinremCaseDataBefore()).thenReturn(originalFinremCaseData);

            ChangeOrganisationRequest changeOrganisationRequest = mock(ChangeOrganisationRequest.class);
            when(changeOrganisationRequest.isNoOrganisationsToAddOrRemove()).thenReturn(false);
            when(finremCaseData.getChangeOrganisationRequestField()).thenReturn(changeOrganisationRequest);

            assertThrows(IllegalStateException.class, () ->
                underTest.revokeApplicantSolicitorOrRespondentSolicitor(info));
            verify(assignCaseAccessService, never()).applyDecision(anyString(), any(CaseDetails.class));
            verify(finremCaseDetailsMapper, never()).mapToCaseDetails(info.getCaseDetails());
            verify(systemUserService, never()).getSysUserToken();
        }
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

            when(caseRoleService.getIntervenerType(caseData, AUTH_TOKEN)).thenReturn(Optional.of(INTERVENER_TWO));
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

    private static BarristerCollectionItem buildBarristerCollectionItem(String userId, String orgId) {
        return BarristerCollectionItem.builder()
            .value(Barrister.builder()
                .userId(userId)
                .organisation(organisation(orgId))
                .build())
            .build();
    }
}
