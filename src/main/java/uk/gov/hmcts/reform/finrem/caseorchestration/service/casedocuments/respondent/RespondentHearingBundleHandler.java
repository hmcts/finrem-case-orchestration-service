package uk.gov.hmcts.reform.finrem.caseorchestration.service.casedocuments.respondent;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseDocumentParty;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.document.CaseDocumentCollectionType;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.FeatureToggleService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.casedocuments.HearingBundleHandler;

@Service
public class RespondentHearingBundleHandler extends HearingBundleHandler {

    @Autowired
    public RespondentHearingBundleHandler(FeatureToggleService featureToggleService) {
        super(CaseDocumentCollectionType.RESP_HEARING_BUNDLES_COLLECTION,
                CaseDocumentParty.RESPONDENT, featureToggleService);
    }

}
