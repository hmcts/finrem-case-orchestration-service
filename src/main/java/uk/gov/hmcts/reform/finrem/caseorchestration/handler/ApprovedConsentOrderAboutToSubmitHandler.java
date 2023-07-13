package uk.gov.hmcts.reform.finrem.caseorchestration.handler;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.ccd.client.model.CallbackRequest;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.ccd.callback.CallbackType;
import uk.gov.hmcts.reform.finrem.caseorchestration.controllers.GenericAboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.finrem.caseorchestration.helper.DocumentHelper;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.EventType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.ApprovedOrder;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseDocument;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CollectionElement;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.PensionTypeCollection;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.ConsentOrderApprovedDocumentService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.ConsentOrderPrintService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.GenericDocumentService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.StampType;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static java.util.Collections.singletonList;
import static org.springframework.util.ObjectUtils.isEmpty;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ConsentedStatus.CONSENT_ORDER_MADE;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.APPROVED_ORDER_COLLECTION;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.LATEST_CONSENT_ORDER;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.STATE;

@Slf4j
@Service
@RequiredArgsConstructor
public class ApprovedConsentOrderAboutToSubmitHandler implements CallbackHandler<Map<String, Object>> {

    private final ConsentOrderApprovedDocumentService consentOrderApprovedDocumentService;
    private final GenericDocumentService genericDocumentService;
    private final ConsentOrderPrintService consentOrderPrintService;
    private final DocumentHelper documentHelper;
    private final ObjectMapper mapper;


    @Override
    public boolean canHandle(final CallbackType callbackType, final CaseType caseType,
                             final EventType eventType) {
        return CallbackType.ABOUT_TO_SUBMIT.equals(callbackType)
            && CaseType.CONSENTED.equals(caseType)
            && EventType.APPROVE_ORDER.equals(eventType);
    }

    @Override
    public GenericAboutToStartOrSubmitCallbackResponse<Map<String, Object>> handle(CallbackRequest callbackRequest,
                                                                                   String userAuthorisation) {
        CaseDetails caseDetails = callbackRequest.getCaseDetails();
        String caseId =  String.valueOf(caseDetails.getId());
        log.info("ConsentOrderApprovedAboutToSubmitHandle handle case Id {}", caseId);

        CaseDocument latestConsentOrder = getLatestConsentOrder(caseDetails.getData());
        if (!isEmpty(latestConsentOrder)) {
            CaseDocument pdfConsentOrder = genericDocumentService.convertDocumentIfNotPdfAlready(latestConsentOrder, userAuthorisation, caseId);
            caseDetails.getData().put(LATEST_CONSENT_ORDER, pdfConsentOrder);
            generateAndPrepareDocuments(userAuthorisation, caseDetails, pdfConsentOrder);
        } else {
            log.info("Failed to handle 'Consent Order Approved' callback because 'latestConsentOrder' is empty for case: {}",
                caseId);
        }
        return GenericAboutToStartOrSubmitCallbackResponse.<Map<String, Object>>builder().data(caseDetails.getData()).build();
    }

    private void generateAndPrepareDocuments(String authToken, CaseDetails caseDetails, CaseDocument latestConsentOrder) {
        String caseId = caseDetails.getId().toString();
        log.info("Generating and preparing documents for latest consent order, case {}", caseId);

        Map<String, Object> caseData = caseDetails.getData();
        StampType stampType = documentHelper.getStampType(caseData);
        CaseDocument approvedConsentOrderLetter = consentOrderApprovedDocumentService.generateApprovedConsentOrderLetter(caseDetails, authToken);
        CaseDocument consentOrderAnnexStamped = genericDocumentService.annexStampDocument(latestConsentOrder, authToken, stampType, caseId);

        ApprovedOrder.ApprovedOrderBuilder approvedOrderBuilder = ApprovedOrder.builder()
            .orderLetter(approvedConsentOrderLetter)
            .consentOrder(consentOrderAnnexStamped);

        ApprovedOrder approvedOrder = approvedOrderBuilder.build();

        if (Boolean.FALSE.equals(isPensionDocumentsEmpty(caseData))) {
            log.info("Pension Documents not empty for case - stamping Pension Documents and adding to approvedOrder for case {}",
                caseId);

            List<PensionTypeCollection> stampedPensionDocs = consentOrderApprovedDocumentService.stampPensionDocuments(
                documentHelper.getPensionDocuments(caseData), authToken, stampType, caseId);
            log.info("Generated StampedPensionDocs = {} for case {}", stampedPensionDocs, caseDetails.getId());
            approvedOrder.setPensionDocuments(stampedPensionDocs);
        }

        List<CollectionElement<ApprovedOrder>> approvedOrders = singletonList(CollectionElement.<ApprovedOrder>builder()
            .value(approvedOrder).build());
        log.info("Generated ApprovedOrders = {} for case {}", approvedOrders, caseId);

        caseData.put(APPROVED_ORDER_COLLECTION, approvedOrders);

        log.info("Successfully generated documents for 'Consent Order Approved' for case {}", caseId);

        if (Boolean.TRUE.equals(isPensionDocumentsEmpty(caseData))) {
            log.info("Case {} has no pension documents, updating status to {} and sending for bulk print",
                caseId,
                CONSENT_ORDER_MADE);
            try {
                // Render Case Data with @JSONProperty names, required to re-use sendToBulkPrint code
                caseData = mapper.readValue(mapper.writeValueAsString(caseData), HashMap.class);
                caseDetails.setData(caseData);
                consentOrderPrintService.sendConsentOrderToBulkPrint(caseDetails, authToken);
                caseData.put(STATE, CONSENT_ORDER_MADE.toString());
            } catch (JsonProcessingException e) {
                log.error("case - {}: Error encountered trying to update status and send for bulk print: {}", caseId, e.getMessage());
            }
        }
    }

    private CaseDocument getLatestConsentOrder(Map<String, Object> caseData) {
        return mapper.convertValue(caseData.get(LATEST_CONSENT_ORDER), new TypeReference<>() {
        });
    }

    private Boolean isPensionDocumentsEmpty(Map<String, Object> caseData) {
        List<CaseDocument> pensionDocumentsData = documentHelper.getPensionDocumentsData(caseData);
        return pensionDocumentsData.isEmpty();
    }
}
