package uk.gov.hmcts.reform.finrem.caseorchestration.controllers;

import com.fasterxml.jackson.core.JsonProcessingException;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
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
import uk.gov.hmcts.reform.finrem.caseorchestration.error.CourtDetailsParseException;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.AdditionalHearingDocumentService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.HearingDocumentService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.NotificationService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.ValidateHearingService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.noc.solicitors.CheckRespondentSolicitorIsDigitalService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.serialisation.FinremCallbackRequestDeserializer;
import uk.gov.hmcts.reform.finrem.ccd.callback.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.finrem.ccd.callback.CallbackRequest;
import uk.gov.hmcts.reform.finrem.ccd.domain.DirectionDetailCollection;
import uk.gov.hmcts.reform.finrem.ccd.domain.Document;
import uk.gov.hmcts.reform.finrem.ccd.domain.FinremCaseData;
import uk.gov.hmcts.reform.finrem.ccd.domain.FinremCaseDetails;

import javax.validation.constraints.NotNull;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;
import static uk.gov.hmcts.reform.finrem.caseorchestration.OrchestrationConstants.AUTHORIZATION_HEADER;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.FORM_C;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.FORM_G;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.OUT_OF_FAMILY_COURT_RESOLUTION;

@RestController
@RequiredArgsConstructor
@RequestMapping(value = "/case-orchestration")
@Slf4j
public class HearingDocumentController extends BaseController {

    private final HearingDocumentService hearingDocumentService;
    private final AdditionalHearingDocumentService additionalHearingDocumentService;
    private final ValidateHearingService validateHearingService;
    private final FinremCallbackRequestDeserializer finremCallbackRequestDeserializer;
    private final NotificationService notificationService;
    private final CheckRespondentSolicitorIsDigitalService checkRespondentSolicitorIsDigitalService;

    @PostMapping(path = "/documents/hearing", consumes = APPLICATION_JSON_VALUE, produces = APPLICATION_JSON_VALUE)
    @Operation(summary = "Handles Form C and G generation. Serves as a callback from CCD")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Callback was processed successfully or in case of an error message is attached to the case",
            content = {@Content(mediaType = "application/json", schema = @Schema(implementation = AboutToStartOrSubmitCallbackResponse.class))}),
        @ApiResponse(responseCode = "400", description = "Bad Request"),
        @ApiResponse(responseCode = "500", description = "Internal Server Error")})
    public ResponseEntity<AboutToStartOrSubmitCallbackResponse> generateHearingDocument(
        @RequestHeader(value = AUTHORIZATION_HEADER) String authorisationToken,
        @NotNull @RequestBody @Parameter(description = "CaseData") String source) {

        CallbackRequest callbackRequest = finremCallbackRequestDeserializer.deserialize(source);
        validateCaseData(callbackRequest);

        FinremCaseDetails caseDetails = callbackRequest.getCaseDetails();
        log.info("Received request for validating a hearing for Case ID: {}", caseDetails.getId());

        List<String> errors = validateHearingService.validateHearingErrors(caseDetails);

        if (!errors.isEmpty()) {
            return ResponseEntity.ok(AboutToStartOrSubmitCallbackResponse.builder().errors(errors).build());
        }

        if (hearingDocumentService.alreadyHadFirstHearing(caseDetails)) {
            if (caseDetails.getCaseData().isContestedPaperApplication()) {
                additionalHearingDocumentService.createAdditionalHearingDocuments(authorisationToken, caseDetails);
            }
        } else {
            Map<String, Object> documents = hearingDocumentService.generateHearingDocuments(authorisationToken, caseDetails);
            caseDetails.getCaseData().setFormC((Document) documents.get(FORM_C));
            caseDetails.getCaseData().setFormG((Document) documents.get(FORM_G));
            caseDetails.getCaseData().setOutOfFamilyCourtResolution((Document) documents.get(OUT_OF_FAMILY_COURT_RESOLUTION));
            logDocumentsGenerated(caseDetails.getCaseData());
        }

        List<String> warnings = validateHearingService.validateHearingWarnings(caseDetails);

        if (caseDetails.getCaseData().isContestedPaperApplication()) {
            FinremCaseDetails caseDetailsBefore = callbackRequest.getCaseDetailsBefore();
            if (caseDetailsBefore != null && hearingDocumentService.alreadyHadFirstHearing(caseDetailsBefore)) {
                log.info("Sending Additional Hearing Document to bulk print for Contested Case ID: {}", caseDetails.getId());
                additionalHearingDocumentService.sendAdditionalHearingDocuments(authorisationToken, caseDetails);
            } else {
                log.info("Sending Forms A, C, G to bulk print for Contested Case ID: {}", caseDetails.getId());
                hearingDocumentService.sendFormCAndGForBulkPrint(caseDetails, authorisationToken);
            }
        }

        return ResponseEntity.ok(AboutToStartOrSubmitCallbackResponse.builder()
            .data(caseDetails.getCaseData())
            .warnings(warnings).build());
    }

    @PostMapping(path = "/contested-upload-direction-order", consumes = APPLICATION_JSON_VALUE, produces = APPLICATION_JSON_VALUE)
    @Operation(summary = "Handles direction order generation. Serves as a callback from CCD")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Callback was processed successfully or in case of an error message is attached to the case",
            content = {@Content(mediaType = "application/json", schema = @Schema(implementation = AboutToStartOrSubmitCallbackResponse.class))}),
        @ApiResponse(responseCode = "400", description = "Bad Request"),
        @ApiResponse(responseCode = "500", description = "Internal Server Error")})
    public ResponseEntity<AboutToStartOrSubmitCallbackResponse> generateHearingDocumentDirectionOrder(
        @RequestHeader(value = AUTHORIZATION_HEADER) String authorisationToken,
        @NotNull @RequestBody @Parameter(description = "CaseData") String source) {

        CallbackRequest callback = finremCallbackRequestDeserializer.deserialize(source);
        validateCaseData(callback);

        FinremCaseDetails caseDetails = callback.getCaseDetails();
        FinremCaseData caseData = caseDetails.getCaseData();
        List<String> errors = new ArrayList<>();

        log.info("Storing Additional Hearing Document for Case ID: {}", caseDetails.getId());
        try {
            additionalHearingDocumentService.createAndStoreAdditionalHearingDocuments(authorisationToken, caseDetails);
            sortDirectionDetailsCollection(caseData);
        } catch (CourtDetailsParseException | JsonProcessingException e) {
            log.error(e.getMessage());
            errors.add(e.getMessage());
        }

        return ResponseEntity.ok(AboutToStartOrSubmitCallbackResponse
            .builder()
            .data(caseData)
            .errors(errors)
            .build());
    }

    private void sortDirectionDetailsCollection(FinremCaseData caseData) {
        List<DirectionDetailCollection> directionDetailsCollectionList =
            Optional.ofNullable(caseData.getDirectionDetailsCollection()).orElse(Collections.emptyList());

        if (!directionDetailsCollectionList.isEmpty()) {
            List<DirectionDetailCollection> sortedDirectionDetailsCollectionList = directionDetailsCollectionList
                .stream()
                .filter(e -> (e.getValue() != null && e.getValue().getDateOfHearing() != null))
                .sorted(Comparator.comparing(e -> e.getValue().getDateOfHearing()))
                .collect(Collectors.toList());

            caseData.setDirectionDetailsCollection(sortedDirectionDetailsCollectionList);
        }
    }

    private void logDocumentsGenerated(FinremCaseData caseData) {
        Optional<Document> formC = Optional.ofNullable(caseData.getFormC());
        formC.ifPresent(document ->
            log.info("Form C generated: Filename = {}, url = {}, binUrl = {}",
                document.getFilename(),
                document.getUrl(),
                document.getBinaryUrl()));

        Optional<Document> formG = Optional.ofNullable(caseData.getFormG());
        formG.ifPresent(document ->
            log.info("Form G generated: Filename = {}, url = {}, binUrl = {}",
                document.getFilename(),
                document.getUrl(),
                document.getBinaryUrl()));

        Optional<Document> familyCourtResolution = Optional.ofNullable(caseData.getOutOfFamilyCourtResolution());
        familyCourtResolution.ifPresent(document ->
            log.info("Out of court Family Resolution generated: Filename = {}, url = {}, binUrl = {}",
                document.getFilename(),
                document.getUrl(),
                document.getBinaryUrl()));

    }
}