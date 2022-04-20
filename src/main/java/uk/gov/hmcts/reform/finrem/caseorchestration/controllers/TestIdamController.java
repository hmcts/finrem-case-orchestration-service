package uk.gov.hmcts.reform.finrem.caseorchestration.controllers;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.SystemUserService;

import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

@Slf4j
@RestController
@RequestMapping(value = "/case-orchestration")
@RequiredArgsConstructor
public class TestIdamController {

    private final SystemUserService systemUserService;

    @PostMapping(path = "/test-idam", produces = APPLICATION_JSON_VALUE)
    public ResponseEntity<String> testIdam() {
        log.info("In test idam endpoint");
        String sysUserToken = systemUserService.getSysUserToken();
        log.info("Returned with token {}", sysUserToken);
        return ResponseEntity.ok("Success");
    }
}