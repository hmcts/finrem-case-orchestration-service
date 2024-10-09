package uk.gov.hmcts.reform.finrem.caseorchestration.service.documentreader;

import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
public class PdfDocumentReader {

    public String[] getContent(byte[] bytes) throws IOException {
        try (PDDocument document = Loader.loadPDF(bytes)) {
            PDFTextStripper stripper = new PDFTextStripper();
            String content = stripper.getText(document);
            return content.split(System.lineSeparator());
        }
    }
}
