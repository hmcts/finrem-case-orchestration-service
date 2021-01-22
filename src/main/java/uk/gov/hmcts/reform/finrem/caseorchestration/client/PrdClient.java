package uk.gov.hmcts.reform.finrem.caseorchestration.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.organisation.OrganisationsResponse;

import static uk.gov.hmcts.reform.finrem.caseorchestration.OrchestrationConstants.AUTHORIZATION_HEADER;
import static uk.gov.hmcts.reform.finrem.caseorchestration.OrchestrationConstants.SERVICE_AUTHORISATION_HEADER;

@FeignClient(name = "prd-client", url = "${prd.api.url}", primary = false)
public interface PrdClient {

    @GetMapping(path = "/refdata/internal/v1/organisations", produces = MediaType.APPLICATION_JSON_VALUE)
    OrganisationsResponse getOrganisationById(
        @RequestHeader(AUTHORIZATION_HEADER) String authToken,
        @RequestHeader(SERVICE_AUTHORISATION_HEADER) String serviceAuthToken,
        @RequestParam("id") String organisationId);
}
