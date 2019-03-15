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
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDCallbackResponse;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDRequest;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseData;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.NotificationService;

import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

@RestController
@Slf4j
public class NotificationsController implements BaseController {

    private static final String LOG_MESSAGE = "received notification request for case reference :    ";

    @Autowired
    private NotificationService notificationService;

    @PostMapping(value = "/case-orchestration/notify/hwf-successful", consumes = APPLICATION_JSON_VALUE)
    @ApiOperation(value = "send e-mail for HWF Successful.")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "HWFSuccessful e-mail sent successfully",
                    response = CCDCallbackResponse.class)})
    public ResponseEntity<CCDCallbackResponse> sendHwfSuccessfulConfirmationEmail(
            @RequestBody CCDRequest ccdRequest,
            @RequestHeader(value = "Authorization") String userToken) {
        log.info(LOG_MESSAGE, ccdRequest.getCaseDetails().getCaseId());
        validateCaseData(ccdRequest);
        if (isSolicitorAgreedToReceiveEmails(ccdRequest)) {
            notificationService.sendHWFSuccessfulConfirmationEmail(ccdRequest, userToken);
        }
        CaseData caseData = ccdRequest.getCaseDetails().getCaseData();
        return ResponseEntity.ok(CCDCallbackResponse.builder().data(caseData).build());
    }

    @PostMapping(value = "/case-orchestration/notify/assign-to-judge", consumes = APPLICATION_JSON_VALUE)
    @ApiOperation(value = "send e-mail for Case assigned to Judge Successful.")
    @ApiResponses(value = {
            @ApiResponse(code = 204, message = "Case assigned to Judge e-mail sent successfully",
                    response = CCDCallbackResponse.class)})
    public ResponseEntity<CCDCallbackResponse> sendAssignToJudgeConfirmationEmail(
            @RequestBody CCDRequest ccdRequest,
            @RequestHeader(value = "Authorization") String userToken) {
        log.info(LOG_MESSAGE, ccdRequest.getCaseDetails().getCaseId());
        validateCaseData(ccdRequest);
        if (isSolicitorAgreedToReceiveEmails(ccdRequest)) {
            notificationService.sendAssignToJudgeConfirmationEmail(ccdRequest, userToken);
        }
        CaseData caseData = ccdRequest.getCaseDetails().getCaseData();
        return ResponseEntity.ok(CCDCallbackResponse.builder().data(caseData).build());
    }

    @PostMapping(value = "/case-orchestration/notify/consent-order-made", consumes = APPLICATION_JSON_VALUE)
    @ApiOperation(value = "send e-mail for Consent Order Made.")
    @ApiResponses(value = {
            @ApiResponse(code = 204, message = "Consent order made e-mail sent successfully",
                    response = CCDCallbackResponse.class)})
    public ResponseEntity<CCDCallbackResponse> sendConsentOrderMadeConfirmationEmail(
            @RequestBody CCDRequest ccdRequest,
            @RequestHeader(value = "Authorization") String userToken) {
        log.info(LOG_MESSAGE, ccdRequest.getCaseDetails().getCaseId());
        validateCaseData(ccdRequest);
        if (isSolicitorAgreedToReceiveEmails(ccdRequest)) {
            notificationService.sendConsentOrderMadeConfirmationEmail(ccdRequest, userToken);
        }
        CaseData caseData = ccdRequest.getCaseDetails().getCaseData();
        return ResponseEntity.ok(CCDCallbackResponse.builder().data(caseData).build());
    }

    @PostMapping(value = "/case-orchestration/notify/consent-order-not-approved", consumes = APPLICATION_JSON_VALUE)
    @ApiOperation(value = "send e-mail for Consent order not approved.")
    @ApiResponses(value = {
            @ApiResponse(code = 204, message = "Consent order not approved e-mail sent successfully",
                    response = CCDCallbackResponse.class)})
    public ResponseEntity<CCDCallbackResponse> sendConsentOrderNotApprovedEmail(
            @RequestBody CCDRequest ccdRequest,
            @RequestHeader(value = "Authorization") String userToken) {
        log.info(LOG_MESSAGE, ccdRequest.getCaseDetails().getCaseId());
        validateCaseData(ccdRequest);
        if (isSolicitorAgreedToReceiveEmails(ccdRequest)) {
            notificationService.sendConsentOrderNotApprovedEmail(ccdRequest, userToken);
        }
        CaseData caseData = ccdRequest.getCaseDetails().getCaseData();
        return ResponseEntity.ok(CCDCallbackResponse.builder().data(caseData).build());
    }

    @PostMapping(value = "/case-orchestration/notify/consent-order-available", consumes = APPLICATION_JSON_VALUE)
    @ApiOperation(value = "send e-mail for Consent order available.")
    @ApiResponses(value = {
            @ApiResponse(code = 204, message = "Consent order available e-mail sent successfully",
                    response = CCDCallbackResponse.class)})
    public ResponseEntity<CCDCallbackResponse> sendConsentOrderAvailableEmail(
            @RequestBody CCDRequest ccdRequest,
            @RequestHeader(value = "Authorization") String userToken) {
        log.info(LOG_MESSAGE, ccdRequest.getCaseDetails().getCaseId());
        validateCaseData(ccdRequest);
        if (isSolicitorAgreedToReceiveEmails(ccdRequest)) {
            notificationService.sendConsentOrderAvailableEmail(ccdRequest, userToken);
        }
        CaseData caseData = ccdRequest.getCaseDetails().getCaseData();
        return ResponseEntity.ok(CCDCallbackResponse.builder().data(caseData).build());
    }

    private boolean isSolicitorAgreedToReceiveEmails(CCDRequest ccdRequest) {
        return "Yes".equalsIgnoreCase(ccdRequest.getCaseDetails().getCaseData().getSolicitorAgreeToReceiveEmails());
    }
}
