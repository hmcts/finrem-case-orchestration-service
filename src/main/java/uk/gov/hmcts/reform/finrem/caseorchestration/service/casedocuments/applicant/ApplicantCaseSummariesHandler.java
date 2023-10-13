package uk.gov.hmcts.reform.finrem.caseorchestration.service.casedocuments.applicant;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseDocumentParty;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.document.CaseDocumentCollectionType;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.FeatureToggleService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.casedocuments.CaseSummariesHandler;

@Service
public class ApplicantCaseSummariesHandler extends CaseSummariesHandler {

    @Autowired
    public ApplicantCaseSummariesHandler(FeatureToggleService featureToggleService) {
        super(CaseDocumentCollectionType.APP_CASE_SUMMARIES_COLLECTION,
            CaseDocumentParty.APPLICANT, featureToggleService);
    }
}
