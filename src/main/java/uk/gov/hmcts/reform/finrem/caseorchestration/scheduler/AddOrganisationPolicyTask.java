package uk.gov.hmcts.reform.finrem.caseorchestration.scheduler;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.SystemUserService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.noc.nocworkflows.UpdateRepresentationWorkflowService;

@Component
@Slf4j
@RequiredArgsConstructor
public class AddOrganisationPolicyTask implements Task {

    @Value("${cron.addOrganisationPolicy.enabled:false}")
    private boolean isAddOrganisationPolicyTaskEnabled;

    private final UpdateRepresentationWorkflowService service;



    @Override
    public String getCaseListFileName() {
        return "organisationPolicyAddCaseReferenceList.csv";
    }

    @Override
    public String getTaskName() {
        return "AddOrganisationPolicyTask";
    }

    @Override
    public boolean isTaskEnabled() {
        return isAddOrganisationPolicyTaskEnabled;
    }

    @Override
    public CaseType getCaseType() {
        return CaseType.CONSENTED;
    }

    @Override
    public String getSummary() {
        return "Added default Org Policy DFR-2492";
    }

    @Override
    public void executeTask(FinremCaseDetails finremCaseDetails, String authToken) {
        service.persistDefaultOrganisationPolicy(finremCaseDetails.getData());
    }
}
