package uk.gov.hmcts.reform.finrem.caseorchestration.service.casedocuments.applicant;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.casedocuments.CorrespondenceHandler;

import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.APP_CORRESPONDENCE_COLLECTION;

@Component
public class ApplicantCorrespondenceHandler extends CorrespondenceHandler {

    @Autowired
    public ApplicantCorrespondenceHandler(ObjectMapper mapper) {
        super(APP_CORRESPONDENCE_COLLECTION, APPLICANT, mapper);
    }
}
