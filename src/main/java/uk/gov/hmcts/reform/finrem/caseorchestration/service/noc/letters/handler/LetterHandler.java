package uk.gov.hmcts.reform.finrem.caseorchestration.service.noc.letters.handler;

import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;

public interface LetterHandler {

    void handle(CaseDetails caseDetails, CaseDetails caseDetailsBefore, String authToken);
}
