package uk.gov.hmcts.reform.finrem.caseorchestration.notifications.controllers;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.notification.NotificationRequest;
import uk.gov.hmcts.reform.finrem.caseorchestration.notifications.service.EmailService;

import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;
import static uk.gov.hmcts.reform.finrem.caseorchestration.notifications.domain.EmailTemplateNames.FR_ASSIGNED_TO_JUDGE;
import static uk.gov.hmcts.reform.finrem.caseorchestration.notifications.domain.EmailTemplateNames.FR_CONSENTED_GENERAL_ORDER;
import static uk.gov.hmcts.reform.finrem.caseorchestration.notifications.domain.EmailTemplateNames.FR_CONSENTED_LIST_FOR_HEARING;
import static uk.gov.hmcts.reform.finrem.caseorchestration.notifications.domain.EmailTemplateNames.FR_CONSENTED_NOC_CASEWORKER;
import static uk.gov.hmcts.reform.finrem.caseorchestration.notifications.domain.EmailTemplateNames.FR_CONSENTED_NOTICE_OF_CHANGE;
import static uk.gov.hmcts.reform.finrem.caseorchestration.notifications.domain.EmailTemplateNames.FR_CONSENT_GENERAL_EMAIL;
import static uk.gov.hmcts.reform.finrem.caseorchestration.notifications.domain.EmailTemplateNames.FR_CONSENT_ORDER_AVAILABLE;
import static uk.gov.hmcts.reform.finrem.caseorchestration.notifications.domain.EmailTemplateNames.FR_CONSENT_ORDER_AVAILABLE_CTSC;
import static uk.gov.hmcts.reform.finrem.caseorchestration.notifications.domain.EmailTemplateNames.FR_CONSENT_ORDER_MADE;
import static uk.gov.hmcts.reform.finrem.caseorchestration.notifications.domain.EmailTemplateNames.FR_CONSENT_ORDER_NOT_APPROVED;
import static uk.gov.hmcts.reform.finrem.caseorchestration.notifications.domain.EmailTemplateNames.FR_CONSENT_ORDER_NOT_APPROVED_SENT;
import static uk.gov.hmcts.reform.finrem.caseorchestration.notifications.domain.EmailTemplateNames.FR_HWF_SUCCESSFUL;
import static uk.gov.hmcts.reform.finrem.caseorchestration.notifications.domain.EmailTemplateNames.FR_TRANSFER_TO_LOCAL_COURT;

@RestController
@RequestMapping(path = "/notify")
@Slf4j
@Validated
public class ConsentedNotificationController {

    @Autowired
    private EmailService emailService;

    @PostMapping(path = "/hwf-successful", consumes = APPLICATION_JSON_VALUE)
    public ResponseEntity<Void> sendEmailHwfSuccessFul(
        @RequestBody final NotificationRequest notificationRequest) {
        log.info("Received request for notification email for HWFSuccessful. Case ID : {}",
            notificationRequest.getCaseReferenceNumber());
        emailService.sendConfirmationEmail(notificationRequest, FR_HWF_SUCCESSFUL);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }

    @PostMapping(path = "/assign-to-judge", consumes = APPLICATION_JSON_VALUE)
    public ResponseEntity<Void> sendEmailAssignToJudge(
        @RequestBody
 final NotificationRequest notificationRequest) {
        log.info("Received request for notification email for Case assigned to Judge Case ID : {}",
            notificationRequest.getCaseReferenceNumber());
        emailService.sendConfirmationEmail(notificationRequest, FR_ASSIGNED_TO_JUDGE);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }

    @PostMapping(path = "/consent-order-made", consumes = APPLICATION_JSON_VALUE)
    public ResponseEntity<Void> sendEmailConsentOrderApproved(
        @RequestBody final NotificationRequest notificationRequest) {
        log.info("Received request for notification email for consent order made. Case ID : {}",
            notificationRequest.getCaseReferenceNumber());
        emailService.sendConfirmationEmail(notificationRequest, FR_CONSENT_ORDER_MADE);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }

    @PostMapping(path = "/consent-order-not-approved", consumes = APPLICATION_JSON_VALUE)
    public ResponseEntity<Void> sendEmailConsentOrderNotApproved(
        @RequestBody
 final NotificationRequest notificationRequest) {
        log.info("Received request for notification email for consent order not approved, Case ID : {}",
            notificationRequest.getCaseReferenceNumber());

        emailService.sendConfirmationEmail(notificationRequest, FR_CONSENT_ORDER_NOT_APPROVED);

        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }

    @PostMapping(path = "/consent-order-not-approved-sent", consumes = APPLICATION_JSON_VALUE)
    public ResponseEntity<Void> sendEmailConsentOrderNotApprovedSent(
        @RequestBody
 final NotificationRequest notificationRequest) {
        log.info("Received request for notification email for consent order not approved sent, Case ID : {}",
            notificationRequest.getCaseReferenceNumber());

        emailService.sendConfirmationEmail(notificationRequest, FR_CONSENT_ORDER_NOT_APPROVED_SENT);

        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }

    @PostMapping(path = "/consent-order-available", consumes = APPLICATION_JSON_VALUE)
    public ResponseEntity<Void> sendEmailConsentOrderAvailable(
        @RequestBody
 final NotificationRequest notificationRequest) {
        log.info("Received request for notification email for consent order available Case ID : {}",
            notificationRequest.getCaseReferenceNumber());
        emailService.sendConfirmationEmail(notificationRequest, FR_CONSENT_ORDER_AVAILABLE);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }

    @PostMapping(path = "/consent-order-available-ctsc", consumes = APPLICATION_JSON_VALUE)
    public ResponseEntity<Void> sendEmailConsentOrderSentCtsc(
        @RequestBody
 final NotificationRequest notificationRequest) {
        log.info("Received request for notification email for CTSC consent order available Case ID : {}",
            notificationRequest.getCaseReferenceNumber());
        emailService.sendConfirmationEmail(notificationRequest, FR_CONSENT_ORDER_AVAILABLE_CTSC);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }

    @PostMapping(path = "/general-order", consumes = APPLICATION_JSON_VALUE)
    public ResponseEntity<Void> sendEmailConsentedGeneralOrder(
        @RequestBody final NotificationRequest notificationRequest) {
        log.info("Received request for notification email for Consented general order, Case ID : {}",
            notificationRequest.getCaseReferenceNumber());
        emailService.sendConfirmationEmail(notificationRequest, FR_CONSENTED_GENERAL_ORDER);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }

    @PostMapping(path = "/general-email", consumes = APPLICATION_JSON_VALUE)
    public ResponseEntity<Void> sendGeneralEmail(
        @RequestBody
 final NotificationRequest notificationRequest) {
        log.info("Received request for notification email for consented general email Notification request : {}",
            notificationRequest);
        emailService.sendConfirmationEmail(notificationRequest, FR_CONSENT_GENERAL_EMAIL);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }

    @PostMapping(path = "/transfer-to-local-court", consumes = APPLICATION_JSON_VALUE)
    public ResponseEntity<Void> sendTransferToLocalCourtEmail(
        @RequestBody
 final NotificationRequest notificationRequest) {
        log.info("Received request for notification email for consented transfer to local court email Notification request : {}",
            notificationRequest);
        emailService.sendConfirmationEmail(notificationRequest, FR_TRANSFER_TO_LOCAL_COURT);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }

    @PostMapping(path = "/notice-of-change", consumes = APPLICATION_JSON_VALUE)
    public ResponseEntity<Void> sendConsentedEmailNoticeOfChange(
        @RequestBody final NotificationRequest notificationRequest) {
        log.info("Received request for notification email for 'Notice of Change'. Case ID : {}",
            notificationRequest.getCaseReferenceNumber());
        emailService.sendConfirmationEmail(notificationRequest, FR_CONSENTED_NOTICE_OF_CHANGE);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }

    @PostMapping(path = "/notice-of-change/caseworker", consumes = APPLICATION_JSON_VALUE)
    public ResponseEntity<Void> sendConsentedEmailNoticeOfChangeCaseworker(
        @RequestBody final NotificationRequest notificationRequest) {
        log.info("Received request for notification email for caseworker-invoked 'Notice of Change'. Case ID : {}",
            notificationRequest.getCaseReferenceNumber());
        emailService.sendConfirmationEmail(notificationRequest, FR_CONSENTED_NOC_CASEWORKER);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }

    @PostMapping(path = "/list-for-hearing", consumes = APPLICATION_JSON_VALUE)
    public ResponseEntity<Void> sendConsentedEmailForHearingSent(
        @RequestBody final NotificationRequest notificationRequest) {
        log.info("Received request for notification email for 'hearing'. Case ID : {}",
            notificationRequest.getCaseReferenceNumber());
        emailService.sendConfirmationEmail(notificationRequest, FR_CONSENTED_LIST_FOR_HEARING);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }
}
