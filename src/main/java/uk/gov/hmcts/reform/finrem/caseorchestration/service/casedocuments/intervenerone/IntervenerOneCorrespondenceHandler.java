package uk.gov.hmcts.reform.finrem.caseorchestration.service.casedocuments.intervenerone;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.casedocuments.CorrespondenceHandler;

import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.INTV_ONE_CORRESPONDENCE_COLLECTION;

@Component
public class IntervenerOneCorrespondenceHandler extends CorrespondenceHandler {

    @Autowired
    public IntervenerOneCorrespondenceHandler(ObjectMapper mapper) {
        super(INTV_ONE_CORRESPONDENCE_COLLECTION, INTERVENER_ONE, mapper);
    }
}
