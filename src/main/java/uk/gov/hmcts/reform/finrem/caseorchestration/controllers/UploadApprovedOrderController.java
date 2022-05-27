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
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.ccd.client.model.CallbackRequest;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.DraftDirectionOrder;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.CaseDataService;

import javax.validation.constraints.NotNull;

import java.util.Map;
import java.util.Optional;

import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;
import static uk.gov.hmcts.reform.finrem.caseorchestration.OrchestrationConstants.AUTHORIZATION_HEADER;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.CONTESTED_ORDER_APPROVED_DATE;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.CONTESTED_ORDER_APPROVED_JUDGE_NAME;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.CONTESTED_ORDER_APPROVED_JUDGE_TYPE;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.DRAFT_DIRECTION_DETAILS_COLLECTION;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.DRAFT_DIRECTION_DETAILS_COLLECTION_RO;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.LATEST_DRAFT_DIRECTION_ORDER;

@RestController
@RequestMapping(value = "/case-orchestration")
@RequiredArgsConstructor
@Slf4j
public class UploadApprovedOrderController extends BaseController {

    private final CaseDataService caseDataService;

    @PostMapping(path = "/approved-order/start", consumes = APPLICATION_JSON_VALUE, produces = APPLICATION_JSON_VALUE)
    @ApiOperation(value = "Cleans data before event that stores hearing order")
    @ApiResponses(value = {
        @ApiResponse(code = 200, message = "Callback was processed successfully or in case of an error message is attached to the case",
            response = AboutToStartOrSubmitCallbackResponse.class),
        @ApiResponse(code = 400, message = "Bad Request"),
        @ApiResponse(code = 500, message = "Internal Server Error")})
    public ResponseEntity<AboutToStartOrSubmitCallbackResponse> startHearingOrder(
        @RequestHeader(value = AUTHORIZATION_HEADER) String authorisationToken,
        @NotNull @RequestBody @ApiParam("CaseData") CallbackRequest callback) {
        CaseDetails caseDetails = callback.getCaseDetails();
        validateCaseData(callback);

        prepareFieldsForOrderApprovedCoverLetter(caseDetails, authorisationToken);

        Map<String, Object> caseData = caseDetails.getData();
        Optional<DraftDirectionOrder> draftDirectionOrderCollectionTail = hearingOrderService.draftDirectionOrderCollectionTail(caseDetails);
        if (draftDirectionOrderCollectionTail.isPresent()) {
            caseData.put(LATEST_DRAFT_DIRECTION_ORDER, draftDirectionOrderCollectionTail.get());
        } else {
            caseData.remove(LATEST_DRAFT_DIRECTION_ORDER);
        }

        return ResponseEntity.ok(AboutToStartOrSubmitCallbackResponse.builder().data(caseData).build());
    }

    @PostMapping(path = "/approved-order/store", consumes = APPLICATION_JSON_VALUE, produces = APPLICATION_JSON_VALUE)
    @ApiOperation(value = "Stores approved hearing order")
    @ApiResponses(value = {
        @ApiResponse(code = 200, message = "Callback was processed successfully or in case of an error message is attached to the case",
            response = AboutToStartOrSubmitCallbackResponse.class),
        @ApiResponse(code = 400, message = "Bad Request"),
        @ApiResponse(code = 500, message = "Internal Server Error")})
    public ResponseEntity<AboutToStartOrSubmitCallbackResponse> storeApprovedHearingOrder(
        @RequestHeader(value = AUTHORIZATION_HEADER) String authorisationToken,
        @NotNull @RequestBody @ApiParam("CaseData") CallbackRequest callback) {
        CaseDetails caseDetails = callback.getCaseDetails();
        validateCaseData(callback);

        Map<String, Object> caseData = caseDetails.getData();

        hearingOrderService.convertToPdfAndStampAndStoreLatestDraftHearingOrder(caseDetails, authorisationToken);
        contestedOrderApprovedLetterService.generateAndStoreContestedOrderApprovedLetter(caseDetails, authorisationToken);
        caseDataService.moveCollection(caseData, DRAFT_DIRECTION_DETAILS_COLLECTION, DRAFT_DIRECTION_DETAILS_COLLECTION_RO);

        if (hearingOrderService.latestDraftDirectionOrderOverridesSolicitorCollection(caseDetails)) {
            hearingOrderService.appendLatestDraftDirectionOrderToJudgesAmendedDirectionOrders(caseDetails);
        }

        caseData.remove(LATEST_DRAFT_DIRECTION_ORDER);

        return ResponseEntity.ok(AboutToStartOrSubmitCallbackResponse.builder().data(caseData).build());
    }

    private void prepareFieldsForOrderApprovedCoverLetter(CaseDetails caseDetails, String authorisationToken) {
        Map<String, Object> caseData = caseDetails.getData();
        caseData.remove(CONTESTED_ORDER_APPROVED_JUDGE_TYPE);
        //need another way of getting the judges' name
        caseData.put(CONTESTED_ORDER_APPROVED_JUDGE_NAME, "PLACEHOLDER_JUDGE_NAME");
        caseData.remove(CONTESTED_ORDER_APPROVED_DATE);
    }

}
