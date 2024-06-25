package uk.gov.hmcts.reform.finrem.caseorchestration.service.documentchecker.contentchecker;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import uk.gov.hmcts.reform.finrem.caseorchestration.FinremCaseDetailsBuilderFactory;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseData;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.wrapper.ContactDetailsWrapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.utils.StringDecorator;

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseType.CONSENTED;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseType.CONTESTED;

class RespondentNameDocumentContentCheckerTest {

    private final RespondentNameDocumentContentChecker underTest = new RespondentNameDocumentContentChecker();

    @Nested
    class ContestedAndConsentedTests {
        private static Stream<Arguments> contestedAndConsentedCases() {
            return Stream.of(
                Arguments.of(CONTESTED, "respondentFmName", "respondentLname"),
                Arguments.of(CONSENTED, "appRespondentFmName", "appRespondentLName")
            );
        }

        @ParameterizedTest
        @MethodSource("contestedAndConsentedCases")
        void givenCaseData_whenContentContainsNameMatchesRespondentFirstNameAndLastName(
            CaseType caseType, String firstNameField, String lastNameField) {
            List<String> testContents = List.of("The respondent is Joe Bloggs", "whatever", "");
            testContents.forEach(validContent ->
                Arrays.stream(StringDecorator.values()).forEach(validContentDecorator ->
                    Arrays.stream(StringDecorator.values()).forEach(fmNameDecorator ->
                        Arrays.stream(StringDecorator.values()).forEach(lnameDecorator ->
                            assertThat(underTest.getWarning(
                                createCaseDetails(caseType, firstNameField, fmNameDecorator.decorate("Joe"), lastNameField,
                                    lnameDecorator.decorate("Bloggs")), new String[]{validContentDecorator.decorate(validContent)}))
                                .isNull()
                        )
                    )
                )
            );
        }

        @ParameterizedTest
        @MethodSource("contestedAndConsentedCases")
        void givenCaseData_whenContentContainsNameMatchesRespondentFirstNameAndEmptyLastName(
            CaseType caseType, String firstNameField, String lastNameField) {
            List<String> testContents = List.of("The respondent is Joe", "whatever", "");
            testContents.forEach(validContent ->
                Arrays.stream(StringDecorator.values()).forEach(validContentDecorator ->
                    Arrays.stream(StringDecorator.values()).forEach(fmNameDecorator ->
                        assertThat(underTest.getWarning(
                            createCaseDetails(caseType, firstNameField, fmNameDecorator.decorate("Joe"), lastNameField, null),
                            new String[]{validContentDecorator.decorate(validContent)}))
                            .isNull()
                    )
                )
            );
        }

        @ParameterizedTest
        @MethodSource("contestedAndConsentedCases")
        void givenCaseData_whenContentContainsNameDoesNotMatchRespondentFirstNameAndLastName(
            CaseType caseType, String firstNameField, String lastNameField) {
            assertThat(underTest.getWarning(
                createCaseDetails(caseType, firstNameField, "Joe", lastNameField, "Bloggs"),
                new String[]{"The respondent is Amy Clarks"}))
                .isEqualTo("Respondent name may not match");
        }

        @ParameterizedTest
        @MethodSource("contestedAndConsentedCases")
        void givenCaseData_whenOneOfTheContentsContainsNameDoesNotMatchRespondentFirstNameAndLastName(
            CaseType caseType, String firstNameField, String lastNameField) {
            assertThat(underTest.getWarning(
                createCaseDetails(caseType, firstNameField, "Joey", lastNameField, "Bloggs"),
                new String[]{"The respondent is Joe Bloggs", "Whatever", "Whenever"}))
                .isEqualTo("Respondent name may not match");
        }

        @ParameterizedTest
        @MethodSource("contestedAndConsentedCases")
        void givenCaseData_whenEmptyContentProvided(
            CaseType caseType, String firstNameField, String lastNameField) {
            assertThat(underTest.getWarning(
                createCaseDetails(caseType, firstNameField, "Joey", lastNameField, "Bloggs"),
                new String[]{}))
                .isNull();
        }

        private FinremCaseDetails createCaseDetails(CaseType caseType, String firstNameField, String firstName, String lastNameField,
                                                    String lastName) {
            ContactDetailsWrapper.ContactDetailsWrapperBuilder contactDetailsWrapperBuilder = ContactDetailsWrapper.builder();
            ContactDetailsWrapper wrapper = contactDetailsWrapperBuilder.build();

            try {
                if (firstNameField != null) {
                    Field fmNameField = ContactDetailsWrapper.class.getDeclaredField(firstNameField);
                    fmNameField.setAccessible(true);
                    fmNameField.set(wrapper, firstName);
                }

                if (lastNameField != null) {
                    Field lnameField = ContactDetailsWrapper.class.getDeclaredField(lastNameField);
                    lnameField.setAccessible(true);
                    lnameField.set(wrapper, lastName);
                }
            } catch (NoSuchFieldException | IllegalAccessException e) {
                throw new RuntimeException(e);
            }

            FinremCaseData.FinremCaseDataBuilder caseDataBuilder = FinremCaseData.builder()
                .contactDetailsWrapper(wrapper);

            return FinremCaseDetailsBuilderFactory.from(caseType, caseDataBuilder).build();
        }
    }

    @Test
    void givenCaseDataWithoutContactDetailsWrapper_whenContentProvided() {
        assertThat(underTest.getWarning(FinremCaseDetailsBuilderFactory.from().build(), new String[] {"The respondent is Joe Bloggs"})).isNull();
    }
}
