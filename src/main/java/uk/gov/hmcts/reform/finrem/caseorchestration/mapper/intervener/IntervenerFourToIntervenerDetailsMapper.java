package uk.gov.hmcts.reform.finrem.caseorchestration.mapper.intervener;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.wrapper.IntervenerFourWrapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.wrapper.IntervenerTwoWrapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.intervener.IntervenerDetails;

@Component
@Slf4j
@RequiredArgsConstructor
public class IntervenerFourToIntervenerDetailsMapper {

    public IntervenerDetails mapToIntervenerDetails(IntervenerFourWrapper intervenerFourWrapper) {
        return IntervenerDetails.builder()
            .intervenerName(intervenerFourWrapper.getIntervener4Name())
            .intervenerAddress(intervenerFourWrapper.getIntervener4Address())
            .intervenerEmail(intervenerFourWrapper.getIntervener4Email())
            .intervenerPhone(intervenerFourWrapper.getIntervener4Phone())
            .intervenerRepresented(intervenerFourWrapper.getIntervener4Represented())
            .intervenerDateAdded(intervenerFourWrapper.getIntervener4DateAdded())
            .intervenerSolName(intervenerFourWrapper.getIntervener4SolName())
            .intervenerSolEmail(intervenerFourWrapper.getIntervener4SolEmail())
            .intervenerSolPhone(intervenerFourWrapper.getIntervener4SolPhone())
            .intervenerOrganisation(intervenerFourWrapper.getIntervener4Organisation())
            .build();
    }
}
