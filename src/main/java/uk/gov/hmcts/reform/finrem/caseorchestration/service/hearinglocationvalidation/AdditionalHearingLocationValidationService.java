package uk.gov.hmcts.reform.finrem.caseorchestration.service.hearinglocationvalidation;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.AdditionalHearingDirectionsCollectionElement;

@Service
public class AdditionalHearingLocationValidationService
    extends HearingLocationValidationService<AdditionalHearingDirectionsCollectionElement> {

    public AdditionalHearingLocationValidationService(ObjectMapper objectMapper) {
        super(objectMapper, AdditionalHearingDirectionsCollectionElement.class);
    }
}
