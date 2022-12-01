package uk.gov.hmcts.reform.finrem.caseorchestration.service.casedocuments.respondent;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.casedocuments.FormEExhibitsManager;

import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.RESP_FORM_E_EXHIBITS_COLLECTION;

@Component
public class RespondentFormEExhibitsManager extends FormEExhibitsManager {

    @Autowired
    public RespondentFormEExhibitsManager(ObjectMapper mapper) {
        super(RESP_FORM_E_EXHIBITS_COLLECTION, RESPONDENT, mapper);
    }

}
