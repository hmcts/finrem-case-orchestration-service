package uk.gov.hmcts.reform.finrem.caseorchestration.service.casedocuments.intervenertwo;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.FeatureToggleService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.casedocuments.CaseSummariesHandler;

import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseDocumentParty.INTERVENER_TWO;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.document.CaseDocumentCollectionType.INTERVENER_TWO_SUMMARIES_COLLECTION;

@Component
public class IntervenerTwoCaseSummariesHandler extends CaseSummariesHandler {

    private final FeatureToggleService featureToggleService;

    @Autowired
    public IntervenerTwoCaseSummariesHandler(FeatureToggleService featureToggleService) {
        super(INTERVENER_TWO_SUMMARIES_COLLECTION, INTERVENER_TWO, featureToggleService);
        this.featureToggleService = featureToggleService;
    }
}
