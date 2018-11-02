package uk.gov.hmcts.reform.finrem.caseorchestration.controllers;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.pba.PaymentByAccount;

import java.util.Arrays;
import javax.ws.rs.core.MediaType;

@RestController
@RequiredArgsConstructor
@RequestMapping(value = "/case-orchestration")
@Slf4j
public class PaymentByAccountStub {
    @GetMapping(path = "/organisations/pba/{emailId}", consumes = MediaType.APPLICATION_JSON,
            produces = MediaType.APPLICATION_JSON)
    public ResponseEntity pbaList(
            @RequestHeader(value = "Authorization", required = false) String authorizationToken,
            @PathVariable String emailId) {
        log.info("Received request for PBA check. Auth token: {}, emailId : {}", authorizationToken, emailId);

        return ResponseEntity.ok(new PaymentByAccount(Arrays.asList("PBA123", "PBA456")));
    }

}
