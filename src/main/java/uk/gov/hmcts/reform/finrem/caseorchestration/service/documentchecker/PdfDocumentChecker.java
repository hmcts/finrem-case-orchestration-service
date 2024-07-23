package uk.gov.hmcts.reform.finrem.caseorchestration.service.documentchecker;

import org.apache.commons.io.FilenameUtils;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.DocumentCheckContext;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseDocument;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.documentchecker.contentchecker.DocumentContentChecker;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.documentreader.PdfDocumentReader;

import java.io.IOException;
import java.util.List;
import java.util.Objects;

@Component
public class PdfDocumentChecker implements DocumentChecker {

    private final List<DocumentContentChecker> documentContentCheckers;

    private final PdfDocumentReader pdfDocumentReader;

    public PdfDocumentChecker(PdfDocumentReader pdfDocumentReader,
                              List<DocumentContentChecker> documentContentCheckers) {
        this.pdfDocumentReader = pdfDocumentReader;
        this.documentContentCheckers = documentContentCheckers;
    }

    @Override
    public boolean canCheck(CaseDocument caseDocument) {
        return FilenameUtils.getExtension(caseDocument.getDocumentFilename()).equalsIgnoreCase("PDF");
    }

    @Override
    public List<String> getWarnings(DocumentCheckContext context)  throws DocumentContentCheckerException {
        try {
            String[] content = getContent(context.getBytes());

            return documentContentCheckers.stream()
                .map(dcc -> dcc.getWarning(context.getCaseDetails(), content))
                .filter(Objects::nonNull)
                .toList();
        } catch (IOException e) {
            throw new DocumentContentCheckerException(e);
        }
    }

    private String[] getContent(byte[] bytes) throws IOException {
        return pdfDocumentReader.getContent(bytes);
    }
}
