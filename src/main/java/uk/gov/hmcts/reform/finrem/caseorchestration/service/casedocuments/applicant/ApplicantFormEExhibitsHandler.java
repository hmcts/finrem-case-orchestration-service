package uk.gov.hmcts.reform.finrem.caseorchestration.service.casedocuments.applicant;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.casedocuments.FormEExhibitsHandler;

import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.APP_FORM_E_EXHIBITS_COLLECTION;

@Component
public class ApplicantFormEExhibitsHandler extends FormEExhibitsHandler {

    @Autowired
    public ApplicantFormEExhibitsHandler(ObjectMapper mapper) {
        super(APP_FORM_E_EXHIBITS_COLLECTION, APPLICANT, mapper);
    }

}
