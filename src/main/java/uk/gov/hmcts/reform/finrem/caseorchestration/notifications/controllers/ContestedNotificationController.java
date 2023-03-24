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
import static uk.gov.hmcts.reform.finrem.caseorchestration.notifications.domain.EmailTemplateNames.FR_BARRISTER_ACCESS_ADDED;
import static uk.gov.hmcts.reform.finrem.caseorchestration.notifications.domain.EmailTemplateNames.FR_BARRISTER_ACCESS_REMOVED;
import static uk.gov.hmcts.reform.finrem.caseorchestration.notifications.domain.EmailTemplateNames.FR_CONTESTED_APPLICATION_ISSUED;
import static uk.gov.hmcts.reform.finrem.caseorchestration.notifications.domain.EmailTemplateNames.FR_CONTESTED_CONSENT_ORDER_APPROVED;
import static uk.gov.hmcts.reform.finrem.caseorchestration.notifications.domain.EmailTemplateNames.FR_CONTESTED_CONSENT_ORDER_NOT_APPROVED;
import static uk.gov.hmcts.reform.finrem.caseorchestration.notifications.domain.EmailTemplateNames.FR_CONTESTED_DRAFT_ORDER;
import static uk.gov.hmcts.reform.finrem.caseorchestration.notifications.domain.EmailTemplateNames.FR_CONTESTED_GENERAL_APPLICATION_OUTCOME;
import static uk.gov.hmcts.reform.finrem.caseorchestration.notifications.domain.EmailTemplateNames.FR_CONTESTED_GENERAL_APPLICATION_REFER_TO_JUDGE;
import static uk.gov.hmcts.reform.finrem.caseorchestration.notifications.domain.EmailTemplateNames.FR_CONTESTED_GENERAL_EMAIL;
import static uk.gov.hmcts.reform.finrem.caseorchestration.notifications.domain.EmailTemplateNames.FR_CONTESTED_GENERAL_ORDER;
import static uk.gov.hmcts.reform.finrem.caseorchestration.notifications.domain.EmailTemplateNames.FR_CONTESTED_GENERAL_ORDER_CONSENT;
import static uk.gov.hmcts.reform.finrem.caseorchestration.notifications.domain.EmailTemplateNames.FR_CONTESTED_HWF_SUCCESSFUL;
import static uk.gov.hmcts.reform.finrem.caseorchestration.notifications.domain.EmailTemplateNames.FR_CONTESTED_INTERIM_HEARING;
import static uk.gov.hmcts.reform.finrem.caseorchestration.notifications.domain.EmailTemplateNames.FR_CONTESTED_NOC_CASEWORKER;
import static uk.gov.hmcts.reform.finrem.caseorchestration.notifications.domain.EmailTemplateNames.FR_CONTESTED_NOTICE_OF_CHANGE;
import static uk.gov.hmcts.reform.finrem.caseorchestration.notifications.domain.EmailTemplateNames.FR_CONTESTED_PREPARE_FOR_HEARING;
import static uk.gov.hmcts.reform.finrem.caseorchestration.notifications.domain.EmailTemplateNames.FR_CONTESTED_PREPARE_FOR_HEARING_ORDER_SENT;
import static uk.gov.hmcts.reform.finrem.caseorchestration.notifications.domain.EmailTemplateNames.FR_CONTESTED_UPDATE_FRC_COURT;
import static uk.gov.hmcts.reform.finrem.caseorchestration.notifications.domain.EmailTemplateNames.FR_CONTESTED_UPDATE_FRC_SOL;
import static uk.gov.hmcts.reform.finrem.caseorchestration.notifications.domain.EmailTemplateNames.FR_CONTEST_ORDER_APPROVED;
import static uk.gov.hmcts.reform.finrem.caseorchestration.notifications.domain.EmailTemplateNames.FR_CONTEST_ORDER_NOT_APPROVED;
import static uk.gov.hmcts.reform.finrem.caseorchestration.notifications.domain.EmailTemplateNames.FR_REJECT_GENERAL_APPLICATION;


@RestController
@RequestMapping(path = "/notify/contested")
@Slf4j
@Validated
public class ContestedNotificationController {

    @Autowired
    private EmailService emailService;

    @PostMapping(path = "/hwf-successful", consumes = APPLICATION_JSON_VALUE)
    public ResponseEntity<Void> sendEmailHwfSuccessFul(
        @RequestBody final NotificationRequest notificationRequest) {
        log.info("Received request for notification email for HWFSuccessful. Case ID : {}",
            notificationRequest.getCaseReferenceNumber());
        emailService.sendConfirmationEmail(notificationRequest, FR_CONTESTED_HWF_SUCCESSFUL);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }

    @PostMapping(path = "/application-issued", consumes = APPLICATION_JSON_VALUE)
    public ResponseEntity<Void> sendEmailApplicationIssued(
        @RequestBody final NotificationRequest notificationRequest) {
        log.info("Received request for notification email for Contested 'Application Issued'. Case ID : {}",
            notificationRequest.getCaseReferenceNumber());
        emailService.sendConfirmationEmail(notificationRequest, FR_CONTESTED_APPLICATION_ISSUED);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }

    @PostMapping(path = "/order-approved", consumes = APPLICATION_JSON_VALUE)
    public ResponseEntity<Void> sendEmailContestOrderApproved(
        @RequestBody final NotificationRequest notificationRequest) {
        log.info("Received request for notification email for 'Contest Order Approved'. Case ID : {}",
            notificationRequest.getCaseReferenceNumber());
        emailService.sendConfirmationEmail(notificationRequest, FR_CONTEST_ORDER_APPROVED);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }

    @PostMapping(path = "/prepare-for-hearing", consumes = APPLICATION_JSON_VALUE)
    public ResponseEntity<Void> sendContestedEmailPrepareForHearing(
        @RequestBody final NotificationRequest notificationRequest) {
        log.info("Received request for notification email for 'Prepare for hearing'. Case ID : {}",
            notificationRequest.getCaseReferenceNumber());
        emailService.sendConfirmationEmail(notificationRequest, FR_CONTESTED_PREPARE_FOR_HEARING);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }

    @PostMapping(path = "/prepare-for-hearing-order-sent", consumes = APPLICATION_JSON_VALUE)
    public ResponseEntity<Void> sendContestedEmailPrepareForHearingOrderSent(
        @RequestBody final NotificationRequest notificationRequest) {
        log.info("Received request for notification email for 'Prepare for hearing order sent'. Case ID : {}",
            notificationRequest.getCaseReferenceNumber());
        emailService.sendConfirmationEmail(notificationRequest, FR_CONTESTED_PREPARE_FOR_HEARING_ORDER_SENT);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }

    @PostMapping(path = "/draft-order", consumes = APPLICATION_JSON_VALUE)
    public ResponseEntity<Void> sendContestedDraftOrder(
        @RequestBody final NotificationRequest notificationRequest) {
        log.info("Received request for notification email for Contested 'Draft Order'. Case ID : {}",
            notificationRequest.getCaseReferenceNumber());
        emailService.sendConfirmationEmail(notificationRequest, FR_CONTESTED_DRAFT_ORDER);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }

    @PostMapping(path = "/order-not-approved", consumes = APPLICATION_JSON_VALUE)
    public ResponseEntity<Void> sendEmailContestOrderNotApproved(
        @RequestBody final NotificationRequest notificationRequest) {
        log.info("Received request for notification email for contest order not approved, Case ID : {}",
            notificationRequest.getCaseReferenceNumber());
        emailService.sendConfirmationEmail(notificationRequest, FR_CONTEST_ORDER_NOT_APPROVED);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }

    @PostMapping(path = "/consent-order-approved", consumes = APPLICATION_JSON_VALUE)
    public ResponseEntity<Void> sendEmailContestedConsentOrderApproved(
        @RequestBody final NotificationRequest notificationRequest) {
        log.info("Received request for notification email for consent order approved, Case ID : {}",
            notificationRequest.getCaseReferenceNumber());
        emailService.sendConfirmationEmail(notificationRequest, FR_CONTESTED_CONSENT_ORDER_APPROVED);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }

    @PostMapping(path = "/consent-order-not-approved", consumes = APPLICATION_JSON_VALUE)
    public ResponseEntity<Void> sendEmailContestedConsentOrderNotApproved(
        @RequestBody
         final NotificationRequest notificationRequest) {
        log.info("Received request for notification email for Consent Order Not Approved, Case ID : {}",
            notificationRequest.getCaseReferenceNumber());
        emailService.sendConfirmationEmail(notificationRequest, FR_CONTESTED_CONSENT_ORDER_NOT_APPROVED);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }

    @PostMapping(path = "/consent-general-order", consumes = APPLICATION_JSON_VALUE)
    public ResponseEntity<Void> sendEmailContestedGeneralOrderConsent(
        @RequestBody final NotificationRequest notificationRequest) {
        log.info("Received request for notification email for Contested general order (consent), Case ID : {}",
            notificationRequest.getCaseReferenceNumber());
        emailService.sendConfirmationEmail(notificationRequest, FR_CONTESTED_GENERAL_ORDER_CONSENT);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }

    @PostMapping(path = "/general-order", consumes = APPLICATION_JSON_VALUE)
    public ResponseEntity<Void> sendEmailContestedGeneralOrder(
        @RequestBody final NotificationRequest notificationRequest) {
        log.info("Received request for notification email for Contested general order, Case ID : {}",
            notificationRequest.getCaseReferenceNumber());
        emailService.sendConfirmationEmail(notificationRequest, FR_CONTESTED_GENERAL_ORDER);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }

    @PostMapping(path = "/general-application-refer-to-judge", consumes = APPLICATION_JSON_VALUE)
    public ResponseEntity<Void> sendEmailContestedGeneralApplicationReferToJudge(
        @RequestBody final NotificationRequest notificationRequest) {
        log.info("Received request for notification email for Contested general application refer to judge, Case ID : {}",
            notificationRequest.getCaseReferenceNumber());
        emailService.sendConfirmationEmail(notificationRequest, FR_CONTESTED_GENERAL_APPLICATION_REFER_TO_JUDGE);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }

    @PostMapping(path = "/general-application-outcome", consumes = APPLICATION_JSON_VALUE)
    public ResponseEntity<Void> sendEmailContestedGeneralApplicationOutcome(
        @RequestBody final NotificationRequest notificationRequest) {
        log.info("Received request for notification email for Contested General Application Outcome, Case ID : {}",
            notificationRequest.getCaseReferenceNumber());
        emailService.sendConfirmationEmail(notificationRequest, FR_CONTESTED_GENERAL_APPLICATION_OUTCOME);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }

    @PostMapping(path = "/general-email", consumes = APPLICATION_JSON_VALUE)
    public ResponseEntity<Void> sendGeneralEmail(
        @RequestBody final NotificationRequest notificationRequest) {
        log.info("Received request for notification email for contested general email Notification request : {}",
            notificationRequest);
        emailService.sendConfirmationEmail(notificationRequest, FR_CONTESTED_GENERAL_EMAIL);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }

    @PostMapping(path = "/prepare-for-interim-hearing-sent", consumes = APPLICATION_JSON_VALUE)
    public ResponseEntity<Void> sendContestedEmailPrepareForInterimHearingSent(
        @RequestBody final NotificationRequest notificationRequest) {
        log.info("Received request for notification email for 'Prepare for interim hearing sent'. Case ID : {}",
            notificationRequest.getCaseReferenceNumber());
        emailService.sendConfirmationEmail(notificationRequest, FR_CONTESTED_INTERIM_HEARING);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }

    @PostMapping(path = "/update-frc-information", consumes = APPLICATION_JSON_VALUE)
    public ResponseEntity<Void> sendContestedEmailUpdateFrcInfo(
        @RequestBody final NotificationRequest notificationRequest) {
        log.info("Received request for notification email for 'Update FRC Information event'");
        emailService.sendConfirmationEmail(notificationRequest, FR_CONTESTED_UPDATE_FRC_SOL);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }

    @PostMapping(path = "/update-frc-information/court", consumes = APPLICATION_JSON_VALUE)
    public ResponseEntity<Void> sendCourtContestedEmailUpdateFrcDetails(
        @RequestBody final NotificationRequest notificationRequest) {
        log.info("Received request for notification email to court for 'Update FRC Information event'");
        emailService.sendConfirmationEmail(notificationRequest, FR_CONTESTED_UPDATE_FRC_COURT);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }

    @PostMapping(path = "/notice-of-change", consumes = APPLICATION_JSON_VALUE)
    public ResponseEntity<Void> sendContestedEmailNoticeOfChange(
        @RequestBody final NotificationRequest notificationRequest) {
        log.info("Received request for notification email for 'Notice of Change'. Case ID : {}",
            notificationRequest.getCaseReferenceNumber());
        emailService.sendConfirmationEmail(notificationRequest, FR_CONTESTED_NOTICE_OF_CHANGE);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }

    @PostMapping(path = "/notice-of-change/caseworker", consumes = APPLICATION_JSON_VALUE)
    public ResponseEntity<Void> sendContestedEmailNoticeOfChangeCaseworker(
        @RequestBody final NotificationRequest notificationRequest) {
        log.info("Received request for notification email for caseworker-invoked 'Notice of Change'. Case ID : {}",
            notificationRequest.getCaseReferenceNumber());
        emailService.sendConfirmationEmail(notificationRequest, FR_CONTESTED_NOC_CASEWORKER);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }

    @PostMapping(path = "/general-application-rejected", consumes = APPLICATION_JSON_VALUE)
    public ResponseEntity<Void> sendContestedEmailRejectGeneralApplication(
        @RequestBody final NotificationRequest notificationRequest) {
        log.info("Received request for notification email for General Application Rejected event. Case ID : {}",
            notificationRequest.getCaseReferenceNumber());
        emailService.sendConfirmationEmail(notificationRequest, FR_REJECT_GENERAL_APPLICATION);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }

    @PostMapping(path = "/barrister-access-added", consumes = APPLICATION_JSON_VALUE)
    public ResponseEntity<Void> sendContestedEmailBarristerAccessAdded(
        @RequestBody final NotificationRequest notificationRequest) {
        log.info("Received request for notification email for Barrister Access Added event. Case ID : {}",
            notificationRequest.getCaseReferenceNumber());
        emailService.sendConfirmationEmail(notificationRequest, FR_BARRISTER_ACCESS_ADDED);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }

    @PostMapping(path = "/barrister-access-removed", consumes = APPLICATION_JSON_VALUE)
    public ResponseEntity<Void> sendContestedEmailBarristerAccessRemoved(
        @RequestBody final NotificationRequest notificationRequest) {
        log.info("Received request for notification email for Barrister Access Removed event. Case ID : {}",
            notificationRequest.getCaseReferenceNumber());
        emailService.sendConfirmationEmail(notificationRequest, FR_BARRISTER_ACCESS_REMOVED);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }
}
