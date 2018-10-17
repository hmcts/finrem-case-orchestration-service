package uk.gov.hmcts.reform.finrem.finremcaseprogression.controllers;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import uk.gov.hmcts.reform.finrem.finremcaseprogression.model.pba.PBAccount;

import javax.ws.rs.core.MediaType;
import java.util.Arrays;

@RestController
@RequiredArgsConstructor
@RequestMapping(value = "/case-progression")
@Slf4j
public class PBAStub {
    @GetMapping(path = "/organisations/pba/{emailId}", consumes = MediaType.APPLICATION_JSON, produces = MediaType.APPLICATION_JSON)
    public ResponseEntity pbaList(@RequestHeader(value = "Authorization", required = false) String authorizationToken,
                                  @PathVariable String emailId) {
        log.info("Received request for PBA check. Auth token: {}, emailId : {}", authorizationToken, emailId);

        return ResponseEntity.ok(new PBAccount(Arrays.asList("PBA123", "PBA456")));
    }

}
