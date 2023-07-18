package uk.gov.hmcts.reform.finrem.caseorchestration.service.casedocuments.intervenerthree;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.casedocuments.OtherDocumentsHandler;

import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.INTV_THREE_OTHER_COLLECTION;

@Component
public class IntervenerThreeOtherDocumentsHandler extends OtherDocumentsHandler {

    @Autowired
    public IntervenerThreeOtherDocumentsHandler(ObjectMapper mapper) {
        super(INTV_THREE_OTHER_COLLECTION, INTERVENER_THREE, mapper);
    }
}
