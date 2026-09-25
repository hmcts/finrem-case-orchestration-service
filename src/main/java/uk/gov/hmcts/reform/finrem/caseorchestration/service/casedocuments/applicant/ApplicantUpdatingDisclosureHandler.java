package uk.gov.hmcts.reform.finrem.caseorchestration.service.casedocuments.applicant;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseDocumentParty;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.document.CaseDocumentCollectionType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.document.DocumentCategory;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.FeatureToggleService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.casedocuments.UpdatingDisclosureHandler;

@Component
public class ApplicantUpdatingDisclosureHandler extends UpdatingDisclosureHandler {

    public ApplicantUpdatingDisclosureHandler(FeatureToggleService featureToggleService) {
        super(
                CaseDocumentCollectionType.APP_UPDATING_DISCLOSURE_COLLECTION,
                CaseDocumentParty.APPLICANT,
                DocumentCategory.APPLICANT_DOCUMENTS_UPDATING_DISCLOSURE,
                featureToggleService
        );
    }
}
