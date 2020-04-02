package uk.gov.hmcts.reform.finrem.caseorchestration.model.bulk.print;

import org.junit.jupiter.api.Test;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseDocument;

import java.util.Map;
import java.util.UUID;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

class BulkPrintMetadataTest {

    @Test
    void toMapShouldReturnValidMap() {
        UUID letterId = UUID.randomUUID();
        CaseDocument coverSheet = new CaseDocument();
        coverSheet.setDocumentBinaryUrl("binary-url");
        coverSheet.setDocumentFilename("document-file-name");
        coverSheet.setDocumentUrl("document-url");

        BulkPrintMetadata input = BulkPrintMetadata.builder()
            .coverSheet(coverSheet)
            .letterId(letterId).build();

        Map<String, Object> result = input.toMap("myCoverSheet", "myLetterId");

        assertThat(result.get("myCoverSheet"), is(coverSheet));
        assertThat(result.get("myLetterId"), is(letterId));
    }
}