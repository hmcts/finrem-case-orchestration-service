package uk.gov.hmcts.reform.finrem.caseorchestration.controllers;

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
import uk.gov.hmcts.reform.finrem.caseorchestration.error.InvalidCaseDataException;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.HearingBundle;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.HearingBundleItems;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.HearingUploadBundle;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.HearingUploadBundleData;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.ContestedCaseOrderService;

import javax.validation.constraints.NotNull;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import static org.springframework.http.HttpStatus.BAD_REQUEST;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;
import static uk.gov.hmcts.reform.finrem.caseorchestration.OrchestrationConstants.AUTHORIZATION_HEADER;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.HEARING_DATE;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.HEARING_UPLOAD_BUNDLE_COLLECTION;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping(value = "/case-orchestration")
public class ContestedOrderController implements BaseController {

    private final ObjectMapper objectMapper;
    private final ContestedCaseOrderService contestedCaseOrderService;

    @PostMapping(path = "/contested/send-order", consumes = APPLICATION_JSON_VALUE, produces = APPLICATION_JSON_VALUE)
    @ApiOperation(value = "Handles Consent order approved generation. Serves as a callback from CCD")
    @ApiResponses(value = {
        @ApiResponse(code = 200, message = "Callback was processed successfully or in case of an error message is attached to the case",
            response = AboutToStartOrSubmitCallbackResponse.class),
        @ApiResponse(code = 400, message = "Bad Request"),
        @ApiResponse(code = 500, message = "Internal Server Error")
    })
    public ResponseEntity<AboutToStartOrSubmitCallbackResponse> stampFinalOrder(
        @RequestHeader(value = AUTHORIZATION_HEADER) String authToken,
        @NotNull @RequestBody @ApiParam("CaseData") CallbackRequest callback) {

        validateCaseData(callback);

        CaseDetails caseDetails = callback.getCaseDetails();
        log.info("Starting to send contested order for case {}", caseDetails.getId());

        contestedCaseOrderService.printAndMailGeneralOrderToParties(caseDetails, authToken);
        contestedCaseOrderService.printAndMailHearingDocuments(caseDetails, authToken);
        contestedCaseOrderService.stampFinalOrder(caseDetails, authToken);

        log.info("Finished sending contested order for case {}", caseDetails.getId());

        return ResponseEntity.ok(AboutToStartOrSubmitCallbackResponse.builder().data(caseDetails.getData()).build());
    }

    @PostMapping(path = "/contested/validateHearingDate", consumes = APPLICATION_JSON_VALUE, produces = APPLICATION_JSON_VALUE)
    @ApiOperation(value = "check hearing date")
    @ApiResponses(value = {
        @ApiResponse(code = 200, message = "Callback was processed successfully or in case of an error message is attached to the case",
            response = AboutToStartOrSubmitCallbackResponse.class),
        @ApiResponse(code = 400, message = "Bad Request"),
        @ApiResponse(code = 500, message = "Internal Server Error")})
    public ResponseEntity<AboutToStartOrSubmitCallbackResponse> checkHearingDate(
        @NotNull @RequestBody @ApiParam("CaseData") CallbackRequest callback) {

        CaseDetails caseDetails = callback.getCaseDetails();
        log.info("Received request to check for manage bundle hearing date for Case ID: {}", caseDetails.getId());
        validateCaseData(callback);

        final List<String> errors = new ArrayList<>();

        try {
            validateHearingDate(callback);
        } catch (InvalidCaseDataException invalidCaseDataException) {
            errors.add(invalidCaseDataException.getMessage());
        }
        return ResponseEntity.ok(AboutToStartOrSubmitCallbackResponse
            .builder()
            .data(caseDetails.getData())
            .errors(errors)
            .build());
    }

    @PostMapping(path = "/contested/sortUploadedHearingBundles", consumes = APPLICATION_JSON_VALUE, produces = APPLICATION_JSON_VALUE)
    @ApiOperation(value = "Documents to be viewed in order of newest first at top of the list")
    @ApiResponses(value = {
        @ApiResponse(code = 200, message = "Callback was processed successfully or in case of an error message is attached to the case",
            response = AboutToStartOrSubmitCallbackResponse.class),
        @ApiResponse(code = 400, message = "Bad Request"),
        @ApiResponse(code = 500, message = "Internal Server Error")})
    public ResponseEntity<AboutToStartOrSubmitCallbackResponse> sortHearingBundles(
        @NotNull @RequestBody @ApiParam("CaseData") CallbackRequest callback) {

        CaseDetails caseDetails = callback.getCaseDetails();
        log.info("Received request for doc newest first at top of the list for Case ID: {}", caseDetails.getId());
        validateCaseData(callback);
        Map<String, Object> caseData = caseDetails.getData();

        List<HearingUploadBundleData> hearingBundleDataList = Optional.ofNullable(caseData.get(HEARING_UPLOAD_BUNDLE_COLLECTION))
                .map(this::convertToHearingBundleDataList).orElse(Collections.emptyList());

        if (!hearingBundleDataList.isEmpty()) {
            List<HearingUploadBundleData> updateUploadDateList = hearingBundleDataList.stream()
                    .map(hd -> HearingUploadBundleData.builder()
                    .id(hd.getId())
                    .value(HearingBundle.builder()
                        .hearingBundleDate(hd.getValue().getHearingBundleDate())
                        .hearingBundleDocuments(hd.getValue().getHearingBundleDocuments().stream()
                             .map(hdi -> HearingUploadBundle.builder().id(hdi.getId())
                                 .value(HearingBundleItems.builder().bundleDocuments(hdi.getValue().getBundleDocuments())
                                     .bundleUploadDate(hdi.getValue().getBundleUploadDate() == null
                                         ? LocalDate.now() : hdi.getValue().getBundleUploadDate())
                                     .build()).build())
                                 .sorted(Comparator.nullsLast((e1, e2) -> e2.getValue().getBundleUploadDate()
                                    .compareTo(e1.getValue().getBundleUploadDate()))).collect(Collectors.toList()))
                        .hearingBundleDescription(hd.getValue().getHearingBundleDescription())
                        .build())
                    .build())
                    .sorted(Comparator.nullsLast((e1, e2) -> e2.getValue().getHearingBundleDate()
                    .compareTo(e1.getValue().getHearingBundleDate())))
                    .collect(Collectors.toList());
            caseData.put(HEARING_UPLOAD_BUNDLE_COLLECTION, updateUploadDateList);
        }
        return ResponseEntity.ok(AboutToStartOrSubmitCallbackResponse
            .builder()
            .data(caseData)
            .build());
    }

    private List<HearingUploadBundleData> convertToHearingBundleDataList(Object object) {
        return objectMapper.convertValue(object, new TypeReference<>() {
        });
    }

    private void validateHearingDate(CallbackRequest callbackRequest) {
        if (callbackRequest.getCaseDetails().getData().get(HEARING_DATE) == null) {
            log.info("Hearing date for Case ID: {} not found", callbackRequest.getCaseDetails().getId());
            throw new InvalidCaseDataException(BAD_REQUEST.value(), "Missing hearing date.");
        }
    }
}
