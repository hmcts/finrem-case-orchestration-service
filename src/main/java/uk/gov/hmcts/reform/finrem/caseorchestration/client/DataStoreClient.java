package uk.gov.hmcts.reform.finrem.caseorchestration.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseAssignedUserRolesResource;

import static org.springframework.http.HttpHeaders.CONTENT_TYPE;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;
import static uk.gov.hmcts.reform.finrem.caseorchestration.OrchestrationConstants.SERVICE_AUTHORISATION_HEADER;

@FeignClient(name = "data-store-api", url = "${ccd.data-store.api.baseurl}")
@Configuration
public interface DataStoreClient {
    @GetMapping(
        path = "/case-users",
        headers = CONTENT_TYPE + "=" + APPLICATION_JSON_VALUE)
    CaseAssignedUserRolesResource getCaseAssignedUserRoles(@PathVariable("case_ids") Long caseID, @RequestHeader(HttpHeaders.AUTHORIZATION)
        String authToken, @RequestHeader(SERVICE_AUTHORISATION_HEADER) String serviceAuthorization);
}
