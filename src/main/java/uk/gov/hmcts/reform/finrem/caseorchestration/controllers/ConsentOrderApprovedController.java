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
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.ApprovedOrder;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.ApprovedOrderData;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseDocument;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.PensionCollectionData;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.ConsentOrderApprovedDocumentService;

import javax.validation.constraints.NotNull;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static java.util.Arrays.asList;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;
import static org.springframework.util.CollectionUtils.isEmpty;
import static org.springframework.util.ObjectUtils.isEmpty;
import static uk.gov.hmcts.reform.finrem.caseorchestration.OrchestrationConstants.AUTHORIZATION_HEADER;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.APPROVED_ORDER_COLLECTION;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.BULK_PRINT_COVER_SHEET;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.BULK_PRINT_COVER_SHEET_APP;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.BULK_PRINT_LETTER_ID_APP;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.CONSENT_ORDER_APPROVED_NOTIFICATION_LETTER;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.LATEST_CONSENT_ORDER;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.PENSION_DOCS_COLLECTION;

@Slf4j
@RestController
@RequestMapping(value = "/case-orchestration")
public class ConsentOrderApprovedController implements BaseController {
    private ObjectMapper mapper = new ObjectMapper();

    @Autowired
    private ConsentOrderApprovedDocumentService service;

    @PostMapping(path = "/documents/consent-order-approved", consumes = APPLICATION_JSON_VALUE, produces = APPLICATION_JSON_VALUE)
    @ApiOperation(value = "Handles 'Consent Order Approved' generation. Serves as a callback from CCD")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Callback was processed successfully or in case of an error message is attached to the case",
                    response = AboutToStartOrSubmitCallbackResponse.class),
            @ApiResponse(code = 400, message = "Bad Request"),
            @ApiResponse(code = 500, message = "Internal Server Error")
        })

    public ResponseEntity<AboutToStartOrSubmitCallbackResponse> consentOrderApproved(
            @RequestHeader(value = AUTHORIZATION_HEADER) String authToken,
            @NotNull @RequestBody @ApiParam("CaseData") CallbackRequest callback) {

        log.info("ConsentOrderApprovedController called");

        validateCaseData(callback);
        CaseDetails caseDetails = callback.getCaseDetails();
        Map<String, Object> caseData = caseDetails.getData();
        CaseDocument latestConsentOrder = getLatestConsentOrder(caseData);
        List<PensionCollectionData> pensionDocs = getPensionDocuments(caseData);

        if (!isEmpty(latestConsentOrder)) {
            CaseDocument approvedConsentOrderNotificationLetter = service.generateApprovedConsentOrderNotificationLetter(caseDetails, authToken);
            CaseDocument approvedConsentOrderLetter = service.generateApprovedConsentOrderLetter(caseDetails, authToken);
            CaseDocument consentOrderAnnexStamped = service.annexStampDocument(latestConsentOrder, authToken);

            log.info("consentNotificationLetter= {}, letter= {}, consentOrderAnnexStamped = {}",
                    approvedConsentOrderNotificationLetter, approvedConsentOrderLetter, consentOrderAnnexStamped);

            ApprovedOrder approvedOrder = ApprovedOrder.builder()
                    .consentOrderApprovedNotificationLetter(approvedConsentOrderNotificationLetter)
                    .orderLetter(approvedConsentOrderLetter)
                    .consentOrder(consentOrderAnnexStamped)
                    .build();

            if (!isEmpty(pensionDocs)) {
                List<PensionCollectionData> stampedPensionDocs = service.stampPensionDocuments(pensionDocs, authToken);
                log.info("Generated StampedPensionDocs = {}", stampedPensionDocs);
                approvedOrder.setPensionDocuments(stampedPensionDocs);
            }

            ApprovedOrderData approvedOrderData = ApprovedOrderData.builder()
                    .approvedOrder(approvedOrder)
                    .build();
            log.info("Generated ApprovedOrderData = {}", approvedOrderData);

            List<ApprovedOrderData> approvedOrders = asList(approvedOrderData);
            caseData.put(APPROVED_ORDER_COLLECTION, approvedOrders);

            cleanupCaseDataBeforeSubmittingToCcd(caseDetails);

            log.info("Successfully generated documents for 'Consent Order Approved'");
        }

        return ResponseEntity.ok(
                AboutToStartOrSubmitCallbackResponse.builder()
                        .data(caseData)
                        .errors(ImmutableList.of())
                        .warnings(ImmutableList.of())
                        .build());
    }

    private void cleanupCaseDataBeforeSubmittingToCcd(CaseDetails caseDetails) {
        // Must remove any added case data as CCD will return an error
        caseDetails.getData().remove("caseNumber");
        caseDetails.getData().remove("reference");
        caseDetails.getData().remove("addressee");
        caseDetails.getData().remove("letterDate");
        caseDetails.getData().remove("applicantName");
        caseDetails.getData().remove("respondentName");
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
