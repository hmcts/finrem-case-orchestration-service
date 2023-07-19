package uk.gov.hmcts.reform.finrem.caseorchestration.service.casedocuments.applicant;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.casedocuments.HearingBundleHandler;

import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.APP_HEARING_BUNDLES_COLLECTION;

@Component
public class ApplicantHearingBundleHandler extends HearingBundleHandler {

    @Autowired
    public ApplicantHearingBundleHandler(ObjectMapper mapper) {
        super(APP_HEARING_BUNDLES_COLLECTION, APPLICANT, mapper);
    }
}
