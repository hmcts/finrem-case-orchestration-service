package uk.gov.hmcts.reform.finrem.caseorchestration.mapper.letterdetails.address;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.IntervenerConstant;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.wrapper.IntervenerTwo;

@Component
@Slf4j
public class IntervenerTwoLetterAddresseeGenerator extends IntervenerLetterAddresseeGenerator<IntervenerTwo> {

    @Autowired
    public IntervenerTwoLetterAddresseeGenerator(ObjectMapper objectMapper) {
        super(objectMapper);
    }

    @Override
    protected String getIntervenerFieldName() {
        return IntervenerConstant.INTERVENER_TWO;
    }

    @Override
    protected IntervenerTwo getIntervenerWrapper(FinremCaseDetails caseDetails) {
        return caseDetails.getData().getIntervenerTwo();
    }

    @Override
    protected IntervenerTwo getIntervenerWrapper(CaseDetails caseDetails) {
        IntervenerTwo intervenerWrapper =
            objectMapper.convertValue(caseDetails.getData().get(getIntervenerFieldName()), new TypeReference<>() {
            });
        return intervenerWrapper;
    }
}
