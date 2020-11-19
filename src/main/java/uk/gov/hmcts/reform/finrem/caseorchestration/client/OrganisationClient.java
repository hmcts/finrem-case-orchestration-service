package uk.gov.hmcts.reform.finrem.caseorchestration.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.organisation.Organisation;

import static org.springframework.http.HttpHeaders.AUTHORIZATION;
import static uk.gov.hmcts.reform.ccd.client.CoreCaseDataApi.SERVICE_AUTHORIZATION;

@FeignClient(name = "rd-professional-api", url = "${prd.api.baseurl}")
public interface OrganisationClient {

    @GetMapping("/refdata/external/v1/organisations")
    Organisation findUserOrganisation(
        @RequestHeader(AUTHORIZATION) String authorisation,
        @RequestHeader(SERVICE_AUTHORIZATION) String serviceAuthorization
    );
}