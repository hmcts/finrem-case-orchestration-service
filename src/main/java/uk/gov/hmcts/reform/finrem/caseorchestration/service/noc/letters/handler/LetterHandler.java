package uk.gov.hmcts.reform.finrem.caseorchestration.service.noc.letters.handler;

import uk.gov.hmcts.reform.finrem.ccd.domain.FinremCaseDetails;

public interface LetterHandler {

    void handle(FinremCaseDetails caseDetails, FinremCaseDetails caseDetailsBefore, String authToken);
}
