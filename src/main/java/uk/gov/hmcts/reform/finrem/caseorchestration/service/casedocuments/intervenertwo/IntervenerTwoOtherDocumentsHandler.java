package uk.gov.hmcts.reform.finrem.caseorchestration.service.casedocuments.intervenertwo;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.casedocuments.OtherDocumentsHandler;

import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.INTV_TWO_OTHER_COLLECTION;

@Component
public class IntervenerTwoOtherDocumentsHandler extends OtherDocumentsHandler {

    @Autowired
    public IntervenerTwoOtherDocumentsHandler(ObjectMapper mapper) {
        super(INTV_TWO_OTHER_COLLECTION, INTERVENER_TWO, mapper);
    }
}
