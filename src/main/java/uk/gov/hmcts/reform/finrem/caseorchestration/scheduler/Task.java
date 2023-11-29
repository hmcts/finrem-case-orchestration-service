package uk.gov.hmcts.reform.finrem.caseorchestration.scheduler;

import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseDetails;

public interface Task {

    String getCaseListFileName();

    String getTaskName();

    boolean isTaskEnabled();

    CaseType getCaseType();

    String getSummary();

    void executeTask(FinremCaseDetails finremCaseDetails, String authToken);
}
