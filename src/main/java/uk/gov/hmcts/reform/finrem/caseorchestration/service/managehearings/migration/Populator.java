package uk.gov.hmcts.reform.finrem.caseorchestration.service.managehearings.migration;

import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseData;

public interface Populator {

    boolean shouldPopulate(FinremCaseData caseData);

    void populate(FinremCaseData caseData);
}
