package uk.gov.hmcts.reform.finrem.caseorchestration.service.casedocuments.intervenertwo;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.casedocuments.FormEExhibitsHandler;

import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.INTV_TWO_FORM_E_EXHIBITS_COLLECTION;

@Component
public class IntervenerTwoFormEExhibitsHandler extends FormEExhibitsHandler {

    @Autowired
    public IntervenerTwoFormEExhibitsHandler(ObjectMapper mapper) {
        super(INTV_TWO_FORM_E_EXHIBITS_COLLECTION, INTERVENER_TWO, mapper);
    }

}
