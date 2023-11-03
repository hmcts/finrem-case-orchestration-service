package uk.gov.hmcts.reform.finrem.caseorchestration.mapper.letterdetails.address;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.Address;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseDataContested;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.wrapper.intevener.IntervenerWrapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.letterdetails.AddresseeDetails;

import java.util.Map;

@Component
@RequiredArgsConstructor
@Slf4j
public abstract class IntervenerLetterAddresseeGenerator<T extends IntervenerWrapper> implements LetterAddresseeGenerator {

    protected final ObjectMapper objectMapper;

    public AddresseeDetails generate(CaseDetails caseDetails) {

        T intervenerWrapper = getIntervenerWrapper(caseDetails);

        return AddresseeDetails.builder().addresseeName(intervenerWrapper.getIntervenerName())
            .addressToSendTo(objectMapper.convertValue(intervenerWrapper.getIntervenerAddress(), Map.class)).build();
    }

    public AddresseeDetails generate(FinremCaseDetails caseDetails) {
        IntervenerWrapper intervenerWrapper = getIntervenerWrapper(caseDetails);
        return AddresseeDetails.builder().addresseeName(intervenerWrapper.getIntervenerName())
            .finremAddressToSendTo(objectMapper.convertValue(intervenerWrapper.getIntervenerAddress(), Address.class)).build();
    }

    protected abstract String getIntervenerFieldName();

    protected abstract T getIntervenerWrapper(CaseDetails caseDetails);

    protected abstract T getIntervenerWrapper(FinremCaseDetails<FinremCaseDataContested> caseDetails);
}
