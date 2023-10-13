package uk.gov.hmcts.reform.finrem.caseorchestration.service.casedocuments.applicant;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseDocumentParty;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.document.CaseDocumentCollectionType;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.FeatureToggleService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.casedocuments.HearingBundleHandler;

@Service
public class ApplicantHearingBundleHandler extends HearingBundleHandler {

    private final FeatureToggleService featureToggleService;

    @Autowired
    public ApplicantHearingBundleHandler(FeatureToggleService featureToggleService) {
        super(CaseDocumentCollectionType.APP_HEARING_BUNDLES_COLLECTION,
            CaseDocumentParty.APPLICANT, featureToggleService);
        this.featureToggleService = featureToggleService;
    }
}
