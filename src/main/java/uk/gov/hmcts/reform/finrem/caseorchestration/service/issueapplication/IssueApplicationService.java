package uk.gov.hmcts.reform.finrem.caseorchestration.service.issueapplication;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.GenerateCoverSheetService;

@Service
@Slf4j
@RequiredArgsConstructor
public class IssueApplicationService {

    private final GenerateCoverSheetService generateCoverSheetService;

    /**
     * Generates cover sheets for both the applicant and the respondent and attaches them to the given case.
     *
     * @param caseDetails the case details containing the applicant and respondent information for which
     *                     the cover sheets will be generated and set
     * @param userAuthorisation the authorisation token of the user making the request
     */
    public void generateCoverSheets(FinremCaseDetails caseDetails, String userAuthorisation) {
        generateCoverSheetService.generateAndSetApplicantCoverSheet(caseDetails, userAuthorisation);
        generateCoverSheetService.generateAndSetRespondentCoverSheet(caseDetails, userAuthorisation);
    }
}
