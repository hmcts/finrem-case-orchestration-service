package uk.gov.hmcts.reform.finrem.caseorchestration.utils;

import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseDocument;

import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.Locale;

public class FileUtils {

    private FileUtils() {
    }

    public static boolean isDocumentType(CaseDocument caseDocument, String... extensions) {
        if (caseDocument.getDocumentFilename() == null) {
            return false;
        }
        String filename = caseDocument.getDocumentFilename().toUpperCase(Locale.ENGLISH);
        return Arrays.stream(extensions).anyMatch(filename::endsWith);
    }

    public static boolean isPdf(CaseDocument caseDocument) {
        return isDocumentType(caseDocument, ".PDF");
    }

    public static boolean isWordDocument(CaseDocument caseDocument) {
        return isDocumentType(caseDocument, ".DOC", ".DOCX");
    }

    /**
     * Inserts a timestamp into a given filename.
     *
     * @param filename the original filename (e.g., "ABC.pdf")
     * @return the filename with the timestamp inserted (e.g., "ABC_20241209_123456.pdf")
     * @throws IllegalArgumentException if the filename is invalid
     */
    public static String insertTimestamp(String filename) {
        if (filename == null || !filename.contains(".")) {
            throw new IllegalArgumentException("Invalid filename: " + filename);
        }

        String timestamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        int lastDotIndex = filename.lastIndexOf(".");
        String baseName = filename.substring(0, lastDotIndex);
        String extension = filename.substring(lastDotIndex);

        return baseName + "_" + timestamp + extension;
    }
}
