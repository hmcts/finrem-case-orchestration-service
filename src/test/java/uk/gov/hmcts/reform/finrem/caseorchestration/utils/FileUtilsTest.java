package uk.gov.hmcts.reform.finrem.caseorchestration.utils;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseDocument;

import java.text.SimpleDateFormat;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
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

    @ParameterizedTest(name = "[{index}] Original: \"{0}\", Expected: Starts with \"{1}\" and ends with \"{2}\"")
    @CsvSource({
        "'ABC.pdf', 'ABC', '.pdf'",
        "'document.docx', 'document', '.docx'",
        "'example.txt', 'example', '.txt'",
        "'example.txt.txt', 'example.txt', '.txt'"
    })
    void testInsertTimestamp(String originalFilename, String expectedBaseName, String expectedExtension) {
        // Call the method under test
        String result = FileUtils.insertTimestamp(originalFilename);

        // Assert that the result starts with the base name
        assertTrue(result.startsWith(expectedBaseName), "Filename does not start with the expected base name");

        // Assert that the result ends with the expected extension
        assertTrue(result.endsWith(expectedExtension), "Filename does not end with the expected extension");

        // Extract the timestamp and validate its format
        String timestampPart = result.substring(expectedBaseName.length() + 1, result.length() - expectedExtension.length());
        assertDoesNotThrow(() -> new SimpleDateFormat("yyyyMMdd_HHmmss").parse(timestampPart),
            "Timestamp part is not in the expected format");
    }

    @ParameterizedTest
    @CsvSource({
        "''", // Empty string
        "null", // Null string
        "'filename_without_extension'" // Filename without an extension
    })
    void testInsertTimestampWithInvalidInputs(String invalidFilename) {
        assertThrows(IllegalArgumentException.class, () -> FileUtils.insertTimestamp(invalidFilename),
            "Expected IllegalArgumentException for invalid filename: " + invalidFilename);
    }

}
