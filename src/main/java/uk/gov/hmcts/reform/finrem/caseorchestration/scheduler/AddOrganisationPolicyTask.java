package uk.gov.hmcts.reform.finrem.caseorchestration.scheduler;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.finrem.caseorchestration.mapper.FinremCaseDetailsMapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.CcdService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.SystemUserService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.noc.nocworkflows.UpdateRepresentationWorkflowService;
import uk.gov.hmcts.reform.finrem.caseorchestration.utils.csv.CaseReferenceCsvLoader;

@Component
@Slf4j
public class AddOrganisationPolicyTask extends BaseTask {

    @Value("${cron.addOrganisationPolicy.enabled:false}")
    private boolean isAddOrganisationPolicyTaskEnabled;

    private final UpdateRepresentationWorkflowService service;

    @Autowired
    protected AddOrganisationPolicyTask(CaseReferenceCsvLoader csvLoader,
                                        CcdService ccdService,
                                        SystemUserService systemUserService,
                                        FinremCaseDetailsMapper finremCaseDetailsMapper,
                                        UpdateRepresentationWorkflowService service) {
        super(csvLoader, ccdService, systemUserService, finremCaseDetailsMapper);
        this.service = service;
    }

    @Override
    protected String getCaseListFileName() {
        return "organisationPolicyAddCaseReferenceList.csv";
    }

    @Override
    protected String getTaskName() {
        return "AddOrganisationPolicyTask";
    }

    @Override
    protected boolean isTaskEnabled() {
        return isAddOrganisationPolicyTaskEnabled;
    }

    @Override
    protected CaseType getCaseType() {
        return CaseType.CONSENTED;
    }

    @Override
    protected String getSummary() {
        return "Added default Org Policy DFR-2492";
    }

    @Override
    protected void executeTask(FinremCaseDetails finremCaseDetails) {
        service.persistDefaultOrganisationPolicy(finremCaseDetails.getData());
    }
}
