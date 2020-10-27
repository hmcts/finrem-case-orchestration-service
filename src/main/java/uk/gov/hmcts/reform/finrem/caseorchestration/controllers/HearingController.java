package uk.gov.hmcts.reform.finrem.caseorchestration.controllers;

import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.ccd.client.model.CallbackRequest;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.AdditionalHearingDocumentService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.HearingDocumentService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.NotificationService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.ValidateHearingService;

import javax.validation.constraints.NotNull;

import java.util.List;
import java.util.Map;

import static java.util.Collections.emptyList;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;
import static uk.gov.hmcts.reform.finrem.caseorchestration.OrchestrationConstants.AUTHORIZATION_HEADER;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.FORM_C;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.FORM_G;
import static uk.gov.hmcts.reform.finrem.caseorchestration.service.CommonFunction.isApplicantSolicitorAgreeToReceiveEmails;
import static uk.gov.hmcts.reform.finrem.caseorchestration.service.CommonFunction.isContestedPaperApplication;
import static uk.gov.hmcts.reform.finrem.caseorchestration.service.CommonFunction.isDocumentPresentInCaseData;

@RestController
@RequiredArgsConstructor
@RequestMapping(value = "/case-orchestration")
@Slf4j
public class HearingController implements BaseController {

    private final ValidateHearingService validateHearingService;
    private final HearingDocumentService hearingDocumentService;
    private final AdditionalHearingDocumentService additionalHearingDocumentService;
    private final NotificationService notificationService;

    @PostMapping(path = "/validate-hearing", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    @ApiOperation(value = "Validates a Hearing")
    @ApiResponses(value = {
        @ApiResponse(code = 200, message = "Callback was processed successfully or in case of an error message is attached to the case",
            response = AboutToStartOrSubmitCallbackResponse.class),
        @ApiResponse(code = 400, message = "Bad Request"),
        @ApiResponse(code = 500, message = "Internal Server Error")})
    public ResponseEntity<AboutToStartOrSubmitCallbackResponse> validateHearing(
        @RequestHeader(value = AUTHORIZATION_HEADER, required = false) String authToken,
        @RequestBody CallbackRequest request) {

        CaseDetails caseDetails = request.getCaseDetails();
        log.info("Received request for validating a hearing for Case ID: {}", caseDetails.getId());

        validateCaseData(request);

        List<String> errors = validateHearingService.validateHearingErrors(caseDetails);
        List<String> warnings = errors.isEmpty() ? validateHearingService.validateHearingWarnings(caseDetails) : emptyList();

        return ResponseEntity.ok(AboutToStartOrSubmitCallbackResponse.builder()
            .warnings(warnings)
            .errors(errors)
            .build());
    }

    @PostMapping(path = "/submit-scheduling-and-listing-details", consumes = APPLICATION_JSON_VALUE, produces = APPLICATION_JSON_VALUE)
    @ApiOperation(value = "Handles Form C and G generation. Send e-mail for 'Prepare for Hearing'. Serves as a callback from CCD")
    @ApiResponses(value = {
        @ApiResponse(code = 200, message = "Callback was processed successfully or in case of an error message is attached to the case",
            response = AboutToStartOrSubmitCallbackResponse.class),
        @ApiResponse(code = 400, message = "Bad Request"),
        @ApiResponse(code = 500, message = "Internal Server Error")})
    public ResponseEntity<AboutToStartOrSubmitCallbackResponse> submitSchedulingAndListingDetails(
        @RequestHeader(value = AUTHORIZATION_HEADER) String authorisationToken,
        @NotNull @RequestBody @ApiParam("CaseData") CallbackRequest request) {

        CaseDetails caseDetails = request.getCaseDetails();
        log.info("Received request for submitting scheduling and listing details for Case ID: {}", caseDetails.getId());

        validateCaseData(request);

        boolean alreadyHadFirstHearing = alreadyHadFirstHearing(caseDetails);

        Map<String, Object> caseData = caseDetails.getData();
        if (! alreadyHadFirstHearing) {
            caseData.putAll(hearingDocumentService.generateHearingDocuments(authorisationToken, caseDetails));
        }

        if (isContestedPaperApplication(caseDetails)) {
            if (alreadyHadFirstHearing) {
                log.info("Sending Additional Hearing Document to bulk print for Contested Paper Case ID: {}", caseDetails.getId());
                additionalHearingDocumentService.createAndSendAdditionalHearingDocuments(authorisationToken, caseDetails);
            } else {
                log.info("Sending Forms A, C, G to bulk print for Contested Paper Case ID: {}", caseDetails.getId());
                hearingDocumentService.sendFormCAndGForBulkPrint(caseDetails, authorisationToken);
            }
        }

        if (isApplicantSolicitorAgreeToReceiveEmails(caseDetails)) {
            log.info("Sending email notification to Applicant Solicitor for 'Prepare for Hearing'");
            notificationService.sendPrepareForHearingEmail(request);
        }

        return ResponseEntity.ok(AboutToStartOrSubmitCallbackResponse.builder().data(caseData).build());
    }

    private boolean alreadyHadFirstHearing(CaseDetails caseDetails) {
        return isDocumentPresentInCaseData(FORM_C, caseDetails)
            && isDocumentPresentInCaseData(FORM_G, caseDetails);
    }
}
