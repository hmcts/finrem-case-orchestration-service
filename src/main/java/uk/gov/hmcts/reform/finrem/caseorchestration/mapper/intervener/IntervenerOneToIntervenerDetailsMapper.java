package uk.gov.hmcts.reform.finrem.caseorchestration.mapper.intervener;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.wrapper.IntervenerFourWrapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.wrapper.IntervenerOneWrapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.wrapper.IntervenerThreeWrapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.wrapper.IntervenerTwoWrapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.intervener.IntervenerChangeDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.intervener.IntervenerDetails;

@Component
@Slf4j
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

    public IntervenerDetails mapToIntervenerTwoDetails(IntervenerTwoWrapper intervenerTwoWrapper) {
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
            .build();
    }

    public IntervenerDetails mapToIntervenerThreeDetails(IntervenerThreeWrapper intervenerThreeWrapper) {
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
            .build();
    }

    public IntervenerDetails mapToIntervenerFourDetails(IntervenerFourWrapper intervenerFourWrapper) {
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

    public IntervenerDetails getIntervenerDetails(FinremCaseDetails caseDetails) {

        IntervenerChangeDetails.IntervenerType intervenerType = caseDetails.getData().getCurrentIntervenerChangeDetails().getIntervenerType();
        IntervenerDetails intervenerDetails = null;
        switch (intervenerType) {
            case INTERVENER_ONE ->
                intervenerDetails = new IntervenerOneToIntervenerDetailsMapper()
                    .mapToIntervenerDetails(caseDetails.getData().getIntervenerOneWrapper());
            case INTERVENER_TWO ->
                intervenerDetails = new IntervenerOneToIntervenerDetailsMapper()
                    .mapToIntervenerTwoDetails(caseDetails.getData().getIntervenerTwoWrapper());
            case INTERVENER_THREE ->
                intervenerDetails = new IntervenerOneToIntervenerDetailsMapper()
                    .mapToIntervenerThreeDetails(caseDetails.getData().getIntervenerThreeWrapper());
            case INTERVENER_FOUR ->
                intervenerDetails = new IntervenerOneToIntervenerDetailsMapper()
                    .mapToIntervenerFourDetails(caseDetails.getData().getIntervenerFourWrapper());
            default -> throw new IllegalStateException("Unexpected value: " + intervenerType);
        }
        return intervenerDetails;

    }
}
