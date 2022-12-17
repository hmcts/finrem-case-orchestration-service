package uk.gov.hmcts.reform.finrem.caseorchestration.service.casedocuments.respondent;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseDocumentParty;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.document.ManageCaseDocumentsCollectionType;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.casedocuments.OtherDocumentsCollectionService;

@Service
public class RespondentOtherDocumentsCollectionService extends OtherDocumentsCollectionService {

    @Autowired
    public RespondentOtherDocumentsCollectionService() {
        super(ManageCaseDocumentsCollectionType.RESP_OTHER_COLLECTION,
                CaseDocumentParty.RESPONDENT);
    }
}
