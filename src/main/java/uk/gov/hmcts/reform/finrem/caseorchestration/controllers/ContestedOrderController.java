package uk.gov.hmcts.reform.finrem.caseorchestration.controllers;

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
import static uk.gov.hmcts.reform.finrem.caseorchestration.OrchestrationConstants.AUTHORIZATION_HEADER;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping(value = "/case-orchestration")
public class ContestedOrderController extends BaseController {

    private final SendOrderContestedAboutToSubmitHandler sendOrderContestedAboutToSubmitHandler;
    private final FinremCallbackRequestDeserializer finremCallbackRequestDeserializer;

    @PostMapping(path = "/contested/send-order", consumes = APPLICATION_JSON_VALUE, produces = APPLICATION_JSON_VALUE)
    @ApiOperation(value = "Handles Consent order approved generation. Serves as a callback from CCD")
    @ApiResponses(value = {
        @ApiResponse(code = 200, message = "Callback was processed successfully or in case of an error message is attached to the case",
            response = AboutToStartOrSubmitCallbackResponse.class),
        @ApiResponse(code = 400, message = "Bad Request"),
        @ApiResponse(code = 500, message = "Internal Server Error")
    })
    //Todo: to be removed once dfr1018 merged to prod
    @Deprecated
    public ResponseEntity<AboutToStartOrSubmitCallbackResponse> stampFinalOrder(
        @RequestHeader(value = AUTHORIZATION_HEADER) String authToken,
        @NotNull @RequestBody @ApiParam("CaseData") String source) {

        CallbackRequest callback = finremCallbackRequestDeserializer.deserialize(source);

        validateCaseData(callback);

        FinremCaseDetails caseDetails = callback.getCaseDetails();
        log.info("Starting to send contested order for case {}", caseDetails.getId());

        sendOrderContestedAboutToSubmitHandler.handle(callback, authToken);

        log.info("Finished sending contested order for case {}", caseDetails.getId());

        return ResponseEntity.ok(sendOrderContestedAboutToSubmitHandler.handle(callback, authToken));
    }

    @PostMapping(path = "/contested/validateHearingDate", consumes = APPLICATION_JSON_VALUE, produces = APPLICATION_JSON_VALUE)
    @ApiOperation(value = "check hearing date")
    @ApiResponses(value = {
        @ApiResponse(code = 200, message = "Callback was processed successfully or in case of an error message is attached to the case",
            response = AboutToStartOrSubmitCallbackResponse.class),
        @ApiResponse(code = 400, message = "Bad Request"),
        @ApiResponse(code = 500, message = "Internal Server Error")})
    public ResponseEntity<AboutToStartOrSubmitCallbackResponse> checkHearingDate(
        @NotNull @RequestBody @ApiParam("CaseData") String source) {

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
    @ApiOperation(value = "Documents to be viewed in order of newest first at top of the list")
    @ApiResponses(value = {
        @ApiResponse(code = 200, message = "Callback was processed successfully or in case of an error message is attached to the case",
            response = AboutToStartOrSubmitCallbackResponse.class),
        @ApiResponse(code = 400, message = "Bad Request"),
        @ApiResponse(code = 500, message = "Internal Server Error")})
    public ResponseEntity<AboutToStartOrSubmitCallbackResponse> sortHearingBundles(
        @NotNull @RequestBody @ApiParam("CaseData") String source) {

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
