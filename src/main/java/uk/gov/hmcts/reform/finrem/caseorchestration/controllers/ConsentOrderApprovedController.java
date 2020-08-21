package uk.gov.hmcts.reform.finrem.caseorchestration.controllers;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.ImmutableList;
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
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.ApprovedOrder;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.ApprovedOrderData;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseDocument;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.PensionCollectionData;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.BulkPrintService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.ConsentOrderApprovedDocumentService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.FeatureToggleService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.GenericDocumentService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.NotificationService;

import javax.validation.constraints.NotNull;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static java.util.Arrays.asList;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;
import static org.springframework.util.CollectionUtils.isEmpty;
import static org.springframework.util.ObjectUtils.isEmpty;
import static uk.gov.hmcts.reform.finrem.caseorchestration.OrchestrationConstants.AUTHORIZATION_HEADER;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ConsentedStatus.CONSENT_ORDER_MADE;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.APPROVED_ORDER_COLLECTION;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.CONSENTED_ORDER_APPROVED;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.CONTESTED_CONSENT_ORDER_COLLECTION;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.GENERAL_ORDER_LATEST_DOCUMENT;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.LATEST_CONSENT_ORDER;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.PENSION_DOCS_COLLECTION;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.STATE;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.UPLOAD_ORDER;

@Slf4j
@RestController
@RequestMapping(value = "/case-orchestration")
@RequiredArgsConstructor
public class ConsentOrderApprovedController implements BaseController {

    private final ConsentOrderApprovedDocumentService consentOrderApprovedDocumentService;
    private final GenericDocumentService genericDocumentService;
    private final ObjectMapper mapper;
    private final BulkPrintService bulkPrintService;
    private final FeatureToggleService featureToggleService;
    private final NotificationService notificationService;

    @PostMapping(path = "/documents/consent-order-approved", consumes = APPLICATION_JSON_VALUE, produces = APPLICATION_JSON_VALUE)
    @ApiOperation(value = "'Consent Order Approved' callback handler. Generates relevant Consent Order Approved documents")
    @ApiResponses(value = {
        @ApiResponse(code = 200, message = "Callback was processed successfully or in case of an error message is attached to the case",
            response = AboutToStartOrSubmitCallbackResponse.class),
        @ApiResponse(code = 400, message = "Bad Request"),
        @ApiResponse(code = 500, message = "Internal Server Error")
    })
    public ResponseEntity<AboutToStartOrSubmitCallbackResponse> consentOrderApproved(
        @RequestHeader(value = AUTHORIZATION_HEADER) String authToken,
        @NotNull @RequestBody @ApiParam("CaseData") CallbackRequest callback) {

        validateCaseData(callback);
        CaseDetails caseDetails = callback.getCaseDetails();
        Map<String, Object> caseData = caseDetails.getData();
        CaseDocument latestConsentOrder = getLatestConsentOrder(caseData);

        if (!isEmpty(latestConsentOrder)) {
            caseData = generateAndPrepareDocuments(authToken, callback);
        } else {
            log.info("Failed to handle 'Consent Order Approved' callback because 'latestConsentOrder' is empty");
        }

        return ResponseEntity.ok(
            AboutToStartOrSubmitCallbackResponse.builder()
                .data(caseData)
                .errors(ImmutableList.of())
                .warnings(ImmutableList.of())
                .build());
    }

    @PostMapping(path = "/consent-in-contested/consent-order-approved", consumes = APPLICATION_JSON_VALUE, produces = APPLICATION_JSON_VALUE)
    @ApiOperation(value = "'Consent Order Approved' callback handler for consent in contetested. Stamps Consent Order Approved documents"
        + "and adds them to a collection")
    @ApiResponses(value = {
        @ApiResponse(code = 200, message = "Callback was processed successfully or in case of an error message is attached to the case",
            response = AboutToStartOrSubmitCallbackResponse.class),
        @ApiResponse(code = 400, message = "Bad Request"),
        @ApiResponse(code = 500, message = "Internal Server Error")
    })
    public ResponseEntity<AboutToStartOrSubmitCallbackResponse> consentInContestedConsentOrderApproved(
        @RequestHeader(value = AUTHORIZATION_HEADER) String authToken,
        @NotNull @RequestBody @ApiParam("CaseData") CallbackRequest callback) {
        validateCaseData(callback);
        CaseDetails caseDetails = callback.getCaseDetails();

        Map<String, Object> caseData = consentOrderApprovedDocumentService
            .stampAndPopulateContestedConsentApprovedOrderCollection(caseDetails.getData(), authToken);

        return ResponseEntity.ok(
            AboutToStartOrSubmitCallbackResponse.builder()
                .data(caseData)
                .errors(ImmutableList.of())
                .warnings(ImmutableList.of())
                .build());
    }

    @PostMapping(path = "/consent-in-contested/send-order", consumes = APPLICATION_JSON_VALUE, produces = APPLICATION_JSON_VALUE)
    @ApiOperation(value = "'Consent Order Approved' callback handler for consent in contested. Checks state and if "
        + "approved generates docs else puts latest general order into uploadORder fields. Then sends the data to bulk print")
    @ApiResponses(value = {
        @ApiResponse(code = 200, message = "Callback was processed successfully or in case of an error message is attached to the case",
            response = AboutToStartOrSubmitCallbackResponse.class),
        @ApiResponse(code = 400, message = "Bad Request"),
        @ApiResponse(code = 500, message = "Internal Server Error")
    })
    public ResponseEntity<AboutToStartOrSubmitCallbackResponse> consentInContestedSendOrder(
        @RequestHeader(value = AUTHORIZATION_HEADER) String authToken,
        @NotNull @RequestBody @ApiParam("CaseData") CallbackRequest callback) {
        CaseDetails caseDetails = callback.getCaseDetails();
        Map<String, Object> caseData = caseDetails.getData();

        if (caseDetails.getState().equals(CONSENTED_ORDER_APPROVED)) {
            //generate consent in contested approval document
            CaseDocument orderLetter = consentOrderApprovedDocumentService.generateApprovedConsentOrderLetter(caseDetails, authToken);
            log.warn("generated letter binary url: {}", orderLetter.getDocumentBinaryUrl());
            //add generated doc to case data
            List<ApprovedOrderData> approvedOrderList = getConsentInContestedApprovedOrderCollection(caseData);
            log.warn("order list size {}", approvedOrderList.size());
            if (approvedOrderList != null && !approvedOrderList.isEmpty()) {
                ApprovedOrder approvedOrder = approvedOrderList.get(0).getApprovedOrder();
                approvedOrder.setOrderLetter(orderLetter);
                caseData.put(CONTESTED_CONSENT_ORDER_COLLECTION, approvedOrderList.stream().map(order ->
                    mapper.convertValue(order, Map.class)).collect(Collectors.toList()));
                log.warn("set CONTESTED_CONSENT_ORDER_COLLECTION");
            }
        } else {
            caseData.put(UPLOAD_ORDER, caseData.get(GENERAL_ORDER_LATEST_DOCUMENT));
        }
        bulkPrintService.sendToBulkPrint(caseDetails, authToken);

        log.warn("DETAILS DATA {}", caseDetails);

        AboutToStartOrSubmitCallbackResponse response = AboutToStartOrSubmitCallbackResponse.builder()
            .data(caseDetails.getData())
            .build();

        log.warn("RESPONSE DATA {}", response.getData());

        return ResponseEntity.ok(response);
    }

    private Map<String, Object> generateAndPrepareDocuments(@RequestHeader(AUTHORIZATION_HEADER) String authToken, CallbackRequest callback) {
        log.info("Generating and preparing documents for latest consent order");

        CaseDetails caseDetails = callback.getCaseDetails();
        Map<String, Object> caseData = caseDetails.getData();
        CaseDocument latestConsentOrder = getLatestConsentOrder(caseData);
        List<PensionCollectionData> pensionDocs = getPensionDocuments(caseData);

        CaseDocument approvedConsentOrderLetter = consentOrderApprovedDocumentService.generateApprovedConsentOrderLetter(caseDetails, authToken);
        CaseDocument consentOrderAnnexStamped = genericDocumentService.annexStampDocument(latestConsentOrder, authToken);

        ApprovedOrder.ApprovedOrderBuilder approvedOrderBuilder = ApprovedOrder.builder()
            .orderLetter(approvedConsentOrderLetter)
            .consentOrder(consentOrderAnnexStamped);

        ApprovedOrder approvedOrder = approvedOrderBuilder.build();

        if (!isEmpty(pensionDocs)) {
            log.info("Pension Documents not empty for case - stamping Pension Documents and adding to approvedOrder");

            List<PensionCollectionData> stampedPensionDocs = consentOrderApprovedDocumentService.stampPensionDocuments(pensionDocs, authToken);
            log.info("Generated StampedPensionDocs = {}", stampedPensionDocs);
            approvedOrder.setPensionDocuments(stampedPensionDocs);
        }

        ApprovedOrderData approvedOrderData = ApprovedOrderData.builder()
            .approvedOrder(approvedOrder)
            .build();
        log.info("Generated ApprovedOrderData = {}", approvedOrderData);

        List<ApprovedOrderData> approvedOrders = asList(approvedOrderData);
        caseData.put(APPROVED_ORDER_COLLECTION, approvedOrders);

        log.info("Successfully generated documents for 'Consent Order Approved'");

        if (featureToggleService.isAutomateSendOrderEnabled() && isEmpty(pensionDocs)) {
            log.info("Case has no pension documents, updating status to {} and sending for bulk print", CONSENT_ORDER_MADE.toString());
            try {
                // Render Case Data with @JSONProperty names, required to re-use sendToBulkPrint code
                caseData = mapper.readValue(mapper.writeValueAsString(caseData), HashMap.class);
                caseDetails.setData(caseData);
                caseData = bulkPrintService.sendToBulkPrint(caseDetails, authToken);
                caseData.put(STATE, CONSENT_ORDER_MADE.toString());
                notificationService.sendConsentOrderAvailableCtscEmail(callback);
            } catch (JsonProcessingException e) {
                log.error(e.getMessage());
            }
        }

        return caseData;
    }

    private CaseDocument getLatestConsentOrder(Map<String, Object> caseData) {
        return mapper.convertValue(caseData.get(LATEST_CONSENT_ORDER),
            new TypeReference<CaseDocument>() {
            });
    }

    private List<PensionCollectionData> getPensionDocuments(Map<String, Object> caseData) {
        return mapper.convertValue(caseData.get(PENSION_DOCS_COLLECTION),
            new TypeReference<List<PensionCollectionData>>() {
            });
    }

    private List<ApprovedOrderData> getConsentInContestedApprovedOrderCollection(Map<String, Object> caseData) {
        return mapper.convertValue(caseData.get(CONTESTED_CONSENT_ORDER_COLLECTION), new TypeReference<List<ApprovedOrderData>>() {
        });
    }
}
