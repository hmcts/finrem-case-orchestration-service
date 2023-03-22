package uk.gov.hmcts.reform.finrem.caseorchestration.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import uk.gov.hmcts.reform.ccd.client.model.CaseUser;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseAssignmentUserRolesRequest;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseAssignmentUserRolesResource;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseAssignmentUserRolesResponse;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.events.AuditEventsResponse;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.searchuserrole.SearchCaseAssignedUserRolesRequest;

import java.util.List;

import static org.springframework.http.HttpHeaders.AUTHORIZATION;
import static uk.gov.hmcts.reform.ccd.client.CoreCaseDataApi.SERVICE_AUTHORIZATION;

@FeignClient(
    name = "core-case-data-api-v2",
    url = "${ccd.data-store.api.baseurl}"
)
public interface CaseDataApiV2 {
    @GetMapping("/cases/{caseId}/events")
    AuditEventsResponse getAuditEvents(
        @RequestHeader(AUTHORIZATION) String authorisation,
        @RequestHeader(SERVICE_AUTHORIZATION) String serviceAuthorization,
        @RequestHeader("experimental") boolean experimental,
        @PathVariable("caseId") String caseId
    );

    @GetMapping(
        value = "/case-users",
        produces = MediaType.APPLICATION_JSON_VALUE
    )
    @ResponseBody
    CaseAssignmentUserRolesResource getUserRoles(
        @RequestHeader(AUTHORIZATION) String authorisation,
        @RequestHeader(SERVICE_AUTHORIZATION) String serviceAuthorization,
        @RequestParam("case_ids") List<String> caseIds
    );

    @GetMapping(
        value = "/case-users",
        produces = MediaType.APPLICATION_JSON_VALUE
    )
    @ResponseBody
    CaseAssignmentUserRolesResource getUserRoles(
        @RequestHeader(AUTHORIZATION) String authorisation,
        @RequestHeader(SERVICE_AUTHORIZATION) String serviceAuthorization,
        @RequestParam("case_ids") String caseId,
        @RequestParam("user_ids") String userId
    );

    @PostMapping(
        value = "/case-users/search",
        produces = MediaType.APPLICATION_JSON_VALUE
    )
    @ResponseBody
    CaseAssignmentUserRolesResource searchCaseUserRoles(
        @RequestHeader(AUTHORIZATION) String authorisation,
        @RequestHeader(SERVICE_AUTHORIZATION) String serviceAuthorization,
        @RequestBody SearchCaseAssignedUserRolesRequest searchCaseAssignedUserRolesRequest);

    @DeleteMapping(
        value = "/case-users",
        consumes = MediaType.APPLICATION_JSON_VALUE
    )
    @ResponseBody
    CaseAssignmentUserRolesResponse removeCaseUserRoles(
        @RequestHeader(AUTHORIZATION) String authorisation,
        @RequestHeader(SERVICE_AUTHORIZATION) String serviceAuthorization,
        @RequestBody CaseAssignmentUserRolesRequest caseRoleRequest
    );

    @PostMapping(
        value = "/case-users",
        consumes = MediaType.APPLICATION_JSON_VALUE
    )
    @ResponseBody
    CaseAssignmentUserRolesResponse addCaseUserRoles(
        @RequestHeader(AUTHORIZATION) String authorisation,
        @RequestHeader(SERVICE_AUTHORIZATION) String serviceAuthorization,
        @RequestBody CaseAssignmentUserRolesRequest caseRoleRequest
    );

    @PutMapping("/cases/{caseReference}/users/{userId}")
    void updateCaseRolesForUser(
        @RequestHeader(AUTHORIZATION) String authorisation,
        @RequestHeader(SERVICE_AUTHORIZATION) String serviceAuthorisation,
        @PathVariable("caseReference") String caseReference,
        @PathVariable("userId") String userId,
        @RequestBody CaseUser caseUser
    );
}
