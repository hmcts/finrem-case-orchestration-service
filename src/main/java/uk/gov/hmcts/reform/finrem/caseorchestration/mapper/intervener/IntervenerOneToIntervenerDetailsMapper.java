package uk.gov.hmcts.reform.finrem.caseorchestration.mapper.intervener;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.wrapper.IntervenerOneWrapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.intervener.IntervenerDetails;

@Component
@RequiredArgsConstructor
public class IntervenerOneToIntervenerDetailsMapper {

    public IntervenerDetails mapToIntervenerDetails(IntervenerOneWrapper intervenerOneWrapper) {
       return IntervenerDetails.builder()
            .intervenerName(intervenerOneWrapper.getIntervener1Name())
            .intervenerAddress(intervenerOneWrapper.getIntervener1Address())
            .intervenerEmail(intervenerOneWrapper.getIntervener1Email())
            .intervenerPhone(intervenerOneWrapper.getIntervener1Phone())
            .intervenerRepresented(intervenerOneWrapper.getIntervener1Represented())
            .intervenerDateAdded(intervenerOneWrapper.getIntervener1DateAdded())
            .intervenerSolName(intervenerOneWrapper.getIntervener1SolName())
            .intervenerSolEmail(intervenerOneWrapper.getIntervener1SolEmail())
            .intervenerSolPhone(intervenerOneWrapper.getIntervener1SolPhone())
            .intervenerOrganisation(intervenerOneWrapper.getIntervener1Organisation())
            .build();
    }
}
