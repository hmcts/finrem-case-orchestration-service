package uk.gov.hmcts.reform.finrem.caseorchestration.controllers;

import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.ccd.client.model.CallbackRequest;
import uk.gov.hmcts.reform.finrem.caseorchestration.error.InvalidCaseDataException;
import uk.gov.hmcts.reform.finrem.caseorchestration.helper.DocumentHelper;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseDocument;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.HearingOrderCollectionData;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.HearingOrderDocument;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.GenericDocumentService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.MoveCollectionService;

import javax.validation.constraints.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static org.springframework.http.HttpStatus.BAD_REQUEST;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;
import static uk.gov.hmcts.reform.finrem.caseorchestration.OrchestrationConstants.AUTHORIZATION_HEADER;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.DRAFT_HEARING_ORDER_COLLECTION;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.DRAFT_HEARING_ORDER_COLLECTION_RO;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.FINAL_ORDER_COLLECTION;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.LATEST_DRAFT_HEARING_ORDER;

@RestController
@RequiredArgsConstructor
@RequestMapping(value = "/case-orchestration")
@Slf4j
public class HearingOrderController implements BaseController {


    @Autowired
    private final DocumentHelper documentHelper;
    @Autowired
    private final GenericDocumentService genericDocumentService;
    @Autowired
    private final MoveCollectionService moveCollectionService;

    @PostMapping(path = "/hearing-order/store", consumes = APPLICATION_JSON_VALUE, produces = APPLICATION_JSON_VALUE)
    @ApiOperation(value = "Handles conversion of hearing order if required and storage")
    @ApiResponses(value = {
        @ApiResponse(code = 200, message = "Callback was processed successfully or in case of an error message is attached to the case",
            response = AboutToStartOrSubmitCallbackResponse.class),
        @ApiResponse(code = 400, message = "Bad Request"),
        @ApiResponse(code = 500, message = "Internal Server Error")})
    public ResponseEntity<AboutToStartOrSubmitCallbackResponse> storeHearingOrder(
        @RequestHeader(value = AUTHORIZATION_HEADER) String authorisationToken,
        @NotNull @RequestBody @ApiParam("CaseData") CallbackRequest callback) {

        validateCaseData(callback);
        log.info("storing hearing order");
        Map<String, Object> caseData = callback.getCaseDetails().getData();
        CaseDocument hearingOrderDocument = getLatestHearingOrderAsPdf(caseData, authorisationToken);
        CaseDocument stampedHearingOrder = genericDocumentService.stampDocument(hearingOrderDocument, authorisationToken);
        caseData = moveCollectionService.moveCollection(caseData, DRAFT_HEARING_ORDER_COLLECTION, DRAFT_HEARING_ORDER_COLLECTION_RO);
        updateCaseDataForLatestDraftHearingOrder(caseData, stampedHearingOrder);
        updateCaseDataForLatestHearingOrderCollection(caseData, stampedHearingOrder);
        return ResponseEntity.ok(
            AboutToStartOrSubmitCallbackResponse.builder().data(caseData).build());

    }

    private CaseDocument getLatestHearingOrderAsPdf(Map<String, Object> caseData, String authorisationToken) {
        CaseDocument hearingOrderDocument = documentHelper.getLatestContestedDraftOrderCollection(caseData);
        if (hearingOrderDocument == null) {
            throw new InvalidCaseDataException(BAD_REQUEST.value(), "Missing data from callbackRequest.");
        }

        log.info("ABX {}", hearingOrderDocument.getDocumentFilename().toLowerCase());
        if (!hearingOrderDocument.getDocumentFilename().toLowerCase().endsWith(".pdf")) {
            log.info("Converting document to pdf");
            hearingOrderDocument = genericDocumentService.convertDocumentToPdf(hearingOrderDocument, authorisationToken);
        }

        return hearingOrderDocument;
    }

    private void updateCaseDataForLatestDraftHearingOrder(Map<String, Object> caseData, CaseDocument stampedHearingOrder) {
        caseData.put(LATEST_DRAFT_HEARING_ORDER, stampedHearingOrder);
    }

    private void updateCaseDataForLatestHearingOrderCollection(Map<String, Object> caseData, CaseDocument stampedHearingOrder) {
        List<HearingOrderCollectionData> finalOrderCollection = documentHelper.getFinalOrderDocuments(caseData);
        if (finalOrderCollection == null) {
            finalOrderCollection = new ArrayList<>();
        }
        finalOrderCollection.add(HearingOrderCollectionData.builder()
            .hearingOrderDocuments(HearingOrderDocument
                .builder()
                .uploadDraftDocument(stampedHearingOrder)
                .build())
            .build());
        caseData.put(FINAL_ORDER_COLLECTION, finalOrderCollection);
    }
}
