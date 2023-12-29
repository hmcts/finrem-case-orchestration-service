package uk.gov.hmcts.reform.finrem.caseorchestration.service.sendorder;

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
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.wrapper.IntervenerFourWrapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.ConsentOrderApprovedDocumentService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.NotificationService;


import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Component
public class SendOrderIntervenerFourDocumentHandler extends SendOrderPartyDocumentHandler {
    private final ConsentOrderApprovedDocumentService consentOrderApprovedDocumentService;
    private final NotificationService notificationService;

    public SendOrderIntervenerFourDocumentHandler(ConsentOrderApprovedDocumentService consentOrderApprovedDocumentService,
                                                 NotificationService notificationService) {

        super(CaseRole.INTVR_SOLICITOR_4.getCcdCode());
        this.consentOrderApprovedDocumentService = consentOrderApprovedDocumentService;
        this.notificationService = notificationService;
    }

    @Override
    protected List<ApprovedOrderCollection> getOrderCollectionForParty(FinremCaseData caseData) {
        return Optional.ofNullable(caseData.getOrderWrapper().getIntv4OrderCollection())
            .orElse(new ArrayList<>());
    }

    @Override
    protected List<ConsentInContestedApprovedOrderCollection> getConsentOrderCollectionForParty(FinremCaseData caseData) {
        return Optional.ofNullable(caseData.getConsentOrderWrapper().getIntv4ConsentApprovedOrders())
            .orElse(new ArrayList<>());
    }

    @Override
    protected void addApprovedConsentOrdersToPartyCollection(FinremCaseData caseData, List<ConsentInContestedApprovedOrderCollection> orderColl) {
        caseData.getConsentOrderWrapper().setIntv4ConsentApprovedOrders(orderColl);
    }

    @Override
    protected List<UnapprovedOrderCollection> getUnapprovedOrderCollectionForParty(FinremCaseData caseData) {
        return Optional.ofNullable(caseData.getConsentOrderWrapper().getIntv4RefusedOrderCollection())
            .orElse(new ArrayList<>());
    }

    @Override
    protected void addOrdersToPartyCollection(FinremCaseData caseData, List<ApprovedOrderCollection> orderColl) {
        caseData.getOrderWrapper().setIntv4OrderCollection(orderColl);
    }

    @Override
    protected void addUnapprovedOrdersToPartyCollection(FinremCaseData caseData, List<UnapprovedOrderCollection> orderColl) {
        caseData.getConsentOrderWrapper().setIntv4RefusedOrderCollection(orderColl);
    }

    @Override
    protected CaseDocument getPartyCoverSheet(FinremCaseDetails caseDetails, String authToken) {
        DocumentHelper.PaperNotificationRecipient recipient = DocumentHelper.PaperNotificationRecipient.INTERVENER_FOUR;
        return consentOrderApprovedDocumentService.getPopulatedConsentCoverSheet(caseDetails, authToken, recipient);
    }

    @Override
    protected void addCoverSheetToPartyField(FinremCaseDetails caseDetails, CaseDocument bulkPrintSheet) {
        FinremCaseData caseData = caseDetails.getData();
        IntervenerFourWrapper wrapper = caseData.getIntervenerFourWrapper();
        if (!notificationService.isIntervenerSolicitorDigitalAndEmailPopulated(wrapper, caseDetails)) {
            caseData.getBulkPrintCoverSheetWrapper().setBulkPrintCoverSheetIntv4(bulkPrintSheet);
        }
    }

    protected void setConsolidateCollection(FinremCaseData caseData, List<ApprovedOrderCollection> orderCollection) {
        List<ApprovedOrderConsolidateCollection> orders = Optional.ofNullable(caseData.getOrderWrapper().getIntv4OrderCollections())
            .orElse(new ArrayList<>());
        orders.add(getConsolidateCollection(orderCollection));
        orders.sort((m1, m2) -> m2.getValue().getOrderReceivedAt().compareTo(m1.getValue().getOrderReceivedAt()));
        caseData.getOrderWrapper().setIntv4OrderCollections(orders);
        caseData.getOrderWrapper().setIntv4OrderCollection(null);
    }
}
