package uk.gov.hmcts.reform.finrem.caseorchestration.controllers;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.pba.validation.PBAAccount;

import javax.ws.rs.core.MediaType;
import java.util.Arrays;

@RestController
@RequiredArgsConstructor
@RequestMapping(value = "/search")
@Slf4j
public class PBAValidationStub {
    @GetMapping(path = "/pba/{emailId}", consumes = MediaType.APPLICATION_JSON,
            produces = MediaType.APPLICATION_JSON)
    public ResponseEntity pbaList(@PathVariable String emailId) {
        log.info("Received request for PBA check, emailId : {}", emailId);
        return ResponseEntity.ok(new PBAAccount(Arrays.asList("PBA123", "PBA456")));
    }

}
