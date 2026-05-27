package uk.gov.hmcts.reform.finrem.caseorchestration.handler.approveapplication;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.finrem.caseorchestration.ccd.callback.CallbackType;
import uk.gov.hmcts.reform.finrem.caseorchestration.controllers.GenericAboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.finrem.caseorchestration.handler.CallbackHandlerLogger;
import uk.gov.hmcts.reform.finrem.caseorchestration.handler.FinremCallbackHandler;
import uk.gov.hmcts.reform.finrem.caseorchestration.handler.FinremCallbackRequest;
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

import java.time.LocalDate;
import java.util.List;

import static java.util.Collections.singletonList;
import static java.util.Objects.nonNull;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.State.CLOSE;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.State.CONSENT_ORDER_APPROVED;
import static uk.gov.hmcts.reform.finrem.caseorchestration.utils.ListUtils.nullIfEmpty;

@Slf4j
@Service
public class ApprovedConsentOrderAboutToSubmitHandler extends FinremCallbackHandler {

    private final ConsentOrderApprovedDocumentService consentOrderApprovedDocumentService;
    private final GenericDocumentService genericDocumentService;
    private final ConsentOrderPrintService consentOrderPrintService;
    private final DocumentHelper documentHelper;

    public ApprovedConsentOrderAboutToSubmitHandler(FinremCaseDetailsMapper mapper,
                                                    ConsentOrderApprovedDocumentService consentOrderApprovedDocumentService,
                                                    GenericDocumentService genericDocumentService,
                                                    ConsentOrderPrintService consentOrderPrintService,
                                                    DocumentHelper documentHelper) {
        super(mapper);
        this.consentOrderApprovedDocumentService = consentOrderApprovedDocumentService;
        this.genericDocumentService = genericDocumentService;
        this.consentOrderPrintService = consentOrderPrintService;
        this.documentHelper = documentHelper;
    }

    @Override
    public boolean canHandle(CallbackType callbackType, CaseType caseType, EventType eventType) {
        return CallbackType.ABOUT_TO_SUBMIT.equals(callbackType)
            && CaseType.CONSENTED.equals(caseType)
            && EventType.APPROVE_ORDER.equals(eventType);
    }

    @Override
    public GenericAboutToStartOrSubmitCallbackResponse<FinremCaseData> handle(FinremCallbackRequest callbackRequest,
                                                                              String userAuthorisation) {
        log.info(CallbackHandlerLogger.aboutToSubmit(callbackRequest));

        FinremCaseData finremCaseData = callbackRequest.getFinremCaseData();

        CaseDocument latestConsentOrder = finremCaseData.getLatestConsentOrder();
        if (nonNull(latestConsentOrder)) {
            CaseDocument pdfConsentOrder = genericDocumentService.convertDocumentIfNotPdfAlready(latestConsentOrder,
                userAuthorisation, finremCaseData.getCcdCaseType());
            finremCaseData.setLatestConsentOrder(pdfConsentOrder);
            generateAndPrepareDocuments(callbackRequest.getCaseDetails(), callbackRequest.getCaseDetailsBefore(),
                userAuthorisation);
        } else {
            throw new IllegalStateException(
                "Failed to handle 'Consent Order Approved' callback because 'latestConsentOrder' is empty for Case ID: %s"
                    .formatted(finremCaseData.getCcdCaseId()));
        }

        return response(finremCaseData);
    }

    private void generateAndPrepareDocuments(
        FinremCaseDetails finremCaseDetails, FinremCaseDetails finremCaseDetailsBefore, String authToken
    ) {
        FinremCaseData finremCaseData = finremCaseDetails.getData();
        String caseId = finremCaseData.getCcdCaseId();
        StampType stampType = documentHelper.getStampType(finremCaseData);

        log.info("{} - Generating and preparing documents for latest consent order with stamp type {}", caseId, stampType);
        CaseDocument approvedConsentOrderLetter = consentOrderApprovedDocumentService
            .generateApprovedConsentOrderLetter(finremCaseDetails, authToken);
        CaseDocument consentOrderAnnexStamped = genericDocumentService
            .annexStampDocument(finremCaseData.getLatestConsentOrder(), authToken, stampType, finremCaseData.getCcdCaseType());

        ApprovedOrder approvedOrder = ApprovedOrder.builder()
            .orderLetter(approvedConsentOrderLetter)
            .consentOrder(consentOrderAnnexStamped).build();

        if (!isPensionDocumentsEmpty(finremCaseData)) {
            log.info("Pension Documents not empty for case - stamping Pension Documents and adding to approvedOrder for Case ID: {}",
                caseId);
            LocalDate approvalDate = getApprovalDate(finremCaseData);
            List<PensionTypeCollection> stampedPensionDocs = consentOrderApprovedDocumentService.stampPensionDocuments(
                nullIfEmpty(finremCaseData.getPensionCollection()), authToken, stampType, approvalDate, finremCaseData.getCcdCaseType());
            log.info("Generated StampedPensionDocs = {} for Case ID: {}", stampedPensionDocs, finremCaseData.getCcdCaseId());
            approvedOrder.setPensionDocuments(stampedPensionDocs);
        }

        finremCaseData.setApprovedOrderCollection(singletonList(
            ConsentOrderCollection.builder().approvedOrder(approvedOrder).build()
        ));

        log.info("Successfully generated documents for 'Consent Order Approved' for Case ID {}", caseId);

        if (isPensionDocumentsEmpty(finremCaseData)) {
            consentOrderPrintService.sendConsentOrderToBulkPrint(finremCaseDetails, finremCaseDetailsBefore,
                EventType.APPROVE_ORDER, authToken);
            finremCaseData.setState(CLOSE.getStateId());
            log.info("Case ID: {} has no pension documents. Case state updated to {} and consent order sent for bulk print.",
                caseId, CLOSE);
        } else {
            finremCaseData.setState(CONSENT_ORDER_APPROVED.getStateId());
        }
    }

    private boolean isPensionDocumentsEmpty(FinremCaseData finremCaseData) {
        List<CaseDocument> pensionDocumentsData = documentHelper.getPensionDocumentsData(finremCaseData);
        return pensionDocumentsData.isEmpty();
    }

    private LocalDate getApprovalDate(FinremCaseData finremCaseData) {
        return finremCaseData.getOrderDirectionDate();
    }
}
