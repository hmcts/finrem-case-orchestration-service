package uk.gov.hmcts.reform.finrem.caseorchestration.service.casedocuments.intervenerthree;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseDocumentParty;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.document.CaseDocumentCollectionType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.document.DocumentCategory;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.FeatureToggleService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.casedocuments.UpdatingDisclosureHandler;

@Component
public class IntervenerThreeUpdatingDisclosureHandler extends UpdatingDisclosureHandler {

    public IntervenerThreeUpdatingDisclosureHandler(FeatureToggleService featureToggleService) {
        super(
                CaseDocumentCollectionType.INTERVENER_THREE_UPDATING_DISCLOSURE_COLLECTION,
                CaseDocumentParty.INTERVENER_THREE,
                DocumentCategory.INTERVENER_DOCUMENTS_INTERVENER_3_SUPPORTING_DOCUMENTS,
                featureToggleService
        );
    }
}
