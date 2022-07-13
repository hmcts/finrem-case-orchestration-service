package uk.gov.hmcts.reform.finrem.caseorchestration.controllers;

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
import uk.gov.hmcts.reform.finrem.caseorchestration.error.InvalidCaseDataException;
import uk.gov.hmcts.reform.finrem.caseorchestration.handler.SendOrderContestedAboutToSubmitHandler;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.serialisation.FinremCallbackRequestDeserializer;
import uk.gov.hmcts.reform.finrem.ccd.callback.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.finrem.ccd.callback.CallbackRequest;
import uk.gov.hmcts.reform.finrem.ccd.domain.Document;
import uk.gov.hmcts.reform.finrem.ccd.domain.FinremCaseData;
import uk.gov.hmcts.reform.finrem.ccd.domain.FinremCaseDetails;
import uk.gov.hmcts.reform.finrem.ccd.domain.HearingBundleDocument;
import uk.gov.hmcts.reform.finrem.ccd.domain.HearingBundleDocumentCollection;
import uk.gov.hmcts.reform.finrem.ccd.domain.HearingUploadBundle;
import uk.gov.hmcts.reform.finrem.ccd.domain.HearingUploadBundleCollection;

import javax.validation.constraints.NotNull;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

import static org.springframework.http.HttpStatus.BAD_REQUEST;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping(value = "/case-orchestration")
public class ContestedOrderController extends BaseController {

    private final SendOrderContestedAboutToSubmitHandler sendOrderContestedAboutToSubmitHandler;
    private final FinremCallbackRequestDeserializer finremCallbackRequestDeserializer;

    @PostMapping(path = "/contested/validateHearingDate", consumes = APPLICATION_JSON_VALUE, produces = APPLICATION_JSON_VALUE)
    @Operation(summary = "check hearing date")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Callback was processed successfully or in case of an error message is attached to the case",
            content = {@Content(mediaType = "application/json", schema = @Schema(implementation = AboutToStartOrSubmitCallbackResponse.class))}),
        @ApiResponse(responseCode = "400", description = "Bad Request"),
        @ApiResponse(responseCode = "500", description = "Internal Server Error")})
    public ResponseEntity<AboutToStartOrSubmitCallbackResponse> checkHearingDate(
        @NotNull @RequestBody @Parameter(description = "CaseData") String source) {

        CallbackRequest callback = finremCallbackRequestDeserializer.deserialize(source);

        FinremCaseDetails caseDetails = callback.getCaseDetails();
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
            .data(caseDetails.getCaseData())
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
        @NotNull @RequestBody @Parameter(description = "CaseData") String source) {

        CallbackRequest callback = finremCallbackRequestDeserializer.deserialize(source);

        FinremCaseDetails caseDetails = callback.getCaseDetails();
        log.info("Received request for doc newest first at top of the list for Case ID: {}", caseDetails.getId());
        validateCaseData(callback);
        FinremCaseData caseData = caseDetails.getCaseData();
        List<String> errors = new ArrayList<>();
        List<HearingUploadBundleCollection> hearingBundleDataList =
            Optional.ofNullable(caseData.getHearingUploadBundle()).orElse(Collections.emptyList());

        if (!hearingBundleDataList.isEmpty()) {
            List<HearingUploadBundleCollection> updateUploadDateList = hearingBundleDataList.stream()
                .map(hd -> HearingUploadBundleCollection.builder()
                    .value(getHearingBundle(errors, hd))
                    .build())
                .sorted(Comparator.nullsLast((e1, e2) -> e2.getValue().getHearingBundleDate()
                    .compareTo(e1.getValue().getHearingBundleDate())))
                .collect(Collectors.toList());
            caseData.setHearingUploadBundle(updateUploadDateList);
        }
        return ResponseEntity.ok(AboutToStartOrSubmitCallbackResponse
            .builder()
            .data(caseData)
            .errors(errors)
            .build());
    }

    private HearingUploadBundle getHearingBundle(List<String> errors, HearingUploadBundleCollection hd) {
        return HearingUploadBundle.builder()
            .hearingBundleDate(hd.getValue().getHearingBundleDate())
            .hearingBundleFdr(hd.getValue().getHearingBundleFdr())
            .hearingBundleDocuments(hd.getValue().getHearingBundleDocuments().stream()
                .map(getHearingUploadBundleFunction(errors))
                .sorted(Comparator.nullsLast(getHearingBundleDocumentCollectionComparator())).collect(Collectors.toList()))
            .hearingBundleDescription(hd.getValue().getHearingBundleDescription())
            .build();
    }

    private Comparator<HearingBundleDocumentCollection> getHearingBundleDocumentCollectionComparator() {
        return (e1, e2) -> e2.getValue().getBundleUploadDate()
            .compareTo(e1.getValue().getBundleUploadDate());
    }

    private Function<HearingBundleDocumentCollection, HearingBundleDocumentCollection> getHearingUploadBundleFunction(List<String> errors) {
        return hdi -> HearingBundleDocumentCollection.builder()
            .value(HearingBundleDocument.builder().bundleDocuments(getBundleDocuments(hdi, errors))
                .bundleUploadDate(hdi.getValue().getBundleUploadDate() == null
                    ? LocalDateTime.now() : hdi.getValue().getBundleUploadDate())
                .build()).build();
    }

    private Document getBundleDocuments(HearingBundleDocumentCollection hdi, List<String> errors) {
        if (!hdi.getValue().getBundleDocuments().getFilename().toUpperCase().endsWith(".PDF")) {
            errors.add(String.format("Uploaded bundle %s is not in expected format. Please upload bundle in pdf format.",
                hdi.getValue().getBundleDocuments().getFilename()));
        }
        return hdi.getValue().getBundleDocuments();
    }

    private void validateHearingDate(CallbackRequest callbackRequest) {
        if (callbackRequest.getCaseDetails().getCaseData().getHearingDate() == null) {
            log.info("Hearing date for Case ID: {} not found", callbackRequest.getCaseDetails().getId());
            throw new InvalidCaseDataException(BAD_REQUEST.value(), "Missing hearing date.");
        }
    }
}
