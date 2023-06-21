package uk.gov.hmcts.reform.finrem.caseorchestration.service.casedocuments.intervenerfour;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.casedocuments.OtherDocumentsHandler;

import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.INTV_FOUR_OTHER_COLLECTION;

@Component
public class IntervenerFourOtherDocumentsHandler extends OtherDocumentsHandler {

    @Autowired
    public IntervenerFourOtherDocumentsHandler(ObjectMapper mapper) {
        super(INTV_FOUR_OTHER_COLLECTION, INTERVENER_FOUR, mapper);
    }
}
