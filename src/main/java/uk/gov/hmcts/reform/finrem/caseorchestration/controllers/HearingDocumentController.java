package uk.gov.hmcts.reform.finrem.caseorchestration.controllers;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
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
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.ccd.client.model.CallbackRequest;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.error.CourtDetailsParseException;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseDocument;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.DirectionDetailsCollectionData;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.AdditionalHearingDocumentService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.CaseDataService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.GenerateCoverSheetService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.HearingDocumentService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.NotificationService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.ValidateHearingService;

import javax.validation.constraints.NotNull;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;
import static uk.gov.hmcts.reform.finrem.caseorchestration.OrchestrationConstants.AUTHORIZATION_HEADER;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.BULK_PRINT_COVER_SHEET_APP;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.BULK_PRINT_COVER_SHEET_APP_CONFIDENTIAL;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.BULK_PRINT_COVER_SHEET_RES;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.BULK_PRINT_COVER_SHEET_RES_CONFIDENTIAL;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.DIRECTION_DETAILS_COLLECTION_CT;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.HEARING_ADDITIONAL_DOC;

@RestController
@RequiredArgsConstructor
@RequestMapping(value = "/case-orchestration")
@Slf4j
public class HearingDocumentController extends BaseController {

    private final HearingDocumentService hearingDocumentService;
    private final AdditionalHearingDocumentService additionalHearingDocumentService;
    private final ValidateHearingService validateHearingService;
    private final CaseDataService caseDataService;
    private final GenerateCoverSheetService coverSheetService;
    private final NotificationService notificationService;
    private final ObjectMapper objectMapper;

    @PostMapping(path = "/documents/hearing", consumes = APPLICATION_JSON_VALUE, produces = APPLICATION_JSON_VALUE)
    @Operation(summary = "Handles Form C and G generation. Serves as a callback from CCD")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Callback was processed successfully or in case of an error message is attached to the case",
            content = {@Content(mediaType = "application/json", schema = @Schema(implementation = AboutToStartOrSubmitCallbackResponse.class))}),
        @ApiResponse(responseCode = "400", description = "Bad Request"),
        @ApiResponse(responseCode = "500", description = "Internal Server Error")})
    public ResponseEntity<AboutToStartOrSubmitCallbackResponse> generateHearingDocument(
        @RequestHeader(value = AUTHORIZATION_HEADER) String authorisationToken,
        @NotNull @RequestBody @Parameter(description = "CaseData") CallbackRequest callbackRequest) throws IOException {

        validateCaseData(callbackRequest);
        CaseDetails caseDetails = callbackRequest.getCaseDetails();
        String caseId = caseDetails.getId().toString();
        log.info("Received request for validating a hearing for Case ID: {}", caseId);


        List<String> errors = validateHearingService.validateHearingErrors(caseDetails);
        if (!errors.isEmpty()) {
            return ResponseEntity.ok(AboutToStartOrSubmitCallbackResponse.builder()
                .errors(errors)
                .build());
        }

        if (caseDetails.getData().get(HEARING_ADDITIONAL_DOC) != null) {
            CaseDocument caseDocument = objectMapper.convertValue(caseDetails.getData().get(HEARING_ADDITIONAL_DOC),
                CaseDocument.class);
            CaseDocument pdfDocument =
                additionalHearingDocumentService.convertToPdf(caseDocument, authorisationToken, caseId);
            callbackRequest.getCaseDetails().getData().put(HEARING_ADDITIONAL_DOC, pdfDocument);
        }

        if (hearingDocumentService.alreadyHadFirstHearing(caseDetails)) {
            if (caseDataService.isContestedApplication(caseDetails)) {
                additionalHearingDocumentService.createAdditionalHearingDocuments(authorisationToken, caseDetails);
            }
        } else {
            caseDetails.getData().putAll(hearingDocumentService.generateHearingDocuments(authorisationToken, caseDetails));
        }

        List<String> warnings = validateHearingService.validateHearingWarnings(caseDetails);
        log.info("Hearing date warning {} Case ID: {}", warnings, caseDetails.getId());

        if (!notificationService.isApplicantSolicitorDigitalAndEmailPopulated(caseDetails)) {
            CaseDocument coverSheet = coverSheetService.generateApplicantCoverSheet(caseDetails, authorisationToken);
            log.info("Applicant coversheet generated and attach to case {}  for case Id {}", caseDetails.getId(), coverSheet);
            if (caseDataService.isApplicantAddressConfidential(caseDetails.getData())) {
                log.info("Applicant has been marked as confidential, adding coversheet to confidential field for caseId {}", caseDetails.getId());
                caseDetails.getData().remove(BULK_PRINT_COVER_SHEET_APP);
                caseDetails.getData().put(BULK_PRINT_COVER_SHEET_APP_CONFIDENTIAL, coverSheet);
            } else {
                log.info("Applicant adding coversheet to coversheet field for caseId {}", caseDetails.getId());
                caseDetails.getData().put(BULK_PRINT_COVER_SHEET_APP, coverSheet);
            }
        }
        if (!notificationService.isRespondentSolicitorDigitalAndEmailPopulated(caseDetails)) {
            CaseDocument coverSheet = coverSheetService.generateRespondentCoverSheet(caseDetails, authorisationToken);
            log.info("Respondent coversheet generated and attach to case {}  for case Id {}", caseDetails.getId(), coverSheet);
            if (caseDataService.isRespondentAddressConfidential(caseDetails.getData())) {
                log.info("Respondent has been marked as confidential, adding coversheet to confidential field for caseId {}", caseDetails.getId());
                caseDetails.getData().remove(BULK_PRINT_COVER_SHEET_RES);
                caseDetails.getData().put(BULK_PRINT_COVER_SHEET_RES_CONFIDENTIAL, coverSheet);
            } else {
                log.info("Respondent adding coversheet to coversheet field for caseId {}", caseDetails.getId());
                caseDetails.getData().put(BULK_PRINT_COVER_SHEET_RES, coverSheet);
            }
        }

        return ResponseEntity.ok(AboutToStartOrSubmitCallbackResponse.builder().data(caseDetails.getData()).warnings(warnings).build());
    }


    @PostMapping(path = "/contested-upload-direction-order", consumes = APPLICATION_JSON_VALUE, produces = APPLICATION_JSON_VALUE)
    @Operation(summary = "Handles direction order generation. Serves as a callback from CCD")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Callback was processed successfully or in case of an error message is attached to the case",
            content = {@Content(mediaType = "application/json", schema = @Schema(implementation = AboutToStartOrSubmitCallbackResponse.class))}),
        @ApiResponse(responseCode = "400", description = "Bad Request"),
        @ApiResponse(responseCode = "500", description = "Internal Server Error")})
    public ResponseEntity<GenericAboutToStartOrSubmitCallbackResponse<Map<String, Object>>> generateHearingDocumentDirectionOrder(
        @RequestHeader(value = AUTHORIZATION_HEADER) String authorisationToken,
        @NotNull @RequestBody @Parameter(description = "CaseData") CallbackRequest callback) {
        CaseDetails caseDetails = callback.getCaseDetails();
        validateCaseData(callback);
        Map<String, Object> caseData = caseDetails.getData();
        List<String> errors = new ArrayList<>();

        log.info("Storing Additional Hearing Document for Case ID: {}", caseDetails.getId());
        try {
            additionalHearingDocumentService.createAndStoreAdditionalHearingDocuments(authorisationToken, caseDetails);
            sortDirectionDetailsCollection(caseData);
        } catch (CourtDetailsParseException | JsonProcessingException e) {
            log.error(e.getMessage());
            errors.add(e.getMessage());
        }

        return ResponseEntity.ok(GenericAboutToStartOrSubmitCallbackResponse
            .<Map<String, Object>>builder()
            .data(caseData)
            .errors(errors)
            .build());
    }

    private void sortDirectionDetailsCollection(Map<String, Object> caseData) {
        List<DirectionDetailsCollectionData> directionDetailsCollectionList = Optional.ofNullable(caseData.get(DIRECTION_DETAILS_COLLECTION_CT))
            .map(this::convertToDirectionDetailsDataList).orElse(Collections.emptyList());

        if (!directionDetailsCollectionList.isEmpty()) {
            List<DirectionDetailsCollectionData> sortedDirectionDetailsCollectionList = directionDetailsCollectionList
                .stream()
                .filter(e -> (e.getDirectionDetailsCollection() != null && e.getDirectionDetailsCollection().getDateOfHearing() != null))
                .sorted(Comparator.comparing(e -> e.getDirectionDetailsCollection().getDateOfHearing()))
                .toList();
            caseData.put(DIRECTION_DETAILS_COLLECTION_CT, sortedDirectionDetailsCollectionList);
        }
    }

    private List<DirectionDetailsCollectionData> convertToDirectionDetailsDataList(Object object) {
        return objectMapper.convertValue(object, new TypeReference<>() {
        });
    }
}
