package uk.gov.hmcts.reform.finrem.caseorchestration.controllers;

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
import uk.gov.hmcts.reform.finrem.caseorchestration.service.ConsentOrderApprovedDocumentService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.FeatureToggleService;

import javax.validation.constraints.NotNull;

import java.util.List;
import java.util.Map;

import static java.util.Arrays.asList;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;
import static org.springframework.util.CollectionUtils.isEmpty;
import static org.springframework.util.ObjectUtils.isEmpty;
import static uk.gov.hmcts.reform.finrem.caseorchestration.OrchestrationConstants.AUTHORIZATION_HEADER;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.APPROVED_ORDER_COLLECTION;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.LATEST_CONSENT_ORDER;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.PENSION_DOCS_COLLECTION;
import static uk.gov.hmcts.reform.finrem.caseorchestration.service.CommonFunction.isPaperApplication;

@Slf4j
@RestController
@RequestMapping(value = "/case-orchestration")
@RequiredArgsConstructor
public class ConsentOrderApprovedController implements BaseController {

    private final ConsentOrderApprovedDocumentService service;
    private final FeatureToggleService featureToggleService;

    private ObjectMapper mapper = new ObjectMapper();

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

        log.info("ConsentOrderApprovedController called");

        validateCaseData(callback);
        CaseDetails caseDetails = callback.getCaseDetails();
        Map<String, Object> caseData = caseDetails.getData();
        CaseDocument latestConsentOrder = getLatestConsentOrder(caseData);
        List<PensionCollectionData> pensionDocs = getPensionDocuments(caseData);

        if (!isEmpty(latestConsentOrder)) {
            log.info("Latest consent order is not empty");

            CaseDocument approvedConsentOrderLetter = service.generateApprovedConsentOrderLetter(caseDetails, authToken);
            CaseDocument consentOrderAnnexStamped = service.annexStampDocument(latestConsentOrder, authToken);
            CaseDocument approvedConsentOrderNotificationLetter = null;

            log.info("isApprovedConsentOrderNotificationLetterEnabled toggle set to: {}",
                featureToggleService.isApprovedConsentOrderNotificationLetterEnabled());

            log.info("isPaperApplication set to: {}", isPaperApplication(caseData));

            ApprovedOrder.ApprovedOrderBuilder approvedOrderBuilder = ApprovedOrder.builder()
                .orderLetter(approvedConsentOrderLetter)
                .consentOrder(consentOrderAnnexStamped);

            if (featureToggleService.isApprovedConsentOrderNotificationLetterEnabled()) {
                log.info("isApprovedConsentOrderNotificationLetterEnabled is toggled on");

                if (isPaperApplication(caseData)) {
                    approvedConsentOrderNotificationLetter = service.generateApprovedConsentOrderNotificationLetter(caseDetails, authToken);

                    log.info("consentNotificationLetter= {}, letter= {}, consentOrderAnnexStamped = {}",
                        approvedConsentOrderNotificationLetter, approvedConsentOrderLetter, consentOrderAnnexStamped);

                    log.info("Adding approvedConsentOrderNotificationLetter to approvedOrderBuilder");
                    approvedOrderBuilder.consentOrderApprovedNotificationLetter(approvedConsentOrderNotificationLetter);
                }
            }

            ApprovedOrder approvedOrder = approvedOrderBuilder.build();

            if (!isEmpty(pensionDocs)) {
                log.info("Pension Documents not empty for case - stamping Pension Documents and adding to approvedOrder");

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
