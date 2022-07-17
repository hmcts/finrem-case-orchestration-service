package uk.gov.hmcts.reform.finrem.caseorchestration.service.noc.solicitors;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.OrganisationPolicy;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.CaseDataService;

import java.util.Optional;
import java.util.regex.Pattern;

@Slf4j
public abstract class CheckSolicitorIsDigitalServiceBase {

    @Autowired
    protected final CaseDataService caseDataService;

    protected CheckSolicitorIsDigitalServiceBase(CaseDataService caseDataService) {
        this.caseDataService = caseDataService;
    }

    protected boolean isOrganisationEmpty(OrganisationPolicy organisationPolicy) {
        return Optional.ofNullable(organisationPolicy.getOrganisation()).isEmpty()
            || organisationPolicy.getOrganisation().getOrganisationID() == null;
    }

    public abstract boolean isSolicitorDigital(CaseDetails caseDetails);

    public final boolean isOrganisationIdRegistered(OrganisationPolicy organisationPolicy) {
        String myHMCTSRegex = "^[A-Z0-9]{7}$";

        if (!isOrganisationEmpty(organisationPolicy)) {
            String organisationId = organisationPolicy.getOrganisation().getOrganisationID();
            return Pattern.matches(myHMCTSRegex, organisationId);
        }
        return false;
    }
}
