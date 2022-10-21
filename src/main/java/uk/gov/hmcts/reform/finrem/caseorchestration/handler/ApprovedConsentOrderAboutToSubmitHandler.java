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
import uk.gov.hmcts.reform.finrem.caseorchestration.model.CaseType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.EventType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.ApprovedOrder;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseDocument;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CollectionElement;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.PensionCollectionData;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.CaseDataService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.ConsentOrderApprovedDocumentService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.ConsentOrderPrintService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.GenericDocumentService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.NotificationService;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static java.util.Collections.singletonList;
import static org.springframework.util.ObjectUtils.isEmpty;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ConsentedStatus.CONSENT_ORDER_MADE;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.APPROVED_ORDER_COLLECTION;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.LATEST_CONSENT_ORDER;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.PENSION_DOCS_COLLECTION;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.STATE;

@Slf4j
@Service
@RequiredArgsConstructor
public class ApprovedConsentOrderAboutToSubmitHandler implements CallbackHandler<Map<String, Object>> {

    private final ConsentOrderApprovedDocumentService consentOrderApprovedDocumentService;
    private final GenericDocumentService genericDocumentService;
    private final ConsentOrderPrintService consentOrderPrintService;
    private final NotificationService notificationService;
    private final CaseDataService caseDataService;
    private final DocumentHelper documentHelper;
    private final ObjectMapper mapper;

    @Override
    public boolean canHandle(final CallbackType callbackType, final CaseType caseType,
                             final EventType eventType) {
        return CallbackType.ABOUT_TO_SUBMIT.equals(callbackType)
            && CaseType.CONSENTED.equals(caseType)
            && EventType.APPROVE_APPLICATION.equals(eventType);
    }

    @Override
    public GenericAboutToStartOrSubmitCallbackResponse<Map<String, Object>> handle(CallbackRequest callbackRequest,
                                                                                   String userAuthorisation) {
        CaseDetails caseDetails = callbackRequest.getCaseDetails();
        log.info("ConsentOrderApprovedAboutToSubmitHandle handle case Id {}", caseDetails.getId());

        CaseDocument latestConsentOrder = getLatestConsentOrder(caseDetails.getData());

        if (!isEmpty(latestConsentOrder)) {
            generateAndPrepareDocuments(userAuthorisation, caseDetails, latestConsentOrder);
        } else {
            log.info("Failed to handle 'Consent Order Approved' callback because 'latestConsentOrder' is empty for case: {}",
                caseDetails.getId());
        }
        return GenericAboutToStartOrSubmitCallbackResponse.<Map<String, Object>>builder().data(caseDetails.getData()).build();
    }


    private void generateAndPrepareDocuments(String authToken, CaseDetails caseDetails, CaseDocument latestConsentOrder) {
        log.info("Generating and preparing documents for latest consent order, case {}", caseDetails.getId());

        Map<String, Object> caseData = caseDetails.getData();

        CaseDocument approvedConsentOrderLetter = consentOrderApprovedDocumentService.generateApprovedConsentOrderLetter(caseDetails, authToken);
        CaseDocument consentOrderAnnexStamped = genericDocumentService.annexStampDocument(latestConsentOrder, authToken);

        ApprovedOrder.ApprovedOrderBuilder approvedOrderBuilder = ApprovedOrder.builder()
            .orderLetter(approvedConsentOrderLetter)
            .consentOrder(consentOrderAnnexStamped);

        ApprovedOrder approvedOrder = approvedOrderBuilder.build();

        if (Boolean.FALSE.equals(pensionDocumentsExists(caseData))) {
            log.info("Pension Documents not empty for case - stamping Pension Documents and adding to approvedOrder for case {}",
                caseDetails.getId());

            List<PensionCollectionData> stampedPensionDocs = consentOrderApprovedDocumentService.stampPensionDocuments(
                getPensionDocuments(caseData), authToken);
            log.info("Generated StampedPensionDocs = {} for case {}", stampedPensionDocs, caseDetails.getId());
            approvedOrder.setPensionDocuments(stampedPensionDocs);
        }

        List<CollectionElement<ApprovedOrder>> approvedOrders = singletonList(CollectionElement.<ApprovedOrder>builder()
            .value(approvedOrder).build());
        log.info("Generated ApprovedOrders = {} for case {}", approvedOrders, caseDetails.getId());

        caseData.put(APPROVED_ORDER_COLLECTION, approvedOrders);

        log.info("Successfully generated documents for 'Consent Order Approved' for case {}", caseDetails.getId());

        if (Boolean.TRUE.equals(pensionDocumentsExists(caseData))) {
            log.info("Case {} has no pension documents, updating status to {} and sending for bulk print",
                caseDetails.getId(),
                CONSENT_ORDER_MADE);
            try {
                // Render Case Data with @JSONProperty names, required to re-use sendToBulkPrint code
                caseData = mapper.readValue(mapper.writeValueAsString(caseData), HashMap.class);
                caseDetails.setData(caseData);
                consentOrderPrintService.sendConsentOrderToBulkPrint(caseDetails, authToken);
                caseData.put(STATE, CONSENT_ORDER_MADE.toString());
                notificationService.sendConsentOrderAvailableCtscEmail(caseDetails);

                if (caseDataService.isApplicantSolicitorAgreeToReceiveEmails(caseDetails)) {
                    log.info("case - {}: Sending email notification for to Applicant Solicitor for 'Consent Order Available'", caseDetails.getId());
                    notificationService.sendConsentOrderAvailableEmailToApplicantSolicitor(caseDetails);
                }
                if (notificationService.isRespondentSolicitorEmailCommunicationEnabled(caseData)) {
                    log.info("case - {}: Sending email notification to Respondent Solicitor for 'Consent Order Available'", caseDetails.getId());
                    notificationService.sendConsentOrderAvailableEmailToRespondentSolicitor(caseDetails);
                }
            } catch (JsonProcessingException e) {
                log.error("case - {}: Error encountered trying to update status and send for bulk print: {}", caseDetails.getId(), e.getMessage());
            }
        }
    }

    private CaseDocument getLatestConsentOrder(Map<String, Object> caseData) {
        return mapper.convertValue(caseData.get(LATEST_CONSENT_ORDER), new TypeReference<>() {
        });
    }

    private List<PensionCollectionData> getPensionDocuments(Map<String, Object> caseData) {
        return mapper.convertValue(caseData.get(PENSION_DOCS_COLLECTION), new TypeReference<>() {
        });
    }

    private Boolean pensionDocumentsExists(Map<String, Object> caseData) {
        List<CaseDocument> pensionDocumentsData = documentHelper.getPensionDocumentsData(caseData);
        return pensionDocumentsData.isEmpty();
    }
}
