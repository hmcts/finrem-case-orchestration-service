package uk.gov.hmcts.reform.finrem.caseorchestration.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.client.OrganisationClient;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.organisation.Organisation;

import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.CREATOR_ORGANISATION;

@Service
@RequiredArgsConstructor
@Slf4j
public class OrganisationService {
    private final OrganisationClient organisationClient;

    public CaseDetails setCreatorOrganisation(String authToken, CaseDetails caseDetails) {
        Organisation organisation = Organisation.builder().organisationIdentifier("SWNAP0V").build();
        caseDetails.getData().put(CREATOR_ORGANISATION, organisation.getOrganisationIdentifier());

        log.info("temp logging - set creator org to {}, {}", organisation.getOrganisationIdentifier(), authToken);
        return caseDetails;
    }
}
