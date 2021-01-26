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
        log.info("Calling IDAM client authenticateUser");
        String serviceUserToken = idamService.authenticateUser(prdOrganisationConfiguration.getUsername(), prdOrganisationConfiguration.getPassword());
        log.info("serviceUserToken: {}", serviceUserToken);

        log.info("Calling PRD client getOrganisationById");
        return prdClient.getOrganisationById(
            serviceUserToken,
            authTokenGenerator.generate(),
            organisationId);
    }
}
