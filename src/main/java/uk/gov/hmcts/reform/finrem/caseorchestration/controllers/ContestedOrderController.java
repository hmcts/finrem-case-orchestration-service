package uk.gov.hmcts.reform.finrem.caseorchestration.controllers;

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
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.ccd.client.model.CallbackRequest;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.error.InvalidCaseDataException;
import uk.gov.hmcts.reform.finrem.caseorchestration.handler.SendOrderContestedAboutToSubmitHandler;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseDocument;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.HearingBundle;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.HearingBundleItems;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.HearingUploadBundle;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.HearingUploadBundleData;

import javax.validation.constraints.NotNull;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

import static org.springframework.http.HttpStatus.BAD_REQUEST;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.HEARING_DATE;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.HEARING_UPLOAD_BUNDLE_COLLECTION;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping(value = "/case-orchestration")
public class ContestedOrderController extends BaseController {

    private final ObjectMapper objectMapper;
    private final SendOrderContestedAboutToSubmitHandler sendOrderContestedAboutToSubmitHandler;

    @PostMapping(path = "/contested/validateHearingDate", consumes = APPLICATION_JSON_VALUE, produces = APPLICATION_JSON_VALUE)
    @Operation(summary = "check hearing date")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Callback was processed successfully or in case of an error message is attached to the case",
            content = {@Content(mediaType = "application/json", schema = @Schema(implementation = AboutToStartOrSubmitCallbackResponse.class))}),
        @ApiResponse(responseCode = "400", description = "Bad Request"),
        @ApiResponse(responseCode = "500", description = "Internal Server Error")})
    public ResponseEntity<AboutToStartOrSubmitCallbackResponse> checkHearingDate(
        @NotNull @RequestBody @Parameter(description = "CaseData") CallbackRequest callback) {

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
    @Operation(summary = "Documents to be viewed in order of newest first at top of the list")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Callback was processed successfully or in case of an error message is attached to the case",
            content = {@Content(mediaType = "application/json", schema = @Schema(implementation = AboutToStartOrSubmitCallbackResponse.class))}),
        @ApiResponse(responseCode = "400", description = "Bad Request"),
        @ApiResponse(responseCode = "500", description = "Internal Server Error")})
    public ResponseEntity<AboutToStartOrSubmitCallbackResponse> sortHearingBundles(
        @NotNull @RequestBody @Parameter(description = "CaseData") CallbackRequest callback) {

        CaseDetails caseDetails = callback.getCaseDetails();
        log.info("Received request for doc newest first at top of the list for Case ID: {}", caseDetails.getId());
        validateCaseData(callback);
        Map<String, Object> caseData = caseDetails.getData();
        List<String> errors = new ArrayList<>();
        List<HearingUploadBundleData> hearingBundleDataList = Optional.ofNullable(caseData.get(HEARING_UPLOAD_BUNDLE_COLLECTION))
            .map(this::convertToHearingBundleDataList).orElse(Collections.emptyList());

        if (!hearingBundleDataList.isEmpty()) {
            List<HearingUploadBundleData> updateUploadDateList = hearingBundleDataList.stream()
                .map(hd -> HearingUploadBundleData.builder()
                    .id(hd.getId())
                    .value(getHearingBundle(errors, hd))
                    .build())
                .sorted(Comparator.nullsLast((e1, e2) -> e2.getValue().getHearingBundleDate()
                    .compareTo(e1.getValue().getHearingBundleDate())))
                .collect(Collectors.toList());
            caseData.put(HEARING_UPLOAD_BUNDLE_COLLECTION, updateUploadDateList);
        }
        return ResponseEntity.ok(AboutToStartOrSubmitCallbackResponse
            .builder()
            .data(caseData)
            .errors(errors)
            .build());
    }

    private HearingBundle getHearingBundle(List<String> errors, HearingUploadBundleData hd) {
        return HearingBundle.builder()
            .hearingBundleDate(hd.getValue().getHearingBundleDate())
            .hearingBundleFdr(hd.getValue().getHearingBundleFdr())
            .hearingBundleDocuments(hd.getValue().getHearingBundleDocuments().stream()
                .map(getHearingUploadBundleFunction(errors))
                .sorted(Comparator.nullsLast((e1, e2) -> e2.getValue().getBundleUploadDate()
                    .compareTo(e1.getValue().getBundleUploadDate()))).collect(Collectors.toList()))
            .hearingBundleDescription(hd.getValue().getHearingBundleDescription())
            .build();
    }

    private Function<HearingUploadBundle, HearingUploadBundle> getHearingUploadBundleFunction(List<String> errors) {
        return hdi -> HearingUploadBundle.builder().id(hdi.getId())
            .value(HearingBundleItems.builder().bundleDocuments(getBundleDocuments(hdi, errors))
                .bundleUploadDate(hdi.getValue().getBundleUploadDate() == null
                    ? LocalDateTime.now() : hdi.getValue().getBundleUploadDate())
                .build()).build();
    }

    private CaseDocument getBundleDocuments(HearingUploadBundle hdi, List<String> errors) {
        if (!hdi.getValue().getBundleDocuments().getDocumentFilename().toUpperCase().endsWith(".PDF")) {
            errors.add(String.format("Uploaded bundle %s is not in expected format. Please upload bundle in pdf format.",
                hdi.getValue().getBundleDocuments().getDocumentFilename()));
        }
        return hdi.getValue().getBundleDocuments();
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
