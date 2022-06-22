package uk.gov.hmcts.reform.finrem.caseorchestration.service.casedocuments.respondent;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.casedocuments.OtherDocumentsHandler;

import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.RESP_OTHER_COLLECTION;

@Component
public class RespondentOtherDocumentsHandler extends OtherDocumentsHandler {

    @Autowired
    public RespondentOtherDocumentsHandler(ObjectMapper mapper) {
        super(RESP_OTHER_COLLECTION, RESPONDENT, mapper);
    }
}
