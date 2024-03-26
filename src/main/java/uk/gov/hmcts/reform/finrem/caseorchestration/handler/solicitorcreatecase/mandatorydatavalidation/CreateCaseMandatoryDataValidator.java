package uk.gov.hmcts.reform.finrem.caseorchestration.handler.solicitorcreatecase.mandatorydatavalidation;

import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseData;

import java.util.List;
import java.util.stream.Collectors;

@Service
@AllArgsConstructor
public class CreateCaseMandatoryDataValidator {

    private final List<MandatoryDataValidator> mandatoryDataValidators;

    public List<String> validate(FinremCaseData caseData) {
        return mandatoryDataValidators.stream()
            .flatMap(v -> v.validate(caseData).stream())
            .collect(Collectors.toList());
    }
}
