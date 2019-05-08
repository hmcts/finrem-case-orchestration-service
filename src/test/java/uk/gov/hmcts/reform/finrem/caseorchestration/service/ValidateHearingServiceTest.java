package uk.gov.hmcts.reform.finrem.caseorchestration.service;

import com.google.common.collect.ImmutableMap;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.junit.Test;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;

import java.time.LocalDate;
import java.util.Date;
import java.util.List;

import static java.util.Arrays.asList;
import static java.util.stream.Collectors.collectingAndThen;
import static java.util.stream.Collectors.toMap;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.hasSize;
import static org.junit.Assert.assertThat;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.CASE_ALLOCATED_TO;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.FAST_TRACK_DECISION;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.HEARING_DATE;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.ISSUE_DATE;
import static uk.gov.hmcts.reform.finrem.caseorchestration.service.ValidateHearingService.DATE_BETWEEN_12_AND_14_WEEKS;
import static uk.gov.hmcts.reform.finrem.caseorchestration.service.ValidateHearingService.DATE_BETWEEN_6_AND_10_WEEKS;

public class ValidateHearingServiceTest {

    private static final String error = "Issue Date , fast track decision or hearingDate is empty";
    private ValidateHearingService service = new ValidateHearingService();

    @Test
    public void issueDateEmpty() {
        List<ImmutablePair<String, Object>> pairs =
                asList(pairOf(FAST_TRACK_DECISION, "Yes"), pairOf(HEARING_DATE, new Date()));

        List<String> errors = doTestErrors(pairs);
        assertThat(errors, hasItem(error));
    }

    @Test
    public void fastTrackDecisionEmpty() {
        List<ImmutablePair<String, Object>> pairs =
                asList(pairOf(ISSUE_DATE, new Date()), pairOf(HEARING_DATE, new Date()));

        List<String> errors = doTestErrors(pairs);
        assertThat(errors, hasItem(error));
    }

    @Test
    public void hearingDateEmpty() {
        List<ImmutablePair<String, Object>> pairs =
                asList(pairOf(ISSUE_DATE, new Date()), pairOf(FAST_TRACK_DECISION, "Yes"));

        List<String> errors = doTestErrors(pairs);
        assertThat(errors, hasItem(error));
    }

    @Test
    public void noErrors() {
        List<ImmutablePair<String, Object>> pairs =
                asList(pairOf(ISSUE_DATE, new Date()), pairOf(FAST_TRACK_DECISION,"Yes"),
                        pairOf(HEARING_DATE, new Date()));

        List<String> errors = doTestErrors(pairs);
        assertThat(errors, hasSize(0));
    }

    @Test
    public void fastTrackHearingDatesWarningWithJudiciaryOutcome() {
        List<ImmutablePair<String, Object>> pairs =
                asList(pairOf(ISSUE_DATE, LocalDate.now()),
                        pairOf(HEARING_DATE, LocalDate.now().plusWeeks(3)),
                        pairOf(CASE_ALLOCATED_TO, "fastTrack"));

        List<String> errors = doTestWarnings(pairs);
        assertThat(errors, hasItem(DATE_BETWEEN_6_AND_10_WEEKS));
    }

    @Test
    public void fastTrackHearingDatesWarningWithoutJudiciaryOutcome() {
        List<ImmutablePair<String, Object>> pairs =
                asList(pairOf(ISSUE_DATE, LocalDate.now()), pairOf(FAST_TRACK_DECISION,"Yes"),
                        pairOf(HEARING_DATE, LocalDate.now().plusWeeks(3)));

        List<String> errors = doTestWarnings(pairs);
        assertThat(errors, hasItem(DATE_BETWEEN_6_AND_10_WEEKS));
    }

    @Test
    public void fastTrackHearingDatesNoWarning() {
        List<ImmutablePair<String, Object>> pairs =
                asList(pairOf(ISSUE_DATE, LocalDate.now()), pairOf(FAST_TRACK_DECISION,"Yes"),
                        pairOf(HEARING_DATE, LocalDate.now().plusWeeks(7)));

        List<String> errors = doTestWarnings(pairs);
        assertThat(errors, hasSize(0));
    }

    @Test
    public void nonFastTrackHearingDatesWarning() {
        List<ImmutablePair<String, Object>> pairs =
                asList(pairOf(ISSUE_DATE, LocalDate.now()), pairOf(FAST_TRACK_DECISION,"No"),
                        pairOf(HEARING_DATE, LocalDate.now().plusWeeks(3)));

        List<String> errors = doTestWarnings(pairs);
        assertThat(errors, hasItem(DATE_BETWEEN_12_AND_14_WEEKS));
    }

    @Test
    public void nonFastTrackHearingDatesNoWarning() {
        List<ImmutablePair<String, Object>> pairs =
                asList(pairOf(ISSUE_DATE, LocalDate.now()), pairOf(FAST_TRACK_DECISION,"No"),
                        pairOf(HEARING_DATE, LocalDate.now().plusWeeks(13)));

        List<String> errors = doTestWarnings(pairs);
        assertThat(errors, hasSize(0));
    }

    private ImmutablePair<String, Object> pairOf(String left, Object right) {
        return ImmutablePair.of(left, right);
    }

    private List<String> doTestWarnings(List<ImmutablePair<String, Object>> pairs) {
        ImmutableMap<String, Object> caseData = pairs.stream()
                .collect(collectingAndThen(
                        toMap(ImmutablePair::getLeft, ImmutablePair::getRight), ImmutableMap::copyOf)
                );

        CaseDetails caseDetails = CaseDetails.builder().data(caseData).build();
        return  service.validateHearingWarnings(caseDetails);
    }

    private List<String> doTestErrors(List<ImmutablePair<String, Object>> pairs) {
        ImmutableMap<String, Object> caseData = pairs.stream()
                .collect(collectingAndThen(
                        toMap(ImmutablePair::getLeft, ImmutablePair::getRight), ImmutableMap::copyOf)
                );

        CaseDetails caseDetails = CaseDetails.builder().data(caseData).build();
        return  service.validateHearingErrors(caseDetails);
    }
}