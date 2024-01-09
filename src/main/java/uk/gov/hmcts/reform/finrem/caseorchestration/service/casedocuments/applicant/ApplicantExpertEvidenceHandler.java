package uk.gov.hmcts.reform.finrem.caseorchestration.service.casedocuments.applicant;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseDocumentParty;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.document.CaseDocumentCollectionType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.document.DocumentCategory;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.FeatureToggleService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.casedocuments.ExpertEvidenceHandler;

@Component
public class ApplicantExpertEvidenceHandler extends ExpertEvidenceHandler {

    @Autowired
    public ApplicantExpertEvidenceHandler(FeatureToggleService featureToggleService) {
        super(CaseDocumentCollectionType.APP_EXPERT_EVIDENCE_COLLECTION,
            CaseDocumentParty.APPLICANT, featureToggleService);
    }

    @Override
    protected DocumentCategory getHearingDocumentsFamilyHomeValuation() {
        return DocumentCategory.HEARING_DOCUMENTS_APPLICANT_FAMILY_HOME_VALUATION;
    }
}
