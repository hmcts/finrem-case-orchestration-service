package uk.gov.hmcts.reform.finrem.caseorchestration.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;
import uk.gov.hmcts.reform.finrem.caseorchestration.client.PrdClient;
import uk.gov.hmcts.reform.finrem.caseorchestration.config.PrdConfiguration;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.organisation.OrganisationsResponse;

@Service
@RequiredArgsConstructor
@Slf4j
public class PrdOrganisationService {

    private final PrdConfiguration prdOrganisationConfiguration;
    private final PrdClient prdClient;
    private final AuthTokenGenerator authTokenGenerator;
    private final IdamService idamService;

    public OrganisationsResponse retrieveOrganisationsData(String organisationId) {
        log.info("Calling PRD to retrieve organisation data for orgId: {}", organisationId);
        OrganisationsResponse orgData = prdClient.getOrganisationById(
            idamService.authenticateUser(prdOrganisationConfiguration.getUsername(), prdOrganisationConfiguration.getPassword()),
            authTokenGenerator.generate(),
            organisationId);
        log.info("Retrieved organisation data from PRD: {}", orgData);
        return orgData;
    }
}
