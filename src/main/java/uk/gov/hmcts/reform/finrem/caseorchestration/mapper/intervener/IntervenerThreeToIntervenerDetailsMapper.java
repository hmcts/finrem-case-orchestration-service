package uk.gov.hmcts.reform.finrem.caseorchestration.mapper.intervener;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.wrapper.IntervenerThreeWrapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.intervener.IntervenerDetails;

@Component
@Slf4j
@RequiredArgsConstructor
public class IntervenerThreeToIntervenerDetailsMapper {

    public IntervenerDetails mapToIntervenerDetails(IntervenerThreeWrapper intervenerThreeWrapper) {
        return IntervenerDetails.builder()
            .intervenerName(intervenerThreeWrapper.getIntervener3Name())
            .intervenerAddress(intervenerThreeWrapper.getIntervener3Address())
            .intervenerEmail(intervenerThreeWrapper.getIntervener3Email())
            .intervenerPhone(intervenerThreeWrapper.getIntervener3Phone())
            .intervenerRepresented(intervenerThreeWrapper.getIntervener3Represented())
            .intervenerDateAdded(intervenerThreeWrapper.getIntervener3DateAdded())
            .intervenerSolName(intervenerThreeWrapper.getIntervener3SolName())
            .intervenerSolEmail(intervenerThreeWrapper.getIntervener3SolEmail())
            .intervenerSolPhone(intervenerThreeWrapper.getIntervener3SolPhone())
            .intervenerOrganisation(intervenerThreeWrapper.getIntervener3Organisation())
            .intervenerSolicitorFirm(intervenerThreeWrapper.getIntervener3SolicitorFirm())
            .intervenerSolicitorReference(intervenerThreeWrapper.getIntervener3SolicitorReference())
            .build();
    }
}
