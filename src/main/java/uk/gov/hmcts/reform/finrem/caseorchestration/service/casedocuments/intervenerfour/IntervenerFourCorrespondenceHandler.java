package uk.gov.hmcts.reform.finrem.caseorchestration.service.casedocuments.intervenerfour;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.casedocuments.CorrespondenceHandler;

import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.INTV_FOUR_CORRESPONDENCE_COLLECTION;

@Component
public class IntervenerFourCorrespondenceHandler extends CorrespondenceHandler {

    @Autowired
    public IntervenerFourCorrespondenceHandler(ObjectMapper mapper) {
        super(INTV_FOUR_CORRESPONDENCE_COLLECTION, INTERVENER_FOUR, mapper);
    }
}
