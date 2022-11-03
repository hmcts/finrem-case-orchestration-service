package uk.gov.hmcts.reform.finrem.caseorchestration.service.hearinglocationvalidation;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.DirectionDetailsCollectionData;

@Service
public class DirectionDetailLocationValidationService
    extends HearingLocationValidationService<DirectionDetailsCollectionData> {

    public DirectionDetailLocationValidationService(ObjectMapper objectMapper) {
        super(objectMapper, DirectionDetailsCollectionData.class);
    }
}
