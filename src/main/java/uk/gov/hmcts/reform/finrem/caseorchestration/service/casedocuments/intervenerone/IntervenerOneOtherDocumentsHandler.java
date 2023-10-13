package uk.gov.hmcts.reform.finrem.caseorchestration.service.casedocuments.intervenerone;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseDocumentType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.document.DocumentCategory;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.FeatureToggleService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.casedocuments.OtherDocumentsHandler;

import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseDocumentParty.INTERVENER_ONE;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.document.CaseDocumentCollectionType.INTERVENER_ONE_OTHER_COLLECTION;

@Component
public class IntervenerOneOtherDocumentsHandler extends OtherDocumentsHandler {

    public IntervenerOneOtherDocumentsHandler(FeatureToggleService featureToggleService) {
        super(INTERVENER_ONE_OTHER_COLLECTION, INTERVENER_ONE, featureToggleService);
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
