package uk.gov.hmcts.reform.finrem.caseorchestration.service.casedocuments.intervenerone;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.casedocuments.CaseSummariesHandler;

import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseDocumentParty.INTERVENER_ONE;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.document.ManageCaseDocumentsCollectionType.INTV_ONE_CASE_SUMMARIES_COLLECTION;

@Component
public class IntervenerOneCaseSummariesHandler extends CaseSummariesHandler {

    @Autowired
    public IntervenerOneCaseSummariesHandler() {
        super(INTV_ONE_CASE_SUMMARIES_COLLECTION, INTERVENER_ONE);
    }
}
