package uk.gov.hmcts.reform.finrem.caseorchestration.service.noc.solicitors;

import lombok.extern.slf4j.Slf4j;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.OrganisationPolicy;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.CaseDataService;

import java.util.Optional;

@Slf4j
public abstract class CheckSolicitorIsDigitalServiceBase {

    protected final CaseDataService caseDataService;

    protected CheckSolicitorIsDigitalServiceBase(CaseDataService caseDataService) {
        this.caseDataService = caseDataService;
    }

    protected boolean isOrganisationEmpty(OrganisationPolicy organisationPolicy) {
        return Optional.ofNullable(organisationPolicy).isEmpty()
            || Optional.ofNullable(organisationPolicy.getOrganisation()).isEmpty()
            || organisationPolicy.getOrganisation().getOrganisationID() == null;
    }

    public abstract boolean isSolicitorDigital(CaseDetails caseDetails);
}
