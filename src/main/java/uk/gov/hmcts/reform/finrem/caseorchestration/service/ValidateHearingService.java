package uk.gov.hmcts.reform.finrem.caseorchestration.service;

import com.google.common.collect.ImmutableList;
import org.apache.commons.lang.ObjectUtils;
import org.apache.commons.lang.StringUtils;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.FAST_TRACK_DECISION;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.HEARING_DATE;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.ISSUE_DATE;

@Service
public class ValidateHearingService {

    public List<String> validateHearingErrors(CaseDetails caseDetails) {
        Map<String, Object> caseData = caseDetails.getData();
        String issueDate = ObjectUtils.toString(caseData.get(ISSUE_DATE));
        String hearingDate = ObjectUtils.toString(caseData.get(HEARING_DATE));
        String fastTrackDecision = ObjectUtils.toString(caseData.get(FAST_TRACK_DECISION));

        if (StringUtils.isBlank(issueDate) || StringUtils.isBlank(fastTrackDecision)
                || StringUtils.isBlank(hearingDate)) {
            return  ImmutableList.of("Issue Date , fast track decision or hearingDate is empty");
        }

        return ImmutableList.of();
    }

    public List<String> validateHearingWarnings(CaseDetails caseDetails) {
        Map<String, Object> caseData = caseDetails.getData();
        String issueDate = ObjectUtils.toString(caseData.get(ISSUE_DATE));
        String hearingDate = ObjectUtils.toString(caseData.get(HEARING_DATE));
        String fastTrackDecision = ObjectUtils.toString(caseData.get(FAST_TRACK_DECISION));

        LocalDate issueLocalDate = LocalDate.parse(issueDate);
        LocalDate hearingLocalDate = LocalDate.parse(hearingDate);

        if (fastTrackDecision.equalsIgnoreCase("yes")) {
            if (!isDateInBetweenIncludingEndPoints(issueLocalDate.plusWeeks(6), issueLocalDate.plusWeeks(10),
                    hearingLocalDate)) {
                return ImmutableList.of("Date of the Fast Track hearing must be between 6 and 10 weeks.");
            }
        } else if (!isDateInBetweenIncludingEndPoints(issueLocalDate.plusWeeks(12), issueLocalDate.plusWeeks(14),
                hearingLocalDate)) {
            return ImmutableList.of("Date of the hearing must be between 12 and 14 weeks.");
        }

        return ImmutableList.of();
    }

    private static boolean isDateInBetweenIncludingEndPoints(final LocalDate min, final LocalDate max,
                                                            final LocalDate date) {
        return !(date.isBefore(min) || date.isAfter(max));
    }
}