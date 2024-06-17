package uk.gov.hmcts.reform.finrem.caseorchestration.service.documentchecker.contentchecker;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseData;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseDetails;

import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.hmcts.reform.finrem.caseorchestration.util.TestResource.getConsentedFinremCaseDetailsBuilder;

class CaseNumberDocumentContentCheckerTest {

    private final CaseNumberDocumentContentChecker underTest = new CaseNumberDocumentContentChecker();

    @ParameterizedTest
    @ValueSource(strings = {
        "Case number 1234567890",
        "Case number 1234567890 ",
        " Case number 1234567890",
        " Case number 1234567890 ",
        "whatever"})
    void givenCaseData_whenContentContainCaseNumber(String validContent) {
        assertThat(underTest.getWarning(getConsentedFinremCaseDetailsBuilder(FinremCaseData.builder()).build(),
            new String[] {validContent})).isNull();
    }

    @Test
    void givenCaseData_whenContentDoesNotMatchCaseNumber() {
        assertThat(underTest.getWarning(getConsentedFinremCaseDetailsBuilder(FinremCaseData.builder()).build(),
            new String[] {"Case number 1234567891"})).isEqualTo("Case numbers may not match");
    }

    @ParameterizedTest
    @ValueSource(strings = {
        "Case number 1234567890",
        "Case number 1234567890 ",
        " Case number 1234567890",
        " Case number 1234567890 ",
        "whatever"})
    void givenCaseDataWithoutId_whenContentDoesNotMatchCaseNumber(String content) {
        assertThat(underTest.getWarning(FinremCaseDetails.builder().build(), new String[] {content})).isNull();
    }

}
