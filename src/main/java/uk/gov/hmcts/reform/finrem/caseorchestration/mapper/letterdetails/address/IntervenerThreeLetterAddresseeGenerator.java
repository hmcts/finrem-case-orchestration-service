package uk.gov.hmcts.reform.finrem.caseorchestration.mapper.letterdetails.address;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseDataContested;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.IntervenerConstant;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.wrapper.IntervenerThreeWrapper;

@Component
@Slf4j
public class IntervenerThreeLetterAddresseeGenerator extends IntervenerLetterAddresseeGenerator<IntervenerThreeWrapper> {

    @Autowired
    public IntervenerThreeLetterAddresseeGenerator(ObjectMapper objectMapper) {
        super(objectMapper);
    }

    @Override
    protected String getIntervenerFieldName() {
        return IntervenerConstant.INTERVENER_THREE;
    }

    @Override
    protected IntervenerThreeWrapper getIntervenerWrapper(CaseDetails caseDetails) {
        IntervenerThreeWrapper intervenerWrapper =
            objectMapper.convertValue(caseDetails.getData().get(getIntervenerFieldName()), new TypeReference<>() {
            });
        return intervenerWrapper;
    }

    @Override
    protected IntervenerThreeWrapper getIntervenerWrapper(FinremCaseDetails<FinremCaseDataContested> caseDetails) {
        return caseDetails.getData().getIntervenerThreeWrapper();
    }
}

