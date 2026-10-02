package uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.Spy;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.wrapper.ContactDetailsWrapper;

import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.clearInvocations;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;

class FinremCaseDetailsTest {

    @Spy
    private FinremCaseData finremCaseData;

    @Spy
    private ContactDetailsWrapper contactDetailsWrapper;

    @Spy
    private OrganisationPolicy organisationPolicy;

    @Spy
    private Organisation organisation;

    private FinremCaseDetails underTest;

    @BeforeEach
    void setUp() {
        underTest = spy(new FinremCaseDetails());
        finremCaseData = spy(new FinremCaseData());
        contactDetailsWrapper = spy(new ContactDetailsWrapper());
        organisationPolicy = spy(new OrganisationPolicy());
        organisation = spy(new Organisation());

        lenient().doReturn(finremCaseData).when(underTest).getData();
        lenient().when(finremCaseData.getContactDetailsWrapper()).thenReturn(contactDetailsWrapper);

        clearInvocations(underTest, finremCaseData);
    }

    @ParameterizedTest(name = "isApplicantSolicitorDigital_returnValue_{0}")
    @MethodSource("applicantSolicitorDigitalScenarios")
    void isApplicantSolicitorDigital_returnValue_conditionDescription(String scenario,
                                                                      YesOrNo represented,
                                                                      String orgId,
                                                                      String solicitorEmail,
                                                                      boolean expected) {
        when(contactDetailsWrapper.getApplicantRepresented()).thenReturn(represented);

        if (YesOrNo.isYes(represented)) {
            if (orgId != null) {
                when(finremCaseData.getApplicantOrganisationPolicy()).thenReturn(organisationPolicy);
                when(organisationPolicy.getOrganisation()).thenReturn(organisation);
                when(organisation.getOrganisationID()).thenReturn(orgId);
            } else {
                when(finremCaseData.getApplicantOrganisationPolicy()).thenReturn(null);
            }
            if (isNotBlankSafe(orgId)) {
                lenient().doReturn(solicitorEmail).when(underTest).getAppSolicitorEmail();
            }
        }

        boolean result = underTest.isApplicantSolicitorDigital();

        assertThat(result).isEqualTo(expected);
    }

    private static Stream<Arguments> applicantSolicitorDigitalScenarios() {
        return Stream.of(
            Arguments.of(
                "notRepresented_returnsFalse", YesOrNo.NO, null, null, false),
            Arguments.of(
                "representedNullPolicy_returnsFalse", YesOrNo.YES, null, "email@test.com", false),
            Arguments.of(
                "representedBlankOrgId_returnsFalse", YesOrNo.YES, "", "email@test.com", false),
            Arguments.of(
                "representedValidOrgIdBlankEmail_returnsFalse", YesOrNo.YES, "ORG123", "", false),
            Arguments.of(
                "representedValidOrgIdNullEmail_returnsFalse", YesOrNo.YES, "ORG123", null, false),
            Arguments.of(
                "representedValidOrgIdAndEmail_returnsTrue", YesOrNo.YES, "ORG123", "email@test.com", true)
        );
    }

    @ParameterizedTest(name = "isRespondentSolicitorDigital_returnValue_{0}")
    @MethodSource("respondentSolicitorDigitalScenarios")
    void isRespondentSolicitorDigital_returnValue_conditionDescription(String scenario,
                                                                       YesOrNo consentedRepresented,
                                                                       YesOrNo contestedRepresented,
                                                                       String orgId,
                                                                       String solicitorEmail,
                                                                       boolean expected) {
        when(contactDetailsWrapper.getConsentedRespondentRepresented()).thenReturn(consentedRepresented);

        if (!YesOrNo.isYes(consentedRepresented)) {
            when(contactDetailsWrapper.getContestedRespondentRepresented()).thenReturn(contestedRepresented);
        }

        boolean represented = YesOrNo.isYes(consentedRepresented) || YesOrNo.isYes(contestedRepresented);

        if (represented) {
            if (orgId != null) {
                when(finremCaseData.getRespondentOrganisationPolicy()).thenReturn(organisationPolicy);
                when(organisationPolicy.getOrganisation()).thenReturn(organisation);
                when(organisation.getOrganisationID()).thenReturn(orgId);
            } else {
                when(finremCaseData.getRespondentOrganisationPolicy()).thenReturn(null);
            }
            if (isNotBlankSafe(orgId)) {
                lenient().doReturn(solicitorEmail).when(underTest).getRespSolicitorEmail();
            }
        }

        boolean result = underTest.isRespondentSolicitorDigital();

        assertThat(result).isEqualTo(expected);
    }

    private static Stream<Arguments> respondentSolicitorDigitalScenarios() {
        return Stream.of(
            Arguments.of(
                "neitherRepresented_returnsFalse", YesOrNo.NO, YesOrNo.NO, null, null, false),
            Arguments.of(
                "consentedRepresentedNullPolicy_returnsFalse", YesOrNo.YES, YesOrNo.NO, null, "email@test.com", false),
            Arguments.of(
                "contestedRepresentedBlankOrgId_returnsFalse", YesOrNo.NO, YesOrNo.YES, "", "email@test.com", false),
            Arguments.of(
                "representedValidOrgIdBlankEmail_returnsFalse", YesOrNo.YES, YesOrNo.NO, "ORG123", "", false),
            Arguments.of(
                "representedValidOrgIdNullEmail_returnsFalse", YesOrNo.NO, YesOrNo.YES, "ORG123", null, false),
            Arguments.of(
                "consentedRepresentedValidOrgIdAndEmail_returnsTrue", YesOrNo.YES, YesOrNo.NO, "ORG123", "email@test.com", true),
            Arguments.of(
                "contestedRepresentedValidOrgIdAndEmail_returnsTrue", YesOrNo.NO, YesOrNo.YES, "ORG123", "email@test.com", true)
        );
    }

    private static boolean isNotBlankSafe(String value) {
        return value != null && !value.isBlank();
    }
}
