package uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.wrapper;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseDocument;

import java.util.List;
import java.util.stream.Stream;

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

    @Nested
    class BinDeletedCaseDocumentTest {

        private final Bin bin = new Bin();
        private final CaseDocument deletedDocument = caseDocument("deleted-url");
        private final CaseDocument retainedDocument = caseDocument("retained-url");
        private final CaseDocument newDocument = caseDocument("new-url");

        @Test
        void shouldBinDocumentsThatExistPreviouslyButNotCurrently() {

            bin.binDeletedCaseDocument(
                Stream.of(deletedDocument, retainedDocument),
                Stream.of(retainedDocument, newDocument)
            );

            assertThat(bin.getFileUrlsToBeDeleted())
                .isNotNull();

            assertThat(bin.getFileUrlsToBeDeleted().stream())
                .extracting(BinFileUrlsCollection::getValue)
                .extracting(BinFileUrls::getBinFileUrl)
                .containsExactly(deletedDocument.getDocumentUrl());
        }

        @Test
        void shouldIgnoreNullDocuments() {

            bin.binDeletedCaseDocument(
                Stream.of(null, deletedDocument, retainedDocument),
                Stream.of(null, retainedDocument)
            );

            assertThat(bin.getFileUrlsToBeDeleted().stream())
                .extracting(BinFileUrlsCollection::getValue)
                .extracting(BinFileUrls::getBinFileUrl)
                .containsExactly(deletedDocument.getDocumentUrl());
        }

        @Test
        void shouldNotBinAnyDocumentsWhenNoDocumentsWereDeleted() {
            CaseDocument document1 = caseDocument("url-1");
            CaseDocument document2 = caseDocument("url-2");

            bin.binDeletedCaseDocument(
                Stream.of(document1, document2),
                Stream.of(document1, document2)
            );

            assertThat(bin.getFileUrlsToBeDeleted()).isNull();
        }
    }
}
