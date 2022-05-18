package uk.gov.hmcts.reform.finrem.caseorchestration.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.cloud.openfeign.FeignClientProperties;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.organisation.OrganisationsResponse;

import static org.springframework.http.HttpHeaders.AUTHORIZATION;
import static uk.gov.hmcts.reform.ccd.client.CoreCaseDataApi.SERVICE_AUTHORIZATION;

@FeignClient(
    name = "rd-professional-api",
    url = "${prd.organisations.url}",
    configuration = FeignClientProperties.FeignClientConfiguration.class
)
public interface OrganisationApi {

    @GetMapping("/refdata/internal/v1/organisations")
    OrganisationsResponse findOrganisationByOrgId(
        @RequestHeader(AUTHORIZATION) String authorisation,
        @RequestHeader(SERVICE_AUTHORIZATION) String serviceAuthorization,
        @RequestParam(value = "id") String organisationId
    );
}
