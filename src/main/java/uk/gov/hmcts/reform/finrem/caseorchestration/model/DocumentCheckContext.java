package uk.gov.hmcts.reform.finrem.caseorchestration.model;

import lombok.Builder;
import lombok.Getter;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseDocument;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseDetails;

@Builder
@Getter
public class DocumentCheckContext {
    private CaseDocument caseDocument;
    private byte[] bytes;
    private FinremCaseDetails beforeCaseDetails;
    private FinremCaseDetails caseDetails;
}
