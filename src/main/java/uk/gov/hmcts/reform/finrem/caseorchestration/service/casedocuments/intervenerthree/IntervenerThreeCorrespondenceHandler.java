package uk.gov.hmcts.reform.finrem.caseorchestration.service.casedocuments.intervenerthree;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.casedocuments.CorrespondenceHandler;

import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.INTV_THREE_CORRESPONDENCE_COLLECTION;

@Component
public class IntervenerThreeCorrespondenceHandler extends CorrespondenceHandler {

    @Autowired
    public IntervenerThreeCorrespondenceHandler(ObjectMapper mapper) {
        super(INTV_THREE_CORRESPONDENCE_COLLECTION, INTERVENER_THREE, mapper);
    }
}
