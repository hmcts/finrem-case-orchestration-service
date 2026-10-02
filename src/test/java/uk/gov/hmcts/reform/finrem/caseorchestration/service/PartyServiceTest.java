package uk.gov.hmcts.reform.finrem.caseorchestration.service;

import org.assertj.core.groups.Tuple;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.CsvSource;
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
import java.util.function.Consumer;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.tuple;
import static org.junit.jupiter.params.provider.Arguments.arguments;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.mock;
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

    @BeforeEach
    void setUp() {
        lenient().when(finremCaseDetails.getData()).thenReturn(finremCaseData);
    }

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

    @ParameterizedTest
    @CsvSource({
        "APP_SOLICITOR,  true,  false",
        "APP_BARRISTER,  true,  false",
        "RESP_SOLICITOR, false, true",
        "RESP_BARRISTER, false, true"
    })
    void givenSelectedParty_shouldSetCorrespondenceEnabledOnlyForThatParty(CaseRole caseRole,
                                                                           boolean expectedApplicantEnabled,
                                                                           boolean expectedRespondentEnabled) {
        FinremCaseData caseData = spy(FinremCaseData.builder().build());
        when(caseData.getPartiesOnCase()).thenReturn(dynamicMultiSelectList(caseRole));

        IntervenerOne intervenerOne = mock(IntervenerOne.class);
        when(caseData.getIntervenerOne()).thenReturn(intervenerOne);
        IntervenerTwo intervenerTwo = mock(IntervenerTwo.class);
        when(caseData.getIntervenerTwo()).thenReturn(intervenerTwo);
        IntervenerThree intervenerThree = mock(IntervenerThree.class);
        when(caseData.getIntervenerThree()).thenReturn(intervenerThree);
        IntervenerFour intervenerFour = mock(IntervenerFour.class);
        when(caseData.getIntervenerFour()).thenReturn(intervenerFour);
        when(finremCaseDetails.getData()).thenReturn(caseData);

        partyService.updateCorrespondenceEnabledFromSelectedParties(finremCaseDetails);

        verify(caseData).setApplicantCorrespondenceEnabled(expectedApplicantEnabled);
        verify(caseData).setRespondentCorrespondenceEnabled(expectedRespondentEnabled);
        verify(intervenerOne).setIntervenerCorrespondenceEnabled(false);
        verify(intervenerTwo).setIntervenerCorrespondenceEnabled(false);
        verify(intervenerThree).setIntervenerCorrespondenceEnabled(false);
        verify(intervenerFour).setIntervenerCorrespondenceEnabled(false);
    }

    @ParameterizedTest
    @CsvSource({
        "INTVR_SOLICITOR_1, 1",
        "INTVR_BARRISTER_1, 1",
        "INTVR_SOLICITOR_2, 2",
        "INTVR_BARRISTER_2, 2",
        "INTVR_SOLICITOR_3, 3",
        "INTVR_BARRISTER_3, 3",
        "INTVR_SOLICITOR_4, 4",
        "INTVR_BARRISTER_4, 4"
    })
    void givenSelectedIntervener_shouldEnableCorrespondenceOnlyForThatIntervener(CaseRole caseRole,
                                                                                 int selectedIntervener) {
        FinremCaseData caseData = spy(FinremCaseData.builder().build());
        when(caseData.getPartiesOnCase()).thenReturn(dynamicMultiSelectList(caseRole));

        IntervenerOne intervenerOne = mock(IntervenerOne.class);
        when(caseData.getIntervenerOne()).thenReturn(intervenerOne);
        IntervenerTwo intervenerTwo = mock(IntervenerTwo.class);
        when(caseData.getIntervenerTwo()).thenReturn(intervenerTwo);
        IntervenerThree intervenerThree = mock(IntervenerThree.class);
        when(caseData.getIntervenerThree()).thenReturn(intervenerThree);
        IntervenerFour intervenerFour = mock(IntervenerFour.class);
        when(caseData.getIntervenerFour()).thenReturn(intervenerFour);
        when(finremCaseDetails.getData()).thenReturn(caseData);

        partyService.updateCorrespondenceEnabledFromSelectedParties(finremCaseDetails);

        verify(caseData).setApplicantCorrespondenceEnabled(false);
        verify(caseData).setRespondentCorrespondenceEnabled(false);
        verify(intervenerOne).setIntervenerCorrespondenceEnabled(selectedIntervener == 1);
        verify(intervenerTwo).setIntervenerCorrespondenceEnabled(selectedIntervener == 2);
        verify(intervenerThree).setIntervenerCorrespondenceEnabled(selectedIntervener == 3);
        verify(intervenerFour).setIntervenerCorrespondenceEnabled(selectedIntervener == 4);
    }

    private static DynamicMultiSelectList dynamicMultiSelectList(CaseRole... selectedCaseRoles) {
        return DynamicMultiSelectList.builder()
            .value(Arrays.stream(selectedCaseRoles).map(CaseRole::getCcdCode)
                .map(ccdCode -> DynamicMultiSelectListElement.builder().code(ccdCode).build())
                .toList())
            .build();
    }

    private static Stream<Arguments> intervenerStubs() {
        return Stream.of(
            arguments("Intervener1 - Intervener A", (Consumer<FinremCaseData>) data ->
                when(data.getIntervenerOneWrapperIfPopulated()).thenReturn(IntervenerOne.builder()
                    .intervenerName("Intervener A")
                    .intervenerOrganisation(organisationPolicy(TEST_ORG_ID, INTERVENER_ORG_POLICY_ASSIGNED_ROLE))
                    .build())),
            arguments("Intervener2 - Intervener A", (Consumer<FinremCaseData>) data ->
                when(data.getIntervenerTwoWrapperIfPopulated()).thenReturn(IntervenerTwo.builder()
                    .intervenerName("Intervener A")
                    .intervenerOrganisation(organisationPolicy(TEST_ORG_ID, INTERVENER_ORG_POLICY_ASSIGNED_ROLE))
                    .build())),
            arguments("Intervener3 - Intervener A", (Consumer<FinremCaseData>) data ->
                when(data.getIntervenerThreeWrapperIfPopulated()).thenReturn(IntervenerThree.builder()
                    .intervenerName("Intervener A")
                    .intervenerOrganisation(organisationPolicy(TEST_ORG_ID, INTERVENER_ORG_POLICY_ASSIGNED_ROLE))
                    .build())),
            arguments("Intervener4 - Intervener A", (Consumer<FinremCaseData>) data ->
                when(data.getIntervenerFourWrapperIfPopulated()).thenReturn(IntervenerFour.builder()
                    .intervenerName("Intervener A")
                    .intervenerOrganisation(organisationPolicy(TEST_ORG_ID, INTERVENER_ORG_POLICY_ASSIGNED_ROLE))
                    .build()))
        );
    }
}
