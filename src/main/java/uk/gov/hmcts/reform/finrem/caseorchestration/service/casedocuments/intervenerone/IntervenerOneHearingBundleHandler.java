package uk.gov.hmcts.reform.finrem.caseorchestration.service.casedocuments.intervenerone;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.casedocuments.HearingBundleHandler;

import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.INTV_ONE_HEARING_BUNDLES_COLLECTION;

@Component
public class IntervenerOneHearingBundleHandler extends HearingBundleHandler {

    @Autowired
    public IntervenerOneHearingBundleHandler(ObjectMapper mapper) {
        super(INTV_ONE_HEARING_BUNDLES_COLLECTION, INTERVENER_ONE, mapper);
    }
}
