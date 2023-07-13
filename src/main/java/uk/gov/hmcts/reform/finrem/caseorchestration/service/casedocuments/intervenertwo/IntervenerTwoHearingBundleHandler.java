package uk.gov.hmcts.reform.finrem.caseorchestration.service.casedocuments.intervenertwo;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.casedocuments.HearingBundleHandler;

import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseDocumentParty.INTERVENER_TWO;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.document.CaseDocumentCollectionType.INTERVENER_TWO_HEARING_BUNDLES_COLLECTION;

@Component
public class IntervenerTwoHearingBundleHandler extends HearingBundleHandler {

    @Autowired
    public IntervenerTwoHearingBundleHandler() {
        super(INTERVENER_TWO_HEARING_BUNDLES_COLLECTION, INTERVENER_TWO);
    }
}
