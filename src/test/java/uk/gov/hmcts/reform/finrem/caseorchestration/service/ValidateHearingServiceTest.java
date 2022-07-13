package uk.gov.hmcts.reform.finrem.caseorchestration.service;

import org.junit.Test;
import uk.gov.hmcts.reform.finrem.caseorchestration.BaseServiceTest;
import uk.gov.hmcts.reform.finrem.ccd.domain.FinremCaseData;
import uk.gov.hmcts.reform.finrem.ccd.domain.FinremCaseDetails;
import uk.gov.hmcts.reform.finrem.ccd.domain.YesOrNo;

import java.time.LocalDate;
import java.util.List;

import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.hasSize;
import static org.junit.Assert.assertThat;
import static uk.gov.hmcts.reform.finrem.caseorchestration.service.ValidateHearingService.DATE_BETWEEN_12_AND_16_WEEKS;
import static uk.gov.hmcts.reform.finrem.caseorchestration.service.ValidateHearingService.DATE_BETWEEN_6_AND_10_WEEKS;
import static uk.gov.hmcts.reform.finrem.ccd.domain.YesOrNo.YES;

public class ValidateHearingServiceTest extends BaseServiceTest {

    private static final String ISSUE_DATE_FAST_TRACK_DECISION_OR_HEARING_DATE_IS_EMPTY =
        "Issue Date, fast track decision or hearingDate is empty";

    private ValidateHearingService service = new ValidateHearingService();

    @Test
    public void issueDateEmpty() {
        FinremCaseData caseData = new FinremCaseData();
        caseData.setFastTrackDecision(YES);
        caseData.setHearingDate(LocalDate.now());

        List<String> errors = doTestErrors(caseData);
        assertThat(errors, hasItem(ISSUE_DATE_FAST_TRACK_DECISION_OR_HEARING_DATE_IS_EMPTY));
    }

    @Test
    public void fastTrackDecisionEmpty() {
        FinremCaseData caseData = new FinremCaseData();
        caseData.setIssueDate(LocalDate.now());
        caseData.setHearingDate(LocalDate.now());

        List<String> errors = doTestErrors(caseData);
        assertThat(errors, hasItem(ISSUE_DATE_FAST_TRACK_DECISION_OR_HEARING_DATE_IS_EMPTY));
    }

    @Test
    public void hearingDateEmpty() {
        FinremCaseData caseData = new FinremCaseData();
        caseData.setIssueDate(LocalDate.now());
        caseData.setFastTrackDecision(YES);

        List<String> errors = doTestErrors(caseData);
        assertThat(errors, hasItem(ISSUE_DATE_FAST_TRACK_DECISION_OR_HEARING_DATE_IS_EMPTY));
    }

    @Test
    public void noErrors() {
        FinremCaseData caseData = new FinremCaseData();
        caseData.setIssueDate(LocalDate.now());
        caseData.setFastTrackDecision(YES);
        caseData.setHearingDate(LocalDate.now());

        List<String> errors = doTestErrors(caseData);
        assertThat(errors, hasSize(0));
    }

    @Test
    public void fastTrackHearingDatesWarningWithJudiciaryOutcome() {
        FinremCaseData caseData = new FinremCaseData();
        caseData.setIssueDate(LocalDate.now());
        caseData.setHearingDate(LocalDate.now().plusWeeks(3));
        caseData.setCaseAllocatedTo(YES);

        List<String> errors = doTestWarnings(caseData);
        assertThat(errors, hasItem(DATE_BETWEEN_6_AND_10_WEEKS));
    }

    @Test
    public void fastTrackHearingDatesWarningWithoutJudiciaryOutcome() {
        FinremCaseData caseData = new FinremCaseData();
        caseData.setFastTrackDecision(YES);
        caseData.setHearingDate(LocalDate.now().plusWeeks(3));

        List<String> errors = doTestWarnings(caseData);
        assertThat(errors, hasItem(DATE_BETWEEN_6_AND_10_WEEKS));
    }

    @Test
    public void fastTrackHearingDatesNoWarning() {
        FinremCaseData caseData = new FinremCaseData();
        caseData.setIssueDate(LocalDate.now());
        caseData.setFastTrackDecision(YES);
        caseData.setHearingDate(LocalDate.now().plusWeeks(7));

        List<String> errors = doTestWarnings(caseData);
        assertThat(errors, hasSize(0));
    }

    @Test
    public void nonFastTrackHearingDatesWarning() {
        FinremCaseData caseData = new FinremCaseData();
        caseData.setIssueDate(LocalDate.now());
        caseData.setFastTrackDecision(YesOrNo.NO);
        caseData.setHearingDate(LocalDate.now().plusWeeks(3));

        List<String> errors = doTestWarnings(caseData);
        assertThat(errors, hasItem(DATE_BETWEEN_12_AND_16_WEEKS));
    }

    @Test
    public void nonFastTrackHearingDatesNoWarning() {
        FinremCaseData caseData = new FinremCaseData();
        caseData.setIssueDate(LocalDate.now());
        caseData.setFastTrackDecision(YesOrNo.NO);
        caseData.setHearingDate(LocalDate.now().plusWeeks(13));

        List<String> errors = doTestWarnings(caseData);
        assertThat(errors, hasSize(0));
    }

    private List<String> doTestWarnings(FinremCaseData caseData) {
        FinremCaseDetails caseDetails = FinremCaseDetails.builder().caseData(caseData).build();
        return service.validateHearingWarnings(caseDetails);
    }

    private List<String> doTestErrors(FinremCaseData caseData) {
        FinremCaseDetails caseDetails = FinremCaseDetails.builder().caseData(caseData).build();
        return service.validateHearingErrors(caseDetails);
    }
}
