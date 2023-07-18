package uk.gov.hmcts.reform.finrem.caseorchestration.service.casedocuments.applicant;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.casedocuments.OtherDocumentsHandler;

import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.APP_OTHER_COLLECTION;

@Component
public class ApplicantOtherDocumentsHandler extends OtherDocumentsHandler {

    @Autowired
    public ApplicantOtherDocumentsHandler(ObjectMapper mapper) {
        super(APP_OTHER_COLLECTION, APPLICANT, mapper);
    }
}
