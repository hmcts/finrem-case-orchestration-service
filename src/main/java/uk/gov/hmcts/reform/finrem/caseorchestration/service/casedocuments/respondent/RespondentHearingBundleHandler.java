package uk.gov.hmcts.reform.finrem.caseorchestration.service.casedocuments.respondent;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.casedocuments.HearingBundleHandler;

import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.RESP_HEARING_BUNDLES_COLLECTION;

@Component
public class RespondentHearingBundleHandler extends HearingBundleHandler {

    @Autowired
    public RespondentHearingBundleHandler(ObjectMapper mapper) {
        super(RESP_HEARING_BUNDLES_COLLECTION, RESPONDENT, mapper);
    }

}
