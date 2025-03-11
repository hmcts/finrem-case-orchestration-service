package uk.gov.hmcts.reform.finrem.caseorchestration.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseRole;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.DynamicMultiSelectList;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.DynamicMultiSelectListElement;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseData;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.YesOrNo;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.correspondence.SelectablePartiesCorrespondenceService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.express.ExpressCaseService;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.ExpressCaseParticipation.ENROLLED;
import static uk.gov.hmcts.reform.finrem.caseorchestration.service.HearingDocumentService.HEARING_DEFAULT_CORRESPONDENCE_ERROR_MESSAGE;
import static uk.gov.hmcts.reform.finrem.caseorchestration.service.ValidateHearingService.DATE_BETWEEN_12_AND_16_WEEKS;
import static uk.gov.hmcts.reform.finrem.caseorchestration.service.ValidateHearingService.DATE_BETWEEN_16_AND_20_WEEKS;
import static uk.gov.hmcts.reform.finrem.caseorchestration.service.ValidateHearingService.DATE_BETWEEN_6_AND_10_WEEKS;

@ExtendWith(MockitoExtension.class)
class ValidateHearingServiceTest {

    private static final String ISSUE_DATE_FAST_TRACK_DECISION_OR_HEARING_DATE_IS_EMPTY =
        "Issue Date, fast track decision or hearingDate is empty";

    @InjectMocks
    private ValidateHearingService service;

    @Mock
    private SelectablePartiesCorrespondenceService selectablePartiesCorrespondenceService;
    @Mock
    private ExpressCaseService expressCaseService;

    private FinremCaseDetails caseDetails;
    private FinremCaseData caseData;

    @BeforeEach
    public void setup() {
        caseDetails = getCaseDetails();
        caseData = caseDetails.getData();
    }

    @Test
    void issueDateEmpty() {
        caseData.setFastTrackDecision(YesOrNo.YES);
        caseData.setIssueDate(null);
        caseData.getListForHearingWrapper().setHearingDate(LocalDate.now().plusWeeks(7));
        List<String> errors = doTestErrors();

        caseData.setPartiesOnCase(getPartiesOnCase());
        assertThat(errors).contains(ISSUE_DATE_FAST_TRACK_DECISION_OR_HEARING_DATE_IS_EMPTY);
    }

    @Test
    void fastTrackDecisionEmpty() {
        caseData.setIssueDate(LocalDate.now());
        caseData.getListForHearingWrapper().setHearingDate(LocalDate.now().plusWeeks(7));
        caseData.setFastTrackDecisionReason(null);
        caseData.setPartiesOnCase(getPartiesOnCase());

        List<String> errors = doTestErrors();
        assertThat(errors).contains(ISSUE_DATE_FAST_TRACK_DECISION_OR_HEARING_DATE_IS_EMPTY);
    }

    @Test
    void hearingDateEmpty() {
        caseData.setIssueDate(LocalDate.now());
        caseData.setFastTrackDecision(YesOrNo.YES);
        caseData.getListForHearingWrapper().setHearingDate(null);
        caseData.setPartiesOnCase(getPartiesOnCase());

        List<String> errors = doTestErrors();
        assertThat(errors).contains(ISSUE_DATE_FAST_TRACK_DECISION_OR_HEARING_DATE_IS_EMPTY);
    }

    @Test
    void givenHearingWithNoAppRespNotificationSelected_whenValidate_thenThrowErrorCustomMessage() {
        caseData.setIssueDate(LocalDate.now());
        caseData.setFastTrackDecision(YesOrNo.YES);
        caseData.getListForHearingWrapper().setHearingDate(LocalDate.now().plusWeeks(7));

        DynamicMultiSelectList partiesOnCase = getPartiesOnCaseMissingSelectedAppAndResp();
        when(selectablePartiesCorrespondenceService.validateApplicantAndRespondentCorrespondenceAreSelected(
            caseData, HEARING_DEFAULT_CORRESPONDENCE_ERROR_MESSAGE)).thenReturn(List.of(HEARING_DEFAULT_CORRESPONDENCE_ERROR_MESSAGE));


        caseData.setPartiesOnCase(partiesOnCase);
        List<String> errors = doTestErrors();
        assertThat(errors).contains(HEARING_DEFAULT_CORRESPONDENCE_ERROR_MESSAGE);
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
    void noErrors() {
        caseData.setIssueDate(LocalDate.now());
        caseData.setFastTrackDecision(YesOrNo.YES);
        caseData.getListForHearingWrapper().setHearingDate(LocalDate.now().plusWeeks(7));
        List<String> errors = doTestErrors();
        assertThat(errors).isEmpty();
    }

    @Test
    void fastTrackHearingDatesWarningWithJudiciaryOutcome() {
        caseData.setIssueDate(LocalDate.now());
        caseData.setFastTrackDecision(null);
        caseData.getListForHearingWrapper().setHearingDate(LocalDate.now().plusWeeks(3));
        caseData.setCaseAllocatedTo(YesOrNo.YES);
        List<String> errors = doTestWarnings();
        assertThat(errors).contains(DATE_BETWEEN_6_AND_10_WEEKS);
    }

    @Test
    void fastTrackHearingDatesWarningWithoutJudiciaryOutcome() {
        caseData.setIssueDate(LocalDate.now());
        caseData.setFastTrackDecision(YesOrNo.YES);
        caseData.getListForHearingWrapper().setHearingDate(LocalDate.now().plusWeeks(3));
        List<String> errors = doTestWarnings();
        assertThat(errors).contains(DATE_BETWEEN_6_AND_10_WEEKS);
    }

    @Test
    void fastTrackHearingDatesNoWarning() {
        caseData.setIssueDate(LocalDate.now());
        caseData.setFastTrackDecision(YesOrNo.YES);
        caseData.getListForHearingWrapper().setHearingDate(LocalDate.now().plusWeeks(7));
        List<String> errors = doTestWarnings();
        assertThat(errors).isEmpty();
    }

    @Test
    void expressPilotDatesWarningWithoutJudiciaryOutcome() {
        caseData.setIssueDate(LocalDate.now());
        caseData.setFastTrackDecision(YesOrNo.NO);
        caseData.setExpressCaseParticipation(ENROLLED);
        caseData.getListForHearingWrapper().setHearingDate(LocalDate.now().plusWeeks(3));
        when(expressCaseService.isExpressCase(caseData)).thenReturn(true);

        List<String> errors = doTestWarnings();
        assertThat(errors).contains(DATE_BETWEEN_16_AND_20_WEEKS);
    }

    @Test
    void expressPilotDatesNoWarning() {
        caseData.setIssueDate(LocalDate.now());
        caseData.setFastTrackDecision(YesOrNo.NO);
        caseData.setExpressCaseParticipation(ENROLLED);
        caseData.getListForHearingWrapper().setHearingDate(LocalDate.now().plusWeeks(17));
        when(expressCaseService.isExpressCase(caseData)).thenReturn(true);

        List<String> errors = doTestWarnings();
        assertThat(errors).isEmpty();
    }

    @Test
    void nonFastTrackHearingDatesWarning() {
        caseData.setIssueDate(LocalDate.now());
        caseData.setFastTrackDecision(YesOrNo.NO);
        caseData.getListForHearingWrapper().setHearingDate(LocalDate.now().plusWeeks(3));
        List<String> errors = doTestWarnings();
        assertThat(errors).contains(DATE_BETWEEN_12_AND_16_WEEKS);
    }

    @Test
    void nonFastTrackHearingDatesNoWarning() {
        caseData.setIssueDate(LocalDate.now());
        caseData.setFastTrackDecision(YesOrNo.NO);
        caseData.getListForHearingWrapper().setHearingDate(LocalDate.now().plusWeeks(13));
        List<String> errors = doTestWarnings();
        assertThat(errors).isEmpty();
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
