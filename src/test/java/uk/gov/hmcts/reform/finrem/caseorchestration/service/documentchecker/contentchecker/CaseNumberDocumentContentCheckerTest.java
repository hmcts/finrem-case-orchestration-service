package uk.gov.hmcts.reform.finrem.caseorchestration.service.documentchecker.contentchecker;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import uk.gov.hmcts.reform.finrem.caseorchestration.FinremCaseDetailsBuilderFactory;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.utils.StringDecorator;

import java.util.Arrays;

import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.hmcts.reform.finrem.caseorchestration.TestConstants.CASE_ID;

class CaseNumberDocumentContentCheckerTest {

    private final CaseNumberDocumentContentChecker underTest = new CaseNumberDocumentContentChecker();

    @ParameterizedTest
    @ValueSource(strings = {"Case number 1234567890", "whatever"})
    void givenCaseData_whenContentContainCaseNumber(String validContent) {
        Arrays.stream(StringDecorator.values()).forEach(validContentDecorator ->
            assertThat(underTest.getWarning(FinremCaseDetailsBuilderFactory.from().build(),
                new String[]{validContentDecorator.decorate(validContent)})).isNull());
    }

    @Test
    void givenCaseData_whenContentDoesNotMatchCaseNumber() {
        assertThat(underTest.getWarning(FinremCaseDetailsBuilderFactory.from(Long.valueOf(CASE_ID)).build(),
            new String[] {"Case number 1234567891"})).isEqualTo("Case numbers may not match");
    }

    @ParameterizedTest
    @ValueSource(strings = {"Case number 1234567890", "whatever"})
    void givenCaseDataWithoutId_whenContentDoesNotMatchCaseNumber(String content) {
        Arrays.stream(StringDecorator.values()).forEach(contentDecorator ->
            assertThat(underTest.getWarning(FinremCaseDetails.builder().build(), new String[] {contentDecorator.decorate(content)})).isNull()
        );
    }

}
