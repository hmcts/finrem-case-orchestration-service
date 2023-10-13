package uk.gov.hmcts.reform.finrem.caseorchestration.service.casedocuments.intervenerone;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.FeatureToggleService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.casedocuments.HearingBundleHandler;

import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseDocumentParty.INTERVENER_ONE;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.document.CaseDocumentCollectionType.INTERVENER_ONE_HEARING_BUNDLES_COLLECTION;

@Component
public class IntervenerOneHearingBundleHandler extends HearingBundleHandler {

    @Autowired
    public IntervenerOneHearingBundleHandler(FeatureToggleService featureToggleService) {
        super(INTERVENER_ONE_HEARING_BUNDLES_COLLECTION, INTERVENER_ONE, featureToggleService);
    }
}
