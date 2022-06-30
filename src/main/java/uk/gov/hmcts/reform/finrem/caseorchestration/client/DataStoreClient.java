package uk.gov.hmcts.reform.finrem.caseorchestration.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseAssignedUserRolesResource;

import static uk.gov.hmcts.reform.finrem.caseorchestration.OrchestrationConstants.SERVICE_AUTHORISATION_HEADER;

@FeignClient(name = "data-store-api", url = "${ccd.data-store.api.baseurl}")
@Configuration
public interface DataStoreClient {
    @GetMapping(
        value = "/case-users",
        produces = MediaType.APPLICATION_JSON_VALUE
    )
    @ResponseBody
    CaseAssignedUserRolesResource getUserRoles(@RequestHeader(HttpHeaders.AUTHORIZATION) String authorisation,
                                                           @RequestHeader(SERVICE_AUTHORISATION_HEADER) String serviceAuthorization,
        @RequestParam("case_ids") String caseIds, @RequestParam("user_ids") String userIds);
}
