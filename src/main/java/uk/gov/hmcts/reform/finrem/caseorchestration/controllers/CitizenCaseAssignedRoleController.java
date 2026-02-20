package uk.gov.hmcts.reform.finrem.caseorchestration.controllers;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.AssignUserToCaseRequest;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.AssignCaseAccessService;

import static uk.gov.hmcts.reform.finrem.caseorchestration.OrchestrationConstants.AUTHORIZATION_HEADER;

@RestController
@RequestMapping(value = "/case-orchestration")
@Slf4j
@RequiredArgsConstructor
public class CitizenCaseAssignedRoleController {

    private final AssignCaseAccessService assignCaseAccessService;

    @PostMapping(path = "/assign-user-to-case")
    @Operation(summary = "Assigns user to case")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "OK"),
        @ApiResponse(responseCode = "400", description = "Bad Request"),
        @ApiResponse(responseCode = "401", description = "Not Authorized"),
        @ApiResponse(responseCode = "500", description = "Internal Server Error")})
    public ResponseEntity<Void> assignCaseToUser(@RequestHeader(value = AUTHORIZATION_HEADER, required = false) String authorisation,
                                                 @Valid @RequestBody AssignUserToCaseRequest body) {
        log.info("assigning case with id: {}", body.getCaseId());
        assignCaseAccessService.grantCaseRoleToUser(body.getCaseId(), body.getUserId(), body.getCaseRole().getCcdCode(), null);
        return ResponseEntity.ok().build();
    }

}

