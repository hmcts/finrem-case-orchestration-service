package uk.gov.hmcts.reform.finrem.caseorchestration.utils.pdf;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;

import java.io.IOException;

public class PdfToText {

    public static String textContent(byte[] bytes) throws IOException {
        try (PDDocument document = PDDocument.load(bytes)) {
            PDFTextStripper stripper = new PDFTextStripper();
            return stripper.getText(document);
        }
    }

    public static String[] textContentLines(byte[] bytes) throws IOException {
        String content = textContent(bytes);
        return content.split(System.lineSeparator());
    }
}
