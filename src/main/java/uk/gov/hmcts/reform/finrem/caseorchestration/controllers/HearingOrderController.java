package uk.gov.hmcts.reform.finrem.caseorchestration.controllers;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import jakarta.validation.constraints.NotNull;
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
import uk.gov.hmcts.reform.finrem.caseorchestration.mapper.FinremCaseDetailsMapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.DraftDirectionOrder;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.CaseDataService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.ContestedOrderApprovedLetterService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.HearingOrderService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.IdamService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.documentcatergory.UploadedDraftOrderCategoriser;

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
public class HearingOrderController extends BaseController {

    private final HearingOrderService hearingOrderService;
    private final ContestedOrderApprovedLetterService contestedOrderApprovedLetterService;
    private final IdamService idamService;
    private final CaseDataService caseDataService;
    private final FinremCaseDetailsMapper finremCaseDetailsMapper;
    private final UploadedDraftOrderCategoriser uploadedDraftOrderCategoriser;

    @PostMapping(path = "/hearing-order/start", consumes = APPLICATION_JSON_VALUE, produces = APPLICATION_JSON_VALUE)
    @Operation(summary = "Cleans data before event that stores hearing order")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Callback was processed successfully or in case of an error message is attached to the case",
            content = {@Content(mediaType = "application/json", schema = @Schema(implementation = AboutToStartOrSubmitCallbackResponse.class))}),
        @ApiResponse(responseCode = "400", description = "Bad Request"),
        @ApiResponse(responseCode = "500", description = "Internal Server Error")})
    public ResponseEntity<AboutToStartOrSubmitCallbackResponse> startHearingOrder(
        @RequestHeader(value = AUTHORIZATION_HEADER) String authorisationToken,
        @NotNull @RequestBody @Parameter(description = "CaseData") CallbackRequest callback) {
        CaseDetails caseDetails = callback.getCaseDetails();
        log.info("Received request to start event storing hearing order for Case ID: {}", caseDetails.getId());

        prepareFieldsForOrderApprovedCoverLetter(caseDetails, authorisationToken);

        return ResponseEntity.ok(AboutToStartOrSubmitCallbackResponse.builder().data(caseDetails.getData()).build());
    }

    @PostMapping(path = "/hearing-order/approval-start", consumes = APPLICATION_JSON_VALUE, produces = APPLICATION_JSON_VALUE)
    @Operation(summary = "Starts hearing order approval")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Callback was processed successfully or in case of an error message is attached to the case",
            content = {@Content(mediaType = "application/json", schema = @Schema(implementation = AboutToStartOrSubmitCallbackResponse.class))}),
        @ApiResponse(responseCode = "400", description = "Bad Request"),
        @ApiResponse(responseCode = "500", description = "Internal Server Error")})
    public ResponseEntity<AboutToStartOrSubmitCallbackResponse> startHearingOrderApproval(
        @RequestHeader(value = AUTHORIZATION_HEADER) String authorisationToken,
        @NotNull @RequestBody @Parameter(description = "CaseData") CallbackRequest callback) {
        CaseDetails caseDetails = callback.getCaseDetails();
        validateCaseData(callback);

        log.info("Received Draft Approved Order about to start callback for Case ID: {}", caseDetails.getId());

        prepareFieldsForOrderApprovedCoverLetter(caseDetails, authorisationToken);

        Map<String, Object> caseData = caseDetails.getData();
        Optional<DraftDirectionOrder> draftDirectionOrderCollectionTail
            = hearingOrderService.draftDirectionOrderCollectionTail(caseDetails, authorisationToken);
        if (draftDirectionOrderCollectionTail.isPresent()) {
            caseData.put(LATEST_DRAFT_DIRECTION_ORDER, draftDirectionOrderCollectionTail.get());
        } else {
            caseData.remove(LATEST_DRAFT_DIRECTION_ORDER);
        }

        return ResponseEntity.ok(AboutToStartOrSubmitCallbackResponse.builder().data(caseData).build());
    }

    @PostMapping(path = "/hearing-order/approval-store", consumes = APPLICATION_JSON_VALUE, produces = APPLICATION_JSON_VALUE)
    @Operation(summary = "Stores approved hearing order")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Callback was processed successfully or in case of an error message is attached to the case",
            content = {@Content(mediaType = "application/json", schema = @Schema(implementation = AboutToStartOrSubmitCallbackResponse.class))}),
        @ApiResponse(responseCode = "400", description = "Bad Request"),
        @ApiResponse(responseCode = "500", description = "Internal Server Error")})
    public ResponseEntity<AboutToStartOrSubmitCallbackResponse> storeApprovedHearingOrder(
        @RequestHeader(value = AUTHORIZATION_HEADER) String authorisationToken,
        @NotNull @RequestBody @Parameter(description = "CaseData") CallbackRequest callback) {
        CaseDetails caseDetails = callback.getCaseDetails();
        validateCaseData(callback);

        log.info("Received Draft Approved Order about to submit callback for Case ID: {}", caseDetails.getId());

        Map<String, Object> caseData = caseDetails.getData();

        hearingOrderService.convertToPdfAndStampAndStoreLatestDraftHearingOrder(caseDetails, authorisationToken);
        contestedOrderApprovedLetterService.generateAndStoreContestedOrderApprovedLetter(caseDetails, authorisationToken);
        caseDataService.moveCollection(caseData, DRAFT_DIRECTION_DETAILS_COLLECTION, DRAFT_DIRECTION_DETAILS_COLLECTION_RO);

        if (hearingOrderService.latestDraftDirectionOrderOverridesSolicitorCollection(caseDetails, authorisationToken)) {
            hearingOrderService.appendLatestDraftDirectionOrderToJudgesAmendedDirectionOrders(caseDetails);
        }

        FinremCaseDetails finremCaseDetails = finremCaseDetailsMapper.mapToFinremCaseDetails(caseDetails);
        uploadedDraftOrderCategoriser.categorise(finremCaseDetails.getData());
        CaseDetails caseDetailsToReturn = finremCaseDetailsMapper.mapToCaseDetails(finremCaseDetails);

        caseDetailsToReturn.getData().remove(LATEST_DRAFT_DIRECTION_ORDER);

        return ResponseEntity.ok(AboutToStartOrSubmitCallbackResponse.builder().data(caseDetailsToReturn.getData()).build());
    }

    private void prepareFieldsForOrderApprovedCoverLetter(CaseDetails caseDetails, String authorisationToken) {
        Map<String, Object> caseData = caseDetails.getData();
        caseData.remove(CONTESTED_ORDER_APPROVED_JUDGE_TYPE);
        caseData.put(CONTESTED_ORDER_APPROVED_JUDGE_NAME, idamService.getIdamFullName(authorisationToken));
        caseData.remove(CONTESTED_ORDER_APPROVED_DATE);
    }
}
