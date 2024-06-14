package uk.gov.hmcts.reform.finrem.caseorchestration.service.documentchecker.contentchecker;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseData;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.wrapper.ContactDetailsWrapper;

import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.hmcts.reform.finrem.caseorchestration.util.TestResource.getConsentedFinremCaseDetailsBuilder;

class ApplicantNameDocumentContentCheckerTest {

    private final ApplicantNameDocumentContentChecker underTest = new ApplicantNameDocumentContentChecker();

    @ParameterizedTest
    @ValueSource(strings = {
        "The applicant is Joe Bloggs",
        "The applicant is Joe Bloggs ",
        " The applicant is Joe Blogg",
        " The applicant is Joe Bloggs "})
    void givenCaseData_whenContentContainsNameMatchesApplicantFirstNameAndLastName(String validContent) {
        assertThat(underTest.getWarning(
            getConsentedFinremCaseDetailsBuilder(FinremCaseData.builder()
                .contactDetailsWrapper(ContactDetailsWrapper.builder()
                    .applicantFmName("Joe")
                    .applicantLname("Bloggs")
                    .build())).build(),
            new String[] {validContent}))
            .isNull();
    }

    @Test
    void givenCaseData_whenContentContainsNameDoesNotMatchApplicantFirstNameAndLastName() {
        assertThat(underTest.getWarning(
            getConsentedFinremCaseDetailsBuilder(FinremCaseData.builder()
                .contactDetailsWrapper(ContactDetailsWrapper.builder()
                    .applicantFmName("Joey")
                    .applicantLname("Bloggs")
                    .build())).build(),
            new String[] {"1. The applicant is Joe Bloggs"}))
            .isEqualTo("Applicant name may not match");
    }

    @Test
    void givenCaseData_whenOneOfTheContentsContainsNameDoesNotMatchApplicantFirstNameAndLastName() {
        assertThat(underTest.getWarning(
            getConsentedFinremCaseDetailsBuilder(FinremCaseData.builder()
                .contactDetailsWrapper(ContactDetailsWrapper.builder()
                    .applicantFmName("Joey")
                    .applicantLname("Bloggs")
                    .build())).build(),
            new String[] {"1. The applicant is Joe Bloggs", "Whatever", "Whenever"}))
            .isEqualTo("Applicant name may not match");
    }

    @Test
    void givenCaseData_whenEmptyContentProvided() {
        assertThat(underTest.getWarning(
            getConsentedFinremCaseDetailsBuilder(FinremCaseData.builder()
                .contactDetailsWrapper(ContactDetailsWrapper.builder()
                    .applicantFmName("Joey")
                    .applicantLname("Bloggs")
                    .build())).build(),
            new String[] {}))
            .isNull();
    }

    @Test
    void givenCaseDataWithoutContactDetailsWrapper_whenContentProvided() {
        assertThat(underTest.getWarning(
            getConsentedFinremCaseDetailsBuilder(FinremCaseData.builder()).build(),
            new String[] {"1. The applicant is Joe Bloggs"}))
            .isNull();
    }
}
