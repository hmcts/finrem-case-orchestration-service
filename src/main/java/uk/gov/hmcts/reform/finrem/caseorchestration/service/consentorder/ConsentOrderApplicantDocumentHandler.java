package uk.gov.hmcts.reform.finrem.caseorchestration.service.consentorder;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.ApprovedOrderCollection;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseRole;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseData;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Component
public class ConsentOrderApplicantDocumentHandler extends ConsentOrderPartyDocumentHandler {


    public ConsentOrderApplicantDocumentHandler() {
        super(CaseRole.APP_SOLICITOR.getCcdCode());
    }

    protected List<ApprovedOrderCollection> getOrderCollectionForParty(FinremCaseData caseData) {
        return Optional.ofNullable(caseData.getAppOrderCollection())
            .orElse(new ArrayList<>());
    }

    protected void addOrdersToPartyCollection(FinremCaseData caseData, List<ApprovedOrderCollection> orderColl) {
        caseData.setAppOrderCollection(orderColl);
    }

}
