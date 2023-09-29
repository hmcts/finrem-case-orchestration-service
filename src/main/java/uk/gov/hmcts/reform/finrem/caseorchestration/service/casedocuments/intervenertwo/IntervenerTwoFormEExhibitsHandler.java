package uk.gov.hmcts.reform.finrem.caseorchestration.service.casedocuments.intervenertwo;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseDocumentType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.document.DocumentCategory;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.casedocuments.FormEExhibitsHandler;

import java.util.Objects;

import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseDocumentParty.INTERVENER_TWO;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.document.CaseDocumentCollectionType.INTERVENER_TWO_FORM_E_EXHIBITS_COLLECTION;

@Component
public class IntervenerTwoFormEExhibitsHandler extends FormEExhibitsHandler {

    public IntervenerTwoFormEExhibitsHandler() {
        super(INTERVENER_TWO_FORM_E_EXHIBITS_COLLECTION, INTERVENER_TWO);
    }

    @Override
    protected DocumentCategory getDocumentCategoryFromDocumentType(CaseDocumentType caseDocumentType) {
        if (Objects.requireNonNull(caseDocumentType) == CaseDocumentType.APPLICANT_FORM_E) {
            return DocumentCategory.INTERVENER_DOCUMENTS;
        }
        return DocumentCategory.UNCATEGORISED;
    }

}
