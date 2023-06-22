package uk.gov.hmcts.reform.finrem.caseorchestration.service.casedocuments.intervenertwo;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.casedocuments.HearingBundleHandler;

import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.INTV_TWO_HEARING_BUNDLES_COLLECTION;

@Component
public class IntervenerTwoHearingBundleHandler extends HearingBundleHandler {

    @Autowired
    public IntervenerTwoHearingBundleHandler(ObjectMapper mapper) {
        super(INTV_TWO_HEARING_BUNDLES_COLLECTION, INTERVENER_TWO, mapper);
    }
}
