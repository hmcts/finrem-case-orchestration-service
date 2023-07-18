package uk.gov.hmcts.reform.finrem.caseorchestration.service.casedocuments.intervenertwo;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.casedocuments.FormsHHandler;

import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.INTV_TWO_FORMS_H_COLLECTION;

@Component
public class IntervenerTwoFormsHHandler extends FormsHHandler {

    @Autowired
    public IntervenerTwoFormsHHandler(ObjectMapper mapper) {
        super(INTV_TWO_FORMS_H_COLLECTION, INTERVENER_TWO, mapper);
    }
}
