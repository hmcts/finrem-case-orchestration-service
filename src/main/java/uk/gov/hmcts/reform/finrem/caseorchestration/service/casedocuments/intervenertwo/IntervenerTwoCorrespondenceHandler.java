package uk.gov.hmcts.reform.finrem.caseorchestration.service.casedocuments.intervenertwo;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.casedocuments.CorrespondenceHandler;

import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.INTV_TWO_CORRESPONDENCE_COLLECTION;

@Component
public class IntervenerTwoCorrespondenceHandler extends CorrespondenceHandler {

    @Autowired
    public IntervenerTwoCorrespondenceHandler(ObjectMapper mapper) {
        super(INTV_TWO_CORRESPONDENCE_COLLECTION, INTERVENER_TWO, mapper);
    }
}
