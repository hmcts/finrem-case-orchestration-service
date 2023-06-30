package uk.gov.hmcts.reform.finrem.caseorchestration.service.casedocuments.intervenerthree;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.casedocuments.CaseSummariesHandler;

import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseDocumentParty.INTERVENER_THREE;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.document.ManageCaseDocumentsCollectionType.INTV_THREE_CASE_SUMMARIES_COLLECTION;

@Component
public class IntervenerThreeCaseSummariesHandler extends CaseSummariesHandler {

    @Autowired
    public IntervenerThreeCaseSummariesHandler() {
        super(INTV_THREE_CASE_SUMMARIES_COLLECTION, INTERVENER_THREE);
    }
}
