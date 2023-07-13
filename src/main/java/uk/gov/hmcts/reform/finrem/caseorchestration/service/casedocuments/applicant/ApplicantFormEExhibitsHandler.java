package uk.gov.hmcts.reform.finrem.caseorchestration.service.casedocuments.applicant;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseDocumentParty;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.document.CaseDocumentCollectionType;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.casedocuments.FormEExhibitsHandler;

@Service
public class ApplicantFormEExhibitsHandler extends FormEExhibitsHandler {

    @Autowired
    public ApplicantFormEExhibitsHandler() {
        super(CaseDocumentCollectionType.APP_FORM_E_EXHIBITS_COLLECTION,
            CaseDocumentParty.APPLICANT);
    }

}
