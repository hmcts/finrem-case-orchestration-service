package uk.gov.hmcts.reform.finrem.caseorchestration.service.consentorder;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.ApprovedOrderCollection;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseRole;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseData;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Component
public class ConsentOrderIntervenerFourDocumentHandler extends ConsentOrderPartyDocumentHandler {
    public ConsentOrderIntervenerFourDocumentHandler() {
        super(CaseRole.INTVR_SOLICITOR_4.getCcdCode());
    }

    @Override
    protected List<ApprovedOrderCollection> getOrderCollectionForParty(FinremCaseData caseData) {
        return Optional.ofNullable(caseData.getIntv4OrderCollection())
            .orElse(new ArrayList<>());
    }

    @Override
    protected void addOrdersToPartyCollection(FinremCaseData caseData, List<ApprovedOrderCollection> orderColl) {
        caseData.setIntv4OrderCollection(orderColl);
    }
}
