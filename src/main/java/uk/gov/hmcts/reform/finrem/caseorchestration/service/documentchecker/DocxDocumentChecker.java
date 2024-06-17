package uk.gov.hmcts.reform.finrem.caseorchestration.service.documentchecker;

import org.apache.commons.io.FilenameUtils;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseDocument;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.documentchecker.contentchecker.DocumentContentChecker;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.documentreader.DocxDocumentReader;

import java.util.List;
import java.util.Objects;

@Component
public class DocxDocumentChecker implements DocumentChecker {

    private final DocxDocumentReader docxDocumentReader;

    private final List<DocumentContentChecker> documentContentCheckers;

    public DocxDocumentChecker(DocxDocumentReader docxDocumentReader, List<DocumentContentChecker> documentContentCheckers) {
        this.docxDocumentReader = docxDocumentReader;
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
            .filter(Objects::nonNull)
            .toList();
    }

    private String[] getContent(byte[] caseDocument) throws DocumentContentCheckerException {
        try {
            return docxDocumentReader.getContent(caseDocument);
        } catch (Exception e) {
            throw new DocumentContentCheckerException(e);
        }
    }
}
