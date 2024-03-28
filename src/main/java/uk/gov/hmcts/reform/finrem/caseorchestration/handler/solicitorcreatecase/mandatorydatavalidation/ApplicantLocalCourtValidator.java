package uk.gov.hmcts.reform.finrem.caseorchestration.handler.solicitorcreatecase.mandatorydatavalidation;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseData;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.wrapper.DefaultCourtListWrapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.utils.NullChecker;

import java.util.Collections;
import java.util.List;

@Component
class ApplicantLocalCourtValidator implements MandatoryDataValidator {
    @Override
    public List<String> validate(FinremCaseData caseData) {
        DefaultCourtListWrapper courtList = caseData
            .getRegionWrapper().getAllocatedRegionWrapper().getDefaultCourtListWrapper();

        return NullChecker.anyNonNull(courtList)
            ? Collections.emptyList()
            : List.of("Applicant's Local Court is required. Update Please choose the Region in which the Applicant resides");
    }
}
