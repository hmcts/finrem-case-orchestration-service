package uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.wrapper;

import org.junit.jupiter.api.Test;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseDocument;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static uk.gov.hmcts.reform.finrem.caseorchestration.TestSetUpUtils.caseDocument;

class BinTest {

    @Test
    void shouldInitialiseBinAndAddCaseDocument() {
        Bin bin = Bin.builder().build();

        CaseDocument doc = caseDocument("fileA");

        bin.binCaseDocument(doc);

        assertAll(
                () -> assertNotNull(bin.getFileUrlsToBeDeleted())
        );

        List<BinFileUrlsCollection> items = bin.getFileUrlsToBeDeleted();

        assertThat(items)
                .hasSize(1)
                .extracting(BinFileUrlsCollection::getValue)
                .extracting(BinFileUrls::getBinFileUrl)
                .containsExactly(doc.getDocumentUrl());
    }

    @Test
    void shouldAppendMultipleCaseDocuments() {
        Bin bin = Bin.builder().build();

        CaseDocument doc1 = caseDocument("fileA");
        CaseDocument doc2 = caseDocument("fileB");

        bin.binCaseDocument(doc1);
        bin.binCaseDocument(doc2);

        assertAll(
                () -> assertNotNull(bin.getFileUrlsToBeDeleted())
        );

        List<BinFileUrlsCollection> items = bin.getFileUrlsToBeDeleted();

        assertThat(items)
                .hasSize(2)
                .extracting(BinFileUrlsCollection::getValue)
                .extracting(BinFileUrls::getBinFileUrl)
                .containsExactly(doc1.getDocumentUrl(), doc2.getDocumentUrl());
    }

    @Test
    void shouldClearBin() {
        Bin bin = Bin.builder().build();

        CaseDocument doc = caseDocument("fileA");

        bin.binCaseDocument(doc);

        assertAll(
                () -> assertNotNull(bin.getFileUrlsToBeDeleted())
        );

        bin.clearBin();

        assertThat(bin.getFileUrlsToBeDeleted()).isNull();
    }
}
