package uk.gov.hmcts.reform.finrem.caseorchestration.controllers;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.ImmutableList;
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
import uk.gov.hmcts.reform.finrem.caseorchestration.helper.DocumentHelper;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.ApprovedOrder;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseDocument;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CollectionElement;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.PensionTypeCollection;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.ConsentOrderApprovedDocumentService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.ConsentOrderPrintService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.GenericDocumentService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.NotificationService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.StampType;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.correspondence.consentorder.ConsentOrderAvailableCorresponder;

import javax.validation.constraints.NotNull;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static java.util.Collections.singletonList;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;
import static org.springframework.util.ObjectUtils.isEmpty;
import static uk.gov.hmcts.reform.finrem.caseorchestration.OrchestrationConstants.AUTHORIZATION_HEADER;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ConsentedStatus.CONSENT_ORDER_MADE;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.APPROVED_ORDER_COLLECTION;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.LATEST_CONSENT_ORDER;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.PENSION_DOCS_COLLECTION;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.STATE;

@Slf4j
@RestController
@RequestMapping(value = "/case-orchestration")
@RequiredArgsConstructor
public class ConsentOrderApprovedController extends BaseController {

    private final ConsentOrderApprovedDocumentService consentOrderApprovedDocumentService;
    private final GenericDocumentService genericDocumentService;
    private final ConsentOrderPrintService consentOrderPrintService;
    private final NotificationService notificationService;
    private final DocumentHelper documentHelper;
    private final ObjectMapper mapper;
    private final ConsentOrderAvailableCorresponder consentOrderAvailableCorresponder;

    @PostMapping(path = "/documents/consent-order-approved", consumes = APPLICATION_JSON_VALUE, produces = APPLICATION_JSON_VALUE)
    @Operation(summary = "'Consent Order Approved' callback handler. Generates relevant Consent Order Approved documents")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200",
            description = "Callback was processed successfully or in case of an error message is attached to the case",
            content = {@Content(mediaType = "application/json", schema = @Schema(implementation = AboutToStartOrSubmitCallbackResponse.class))}),
        @ApiResponse(responseCode = "400", description = "Bad Request"),
        @ApiResponse(responseCode = "500", description = "Internal Server Error")})
    public ResponseEntity<AboutToStartOrSubmitCallbackResponse> consentOrderApproved(
        @RequestHeader(value = AUTHORIZATION_HEADER) String authToken,
        @NotNull @RequestBody @Parameter(description = "CaseData") CallbackRequest callback) {

        validateCaseData(callback);
        CaseDetails caseDetails = callback.getCaseDetails();
        CaseDocument latestConsentOrder = getLatestConsentOrder(caseDetails.getData());

        if (!isEmpty(latestConsentOrder)) {
            generateAndPrepareDocuments(authToken, caseDetails);
        } else {
            log.info("Failed to handle 'Consent Order Approved' callback because 'latestConsentOrder' is empty for case: {}",
                caseDetails.getId());
        }

        return ResponseEntity.ok(
            AboutToStartOrSubmitCallbackResponse.builder()
                .data(caseDetails.getData())
                .errors(ImmutableList.of())
                .warnings(ImmutableList.of())
                .build());
    }

    @PostMapping(path = "/consent-in-contested/consent-order-approved", consumes = APPLICATION_JSON_VALUE, produces = APPLICATION_JSON_VALUE)
    @Operation(summary = "'Consent Order Approved' callback handler for consent in contested. Stamps Consent Order Approved documents\"\n"
        + "        + \"and adds them to a collection")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200",
            description = "Callback was processed successfully or in case of an error message is attached to the case",
            content = {@Content(mediaType = "application/json", schema = @Schema(implementation = AboutToStartOrSubmitCallbackResponse.class))}),
        @ApiResponse(responseCode = "400", description = "Bad Request"),
        @ApiResponse(responseCode = "500", description = "Internal Server Error")})
    public ResponseEntity<AboutToStartOrSubmitCallbackResponse> consentInContestedConsentOrderApproved(
        @RequestHeader(value = AUTHORIZATION_HEADER) String authToken,
        @NotNull @RequestBody @Parameter(description = "CaseData") CallbackRequest callback) {
        validateCaseData(callback);
        CaseDetails caseDetails = callback.getCaseDetails();
        Map<String, Object> caseData = caseDetails.getData();

        consentOrderApprovedDocumentService.stampAndPopulateContestedConsentApprovedOrderCollection(caseData,
                authToken, caseDetails.getId().toString());
        consentOrderApprovedDocumentService.generateAndPopulateConsentOrderLetter(caseDetails, authToken);

        return ResponseEntity.ok(
            AboutToStartOrSubmitCallbackResponse.builder()
                .data(caseData)
                .errors(ImmutableList.of())
                .warnings(ImmutableList.of())
                .build());
    }

    @PostMapping(path = "/consent-in-contested/send-order", consumes = APPLICATION_JSON_VALUE, produces = APPLICATION_JSON_VALUE)
    @Operation(summary = "'Consent Order Approved' and 'Consent Order Not Approved' callback handler for consent in contested. \"\n"
        + "        + \"Checks state and if not/approved generates docs else puts latest general order into uploadOrder fields. \"\n"
        + "        + \"Then sends the data to bulk print")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200",
            description = "Callback was processed successfully or in case of an error message is attached to the case",
            content = {@Content(mediaType = "application/json", schema = @Schema(implementation = AboutToStartOrSubmitCallbackResponse.class))}),
        @ApiResponse(responseCode = "400", description = "Bad Request"),
        @ApiResponse(responseCode = "500", description = "Internal Server Error")})
    public ResponseEntity<AboutToStartOrSubmitCallbackResponse> consentInContestedSendOrder(
        @RequestHeader(value = AUTHORIZATION_HEADER) String authToken,
        @NotNull @RequestBody @Parameter(description = "CaseData") CallbackRequest callback) {
        CaseDetails caseDetails = callback.getCaseDetails();

        consentOrderPrintService.sendConsentOrderToBulkPrint(caseDetails, authToken);

        return ResponseEntity.ok(AboutToStartOrSubmitCallbackResponse.builder()
            .data(caseDetails.getData())
            .build());
    }

    private void generateAndPrepareDocuments(String authToken, CaseDetails caseDetails) {
        String caseId = caseDetails.getId().toString();
        log.info("Generating and preparing documents for latest consent order, case {}", caseId);

        Map<String, Object> caseData = caseDetails.getData();
        CaseDocument latestConsentOrder = getLatestConsentOrder(caseData);
        StampType stampType = documentHelper.getStampType(caseData);
        CaseDocument approvedConsentOrderLetter = consentOrderApprovedDocumentService.generateApprovedConsentOrderLetter(caseDetails, authToken);
        CaseDocument consentOrderAnnexStamped = genericDocumentService.annexStampDocument(latestConsentOrder, authToken, stampType, caseId);

        ApprovedOrder.ApprovedOrderBuilder approvedOrderBuilder = ApprovedOrder.builder()
            .orderLetter(approvedConsentOrderLetter)
            .consentOrder(consentOrderAnnexStamped);

        ApprovedOrder approvedOrder = approvedOrderBuilder.build();

        if (!documentHelper.getPensionDocumentsData(caseData).isEmpty()) {
            log.info("Pension Documents not empty for case - stamping Pension Documents and adding to approvedOrder for case {}",
                caseId);

            List<PensionTypeCollection> pensionDocList = documentHelper.getPensionDocuments(caseData);
            List<PensionTypeCollection> stampedPensionDocs = consentOrderApprovedDocumentService.stampPensionDocuments(
                pensionDocList, authToken, stampType, caseId);
            log.info("Generated StampedPensionDocs = {} for case {}", stampedPensionDocs, caseDetails.getId());
            approvedOrder.setPensionDocuments(stampedPensionDocs);
        }

        List<CollectionElement<ApprovedOrder>> approvedOrders = singletonList(CollectionElement.<ApprovedOrder>builder()
            .value(approvedOrder).build());
        log.info("Generated ApprovedOrders = {} for case {}", approvedOrders, caseId);

        caseData.put(APPROVED_ORDER_COLLECTION, approvedOrders);

        log.info("Successfully generated documents for 'Consent Order Approved' for case {}", caseId);

        if (documentHelper.getPensionDocumentsData(caseData).isEmpty()) {
            log.info("Case {} has no pension documents, updating status to {} and sending for bulk print",
                caseId,
                CONSENT_ORDER_MADE.toString());
            try {
                // Render Case Data with @JSONProperty names, required to re-use sendToBulkPrint code
                caseData = mapper.readValue(mapper.writeValueAsString(caseData), HashMap.class);
                caseDetails.setData(caseData);
                consentOrderPrintService.sendConsentOrderToBulkPrint(caseDetails, authToken);
                caseData.put(STATE, CONSENT_ORDER_MADE.toString());
                notificationService.sendConsentOrderAvailableCtscEmail(caseDetails);
                consentOrderAvailableCorresponder.sendCorrespondence(caseDetails);

            } catch (JsonProcessingException e) {
                log.error("case - {}: Error encountered trying to update status and send for bulk print: {}", caseId, e.getMessage());
            }
        }
    }

    private CaseDocument getLatestConsentOrder(Map<String, Object> caseData) {
        return mapper.convertValue(caseData.get(LATEST_CONSENT_ORDER), new TypeReference<>() {
        });
    }

    private List<PensionTypeCollection> getPensionDocuments(Map<String, Object> caseData) {
        return mapper.convertValue(caseData.get(PENSION_DOCS_COLLECTION), new TypeReference<>() {
        });
    }
}
