package uk.gov.hmcts.reform.finrem.caseorchestration.service.casedocuments.intervenerfour;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseDocumentParty;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.document.CaseDocumentCollectionType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.document.DocumentCategory;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.FeatureToggleService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.casedocuments.UpdatingDisclosureHandler;

@Component
public class IntervenerFourUpdatingDisclosureHandler extends UpdatingDisclosureHandler {

    public IntervenerFourUpdatingDisclosureHandler(FeatureToggleService featureToggleService) {
        super(
                CaseDocumentCollectionType.INTERVENER_FOUR_UPDATING_DISCLOSURE_COLLECTION,
                CaseDocumentParty.INTERVENER_FOUR,
                DocumentCategory.INTERVENER_DOCUMENTS_INTERVENER_4_SUPPORTING_DOCUMENTS,
                featureToggleService
        );
    }
}
