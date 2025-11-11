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
import uk.gov.hmcts.reform.finrem.caseorchestration.service.NotificationService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.TransferCourtService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.correspondence.consentorder.ConsentOrderAvailableCorresponder;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.correspondence.consentorder.ConsentOrderNotApprovedCorresponder;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.correspondence.consentorder.ConsentOrderNotApprovedSentCorresponder;
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
    private final CaseDataService caseDataService;
    private final TransferCourtService transferCourtService;
    private final NocLetterNotificationService nocLetterNotificationService;
    private final UpdateFrcCorrespondenceService updateFrcCorrespondenceService;
    private final ConsentOrderNotApprovedCorresponder consentOrderNotApprovedCorresponder;
    private final ConsentOrderAvailableCorresponder consentOrderAvailableCorresponder;
    private final ConsentOrderNotApprovedSentCorresponder consentOrderNotApprovedSentCorresponder;

    @PostMapping(value = "/order-not-approved", consumes = APPLICATION_JSON_VALUE)
    @Operation(summary = "send e-mail for consent/contest order not approved.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200",
            description = "Consent/Contest order not approved e-mail sent successfully",
            content = {@Content(mediaType = "application/json", schema = @Schema(implementation = AboutToStartOrSubmitCallbackResponse.class))})})
    public ResponseEntity<AboutToStartOrSubmitCallbackResponse> sendConsentOrderNotApprovedEmail(
        @RequestBody CallbackRequest callbackRequest) {
        log.info("Received request to process notifications for 'Consent/Contest Order Not Approved' for Case ID: {}",
            callbackRequest.getCaseDetails().getId());

        validateCaseData(callbackRequest);
        CaseDetails caseDetails = callbackRequest.getCaseDetails();
        Map<String, Object> caseData = caseDetails.getData();

        consentOrderNotApprovedCorresponder.sendCorrespondence(callbackRequest.getCaseDetails());

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

        log.info("Received request to process notifications for 'Consent Order Available' for Case ID: {}", callbackRequest.getCaseDetails().getId());
        validateCaseData(callbackRequest);
        CaseDetails caseDetails = callbackRequest.getCaseDetails();
        Map<String, Object> caseData = caseDetails.getData();

        consentOrderAvailableCorresponder.sendCorrespondence(callbackRequest.getCaseDetails());

        return ResponseEntity.ok(AboutToStartOrSubmitCallbackResponse.builder().data(caseData).build());
    }

    @PostMapping(value = "/general-application-outcome", consumes = APPLICATION_JSON_VALUE)
    @Operation(summary = "send general application outcome email")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200",
            description = "General Application Outcome email sent successfully",
            content = {@Content(mediaType = "application/json", schema = @Schema(implementation = AboutToStartOrSubmitCallbackResponse.class))})})
    @SuppressWarnings("squid:CallToDeprecatedMethod")
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
        log.info("Received request to process notifications for consent order not approved sent email for Case ID: {}", caseDetails.getId());
        consentOrderNotApprovedSentCorresponder.sendCorrespondence(caseDetails);

        return ResponseEntity.ok(SubmittedCallbackResponse.builder().build());
    }

    @PostMapping(value = "/transfer-to-local-court", consumes = APPLICATION_JSON_VALUE)
    @Operation(summary = "send a transfer to local courts email")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200",
            description = "Transfer to Local Courts e-mail sent successfully",
            content = {@Content(mediaType = "application/json", schema = @Schema(implementation = AboutToStartOrSubmitCallbackResponse.class))})})
    @SuppressWarnings("squid:CallToDeprecatedMethod")
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

    @PostMapping(value = "/notice-of-change", consumes = APPLICATION_JSON_VALUE)
    @Operation(summary = "Send a notice of change to the Solicitor email and a letter to the organization.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200",
            description = "Notice of change e-mail and letter sent successfully",
            content = {@Content(mediaType = "application/json", schema = @Schema(implementation = AboutToStartOrSubmitCallbackResponse.class))})})
    @SuppressWarnings("squid:CallToDeprecatedMethod")
    public ResponseEntity<AboutToStartOrSubmitCallbackResponse> sendNoticeOfChangeNotifications(
        @RequestHeader(value = AUTHORIZATION_HEADER) String authorisationToken,
        @RequestBody CallbackRequest callbackRequest) {

        CaseDetails caseDetails = callbackRequest.getCaseDetails();
        Long caseId = caseDetails.getId();
        log.info("{} - Received request to send Notice of Change email and letter.", caseId);
        validateCaseData(callbackRequest);

        try {
            if (!YES_VALUE.equals(caseDetails.getData().get(IS_NOC_REJECTED))) {
                log.info("{} - Sending notice of change email & letters.", caseId);
                notificationService.sendNoticeOfChangeEmail(caseDetails);
                nocLetterNotificationService.sendNoticeOfChangeLetters(caseDetails, callbackRequest.getCaseDetailsBefore(), authorisationToken);
            }
        } catch (Throwable e) {
            log.info("{} - Sending notice of change email & letters failed. Caught the exception to avoid breaking NOC user interface flow",
                caseId);
        }
        return ResponseEntity.ok(AboutToStartOrSubmitCallbackResponse.builder().data(caseDetails.getData()).build());
    }

    @PostMapping(value = "/notice-of-change/caseworker", consumes = APPLICATION_JSON_VALUE)
    @Operation(summary = "Initiated by caseworker, sends a notice of change to the Solicitor email and a letter to the organization.")
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
            description = "Update FRC information notifications sent successfully",
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

        return Optional.ofNullable(caseData.get(INCLUDES_REPRESENTATIVE_UPDATE))
            .map(updateField -> updateField.equals(YES_VALUE)).orElse(false);
    }
}
