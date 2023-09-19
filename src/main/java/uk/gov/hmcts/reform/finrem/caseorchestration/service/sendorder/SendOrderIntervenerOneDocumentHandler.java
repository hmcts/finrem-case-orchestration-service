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
public class SendOrderIntervenerOneDocumentHandler extends SendOrderPartyDocumentHandler {
    public SendOrderIntervenerOneDocumentHandler() {
        super(CaseRole.INTVR_SOLICITOR_1.getCcdCode());
    }

    @Override
    protected List<ApprovedOrderCollection> getOrderCollectionForParty(FinremCaseData caseData) {
        return Optional.ofNullable(caseData.getIntv1OrderCollection())
            .orElse(new ArrayList<>());
    }

    @Override
    protected void addOrdersToPartyCollection(FinremCaseData caseData, List<ApprovedOrderCollection> orderColl) {
        caseData.setIntv1OrderCollection(orderColl);
    }

    protected void setConsolidateCollection(FinremCaseData caseData, List<ApprovedOrderCollection> orderCollection) {
        List<ApprovedOrderConsolidateCollection> orders = Optional.ofNullable(caseData.getIntv1OrderCollections())
            .orElse(new ArrayList<>());
        orders.add(getConsolidateCollection(orderCollection));
        caseData.setIntv1OrderCollections(orders);
        caseData.setIntv1OrderCollection(null);
    }
}
