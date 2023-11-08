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
            case OTHER -> {
                return DocumentCategory.INTERVENER_DOCUMENTS_INTERVENER_1_MISCELLANEOUS_OR_OTHER;
            }
            case PENSION_PLAN -> {
                return DocumentCategory.INTERVENER_DOCUMENTS_INTERVENER_1_PENSION_PLAN;
            }
            case FORM_B, FORM_F, CARE_PLAN -> {
                return DocumentCategory.ADMINISTRATIVE_DOCUMENTS_OTHER;
            }
            default -> {
                return DocumentCategory.INTERVENER_DOCUMENTS_INTERVENER_1;
            }
        }
    }
}
