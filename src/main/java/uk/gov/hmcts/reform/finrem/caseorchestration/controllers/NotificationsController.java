package uk.gov.hmcts.reform.finrem.caseorchestration.controllers;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RestController;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDRequest;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.NotificationService;

import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

@RestController
@Slf4j
public class NotificationsController {

    @Autowired
    private NotificationService notificationService;

    @PostMapping(value = "/case-orchestration/notify/hwfSuccessful", consumes = APPLICATION_JSON_VALUE)
    public ResponseEntity<Object> sendHwfSuccessfulConfirmationEmail(
            @RequestBody CCDRequest ccdRequest,
            @RequestHeader(value = "Authorization")String userToken) {
        try {
            log.info("received notification request for case reference :    ", ccdRequest.getCaseId());
            notificationService.sendHWFSuccessfulConfirmationEmail(ccdRequest, userToken);
            return ResponseEntity.noContent().build();
        } catch (Exception exception) {
            log.error("HWFSuccessful Confirmation Email failed for case reference Number ",
                    ccdRequest.getCaseId(),
                    exception.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(exception.getMessage());
        }
    }
}
