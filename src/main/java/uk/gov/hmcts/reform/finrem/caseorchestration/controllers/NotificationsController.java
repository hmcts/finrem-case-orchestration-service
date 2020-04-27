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
import uk.gov.hmcts.reform.finrem.caseorchestration.service.BulkPrintService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.FeatureToggleService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.NotificationService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.bulkprint.AssignedToJudgeBulkPrintService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.bulkprint.HelpWithFeesBulkPrintService;

import java.util.Map;
import java.util.Objects;
import java.util.UUID;

import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;
import static uk.gov.hmcts.reform.finrem.caseorchestration.OrchestrationConstants.AUTHORIZATION_HEADER;
import static uk.gov.hmcts.reform.finrem.caseorchestration.OrchestrationConstants.YES_VALUE;
import static uk.gov.hmcts.reform.finrem.caseorchestration.controllers.BaseController.isConsentedApplication;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.APPLICANT_SOL_CONSENT_FOR_EMAILS;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.ASSIGNED_TO_JUDGE_NOTIFICATION_LETTER;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.ASSIGNED_TO_JUDGE_NOTIFICATION_LETTER_ID;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.HWF_SUCCESS_NOTIFICATION_LETTER;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.HWF_SUCCESS_NOTIFICATION_LETTER_ID;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.SOLICITOR_AGREE_TO_RECEIVE_EMAILS;
import static uk.gov.hmcts.reform.finrem.caseorchestration.service.CommonFunction.isPaperApplication;

@RestController
@Slf4j
@RequiredArgsConstructor
public class NotificationsController implements BaseController {

    private final NotificationService notificationService;
    private final BulkPrintService bulkPrintService;
    private final HelpWithFeesBulkPrintService helpWithFeesBulkPrintService;
    private final AssignedToJudgeBulkPrintService assignedToJudgeBulkPrintService;
    private final FeatureToggleService featureToggleService;

    @PostMapping(value = "/case-orchestration/notify/hwf-successful", consumes = APPLICATION_JSON_VALUE)
    @ApiOperation(value = "send e-mail for HWF Successful.")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "HWFSuccessful e-mail sent successfully",
                    response = AboutToStartOrSubmitCallbackResponse.class)})
    public ResponseEntity<AboutToStartOrSubmitCallbackResponse> sendHwfSuccessfulConfirmationEmail(
        @RequestHeader(value = AUTHORIZATION_HEADER) String authToken,
        @RequestBody CallbackRequest callbackRequest) {
        log.info("Received request to send email for HWF Successful for Case ID: {}", callbackRequest.getCaseDetails().getId());
        validateCaseData(callbackRequest);
        CaseDetails caseDetails = callbackRequest.getCaseDetails();
        Map<String, Object> caseData = caseDetails.getData();
        if (isConsentedApplication(caseData)) {
            if (isSolicitorAgreedToReceiveEmails(caseData, SOLICITOR_AGREE_TO_RECEIVE_EMAILS)) {

                log.info("Sending Consented HWF Successful email notification to Solicitor");
                notificationService.sendHWFSuccessfulConfirmationEmail(callbackRequest);
            // TODO: Remove this when we're ready to release Bulk Print
            // TODO: Does this need wrapped in paperApplication logic?
            } else if (isPaperApplication(caseData) && featureToggleService.isHwfSuccessfulNotificationLetterEnabled()) {

                log.info("Sending Consented HWF Successful notification bulk print letter");
                CaseDocument hwfSuccessfulLetter =
                    helpWithFeesBulkPrintService.generateHwfSuccessfulLetter(authToken, caseDetails);
                UUID hwfSuccessfulLetterId = bulkPrintService.sendNotificationLetterForBulkPrint(hwfSuccessfulLetter, caseDetails);

                log.info("Generated 'HWF Successful' letter bulk print. letter: {}, letterId : {}",
                    hwfSuccessfulLetter, hwfSuccessfulLetterId);

                // Need to create CCD fields for hwfSuccessfulLetter document and ID

                caseData.put(HWF_SUCCESS_NOTIFICATION_LETTER, hwfSuccessfulLetter);
                caseData.put(HWF_SUCCESS_NOTIFICATION_LETTER_ID, hwfSuccessfulLetterId);

                log.info("Bulk print is successful for 'Judge assigned to case' notification letter");
            }
        } else if (isSolicitorAgreedToReceiveEmails(caseData, APPLICANT_SOL_CONSENT_FOR_EMAILS)) {
            log.info("Sending Contested HWF Successful email notification to Solicitor");
            notificationService.sendContestedHwfSuccessfulConfirmationEmail(callbackRequest);
        }
        return ResponseEntity.ok(AboutToStartOrSubmitCallbackResponse.builder().data(caseData).build());
    }

    @PostMapping(value = "/case-orchestration/notify/assign-to-judge", consumes = APPLICATION_JSON_VALUE)
    @ApiOperation(value = "Notify user by e-mail or letter when Judge is assigned to the case.")
    @ApiResponses(value = {
            @ApiResponse(code = 204, message = "Case assigned to Judge e-mail sent successfully",
                    response = AboutToStartOrSubmitCallbackResponse.class)})
    public ResponseEntity<AboutToStartOrSubmitCallbackResponse> sendAssignToJudgeConfirmationEmail(
        @RequestHeader(value = AUTHORIZATION_HEADER) String authToken,
        @RequestBody CallbackRequest callbackRequest) {
        CaseDetails caseDetails = callbackRequest.getCaseDetails();
        log.info("Received request to notify user that Judge was successfully assigned to case for Case ID: {}",
            caseDetails.getId());
        validateCaseData(callbackRequest);
        Map<String, Object> caseData = caseDetails.getData();
        if (isSolicitorAgreedToReceiveEmails(caseData, SOLICITOR_AGREE_TO_RECEIVE_EMAILS)) {
            log.info("Sending email notification to Solicitor for Judge assigned to case");
            notificationService.sendAssignToJudgeConfirmationEmail(callbackRequest);
            // TODO: Remove this when we're ready to release Bulk Print
            // TODO: Does this need wrapped in paperApplication logic?
        } else if (isConsentedApplication(caseData) && isPaperApplication(caseData)) {

            if (featureToggleService.isAssignedToJudgeNotificationLetterEnabled()) {
                log.info("Sending 'Judge assigned to case' letter to bulk print");

                CaseDocument judgeAssignedToCaseLetter =
                    assignedToJudgeBulkPrintService.generateJudgeAssignedToCaseLetter(authToken, caseDetails);
                UUID judgeAssignedToCaseLetterId =
                    bulkPrintService.sendNotificationLetterForBulkPrint(judgeAssignedToCaseLetter, caseDetails);

                log.info("Generated Judge assigned to case letter bulk print. letter: {}, letterId : {}",
                    judgeAssignedToCaseLetter, judgeAssignedToCaseLetterId);

                // Need to create CCD fields for assignedToJudge Letter document and ID
                caseData.put(ASSIGNED_TO_JUDGE_NOTIFICATION_LETTER, judgeAssignedToCaseLetter);
                caseData.put(ASSIGNED_TO_JUDGE_NOTIFICATION_LETTER_ID, judgeAssignedToCaseLetterId);

                log.info("Bulk print is successful for 'Judge assigned to case' notification letter");
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
        if (isSolicitorAgreedToReceiveEmails(caseData, SOLICITOR_AGREE_TO_RECEIVE_EMAILS)) {
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
        if (isSolicitorAgreedToReceiveEmails(caseData, SOLICITOR_AGREE_TO_RECEIVE_EMAILS)) {
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
        if (isSolicitorAgreedToReceiveEmails(caseData, SOLICITOR_AGREE_TO_RECEIVE_EMAILS)) {
            log.info("Sending email notification to Solicitor for 'Consent Order Available'");
            notificationService.sendConsentOrderAvailableEmail(callbackRequest);
        }
        return ResponseEntity.ok(AboutToStartOrSubmitCallbackResponse.builder().data(caseData).build());
    }

    private boolean isSolicitorAgreedToReceiveEmails(Map<String, Object> mapOfCaseData, String solicitorAgreeToReceiveEmails) {
        return YES_VALUE.equalsIgnoreCase(Objects.toString(mapOfCaseData.get(solicitorAgreeToReceiveEmails)));
    }
}
