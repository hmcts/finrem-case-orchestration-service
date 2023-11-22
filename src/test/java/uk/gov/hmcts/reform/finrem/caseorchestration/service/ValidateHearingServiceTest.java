package uk.gov.hmcts.reform.finrem.caseorchestration.service;

import org.junit.Before;
import org.junit.Test;
import uk.gov.hmcts.reform.finrem.caseorchestration.BaseServiceTest;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseData;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.YesOrNo;

import java.time.LocalDate;
import java.util.List;

import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.hasSize;
import static org.junit.Assert.assertThat;
import static uk.gov.hmcts.reform.finrem.caseorchestration.service.ValidateHearingService.DATE_BETWEEN_12_AND_16_WEEKS;
import static uk.gov.hmcts.reform.finrem.caseorchestration.service.ValidateHearingService.DATE_BETWEEN_6_AND_10_WEEKS;

public class ValidateHearingServiceTest extends BaseServiceTest {

    private static final String ISSUE_DATE_FAST_TRACK_DECISION_OR_HEARING_DATE_IS_EMPTY =
        "Issue Date, fast track decision or hearingDate is empty";

    private ValidateHearingService service = new ValidateHearingService();
    private FinremCaseDetails caseDetails;
    private FinremCaseData caseData;

    @Before
    public void setup() {
        caseDetails = getCaseDetails();
        caseData = caseDetails.getData();
    }

    @Test
    public void issueDateEmpty() {
        caseData.setFastTrackDecision(YesOrNo.YES);
        caseData.setIssueDate(null);
        caseData.setHearingDate(LocalDate.now().plusWeeks(7));
        List<String> errors = doTestErrors();
        assertThat(errors, hasItem(ISSUE_DATE_FAST_TRACK_DECISION_OR_HEARING_DATE_IS_EMPTY));
    }

    @Test
    public void fastTrackDecisionEmpty() {
        caseData.setIssueDate(LocalDate.now());
        caseData.setHearingDate(LocalDate.now().plusWeeks(7));
        caseData.setFastTrackDecisionReason(null);
        List<String> errors = doTestErrors();
        assertThat(errors, hasItem(ISSUE_DATE_FAST_TRACK_DECISION_OR_HEARING_DATE_IS_EMPTY));
    }

    @Test
    public void hearingDateEmpty() {
        caseData.setIssueDate(LocalDate.now());
        caseData.setFastTrackDecision(YesOrNo.YES);
        caseData.setHearingDate(null);
        List<String> errors = doTestErrors();
        assertThat(errors, hasItem(ISSUE_DATE_FAST_TRACK_DECISION_OR_HEARING_DATE_IS_EMPTY));
    }

    @Test
    public void noErrors() {
        caseData.setIssueDate(LocalDate.now());
        caseData.setFastTrackDecision(YesOrNo.YES);
        caseData.setHearingDate(LocalDate.now().plusWeeks(7));
        List<String> errors = doTestErrors();
        assertThat(errors, hasSize(0));
    }

    @Test
    public void fastTrackHearingDatesWarningWithJudiciaryOutcome() {
        caseData.setIssueDate(LocalDate.now());
        caseData.setFastTrackDecision(null);
        caseData.setHearingDate(LocalDate.now().plusWeeks(3));
        caseData.setCaseAllocatedTo(YesOrNo.YES);
        List<String> errors = doTestWarnings();
        assertThat(errors, hasItem(DATE_BETWEEN_6_AND_10_WEEKS));
    }

    @Test
    public void fastTrackHearingDatesWarningWithoutJudiciaryOutcome() {
        caseData.setIssueDate(LocalDate.now());
        caseData.setFastTrackDecision(YesOrNo.YES);
        caseData.setHearingDate(LocalDate.now().plusWeeks(3));
        List<String> errors = doTestWarnings();
        assertThat(errors, hasItem(DATE_BETWEEN_6_AND_10_WEEKS));
    }

    @Test
    public void fastTrackHearingDatesNoWarning() {
        caseData.setIssueDate(LocalDate.now());
        caseData.setFastTrackDecision(YesOrNo.YES);
        caseData.setHearingDate(LocalDate.now().plusWeeks(7));
        List<String> errors = doTestWarnings();
        assertThat(errors, hasSize(0));
    }

    @Test
    public void nonFastTrackHearingDatesWarning() {
        caseData.setIssueDate(LocalDate.now());
        caseData.setFastTrackDecision(YesOrNo.NO);
        caseData.setHearingDate(LocalDate.now().plusWeeks(3));
        List<String> errors = doTestWarnings();
        assertThat(errors, hasItem(DATE_BETWEEN_12_AND_16_WEEKS));
    }

    @Test
    public void nonFastTrackHearingDatesNoWarning() {
        caseData.setIssueDate(LocalDate.now());
        caseData.setFastTrackDecision(YesOrNo.NO);
        caseData.setHearingDate(LocalDate.now().plusWeeks(13));
        List<String> errors = doTestWarnings();
        assertThat(errors, hasSize(0));
    }

    private List<String> doTestWarnings() {
        return service.validateHearingWarnings(caseDetails);
    }

    private List<String> doTestErrors() {
        return service.validateHearingErrors(caseDetails);
    }

    private FinremCaseDetails getCaseDetails() {
        FinremCaseData caseData = FinremCaseData.builder().build();
        return FinremCaseDetails.builder().id(123L).data(caseData).build();
    }
}
