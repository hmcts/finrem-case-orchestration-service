package uk.gov.hmcts.reform.finrem.caseorchestration.service.casedocuments.intervenertwo;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.casedocuments.CaseSummariesHandler;

import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseDocumentParty.INTERVENER_TWO;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.document.ManageCaseDocumentsCollectionType.INTV_TWO_CASE_SUMMARIES_COLLECTION;

@Component
public class IntervenerTwoCaseSummariesHandler extends CaseSummariesHandler {

    @Autowired
    public IntervenerTwoCaseSummariesHandler() {
        super(INTV_TWO_CASE_SUMMARIES_COLLECTION, INTERVENER_TWO);
    }
}
