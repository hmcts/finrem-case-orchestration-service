package uk.gov.hmcts.reform.finrem.caseorchestration.service.casedocuments.intervenerthree;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.casedocuments.HearingBundleHandler;

import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseDocumentParty.INTERVENER_THREE;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.document.CaseDocumentCollectionType.INTERVENER_THREE_HEARING_BUNDLES_COLLECTION;

@Component
public class IntervenerThreeHearingBundleHandler extends HearingBundleHandler {

    @Autowired
    public IntervenerThreeHearingBundleHandler() {
        super(INTERVENER_THREE_HEARING_BUNDLES_COLLECTION, INTERVENER_THREE);
    }
}
