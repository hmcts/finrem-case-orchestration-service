package uk.gov.hmcts.reform.finrem.caseorchestration.service.documentchecker;

import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseDocument;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseDetails;

import java.util.List;

public interface DocumentChecker {

    boolean canCheck(CaseDocument caseDocument);

    List<String> getWarnings(CaseDocument caseDocument, byte[] bytes, FinremCaseDetails caseDetails)
        throws DocumentContentCheckerException;
}
