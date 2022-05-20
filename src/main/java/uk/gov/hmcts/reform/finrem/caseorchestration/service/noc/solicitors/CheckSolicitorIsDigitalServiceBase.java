package uk.gov.hmcts.reform.finrem.caseorchestration.service.noc.solicitors;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.CaseDataService;

@Slf4j
public abstract class CheckSolicitorIsDigitalServiceBase {

    @Autowired
    protected final CaseDataService caseDataService;

    public CheckSolicitorIsDigitalServiceBase(CaseDataService caseDataService) {
        this.caseDataService = caseDataService;
    }

    public abstract boolean isSolicitorDigital(CaseDetails caseDetails);
}
