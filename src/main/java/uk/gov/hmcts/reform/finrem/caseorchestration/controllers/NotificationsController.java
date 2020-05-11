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
import uk.gov.hmcts.reform.finrem.caseorchestration.service.FeatureToggleService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.NotificationService;

import java.util.Map;

import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;
import static uk.gov.hmcts.reform.finrem.caseorchestration.OrchestrationConstants.AUTHORIZATION_HEADER;
import static uk.gov.hmcts.reform.finrem.caseorchestration.controllers.BaseController.isConsentedApplication;
import static uk.gov.hmcts.reform.finrem.caseorchestration.service.CommonFunction.isApplicantSolicitorAgreeToReceiveEmails;
import static uk.gov.hmcts.reform.finrem.caseorchestration.service.CommonFunction.isApplicantSolicitorResponsibleToDraftOrder;
import static uk.gov.hmcts.reform.finrem.caseorchestration.service.CommonFunction.isPaperApplication;

@RestController
@Slf4j
@RequiredArgsConstructor
public class NotificationsController implements BaseController {

    private final NotificationService notificationService;
    private final BulkPrintService bulkPrintService;
    private final FeatureToggleService featureToggleService;
    private final AssignedToJudgeDocumentService assignedToJudgeDocumentService;

    @PostMapping(value = "/case-orchestration/notify/hwf-successful", consumes = APPLICATION_JSON_VALUE)
    @ApiOperation(value = "send e-mail for HWF Successful.")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "HWFSuccessful e-mail sent successfully",
                    response = AboutToStartOrSubmitCallbackResponse.class)})
    public ResponseEntity<AboutToStartOrSubmitCallbackResponse> sendHwfSuccessfulConfirmationEmail(
            @RequestBody CallbackRequest callbackRequest) {
        log.info("Received request to send email for HWF Successful for Case ID: {}", callbackRequest.getCaseDetails().getId());
        validateCaseData(callbackRequest);
        Map<String, Object> caseData = callbackRequest.getCaseDetails().getData();
        if (isApplicantSolicitorAgreeToReceiveEmails(caseData)) {
            if (isConsentedApplication(caseData)) {
                log.info("Sending Consented HWF Successful email notification to Solicitor");
                notificationService.sendConsentedHWFSuccessfulConfirmationEmail(callbackRequest);
            } else {
                log.info("Sending Contested HWF Successful email notification to Solicitor");
                notificationService.sendContestedHwfSuccessfulConfirmationEmail(callbackRequest);
            }
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
        Map<String, Object> caseData = callbackRequest.getCaseDetails().getData();
        if (isApplicantSolicitorAgreeToReceiveEmails(caseData)) {
            log.info("Sending email notification to Solicitor for Judge successfully assigned to case");
            notificationService.sendAssignToJudgeConfirmationEmail(callbackRequest);
        } else if (isConsentedApplication(caseData) && isPaperApplication(caseData)) {
            if (featureToggleService.isAssignedToJudgeNotificationLetterEnabled()) {
                log.info("isAssignedToJudgeNotificationLetterEnabled is toggled on");
                log.info("Sending AssignedToJudge notification letter for bulk print");

                CaseDetails caseDetails = callbackRequest.getCaseDetails();

                // Generate PDF notification letter
                CaseDocument assignedToJudgeNotificationLetter =
                    assignedToJudgeDocumentService.generateAssignedToJudgeNotificationLetter(caseDetails, authToken);

                // Send notification letter to Bulk Print
                bulkPrintService.sendNotificationLetterForBulkPrint(assignedToJudgeNotificationLetter, caseDetails);
            }
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
        Map<String, Object> caseData = callbackRequest.getCaseDetails().getData();
        if (isApplicantSolicitorAgreeToReceiveEmails(caseData)) {
            log.info("Sending email notification to Solicitor for 'Consent Order Made'");
            notificationService.sendConsentOrderMadeConfirmationEmail(callbackRequest);
        }
        return ResponseEntity.ok(AboutToStartOrSubmitCallbackResponse.builder().data(caseData).build());
    }

    @PostMapping(value = "/case-orchestration/notify/consent-order-not-approved", consumes = APPLICATION_JSON_VALUE)
    @ApiOperation(value = "send e-mail for Consent order not approved.")
    @ApiResponses(value = {
            @ApiResponse(code = 204, message = "Consent order not approved e-mail sent successfully",
                    response = AboutToStartOrSubmitCallbackResponse.class)})
    public ResponseEntity<AboutToStartOrSubmitCallbackResponse> sendConsentOrderNotApprovedEmail(
            @RequestBody CallbackRequest callbackRequest) {
        log.info("Received request to send email for 'Consent Order Not Approved' for Case ID: {}", callbackRequest.getCaseDetails().getId());
        validateCaseData(callbackRequest);
        Map<String, Object> caseData = callbackRequest.getCaseDetails().getData();
        if (isApplicantSolicitorAgreeToReceiveEmails(caseData)) {
            log.info("Sending email notification to Solicitor for 'Consent Order Not Approved'");
            notificationService.sendConsentOrderNotApprovedEmail(callbackRequest);
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
        Map<String, Object> caseData = callbackRequest.getCaseDetails().getData();
        if (isApplicantSolicitorAgreeToReceiveEmails(caseData)) {
            log.info("Sending email notification to Solicitor for 'Consent Order Available'");
            notificationService.sendConsentOrderAvailableEmail(callbackRequest);
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

        Map<String, Object> caseData = callbackRequest.getCaseDetails().getData();

        if (isApplicantSolicitorResponsibleToDraftOrder(caseData) && isApplicantSolicitorAgreeToReceiveEmails(caseData)) {
            log.info("Sending email notification to Applicant Solicitor for 'Draft Order'");
            notificationService.sendSolicitorToDraftOrderEmail(callbackRequest);
        }
        return ResponseEntity.ok(AboutToStartOrSubmitCallbackResponse.builder().data(caseData).build());
    }
}
