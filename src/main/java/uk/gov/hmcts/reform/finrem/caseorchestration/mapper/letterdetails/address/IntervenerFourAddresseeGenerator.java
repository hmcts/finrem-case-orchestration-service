package uk.gov.hmcts.reform.finrem.caseorchestration.mapper.letterdetails.address;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.wrapper.IntervenerFourWrapper;

@Component
@Slf4j
public class IntervenerFourAddresseeGenerator extends IntervenerAddresseeGenerator<IntervenerFourWrapper> {

    @Autowired
    public IntervenerFourAddresseeGenerator(ObjectMapper objectMapper) {
        super(objectMapper);
    }


    @Override
    protected String getIntervenerFieldName() {
        return CCDConfigConstant.INTERVENER_FOUR;
    }

    @Override
    protected IntervenerFourWrapper getIntervenerWrapper(CaseDetails caseDetails) {
        IntervenerFourWrapper intervenerWrapper =
            objectMapper.convertValue(caseDetails.getData().get(getIntervenerFieldName()), new TypeReference<>() {
            });
        return intervenerWrapper;
    }

    @Override
    protected IntervenerFourWrapper getIntervenerWrapper(FinremCaseDetails caseDetails) {
        return caseDetails.getData().getIntervenerFourWrapper();
    }
}
