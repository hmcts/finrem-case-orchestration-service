package uk.gov.hmcts.reform.finrem.caseorchestration.service.casedocuments.intervenerone;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.casedocuments.FormsHHandler;

import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.INTV_ONE_FORMS_H_COLLECTION;

@Component
public class IntervenerOneFormsHHandler extends FormsHHandler {

    @Autowired
    public IntervenerOneFormsHHandler(ObjectMapper mapper) {
        super(INTV_ONE_FORMS_H_COLLECTION, INTERVENER_ONE, mapper);
    }
}
