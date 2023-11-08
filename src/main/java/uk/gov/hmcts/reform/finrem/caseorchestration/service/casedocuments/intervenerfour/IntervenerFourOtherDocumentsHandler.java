package uk.gov.hmcts.reform.finrem.caseorchestration.service.casedocuments.intervenerfour;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseDocumentType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.document.DocumentCategory;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.FeatureToggleService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.casedocuments.OtherDocumentsHandler;

import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseDocumentParty.INTERVENER_FOUR;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.document.CaseDocumentCollectionType.INTERVENER_FOUR_OTHER_COLLECTION;

@Component
public class IntervenerFourOtherDocumentsHandler extends OtherDocumentsHandler {

    public IntervenerFourOtherDocumentsHandler(FeatureToggleService featureToggleService) {
        super(INTERVENER_FOUR_OTHER_COLLECTION, INTERVENER_FOUR, featureToggleService);
    }

    @Override
    protected DocumentCategory getDocumentCategoryFromDocumentType(CaseDocumentType caseDocumentType) {
        switch (caseDocumentType) {
            case OTHER -> {
                return DocumentCategory.INTERVENER_DOCUMENTS_INTERVENER_4_MISCELLANEOUS_OR_OTHER;
            }
            case PENSION_PLAN -> {
                return DocumentCategory.INTERVENER_DOCUMENTS_INTERVENER_4_PENSION_PLAN;
            }
            case FORM_B, FORM_F, CARE_PLAN -> {
                return DocumentCategory.ADMINISTRATIVE_DOCUMENTS_OTHER;
            }
            default -> {
                return DocumentCategory.INTERVENER_DOCUMENTS_INTERVENER_4;
            }
        }
    }
}
