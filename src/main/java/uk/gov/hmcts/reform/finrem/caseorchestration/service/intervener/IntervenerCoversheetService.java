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

    /**
     * Updates the intervener coversheet based on the specified action.
     * This method either generates and stores a new intervener coversheet
     * for the "ADDED" action or removes the existing intervener coversheet
     * for the "REMOVED" action.
     *
     * @param finremCaseDetails    the details of the financial remedy case to update the coversheet for
     * @param intervenerChangeDetails details of the change made to the intervener, including the action to perform and type of intervener
     * @param authorisationToken   the token used for authorisation in the coversheet update process
     */
    public void updateIntervenerCoversheet(FinremCaseDetails finremCaseDetails,
                                           IntervenerChangeDetails intervenerChangeDetails,
                                           String authorisationToken) {
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
