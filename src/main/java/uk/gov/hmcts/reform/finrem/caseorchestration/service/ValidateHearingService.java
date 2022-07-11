package uk.gov.hmcts.reform.finrem.caseorchestration.service;

import com.google.common.collect.ImmutableList;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.finrem.ccd.domain.FinremCaseData;
import uk.gov.hmcts.reform.finrem.ccd.domain.FinremCaseDetails;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Stream;

import static uk.gov.hmcts.reform.finrem.caseorchestration.service.CaseDataService.nullToEmpty;

@Service
public class ValidateHearingService {

    public static final String DATE_BETWEEN_6_AND_10_WEEKS =
        "Date of the Fast Track hearing must be between 6 and 10 weeks.";
    public static final String DATE_BETWEEN_12_AND_16_WEEKS = "Date of the hearing must be between 12 and 16 weeks.";
    public static final String REQUIRED_FIELD_EMPTY_ERROR = "Issue Date, fast track decision or hearingDate is empty";

    private static final Function<FinremCaseData, Boolean> isFastTrackApplication = caseData ->
        Optional.ofNullable(caseData.getCaseAllocatedTo())
            .map(s -> s.isYes())
            .orElseGet(() -> caseData.getFastTrackDecision().isYes());

    public List<String> validateHearingErrors(FinremCaseDetails caseDetails) {
        FinremCaseData caseData = caseDetails.getCaseData();
        String issueDate = nullToEmpty(caseData.getIssueDate());
        String hearingDate = nullToEmpty(caseData.getHearingDate());
        String fastTrackDecision = nullToEmpty(caseData.getFastTrackDecision());
        return Stream.of(issueDate, hearingDate, fastTrackDecision).anyMatch(StringUtils::isBlank)
            ? ImmutableList.of(REQUIRED_FIELD_EMPTY_ERROR) : ImmutableList.of();
    }

    public List<String> validateHearingWarnings(FinremCaseDetails caseDetails) {
        FinremCaseData caseData = caseDetails.getCaseData();
        String issueDate = nullToEmpty(caseData.getIssueDate());
        String hearingDate = nullToEmpty(caseData.getHearingDate());

        LocalDate issueLocalDate = LocalDate.parse(issueDate);
        LocalDate hearingLocalDate = LocalDate.parse(hearingDate);

        boolean fastTrackApplication = isFastTrackApplication.apply(caseData);
        if (fastTrackApplication) {
            if (!isDateInBetweenIncludingEndPoints(issueLocalDate.plusWeeks(6), issueLocalDate.plusWeeks(10),
                hearingLocalDate)) {
                return ImmutableList.of(DATE_BETWEEN_6_AND_10_WEEKS);
            }
        } else if (!isDateInBetweenIncludingEndPoints(issueLocalDate.plusWeeks(12), issueLocalDate.plusWeeks(16),
            hearingLocalDate)) {
            return ImmutableList.of(DATE_BETWEEN_12_AND_16_WEEKS);
        }
        return ImmutableList.of();
    }

    private static boolean isDateInBetweenIncludingEndPoints(final LocalDate min, final LocalDate max,
                                                             final LocalDate date) {
        return !(date.isBefore(min) || date.isAfter(max));
    }
}
