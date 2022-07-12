package uk.gov.hmcts.reform.finrem.caseorchestration.service;

import com.google.common.collect.ImmutableList;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Stream;

import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.FAST_TRACK_DECISION;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.HEARING_DATE;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.ISSUE_DATE;
import static uk.gov.hmcts.reform.finrem.caseorchestration.service.CaseHearingFunctions.isFastTrackApplication;

@Service
public class ValidateHearingService {

    public static final String DATE_BETWEEN_6_AND_10_WEEKS =
        "Date of the Fast Track hearing must be between 6 and 10 weeks.";
    public static final String DATE_BETWEEN_12_AND_16_WEEKS = "Date of the hearing must be between 12 and 16 weeks.";
    public static final String REQUIRED_FIELD_EMPTY_ERROR = "Issue Date, fast track decision or hearingDate is empty";

    public List<String> validateHearingErrors(CaseDetails caseDetails) {
        Map<String, Object> caseData = caseDetails.getData();
        String issueDate = Objects.toString(caseData.get(ISSUE_DATE), "");
        String hearingDate = Objects.toString(caseData.get(HEARING_DATE), "");
        String fastTrackDecision = Objects.toString(caseData.get(FAST_TRACK_DECISION), "");

        return Stream.of(issueDate, hearingDate, fastTrackDecision).anyMatch(StringUtils::isBlank)
            ? ImmutableList.of(REQUIRED_FIELD_EMPTY_ERROR) : ImmutableList.of();
    }

    public List<String> validateHearingWarnings(CaseDetails caseDetails) {
        Map<String, Object> caseData = caseDetails.getData();
        String issueDate = Objects.toString(caseData.get(ISSUE_DATE), "");
        String hearingDate = Objects.toString(caseData.get(HEARING_DATE), "");

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
