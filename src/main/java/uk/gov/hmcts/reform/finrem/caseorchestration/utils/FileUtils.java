package uk.gov.hmcts.reform.finrem.caseorchestration.utils;

import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseDocument;

import java.util.Arrays;
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

}
