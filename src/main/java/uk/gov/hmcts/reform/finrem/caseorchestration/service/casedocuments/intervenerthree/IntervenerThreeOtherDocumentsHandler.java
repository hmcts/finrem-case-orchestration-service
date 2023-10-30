package uk.gov.hmcts.reform.finrem.caseorchestration.service.casedocuments.intervenerthree;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseDocumentType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.document.DocumentCategory;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.FeatureToggleService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.casedocuments.OtherDocumentsHandler;

import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseDocumentParty.INTERVENER_THREE;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.document.CaseDocumentCollectionType.INTERVENER_THREE_OTHER_COLLECTION;

@Component
public class IntervenerThreeOtherDocumentsHandler extends OtherDocumentsHandler {

    public IntervenerThreeOtherDocumentsHandler(FeatureToggleService featureToggleService) {
        super(INTERVENER_THREE_OTHER_COLLECTION, INTERVENER_THREE, featureToggleService);
    }

    @Override
    protected DocumentCategory getDocumentCategoryFromDocumentType(CaseDocumentType caseDocumentType) {
        switch (caseDocumentType) {
            case OTHER -> {
                return DocumentCategory.INTERVENER_DOCUMENTS_INTERVENER_3_MISCELLANEOUS_OR_OTHER;
            }
            default -> {
                return DocumentCategory.INTERVENER_DOCUMENTS_INTERVENER_3;
            }
        }
    }
}
