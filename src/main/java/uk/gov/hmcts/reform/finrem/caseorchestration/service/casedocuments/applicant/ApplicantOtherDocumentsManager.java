package uk.gov.hmcts.reform.finrem.caseorchestration.service.casedocuments.applicant;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.casedocuments.OtherDocumentsManager;

import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.APP_OTHER_COLLECTION;

@Component
public class ApplicantOtherDocumentsManager extends OtherDocumentsManager {

    @Autowired
    public ApplicantOtherDocumentsManager(ObjectMapper mapper) {
        super(APP_OTHER_COLLECTION, APPLICANT, mapper);
    }
}
