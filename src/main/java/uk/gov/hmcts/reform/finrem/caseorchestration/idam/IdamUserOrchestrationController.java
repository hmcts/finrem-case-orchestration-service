package uk.gov.hmcts.reform.finrem.caseorchestration.idam;

import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping(value = "/case-orchestration/idam/users")
@Slf4j
public class IdamUserOrchestrationController {

    public static final String IDAM_CLIENT_SECRET_HEADER = "X-IDAM-Client-Secret";

    private final IdamUserOrchestrationService idamUserOrchestrationService;

    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Create an IDAM test user")
    public ResponseEntity<IdamUserOrchestrationModels.OrchestrationResponse> createUser(
        @RequestHeader(value = IDAM_CLIENT_SECRET_HEADER, required = false) String clientSecretOverride,
        @Valid @RequestBody IdamUserOrchestrationModels.CreateUserRequest request) {

        log.info("Creating {} IDAM test users in {}", request.users().size(), request.environment());
        return ResponseEntity
            .status(HttpStatus.CREATED)
            .body(idamUserOrchestrationService.createUser(request, clientSecretOverride));
    }

    @DeleteMapping(consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Delete an IDAM test user")
    public ResponseEntity<IdamUserOrchestrationModels.OrchestrationResponse> deleteUser(
        @Valid @RequestBody IdamUserOrchestrationModels.DeleteUserRequest request) {

        log.info("Deleting {} IDAM test users in {}", request.users().size(), request.environment());
        return ResponseEntity.ok(idamUserOrchestrationService.deleteUser(request));
    }

    @PutMapping(consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Update an IDAM test user by deleting and recreating the account")
    public ResponseEntity<IdamUserOrchestrationModels.OrchestrationResponse> updateUser(
        @RequestHeader(value = IDAM_CLIENT_SECRET_HEADER, required = false) String clientSecretOverride,
        @Valid @RequestBody IdamUserOrchestrationModels.UpdateUserRequest request) {

        log.info("Updating {} IDAM test users in {}", request.users().size(), request.environment());
        return ResponseEntity.ok(idamUserOrchestrationService.updateUser(request, clientSecretOverride));
    }
}
