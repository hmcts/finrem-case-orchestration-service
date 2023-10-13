package uk.gov.hmcts.reform.finrem.caseorchestration.service.casedocuments.intervenerone;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.FeatureToggleService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.casedocuments.CorrespondenceHandler;

import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseDocumentParty.INTERVENER_ONE;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.document.CaseDocumentCollectionType.INTERVENER_ONE_CORRESPONDENCE_COLLECTION;

@Component
public class IntervenerOneCorrespondenceHandler extends CorrespondenceHandler {

    private final FeatureToggleService featureToggleService;

    @Autowired
    public IntervenerOneCorrespondenceHandler(FeatureToggleService featureToggleService) {
        super(INTERVENER_ONE_CORRESPONDENCE_COLLECTION, INTERVENER_ONE, featureToggleService);
        this.featureToggleService = featureToggleService;
    }
}
