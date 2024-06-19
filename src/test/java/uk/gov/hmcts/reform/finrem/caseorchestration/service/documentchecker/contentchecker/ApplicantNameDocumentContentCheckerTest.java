package uk.gov.hmcts.reform.finrem.caseorchestration.service.documentchecker.contentchecker;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import uk.gov.hmcts.reform.finrem.caseorchestration.FinremCaseDetailsBuilderFactory;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseData;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.wrapper.ContactDetailsWrapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.utils.StringDecorator;

import java.util.Arrays;

import static org.assertj.core.api.Assertions.assertThat;

class ApplicantNameDocumentContentCheckerTest {

    private final ApplicantNameDocumentContentChecker underTest = new ApplicantNameDocumentContentChecker();

    @ParameterizedTest
    @ValueSource(strings = {
        "1. The applicant is Joe Bloggs",
        "whatever"})
    void givenCaseData_whenContentContainsNameMatchesApplicantFirstNameAndLastName(String validContent) {
        Arrays.stream(StringDecorator.values()).forEach(validContentDecorator ->
            Arrays.stream(StringDecorator.values()).forEach(fmNameDecorator ->
                Arrays.stream(StringDecorator.values()).forEach(lnameDecorator ->
                    assertThat(underTest.getWarning(
                        FinremCaseDetailsBuilderFactory.from(FinremCaseData.builder()
                            .contactDetailsWrapper(ContactDetailsWrapper.builder()
                                .applicantFmName(fmNameDecorator.decorate("Joe"))
                                .applicantLname(lnameDecorator.decorate("Bloggs"))
                                .build())).build(),
                        new String[]{validContentDecorator.decorate(validContent)}))
                        .isNull()
                )
            )
        );
    }

    @ParameterizedTest
    @ValueSource(strings = {
        "1. The applicant is Joe",
        "whatever"})
    void givenCaseData_whenContentContainsNameMatchesApplicantFirstNameAndEmptyLastName(String validContent) {
        Arrays.stream(StringDecorator.values()).forEach(validContentDecorator ->
            Arrays.stream(StringDecorator.values()).forEach(fmNameDecorator ->
                assertThat(underTest.getWarning(
                    FinremCaseDetailsBuilderFactory.from(FinremCaseData.builder()
                        .contactDetailsWrapper(ContactDetailsWrapper.builder()
                            .applicantFmName(fmNameDecorator.decorate("Joe"))
                            .build())).build(),
                    new String[]{validContentDecorator.decorate(validContent)}))
                    .isNull()
            )
        );
    }

    @Test
    void givenCaseData_whenContentContainsNameDoesNotMatchApplicantFirstNameAndLastName() {
        assertThat(underTest.getWarning(
            FinremCaseDetailsBuilderFactory.from(FinremCaseData.builder()
                .contactDetailsWrapper(ContactDetailsWrapper.builder()
                    .applicantFmName("Joe")
                    .applicantLname("Bloggs")
                    .build())).build(),
            new String[] {"1. The applicant is Amy Clarks"}))
            .isEqualTo("Applicant name may not match");
    }

    @Test
    void givenCaseData_whenOneOfTheContentsContainsNameDoesNotMatchApplicantFirstNameAndLastName() {
        assertThat(underTest.getWarning(
            FinremCaseDetailsBuilderFactory.from(FinremCaseData.builder()
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
            FinremCaseDetailsBuilderFactory.from(FinremCaseData.builder()
                .contactDetailsWrapper(ContactDetailsWrapper.builder()
                    .applicantFmName("Joey")
                    .applicantLname("Bloggs")
                    .build())).build(),
            new String[] {}))
            .isNull();
    }

    @Test
    void givenCaseDataWithoutContactDetailsWrapper_whenContentProvided() {
        assertThat(underTest.getWarning(FinremCaseDetailsBuilderFactory.from().build(), new String[] {"1. The applicant is Joe Bloggs"})).isNull();
    }
}
