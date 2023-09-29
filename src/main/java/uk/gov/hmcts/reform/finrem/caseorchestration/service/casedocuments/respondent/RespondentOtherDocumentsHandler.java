package uk.gov.hmcts.reform.finrem.caseorchestration.service.casedocuments.respondent;

import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseDocumentParty;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseDocumentType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.document.CaseDocumentCollectionType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.document.DocumentCategory;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.casedocuments.OtherDocumentsHandler;

@Service
public class RespondentOtherDocumentsHandler extends OtherDocumentsHandler {

    public RespondentOtherDocumentsHandler() {
        super(CaseDocumentCollectionType.RESP_OTHER_COLLECTION,
                CaseDocumentParty.RESPONDENT);
    }

    @Override
    protected DocumentCategory getDocumentCategoryFromDocumentType(CaseDocumentType caseDocumentType) {
        switch (caseDocumentType) {
            case OTHER, FORM_F, CARE_PLAN, PENSION_PLAN -> {
                return DocumentCategory.RESPONDENT_DOCUMENTS;
                //TODO: Check category is correct for Form F, Care Plan & Pension Plan
            }
            case FORM_B -> {
                return DocumentCategory.APPLICATIONS;
            }
            default -> {
                return DocumentCategory.UNCATEGORISED;
            }
        }
    }
}
