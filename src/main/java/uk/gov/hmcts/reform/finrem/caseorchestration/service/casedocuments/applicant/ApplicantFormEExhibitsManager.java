package uk.gov.hmcts.reform.finrem.caseorchestration.service.casedocuments.applicant;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.casedocuments.FormEExhibitsManager;

import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.APP_FORM_E_EXHIBITS_COLLECTION;

@Component
public class ApplicantFormEExhibitsManager extends FormEExhibitsManager {

    @Autowired
    public ApplicantFormEExhibitsManager(ObjectMapper mapper) {
        super(APP_FORM_E_EXHIBITS_COLLECTION, APPLICANT, mapper);
    }

}
