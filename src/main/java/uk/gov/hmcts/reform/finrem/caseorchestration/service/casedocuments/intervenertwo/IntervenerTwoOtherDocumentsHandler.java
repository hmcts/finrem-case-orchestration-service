package uk.gov.hmcts.reform.finrem.caseorchestration.service.casedocuments.intervenertwo;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseDocumentType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.document.DocumentCategory;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.casedocuments.OtherDocumentsHandler;

import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseDocumentParty.INTERVENER_TWO;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.document.CaseDocumentCollectionType.INTERVENER_TWO_OTHER_COLLECTION;

@Component
public class IntervenerTwoOtherDocumentsHandler extends OtherDocumentsHandler {

    public IntervenerTwoOtherDocumentsHandler() {
        super(INTERVENER_TWO_OTHER_COLLECTION, INTERVENER_TWO);
    }

    @Override
    protected DocumentCategory getDocumentCategoryFromDocumentType(CaseDocumentType caseDocumentType) {
        switch (caseDocumentType) {
            case OTHER, FORM_F, CARE_PLAN, PENSION_PLAN -> {
                return DocumentCategory.INTERVENER_DOCUMENTS;
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
