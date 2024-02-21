package uk.gov.hmcts.reform.finrem.caseorchestration.service.sendorder;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.finrem.caseorchestration.helper.DocumentHelper;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.ApprovedOrderCollection;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.ApprovedOrderConsolidateCollection;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseDocument;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseRole;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.ConsentInContestedApprovedOrderCollection;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseData;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.UnapprovedOrderCollection;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.CaseDataService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.ConsentOrderApprovedDocumentService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.NotificationService;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Component
@Slf4j
public class SendOrderApplicantDocumentHandler extends SendOrderPartyDocumentHandler {
    private final ConsentOrderApprovedDocumentService consentOrderApprovedDocumentService;
    private final CaseDataService caseDataService;
    private final NotificationService notificationService;

    public SendOrderApplicantDocumentHandler(
                                             ConsentOrderApprovedDocumentService consentOrderApprovedDocumentService,
                                             NotificationService notificationService,
                                             CaseDataService caseDataService) {

        super(CaseRole.APP_SOLICITOR.getCcdCode());
        this.consentOrderApprovedDocumentService = consentOrderApprovedDocumentService;
        this.caseDataService = caseDataService;
        this.notificationService = notificationService;
    }

    @Override
    protected List<ApprovedOrderCollection> getOrderCollectionForParty(FinremCaseData caseData) {
        return Optional.ofNullable(caseData.getOrderWrapper().getAppOrderCollection())
            .orElse(new ArrayList<>());
    }

    @Override
    protected List<ConsentInContestedApprovedOrderCollection> getConsentOrderCollectionForParty(FinremCaseData caseData) {
        return Optional.ofNullable(caseData.getConsentOrderWrapper().getAppConsentApprovedOrders())
            .orElse(new ArrayList<>());
    }

    @Override
    protected List<UnapprovedOrderCollection> getUnapprovedOrderCollectionForParty(FinremCaseData caseData) {
        return Optional.ofNullable(caseData.getConsentOrderWrapper().getAppRefusedOrderCollection())
            .orElse(new ArrayList<>());
    }

    @Override
    protected void addOrdersToPartyCollection(FinremCaseData caseData, List<ApprovedOrderCollection> orderColl) {
        caseData.getOrderWrapper().setAppOrderCollection(orderColl);
    }

    @Override
    protected void addApprovedConsentOrdersToPartyCollection(FinremCaseData caseData, List<ConsentInContestedApprovedOrderCollection> orderColl) {
        caseData.getConsentOrderWrapper().setAppConsentApprovedOrders(orderColl);
    }

    @Override
    protected void addUnapprovedOrdersToPartyCollection(FinremCaseData caseData, List<UnapprovedOrderCollection> orderColl) {
        caseData.getConsentOrderWrapper().setAppRefusedOrderCollection(orderColl);
    }

    @Override
    protected CaseDocument getPartyCoverSheet(FinremCaseDetails caseDetails, String authToken) {
        DocumentHelper.PaperNotificationRecipient recipient = DocumentHelper.PaperNotificationRecipient.APPLICANT;
        return consentOrderApprovedDocumentService.getPopulatedConsentCoverSheet(caseDetails, authToken, recipient);
    }

    @Override
    protected void addCoverSheetToPartyField(FinremCaseDetails caseDetails, CaseDocument bulkPrintSheet) {
        FinremCaseData caseData = caseDetails.getData();
        if (!notificationService.isApplicantSolicitorDigitalAndEmailPopulated(caseDetails)) {
            if (caseDataService.isApplicantAddressConfidential(caseData)) {
                setConfidentialBulkPrintFieldForApplicant(caseDetails, bulkPrintSheet, caseData);
            } else {
                caseData.setBulkPrintCoverSheetApp(bulkPrintSheet);
            }
        }
    }

    private static void setConfidentialBulkPrintFieldForApplicant(FinremCaseDetails finremCaseDetails,
                                                                  CaseDocument bulkPrintSheet,
                                                                  FinremCaseData caseData) {
        log.info("Case {}, has been marked as confidential. Adding applicant cover sheet to confidential field", finremCaseDetails.getId());
        caseData.setBulkPrintCoverSheetApp(null);
        caseData.setBulkPrintCoverSheetAppConfidential(bulkPrintSheet);
    }

    protected void setConsolidateCollection(FinremCaseData caseData, List<ApprovedOrderCollection> orderCollection) {
        List<ApprovedOrderCollection> newOrderCollection = new ArrayList<>(orderCollection);
        List<ApprovedOrderConsolidateCollection> orders
            = getPartyConsolidateCollection(caseData.getOrderWrapper().getAppOrderCollections());
        orders.add(getConsolidateCollection(newOrderCollection));
        orders.sort((m1, m2) -> m2.getValue().getOrderReceivedAt().compareTo(m1.getValue().getOrderReceivedAt()));
        caseData.getOrderWrapper().setAppOrderCollections(orders);
        caseData.getOrderWrapper().setAppOrderCollection(null);
    }

    protected List<ApprovedOrderConsolidateCollection> getExistingConsolidateCollection(FinremCaseData caseData) {
        return Optional.ofNullable(caseData.getOrderWrapper().getAppOrderCollections())
            .orElse(new ArrayList<>());
    }
}
