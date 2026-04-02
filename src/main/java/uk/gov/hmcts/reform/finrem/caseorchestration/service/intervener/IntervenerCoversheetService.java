package uk.gov.hmcts.reform.finrem.caseorchestration.service.intervener;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.intervener.IntervenerAction;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.intervener.IntervenerChangeDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.GenerateCoverSheetService;

@Service
@RequiredArgsConstructor
public class IntervenerCoversheetService {

    private final GenerateCoverSheetService generateCoverSheetService;

    public void updateIntervenerCoversheet(FinremCaseDetails finremCaseDetails, IntervenerChangeDetails intervenerChangeDetails, String authorisationToken) {
        if (IntervenerAction.ADDED.equals(intervenerChangeDetails.getIntervenerAction())) {
            generateCoverSheetService.generateAndStoreIntervenerCoversheet(
                finremCaseDetails,
                intervenerChangeDetails.getIntervenerType(),
                authorisationToken);
        }

        if (IntervenerAction.REMOVED.equals(intervenerChangeDetails.getIntervenerAction())) {
            generateCoverSheetService.removeIntervenerCoverSheet(finremCaseDetails, intervenerChangeDetails, authorisationToken);
        }
    }
}
