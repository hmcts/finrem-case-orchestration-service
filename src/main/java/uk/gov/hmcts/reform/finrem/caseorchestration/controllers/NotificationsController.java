package uk.gov.hmcts.reform.finrem.caseorchestration.controllers;

import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RestController;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.ccd.client.model.CallbackRequest;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseDocument;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.AssignedToJudgeDocumentService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.BulkPrintService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.GeneralEmailService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.HelpWithFeesDocumentService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.ManualPaymentDocumentService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.NotificationService;

import java.io.IOException;
import java.util.Map;
import java.util.Objects;

import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;
import static uk.gov.hmcts.reform.finrem.caseorchestration.OrchestrationConstants.AUTHORIZATION_HEADER;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.FINAL_ORDER_COLLECTION;
import static uk.gov.hmcts.reform.finrem.caseorchestration.service.CommonFunction.isApplicantSolicitorAgreeToReceiveEmails;
import static uk.gov.hmcts.reform.finrem.caseorchestration.service.CommonFunction.isApplicantSolicitorResponsibleToDraftOrder;
import static uk.gov.hmcts.reform.finrem.caseorchestration.service.CommonFunction.isConsentedApplication;
import static uk.gov.hmcts.reform.finrem.caseorchestration.service.CommonFunction.isContestedPaperApplication;
import static uk.gov.hmcts.reform.finrem.caseorchestration.service.CommonFunction.isPaperApplication;

@RestController
@Slf4j
@RequiredArgsConstructor
public class NotificationsController implements BaseController {

    private final NotificationService notificationService;
    private final BulkPrintService bulkPrintService;
    private final AssignedToJudgeDocumentService assignedToJudgeDocumentService;
    private final HelpWithFeesDocumentService helpWithFeesDocumentService;
    private final ManualPaymentDocumentService manualPaymentDocumentService;
    private final GeneralEmailService generalEmailService;

    @PostMapping(value = "/case-orchestration/notify/hwf-successful", consumes = APPLICATION_JSON_VALUE)
    @ApiOperation(value = "Notify Applicant/Applicant Solicitor of HWF Successful by email or letter.")
    @ApiResponses(value = {
        @ApiResponse(code = 200, message = "HWFSuccessful notification sent successfully",
            response = AboutToStartOrSubmitCallbackResponse.class)})
    public ResponseEntity<AboutToStartOrSubmitCallbackResponse> sendHwfSuccessfulConfirmationEmail(
        @RequestHeader(value = AUTHORIZATION_HEADER) String authToken,
        @RequestBody CallbackRequest callbackRequest) {

        log.info("Received request to send email for HWF Successful for Case ID: {}", callbackRequest.getCaseDetails().getId());
        validateCaseData(callbackRequest);
        CaseDetails caseDetails = callbackRequest.getCaseDetails();
        Map<String, Object> caseData = caseDetails.getData();

        if (isConsentedApplication(callbackRequest.getCaseDetails())) {
            if (isPaperApplication(caseData)) {
                log.info("Case is paper application");
                log.info("Sending Consented HWF Successful notification letter for bulk print");

                // Generate PDF notification letter
                CaseDocument hwfSuccessfulNotificationLetter =
                    helpWithFeesDocumentService.generateHwfSuccessfulNotificationLetter(caseDetails, authToken);

                // Send notification letter to Bulk Print
                bulkPrintService.sendDocumentForPrint(hwfSuccessfulNotificationLetter, caseDetails);
                log.info("Notification letter sent to Bulk Print: {} for Case ID: {}", hwfSuccessfulNotificationLetter,
                    caseDetails.getId());

            } else if (isApplicantSolicitorAgreeToReceiveEmails(caseDetails)) {
                log.info("Sending Consented HWF Successful email notification to Solicitor");
                notificationService.sendConsentedHWFSuccessfulConfirmationEmail(callbackRequest);
            }
        } else if (isApplicantSolicitorAgreeToReceiveEmails(caseDetails)) {
            log.info("Sending Contested HWF Successful email notification to Solicitor");
            notificationService.sendContestedHwfSuccessfulConfirmationEmail(callbackRequest);
        }
        return ResponseEntity.ok(AboutToStartOrSubmitCallbackResponse.builder().data(caseData).build());
    }

    @PostMapping(value = "/case-orchestration/notify/assign-to-judge", consumes = APPLICATION_JSON_VALUE)
    @ApiOperation(value = "Notify solicitor when Judge assigned to case via email or letter")
    @ApiResponses(value = {
        @ApiResponse(code = 204, message = "Case assigned to Judge notification sent successfully",
            response = AboutToStartOrSubmitCallbackResponse.class)})
    public ResponseEntity<AboutToStartOrSubmitCallbackResponse> sendAssignToJudgeConfirmationEmail(
        @RequestHeader(value = AUTHORIZATION_HEADER) String authToken,
        @RequestBody CallbackRequest callbackRequest) {

        log.info("Received request to notify solicitor for Judge successfully assigned to case for Case ID: {}",
            callbackRequest.getCaseDetails().getId());
        validateCaseData(callbackRequest);
        CaseDetails caseDetails = callbackRequest.getCaseDetails();
        Map<String, Object> caseData = caseDetails.getData();

        if (isPaperApplication(caseData)) {
            log.info("Sending AssignedToJudge notification letter for bulk print for Case ID: {}",
                callbackRequest.getCaseDetails().getId());

            // Generate PDF notification letter
            CaseDocument assignedToJudgeNotificationLetter =
                assignedToJudgeDocumentService.generateAssignedToJudgeNotificationLetter(caseDetails, authToken);

            // Send notification letter to Bulk Print
            bulkPrintService.sendDocumentForPrint(assignedToJudgeNotificationLetter, caseDetails);
            log.info("Notification letter sent to Bulk Print: {} for Case ID: {}", assignedToJudgeNotificationLetter,
                caseDetails.getId());
        } else if (isApplicantSolicitorAgreeToReceiveEmails(caseDetails)) {
            log.info("Sending email notification to Solicitor for Judge successfully assigned to case");
            notificationService.sendAssignToJudgeConfirmationEmail(callbackRequest);
        }

        return ResponseEntity.ok(AboutToStartOrSubmitCallbackResponse.builder().data(caseData).build());
    }

    @PostMapping(value = "/case-orchestration/notify/consent-order-made", consumes = APPLICATION_JSON_VALUE)
    @ApiOperation(value = "send e-mail for Consent Order Made.")
    @ApiResponses(value = {
        @ApiResponse(code = 204, message = "Consent order made e-mail sent successfully",
            response = AboutToStartOrSubmitCallbackResponse.class)})
    public ResponseEntity<AboutToStartOrSubmitCallbackResponse> sendConsentOrderMadeConfirmationEmail(
        @RequestBody CallbackRequest callbackRequest) {

        log.info("Received request to send email for 'Consent Order Made' for Case ID: {}", callbackRequest.getCaseDetails().getId());
        validateCaseData(callbackRequest);
        CaseDetails caseDetails = callbackRequest.getCaseDetails();
        Map<String, Object> caseData = caseDetails.getData();

        if (isApplicantSolicitorAgreeToReceiveEmails(caseDetails)) {
            log.info("Sending email notification to Solicitor for 'Consent Order Made'");
            notificationService.sendConsentOrderMadeConfirmationEmail(callbackRequest);
        }
        return ResponseEntity.ok(AboutToStartOrSubmitCallbackResponse.builder().data(caseData).build());
    }

    @PostMapping(value = "/case-orchestration/notify/order-not-approved", consumes = APPLICATION_JSON_VALUE)
    @ApiOperation(value = "send e-mail for consent/contest order not approved.")
    @ApiResponses(value = {
        @ApiResponse(code = 204, message = "Consent/Contest order not approved e-mail sent successfully",
            response = AboutToStartOrSubmitCallbackResponse.class)})
    public ResponseEntity<AboutToStartOrSubmitCallbackResponse> sendConsentOrderNotApprovedEmail(
        @RequestBody CallbackRequest callbackRequest) {

        log.info("Received request to send email for 'Consent/Contest Order Not Approved' for Case ID: {}", callbackRequest.getCaseDetails().getId());
        validateCaseData(callbackRequest);
        CaseDetails caseDetails = callbackRequest.getCaseDetails();
        Map<String, Object> caseData = caseDetails.getData();

        if (isApplicantSolicitorAgreeToReceiveEmails(caseDetails)) {
            if (isConsentedApplication(callbackRequest.getCaseDetails())) {
                log.info("Sending email notification to Solicitor for 'Consent Order Not Approved'");
                notificationService.sendConsentOrderNotApprovedEmail(callbackRequest);
            } else {
                log.info("Sending email notification to Solicitor for 'Contest Order Not Approved'");
                notificationService.sendContestOrderNotApprovedEmail(callbackRequest);
            }
        }
        return ResponseEntity.ok(AboutToStartOrSubmitCallbackResponse.builder().data(caseData).build());
    }

    @PostMapping(value = "/case-orchestration/notify/contested-consent-order-approved", consumes = APPLICATION_JSON_VALUE)
    @ApiOperation(value = "send e-mail for contested consent order approved.")
    @ApiResponses(value = {
        @ApiResponse(code = 204, message = "Contested consent order approved e-mail sent successfully",
            response = AboutToStartOrSubmitCallbackResponse.class)})
    public ResponseEntity<AboutToStartOrSubmitCallbackResponse> sendContestedConsentOrderApprovedEmail(
        @RequestBody CallbackRequest callbackRequest) {

        log.info("Received request to send email for 'Contested Consent Order Approved' for Case ID: {}", callbackRequest.getCaseDetails().getId());
        validateCaseData(callbackRequest);
        CaseDetails caseDetails = callbackRequest.getCaseDetails();
        Map<String, Object> caseData = caseDetails.getData();

        if (isApplicantSolicitorAgreeToReceiveEmails(caseDetails)) {
            log.info("Sending email notification to Solicitor for 'Contested Consent Order Approved'");
            notificationService.sendContestedConsentOrderApprovedEmail(callbackRequest);
        }

        return ResponseEntity.ok(AboutToStartOrSubmitCallbackResponse.builder().data(caseData).build());
    }

    @PostMapping(value = "/case-orchestration/notify/contested-consent-order-not-approved", consumes = APPLICATION_JSON_VALUE)
    @ApiOperation(value = "send e-mail for contested consent order not approved.")
    @ApiResponses(value = {
        @ApiResponse(code = 204, message = "Contested consent order not approved e-mail sent successfully",
            response = AboutToStartOrSubmitCallbackResponse.class)})
    public ResponseEntity<AboutToStartOrSubmitCallbackResponse> sendContestedConsentOrderNotApprovedEmail(
        @RequestBody CallbackRequest callbackRequest) {

        log.info("Received request to send email for 'Contested Consent Order Not Approved' for Case ID: {}",
            callbackRequest.getCaseDetails().getId());
        validateCaseData(callbackRequest);
        CaseDetails caseDetails = callbackRequest.getCaseDetails();
        Map<String, Object> caseData = caseDetails.getData();

        if (isApplicantSolicitorAgreeToReceiveEmails(caseDetails)) {
            log.info("Sending email notification to Solicitor for 'Contested Consent General Order'");
            notificationService.sendContestedConsentOrderNotApprovedEmail(callbackRequest);
        }

        return ResponseEntity.ok(AboutToStartOrSubmitCallbackResponse.builder().data(caseData).build());
    }

    @PostMapping(value = "/case-orchestration/notify/contested-consent-general-order", consumes = APPLICATION_JSON_VALUE)
    @ApiOperation(value = "send e-mail for contested consent general order.")
    @ApiResponses(value = {
        @ApiResponse(code = 204, message = "Contested consent general order e-mail sent successfully",
            response = AboutToStartOrSubmitCallbackResponse.class)})
    public ResponseEntity<AboutToStartOrSubmitCallbackResponse> sendContestedConsentGeneralOrderEmail(
        @RequestBody CallbackRequest callbackRequest) {

        log.info("Received request to send email for 'Contested Consent General Order' for Case ID: {}", callbackRequest.getCaseDetails().getId());
        validateCaseData(callbackRequest);
        CaseDetails caseDetails = callbackRequest.getCaseDetails();
        Map<String, Object> caseData = caseDetails.getData();

        if (isApplicantSolicitorAgreeToReceiveEmails(caseDetails)) {
            log.info("Sending email notification to Solicitor for 'Contested Consent General Order'");
            notificationService.sendContestedConsentGeneralOrderEmail(callbackRequest);
        }

        return ResponseEntity.ok(AboutToStartOrSubmitCallbackResponse.builder().data(caseData).build());
    }

    @PostMapping(value = "/case-orchestration/notify/consent-order-available", consumes = APPLICATION_JSON_VALUE)
    @ApiOperation(value = "send e-mail for Consent order available.")
    @ApiResponses(value = {
        @ApiResponse(code = 204, message = "Consent order available e-mail sent successfully",
            response = AboutToStartOrSubmitCallbackResponse.class)})
    public ResponseEntity<AboutToStartOrSubmitCallbackResponse> sendConsentOrderAvailableEmail(
        @RequestBody CallbackRequest callbackRequest) {

        log.info("Received request to send email for 'Consent Order Available' for Case ID: {}", callbackRequest.getCaseDetails().getId());
        validateCaseData(callbackRequest);
        CaseDetails caseDetails = callbackRequest.getCaseDetails();
        Map<String, Object> caseData = caseDetails.getData();

        if (isApplicantSolicitorAgreeToReceiveEmails(caseDetails)) {
            log.info("Sending email notification to Solicitor for 'Consent Order Available'");
            notificationService.sendConsentOrderAvailableEmail(callbackRequest);
        }
        return ResponseEntity.ok(AboutToStartOrSubmitCallbackResponse.builder().data(caseData).build());
    }

    @PostMapping(value = "/case-orchestration/notify/prepare-for-hearing", consumes = APPLICATION_JSON_VALUE)
    @ApiOperation(value = "send e-mail for 'Prepare for Hearing'.")
    @ApiResponses(value = {
        @ApiResponse(code = 204, message = "'Prepare for Hearing' e-mail sent successfully",
            response = AboutToStartOrSubmitCallbackResponse.class)})
    public ResponseEntity<AboutToStartOrSubmitCallbackResponse> sendPrepareForHearingEmail(
        @RequestBody CallbackRequest callbackRequest) {

        log.info("Received request to send email for 'Prepare for Hearing' for Case ID: {}", callbackRequest.getCaseDetails().getId());
        validateCaseData(callbackRequest);
        CaseDetails caseDetails = callbackRequest.getCaseDetails();
        Map<String, Object> caseData = caseDetails.getData();

        if (isApplicantSolicitorAgreeToReceiveEmails(caseDetails)) {
            log.info("Sending email notification to Applicant Solicitor for 'Prepare for Hearing'");
            notificationService.sendPrepareForHearingEmail(callbackRequest);
        }
        return ResponseEntity.ok(AboutToStartOrSubmitCallbackResponse.builder().data(caseData).build());
    }

    @PostMapping(value = "/case-orchestration/notify/prepare-for-hearing-order-sent", consumes = APPLICATION_JSON_VALUE)
    @ApiOperation(value = "send e-mail for 'Prepare for Hearing (after order sent)'.")
    @ApiResponses(value = {
        @ApiResponse(code = 204, message = "'Prepare for Hearing (after send order)' e-mail sent successfully",
            response = AboutToStartOrSubmitCallbackResponse.class)})
    public ResponseEntity<AboutToStartOrSubmitCallbackResponse> sendPrepareForHearingOrderSentEmail(
        @RequestBody CallbackRequest callbackRequest) {

        log.info("Received request to send email for 'Prepare for Hearing (after order sent)' for Case ID: {}",
            callbackRequest.getCaseDetails().getId());
        validateCaseData(callbackRequest);
        CaseDetails caseDetails = callbackRequest.getCaseDetails();
        Map<String, Object> caseData = caseDetails.getData();

        if (isApplicantSolicitorAgreeToReceiveEmails(caseDetails)) {
            log.info("Sending email notification to Applicant Solicitor for 'Prepare for Hearing (after send order)'");
            notificationService.sendPrepareForHearingOrderSentEmail(callbackRequest);
        }
        return ResponseEntity.ok(AboutToStartOrSubmitCallbackResponse.builder().data(caseData).build());
    }

    @PostMapping(value = "/case-orchestration/notify/contest-application-issued", consumes = APPLICATION_JSON_VALUE)
    @ApiOperation(value = "send e-mail for Contested 'Application Issued'.")
    @ApiResponses(value = {
        @ApiResponse(code = 204, message = "Consent order available e-mail sent successfully",
            response = AboutToStartOrSubmitCallbackResponse.class)})
    public ResponseEntity<AboutToStartOrSubmitCallbackResponse> sendContestedApplicationIssuedEmail(
        @RequestBody CallbackRequest callbackRequest) {
        log.info("Received request to send email for Contested 'Application Issued' for Case ID: {}", callbackRequest.getCaseDetails().getId());
        validateCaseData(callbackRequest);
        CaseDetails caseDetails = callbackRequest.getCaseDetails();
        Map<String, Object> caseData = caseDetails.getData();

        if (isApplicantSolicitorAgreeToReceiveEmails(caseDetails)) {
            log.info("Sending Contested 'Application Issued' email notification to Applicant Solicitor");
            notificationService.sendContestedApplicationIssuedEmail(callbackRequest);
        }
        return ResponseEntity.ok(AboutToStartOrSubmitCallbackResponse.builder().data(caseData).build());
    }

    @PostMapping(value = "/case-orchestration/notify/contest-order-approved", consumes = APPLICATION_JSON_VALUE)
    @ApiOperation(value = "send e-mail for 'Contest Order Approved'.")
    @ApiResponses(value = {
        @ApiResponse(code = 204, message = "Contest order approved e-mail sent successfully",
            response = AboutToStartOrSubmitCallbackResponse.class)})
    public ResponseEntity<AboutToStartOrSubmitCallbackResponse> sendContestOrderApprovedEmail(
        @RequestBody CallbackRequest callbackRequest) {

        validateCaseData(callbackRequest);
        CaseDetails caseDetails = callbackRequest.getCaseDetails();
        Map<String, Object> caseData = caseDetails.getData();

        if (!isPaperApplication(caseData) && Objects.nonNull(caseData.get(FINAL_ORDER_COLLECTION))) {
            log.info("Received request to send email for 'Contest Order Approved' for Case ID: {}", callbackRequest.getCaseDetails().getId());
            if (isApplicantSolicitorAgreeToReceiveEmails(caseDetails)) {
                log.info("Sending 'Contest Order Approved' email notification to Applicant Solicitor");
                notificationService.sendContestOrderApprovedEmail(callbackRequest);
            }
        }

        return ResponseEntity.ok(AboutToStartOrSubmitCallbackResponse.builder().data(caseData).build());
    }

    @PostMapping(value = "/case-orchestration/notify/draft-order", consumes = APPLICATION_JSON_VALUE)
    @ApiOperation(value = "send e-mail for Solicitor To Draft Order")
    @ApiResponses(value = {
            @ApiResponse(code = 204, message = "Draft Order e-mail sent successfully",
                    response = AboutToStartOrSubmitCallbackResponse.class)})
    public ResponseEntity<AboutToStartOrSubmitCallbackResponse> sendDraftOrderEmail(
            @RequestBody CallbackRequest callbackRequest) {

        log.info("Received request to send email for 'Applicant Solicitor To Draft Order' for Case ID: {}", callbackRequest.getCaseDetails().getId());
        validateCaseData(callbackRequest);
        CaseDetails caseDetails = callbackRequest.getCaseDetails();
        Map<String, Object> caseData = caseDetails.getData();

        if (isApplicantSolicitorResponsibleToDraftOrder(caseData) && isApplicantSolicitorAgreeToReceiveEmails(caseDetails)) {
            log.info("Sending email notification to Applicant Solicitor for 'Draft Order'");
            notificationService.sendSolicitorToDraftOrderEmail(callbackRequest);
        }
        return ResponseEntity.ok(AboutToStartOrSubmitCallbackResponse.builder().data(caseData).build());
    }

    @PostMapping(value = "/case-orchestration/notify/general-email", consumes = APPLICATION_JSON_VALUE)
    @ApiOperation(value = "send a general email")
    @ApiResponses(value = {
        @ApiResponse(code = 204, message = "General e-mail sent successfully",
            response = AboutToStartOrSubmitCallbackResponse.class)})
    public ResponseEntity<AboutToStartOrSubmitCallbackResponse> sendGeneralEmail(
        @RequestBody CallbackRequest callbackRequest) {

        log.info("Received request to send general email for Case ID: {}", callbackRequest.getCaseDetails().getId());
        validateCaseData(callbackRequest);

        log.info("Sending general email notification");
        if (isConsentedApplication(callbackRequest.getCaseDetails())) {
            notificationService.sendConsentGeneralEmail(callbackRequest);
        } else {
            notificationService.sendContestedGeneralEmail(callbackRequest);
        }

        CaseDetails updatedDetails = generalEmailService.storeGeneralEmail(callbackRequest.getCaseDetails());

        return ResponseEntity.ok(AboutToStartOrSubmitCallbackResponse.builder().data(updatedDetails.getData()).build());
    }

    @PostMapping(value = "/case-orchestration/notify/manual-payment", consumes = APPLICATION_JSON_VALUE)
    @ApiOperation(value = "send a manual payment letter")
    @ApiResponses(value = {
        @ApiResponse(code = 204, message = "Manual Payment letter sent successfully",
            response = AboutToStartOrSubmitCallbackResponse.class)})
    public ResponseEntity<AboutToStartOrSubmitCallbackResponse> sendManualPayment(
        @RequestHeader(value = AUTHORIZATION_HEADER) String authToken,
        @RequestBody CallbackRequest callbackRequest) {
        log.info("Received request to send Manual Payment Letter for Case ID: {}", callbackRequest.getCaseDetails().getId());
        validateCaseData(callbackRequest);

        CaseDetails caseDetails = callbackRequest.getCaseDetails();

        if (isContestedPaperApplication(caseDetails)) {
            CaseDocument applicantManualPaymentLetter =
                manualPaymentDocumentService.generateApplicantManualPaymentLetter(caseDetails, authToken);
            bulkPrintService.sendDocumentForPrint(applicantManualPaymentLetter, caseDetails);
        }

        return ResponseEntity.ok(AboutToStartOrSubmitCallbackResponse.builder().data(caseDetails.getData()).build());
    }

    @PostMapping(value = "/case-orchestration/notify/general-application-refer-to-judge", consumes = APPLICATION_JSON_VALUE)
    @ApiOperation(value = "send general application refer to judge email")
    @ApiResponses(value = {
        @ApiResponse(code = 204, message = "General application refer to judge email sent successfully",
            response = AboutToStartOrSubmitCallbackResponse.class)})
    public ResponseEntity<AboutToStartOrSubmitCallbackResponse> sendGeneralApplicationReferToJudgeEmail(
        @RequestHeader(value = AUTHORIZATION_HEADER) String authToken,
        @RequestBody CallbackRequest callbackRequest) {
        log.info("Received request to send general application refer to judge email for Case ID: {}", callbackRequest.getCaseDetails().getId());
        validateCaseData(callbackRequest);

        CaseDetails caseDetails = callbackRequest.getCaseDetails();

        notificationService.sendContestedGeneralApplicationReferToJudgeEmail(callbackRequest);

        return ResponseEntity.ok(AboutToStartOrSubmitCallbackResponse.builder().data(caseDetails.getData()).build());
    }

    @PostMapping(value = "/case-orchestration/notify/general-application-outcome", consumes = APPLICATION_JSON_VALUE)
    @ApiOperation(value = "send general application outcome email")
    @ApiResponses(value = {
        @ApiResponse(code = 204, message = "General Application Outcome email sent successfully",
            response = AboutToStartOrSubmitCallbackResponse.class)})
    public ResponseEntity<AboutToStartOrSubmitCallbackResponse> sendGeneralApplicationOutcomeEmail(
        @RequestHeader(value = AUTHORIZATION_HEADER) String authToken,
        @RequestBody CallbackRequest callbackRequest) throws IOException {
        log.info("Received request to send General Application Outcome email for Case ID: {}", callbackRequest.getCaseDetails().getId());
        validateCaseData(callbackRequest);

        CaseDetails caseDetails = callbackRequest.getCaseDetails();

        notificationService.sendContestedGeneralApplicationOutcomeEmail(callbackRequest);

        return ResponseEntity.ok(AboutToStartOrSubmitCallbackResponse.builder().data(caseDetails.getData()).build());
    }
}
