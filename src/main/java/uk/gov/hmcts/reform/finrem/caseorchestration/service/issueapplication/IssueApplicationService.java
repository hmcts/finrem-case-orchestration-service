package uk.gov.hmcts.reform.finrem.caseorchestration.service.issueapplication;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.finrem.caseorchestration.config.DefaultsConfiguration;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseData;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.GenerateCoverSheetService;

import java.time.Clock;
import java.time.LocalDate;

import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.AssignToJudgeReason.DRAFT_CONSENT_ORDER;

@Service
@Slf4j
@RequiredArgsConstructor
public class IssueApplicationService {

    private final Clock clock;
    private final GenerateCoverSheetService generateCoverSheetService;
    private final DefaultsConfiguration defaultsConfiguration;

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

    /**
     * Populates the "assign to judge" fields on the given case data with default values used for consent order
     * processing. Sets the assigned judge, assignment reason, and refer-to-judge date and text.
     * It's dedicated for consented cases only.
     *
     * @param caseData the {@link FinremCaseData} to populate
     */
    public void populateAssignToJudgeFields(FinremCaseData caseData) {
        caseData.setAssignedToJudge(defaultsConfiguration.getAssignedToJudgeDefault());
        caseData.setAssignedToJudgeReason(DRAFT_CONSENT_ORDER);
        caseData.getReferToJudgeWrapper().setReferToJudgeDate(LocalDate.now(clock));
        caseData.getReferToJudgeWrapper().setReferToJudgeText("consent for approval");
    }
}
