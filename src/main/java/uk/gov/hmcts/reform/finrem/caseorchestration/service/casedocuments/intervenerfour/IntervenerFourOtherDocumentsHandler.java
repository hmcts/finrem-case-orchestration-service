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

    private final FeatureToggleService featureToggleService;

    public IntervenerFourOtherDocumentsHandler(FeatureToggleService featureToggleService) {
        super(INTERVENER_FOUR_OTHER_COLLECTION, INTERVENER_FOUR, featureToggleService);
        this.featureToggleService = featureToggleService;
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
