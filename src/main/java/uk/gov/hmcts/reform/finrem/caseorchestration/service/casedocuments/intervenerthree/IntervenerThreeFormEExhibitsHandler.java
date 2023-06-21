package uk.gov.hmcts.reform.finrem.caseorchestration.service.casedocuments.intervenerthree;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.casedocuments.FormEExhibitsHandler;

import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.INTV_THREE_FORM_E_EXHIBITS_COLLECTION;

@Component
public class IntervenerThreeFormEExhibitsHandler extends FormEExhibitsHandler {

    @Autowired
    public IntervenerThreeFormEExhibitsHandler(ObjectMapper mapper) {
        super(INTV_THREE_FORM_E_EXHIBITS_COLLECTION, INTERVENER_THREE, mapper);
    }

}
