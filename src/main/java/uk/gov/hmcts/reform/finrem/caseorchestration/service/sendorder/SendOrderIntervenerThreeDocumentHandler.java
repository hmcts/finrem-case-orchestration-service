package uk.gov.hmcts.reform.finrem.caseorchestration.service.sendorder;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.finrem.caseorchestration.helper.DocumentHelper;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.ApprovedOrderCollection;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.ApprovedOrderConsolidateCollection;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseDocument;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseRole;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseData;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.RoleConsentOrderCollection;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.UnapprovedOrderCollection;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.wrapper.IntervenerThreeWrapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.ConsentOrderApprovedDocumentService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.NotificationService;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Component
public class SendOrderIntervenerThreeDocumentHandler extends SendOrderPartyDocumentHandler {
    private final ConsentOrderApprovedDocumentService consentOrderApprovedDocumentService;
    private final NotificationService notificationService;

    public SendOrderIntervenerThreeDocumentHandler(ConsentOrderApprovedDocumentService consentOrderApprovedDocumentService,
                                                   NotificationService notificationService) {

        super(CaseRole.INTVR_SOLICITOR_3.getCcdCode());
        this.consentOrderApprovedDocumentService = consentOrderApprovedDocumentService;
        this.notificationService = notificationService;
    }

    @Override
    protected List<ApprovedOrderCollection> getOrderCollectionForParty(FinremCaseData caseData) {
        return Optional.ofNullable(caseData.getOrderWrapper().getIntv3OrderCollection())
            .orElse(new ArrayList<>());
    }

    @Override
    protected List<RoleConsentOrderCollection> getConsentOrderCollectionForParty(FinremCaseData caseData) {
        return Optional.ofNullable(caseData.getConsentOrderWrapper().getIntv3ConsentApprovedOrders())
            .orElse(new ArrayList<>());
    }

    @Override
    protected void addApprovedConsentOrdersToPartyCollection(FinremCaseData caseData, List<RoleConsentOrderCollection> orderColl) {
        caseData.getConsentOrderWrapper().setIntv3ConsentApprovedOrders(orderColl);
    }

    @Override
    protected List<UnapprovedOrderCollection> getUnapprovedOrderCollectionForParty(FinremCaseData caseData) {
        return Optional.ofNullable(caseData.getConsentOrderWrapper().getIntv3RefusedOrderCollection())
            .orElse(new ArrayList<>());
    }

    @Override
    protected void addOrdersToPartyCollection(FinremCaseData caseData, List<ApprovedOrderCollection> orderColl) {
        caseData.getOrderWrapper().setIntv3OrderCollection(orderColl);
    }

    @Override
    protected void addUnapprovedOrdersToPartyCollection(FinremCaseData caseData, List<UnapprovedOrderCollection> orderColl) {
        caseData.getConsentOrderWrapper().setIntv3RefusedOrderCollection(orderColl);
    }

    @Override
    protected CaseDocument getPartyCoverSheet(FinremCaseDetails caseDetails, String authToken) {
        DocumentHelper.PaperNotificationRecipient recipient = DocumentHelper.PaperNotificationRecipient.INTERVENER_THREE;
        return consentOrderApprovedDocumentService.getPopulatedConsentCoverSheet(caseDetails, authToken, recipient);
    }

    @Override
    protected void addCoverSheetToPartyField(FinremCaseDetails caseDetails, CaseDocument bulkPrintSheet) {
        FinremCaseData caseData = caseDetails.getData();
        IntervenerThreeWrapper wrapper = caseData.getIntervenerThreeWrapper();
        if (!notificationService.isIntervenerSolicitorDigitalAndEmailPopulated(wrapper, caseDetails)) {
            caseData.setBulkPrintCoverSheetIntv3(bulkPrintSheet);
        }
    }

    protected void setConsolidateCollection(FinremCaseData caseData, List<ApprovedOrderCollection> orderCollection) {
        List<ApprovedOrderConsolidateCollection> orders = Optional.ofNullable(caseData.getOrderWrapper().getIntv3OrderCollections())
            .orElse(new ArrayList<>());
        orders.add(getConsolidateCollection(orderCollection));
        caseData.getOrderWrapper().setIntv3OrderCollections(orders);
        caseData.getOrderWrapper().setIntv3OrderCollection(null);
    }
}
