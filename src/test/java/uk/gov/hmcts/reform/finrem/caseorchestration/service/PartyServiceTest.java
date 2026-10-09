package uk.gov.hmcts.reform.finrem.caseorchestration.service;

import org.assertj.core.groups.Tuple;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseRole;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.DynamicMultiSelectList;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.DynamicMultiSelectListElement;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseData;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.wrapper.IntervenerFour;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.wrapper.IntervenerOne;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.wrapper.IntervenerThree;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.wrapper.IntervenerTwo;

import java.util.Arrays;
import java.util.List;
import java.util.function.Consumer;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.tuple;
import static org.junit.jupiter.params.provider.Arguments.arguments;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.finrem.caseorchestration.TestConstants.TEST_ORG_ID;
import static uk.gov.hmcts.reform.finrem.caseorchestration.TestSetUpUtils.organisationPolicy;

@ExtendWith(MockitoExtension.class)
class PartyServiceTest {

    private static final String APPLICANT_ORG_POLICY_ASSIGNED_ROLE = "APPLICANT_ORG_POLICY_ASSIGNED_ROLE";
    private static final String RESPONDENT_ORG_POLICY_ASSIGNED_ROLE = "RESPONDENT_ORG_POLICY_ASSIGNED_ROLE";
    private static final String INTERVENER_ORG_POLICY_ASSIGNED_ROLE = "INTERVENER_ORG_POLICY_ASSIGNED_ROLE";
    private static final String INTERVENER_TWO_ORG_POLICY_ASSIGNED_ROLE = "INTERVENER_TWO_ORG_POLICY_ASSIGNED_ROLE";

    private static final String APPLICANT_LABEL = "Applicant - Applicant A";
    private static final String RESPONDENT_LABEL = "Respondent - Respondent A";

    // Represented parties (code is the organisation policy assigned role)
    private static final Tuple REPRESENTED_APPLICANT_TUPLE =
        tuple(APPLICANT_ORG_POLICY_ASSIGNED_ROLE, APPLICANT_LABEL);
    private static final Tuple REPRESENTED_RESPONDENT_TUPLE =
        tuple(RESPONDENT_ORG_POLICY_ASSIGNED_ROLE, RESPONDENT_LABEL);

    // Unrepresented parties (code falls back to the solicitor case role)
    private static final Tuple UNREPRESENTED_APPLICANT_TUPLE =
        tuple(CaseRole.APP_SOLICITOR.getCcdCode(), APPLICANT_LABEL);
    private static final Tuple UNREPRESENTED_RESPONDENT_TUPLE =
        tuple(CaseRole.RESP_SOLICITOR.getCcdCode(), RESPONDENT_LABEL);

    @InjectMocks
    private PartyService partyService;

    @Mock
    private FinremCaseDetails finremCaseDetails;

    @Mock
    private FinremCaseData finremCaseData;

    @Mock
    private IntervenerOne intervenerOne;

    @Mock
    private IntervenerTwo intervenerTwo;

    @Mock
    private IntervenerThree intervenerThree;

    @Mock
    private IntervenerFour intervenerFour;

    @BeforeEach
    void setUp() {
        lenient().when(finremCaseDetails.getData()).thenReturn(finremCaseData);
    }

    // ---------------------------------------------------------------------
    // getAllActivePartyList
    // ---------------------------------------------------------------------

    @Test
    void givenOnlyApplicantRepresented_getAllActivePartyList_returnActivePartyList() {
        when(finremCaseData.getApplicantOrganisationPolicy())
            .thenReturn(organisationPolicy(TEST_ORG_ID, APPLICANT_ORG_POLICY_ASSIGNED_ROLE));
        when(finremCaseData.getFullApplicantName()).thenReturn("Applicant A");
        when(finremCaseData.getRespondentFullName()).thenReturn("Respondent A");

        DynamicMultiSelectList dynamicMultiSelectList = partyService.getAllActivePartyList(finremCaseDetails);

        assertThat(dynamicMultiSelectList.getValue())
            .extracting(DynamicMultiSelectListElement::getCode, DynamicMultiSelectListElement::getLabel)
            .containsExactly(REPRESENTED_APPLICANT_TUPLE, UNREPRESENTED_RESPONDENT_TUPLE);
        assertThat(dynamicMultiSelectList.getListItems())
            .extracting(DynamicMultiSelectListElement::getCode, DynamicMultiSelectListElement::getLabel)
            .containsExactly(REPRESENTED_APPLICANT_TUPLE, UNREPRESENTED_RESPONDENT_TUPLE);
    }

    @Test
    void givenOnlyRespondentRepresented_getAllActivePartyList_returnActivePartyList() {
        when(finremCaseData.getRespondentOrganisationPolicy())
            .thenReturn(organisationPolicy(TEST_ORG_ID, RESPONDENT_ORG_POLICY_ASSIGNED_ROLE));
        when(finremCaseData.getFullApplicantName()).thenReturn("Applicant A");
        when(finremCaseData.getRespondentFullName()).thenReturn("Respondent A");

        DynamicMultiSelectList dynamicMultiSelectList = partyService.getAllActivePartyList(finremCaseDetails);

        assertThat(dynamicMultiSelectList.getValue())
            .extracting(DynamicMultiSelectListElement::getCode, DynamicMultiSelectListElement::getLabel)
            .containsExactly(REPRESENTED_RESPONDENT_TUPLE, UNREPRESENTED_APPLICANT_TUPLE);
        assertThat(dynamicMultiSelectList.getListItems())
            .extracting(DynamicMultiSelectListElement::getCode, DynamicMultiSelectListElement::getLabel)
            .containsExactly(REPRESENTED_RESPONDENT_TUPLE, UNREPRESENTED_APPLICANT_TUPLE);
    }

    @Test
    void givenBothPartiesRepresented_getAllActivePartyList_returnActivePartyList() {
        when(finremCaseData.getApplicantOrganisationPolicy())
            .thenReturn(organisationPolicy(TEST_ORG_ID, APPLICANT_ORG_POLICY_ASSIGNED_ROLE));
        when(finremCaseData.getRespondentOrganisationPolicy())
            .thenReturn(organisationPolicy(TEST_ORG_ID, RESPONDENT_ORG_POLICY_ASSIGNED_ROLE));
        when(finremCaseData.getFullApplicantName()).thenReturn("Applicant A");
        when(finremCaseData.getRespondentFullName()).thenReturn("Respondent A");

        DynamicMultiSelectList dynamicMultiSelectList = partyService.getAllActivePartyList(finremCaseDetails);

        assertThat(dynamicMultiSelectList.getValue())
            .extracting(DynamicMultiSelectListElement::getCode, DynamicMultiSelectListElement::getLabel)
            .containsExactly(REPRESENTED_APPLICANT_TUPLE, REPRESENTED_RESPONDENT_TUPLE);
        assertThat(dynamicMultiSelectList.getListItems())
            .extracting(DynamicMultiSelectListElement::getCode, DynamicMultiSelectListElement::getLabel)
            .containsExactly(REPRESENTED_APPLICANT_TUPLE, REPRESENTED_RESPONDENT_TUPLE);
    }

    @Test
    void givenBothPartiesUnrepresented_getAllActivePartyList_returnActivePartyList() {
        when(finremCaseData.getFullApplicantName()).thenReturn("Applicant A");
        when(finremCaseData.getRespondentFullName()).thenReturn("Respondent A");

        DynamicMultiSelectList dynamicMultiSelectList = partyService.getAllActivePartyList(finremCaseDetails);

        assertThat(dynamicMultiSelectList.getValue())
            .extracting(DynamicMultiSelectListElement::getCode, DynamicMultiSelectListElement::getLabel)
            .containsExactly(UNREPRESENTED_APPLICANT_TUPLE, UNREPRESENTED_RESPONDENT_TUPLE);
        assertThat(dynamicMultiSelectList.getListItems())
            .extracting(DynamicMultiSelectListElement::getCode, DynamicMultiSelectListElement::getLabel)
            .containsExactly(UNREPRESENTED_APPLICANT_TUPLE, UNREPRESENTED_RESPONDENT_TUPLE);
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("intervenerStubs")
    void givenIntervenerRepresented_getAllActivePartyList_thenIntervenerNotSelected(
        String intervenerLabel, Consumer<FinremCaseData> intervenerStub) {
        when(finremCaseData.getFullApplicantName()).thenReturn("Applicant A");
        when(finremCaseData.getRespondentFullName()).thenReturn("Respondent A");
        intervenerStub.accept(finremCaseData);

        DynamicMultiSelectList dynamicMultiSelectList = partyService.getAllActivePartyList(finremCaseDetails);

        Tuple intervenerTuple = tuple(INTERVENER_ORG_POLICY_ASSIGNED_ROLE, intervenerLabel);

        assertThat(dynamicMultiSelectList.getValue())
            .extracting(DynamicMultiSelectListElement::getCode, DynamicMultiSelectListElement::getLabel)
            .containsExactly(UNREPRESENTED_APPLICANT_TUPLE, UNREPRESENTED_RESPONDENT_TUPLE)
            .doesNotContain(intervenerTuple);
        assertThat(dynamicMultiSelectList.getListItems())
            .extracting(DynamicMultiSelectListElement::getCode, DynamicMultiSelectListElement::getLabel)
            .containsExactly(
                UNREPRESENTED_APPLICANT_TUPLE,
                UNREPRESENTED_RESPONDENT_TUPLE,
                intervenerTuple);
    }

    @Test
    void givenMultipleIntervenersRepresented_getAllActivePartyList_thenIntervenersNotSelected() {
        when(finremCaseData.getFullApplicantName()).thenReturn("Applicant A");
        when(finremCaseData.getRespondentFullName()).thenReturn("Respondent A");
        when(finremCaseData.getIntervenerOneWrapperIfPopulated()).thenReturn(IntervenerOne.builder()
            .intervenerName("Intervener A")
            .intervenerOrganisation(organisationPolicy(TEST_ORG_ID, INTERVENER_ORG_POLICY_ASSIGNED_ROLE))
            .build());
        when(finremCaseData.getIntervenerTwoWrapperIfPopulated()).thenReturn(IntervenerTwo.builder()
            .intervenerName("Intervener B")
            .intervenerOrganisation(organisationPolicy(TEST_ORG_ID, INTERVENER_TWO_ORG_POLICY_ASSIGNED_ROLE))
            .build());

        DynamicMultiSelectList dynamicMultiSelectList = partyService.getAllActivePartyList(finremCaseDetails);

        Tuple intervenerTuple = tuple(INTERVENER_ORG_POLICY_ASSIGNED_ROLE, "Intervener1 - Intervener A");
        Tuple intervener2Tuple = tuple(INTERVENER_TWO_ORG_POLICY_ASSIGNED_ROLE, "Intervener2 - Intervener B");

        assertThat(dynamicMultiSelectList.getValue())
            .extracting(DynamicMultiSelectListElement::getCode, DynamicMultiSelectListElement::getLabel)
            .containsExactly(UNREPRESENTED_APPLICANT_TUPLE, UNREPRESENTED_RESPONDENT_TUPLE)
            .doesNotContain(intervenerTuple, intervener2Tuple);
        assertThat(dynamicMultiSelectList.getListItems())
            .extracting(DynamicMultiSelectListElement::getCode, DynamicMultiSelectListElement::getLabel)
            .containsExactly(
                UNREPRESENTED_APPLICANT_TUPLE,
                UNREPRESENTED_RESPONDENT_TUPLE,
                intervenerTuple, intervener2Tuple);
    }

    // ---------------------------------------------------------------------
    // getCheckedActiveParties
    // ---------------------------------------------------------------------

    @Test
    void givenCheckedParties_getCheckedActiveParties_returnsCodesInOrder() {
        when(finremCaseData.getPartiesOnCase())
            .thenReturn(dynamicMultiSelectList(CaseRole.RESP_BARRISTER, CaseRole.APP_SOLICITOR));

        assertThat(partyService.getCheckedActiveParties(finremCaseDetails))
            .containsExactly(CaseRole.RESP_BARRISTER.getCcdCode(), CaseRole.APP_SOLICITOR.getCcdCode());
    }

    @Test
    void givenNoCheckedParties_getCheckedActiveParties_returnsEmptyList() {
        when(finremCaseData.getPartiesOnCase()).thenReturn(dynamicMultiSelectList());

        assertThat(partyService.getCheckedActiveParties(finremCaseDetails)).isEmpty();
    }

    @Test
    void givenNullPartiesOnCase_getCheckedActiveParties_returnsEmptyList() {
        when(finremCaseData.getPartiesOnCase()).thenReturn(null);

        assertThat(partyService.getCheckedActiveParties(finremCaseDetails)).isEmpty();
    }

    // ---------------------------------------------------------------------
    // updateCorrespondenceEnabledFromSelectedParties
    // ---------------------------------------------------------------------

    @ParameterizedTest(name = "{0}")
    @MethodSource("selectedRoleToExpectedFlags")
    void givenSelectedRole_updateCorrespondenceEnabled_enablesOnlyThatParty(
        CaseRole selectedRole, boolean applicant, boolean respondent,
        boolean intervener1, boolean intervener2, boolean intervener3, boolean intervener4) {
        FinremCaseData caseData = caseDataWithMockInterveners();
        when(caseData.getPartiesOnCase()).thenReturn(dynamicMultiSelectList(selectedRole));
        when(finremCaseDetails.getData()).thenReturn(caseData);

        partyService.updateCorrespondenceEnabledFromSelectedParties(finremCaseDetails);

        verifyFlags(caseData, applicant, respondent, intervener1, intervener2, intervener3, intervener4);
    }

    @Test
    void givenNoPartiesSelected_updateCorrespondenceEnabled_disablesEveryParty() {
        FinremCaseData caseData = caseDataWithMockInterveners();
        when(caseData.getPartiesOnCase()).thenReturn(dynamicMultiSelectList());
        when(finremCaseDetails.getData()).thenReturn(caseData);

        partyService.updateCorrespondenceEnabledFromSelectedParties(finremCaseDetails);

        verifyFlags(caseData, false, false, false, false, false, false);
    }

    @Test
    void givenSolicitorAndBarristerOfDifferentParties_updateCorrespondenceEnabled_enablesBoth() {
        FinremCaseData caseData = caseDataWithMockInterveners();
        when(caseData.getPartiesOnCase())
            .thenReturn(dynamicMultiSelectList(CaseRole.APP_BARRISTER, CaseRole.INTVR_SOLICITOR_3));
        when(finremCaseDetails.getData()).thenReturn(caseData);

        partyService.updateCorrespondenceEnabledFromSelectedParties(finremCaseDetails);

        verifyFlags(caseData, true, false, false, false, true, false);
    }

    // ---------------------------------------------------------------------
    // isApplicantPartySelected / isRespondentPartySelected
    // ---------------------------------------------------------------------

    @Test
    void givenApplicantRoleChecked_isApplicantPartySelected_returnsTrue() {
        when(finremCaseData.getApplicantOrganisationPolicy())
            .thenReturn(organisationPolicy(TEST_ORG_ID, APPLICANT_ORG_POLICY_ASSIGNED_ROLE));
        givenCheckedPartyCodes(APPLICANT_ORG_POLICY_ASSIGNED_ROLE, "SOME_OTHER_ROLE");

        assertThat(partyService.isApplicantPartySelected(finremCaseDetails)).isTrue();
    }

    @Test
    void givenApplicantRoleNotChecked_isApplicantPartySelected_returnsFalse() {
        when(finremCaseData.getApplicantOrganisationPolicy())
            .thenReturn(organisationPolicy(TEST_ORG_ID, APPLICANT_ORG_POLICY_ASSIGNED_ROLE));
        givenCheckedPartyCodes(RESPONDENT_ORG_POLICY_ASSIGNED_ROLE);

        assertThat(partyService.isApplicantPartySelected(finremCaseDetails)).isFalse();
    }

    @Test
    void givenNoApplicantPolicy_isApplicantPartySelected_returnsFalse() {
        when(finremCaseData.getApplicantOrganisationPolicy()).thenReturn(null);
        givenCheckedPartyCodes(APPLICANT_ORG_POLICY_ASSIGNED_ROLE);

        assertThat(partyService.isApplicantPartySelected(finremCaseDetails)).isFalse();
    }

    @Test
    void givenApplicantPolicyWithoutRole_isApplicantPartySelected_returnsFalse() {
        when(finremCaseData.getApplicantOrganisationPolicy())
            .thenReturn(organisationPolicy(TEST_ORG_ID, null));
        givenCheckedPartyCodes(APPLICANT_ORG_POLICY_ASSIGNED_ROLE);

        assertThat(partyService.isApplicantPartySelected(finremCaseDetails)).isFalse();
    }

    @Test
    void givenRespondentRoleChecked_isRespondentPartySelected_returnsTrue() {
        when(finremCaseData.getRespondentOrganisationPolicy())
            .thenReturn(organisationPolicy(TEST_ORG_ID, RESPONDENT_ORG_POLICY_ASSIGNED_ROLE));
        givenCheckedPartyCodes(RESPONDENT_ORG_POLICY_ASSIGNED_ROLE, "SOME_OTHER_ROLE");

        assertThat(partyService.isRespondentPartySelected(finremCaseDetails)).isTrue();
    }

    @Test
    void givenRespondentRoleNotChecked_isRespondentPartySelected_returnsFalse() {
        when(finremCaseData.getRespondentOrganisationPolicy())
            .thenReturn(organisationPolicy(TEST_ORG_ID, RESPONDENT_ORG_POLICY_ASSIGNED_ROLE));
        givenCheckedPartyCodes(APPLICANT_ORG_POLICY_ASSIGNED_ROLE);

        assertThat(partyService.isRespondentPartySelected(finremCaseDetails)).isFalse();
    }

    @Test
    void givenNoRespondentPolicy_isRespondentPartySelected_returnsFalse() {
        when(finremCaseData.getRespondentOrganisationPolicy()).thenReturn(null);
        givenCheckedPartyCodes(RESPONDENT_ORG_POLICY_ASSIGNED_ROLE);

        assertThat(partyService.isRespondentPartySelected(finremCaseDetails)).isFalse();
    }

    @Test
    void givenRespondentPolicyWithoutRole_isRespondentPartySelected_returnsFalse() {
        when(finremCaseData.getRespondentOrganisationPolicy())
            .thenReturn(organisationPolicy(TEST_ORG_ID, null));
        givenCheckedPartyCodes(RESPONDENT_ORG_POLICY_ASSIGNED_ROLE);

        assertThat(partyService.isRespondentPartySelected(finremCaseDetails)).isFalse();
    }

    // ---------------------------------------------------------------------
    // Helpers
    // ---------------------------------------------------------------------

    private void givenCheckedPartyCodes(String... codes) {
        when(finremCaseData.getPartiesOnCase()).thenReturn(dynamicMultiSelectListOfCodes(codes));
    }

    private FinremCaseData caseDataWithMockInterveners() {
        FinremCaseData caseData = spy(FinremCaseData.builder().build());
        when(caseData.getIntervenerOne()).thenReturn(intervenerOne);
        when(caseData.getIntervenerTwo()).thenReturn(intervenerTwo);
        when(caseData.getIntervenerThree()).thenReturn(intervenerThree);
        when(caseData.getIntervenerFour()).thenReturn(intervenerFour);
        return caseData;
    }

    private void verifyFlags(FinremCaseData caseData, boolean applicant, boolean respondent,
                             boolean intervener1, boolean intervener2,
                             boolean intervener3, boolean intervener4) {
        verify(caseData).setApplicantCorrespondenceEnabled(applicant);
        verify(caseData).setRespondentCorrespondenceEnabled(respondent);
        verify(intervenerOne).setIntervenerCorrespondenceEnabled(intervener1);
        verify(intervenerTwo).setIntervenerCorrespondenceEnabled(intervener2);
        verify(intervenerThree).setIntervenerCorrespondenceEnabled(intervener3);
        verify(intervenerFour).setIntervenerCorrespondenceEnabled(intervener4);
    }

    private static DynamicMultiSelectList dynamicMultiSelectList(CaseRole... selectedCaseRoles) {
        return dynamicMultiSelectListOfCodes(
            Arrays.stream(selectedCaseRoles).map(CaseRole::getCcdCode).toArray(String[]::new));
    }

    private static DynamicMultiSelectList dynamicMultiSelectListOfCodes(String... codes) {
        List<DynamicMultiSelectListElement> elements = Arrays.stream(codes)
            .map(code -> DynamicMultiSelectListElement.builder().code(code).build())
            .toList();
        return DynamicMultiSelectList.builder().value(elements).build();
    }

    // Columns: selected role, applicant, respondent, intervener 1, 2, 3, 4
    private static Stream<Arguments> selectedRoleToExpectedFlags() {
        return Stream.of(
            arguments(CaseRole.APP_SOLICITOR, true, false, false, false, false, false),
            arguments(CaseRole.APP_BARRISTER, true, false, false, false, false, false),
            arguments(CaseRole.RESP_SOLICITOR, false, true, false, false, false, false),
            arguments(CaseRole.RESP_BARRISTER, false, true, false, false, false, false),
            arguments(CaseRole.INTVR_SOLICITOR_1, false, false, true, false, false, false),
            arguments(CaseRole.INTVR_BARRISTER_1, false, false, true, false, false, false),
            arguments(CaseRole.INTVR_SOLICITOR_2, false, false, false, true, false, false),
            arguments(CaseRole.INTVR_BARRISTER_2, false, false, false, true, false, false),
            arguments(CaseRole.INTVR_SOLICITOR_3, false, false, false, false, true, false),
            arguments(CaseRole.INTVR_BARRISTER_3, false, false, false, false, true, false),
            arguments(CaseRole.INTVR_SOLICITOR_4, false, false, false, false, false, true),
            arguments(CaseRole.INTVR_BARRISTER_4, false, false, false, false, false, true)
        );
    }

    private static Stream<Arguments> intervenerStubs() {
        return Stream.of(
            arguments("Intervener1 - Intervener A", (Consumer<FinremCaseData>) data ->
                when(data.getIntervenerOneWrapperIfPopulated()).thenReturn(IntervenerOne.builder()
                    .intervenerName("Intervener A")
                    .intervenerOrganisation(organisationPolicy("TEST_ORG_ID_INTV1", INTERVENER_ORG_POLICY_ASSIGNED_ROLE))
                    .build())),
            arguments("Intervener2 - Intervener A", (Consumer<FinremCaseData>) data ->
                when(data.getIntervenerTwoWrapperIfPopulated()).thenReturn(IntervenerTwo.builder()
                    .intervenerName("Intervener A")
                    .intervenerOrganisation(organisationPolicy("TEST_ORG_ID_INTV2", INTERVENER_ORG_POLICY_ASSIGNED_ROLE))
                    .build())),
            arguments("Intervener3 - Intervener A", (Consumer<FinremCaseData>) data ->
                when(data.getIntervenerThreeWrapperIfPopulated()).thenReturn(IntervenerThree.builder()
                    .intervenerName("Intervener A")
                    .intervenerOrganisation(organisationPolicy("TEST_ORG_ID_INTV3", INTERVENER_ORG_POLICY_ASSIGNED_ROLE))
                    .build())),
            arguments("Intervener4 - Intervener A", (Consumer<FinremCaseData>) data ->
                when(data.getIntervenerFourWrapperIfPopulated()).thenReturn(IntervenerFour.builder()
                    .intervenerName("Intervener A")
                    .intervenerOrganisation(organisationPolicy("TEST_ORG_ID_INTV4", INTERVENER_ORG_POLICY_ASSIGNED_ROLE))
                    .build()))
        );
    }
}
