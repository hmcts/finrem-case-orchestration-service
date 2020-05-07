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
import uk.gov.hmcts.reform.finrem.caseorchestration.service.HelpWithFeesDocumentService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.NotificationService;

import java.util.Map;

import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;
import static uk.gov.hmcts.reform.finrem.caseorchestration.OrchestrationConstants.AUTHORIZATION_HEADER;
import static uk.gov.hmcts.reform.finrem.caseorchestration.controllers.BaseController.isConsentedApplication;
import static uk.gov.hmcts.reform.finrem.caseorchestration.service.CommonFunction.isApplicantSolicitorAgreeToReceiveEmails;
import static uk.gov.hmcts.reform.finrem.caseorchestration.service.CommonFunction.isPaperApplication;

@RestController
@Slf4j
@RequiredArgsConstructor
public class NotificationsController implements BaseController {

    private final NotificationService notificationService;
    private final BulkPrintService bulkPrintService;
    private final FeatureToggleService featureToggleService;
    private final AssignedToJudgeDocumentService assignedToJudgeDocumentService;
    private final HelpWithFeesDocumentService helpWithFeesDocumentService;

    @PostMapping(value = "/case-orchestration/notify/hwf-successful", consumes = APPLICATION_JSON_VALUE)
    @ApiOperation(value = "Notify Applicant/Applicant Solicitor of HWF Successful by email or letter.")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "HWFSuccessful notification sent successfully",
                    response = AboutToStartOrSubmitCallbackResponse.class)})
    public ResponseEntity<AboutToStartOrSubmitCallbackResponse> sendHwfSuccessfulConfirmationEmail(
            @RequestHeader(value = AUTHORIZATION_HEADER) String authToken,
            @RequestBody CallbackRequest callbackRequest) {
        log.info("Received request to notify user for Judge successfully assigned to case for Case ID: {}",
            callbackRequest.getCaseDetails().getId());
        validateCaseData(callbackRequest);
        Map<String, Object> caseData = callbackRequest.getCaseDetails().getData();

        if (isConsentedApplication(caseData)) {
            if (isApplicantSolicitorAgreeToReceiveEmails(caseData)) {
                log.info("Sending Consented HWF Successful email notification to Solicitor");
                notificationService.sendConsentedHWFSuccessfulConfirmationEmail(callbackRequest);
            } else if (isPaperApplication(caseData) && featureToggleService.isHwfSuccessfulNotificationLetterEnabled()) {
                log.info("Case is paper application");
                log.info("isHwfSuccessfulNotificationLetterEnabled is toggled on");
                log.info("Sending Consented HWF Successful notification letter for bulk print");

                CaseDetails caseDetails = callbackRequest.getCaseDetails();

                // Generate PDF notification letter
                CaseDocument hwfSuccessfulNotificationLetter =
                    helpWithFeesDocumentService.generateHwfSuccessfulNotificationLetter(caseDetails, authToken);

                // Send notification letter to Bulk Print
                bulkPrintService.sendNotificationLetterForBulkPrint(hwfSuccessfulNotificationLetter, caseDetails);

                // will possibly remove once verified passing in reference works
                cleanupCaseDataBeforeSubmittingToCcd(caseDetails);
            }
        } else if (isApplicantSolicitorAgreeToReceiveEmails(caseData)) {
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

                // will possibly remove once verified passing in reference works
                cleanupCaseDataBeforeSubmittingToCcd(caseDetails);
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

    private void cleanupCaseDataBeforeSubmittingToCcd(CaseDetails caseDetails) {
        // Must remove any added case data as CCD will return an error
        caseDetails.getData().remove("caseNumber");
        caseDetails.getData().remove("reference");
        caseDetails.getData().remove("addressee");
        caseDetails.getData().remove("letterDate");
        caseDetails.getData().remove("applicantName");
        caseDetails.getData().remove("respondentName");
        caseDetails.getData().remove("ctscContactDetails");
    }
}
