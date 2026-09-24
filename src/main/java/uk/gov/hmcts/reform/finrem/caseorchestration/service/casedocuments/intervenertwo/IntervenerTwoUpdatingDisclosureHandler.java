package uk.gov.hmcts.reform.finrem.caseorchestration.service.casedocuments.intervenertwo;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseDocumentParty;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseDocumentType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.document.CaseDocumentCollectionType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.document.DocumentCategory;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.FeatureToggleService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.casedocuments.UpdatingDisclosureHandler;

@Component
public class IntervenerTwoUpdatingDisclosureHandler extends UpdatingDisclosureHandler {

    public IntervenerTwoUpdatingDisclosureHandler(FeatureToggleService featureToggleService) {
        super(
            CaseDocumentCollectionType.INTERVENER_TWO_UPDATING_DISCLOSURE_COLLECTION,
            CaseDocumentParty.INTERVENER_TWO,
            featureToggleService
        );
    }

    @Override
    protected DocumentCategory getDocumentCategoryFromDocumentType(
        CaseDocumentType caseDocumentType,
        CaseDocumentParty caseDocumentParty) {

        if (CaseDocumentType.UPDATING_DISCLOSURE.equals(caseDocumentType)) {
            return DocumentCategory.INTERVENER_DOCUMENTS_INTERVENER_2_SUPPORTING_DOCUMENTS;
        }

        return DocumentCategory.UNCATEGORISED;
    }
}
