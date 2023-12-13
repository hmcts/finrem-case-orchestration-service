package uk.gov.hmcts.reform.finrem.caseorchestration.service;

import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseData;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseDetails;

import java.time.LocalDate;
import java.util.List;

@Service
public class ValidateHearingService {

    public static final String DATE_BETWEEN_6_AND_10_WEEKS =
        "Date of the Fast Track hearing must be between 6 and 10 weeks.";
    public static final String DATE_BETWEEN_12_AND_16_WEEKS = "Date of the hearing must be between 12 and 16 weeks.";
    public static final String REQUIRED_FIELD_EMPTY_ERROR = "Issue Date, fast track decision or hearingDate is empty";

    //TODO: Refactor as it is always used inverted
    private static boolean isDateInBetweenIncludingEndPoints(final LocalDate min, final LocalDate max,
                                                             final LocalDate date) {
        return !(date.isBefore(min) || date.isAfter(max));
    }

    public List<String> validateHearingErrors(FinremCaseDetails finremCaseDetails) {
        FinremCaseData caseData = finremCaseDetails.getData();
        return caseData.getIssueDate() == null || caseData.getHearingDate() == null || caseData.getFastTrackDecision() == null
            ? List.of(REQUIRED_FIELD_EMPTY_ERROR) : List.of();
    }

    public List<String> validateHearingWarnings(FinremCaseDetails caseDetails) {
        FinremCaseData caseData = caseDetails.getData();
        LocalDate issueDate = caseData.getIssueDate();
        LocalDate hearingDate = caseData.getHearingDate();

        boolean fastTrackApplication = caseData.isFastTrackApplication();
        if (fastTrackApplication) {
            if (!isDateInBetweenIncludingEndPoints(issueDate.plusWeeks(6), issueDate.plusWeeks(10),
                    hearingDate)) {
                return List.of(DATE_BETWEEN_6_AND_10_WEEKS);
            }
        } else if (!isDateInBetweenIncludingEndPoints(issueDate.plusWeeks(12), issueDate.plusWeeks(16),
                hearingDate)) {
            return List.of(DATE_BETWEEN_12_AND_16_WEEKS);
        }
        return List.of();
    }
}
