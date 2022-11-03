package uk.gov.hmcts.reform.finrem.caseorchestration.service.hearinglocationvalidation;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.InterimHearingData;

@Service
public class InterimHearingLocationValidationService extends HearingLocationValidationService<InterimHearingData> {

    public InterimHearingLocationValidationService(ObjectMapper objectMapper) {
        super(objectMapper, InterimHearingData.class);
    }
}
