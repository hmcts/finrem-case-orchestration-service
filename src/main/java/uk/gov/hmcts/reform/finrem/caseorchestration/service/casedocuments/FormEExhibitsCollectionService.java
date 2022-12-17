package uk.gov.hmcts.reform.finrem.caseorchestration.service.casedocuments;

import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseDocumentParty;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseDocumentType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.document.ManageCaseDocumentsCollectionType;

public class FormEExhibitsCollectionService extends PartyDocumentCollectionService {

    public FormEExhibitsCollectionService(ManageCaseDocumentsCollectionType manageCaseDocumentsCollectionType,
                                          CaseDocumentParty party) {
        super(manageCaseDocumentsCollectionType, party);
    }

    @Override
    protected boolean canProcessDocumentType(CaseDocumentType caseDocumentType) {
        return caseDocumentType.equals(CaseDocumentType.APPLICANT_FORM_E);
    }
}
