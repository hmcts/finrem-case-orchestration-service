package uk.gov.hmcts.reform.finrem.caseorchestration.service.casedocuments.intervenerfour;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.casedocuments.FormEExhibitsHandler;

import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.INTV_FOUR_FORM_E_EXHIBITS_COLLECTION;

@Component
public class IntervenerFourFormEExhibitsHandler extends FormEExhibitsHandler {

    @Autowired
    public IntervenerFourFormEExhibitsHandler(ObjectMapper mapper) {
        super(INTV_FOUR_FORM_E_EXHIBITS_COLLECTION, INTERVENER_FOUR, mapper);
    }

}
