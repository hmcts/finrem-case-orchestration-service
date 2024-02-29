package uk.gov.hmcts.reform.finrem.caseorchestration.service.documentchecker;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseDocument;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseData;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseDetails;

import java.util.Collections;
import java.util.List;
import java.util.function.Supplier;

@Component
public class DuplicateFilenameDocumentChecker implements DocumentChecker {

    private static final String WARNING = "A document with this filename already exists on the case";

    @Override
    public boolean canCheck(CaseDocument caseDocument) {
        return true;
    }

    @Override
    public List<String> getWarnings(CaseDocument caseDocument, byte[] bytes, FinremCaseDetails caseDetails)
        throws DocumentContentCheckerException {

        FinremCaseData caseData = caseDetails.getData();
        if (isDuplicateFilename(caseDocument, caseData::getAdditionalDocument)) {
            return List.of(WARNING);
        }
        if (isDuplicateFilename(caseDocument, caseData.getGeneralOrderWrapper()::getGeneralOrderLatestDocument)) {
            return List.of(WARNING);
        }

        return Collections.emptyList();
    }

    private boolean isDuplicateFilename(CaseDocument caseDocument, Supplier<CaseDocument> caseDocumentSupplier) {
        return caseDocumentSupplier.get() != null && caseDocumentSupplier.get().getDocumentFilename().equals(caseDocument.getDocumentFilename());
    }
}
