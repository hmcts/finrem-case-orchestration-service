package uk.gov.hmcts.reform.finrem.caseorchestration.service.casedocuments;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseDocumentParty;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseDocumentType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.document.ManageCaseDocumentsCollectionType;

@Service
public class CaseDocumentCollectionService extends PartyDocumentCollectionService {

    @Autowired
    public CaseDocumentCollectionService() {
        super(ManageCaseDocumentsCollectionType.CONTESTED_UPLOADED_DOCUMENTS,
            CaseDocumentParty.CASE);
    }

    @Override
    protected boolean canProcessDocumentType(CaseDocumentType caseDocumentType) {
        return true;
    }
}
