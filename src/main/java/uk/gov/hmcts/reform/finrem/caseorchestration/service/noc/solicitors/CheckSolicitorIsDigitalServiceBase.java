package uk.gov.hmcts.reform.finrem.caseorchestration.service.noc.solicitors;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.OrganisationPolicy;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.CaseDataService;
import uk.gov.hmcts.reform.finrem.ccd.domain.FinremCaseDetails;

import java.util.Optional;

import static uk.gov.hmcts.reform.finrem.caseorchestration.service.CaseDataService.nullToEmpty;

@Slf4j
public abstract class CheckSolicitorIsDigitalServiceBase {

    @Autowired
    protected final CaseDataService caseDataService;

    public CheckSolicitorIsDigitalServiceBase(CaseDataService caseDataService) {
        this.caseDataService = caseDataService;
    }

    @Deprecated
    protected boolean isOrganisationEmpty(OrganisationPolicy organisationPolicy) {
        return Optional.ofNullable(organisationPolicy.getOrganisation()).isEmpty()
            || nullToEmpty(organisationPolicy.getOrganisation().getOrganisationID()).isEmpty();
    }

    protected boolean isOrganisationEmpty(uk.gov.hmcts.reform.finrem.ccd.domain.OrganisationPolicy organisationPolicy) {
        return Optional.ofNullable(organisationPolicy.getOrganisation()).isEmpty()
            || nullToEmpty(organisationPolicy.getOrganisation().getOrganisationID()).isEmpty();
    }

    @Deprecated
    public abstract boolean isSolicitorDigital(CaseDetails caseDetails);

    public abstract boolean isSolicitorDigital(FinremCaseDetails caseDetails);
}
