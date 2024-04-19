package uk.gov.hmcts.reform.finrem.caseorchestration.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Before;
import org.junit.Test;
import uk.gov.hmcts.reform.finrem.caseorchestration.BaseServiceTest;
import uk.gov.hmcts.reform.finrem.caseorchestration.mapper.FinremCaseDetailsMapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseRole;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.DynamicMultiSelectList;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.DynamicMultiSelectListElement;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseData;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.YesOrNo;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.correspondence.SelectablePartiesCorrespondenceService;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.hasSize;
import static org.junit.Assert.assertThat;
import static uk.gov.hmcts.reform.finrem.caseorchestration.service.HearingDocumentService.HEARING_DEFAULT_CORRESPONDENCE_ERROR_MESSAGE;
import static uk.gov.hmcts.reform.finrem.caseorchestration.service.ValidateHearingService.DATE_BETWEEN_12_AND_16_WEEKS;
import static uk.gov.hmcts.reform.finrem.caseorchestration.service.ValidateHearingService.DATE_BETWEEN_6_AND_10_WEEKS;

public class ValidateHearingServiceTest extends BaseServiceTest {

    private static final String ISSUE_DATE_FAST_TRACK_DECISION_OR_HEARING_DATE_IS_EMPTY =
        "Issue Date, fast track decision or hearingDate is empty";

    private ValidateHearingService service = new ValidateHearingService(new SelectablePartiesCorrespondenceService(
        new FinremCaseDetailsMapper(new ObjectMapper())));
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
        caseData.setPartiesOnCase(getPartiesOnCase());

        assertThat(errors, hasItem(ISSUE_DATE_FAST_TRACK_DECISION_OR_HEARING_DATE_IS_EMPTY));
    }

    @Test
    public void fastTrackDecisionEmpty() {
        caseData.setIssueDate(LocalDate.now());
        caseData.setHearingDate(LocalDate.now().plusWeeks(7));
        caseData.setFastTrackDecisionReason(null);
        caseData.setPartiesOnCase(getPartiesOnCase());

        List<String> errors = doTestErrors();
        assertThat(errors, hasItem(ISSUE_DATE_FAST_TRACK_DECISION_OR_HEARING_DATE_IS_EMPTY));
    }

    @Test
    public void hearingDateEmpty() {
        caseData.setIssueDate(LocalDate.now());
        caseData.setFastTrackDecision(YesOrNo.YES);
        caseData.setHearingDate(null);
        caseData.setPartiesOnCase(getPartiesOnCase());
        List<String> errors = doTestErrors();
        assertThat(errors, hasItem(ISSUE_DATE_FAST_TRACK_DECISION_OR_HEARING_DATE_IS_EMPTY));
    }

    @Test
    public void givenHearingWithNoAppRespNotificationSelected_whenValidate_thenThrowErrorCustomMessage() {
        caseData.setIssueDate(LocalDate.now());
        caseData.setFastTrackDecision(YesOrNo.YES);
        caseData.setHearingDate(LocalDate.now().plusWeeks(7));

        DynamicMultiSelectList partiesOnCase = getPartiesOnCaseMissingSelectedAppAndResp();


        caseData.setPartiesOnCase(partiesOnCase);
        List<String> errors = doTestErrors();
        assertThat(errors, hasItem(HEARING_DEFAULT_CORRESPONDENCE_ERROR_MESSAGE));
    }

    private DynamicMultiSelectList getPartiesOnCase() {
        DynamicMultiSelectList partiesOnCase = getPartiesOnCaseMissingSelectedAppAndResp();
        partiesOnCase.getValue().addAll(new ArrayList<>(
            List.of(getDynamicElementList(CaseRole.APP_SOLICITOR.getCcdCode()),
                getDynamicElementList(CaseRole.RESP_SOLICITOR.getCcdCode()))));

        return partiesOnCase;
    }

    private DynamicMultiSelectList getPartiesOnCaseMissingSelectedAppAndResp() {
        List<DynamicMultiSelectListElement> activeParties = new ArrayList<>(
            List.of(getDynamicElementList(CaseRole.APP_SOLICITOR.getCcdCode()),
                getDynamicElementList(CaseRole.RESP_SOLICITOR.getCcdCode()),
                getDynamicElementList(CaseRole.INTVR_SOLICITOR_1.getCcdCode()),
                getDynamicElementList(CaseRole.INTVR_SOLICITOR_2.getCcdCode()),
                getDynamicElementList(CaseRole.INTVR_SOLICITOR_3.getCcdCode()),
                getDynamicElementList(CaseRole.INTVR_SOLICITOR_4.getCcdCode())));

        List<DynamicMultiSelectListElement> selectedParties = new ArrayList<>(
            List.of(getDynamicElementList(CaseRole.INTVR_SOLICITOR_1.getCcdCode()),
                getDynamicElementList(CaseRole.INTVR_SOLICITOR_2.getCcdCode()),
                getDynamicElementList(CaseRole.INTVR_SOLICITOR_3.getCcdCode()),
                getDynamicElementList(CaseRole.INTVR_SOLICITOR_4.getCcdCode())));
        DynamicMultiSelectList partiesOnCase = DynamicMultiSelectList.builder()
            .value(selectedParties)
            .listItems(activeParties)
            .build();
        return partiesOnCase;
    }

    private DynamicMultiSelectListElement getDynamicElementList(String role) {
        return DynamicMultiSelectListElement.builder()
            .code(role)
            .label(role)
            .build();
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
