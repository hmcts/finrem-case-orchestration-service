package uk.gov.hmcts.reform.finrem.caseorchestration.controllers;

import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RestController;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.ccd.client.model.CallbackRequest;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.NotificationService;

import java.util.Map;
import java.util.Objects;

import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;
import static uk.gov.hmcts.reform.finrem.caseorchestration.OrchestrationConstants.AUTHORIZATION_HEADER;
import static uk.gov.hmcts.reform.finrem.caseorchestration.OrchestrationConstants.YES_VALUE;
import static uk.gov.hmcts.reform.finrem.caseorchestration.controllers.BaseController.isConsentedApplication;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.APPLICANT_SOLICITOR_CONSENTS_FOR_EMAILS;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.SOLICITOR_AGREE_TO_RECEIVE_EMAILS;

@RestController
@Slf4j
public class NotificationsController implements BaseController {

    @Autowired
    private NotificationService notificationService;

    @PostMapping(value = "/case-orchestration/notify/hwf-successful", consumes = APPLICATION_JSON_VALUE)
    @ApiOperation(value = "send e-mail for HWF Successful.")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "HWFSuccessful e-mail sent successfully",
                    response = AboutToStartOrSubmitCallbackResponse.class)})
    public ResponseEntity<AboutToStartOrSubmitCallbackResponse> sendHwfSuccessfulConfirmationEmail(
            @RequestBody CallbackRequest callbackRequest,
            @RequestHeader(value = AUTHORIZATION_HEADER) String authToken) {
        log.info("Received request to send email for HWF Successful for Case ID: {}", callbackRequest.getCaseDetails().getId());
        validateCaseData(callbackRequest);
        Map<String, Object> caseData = callbackRequest.getCaseDetails().getData();
        if (isConsentedApplication(caseData)) {
            if (isSolicitorAgreedToReceiveEmails(caseData, SOLICITOR_AGREE_TO_RECEIVE_EMAILS)) {
                log.info("Sending Consented HWF Successful email notification to Solicitor");
                notificationService.sendHWFSuccessfulConfirmationEmail(callbackRequest);
            }
        } else if (isSolicitorAgreedToReceiveEmails(caseData, APPLICANT_SOLICITOR_CONSENTS_FOR_EMAILS)) {
            log.info("Sending Contested HWF Successful email notification to Solicitor");
            notificationService.sendContestedHwfSuccessfulConfirmationEmail(callbackRequest);
        }
        return ResponseEntity.ok(AboutToStartOrSubmitCallbackResponse.builder().data(caseData).build());
    }

    @PostMapping(value = "/case-orchestration/notify/assign-to-judge", consumes = APPLICATION_JSON_VALUE)
    @ApiOperation(value = "send e-mail for case assigned to Judge Successful.")
    @ApiResponses(value = {
            @ApiResponse(code = 204, message = "Case assigned to Judge e-mail sent successfully",
                    response = AboutToStartOrSubmitCallbackResponse.class)})
    public ResponseEntity<AboutToStartOrSubmitCallbackResponse> sendAssignToJudgeConfirmationEmail(
            @RequestBody CallbackRequest callbackRequest) {
        log.info("Received request to send email for Judge successfully assigned to case for Case ID: {}", callbackRequest.getCaseDetails().getId());
        validateCaseData(callbackRequest);
        Map<String, Object> caseData = callbackRequest.getCaseDetails().getData();
        if (isSolicitorAgreedToReceiveEmails(caseData, SOLICITOR_AGREE_TO_RECEIVE_EMAILS)) {
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
