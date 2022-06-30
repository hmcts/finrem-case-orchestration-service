package uk.gov.hmcts.reform.finrem.caseorchestration.service.casedocuments.respondent;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.casedocuments.CorrespondenceHandler;

import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.RESP_CORRESPONDENCE_COLLECTION;

@Component
public class RespondentCorrespondenceHandler extends CorrespondenceHandler {

    @Autowired
    public RespondentCorrespondenceHandler(ObjectMapper mapper) {
        super(RESP_CORRESPONDENCE_COLLECTION, RESPONDENT, mapper);
    }
}
