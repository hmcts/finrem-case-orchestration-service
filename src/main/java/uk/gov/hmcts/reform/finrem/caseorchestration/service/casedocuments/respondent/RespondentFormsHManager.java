package uk.gov.hmcts.reform.finrem.caseorchestration.service.casedocuments.respondent;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.casedocuments.FormsHManager;

import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.RESP_FORM_H_COLLECTION;

@Component
public class RespondentFormsHManager extends FormsHManager {

    @Autowired
    public RespondentFormsHManager(ObjectMapper mapper) {
        super(RESP_FORM_H_COLLECTION, RESPONDENT, mapper);
    }
}
