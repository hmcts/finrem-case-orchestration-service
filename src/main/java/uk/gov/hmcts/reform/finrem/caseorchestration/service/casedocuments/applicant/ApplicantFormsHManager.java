package uk.gov.hmcts.reform.finrem.caseorchestration.service.casedocuments.applicant;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.casedocuments.FormsHManager;

import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.APP_FORMS_H_COLLECTION;

@Component
public class ApplicantFormsHManager extends FormsHManager {

    @Autowired
    public ApplicantFormsHManager(ObjectMapper mapper) {
        super(APP_FORMS_H_COLLECTION, APPLICANT, mapper);
    }
}
