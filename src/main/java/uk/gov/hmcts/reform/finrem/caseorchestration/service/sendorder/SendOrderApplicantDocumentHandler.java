package uk.gov.hmcts.reform.finrem.caseorchestration.service.sendorder;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.ApprovedOrderCollection;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.ApprovedOrderConsolidateCollection;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseDocument;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseRole;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseData;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Component
public class SendOrderApplicantDocumentHandler extends SendOrderPartyDocumentHandler {


    public SendOrderApplicantDocumentHandler() {
        super(CaseRole.APP_SOLICITOR.getCcdCode());
    }

    protected List<ApprovedOrderCollection> getOrderCollectionForParty(FinremCaseData caseData) {
        return Optional.ofNullable(caseData.getAppOrderCollection())
            .orElse(new ArrayList<>());
    }

    protected void addOrdersToPartyCollection(FinremCaseData caseData, List<ApprovedOrderCollection> orderColl) {
        caseData.setAppOrderCollection(orderColl);
    }


    protected void setConsolidateCollection(FinremCaseData caseData, List<ApprovedOrderCollection> orderCollection) {
        List<ApprovedOrderConsolidateCollection> orders = Optional.ofNullable(caseData.getAppOrderCollections())
            .orElse(new ArrayList<>());
        orders.add(getConsolidateCollection(orderCollection));
        orders.sort((m1, m2) -> m2.getValue().getOrderReceivedAt().compareTo(m1.getValue().getOrderReceivedAt()));
        caseData.setAppOrderCollections(orders);
        caseData.setAppOrderCollection(null);
    }

    protected boolean shouldAddDocumentToOrderColl(FinremCaseData caseData,
                                                   CaseDocument document,
                                                   List<ApprovedOrderCollection> orderColl) {
        List<ApprovedOrderConsolidateCollection> existingCollection = Optional.ofNullable(caseData.getAppOrderCollections())
            .orElse(new ArrayList<>());
        if (existingCollection.isEmpty()) {
            return true;
        }
        return existingCollection.stream().noneMatch(doc -> doc.getValue().getApproveOrders().stream().anyMatch(order ->
            order.getValue().getCaseDocument().getDocumentFilename().equals(additionalHearingFileName)
                && order.getValue().getCaseDocument().getDocumentUrl().equals(document.getDocumentUrl())
        ));
    }
}
