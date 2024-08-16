package uk.gov.hmcts.reform.finrem.caseorchestration.scheduler.removesolicitorfromcase;

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class RemoveSolicitorFromCaseFileReaderTest {

    @Test
    void givenFileWhenReadThenRequestRecordsRead() throws IOException {
        RemoveSolicitorFromCaseFileReader reader = new RemoveSolicitorFromCaseFileReader();
        List<RemoveSolicitorFromCaseRequest> requests = reader.read("testRemoveSolicitorFromCase.csv");
        assertThat(requests).hasSize(2);
        assertThat(requests.get(0)).isEqualTo(
            new RemoveSolicitorFromCaseRequest("17234591981552048", "FinancialRemedyContested",
                "68a68764-a612-48bd-a10b-463e9546a0d2", "[APPSOLICITOR]", "DFR-1"));
        assertThat(requests.get(1)).isEqualTo(
            new RemoveSolicitorFromCaseRequest("17234391958155204", "FinancialRemedyMVP2",
                "9bc32daa-539f-4156-b355-2c246c5fae79", "[RESPSOLICITOR]", "DFR-2"));
    }

    @Test
    void givenNoFileWhenReadThenThrowsException() {
        RemoveSolicitorFromCaseFileReader reader = new RemoveSolicitorFromCaseFileReader();
        assertThatThrownBy(() -> reader.read("file-does-not-exist.xyz"))
            .isInstanceOf(IOException.class)
            .hasMessageMatching("File file-does-not-exist.xyz not found");
    }
}
