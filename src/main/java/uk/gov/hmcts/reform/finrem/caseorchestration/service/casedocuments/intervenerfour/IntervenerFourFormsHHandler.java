package uk.gov.hmcts.reform.finrem.caseorchestration.service.casedocuments.intervenerfour;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.casedocuments.FormsHHandler;

import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.INTV_FOUR_FORMS_H_COLLECTION;

@Component
public class IntervenerFourFormsHHandler extends FormsHHandler {

    @Autowired
    public IntervenerFourFormsHHandler(ObjectMapper mapper) {
        super(INTV_FOUR_FORMS_H_COLLECTION, INTERVENER_FOUR, mapper);
    }
}
