package uk.gov.hmcts.reform.finrem.caseorchestration.service.casedocuments.respondent;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseDocumentParty;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.document.CaseDocumentCollectionType;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.casedocuments.OtherDocumentsHandler;

@Service
public class RespondentOtherDocumentsHandler extends OtherDocumentsHandler {

    @Autowired
    public RespondentOtherDocumentsHandler() {
        super(CaseDocumentCollectionType.RESP_OTHER_COLLECTION,
                CaseDocumentParty.RESPONDENT);
    }
}
