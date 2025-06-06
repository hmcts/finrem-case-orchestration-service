package uk.gov.hmcts.reform.finrem.caseorchestration.handler.solicitorcreatecase.mandatorydatavalidation;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.NullSource;
import org.junit.jupiter.params.provider.ValueSource;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.mapper.FinremCaseDetailsMapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseData;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;

class ApplicantSolicitorDetailsValidatorTest {

    private FinremCaseDetailsMapper finremCaseDetailsMapper;

    enum NullOrEmptyTest {
        EMPTY_STRING,
        SET_NULL
    }

    @BeforeEach
    void setup() {
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
        finremCaseDetailsMapper = new FinremCaseDetailsMapper(objectMapper);
    }

    @ParameterizedTest
    @ValueSource(strings = {"No"})
    @NullSource
    void shouldReturnEmptyError(String applicantRepresented) {
        Map<String, Object> map = getApplicantInfosMap(applicantRepresented);

        CaseDetails cd = CaseDetails.builder().caseTypeId(CaseType.CONSENTED.getCcdType()).data(map).build();

        FinremCaseData caseData = finremCaseDetailsMapper.mapToFinremCaseDetails(cd).getData();
        ApplicantSolicitorDetailsValidator validator = new ApplicantSolicitorDetailsValidator();
        List<String> validationErrors = validator.validate(caseData);
        assertThat(validationErrors).isEmpty();
    }

    @Test
    void shouldReturnEmptyErrorIfContestedCase() {
        CaseDetails cd = CaseDetails.builder().caseTypeId(CaseType.CONTESTED.getCcdType())
            .data(getApplicantOrganisationPolicyMap()).build();

        FinremCaseData caseData = finremCaseDetailsMapper.mapToFinremCaseDetails(cd).getData();
        ApplicantSolicitorDetailsValidator validator = new ApplicantSolicitorDetailsValidator();
        List<String> validationErrors = validator.validate(caseData);
        assertThat(validationErrors).isEmpty();
    }

    @Test
    void shouldReturnEmptyErrorIfApplicantRepresentedIsNo() {
        CaseDetails cd = CaseDetails.builder().caseTypeId(CaseType.CONSENTED.getCcdType())
            .data(Map.of("applicantRepresented", "No")).build();

        FinremCaseData caseData = finremCaseDetailsMapper.mapToFinremCaseDetails(cd).getData();
        ApplicantSolicitorDetailsValidator validator = new ApplicantSolicitorDetailsValidator();
        List<String> validationErrors = validator.validate(caseData);
        assertThat(validationErrors).isEmpty();
    }

    private static Stream<Arguments> parameters() {
        return Stream.of(
            Arguments.of("solicitorEmail", "Applicant solicitor's email is required."),
            Arguments.of("solicitorPhone", "Applicant solicitor's phone is required."),
            Arguments.of("solicitorFirm", "Applicant solicitor's name of your firm is required."),
            Arguments.of("solicitorName", "Applicant solicitor's name is required."),
            Arguments.of("solicitorAddress", "Applicant solicitor's address is required.")
        );
    }

    @ParameterizedTest
    @MethodSource("parameters")
    void shouldReturnSingleError(String fieldName, String expectedError) {
        Arrays.stream(NullOrEmptyTest.values()).forEach(t -> {
            Map<String, Object> map = getApplicantInfosMap("Yes");
            if (t == NullOrEmptyTest.EMPTY_STRING) {
                if ("solicitorAddress".equals(fieldName)) {
                    // skip solicitorAddress if it's not a string
                    return;
                }
                map.put(fieldName, "");
            } else {
                map.remove(fieldName);
            }
            CaseDetails cd = CaseDetails.builder().caseTypeId(CaseType.CONSENTED.getCcdType()).data(map).build();

            FinremCaseData caseData = finremCaseDetailsMapper.mapToFinremCaseDetails(cd).getData();
            ApplicantSolicitorDetailsValidator validator = new ApplicantSolicitorDetailsValidator();
            List<String> validationErrors = validator.validate(caseData);
            assertThat(validationErrors).containsExactly(expectedError);
        });
    }

    @ParameterizedTest
    @MethodSource("parameters")
    void shouldReturnMultipleErrors(String fieldName, String expectedError) {
        Arrays.stream(NullOrEmptyTest.values()).forEach(t -> {
            Map<String, Object> map = getApplicantInfosMap("Yes");
            // Remove solicitorAddress to trigger its error
            map.remove("solicitorAddress");
            if ("solicitorAddress".equals(fieldName)) {
                // skip parameter: solicitorAddress
                return;
            }
            if (t == NullOrEmptyTest.EMPTY_STRING) {
                map.put(fieldName, "");
            } else {
                map.remove(fieldName);
            }
            CaseDetails cd = CaseDetails.builder().caseTypeId(CaseType.CONSENTED.getCcdType()).data(map).build();

            FinremCaseData caseData = finremCaseDetailsMapper.mapToFinremCaseDetails(cd).getData();
            ApplicantSolicitorDetailsValidator validator = new ApplicantSolicitorDetailsValidator();
            List<String> validationErrors = validator.validate(caseData);
            assertThat(validationErrors).containsExactly("Applicant solicitor's address is required.",
                expectedError);
        });
    }

    @ParameterizedTest
    @EnumSource(value = CaseType.class, names = {"CONSENTED", "CONTESTED"})
    void givenConsentedCase_whenApplicantOrganisationPolicyMissing_thenReturnError(CaseType caseType) {
        Map<String, Object> map = getApplicantInfosMap("Yes");
        map.remove("ApplicantOrganisationPolicy");
        CaseDetails cd = CaseDetails.builder().caseTypeId(caseType.getCcdType()).data(map).build();

        FinremCaseData caseData = finremCaseDetailsMapper.mapToFinremCaseDetails(cd).getData();
        ApplicantSolicitorDetailsValidator validator = new ApplicantSolicitorDetailsValidator();
        List<String> validationErrors = validator.validate(caseData);
        assertThat(validationErrors).containsExactly("Applicant organisation policy is missing.");
    }

    @ParameterizedTest
    @EnumSource(value = CaseType.class, names = {"CONSENTED", "CONTESTED"})
    void givenConsentedCase_whenApplicantNotRepresentedAndOrganisationPolicyMissing_thenNoError(CaseType caseType) {
        Map<String, Object> map = getApplicantInfosMap("No");
        map.remove("ApplicantOrganisationPolicy");
        CaseDetails cd = CaseDetails.builder().caseTypeId(caseType.getCcdType()).data(map).build();

        FinremCaseData caseData = finremCaseDetailsMapper.mapToFinremCaseDetails(cd).getData();
        ApplicantSolicitorDetailsValidator validator = new ApplicantSolicitorDetailsValidator();
        List<String> validationErrors = validator.validate(caseData);
        assertThat(validationErrors).isEmpty();
    }

    private static Map<String, Object> getApplicantOrganisationPolicyMap() {
        return Map.of(
            "ApplicantOrganisationPolicy", Map.of(
                "Organisation", Map.of(
                    "OrganisationID", "FinRem-1-Org"
                )
            )
        );
    }

    private static Map<String, Object> getApplicantInfosMap(String applicantRepresented) {
        Map<String, Object> map = new HashMap<>(Map.of(
            "ApplicantOrganisationPolicy", Map.of(
                "Organisation", Map.of(
                    "OrganisationID", "FinRem-1-Org"
                )
            ),
            "solicitorEmail", "this_is@email.com",
            "solicitorPhone", "9999999999",
            "solicitorName", "SOLICITOR NAME",
            "solicitorFirm", "SOLICITOR FIRM",
            "solicitorAddress", Map.of("PostCode", "GU2 7NY")));
        map.put("applicantRepresented", applicantRepresented);
        return map;
    }
}
