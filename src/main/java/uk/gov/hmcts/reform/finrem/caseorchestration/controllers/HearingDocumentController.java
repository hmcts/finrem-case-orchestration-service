package uk.gov.hmcts.reform.finrem.caseorchestration.controllers;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
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
import uk.gov.hmcts.reform.finrem.caseorchestration.error.CourtDetailsParseException;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.DirectionDetailsCollectionData;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.AdditionalHearingDocumentService;

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
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.DIRECTION_DETAILS_COLLECTION_CT;

@RestController
@RequiredArgsConstructor
@RequestMapping(value = "/case-orchestration")
@Slf4j
public class HearingDocumentController extends BaseController {

    private final AdditionalHearingDocumentService additionalHearingDocumentService;
    private final ObjectMapper objectMapper;

    @PostMapping(path = "/contested-upload-direction-order", consumes = APPLICATION_JSON_VALUE, produces = APPLICATION_JSON_VALUE)
    @ApiOperation(value = "Handles direction order generation. Serves as a callback from CCD")
    @ApiResponses(value = {
        @ApiResponse(code = 200, message = "Callback was processed successfully or in case of an error message is attached to the case",
            response = AboutToStartOrSubmitCallbackResponse.class),
        @ApiResponse(code = 400, message = "Bad Request"),
        @ApiResponse(code = 500, message = "Internal Server Error")})
    public ResponseEntity<AboutToStartOrSubmitCallbackResponse> generateHearingDocumentDirectionOrder(
        @RequestHeader(value = AUTHORIZATION_HEADER) String authorisationToken,
        @NotNull @RequestBody @ApiParam("CaseData") CallbackRequest callback) {
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

        return ResponseEntity.ok(AboutToStartOrSubmitCallbackResponse
            .builder()
            .data(caseData)
            .errors(errors)
            .build());
    }

    private void sortDirectionDetailsCollection(Map<String, Object> caseData) {
        List<DirectionDetailsCollectionData> directionDetailsCollectionList = Optional.ofNullable(caseData.get(DIRECTION_DETAILS_COLLECTION_CT))
            .map(this::convertToDirectionDetailsDataList).orElse(Collections.emptyList());

        if (! directionDetailsCollectionList.isEmpty()) {
            List<DirectionDetailsCollectionData> sortedDirectionDetailsCollectionList = directionDetailsCollectionList
                .stream()
                .filter(e -> (e.getDirectionDetailsCollection() != null &&  e.getDirectionDetailsCollection().getDateOfHearing() != null))
                .sorted(Comparator.comparing(e -> e.getDirectionDetailsCollection().getDateOfHearing()))
                .collect(Collectors.toList());
            caseData.put(DIRECTION_DETAILS_COLLECTION_CT, sortedDirectionDetailsCollectionList);
        }
    }

    private List<DirectionDetailsCollectionData> convertToDirectionDetailsDataList(Object object) {
        return objectMapper.convertValue(object, new TypeReference<>() {
        });
    }
}
