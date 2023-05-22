package uk.gov.hmcts.reform.finrem.caseorchestration.mapper.intervener;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.wrapper.IntervenerTwoWrapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.intervener.IntervenerDetails;

@Component
@Slf4j
@RequiredArgsConstructor
public class IntervenerTwoToIntervenerDetailsMapper {

    public IntervenerDetails mapToIntervenerDetails(IntervenerTwoWrapper intervenerTwoWrapper) {
        return IntervenerDetails.builder()
            .intervenerName(intervenerTwoWrapper.getIntervener2Name())
            .intervenerAddress(intervenerTwoWrapper.getIntervener2Address())
            .intervenerEmail(intervenerTwoWrapper.getIntervener2Email())
            .intervenerPhone(intervenerTwoWrapper.getIntervener2Phone())
            .intervenerRepresented(intervenerTwoWrapper.getIntervener2Represented())
            .intervenerDateAdded(intervenerTwoWrapper.getIntervener2DateAdded())
            .intervenerSolName(intervenerTwoWrapper.getIntervener2SolName())
            .intervenerSolEmail(intervenerTwoWrapper.getIntervener2SolEmail())
            .intervenerSolPhone(intervenerTwoWrapper.getIntervener2SolPhone())
            .intervenerOrganisation(intervenerTwoWrapper.getIntervener2Organisation())
            .intervenerSolicitorFirm(intervenerTwoWrapper.getIntervener2SolicitorFirm())
            .intervenerSolicitorReference(intervenerTwoWrapper.getIntervener2SolicitorReference())
            .build();
    }
}
