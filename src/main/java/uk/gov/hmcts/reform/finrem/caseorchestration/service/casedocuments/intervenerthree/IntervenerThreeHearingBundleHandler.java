package uk.gov.hmcts.reform.finrem.caseorchestration.service.casedocuments.intervenerthree;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.casedocuments.HearingBundleHandler;

import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.INTV_THREE_HEARING_BUNDLES_COLLECTION;

@Component
public class IntervenerThreeHearingBundleHandler extends HearingBundleHandler {

    @Autowired
    public IntervenerThreeHearingBundleHandler(ObjectMapper mapper) {
        super(INTV_THREE_HEARING_BUNDLES_COLLECTION, INTERVENER_THREE, mapper);
    }
}
