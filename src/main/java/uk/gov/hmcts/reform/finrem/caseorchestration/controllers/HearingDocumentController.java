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
import uk.gov.hmcts.reform.finrem.caseorchestration.service.CaseDataService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.HearingDocumentService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.NotificationService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.ValidateHearingService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.noc.solicitors.CheckRespondentSolicitorIsDigitalService;

import javax.validation.constraints.NotNull;

import java.io.IOException;
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

    private final HearingDocumentService hearingDocumentService;
    private final AdditionalHearingDocumentService additionalHearingDocumentService;
    private final ValidateHearingService validateHearingService;
    private final CaseDataService caseDataService;
    private final NotificationService notificationService;
    private final ObjectMapper objectMapper;
    private final CheckRespondentSolicitorIsDigitalService checkRespondentSolicitorIsDigitalService;

    @PostMapping(path = "/documents/hearing", consumes = APPLICATION_JSON_VALUE, produces = APPLICATION_JSON_VALUE)
    @ApiOperation(value = "Handles Form C and G generation. Serves as a callback from CCD")
    @ApiResponses(value = {
        @ApiResponse(code = 200, message = "Callback was processed successfully or in case of an error message is attached to the case",
            response = AboutToStartOrSubmitCallbackResponse.class),
        @ApiResponse(code = 400, message = "Bad Request"),
        @ApiResponse(code = 500, message = "Internal Server Error")})
    public ResponseEntity<AboutToStartOrSubmitCallbackResponse> generateHearingDocument(
            @RequestHeader(value = AUTHORIZATION_HEADER) String authorisationToken,
            @NotNull @RequestBody @ApiParam("CaseData") CallbackRequest callbackRequest) throws IOException {

        CaseDetails caseDetails = callbackRequest.getCaseDetails();
        log.info("Received request for validating a hearing for Case ID: {}", caseDetails.getId());

        validateCaseData(callbackRequest);

        List<String> errors = validateHearingService.validateHearingErrors(caseDetails);
        if (!errors.isEmpty()) {
            return ResponseEntity.ok(AboutToStartOrSubmitCallbackResponse.builder()
                .errors(errors)
                .build());
        }

        if (hearingDocumentService.alreadyHadFirstHearing(caseDetails)) {
            if (caseDataService.isContestedPaperApplication(caseDetails)) {
                additionalHearingDocumentService.createAdditionalHearingDocuments(authorisationToken, caseDetails);
            }
        } else {
            caseDetails.getData().putAll(hearingDocumentService.generateHearingDocuments(authorisationToken, caseDetails));
        }

        List<String> warnings = validateHearingService.validateHearingWarnings(caseDetails);

        if (caseDataService.isContestedApplication(caseDetails)
            && notificationService.isSolicitorEmailCommunicationProhibited(caseDetails)) {
            CaseDetails caseDetailsBefore = callbackRequest.getCaseDetailsBefore();
            if (caseDetailsBefore != null && hearingDocumentService.alreadyHadFirstHearing(caseDetailsBefore)) {
                log.info("Sending Additional Hearing Document to bulk print for Contested Case ID: {}", caseDetails.getId());
                additionalHearingDocumentService.sendAdditionalHearingDocuments(authorisationToken, caseDetails);
            } else {
                log.info("Sending Forms A, C, G to bulk print for Contested Case ID: {}", caseDetails.getId());
                hearingDocumentService.sendFormCAndGForBulkPrint(caseDetails, authorisationToken);
            }
        }
        return ResponseEntity.ok(AboutToStartOrSubmitCallbackResponse.builder().data(caseDetails.getData()).warnings(warnings).build());
    }

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

        if (!directionDetailsCollectionList.isEmpty()) {
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