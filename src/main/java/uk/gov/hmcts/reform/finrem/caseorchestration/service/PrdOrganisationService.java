package uk.gov.hmcts.reform.finrem.caseorchestration.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.finrem.caseorchestration.config.PrdOrganisationConfiguration;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.organisation.OrganisationsResponse;

@Service
@RequiredArgsConstructor
@Slf4j
public class PrdOrganisationService {

    private final PrdOrganisationConfiguration prdOrganisationConfiguration;
    private final RestService restService;

    public OrganisationsResponse retrieveOrganisationsData(String authToken) {
        return (OrganisationsResponse) restService.restApiGetCall(authToken, prdOrganisationConfiguration.getOrganisationsUrl());
    }
}
