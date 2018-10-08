package uk.gov.hmcts.reform.finrem.finremcaseprogression.controllers;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import uk.gov.hmcts.reform.finrem.finremcaseprogression.ccd.CCDCallbackResponse;
import uk.gov.hmcts.reform.finrem.finremcaseprogression.ccd.CCDRequest;

import java.util.ArrayList;
import javax.ws.rs.core.MediaType;

@RestController
@RequestMapping(value = "/caseprogression")
@Slf4j
public class FRCCDCallbackController {

    @PostMapping(path = "/fee-lookup", consumes = MediaType.APPLICATION_JSON,
            produces = MediaType.APPLICATION_JSON)
    public ResponseEntity<CCDCallbackResponse> addCase(@RequestHeader(value = "Authorization", required = false) String authorizationToken,
                                                       @RequestBody CCDRequest caseDetailsRequest) {
        log.info("Received request with a case-added event. Auth token: {}", authorizationToken);
        return ResponseEntity.ok(new CCDCallbackResponse(null, new ArrayList<>(), new ArrayList<>()));
    }
}
