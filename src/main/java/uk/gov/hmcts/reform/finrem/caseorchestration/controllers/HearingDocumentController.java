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
import uk.gov.hmcts.reform.finrem.caseorchestration.mapper.FinremCaseDetailsMapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.EventType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseDocument;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.DirectionDetailsCollectionData;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.AdditionalHearingDocumentService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.CaseDataService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.GenerateCoverSheetService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.HearingDocumentService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.NotificationService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.ValidateHearingService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.documentcatergory.DraftOrderDocumentCategoriser;

import javax.validation.constraints.NotNull;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;
import static uk.gov.hmcts.reform.finrem.caseorchestration.OrchestrationConstants.AUTHORIZATION_HEADER;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.DIRECTION_DETAILS_COLLECTION_CT;

@RestController
@RequiredArgsConstructor
@RequestMapping(value = "/case-orchestration")
@Slf4j
public class HearingDocumentController extends BaseController {

    private final AdditionalHearingDocumentService additionalHearingDocumentService;
    private final ObjectMapper objectMapper;
    private final FinremCaseDetailsMapper finremCaseDetailsMapper;
    private final DraftOrderDocumentCategoriser draftOrderDocumentCategoriser;

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
        FinremCaseDetails finremCaseDetails = finremCaseDetailsMapper.mapToFinremCaseDetails(caseDetails);
        draftOrderDocumentCategoriser.categorise(finremCaseDetails.getData());
        CaseDetails mappedCaseDetails = finremCaseDetailsMapper.mapToCaseDetails(finremCaseDetails);

        return ResponseEntity.ok(GenericAboutToStartOrSubmitCallbackResponse
            .<Map<String, Object>>builder()
            .data(mappedCaseDetails.getData())
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