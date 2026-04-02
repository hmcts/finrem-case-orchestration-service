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

    public void updateInterveneCoversheet(FinremCaseDetails finremCaseDetails, IntervenerChangeDetails intervenerChangeDetails, String authorisationToken) {
        if (IntervenerAction.ADDED.equals(intervenerChangeDetails.getIntervenerAction())) {
            generateCoverSheetService.generateAndStoreIntervenerCoversheet(
                finremCaseDetails,
                intervenerChangeDetails.getIntervenerType(),
                authorisationToken);
        } else if (IntervenerAction.REMOVED.equals(intervenerChangeDetails.getIntervenerAction())) {
            // TODO: Remove coversheet
        }

    }
}
