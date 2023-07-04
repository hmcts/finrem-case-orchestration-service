package uk.gov.hmcts.reform.finrem.caseorchestration.service.casedocuments.intervenerone;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.casedocuments.FormEExhibitsHandler;

import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.INTV_ONE_FORM_E_EXHIBITS_COLLECTION;

@Component
public class IntervenerOneFormEExhibitsHandler extends FormEExhibitsHandler {

    @Autowired
    public IntervenerOneFormEExhibitsHandler(ObjectMapper mapper) {
        super(INTV_ONE_FORM_E_EXHIBITS_COLLECTION, INTERVENER_ONE, mapper);
    }

}
