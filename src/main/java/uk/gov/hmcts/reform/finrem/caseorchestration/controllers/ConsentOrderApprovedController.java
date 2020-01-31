package uk.gov.hmcts.reform.finrem.caseorchestration.controllers;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.ImmutableList;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
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
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.ApprovedOrder;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.ApprovedOrderData;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseDocument;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.PensionCollectionData;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.ConsentOrderApprovedDocumentService;

import javax.validation.constraints.NotNull;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;
import static org.springframework.util.CollectionUtils.isEmpty;
import static org.springframework.util.ObjectUtils.isEmpty;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.APPROVED_ORDER_COLLECTION;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.LATEST_CONSENT_ORDER;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.PENSION_DOCS_COLLECTION;

@Slf4j
@RestController
@RequestMapping(value = "/case-orchestration")
public class ConsentOrderApprovedController implements BaseController {
    private ObjectMapper mapper = new ObjectMapper();

    @Autowired
    private ConsentOrderApprovedDocumentService service;

    @PostMapping(path = "/documents/consent-order-approved", consumes = APPLICATION_JSON_VALUE,
            produces = APPLICATION_JSON_VALUE)
    @ApiOperation(value = "Handles Consent order approved generation. Serves as a callback from CCD")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Callback was processed successfully or in case of an error message is "
                    + "attached to the case",
                    response = AboutToStartOrSubmitCallbackResponse.class),
            @ApiResponse(code = 400, message = "Bad Request"),
            @ApiResponse(code = 500, message = "Internal Server Error")
        })

    public ResponseEntity<AboutToStartOrSubmitCallbackResponse> consentOrderApproved(
            @RequestHeader(value = "Authorization") String authToken,
            @NotNull @RequestBody @ApiParam("CaseData") CallbackRequest callback) {

        validateCaseData(callback);
        Map<String, Object> caseData = callback.getCaseDetails().getData();
        CaseDocument latestConsentOrder = getLatestConsentOrder(caseData);
        List<PensionCollectionData> pensionDocs = getPensionDocuments(caseData);

        log.info("ConsentOrderApprovedController called with latestConsentOrder = {}, pensionDocs = {}",
                latestConsentOrder, pensionDocs);

        if (!isEmpty(latestConsentOrder)) {
            CaseDocument letter = service.generateApprovedConsentOrderLetter(
                    callback.getCaseDetails(), authToken);
            CaseDocument consentOrderAnnexStamped = service.annexStampDocument(latestConsentOrder, authToken);

            log.info("letter= {}, consentOrderAnnexStamped = {}", letter, consentOrderAnnexStamped);

            ApprovedOrder approvedOrder = ApprovedOrder.builder()
                .orderLetter(letter)
                .consentOrder(consentOrderAnnexStamped)
                .build();

            if (!isEmpty(pensionDocs)) {
                List<PensionCollectionData> stampedPensionDocs = service.stampPensionDocuments(pensionDocs, authToken);
                log.info(" stampedPensionDocs = {}", stampedPensionDocs);
                approvedOrder.setPensionDocuments(stampedPensionDocs);
            }

            ApprovedOrderData approvedOrderData = ApprovedOrderData.builder()
                    .approvedOrder(approvedOrder)
                    .build();
            log.info("approvedOrderData = {}", approvedOrderData);

            List<ApprovedOrderData> approvedOrders = Collections.singletonList(approvedOrderData);
            caseData.put(APPROVED_ORDER_COLLECTION, approvedOrders);
        }

        return ResponseEntity.ok(
            AboutToStartOrSubmitCallbackResponse.builder()
                .data(caseData)
                .errors(ImmutableList.of())
                .warnings(ImmutableList.of())
                .build());
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
}
