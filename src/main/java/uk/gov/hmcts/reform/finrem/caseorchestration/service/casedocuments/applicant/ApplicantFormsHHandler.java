package uk.gov.hmcts.reform.finrem.caseorchestration.service.casedocuments.applicant;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.casedocuments.FormsHHandler;

import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.APP_FORMS_H_COLLECTION;

@Component
public class ApplicantFormsHHandler extends FormsHHandler {

    @Autowired
    public ApplicantFormsHHandler(ObjectMapper mapper) {
        super(APP_FORMS_H_COLLECTION, APPLICANT, mapper);
    }
}
