package uk.gov.hmcts.reform.finrem.caseorchestration.service.sendorder;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.ApprovedOrderCollection;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.ApprovedOrderConsolidateCollection;
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
}
