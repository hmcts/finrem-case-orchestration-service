package uk.gov.hmcts.reform.finrem.caseorchestration.service.documentchecker;

import uk.gov.hmcts.reform.finrem.caseorchestration.model.DocumentCheckContext;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseDocument;

import java.util.List;

public interface DocumentChecker {

    boolean canCheck(CaseDocument caseDocument);

    List<String> getWarnings(DocumentCheckContext context) throws DocumentContentCheckerException;
}
