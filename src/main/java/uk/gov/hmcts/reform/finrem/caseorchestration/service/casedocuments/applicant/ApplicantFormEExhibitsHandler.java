package uk.gov.hmcts.reform.finrem.caseorchestration.service.casedocuments.applicant;

import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseDocumentParty;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseDocumentType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.document.CaseDocumentCollectionType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.document.DocumentCategory;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.casedocuments.FormEExhibitsHandler;

import java.util.Objects;

@Service
public class ApplicantFormEExhibitsHandler extends FormEExhibitsHandler {

    public ApplicantFormEExhibitsHandler() {
        super(CaseDocumentCollectionType.APP_FORM_E_EXHIBITS_COLLECTION,
            CaseDocumentParty.APPLICANT);
    }

    @Override
    protected DocumentCategory getDocumentCategoryFromDocumentType(CaseDocumentType caseDocumentType) {
        if (Objects.requireNonNull(caseDocumentType) == CaseDocumentType.APPLICANT_FORM_E) {
            return DocumentCategory.APPLICANT_DOCUMENTS;
        }
        return DocumentCategory.UNCATEGORISED;
    }

}
