package uk.gov.hmcts.reform.finrem.caseorchestration.utils;

import org.junit.jupiter.api.Test;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseDocument;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class FileUtilsTest {

    @Test
    void shouldReturnTrueForPdfFile() {
        CaseDocument pdfDocument = CaseDocument.builder().documentFilename("sample.PDF").build();
        assertTrue(FileUtils.isPdf(pdfDocument));
    }

    @Test
    void shouldReturnFalseForNonPdfFile() {
        CaseDocument docDocument = CaseDocument.builder().documentFilename("sample.DOC").build();
        assertFalse(FileUtils.isPdf(docDocument));
    }

    @Test
    void shouldReturnTrueForDocFile() {
        CaseDocument docDocument = CaseDocument.builder().documentFilename("sample.DOC").build();
        assertTrue(FileUtils.isWordDocument(docDocument));
    }

    @Test
    void shouldReturnTrueForDocxFile() {
        CaseDocument docxDocument = CaseDocument.builder().documentFilename("sample.DOCX").build();
        assertTrue(FileUtils.isWordDocument(docxDocument));
    }

    @Test
    void shouldReturnFalseForNonWordFile() {
        CaseDocument pdfDocument = CaseDocument.builder().documentFilename("sample.PDF").build();
        assertFalse(FileUtils.isWordDocument(pdfDocument));
    }

    @Test
    void shouldHandleLowercaseExtensions() {
        CaseDocument docDocument = CaseDocument.builder().documentFilename("sample.doc").build();
        CaseDocument docxDocument = CaseDocument.builder().documentFilename("sample.docx").build();
        CaseDocument pdfDocument = CaseDocument.builder().documentFilename("sample.pdf").build();

        assertTrue(FileUtils.isWordDocument(docDocument));
        assertTrue(FileUtils.isWordDocument(docxDocument));
        assertTrue(FileUtils.isPdf(pdfDocument));
    }

    @Test
    void shouldReturnFalseForEmptyFilename() {
        CaseDocument emptyDocument = CaseDocument.builder().documentFilename("").build();
        assertFalse(FileUtils.isPdf(emptyDocument));
        assertFalse(FileUtils.isWordDocument(emptyDocument));
    }

    @Test
    void shouldReturnFalseForNullFilename() {
        CaseDocument nullDocument = CaseDocument.builder().documentFilename(null).build();
        assertFalse(FileUtils.isPdf(nullDocument));
        assertFalse(FileUtils.isWordDocument(nullDocument));
    }

}
