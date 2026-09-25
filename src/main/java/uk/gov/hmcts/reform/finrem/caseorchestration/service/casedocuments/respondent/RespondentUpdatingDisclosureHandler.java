package uk.gov.hmcts.reform.finrem.caseorchestration.service.casedocuments.respondent;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseDocumentParty;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.document.CaseDocumentCollectionType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.document.DocumentCategory;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.FeatureToggleService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.casedocuments.UpdatingDisclosureHandler;

@Component
public class RespondentUpdatingDisclosureHandler extends UpdatingDisclosureHandler {

    public RespondentUpdatingDisclosureHandler(FeatureToggleService featureToggleService) {
        super(
                CaseDocumentCollectionType.RESP_UPDATING_DISCLOSURE_COLLECTION,
                CaseDocumentParty.RESPONDENT,
                DocumentCategory.RESPONDENT_DOCUMENTS_UPDATING_DISCLOSURE,
                featureToggleService
        );
    }
}
