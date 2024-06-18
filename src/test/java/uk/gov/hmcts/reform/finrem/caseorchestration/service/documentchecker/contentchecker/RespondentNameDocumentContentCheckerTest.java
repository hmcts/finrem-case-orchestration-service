package uk.gov.hmcts.reform.finrem.caseorchestration.service.documentchecker.contentchecker;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseData;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.wrapper.ContactDetailsWrapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.utils.StringDecorator;

import java.util.Arrays;

import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.hmcts.reform.finrem.caseorchestration.util.TestResource.getConsentedFinremCaseDetailsBuilder;

class RespondentNameDocumentContentCheckerTest {

    private final RespondentNameDocumentContentChecker underTest = new RespondentNameDocumentContentChecker();

    @ParameterizedTest
    @ValueSource(strings = {
        "The respondent is Joe Bloggs",
        "whatever"})
    void givenCaseData_whenContentContainsNameMatchesRespondentFirstNameAndLastName(String validContent) {
        Arrays.stream(StringDecorator.values()).forEach(validContentDecorator ->
            Arrays.stream(StringDecorator.values()).forEach(fmNameDecorator ->
                Arrays.stream(StringDecorator.values()).forEach(lnameDecorator ->
                    assertThat(underTest.getWarning(
                        getConsentedFinremCaseDetailsBuilder(FinremCaseData.builder()
                            .contactDetailsWrapper(ContactDetailsWrapper.builder()
                                .respondentFmName(fmNameDecorator.decorate("Joe"))
                                .respondentLname(lnameDecorator.decorate("Bloggs"))
                                .build())).build(),
                        new String[]{validContentDecorator.decorate(validContent)}))
                        .isNull()
                )
            )
        );
    }

    @ParameterizedTest
    @ValueSource(strings = {
        "The respondent is Joe",
        "whatever"})
    void givenCaseData_whenContentContainsNameMatchesRespondentFirstNameAndEmptyLastName(String validContent) {
        Arrays.stream(StringDecorator.values()).forEach(validContentDecorator ->
            Arrays.stream(StringDecorator.values()).forEach(fmNameDecorator ->
                assertThat(underTest.getWarning(
                    getConsentedFinremCaseDetailsBuilder(FinremCaseData.builder()
                        .contactDetailsWrapper(ContactDetailsWrapper.builder()
                            .respondentFmName(fmNameDecorator.decorate("Joe"))
                            .build())).build(),
                    new String[]{validContentDecorator.decorate(validContent)}))
                    .isNull()
            )
        );
    }

    @Test
    void givenCaseData_whenContentContainsNameDoesNotMatchRespondentFirstNameAndLastName() {
        assertThat(underTest.getWarning(
            getConsentedFinremCaseDetailsBuilder(FinremCaseData.builder()
                .contactDetailsWrapper(ContactDetailsWrapper.builder()
                    .respondentFmName("Joe")
                    .respondentLname("Bloggs")
                    .build())).build(),
            new String[] {"The respondent is Amy Clarks"}))
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
