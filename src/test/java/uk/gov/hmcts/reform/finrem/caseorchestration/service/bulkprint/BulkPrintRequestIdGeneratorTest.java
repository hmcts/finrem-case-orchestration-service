package uk.gov.hmcts.reform.finrem.caseorchestration.service.bulkprint;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseDetails;

import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;

class BulkPrintRequestIdGeneratorTest {

    @ParameterizedTest
    @MethodSource
    void testGenerate(long caseId, int version, String recipientParty, String expectedRequestId) {
        FinremCaseDetails caseDetails = FinremCaseDetails.builder()
            .id(caseId)
            .version(version)
            .build();

        String requestId = BulkPrintRequestIdGenerator.generate(caseDetails, recipientParty);
        assertThat(requestId).isEqualTo(expectedRequestId);
    }

    private static Stream<Arguments> testGenerate() {
        return Stream.of(
            Arguments.of(3487654L, 45, "Applicant", "3487654:Applicant:45"),
            Arguments.of(1234567L, 1, "Respondent", "1234567:Respondent:1"),
            Arguments.of(9876543L, 99, "Intervener", "9876543:Intervener:99")
        );
    }
}
