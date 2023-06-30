package uk.gov.hmcts.reform.finrem.caseorchestration.service.casedocuments.intervenerfour;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.casedocuments.HearingBundleHandler;

import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseDocumentParty.INTERVENER_FOUR;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.document.ManageCaseDocumentsCollectionType.INTV_FOUR_HEARING_BUNDLES_COLLECTION;

@Component
public class IntervenerFourHearingBundleHandler extends HearingBundleHandler {

    @Autowired
    public IntervenerFourHearingBundleHandler() {
        super(INTV_FOUR_HEARING_BUNDLES_COLLECTION, INTERVENER_FOUR);
    }
}
