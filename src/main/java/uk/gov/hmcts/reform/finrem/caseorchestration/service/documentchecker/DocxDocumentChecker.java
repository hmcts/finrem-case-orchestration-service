package uk.gov.hmcts.reform.finrem.caseorchestration.service.documentchecker;

import org.apache.commons.io.FilenameUtils;
import org.apache.poi.xwpf.extractor.XWPFWordExtractor;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseDocument;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.documentchecker.contentchecker.DocumentContentChecker;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;


public class DocxDocumentChecker implements DocumentChecker {

    private final List<DocumentContentChecker> documentContentCheckers;

    public DocxDocumentChecker(List<DocumentContentChecker> documentContentCheckers) {
        this.documentContentCheckers = documentContentCheckers;
    }

    @Override
    public boolean canCheck(CaseDocument caseDocument) {
        return FilenameUtils.getExtension(caseDocument.getDocumentFilename()).equalsIgnoreCase("DOCX");
    }

    @Override
    public List<String> getWarnings(CaseDocument caseDocument, byte[] bytes, FinremCaseDetails caseDetails)
        throws DocumentContentCheckerException {
        String[] content = getContent(bytes);

        return documentContentCheckers.stream()
            .map(dcc -> dcc.getWarning(caseDetails, content))
            .toList();
    }

    private String[] getContent(byte[] caseDocument) throws DocumentContentCheckerException {
        InputStream is = new ByteArrayInputStream(caseDocument);
        try (XWPFDocument wordDoc = new XWPFDocument(is)) {
            XWPFWordExtractor extractor = new XWPFWordExtractor(wordDoc);
            String content = extractor.getText();
            return content.split(System.lineSeparator());

        } catch (IOException e) {
            throw new DocumentContentCheckerException(e);
        }
    }
}
