package uk.gov.hmcts.reform.finrem.caseorchestration.handler;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.finrem.caseorchestration.ccd.callback.CallbackType;
import uk.gov.hmcts.reform.finrem.caseorchestration.controllers.GenericAboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.finrem.caseorchestration.helper.DocumentHelper;
import uk.gov.hmcts.reform.finrem.caseorchestration.mapper.FinremCaseDetailsMapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.EventType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.ApprovedOrder;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseDocument;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.ConsentOrderCollection;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseData;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.PensionTypeCollection;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.ConsentOrderApprovedDocumentService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.ConsentOrderPrintService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.GenericDocumentService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.StampType;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.springframework.util.ObjectUtils.isEmpty;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ConsentedStatus.CONSENT_ORDER_MADE;

@Slf4j
@Service
public class ApprovedConsentOrderAboutToSubmitHandler extends FinremCallbackHandler {

    private final ConsentOrderApprovedDocumentService consentOrderApprovedDocumentService;
    private final GenericDocumentService genericDocumentService;
    private final ConsentOrderPrintService consentOrderPrintService;
    private final DocumentHelper documentHelper;


    public ApprovedConsentOrderAboutToSubmitHandler(FinremCaseDetailsMapper finremCaseDetailsMapper,
                                                    ConsentOrderApprovedDocumentService consentOrderApprovedDocumentService,
                                                    GenericDocumentService genericDocumentService,
                                                    ConsentOrderPrintService consentOrderPrintService,
                                                    DocumentHelper documentHelper) {
        super(finremCaseDetailsMapper);
        this.consentOrderApprovedDocumentService = consentOrderApprovedDocumentService;
        this.genericDocumentService = genericDocumentService;
        this.consentOrderPrintService = consentOrderPrintService;
        this.documentHelper = documentHelper;
    }

    @Override
    public boolean canHandle(final CallbackType callbackType, final CaseType caseType,
                             final EventType eventType) {
        return CallbackType.ABOUT_TO_SUBMIT.equals(callbackType)
            && CaseType.CONSENTED.equals(caseType)
            && EventType.APPROVE_ORDER.equals(eventType);
    }

    @Override
    public GenericAboutToStartOrSubmitCallbackResponse<FinremCaseData> handle(FinremCallbackRequest callbackRequest,
                                                                              String userAuthorisation) {
        FinremCaseDetails caseDetails = callbackRequest.getCaseDetails();
        String caseId = String.valueOf(caseDetails.getId());
        log.info("Invoking consent event {} mid callback for Case ID: {}", EventType.APPROVE_ORDER, caseId);
        List<String> errors = new ArrayList<>();
        FinremCaseData caseData = caseDetails.getData();
        CaseDocument latestConsentOrder = caseData.getLatestConsentOrder();
        if (!isEmpty(latestConsentOrder)) {
            CaseDocument pdfConsentOrder = genericDocumentService.convertDocumentIfNotPdfAlready(latestConsentOrder, userAuthorisation, caseId);
            caseData.setLatestConsentOrder(pdfConsentOrder);
            generateAndPrepareDocuments(userAuthorisation, caseDetails, pdfConsentOrder);
        } else {
            log.info("Failed to handle 'Consent Order Approved' callback because 'latestConsentOrder' is empty for Case ID: {}",
                caseId);
        }
        return GenericAboutToStartOrSubmitCallbackResponse.<FinremCaseData>builder().data(caseData).errors(errors).build();
    }

    private void generateAndPrepareDocuments(String authToken,
                                             FinremCaseDetails caseDetails,
                                             CaseDocument latestConsentOrder) {
        String caseId = String.valueOf(caseDetails.getId());
        log.info("Generating and preparing documents for latest consent order, Case ID: {}", caseId);

        FinremCaseData caseData = caseDetails.getData();
        StampType stampType = documentHelper.getStampType(caseData);
        CaseDocument approvedConsentOrderLetter = consentOrderApprovedDocumentService.generateApprovedConsentOrderLetter(caseDetails, authToken);
        CaseDocument consentOrderAnnexStamped = genericDocumentService.annexStampDocument(latestConsentOrder, authToken, stampType, caseId);

        ApprovedOrder approvedOrder = ApprovedOrder.builder()
            .orderLetter(approvedConsentOrderLetter)
            .consentOrder(consentOrderAnnexStamped)
            .build();

        if (!isPensionDocumentsEmpty(caseData)) {
            log.info("Pension Documents not empty for case - stamping Pension Documents and adding to approvedOrder for Case ID: {}",
                caseId);

            List<PensionTypeCollection> stampedPensionDocs = consentOrderApprovedDocumentService.stampPensionDocuments(
                caseData.getPensionCollection(), authToken, stampType, caseId);
            log.info("Generated StampedPensionDocs = {} for Case ID: {}", stampedPensionDocs, caseDetails.getId());
            approvedOrder.setPensionDocuments(stampedPensionDocs);
        }

        List<ConsentOrderCollection> approvedOrders = Optional.ofNullable(caseData.getApprovedOrderCollection()).orElse(new ArrayList<>());
        ConsentOrderCollection consentOrderCollection = ConsentOrderCollection.builder().approvedOrder(approvedOrder).build();
        approvedOrders.add(consentOrderCollection);
        log.info("Generated ApprovedOrders = {} for Case ID {}", approvedOrders, caseId);

        caseData.setApprovedOrderCollection(approvedOrders);

        log.info("Successfully generated documents for 'Consent Order Approved' for Case ID {}", caseId);

        consentOrderPrintService.sendConsentOrderToBulkPrint(caseDetails, authToken);
        caseData.setState(CONSENT_ORDER_MADE.toString());
    }

    private boolean isPensionDocumentsEmpty(FinremCaseData caseData) {
        List<CaseDocument> pensionDocumentsData = documentHelper.getPensionDocumentsData(caseData);
        return pensionDocumentsData.isEmpty();
    }
}
