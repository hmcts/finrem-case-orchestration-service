package uk.gov.hmcts.reform.finrem.caseorchestration.mapper.letterdetails.address;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.IntervenerConstant;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.wrapper.IntervenerOne;

@Component
@Slf4j
public class IntervenerOneLetterAddresseeGenerator extends IntervenerLetterAddresseeGenerator<IntervenerOne> {

    @Autowired
    public IntervenerOneLetterAddresseeGenerator(ObjectMapper objectMapper) {
        super(objectMapper);
    }

    @Override
    protected String getIntervenerFieldName() {
        return IntervenerConstant.INTERVENER_ONE;
    }

    @Override
    protected IntervenerOne getIntervenerWrapper(FinremCaseDetails caseDetails) {
        return caseDetails.getData().getIntervenerOne();
    }

    protected IntervenerOne getIntervenerWrapper(CaseDetails caseDetails) {
        IntervenerOne intervenerWrapper =
            objectMapper.convertValue(caseDetails.getData().get(getIntervenerFieldName()), new TypeReference<>() {
            });
        return intervenerWrapper;
    }


}
