package uk.gov.hmcts.reform.finrem.caseorchestration.service.documentchecker.contentchecker;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseData;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.wrapper.ContactDetailsWrapper;

import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.hmcts.reform.finrem.caseorchestration.util.TestResource.getConsentedFinremCaseDetailsBuilder;

class RespondentNameDocumentContentCheckerTest {

    private final RespondentNameDocumentContentChecker underTest = new RespondentNameDocumentContentChecker();

    @ParameterizedTest
    @ValueSource(strings = {
        "The respondent is Joe Bloggs",
        "The respondent is Joe Bloggs ",
        " The respondent is Joe Bloggs",
        " The respondent is Joe Bloggs "})
    void givenCaseData_whenContentContainsNameMatchesRespondentFirstNameAndLastName(String validContent) {
        assertThat(underTest.getWarning(
            getConsentedFinremCaseDetailsBuilder(FinremCaseData.builder()
                .contactDetailsWrapper(ContactDetailsWrapper.builder()
                    .respondentFmName("Joe")
                    .respondentLname("Bloggs")
                    .build())).build(),
            new String[] {validContent}))
            .isNull();
    }

    @Test
    void givenCaseData_whenContentContainsNameDoesNotMatchRespondentFirstNameAndLastName() {
        assertThat(underTest.getWarning(
            getConsentedFinremCaseDetailsBuilder(FinremCaseData.builder()
                .contactDetailsWrapper(ContactDetailsWrapper.builder()
                    .respondentFmName("Joey")
                    .respondentLname("Bloggs")
                    .build())).build(),
            new String[] {"The respondent is Joe Bloggs"}))
            .isEqualTo("Respondent name may not match");
    }

    @Test
    void givenCaseData_whenOneOfTheContentsContainsNameDoesNotMatchRespondentFirstNameAndLastName() {
        assertThat(underTest.getWarning(
            getConsentedFinremCaseDetailsBuilder(FinremCaseData.builder()
                .contactDetailsWrapper(ContactDetailsWrapper.builder()
                    .respondentFmName("Joey")
                    .respondentLname("Bloggs")
                    .build())).build(),
            new String[] {"The respondent is Joe Bloggs", "Whatever", "Whenever"}))
            .isEqualTo("Respondent name may not match");
    }

    @Test
    void givenCaseData_whenEmptyContentProvided() {
        assertThat(underTest.getWarning(
            getConsentedFinremCaseDetailsBuilder(FinremCaseData.builder()
                .contactDetailsWrapper(ContactDetailsWrapper.builder()
                    .respondentFmName("Joey")
                    .respondentLname("Bloggs")
                    .build())).build(),
            new String[] {}))
            .isNull();
    }

    @Test
    void givenCaseDataWithoutContactDetailsWrapper_whenContentProvided() {
        assertThat(underTest.getWarning(
            getConsentedFinremCaseDetailsBuilder(FinremCaseData.builder()).build(),
            new String[] {"The respondent is Joe Bloggs"}))
            .isNull();
    }
}
