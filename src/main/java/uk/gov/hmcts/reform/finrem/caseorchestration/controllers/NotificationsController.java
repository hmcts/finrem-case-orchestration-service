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
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.ccd.client.model.CallbackRequest;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseDocument;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.AdditionalHearingDocumentService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.AssignedToJudgeDocumentService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.BulkPrintService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.CaseDataService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.FeatureToggleService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.GeneralEmailService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.HearingDocumentService;
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
import static uk.gov.hmcts.reform.finrem.caseorchestration.service.CommonFunction.isConsentedInContestedCase;
import static uk.gov.hmcts.reform.finrem.caseorchestration.service.CommonFunction.isContestedApplication;
import static uk.gov.hmcts.reform.finrem.caseorchestration.service.CommonFunction.isContestedPaperApplication;
import static uk.gov.hmcts.reform.finrem.caseorchestration.service.CommonFunction.isPaperApplication;

@RestController
@Slf4j
@RequestMapping(value = "/case-orchestration/notify")
@RequiredArgsConstructor
public class NotificationsController implements BaseController {

    private final NotificationService notificationService;
    private final BulkPrintService bulkPrintService;
    private final AssignedToJudgeDocumentService assignedToJudgeDocumentService;
    private final HelpWithFeesDocumentService helpWithFeesDocumentService;
    private final ManualPaymentDocumentService manualPaymentDocumentService;
    private final GeneralEmailService generalEmailService;
    private final CaseDataService caseDataService;
    private final FeatureToggleService featureToggleService;
    private final HearingDocumentService hearingDocumentService;
    private final AdditionalHearingDocumentService additionalHearingDocumentService;

    @PostMapping(value = "/hwf-successful", consumes = APPLICATION_JSON_VALUE)
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

    @PostMapping(value = "/assign-to-judge", consumes = APPLICATION_JSON_VALUE)
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

    @PostMapping(value = "/assign-to-judge-consent-in-contested", consumes = APPLICATION_JSON_VALUE)
    @ApiOperation(value = "Notify applicant and respondent when Judge assigned to case via letter")
    @ApiResponses(value = {
        @ApiResponse(code = 204, message = "Case assigned to Judge notification sent successfully",
            response = AboutToStartOrSubmitCallbackResponse.class)})
    public ResponseEntity<AboutToStartOrSubmitCallbackResponse> sendConsentInContestedAssignToJudgeConfirmationEmail(
        @RequestHeader(value = AUTHORIZATION_HEADER) String authToken,
        @RequestBody CallbackRequest callbackRequest) {

        log.info("Received request to notify applicant and respondent for Judge successfully assigned to case for Case ID: {}",
            callbackRequest.getCaseDetails().getId());
        validateCaseData(callbackRequest);
        CaseDetails caseDetails = callbackRequest.getCaseDetails();

        if (bulkPrintService.shouldPrintForApplicant(caseDetails)) {
            log.info("Sending applicant Consent in Contested AssignedToJudge notification letter for bulk print for Case ID: {}",
                callbackRequest.getCaseDetails().getId());

            CaseDocument applicantAssignedToJudgeNotificationLetter =
                assignedToJudgeDocumentService.generateApplicantConsentInContestedAssignedToJudgeNotificationLetter(caseDetails, authToken);
            bulkPrintService.sendDocumentForPrint(applicantAssignedToJudgeNotificationLetter, caseDetails);
        }

        log.info("Sending respondent Consent in Contested AssignedToJudge notification letter for bulk print for Case ID: {}",
            callbackRequest.getCaseDetails().getId());

        CaseDocument respondentAssignedToJudgeNotificationLetter =
            assignedToJudgeDocumentService.generateRespondentConsentInContestedAssignedToJudgeNotificationLetter(caseDetails, authToken);
        bulkPrintService.sendDocumentForPrint(respondentAssignedToJudgeNotificationLetter, caseDetails);

        return ResponseEntity.ok(AboutToStartOrSubmitCallbackResponse.builder().data(caseDetails.getData()).build());
    }

    @PostMapping(value = "/consent-order-made", consumes = APPLICATION_JSON_VALUE)
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

    @PostMapping(value = "/order-not-approved", consumes = APPLICATION_JSON_VALUE)
    @ApiOperation(value = "send e-mail for consent/contest order not approved.")
    @ApiResponses(value = {
        @ApiResponse(code = 204, message = "Consent/Contest order not approved e-mail sent successfully",
            response = AboutToStartOrSubmitCallbackResponse.class)})
    public ResponseEntity<AboutToStartOrSubmitCallbackResponse> sendConsentOrderNotApprovedEmail(
        @RequestBody CallbackRequest callbackRequest) {

        log.info("Received request to send email for 'Consent/Contest Order Not Approved' for Case ID: {}", callbackRequest.getCaseDetails().getId());
        validateCaseData(callbackRequest);
        CaseDetails caseDetails = callbackRequest.getCaseDetails();

        if (isApplicantSolicitorAgreeToReceiveEmails(caseDetails)) {
            if (isConsentedApplication(callbackRequest.getCaseDetails())) {
                log.info("Sending email notification to Solicitor for 'Consent Order Not Approved'");
                notificationService.sendConsentOrderNotApprovedEmail(callbackRequest);
            } else {
                log.info("Sending email notification to Solicitor for 'Contest Order Not Approved'");
                notificationService.sendContestOrderNotApprovedEmailApplicant(callbackRequest);
            }
        }

        Map<String, Object> caseData = caseDetails.getData();
        if (featureToggleService.isRespondentSolicitorEmailNotificationEnabled() && notificationService.shouldEmailRespondentSolicitor(caseData)
            && isContestedApplication(caseDetails)) {
            notificationService.sendContestOrderNotApprovedEmailRespondent(callbackRequest);
        }

        return ResponseEntity.ok(AboutToStartOrSubmitCallbackResponse.builder().data(caseData).build());
    }

    @PostMapping(value = "/contested-consent-order-approved", consumes = APPLICATION_JSON_VALUE)
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
            log.info("Sending email notification to Applicant Solicitor for 'Contested Consent Order Approved'");
            notificationService.sendContestedConsentOrderApprovedEmailToApplicantSolicitor(callbackRequest);
        }

        if (featureToggleService.isRespondentSolicitorEmailNotificationEnabled() && notificationService.shouldEmailRespondentSolicitor(caseData)) {
            log.info("Sending email notification to Respondent Solicitor for 'Contested Consent Order Approved'");
            notificationService.sendContestedConsentOrderApprovedEmailToRespondentSolicitor(callbackRequest);
        }

        return ResponseEntity.ok(AboutToStartOrSubmitCallbackResponse.builder().data(caseData).build());
    }

    @PostMapping(value = "/contested-consent-order-not-approved", consumes = APPLICATION_JSON_VALUE)
    @ApiOperation(value = "send e-mail for contested consent order not approved.")
    @ApiResponses(value = {
        @ApiResponse(code = 204, message = "Contested consent order not approved e-mail sent successfully",
            response = AboutToStartOrSubmitCallbackResponse.class)})
    public ResponseEntity<AboutToStartOrSubmitCallbackResponse> sendContestedConsentOrderNotApprovedEmail(
        @RequestBody CallbackRequest callbackRequest) {
        validateCaseData(callbackRequest);
        CaseDetails caseDetails = callbackRequest.getCaseDetails();
        Map<String, Object> caseData = caseDetails.getData();

        if (!isPaperApplication(caseData) && isApplicantSolicitorAgreeToReceiveEmails(caseDetails)) {
            log.info("Sending email for 'Contested Consent Order Not Approved' to Applicant Solicitor for Case ID: {}", caseDetails.getId());
            notificationService.sendContestedConsentOrderNotApprovedEmailApplicantSolicitor(callbackRequest);
        }

        if (featureToggleService.isRespondentSolicitorEmailNotificationEnabled() && notificationService.shouldEmailRespondentSolicitor(caseData)) {
            log.info("Sending email for 'Contested Consent Order Not Approved' to Respondent Solicitor for Case ID: {}", caseDetails.getId());
            notificationService.sendContestedConsentOrderNotApprovedEmailRespondentSolicitor(callbackRequest);
        }

        return ResponseEntity.ok(AboutToStartOrSubmitCallbackResponse.builder().data(caseData).build());
    }

    @PostMapping(value = "/general-order-raised", consumes = APPLICATION_JSON_VALUE)
    @ApiOperation(value = "send e-mail for general order raised.")
    @ApiResponses(value = {
        @ApiResponse(code = 204, message = "General order raised e-mail sent successfully",
            response = AboutToStartOrSubmitCallbackResponse.class)})
    public ResponseEntity<AboutToStartOrSubmitCallbackResponse> sendGeneralOrderRaisedEmail(
        @RequestBody CallbackRequest callbackRequest) {

        log.info("Received request to send email for General Order raised for Case ID: {}", callbackRequest.getCaseDetails().getId());
        validateCaseData(callbackRequest);
        CaseDetails caseDetails = callbackRequest.getCaseDetails();
        Map<String, Object> caseData = caseDetails.getData();

        if (isApplicantSolicitorAgreeToReceiveEmails(caseDetails)) {
            if (isConsentedApplication(caseDetails)) {
                log.info("Sending email notification to Solicitor for 'Consented General Order'");
                notificationService.sendConsentedGeneralOrderEmail(callbackRequest);
            } else {
                if (isConsentedInContestedCase(caseDetails)) {
                    log.info("Sending email notification to Solicitor for 'Contested consent General Order'");
                    notificationService.sendContestedConsentGeneralOrderEmail(callbackRequest);
                } else {
                    log.info("Sending email notification to Solicitor for 'Contested General Order'");
                    notificationService.sendContestedGeneralOrderEmail(callbackRequest);
                }
            }
        }

        return ResponseEntity.ok(AboutToStartOrSubmitCallbackResponse.builder().data(caseData).build());
    }

    @PostMapping(value = "/consent-order-available", consumes = APPLICATION_JSON_VALUE)
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

    @PostMapping(value = "/prepare-for-hearing", consumes = APPLICATION_JSON_VALUE)
    @ApiOperation(value = "send e-mail for 'Prepare for Hearing'.")
    @ApiResponses(value = {
        @ApiResponse(code = 204, message = "'Prepare for Hearing' e-mail sent successfully",
            response = AboutToStartOrSubmitCallbackResponse.class)})
    public ResponseEntity<AboutToStartOrSubmitCallbackResponse> sendPrepareForHearingEmail(
        @RequestHeader(value = AUTHORIZATION_HEADER) String authorisationToken,
        @RequestBody CallbackRequest callbackRequest) {

        log.info("Received request to send email for 'Prepare for Hearing' for Case ID: {}", callbackRequest.getCaseDetails().getId());
        validateCaseData(callbackRequest);
        CaseDetails caseDetails = callbackRequest.getCaseDetails();

        if (isApplicantSolicitorAgreeToReceiveEmails(caseDetails)) {
            log.info("Sending email notification to Applicant Solicitor for 'Prepare for Hearing'");
            notificationService.sendPrepareForHearingEmail(callbackRequest);
        }

        if (isContestedPaperApplication(caseDetails)) {
            if (hearingDocumentService.alreadyHadFirstHearing(callbackRequest.getCaseDetailsBefore())) {
                log.info("Sending Additional Hearing Document to bulk print for Contested Paper Case ID: {}", caseDetails.getId());
                additionalHearingDocumentService.sendAdditionalHearingDocuments(authorisationToken, caseDetails);
            } else {
                log.info("Sending Forms A, C, G to bulk print for Contested Paper Case ID: {}", caseDetails.getId());
                hearingDocumentService.sendFormCAndGForBulkPrint(caseDetails, authorisationToken);
            }
        }

        return ResponseEntity.ok(AboutToStartOrSubmitCallbackResponse.builder().data(caseDetails.getData()).build());
    }

    @PostMapping(value = "/prepare-for-hearing-order-sent", consumes = APPLICATION_JSON_VALUE)
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
            notificationService.sendPrepareForHearingOrderSentEmailApplicant(callbackRequest);
        }

        if (featureToggleService.isRespondentSolicitorEmailNotificationEnabled() && notificationService.shouldEmailRespondentSolicitor(caseData)) {
            log.info("Sending email notification to Respondent Solicitor for 'Prepare for Hearing (after send order)'");
            notificationService.sendPrepareForHearingOrderSentEmailRespondent(callbackRequest);
        }

        return ResponseEntity.ok(AboutToStartOrSubmitCallbackResponse.builder().data(caseData).build());
    }

    @PostMapping(value = "/contest-application-issued", consumes = APPLICATION_JSON_VALUE)
    @ApiOperation(value = "send e-mail for Contested 'Application Issued'.")
    @ApiResponses(value = {
        @ApiResponse(code = 204, message = "Consent order available e-mail sent successfully",
            response = AboutToStartOrSubmitCallbackResponse.class)})
    public ResponseEntity<AboutToStartOrSubmitCallbackResponse> sendContestedApplicationIssuedEmail(
        @RequestBody CallbackRequest callbackRequest) {
        log.info("Received request to send email for Contested 'Application Issued' for Case ID: {}", callbackRequest.getCaseDetails().getId());
        validateCaseData(callbackRequest);
        CaseDetails caseDetails = callbackRequest.getCaseDetails();

        if (isApplicantSolicitorAgreeToReceiveEmails(caseDetails)) {
            log.info("Sending Contested 'Application Issued' email notification to Applicant Solicitor");
            notificationService.sendContestedApplicationIssuedEmailToApplicantSolicitor(callbackRequest);
        }

        Map<String, Object> caseData = caseDetails.getData();
        if (featureToggleService.isRespondentSolicitorEmailNotificationEnabled() && notificationService.shouldEmailRespondentSolicitor(caseData)) {
            log.info("Sending Contested 'Application Issued' email notification to Respondent Solicitor");
            notificationService.sendContestedApplicationIssuedEmailToRespondentSolicitor(callbackRequest);
        }

        return ResponseEntity.ok(AboutToStartOrSubmitCallbackResponse.builder().data(caseData).build());
    }

    @PostMapping(value = "/contest-order-approved", consumes = APPLICATION_JSON_VALUE)
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

    @PostMapping(value = "/draft-order", consumes = APPLICATION_JSON_VALUE)
    @ApiOperation(value = "send e-mail for Solicitor To Draft Order")
    @ApiResponses(value = {
        @ApiResponse(code = 204, message = "Draft Order e-mail sent successfully",
            response = AboutToStartOrSubmitCallbackResponse.class)})
    public ResponseEntity<AboutToStartOrSubmitCallbackResponse> sendDraftOrderEmail(
        @RequestBody CallbackRequest callbackRequest) {

        log.info("Received request to send email for 'Solicitor To Draft Order' for Case ID: {}", callbackRequest.getCaseDetails().getId());
        validateCaseData(callbackRequest);
        CaseDetails caseDetails = callbackRequest.getCaseDetails();
        Map<String, Object> caseData = caseDetails.getData();

        if (isApplicantSolicitorResponsibleToDraftOrder(caseData) && isApplicantSolicitorAgreeToReceiveEmails(caseDetails)) {
            log.info("Sending email notification to Applicant Solicitor for 'Draft Order'");
            notificationService.sendSolicitorToDraftOrderEmailApplicant(callbackRequest);
        }

        if (featureToggleService.isRespondentSolicitorEmailNotificationEnabled()
            && caseDataService.isRespondentSolicitorResponsibleToDraftOrder(caseData)
            && notificationService.shouldEmailRespondentSolicitor(caseData)) {
            log.info("Sending email notification to Respondent Solicitor for 'Draft Order'");
            notificationService.sendSolicitorToDraftOrderEmailRespondent(callbackRequest);
        }

        return ResponseEntity.ok(AboutToStartOrSubmitCallbackResponse.builder().data(caseData).build());
    }

    @PostMapping(value = "/general-email", consumes = APPLICATION_JSON_VALUE)
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

    @PostMapping(value = "/manual-payment", consumes = APPLICATION_JSON_VALUE)
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

    @PostMapping(value = "/general-application-refer-to-judge", consumes = APPLICATION_JSON_VALUE)
    @ApiOperation(value = "send general application refer to judge email")
    @ApiResponses(value = {
        @ApiResponse(code = 204, message = "General application refer to judge email sent successfully",
            response = AboutToStartOrSubmitCallbackResponse.class)})
    public ResponseEntity<AboutToStartOrSubmitCallbackResponse> sendGeneralApplicationReferToJudgeEmail(
        @RequestBody CallbackRequest callbackRequest) {
        log.info("Received request to send general application refer to judge email for Case ID: {}", callbackRequest.getCaseDetails().getId());
        validateCaseData(callbackRequest);

        CaseDetails caseDetails = callbackRequest.getCaseDetails();

        notificationService.sendContestedGeneralApplicationReferToJudgeEmail(callbackRequest);

        return ResponseEntity.ok(AboutToStartOrSubmitCallbackResponse.builder().data(caseDetails.getData()).build());
    }

    @PostMapping(value = "/general-application-outcome", consumes = APPLICATION_JSON_VALUE)
    @ApiOperation(value = "send general application outcome email")
    @ApiResponses(value = {
        @ApiResponse(code = 204, message = "General Application Outcome email sent successfully",
            response = AboutToStartOrSubmitCallbackResponse.class)})
    public ResponseEntity<AboutToStartOrSubmitCallbackResponse> sendGeneralApplicationOutcomeEmail(
        @RequestBody CallbackRequest callbackRequest) throws IOException {
        log.info("Received request to send General Application Outcome email for Case ID: {}", callbackRequest.getCaseDetails().getId());
        validateCaseData(callbackRequest);

        CaseDetails caseDetails = callbackRequest.getCaseDetails();

        notificationService.sendContestedGeneralApplicationOutcomeEmail(callbackRequest);

        return ResponseEntity.ok(AboutToStartOrSubmitCallbackResponse.builder().data(caseDetails.getData()).build());
    }
}
