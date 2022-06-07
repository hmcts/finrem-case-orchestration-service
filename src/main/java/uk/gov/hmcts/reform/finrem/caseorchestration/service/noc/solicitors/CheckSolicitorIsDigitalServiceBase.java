package uk.gov.hmcts.reform.finrem.caseorchestration.service.noc.solicitors;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.OrganisationPolicy;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.CaseDataService;

import java.util.Optional;

@Slf4j
public abstract class CheckSolicitorIsDigitalServiceBase {

    @Autowired
    protected final CaseDataService caseDataService;

    public CheckSolicitorIsDigitalServiceBase(CaseDataService caseDataService) {
        this.caseDataService = caseDataService;
    }

    protected boolean isOrganisationEmpty(OrganisationPolicy organisationPolicy) {
        return Optional.ofNullable(organisationPolicy.getOrganisation()).isEmpty()
            || organisationPolicy.getOrganisation().getOrganisationID() == null;
    }

    public abstract boolean isSolicitorDigital(CaseDetails caseDetails);
}
