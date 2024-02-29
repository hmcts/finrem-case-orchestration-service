package uk.gov.hmcts.reform.finrem.caseorchestration.service.documentchecker.contentchecker;

import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseDetails;

public interface DocumentContentChecker {

    String getWarning(FinremCaseDetails caseDetails, String[] content);
}
