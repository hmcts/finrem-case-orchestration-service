package uk.gov.hmcts.reform.finrem.caseorchestration.service.casedocuments.respondent;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseDocumentParty;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseDocumentType;
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
            featureToggleService
        );
    }

    @Override
    protected DocumentCategory getDocumentCategoryFromDocumentType(
        CaseDocumentType caseDocumentType,
        CaseDocumentParty caseDocumentParty) {

        if (CaseDocumentType.UPDATING_DISCLOSURE.equals(caseDocumentType)) {
            return DocumentCategory.RESPONDENT_DOCUMENTS_UPDATING_DISCLOSURE;
        }

        return DocumentCategory.UNCATEGORISED;
    }
}
