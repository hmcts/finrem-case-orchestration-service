package uk.gov.hmcts.reform.finrem.caseorchestration.service.barristers;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.finrem.caseorchestration.TestSetUpUtils;
import uk.gov.hmcts.reform.finrem.caseorchestration.helper.BarristerUpdateDifferenceCalculator;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.BarristerChange;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.Barrister;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.BarristerCollectionItem;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.BarristerParty;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseRole;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseData;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.wrapper.BarristerCollectionWrapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.AssignCaseAccessService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.PrdOrganisationService;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.finrem.caseorchestration.TestConstants.CASE_ID_IN_LONG;
import static uk.gov.hmcts.reform.finrem.caseorchestration.TestConstants.TEST_CASE_ROLE;
import static uk.gov.hmcts.reform.finrem.caseorchestration.TestConstants.TEST_ORG2_ID;
import static uk.gov.hmcts.reform.finrem.caseorchestration.TestConstants.TEST_ORG_ID;
import static uk.gov.hmcts.reform.finrem.caseorchestration.TestConstants.TEST_USER_ID;

@ExtendWith(MockitoExtension.class)
class ManageBarristerServiceTest {

    @InjectMocks
    @Spy
    private ManageBarristerService manageBarristerService;
    @Mock
    private BarristerUpdateDifferenceCalculator barristerUpdateDifferenceCalculator;
    @Mock
    private PrdOrganisationService organisationService;
    @Mock
    private AssignCaseAccessService assignCaseAccessService;

    @Test
    void givenCaseWorkerRunsManageBarristerEvent_whenGetManageBarristerParty_thenReturnSelectedParty() {
        FinremCaseDetails caseDetails = createCaseDetails(BarristerParty.APPLICANT);

        BarristerParty barristerParty = manageBarristerService.getManageBarristerParty(caseDetails, CaseRole.CASEWORKER);

        assertThat(barristerParty).isEqualTo(BarristerParty.APPLICANT);
    }

    @ParameterizedTest
    @MethodSource("getManageBarristerPartyData")
    void givenSolicitorRunsManageBarristerEvent_whenGetManageBarristerParty_thenReturnPartyFromUserRole(
        CaseRole caseRole, BarristerParty expectedBarristerParty) {
        FinremCaseDetails caseDetails = createCaseDetails(null);

        BarristerParty barristerParty = manageBarristerService.getManageBarristerParty(caseDetails, caseRole);

        assertThat(barristerParty).isEqualTo(expectedBarristerParty);
    }

    private static Stream<Arguments> getManageBarristerPartyData() {
        return Stream.of(
            Arguments.of(CaseRole.APP_SOLICITOR, BarristerParty.APPLICANT),
            Arguments.of(CaseRole.RESP_SOLICITOR, BarristerParty.RESPONDENT),
            Arguments.of(CaseRole.INTVR_SOLICITOR_1, BarristerParty.INTERVENER1),
            Arguments.of(CaseRole.INTVR_SOLICITOR_2, BarristerParty.INTERVENER2),
            Arguments.of(CaseRole.INTVR_SOLICITOR_3, BarristerParty.INTERVENER3),
            Arguments.of(CaseRole.INTVR_SOLICITOR_4, BarristerParty.INTERVENER4)
        );
    }

    @Test
    void givenUnexpectedCaseRoleRunsManageBarristerEvent_whenGetManageBarristerParty_thenThrowsException() {
        FinremCaseDetails caseDetails = createCaseDetails(null);

        assertThatThrownBy(() -> manageBarristerService.getManageBarristerParty(caseDetails, CaseRole.APP_BARRISTER))
            .isInstanceOf(IllegalStateException.class)
            .hasMessage("Case ID 1234567890: Unexpected case role value APP_BARRISTER");
    }

    @ParameterizedTest
    @MethodSource
    void testGetBarristerCaseRole(BarristerParty barristerParty, CaseRole expectedCaseRole) {
        CaseRole caseRole = manageBarristerService.getBarristerCaseRole(barristerParty);
        assertThat(caseRole).isEqualTo(expectedCaseRole);
    }

    private static Stream<Arguments> testGetBarristerCaseRole() {
        return Stream.of(
            Arguments.of(BarristerParty.APPLICANT, CaseRole.APP_BARRISTER),
            Arguments.of(BarristerParty.RESPONDENT, CaseRole.RESP_BARRISTER),
            Arguments.of(BarristerParty.INTERVENER1, CaseRole.INTVR_BARRISTER_1),
            Arguments.of(BarristerParty.INTERVENER2, CaseRole.INTVR_BARRISTER_2),
            Arguments.of(BarristerParty.INTERVENER3, CaseRole.INTVR_BARRISTER_3),
            Arguments.of(BarristerParty.INTERVENER4, CaseRole.INTVR_BARRISTER_4)
        );
    }

    @ParameterizedTest
    @MethodSource
    void testGetEventBarristers(BarristerParty barristerParty, Function<BarristerCollectionWrapper, List<BarristerCollectionItem>> collectionGetter) {
        FinremCaseDetails caseDetails = createCaseDetails(barristerParty);

        List<BarristerCollectionItem> barristers = manageBarristerService.getEventBarristers(caseDetails.getData(), barristerParty);

        assertThat(barristers).isEqualTo(collectionGetter.apply(caseDetails.getData().getBarristerCollectionWrapper()));
    }

    private static Stream<Arguments> testGetEventBarristers() {
        return Stream.of(
            Arguments.of(
                BarristerParty.APPLICANT,
                (Function<BarristerCollectionWrapper, List<BarristerCollectionItem>>) BarristerCollectionWrapper::getApplicantBarristers
            ),
            Arguments.of(
                BarristerParty.RESPONDENT,
                (Function<BarristerCollectionWrapper, List<BarristerCollectionItem>>) BarristerCollectionWrapper::getRespondentBarristers
            ),
            Arguments.of(
                BarristerParty.INTERVENER1,
                (Function<BarristerCollectionWrapper, List<BarristerCollectionItem>>) BarristerCollectionWrapper::getIntvr1Barristers
            ),
            Arguments.of(
                BarristerParty.INTERVENER2,
                (Function<BarristerCollectionWrapper, List<BarristerCollectionItem>>) BarristerCollectionWrapper::getIntvr2Barristers
            ),
            Arguments.of(
                BarristerParty.INTERVENER3,
                (Function<BarristerCollectionWrapper, List<BarristerCollectionItem>>) BarristerCollectionWrapper::getIntvr3Barristers
            ),
            Arguments.of(
                BarristerParty.INTERVENER4,
                (Function<BarristerCollectionWrapper, List<BarristerCollectionItem>>) BarristerCollectionWrapper::getIntvr4Barristers
            )
        );
    }

    @Test
    void testAddUserIdToBarristerData() {
        when(organisationService.findUserByEmail("barrister1@test.com")).thenReturn(Optional.of("barrister1-userid"));
        when(organisationService.findUserByEmail("barrister2@test.com")).thenReturn(Optional.of("barrister2-userid"));

        List<BarristerCollectionItem> barristers = List.of(
            BarristerCollectionItem.builder()
                .value(Barrister.builder()
                    .email("barrister1@test.com")
                    .build())
                .build(),
            BarristerCollectionItem.builder()
                .value(Barrister.builder()
                    .email("barrister2@test.com")
                    .build())
                .build()
        );

        manageBarristerService.addUserIdToBarristerData(barristers);

        assertThat(barristers.getFirst().getValue().getUserId()).isEqualTo("barrister1-userid");
        assertThat(barristers.getLast().getValue().getUserId()).isEqualTo("barrister2-userid");
    }

    @Test
    void testGetBarristerChange() {
        FinremCaseDetails caseDetails = createCaseDetails(BarristerParty.APPLICANT);
        FinremCaseDetails caseDetailsBefore = FinremCaseDetails.builder()
            .data(FinremCaseData.builder().build())
            .build();

        List<Barrister> barristers = caseDetails.getData().getBarristerCollectionWrapper().getApplicantBarristers().stream()
            .map(BarristerCollectionItem::getValue).toList();
        List<Barrister> barristersBefore = Collections.emptyList();

        BarristerChange calculatedBarristerChange = BarristerChange.builder().build();
        when(barristerUpdateDifferenceCalculator.calculate(BarristerParty.APPLICANT, barristersBefore, barristers))
            .thenReturn(calculatedBarristerChange);

        BarristerChange barristerChange = manageBarristerService.getBarristerChange(caseDetails,
            caseDetailsBefore.getData(), CaseRole.CASEWORKER);

        assertThat(barristerChange).isEqualTo(calculatedBarristerChange);
    }

    @Test
    void testExecuteBarristerChange() {
        BarristerParty barristerParty = mock(BarristerParty.class);

        BarristerChange barristerChange = mock(BarristerChange.class);
        when(barristerChange.getBarristerParty()).thenReturn(barristerParty);

        Barrister barristerAdded = mock(Barrister.class);
        when(barristerAdded.getUserId()).thenReturn(TEST_USER_ID);
        when(barristerAdded.getOrganisation()).thenReturn(TestSetUpUtils.organisation(TEST_ORG_ID));
        when(barristerChange.getAdded()).thenReturn(Set.of(barristerAdded));
        Barrister barristerRemoved = mock(Barrister.class);
        when(barristerRemoved.getUserId()).thenReturn("userIdToBeRemoved");
        when(barristerRemoved.getOrganisation()).thenReturn(TestSetUpUtils.organisation(TEST_ORG2_ID));
        when(barristerChange.getRemoved()).thenReturn(Set.of(barristerRemoved));

        CaseRole caseRole = mock(CaseRole.class);
        when(caseRole.getCcdCode()).thenReturn(TEST_CASE_ROLE);
        when(manageBarristerService.getBarristerCaseRole(barristerParty)).thenReturn(caseRole);

        manageBarristerService.executeBarristerChange(CASE_ID_IN_LONG, barristerChange);

        verify(assignCaseAccessService).grantCaseRoleToUser(CASE_ID_IN_LONG, TEST_USER_ID, TEST_CASE_ROLE, TEST_ORG_ID);
        verify(assignCaseAccessService).removeCaseRoleToUser(CASE_ID_IN_LONG, "userIdToBeRemoved", TEST_CASE_ROLE,
            TEST_ORG2_ID);
    }

    @Test
    void testExecuteBarristerChangeWhenRemoveUserIdMissing() {
        BarristerParty barristerParty = mock(BarristerParty.class);

        BarristerChange barristerChange = mock(BarristerChange.class);
        when(barristerChange.getBarristerParty()).thenReturn(barristerParty);

        Barrister barristerAdded = mock(Barrister.class);
        when(barristerAdded.getUserId()).thenReturn(TEST_USER_ID);
        when(barristerAdded.getOrganisation()).thenReturn(TestSetUpUtils.organisation(TEST_ORG_ID));
        when(barristerChange.getAdded()).thenReturn(
            Set.of(barristerAdded)
        );
        Barrister barristerRemoved = mock(Barrister.class);
        when(barristerRemoved.getUserId()).thenReturn(null);
        when(barristerRemoved.getOrganisation()).thenReturn(TestSetUpUtils.organisation(TEST_ORG2_ID));
        when(barristerChange.getRemoved()).thenReturn(
            Set.of(barristerRemoved)
        );

        CaseRole caseRole = mock(CaseRole.class);
        when(caseRole.getCcdCode()).thenReturn(TEST_CASE_ROLE);
        when(manageBarristerService.getBarristerCaseRole(barristerParty)).thenReturn(caseRole);

        manageBarristerService.executeBarristerChange(CASE_ID_IN_LONG, barristerChange);

        verify(assignCaseAccessService).grantCaseRoleToUser(CASE_ID_IN_LONG, TEST_USER_ID, TEST_CASE_ROLE, TEST_ORG_ID);
        verify(assignCaseAccessService, never()).removeCaseRoleToUser(eq(CASE_ID_IN_LONG), anyString(), eq(TEST_CASE_ROLE),
            eq(TEST_ORG2_ID));
    }

    private FinremCaseDetails createCaseDetails(BarristerParty barristerParty) {
        FinremCaseData caseData = FinremCaseData.builder()
            .barristerParty(barristerParty)
            .barristerCollectionWrapper(BarristerCollectionWrapper.builder()
                .applicantBarristers(createBarristers())
                .respondentBarristers(createBarristers())
                .intvr1Barristers(createBarristers())
                .intvr2Barristers(createBarristers())
                .intvr3Barristers(createBarristers())
                .intvr4Barristers(createBarristers())
                .build())
            .build();
        return FinremCaseDetails.builder()
            .id(CASE_ID_IN_LONG)
            .data(caseData)
            .build();
    }

    private List<BarristerCollectionItem> createBarristers() {
        return List.of(
            BarristerCollectionItem.builder()
                .id(UUID.randomUUID().toString())
                .value(Barrister.builder()
                    .build())
                .build()
        );
    }
}
