package uk.gov.hmcts.reform.finrem.caseorchestration.service.documentchecker;

import org.apache.commons.io.FilenameUtils;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseDocument;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.documentchecker.contentchecker.DocumentContentChecker;

import java.io.IOException;
import java.util.List;

@Component
public class PdfDocumentChecker implements DocumentChecker {

    private final List<DocumentContentChecker> documentContentCheckers;

    public PdfDocumentChecker(List<DocumentContentChecker> documentContentCheckers) {
        this.documentContentCheckers = documentContentCheckers;
    }

    @Override
    public boolean canCheck(CaseDocument caseDocument) {
        return FilenameUtils.getExtension(caseDocument.getDocumentFilename()).equalsIgnoreCase("PDF");
    }

    @Override
    public List<String> getWarnings(CaseDocument caseDocument, byte[] bytes, FinremCaseDetails caseDetails)
        throws DocumentContentCheckerException {
        try {
            String[] content = getContent(bytes);

            return documentContentCheckers.stream()
                .map(dcc -> dcc.getWarning(caseDetails, content))
                .toList();
        } catch (IOException e) {
            throw new DocumentContentCheckerException(e);
        }
    }

    private String[] getContent(byte[] bytes) throws IOException {
        try (PDDocument document = PDDocument.load(bytes)) {
            PDFTextStripper stripper = new PDFTextStripper();
            String content = stripper.getText(document);
            return content.split(System.lineSeparator());
        }
    }
}
