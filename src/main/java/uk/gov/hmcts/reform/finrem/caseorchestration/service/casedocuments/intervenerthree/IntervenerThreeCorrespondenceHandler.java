package uk.gov.hmcts.reform.finrem.caseorchestration.service.casedocuments.intervenerthree;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.FeatureToggleService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.casedocuments.CorrespondenceHandler;

import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseDocumentParty.INTERVENER_THREE;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.document.CaseDocumentCollectionType.INTERVENER_THREE_CORRESPONDENCE_COLLECTION;

@Component
public class IntervenerThreeCorrespondenceHandler extends CorrespondenceHandler {

    private final FeatureToggleService featureToggleService;

    @Autowired
    public IntervenerThreeCorrespondenceHandler(FeatureToggleService featureToggleService) {
        super(INTERVENER_THREE_CORRESPONDENCE_COLLECTION, INTERVENER_THREE, featureToggleService);
        this.featureToggleService = featureToggleService;
    }
}
