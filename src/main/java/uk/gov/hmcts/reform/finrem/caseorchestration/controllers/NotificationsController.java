package uk.gov.hmcts.reform.finrem.caseorchestration.controllers;

import com.fasterxml.jackson.core.JsonProcessingException;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.ccd.client.model.CallbackRequest;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.ccd.client.model.SubmittedCallbackResponse;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.CaseDataService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.FeatureToggleService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.GeneralEmailService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.NotificationService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.PaperNotificationService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.TransferCourtService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.correspondence.hwf.HwfCorrespondenceService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.correspondence.updatefrc.UpdateFrcCorrespondenceService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.noc.NocLetterNotificationService;

import java.io.IOException;
import java.util.Map;
import java.util.Optional;

import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;
import static uk.gov.hmcts.reform.finrem.caseorchestration.OrchestrationConstants.AUTHORIZATION_HEADER;
import static uk.gov.hmcts.reform.finrem.caseorchestration.OrchestrationConstants.YES_VALUE;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.INCLUDES_REPRESENTATIVE_UPDATE;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.IS_NOC_REJECTED;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.NOC_PARTY;

@RestController
@Slf4j
@RequestMapping(value = "/case-orchestration/notify")
@RequiredArgsConstructor
public class NotificationsController extends BaseController {

    private final NotificationService notificationService;
    private final PaperNotificationService paperNotificationService;
    private final GeneralEmailService generalEmailService;
    private final CaseDataService caseDataService;
    private final TransferCourtService transferCourtService;
    private final FeatureToggleService featureToggleService;
    private final NocLetterNotificationService nocLetterNotificationService;
    private final HwfCorrespondenceService hwfNotificationsService;
    private final UpdateFrcCorrespondenceService updateFrcCorrespondenceService;


    @PostMapping(value = "/hwf-successful", consumes = APPLICATION_JSON_VALUE)
    @Operation(summary = "Notify Applicant/Applicant Solicitor of HWF Successful by email or letter.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200",
            description = "HWFSuccessful notification sent successfully",
            content = {@Content(mediaType = "application/json", schema = @Schema(implementation = AboutToStartOrSubmitCallbackResponse.class))})})
    public ResponseEntity<AboutToStartOrSubmitCallbackResponse> sendHwfSuccessfulConfirmationNotification(
        @RequestHeader(value = AUTHORIZATION_HEADER) String authToken,
        @RequestBody CallbackRequest callbackRequest) {

        log.info("Received request to send email for HWF Successful for Case ID: {}", callbackRequest.getCaseDetails().getId());
        validateCaseData(callbackRequest);
        CaseDetails caseDetails = callbackRequest.getCaseDetails();
        Map<String, Object> caseData = caseDetails.getData();

        hwfNotificationsService.sendCorrespondence(caseDetails, authToken);
        return ResponseEntity.ok(AboutToStartOrSubmitCallbackResponse.builder().data(caseData).build());
    }

    @PostMapping(value = "/assign-to-judge", consumes = APPLICATION_JSON_VALUE)
    @Operation(summary = "Notify solicitor when Judge assigned to case via email or letter")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200",
            description = "Case assigned to Judge notification sent successfully",
            content = {@Content(mediaType = "application/json", schema = @Schema(implementation = AboutToStartOrSubmitCallbackResponse.class))})})
    public ResponseEntity<AboutToStartOrSubmitCallbackResponse> sendAssignToJudgeConfirmationNotification(
        @RequestHeader(value = AUTHORIZATION_HEADER) String authToken,
        @RequestBody CallbackRequest callbackRequest) {

        log.info("Received request to notify solicitor for Judge successfully assigned to case for Case ID: {}",
            callbackRequest.getCaseDetails().getId());
        validateCaseData(callbackRequest);
        CaseDetails caseDetails = callbackRequest.getCaseDetails();
        Map<String, Object> caseData = caseDetails.getData();

        paperNotificationService.printAssignToJudgeNotification(caseDetails, authToken);

        if (!caseDataService.isPaperApplication(caseData)
            && caseDataService.isApplicantSolicitorAgreeToReceiveEmails(caseDetails)) {
            log.info("Sending email notification to Applicant Solicitor for Judge successfully assigned to case");
            notificationService.sendAssignToJudgeConfirmationEmailToApplicantSolicitor(caseDetails);
        }

        if (notificationService.isRespondentSolicitorEmailCommunicationEnabled(caseData)) {
            log.info("Sending email notification to Respondent Solicitor for Judge successfully assigned to case");
            notificationService.sendAssignToJudgeConfirmationEmailToRespondentSolicitor(caseDetails);
        }

        return ResponseEntity.ok(AboutToStartOrSubmitCallbackResponse.builder().data(caseData).build());
    }

    @PostMapping(value = "/assign-to-judge-consent-in-contested", consumes = APPLICATION_JSON_VALUE)
    @Operation(summary = "Notify applicant and respondent when Judge assigned to case via letter")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200",
            description = "Case assigned to Judge notification sent successfully",
            content = {@Content(mediaType = "application/json", schema = @Schema(implementation = AboutToStartOrSubmitCallbackResponse.class))})})
    public ResponseEntity<AboutToStartOrSubmitCallbackResponse> sendConsentInContestedAssignToJudgeConfirmationPaperNotification(
        @RequestHeader(value = AUTHORIZATION_HEADER) String authToken,
        @RequestBody CallbackRequest callbackRequest) {

        log.info("Received request to notify applicant and respondent for Judge successfully assigned to case for Case ID: {}",
            callbackRequest.getCaseDetails().getId());
        validateCaseData(callbackRequest);
        CaseDetails caseDetails = callbackRequest.getCaseDetails();

        paperNotificationService.printConsentInContestedAssignToJudgeConfirmationNotification(caseDetails, authToken);

        return ResponseEntity.ok(AboutToStartOrSubmitCallbackResponse.builder().data(caseDetails.getData()).build());
    }

    @PostMapping(value = "/consent-order-made", consumes = APPLICATION_JSON_VALUE)
    @Operation(summary = "send e-mail for Consent Order Made.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200",
            description = "Consent order made e-mail sent successfully",
            content = {@Content(mediaType = "application/json", schema = @Schema(implementation = AboutToStartOrSubmitCallbackResponse.class))})})
    public ResponseEntity<AboutToStartOrSubmitCallbackResponse> sendConsentOrderMadeConfirmationEmail(
        @RequestBody CallbackRequest callbackRequest) {

        log.info("Received request to send email for 'Consent Order Made' for Case ID: {}", callbackRequest.getCaseDetails().getId());
        validateCaseData(callbackRequest);
        CaseDetails caseDetails = callbackRequest.getCaseDetails();
        Map<String, Object> caseData = caseDetails.getData();

        if (caseDataService.isApplicantSolicitorAgreeToReceiveEmails(caseDetails)
            && caseDataService.isConsentedApplication(caseDetails)) {
            log.info("Sending email notification to Applicant Solicitor for 'Consent Order Made'");
            notificationService.sendConsentOrderMadeConfirmationEmailToApplicantSolicitor(caseDetails);
        }

        if (notificationService.isRespondentSolicitorEmailCommunicationEnabled(caseData)) {
            log.info("Sending email notification to Respondent Solicitor for 'Consent Order Made'");
            notificationService.sendConsentOrderMadeConfirmationEmailToRespondentSolicitor(caseDetails);
        }

        return ResponseEntity.ok(AboutToStartOrSubmitCallbackResponse.builder().data(caseData).build());
    }

    @PostMapping(value = "/order-not-approved", consumes = APPLICATION_JSON_VALUE)
    @Operation(summary = "send e-mail for consent/contest order not approved.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200",
            description = "Consent/Contest order not approved e-mail sent successfully",
            content = {@Content(mediaType = "application/json", schema = @Schema(implementation = AboutToStartOrSubmitCallbackResponse.class))})})
    public ResponseEntity<AboutToStartOrSubmitCallbackResponse> sendConsentOrderNotApprovedEmail(
        @RequestBody CallbackRequest callbackRequest) {
        log.info("Received request to send email for 'Consent/Contest Order Not Approved' for Case ID: {}", callbackRequest.getCaseDetails().getId());

        validateCaseData(callbackRequest);

        CaseDetails caseDetails = callbackRequest.getCaseDetails();
        if (caseDataService.isApplicantSolicitorAgreeToReceiveEmails(caseDetails)) {
            if (caseDataService.isConsentedApplication(caseDetails)) {
                log.info("Sending email notification to Applicant Solicitor for 'Consent Order Not Approved'");
                notificationService.sendConsentOrderNotApprovedEmailToApplicantSolicitor(caseDetails);
            } else {
                log.info("Sending email notification to Applicant Solicitor for 'Contest Order Not Approved'");
                notificationService.sendContestOrderNotApprovedEmailApplicant(caseDetails);
            }
        }

        Map<String, Object> caseData = caseDetails.getData();
        if (notificationService.isRespondentSolicitorEmailCommunicationEnabled(caseData)) {
            if (caseDataService.isConsentedApplication(caseDetails)) {
                log.info("Sending email notification to Respondent Solicitor for 'Consent Order Not Approved'");
                notificationService.sendConsentOrderNotApprovedEmailToRespondentSolicitor(caseDetails);
            } else {
                log.info("Sending email notification to Respondent Solicitor for 'Contest Order Not Approved'");
                notificationService.sendContestOrderNotApprovedEmailRespondent(caseDetails);
            }
        }

        return ResponseEntity.ok(AboutToStartOrSubmitCallbackResponse.builder().data(caseData).build());
    }

    @PostMapping(value = "/contested-consent-order-approved", consumes = APPLICATION_JSON_VALUE)
    @Operation(summary = "send e-mail for contested consent order approved.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200",
            description = "Contested consent order approved e-mail sent successfully",
            content = {@Content(mediaType = "application/json", schema = @Schema(implementation = AboutToStartOrSubmitCallbackResponse.class))})})
    public ResponseEntity<AboutToStartOrSubmitCallbackResponse> sendContestedConsentOrderApprovedEmail(
        @RequestBody CallbackRequest callbackRequest) {

        log.info("Received request to send email for 'Contested Consent Order Approved' for Case ID: {}", callbackRequest.getCaseDetails().getId());
        validateCaseData(callbackRequest);
        CaseDetails caseDetails = callbackRequest.getCaseDetails();
        Map<String, Object> caseData = caseDetails.getData();

        if (caseDataService.isApplicantSolicitorAgreeToReceiveEmails(caseDetails)) {
            log.info("Sending email notification to Applicant Solicitor for 'Contested Consent Order Approved'");
            notificationService.sendContestedConsentOrderApprovedEmailToApplicantSolicitor(caseDetails);
        }

        if (notificationService.isRespondentSolicitorEmailCommunicationEnabled(caseData)) {
            log.info("Sending email notification to Respondent Solicitor for 'Contested Consent Order Approved'");
            notificationService.sendContestedConsentOrderApprovedEmailToRespondentSolicitor(caseDetails);
        }

        return ResponseEntity.ok(AboutToStartOrSubmitCallbackResponse.builder().data(caseData).build());
    }

    @PostMapping(value = "/contested-consent-order-not-approved", consumes = APPLICATION_JSON_VALUE)
    @Operation(summary = "send e-mail for contested consent order not approved.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200",
            description = "Contested consent order not approved e-mail sent successfully",
            content = {@Content(mediaType = "application/json", schema = @Schema(implementation = AboutToStartOrSubmitCallbackResponse.class))})})
    public ResponseEntity<AboutToStartOrSubmitCallbackResponse> sendContestedConsentOrderNotApprovedEmail(
        @RequestBody CallbackRequest callbackRequest) {
        validateCaseData(callbackRequest);
        CaseDetails caseDetails = callbackRequest.getCaseDetails();
        Map<String, Object> caseData = caseDetails.getData();

        if (!caseDataService.isPaperApplication(caseData)
            && caseDataService.isApplicantSolicitorAgreeToReceiveEmails(caseDetails)) {
            log.info("Sending email for 'Contested Consent Order Not Approved' to Applicant Solicitor for Case ID: {}", caseDetails.getId());
            notificationService.sendContestedConsentOrderNotApprovedEmailApplicantSolicitor(caseDetails);
        }

        if (notificationService.isRespondentSolicitorEmailCommunicationEnabled(caseData)) {
            log.info("Sending email for 'Contested Consent Order Not Approved' to Respondent Solicitor for Case ID: {}", caseDetails.getId());
            notificationService.sendContestedConsentOrderNotApprovedEmailRespondentSolicitor(caseDetails);
        }

        return ResponseEntity.ok(AboutToStartOrSubmitCallbackResponse.builder().data(caseData).build());
    }

    @PostMapping(value = "/general-order-raised", consumes = APPLICATION_JSON_VALUE)
    @Operation(summary = "send e-mail for general order raised.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200",
            description = "General order raised e-mail sent successfully",
            content = {@Content(mediaType = "application/json", schema = @Schema(implementation = AboutToStartOrSubmitCallbackResponse.class))})})
    public ResponseEntity<AboutToStartOrSubmitCallbackResponse> sendGeneralOrderRaisedEmail(
        @RequestBody CallbackRequest callbackRequest) {

        log.info("Received request to send email for General Order raised for Case ID: {}", callbackRequest.getCaseDetails().getId());
        validateCaseData(callbackRequest);
        CaseDetails caseDetails = callbackRequest.getCaseDetails();

        if (caseDataService.isApplicantSolicitorAgreeToReceiveEmails(caseDetails)) {
            if (caseDataService.isConsentedApplication(caseDetails)) {
                log.info("Sending email notification to applicant Solicitor for 'Consented General Order'");
                notificationService.sendConsentedGeneralOrderEmailToApplicantSolicitor(caseDetails);
            } else {
                if (caseDataService.isConsentedInContestedCase(caseDetails)) {
                    log.info("Sending email notification to applicant Solicitor for 'Contested consent General Order'");
                    notificationService.sendContestedConsentGeneralOrderEmailApplicantSolicitor(caseDetails);
                } else {
                    log.info("Sending email notification to applicant solicitor for 'Contested General Order'");
                    notificationService.sendContestedGeneralOrderEmailApplicant(caseDetails);
                }
            }
        }

        Map<String, Object> caseData = caseDetails.getData();
        if (notificationService.isRespondentSolicitorEmailCommunicationEnabled(caseData)) {
            if (caseDataService.isConsentedApplication(caseDetails)) {
                log.info("Sending email notification to respondent Solicitor for 'Consented General Order'");
                notificationService.sendConsentedGeneralOrderEmailToRespondentSolicitor(caseDetails);
            } else {
                if (caseDataService.isConsentedInContestedCase(caseDetails)) {
                    log.info("Sending email notification to respondent Solicitor for 'Contested consent General Order'");
                    notificationService.sendContestedConsentGeneralOrderEmailRespondentSolicitor(caseDetails);
                } else {
                    log.info("Sending email notification to respondent solicitor for 'Contested General Order'");
                    notificationService.sendContestedGeneralOrderEmailRespondent(caseDetails);
                }
            }
        }

        return ResponseEntity.ok(AboutToStartOrSubmitCallbackResponse.builder().data(caseData).build());
    }

    @PostMapping(value = "/consent-order-available", consumes = APPLICATION_JSON_VALUE)
    @Operation(summary = "send e-mail for Consent order available.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200",
            description = "Consent order available e-mail sent successfully",
            content = {@Content(mediaType = "application/json", schema = @Schema(implementation = AboutToStartOrSubmitCallbackResponse.class))})})
    public ResponseEntity<AboutToStartOrSubmitCallbackResponse> sendConsentOrderAvailableEmail(
        @RequestBody CallbackRequest callbackRequest) {

        log.info("Received request to send email for 'Consent Order Available' for Case ID: {}", callbackRequest.getCaseDetails().getId());
        validateCaseData(callbackRequest);
        CaseDetails caseDetails = callbackRequest.getCaseDetails();
        Map<String, Object> caseData = caseDetails.getData();

        if (caseDataService.isApplicantSolicitorAgreeToReceiveEmails(caseDetails)) {
            log.info("Sending email notification to Applicant Solicitor for 'Consent Order Available'");
            notificationService.sendConsentOrderAvailableEmailToApplicantSolicitor(caseDetails);
        }

        if (notificationService.isRespondentSolicitorEmailCommunicationEnabled(caseData)) {
            log.info("Sending email notification to Respondent Solicitor for 'Consent Order Available'");
            notificationService.sendConsentOrderAvailableEmailToRespondentSolicitor(caseDetails);
        }

        return ResponseEntity.ok(AboutToStartOrSubmitCallbackResponse.builder().data(caseData).build());
    }

    @PostMapping(value = "/prepare-for-hearing", consumes = APPLICATION_JSON_VALUE)
    @Operation(summary = "send e-mail for 'Prepare for Hearing'.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "204",
            description = "'Prepare for Hearing' e-mail sent successfully",
            content = {@Content(mediaType = "application/json", schema = @Schema(implementation = AboutToStartOrSubmitCallbackResponse.class))})})
    public ResponseEntity<SubmittedCallbackResponse> sendPrepareForHearingEmail(
        @RequestHeader(value = AUTHORIZATION_HEADER) String authorisationToken,
        @RequestBody CallbackRequest callbackRequest) {

        log.info("Received request for 'Prepare for Hearing' for Case ID: {}", callbackRequest.getCaseDetails().getId());
        validateCaseData(callbackRequest);
        CaseDetails caseDetails = callbackRequest.getCaseDetails();

        if (notificationService.isApplicantSolicitorDigitalAndEmailPopulated(caseDetails)) {
            log.info("Sending email notification to Applicant Solicitor for 'Prepare for Hearing' for Case ID: {}",
                callbackRequest.getCaseDetails().getId());
            notificationService.sendPrepareForHearingEmailApplicant(caseDetails);
        }
        if (notificationService.isRespondentSolicitorRegisteredAndEmailCommunicationEnabled(caseDetails)) {
            log.info("Sending email notification to Respondent Solicitor for 'Prepare for Hearing' for Case ID: {}",
                callbackRequest.getCaseDetails().getId());
            notificationService.sendPrepareForHearingEmailRespondent(caseDetails);
        }

        return ResponseEntity.ok(SubmittedCallbackResponse.builder().build());
    }

    @PostMapping(value = "/prepare-for-hearing-order-sent", consumes = APPLICATION_JSON_VALUE)
    @Operation(summary = "send e-mail for 'Prepare for Hearing (after order sent)'.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200",
            description = "'Prepare for Hearing (after send order)' e-mail sent successfully",
            content = {@Content(mediaType = "application/json", schema = @Schema(implementation = AboutToStartOrSubmitCallbackResponse.class))})})
    public ResponseEntity<AboutToStartOrSubmitCallbackResponse> sendPrepareForHearingOrderSentEmail(
        @RequestBody CallbackRequest callbackRequest) {

        log.info("Received request to send email for 'Prepare for Hearing (after order sent)' for Case ID: {}",
            callbackRequest.getCaseDetails().getId());
        validateCaseData(callbackRequest);
        CaseDetails caseDetails = callbackRequest.getCaseDetails();
        Map<String, Object> caseData = caseDetails.getData();

        if (caseDataService.isApplicantSolicitorAgreeToReceiveEmails(caseDetails)) {
            log.info("Sending email notification to Applicant Solicitor for 'Prepare for Hearing (after send order)'");
            notificationService.sendPrepareForHearingOrderSentEmailApplicant(caseDetails);
        }

        if (notificationService.isRespondentSolicitorEmailCommunicationEnabled(caseData)) {
            log.info("Sending email notification to Respondent Solicitor for 'Prepare for Hearing (after send order)'");
            notificationService.sendPrepareForHearingOrderSentEmailRespondent(caseDetails);
        }

        return ResponseEntity.ok(AboutToStartOrSubmitCallbackResponse.builder().data(caseData).build());
    }

    @PostMapping(value = "/contest-application-issued", consumes = APPLICATION_JSON_VALUE)
    @Operation(summary = "send e-mail for Contested 'Application Issued'.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200",
            description = "Consent order available e-mail sent successfully",
            content = {@Content(mediaType = "application/json", schema = @Schema(implementation = AboutToStartOrSubmitCallbackResponse.class))})})
    public ResponseEntity<AboutToStartOrSubmitCallbackResponse> sendContestedApplicationIssuedEmail(
        @RequestBody CallbackRequest callbackRequest) {
        log.info("Received request to send email for Contested 'Application Issued' for Case ID: {}", callbackRequest.getCaseDetails().getId());
        validateCaseData(callbackRequest);
        CaseDetails caseDetails = callbackRequest.getCaseDetails();

        if (caseDataService.isApplicantSolicitorAgreeToReceiveEmails(caseDetails)) {
            log.info("Sending Contested 'Application Issued' email notification to Applicant Solicitor");
            notificationService.sendContestedApplicationIssuedEmailToApplicantSolicitor(caseDetails);
        }

        Map<String, Object> caseData = caseDetails.getData();
        return ResponseEntity.ok(AboutToStartOrSubmitCallbackResponse.builder().data(caseData).build());
    }

    @PostMapping(value = "/draft-order", consumes = APPLICATION_JSON_VALUE)
    @Operation(summary = "send e-mail for Solicitor To Draft Order")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200",
            description = "Draft Order e-mail sent successfully",
            content = {@Content(mediaType = "application/json", schema = @Schema(implementation = AboutToStartOrSubmitCallbackResponse.class))})})
    public ResponseEntity<AboutToStartOrSubmitCallbackResponse> sendDraftOrderEmail(
        @RequestBody CallbackRequest callbackRequest) {

        log.info("Received request to send email for 'Solicitor To Draft Order' for Case ID: {}", callbackRequest.getCaseDetails().getId());
        validateCaseData(callbackRequest);
        CaseDetails caseDetails = callbackRequest.getCaseDetails();
        Map<String, Object> caseData = caseDetails.getData();

        if (caseDataService.isApplicantSolicitorResponsibleToDraftOrder(caseData)
            && caseDataService.isApplicantSolicitorAgreeToReceiveEmails(caseDetails)) {
            log.info("Sending email notification to Applicant Solicitor for 'Draft Order'");
            notificationService.sendSolicitorToDraftOrderEmailApplicant(caseDetails);
        }

        if (caseDataService.isRespondentSolicitorResponsibleToDraftOrder(caseData)
            && notificationService.isRespondentSolicitorEmailCommunicationEnabled(caseData)) {
            log.info("Sending email notification to Respondent Solicitor for 'Draft Order'");
            notificationService.sendSolicitorToDraftOrderEmailRespondent(caseDetails);
        }

        return ResponseEntity.ok(AboutToStartOrSubmitCallbackResponse.builder().data(caseData).build());
    }

    @PostMapping(value = "/general-email", consumes = APPLICATION_JSON_VALUE)
    @Operation(summary = "send a general email")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200",
            description = "General e-mail sent successfully",
            content = {@Content(mediaType = "application/json", schema = @Schema(implementation = AboutToStartOrSubmitCallbackResponse.class))})})
    public ResponseEntity<AboutToStartOrSubmitCallbackResponse> sendGeneralEmail(
        @RequestBody CallbackRequest callbackRequest) {

        log.info("Received request to send general email for Case ID: {}", callbackRequest.getCaseDetails().getId());
        validateCaseData(callbackRequest);

        log.info("Sending general email notification");
        CaseDetails caseDetails = callbackRequest.getCaseDetails();
        if (caseDataService.isConsentedApplication(caseDetails)) {
            notificationService.sendConsentGeneralEmail(caseDetails);
        } else {
            notificationService.sendContestedGeneralEmail(caseDetails);
        }

        generalEmailService.storeGeneralEmail(caseDetails);

        return ResponseEntity.ok(AboutToStartOrSubmitCallbackResponse.builder().data(caseDetails.getData()).build());
    }

    @PostMapping(value = "/manual-payment", consumes = APPLICATION_JSON_VALUE)
    @Operation(summary = "send a manual payment letter")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200",
            description = "Manual Payment letter sent successfully",
            content = {@Content(mediaType = "application/json", schema = @Schema(implementation = AboutToStartOrSubmitCallbackResponse.class))})})
    public ResponseEntity<AboutToStartOrSubmitCallbackResponse> sendManualPaymentPaperNotification(
        @RequestHeader(value = AUTHORIZATION_HEADER) String authToken,
        @RequestBody CallbackRequest callbackRequest) {
        CaseDetails caseDetails = callbackRequest.getCaseDetails();
        log.info("Received request to send Manual Payment Letter for Case ID: {}", caseDetails.getId());
        validateCaseData(callbackRequest);

        paperNotificationService.printManualPaymentNotification(caseDetails, authToken);

        return ResponseEntity.ok(AboutToStartOrSubmitCallbackResponse.builder().data(caseDetails.getData()).build());
    }

    @PostMapping(value = "/general-application-refer-to-judge", consumes = APPLICATION_JSON_VALUE)
    @Operation(summary = "send general application refer to judge email")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200",
            description = "General application refer to judge email sent successfully",
            content = {@Content(mediaType = "application/json", schema = @Schema(implementation = AboutToStartOrSubmitCallbackResponse.class))})})
    public ResponseEntity<AboutToStartOrSubmitCallbackResponse> sendGeneralApplicationReferToJudgeEmail(
        @RequestBody CallbackRequest callbackRequest) {
        log.info("Received request to send general application refer to judge email for Case ID: {}", callbackRequest.getCaseDetails().getId());
        validateCaseData(callbackRequest);

        CaseDetails caseDetails = callbackRequest.getCaseDetails();
        notificationService.sendContestedGeneralApplicationReferToJudgeEmail(caseDetails);

        return ResponseEntity.ok(AboutToStartOrSubmitCallbackResponse.builder().data(caseDetails.getData()).build());
    }

    @PostMapping(value = "/general-application-outcome", consumes = APPLICATION_JSON_VALUE)
    @Operation(summary = "send general application outcome email")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200",
            description = "General Application Outcome email sent successfully",
            content = {@Content(mediaType = "application/json", schema = @Schema(implementation = AboutToStartOrSubmitCallbackResponse.class))})})
    public ResponseEntity<AboutToStartOrSubmitCallbackResponse> sendGeneralApplicationOutcomeEmail(
        @RequestBody CallbackRequest callbackRequest) throws IOException {
        log.info("Received request to send General Application Outcome email for Case ID: {}", callbackRequest.getCaseDetails().getId());
        validateCaseData(callbackRequest);

        CaseDetails caseDetails = callbackRequest.getCaseDetails();
        notificationService.sendContestedGeneralApplicationOutcomeEmail(caseDetails);

        return ResponseEntity.ok(AboutToStartOrSubmitCallbackResponse.builder().data(caseDetails.getData()).build());
    }

    @PostMapping(value = "/consent-order-not-approved-sent", consumes = APPLICATION_JSON_VALUE)
    @Operation(summary = "send consent order not approved sent email")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200",
            description = "Consent order not approved sent email sent successfully",
            content = {@Content(mediaType = "application/json", schema = @Schema(implementation = AboutToStartOrSubmitCallbackResponse.class))})})
    public ResponseEntity<SubmittedCallbackResponse> sendConsentOrderNotApprovedSentEmail(
        @RequestBody CallbackRequest callbackRequest) {
        validateCaseData(callbackRequest);

        CaseDetails caseDetails = callbackRequest.getCaseDetails();
        Map<String, Object> caseData = caseDetails.getData();

        if (caseDataService.isApplicantSolicitorAgreeToReceiveEmails(caseDetails)) {
            log.info("Sending email notification to Applicant Solicitor about consent order not approved being sent");
            notificationService.sendConsentOrderNotApprovedSentEmailToApplicantSolicitor(caseDetails);
        }

        if (notificationService.isRespondentSolicitorEmailCommunicationEnabled(caseData)) {
            log.info("Sending email notification to Respondent Solicitor about consent order not approved being sent");
            notificationService.sendConsentOrderNotApprovedSentEmailToRespondentSolicitor(caseDetails);
        }

        return ResponseEntity.ok(SubmittedCallbackResponse.builder().build());
    }

    @PostMapping(value = "/transfer-to-local-court", consumes = APPLICATION_JSON_VALUE)
    @Operation(summary = "send a transfer to local courts email")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200",
            description = "Transfer to Local Courts e-mail sent successfully",
            content = {@Content(mediaType = "application/json", schema = @Schema(implementation = AboutToStartOrSubmitCallbackResponse.class))})})
    public ResponseEntity<AboutToStartOrSubmitCallbackResponse> sendTransferCourtsEmail(
        @RequestBody CallbackRequest callbackRequest) {

        log.info("Received request to send transfer courts email for Case ID: {}", callbackRequest.getCaseDetails().getId());
        validateCaseData(callbackRequest);

        CaseDetails caseDetails = callbackRequest.getCaseDetails();

        if (caseDataService.isConsentedApplication(caseDetails)) {
            log.info("Sending transfer courts email notification");
            notificationService.sendTransferToLocalCourtEmail(caseDetails);

            transferCourtService.storeTransferToCourtEmail(caseDetails);
        }

        return ResponseEntity.ok(AboutToStartOrSubmitCallbackResponse.builder().data(caseDetails.getData()).build());
    }

    @PostMapping(value = "/prepare-for-interim-hearing", consumes = APPLICATION_JSON_VALUE)
    @Operation(summary = "send general application refer to judge email")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200",
            description = "General application refer to judge email sent successfully",
            content = {@Content(mediaType = "application/json", schema = @Schema(implementation = AboutToStartOrSubmitCallbackResponse.class))})})
    public ResponseEntity<AboutToStartOrSubmitCallbackResponse> sendInterimHearingNotification(
        @RequestBody CallbackRequest callbackRequest) {
        log.info("Received request to send general application refer to judge email for Case ID: {}", callbackRequest.getCaseDetails().getId());
        validateCaseData(callbackRequest);

        CaseDetails caseDetails = callbackRequest.getCaseDetails();
        Map<String, Object> caseData = caseDetails.getData();

        if (!caseDataService.isPaperApplication(caseData)) {
            if (caseDataService.isApplicantSolicitorAgreeToReceiveEmails(caseDetails)) {
                log.info("Sending email notification to Applicant Solicitor about interim hearing");
                notificationService.sendInterimNotificationEmailToApplicantSolicitor(caseDetails);
            }
            if (notificationService.isRespondentSolicitorEmailCommunicationEnabled(caseData)) {
                log.info("Sending email notification to Respondent Solicitor about interim hearing");
                notificationService.sendInterimNotificationEmailToRespondentSolicitor(caseDetails);
            }
        }
        return ResponseEntity.ok(AboutToStartOrSubmitCallbackResponse.builder().data(caseDetails.getData()).build());
    }


    @PostMapping(value = "/notice-of-change", consumes = APPLICATION_JSON_VALUE)
    @Operation(summary = "Send a notice of change to the Solicitor email and a letter to the organization.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200",
            description = "Notice of change e-mail and letter sent successfully",
            content = {@Content(mediaType = "application/json", schema = @Schema(implementation = AboutToStartOrSubmitCallbackResponse.class))})})
    public ResponseEntity<AboutToStartOrSubmitCallbackResponse> sendNoticeOfChangeNotifications(
        @RequestHeader(value = AUTHORIZATION_HEADER) String authorisationToken,
        @RequestBody CallbackRequest callbackRequest) {

        log.info("Received request to send Notice of Change email and letter for Case ID: {}", callbackRequest.getCaseDetails().getId());
        validateCaseData(callbackRequest);

        CaseDetails caseDetails = callbackRequest.getCaseDetails();

        if (!YES_VALUE.equals(caseDetails.getData().get(IS_NOC_REJECTED))) {
            notificationService.sendNoticeOfChangeEmail(caseDetails);
            log.info("Call the noc letter service");
            nocLetterNotificationService.sendNoticeOfChangeLetters(caseDetails, callbackRequest.getCaseDetailsBefore(), authorisationToken);
        }

        return ResponseEntity.ok(AboutToStartOrSubmitCallbackResponse.builder().data(caseDetails.getData()).build());
    }

    @PostMapping(value = "/notice-of-change/caseworker", consumes = APPLICATION_JSON_VALUE)
    @Operation(summary = "Send a notice of change to the Solicitor email and a letter to the organization.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200",
            description = "Notice of change e-mail and letter sent successfully",
            content = {@Content(mediaType = "application/json", schema = @Schema(implementation = AboutToStartOrSubmitCallbackResponse.class))})})
    public ResponseEntity<AboutToStartOrSubmitCallbackResponse> sendNoticeOfChangeNotificationsCaseworker(
        @RequestHeader(value = AUTHORIZATION_HEADER) String authorisationToken,
        @RequestBody CallbackRequest callbackRequest) {

        validateCaseData(callbackRequest);

        CaseDetails caseDetails = callbackRequest.getCaseDetails();
        if (!requiresNotifications(callbackRequest)) {
            return ResponseEntity.ok(AboutToStartOrSubmitCallbackResponse.builder().data(caseDetails.getData()).build());
        }
        log.info("Received request to send Notice of Change email and letter for Case ID: {}", callbackRequest.getCaseDetails().getId());
        notificationService.sendNoticeOfChangeEmailCaseworker(caseDetails);
        log.info("Call the noc letter service");
        nocLetterNotificationService.sendNoticeOfChangeLetters(caseDetails, callbackRequest.getCaseDetailsBefore(), authorisationToken);
        caseDetails.getData().put(INCLUDES_REPRESENTATIVE_UPDATE, null);
        caseDetails.getData().put(NOC_PARTY, null);
        return ResponseEntity.ok(AboutToStartOrSubmitCallbackResponse.builder().data(caseDetails.getData()).build());
    }

    @PostMapping(value = "/update-frc", consumes = APPLICATION_JSON_VALUE)
    @Operation(summary = "Send FRC change update notifications")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "204",
            description = "Update FRC information notificatons sent successfully",
            content = {@Content(mediaType = "application/json", schema = @Schema(implementation = AboutToStartOrSubmitCallbackResponse.class))})})
    ResponseEntity<AboutToStartOrSubmitCallbackResponse> sendUpdateFrcNotifications(
        @RequestHeader(value = AUTHORIZATION_HEADER) String authToken,
        @RequestBody CallbackRequest callbackRequest) throws JsonProcessingException {
        log.info("Received request to send update FRC info notifications for Case ID: {}", callbackRequest.getCaseDetails().getId());
        validateCaseData(callbackRequest);

        CaseDetails caseDetails = callbackRequest.getCaseDetails();
        Map<String, Object> caseData = caseDetails.getData();

        updateFrcCorrespondenceService.sendCorrespondence(caseDetails, authToken);

        return ResponseEntity.ok(AboutToStartOrSubmitCallbackResponse.builder().data(caseData).build());
    }

    private boolean requiresNotifications(CallbackRequest callbackRequest) {
        Map<String, Object> caseData = callbackRequest.getCaseDetails().getData();

        return featureToggleService.isCaseworkerNoCEnabled()
            && Optional.ofNullable(caseData.get(INCLUDES_REPRESENTATIVE_UPDATE))
            .map(updateField -> updateField.equals(YES_VALUE)).orElse(false);
    }
}
