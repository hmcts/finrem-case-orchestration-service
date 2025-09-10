package uk.gov.hmcts.reform.finrem.caseorchestration.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.finrem.caseorchestration.TestConstants;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseRole;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.DynamicList;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.DynamicListElement;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.DynamicMultiSelectList;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.DynamicMultiSelectListElement;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseData;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.YesOrNo;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.managehearings.HearingType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.managehearings.WorkingHearing;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.correspondence.SelectablePartiesCorrespondenceService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.express.ExpressCaseService;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseRole.APP_SOLICITOR;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseRole.INTVR_SOLICITOR_1;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseRole.INTVR_SOLICITOR_2;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseRole.INTVR_SOLICITOR_3;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseRole.INTVR_SOLICITOR_4;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.ExpressCaseParticipation.ENROLLED;
import static uk.gov.hmcts.reform.finrem.caseorchestration.service.HearingDocumentService.HEARING_DEFAULT_CORRESPONDENCE_ERROR_MESSAGE;
import static uk.gov.hmcts.reform.finrem.caseorchestration.service.ValidateHearingService.DATE_BETWEEN_12_AND_16_WEEKS;
import static uk.gov.hmcts.reform.finrem.caseorchestration.service.ValidateHearingService.DATE_BETWEEN_16_AND_20_WEEKS;
import static uk.gov.hmcts.reform.finrem.caseorchestration.service.ValidateHearingService.DATE_BETWEEN_6_AND_10_WEEKS;
import static uk.gov.hmcts.reform.finrem.caseorchestration.service.ValidateHearingService.REQUIRED_FIELD_EMPTY_ERROR;

@ExtendWith(MockitoExtension.class)
class ValidateHearingServiceTest {

    @InjectMocks
    private ValidateHearingService service;

    @Mock
    private SelectablePartiesCorrespondenceService selectablePartiesCorrespondenceService;
    @Mock
    private ExpressCaseService expressCaseService;

    private FinremCaseDetails caseDetails;
    private FinremCaseData caseData;

    @BeforeEach
    void setup() {
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
        assertThat(errors).containsExactly(REQUIRED_FIELD_EMPTY_ERROR);
    }

    @Test
    void fastTrackDecisionEmpty() {
        caseData.setIssueDate(LocalDate.now());
        caseData.getListForHearingWrapper().setHearingDate(LocalDate.now().plusWeeks(7));
        caseData.setFastTrackDecisionReason(null);
        caseData.setPartiesOnCase(getPartiesOnCase());

        List<String> errors = doTestErrors();
        assertThat(errors).containsExactly(REQUIRED_FIELD_EMPTY_ERROR);
    }

    @Test
    void hearingDateEmpty() {
        caseData.setIssueDate(LocalDate.now());
        caseData.setFastTrackDecision(YesOrNo.YES);
        caseData.getListForHearingWrapper().setHearingDate(null);
        caseData.setPartiesOnCase(getPartiesOnCase());

        List<String> errors = doTestErrors();
        assertThat(errors).containsExactly(REQUIRED_FIELD_EMPTY_ERROR);
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
        assertThat(errors).containsExactly(HEARING_DEFAULT_CORRESPONDENCE_ERROR_MESSAGE);
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
        assertThat(errors).containsExactly(DATE_BETWEEN_6_AND_10_WEEKS);
    }

    @Test
    void fastTrackHearingDatesWarningWithoutJudiciaryOutcome() {
        caseData.setIssueDate(LocalDate.now());
        caseData.setFastTrackDecision(YesOrNo.YES);
        caseData.getListForHearingWrapper().setHearingDate(LocalDate.now().plusWeeks(3));
        List<String> errors = doTestWarnings();
        assertThat(errors).containsExactly(DATE_BETWEEN_6_AND_10_WEEKS);
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
        caseData.getExpressCaseWrapper().setExpressCaseParticipation(ENROLLED);
        caseData.getListForHearingWrapper().setHearingDate(LocalDate.now().plusWeeks(3));
        when(expressCaseService.isExpressCase(caseData)).thenReturn(true);

        List<String> errors = doTestWarnings();
        assertThat(errors).containsExactly(DATE_BETWEEN_16_AND_20_WEEKS);
    }

    @Test
    void expressPilotDatesNoWarning() {
        caseData.setIssueDate(LocalDate.now());
        caseData.setFastTrackDecision(YesOrNo.NO);
        caseData.getExpressCaseWrapper().setExpressCaseParticipation(ENROLLED);
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
        assertThat(errors).containsExactly(DATE_BETWEEN_12_AND_16_WEEKS);
    }

    @Test
    void nonFastTrackHearingDatesNoWarning() {
        caseData.setIssueDate(LocalDate.now());
        caseData.setFastTrackDecision(YesOrNo.NO);
        caseData.getListForHearingWrapper().setHearingDate(LocalDate.now().plusWeeks(13));
        List<String> errors = doTestWarnings();
        assertThat(errors).isEmpty();
    }

    @Test
    void manageHearingIssueDateEmpty() {
        caseData.setFastTrackDecision(YesOrNo.YES);
        caseData.setIssueDate(null);
        setUpManageHearingToAdd(caseData, LocalDate.now().plusWeeks(7));
        List<String> errors = service.validateManageHearingErrors(caseData);

        caseData.setPartiesOnCase(getPartiesOnCase());
        assertThat(errors).containsExactly(REQUIRED_FIELD_EMPTY_ERROR);
    }

    @Test
    void manageHearingFastTrackDecisionEmpty() {
        caseData.setIssueDate(LocalDate.now());
        setUpManageHearingToAdd(caseData, LocalDate.now().plusWeeks(7));
        caseData.setFastTrackDecisionReason(null);
        caseData.setPartiesOnCase(getPartiesOnCase());

        List<String> errors = service.validateManageHearingErrors(caseData);
        assertThat(errors).containsExactly(REQUIRED_FIELD_EMPTY_ERROR);
    }

    @Test
    void manageHearingHearingNoErrors() {
        caseData.setIssueDate(LocalDate.now());
        caseData.setFastTrackDecision(YesOrNo.YES);
        setUpManageHearingToAdd(caseData, LocalDate.now().plusWeeks(7));

        List<String> errors = service.validateManageHearingErrors(caseData);
        assertThat(errors).isEmpty();
    }

    @Test
    void manageFastTrackHearingDatesWarningFdr() {
        caseData.setIssueDate(LocalDate.now());
        caseData.setFastTrackDecision(YesOrNo.YES);
        setUpManageHearingToAdd(caseData, LocalDate.now().plusWeeks(3));

        List<String> errors = service.validateManageHearingWarnings(caseData, HearingType.FDR);

        assertThat(errors).containsExactly(DATE_BETWEEN_6_AND_10_WEEKS);
    }

    @Test
    void manageFastTrackHearingDatesWarningFda() {
        caseData.setIssueDate(LocalDate.now());
        caseData.setFastTrackDecision(YesOrNo.YES);
        setUpManageHearingToAdd(caseData, LocalDate.now().plusWeeks(3));

        List<String> errors = service.validateManageHearingWarnings(caseData, HearingType.FDA);
        assertThat(errors).containsExactly(DATE_BETWEEN_6_AND_10_WEEKS);
    }

    @Test
    void manageHearingHearingFastTrackHearingDatesNoWarningFdr() {
        caseData.setIssueDate(LocalDate.now());
        caseData.setFastTrackDecision(YesOrNo.YES);
        setUpManageHearingToAdd(caseData, LocalDate.now().plusWeeks(7));

        List<String> errors = service.validateManageHearingWarnings(caseData, HearingType.FDR);
        assertThat(errors).isEmpty();
    }

    @Test
    void manageHearingHearingFastTrackHearingDatesNoWarningFda() {
        caseData.setIssueDate(LocalDate.now());
        caseData.setFastTrackDecision(YesOrNo.YES);
        setUpManageHearingToAdd(caseData, LocalDate.now().plusWeeks(7));

        List<String> errors = service.validateManageHearingWarnings(caseData, HearingType.FDA);
        assertThat(errors).isEmpty();
    }

    @Test
    void manageHearingHearingExpressPilotDatesFdr() {
        caseData.setIssueDate(LocalDate.now());
        caseData.setFastTrackDecision(YesOrNo.NO);
        caseData.getExpressCaseWrapper().setExpressCaseParticipation(ENROLLED);
        setUpManageHearingToAdd(caseData, LocalDate.now().plusWeeks(3));
        when(expressCaseService.isExpressCase(caseData)).thenReturn(true);

        List<String> errors = service.validateManageHearingWarnings(caseData, HearingType.FDR);
        assertThat(errors).containsExactly(DATE_BETWEEN_16_AND_20_WEEKS);
    }

    @Test
    void manageHearingHearingExpressPilotDatesFda() {
        caseData.setIssueDate(LocalDate.now());
        caseData.setFastTrackDecision(YesOrNo.NO);
        caseData.getExpressCaseWrapper().setExpressCaseParticipation(ENROLLED);
        setUpManageHearingToAdd(caseData, LocalDate.now().plusWeeks(3));
        when(expressCaseService.isExpressCase(caseData)).thenReturn(true);

        List<String> errors = service.validateManageHearingWarnings(caseData, HearingType.FDA);
        assertThat(errors).containsExactly(DATE_BETWEEN_16_AND_20_WEEKS);
    }

    @Test
    void manageHearingHearingExpressPilotDatesNoWarningFdr() {
        caseData.setIssueDate(LocalDate.now());
        caseData.setFastTrackDecision(YesOrNo.NO);
        caseData.getExpressCaseWrapper().setExpressCaseParticipation(ENROLLED);
        setUpManageHearingToAdd(caseData, LocalDate.now().plusWeeks(17));
        when(expressCaseService.isExpressCase(caseData)).thenReturn(true);

        List<String> errors = service.validateManageHearingWarnings(caseData, HearingType.FDR);
        assertThat(errors).isEmpty();
    }

    @Test
    void manageHearingHearingExpressPilotDatesNoWarningFda() {
        caseData.setIssueDate(LocalDate.now());
        caseData.setFastTrackDecision(YesOrNo.NO);
        caseData.getExpressCaseWrapper().setExpressCaseParticipation(ENROLLED);
        setUpManageHearingToAdd(caseData, LocalDate.now().plusWeeks(17));
        when(expressCaseService.isExpressCase(caseData)).thenReturn(true);

        List<String> errors = service.validateManageHearingWarnings(caseData, HearingType.FDA);
        assertThat(errors).isEmpty();
    }

    @Test
    void manageHearingHearingNonFastTrackHearingDatesWarningFdr() {
        caseData.setIssueDate(LocalDate.now());
        caseData.setFastTrackDecision(YesOrNo.NO);
        setUpManageHearingToAdd(caseData, LocalDate.now().plusWeeks(3));
        List<String> errors = service.validateManageHearingWarnings(caseData, HearingType.FDR);
        assertThat(errors).containsExactly(DATE_BETWEEN_12_AND_16_WEEKS);
    }

    @Test
    void manageHearingHearingNonFastTrackHearingDatesWarningFda() {
        caseData.setIssueDate(LocalDate.now());
        caseData.setFastTrackDecision(YesOrNo.NO);
        setUpManageHearingToAdd(caseData, LocalDate.now().plusWeeks(3));
        List<String> errors = service.validateManageHearingWarnings(caseData, HearingType.FDA);
        assertThat(errors).containsExactly(DATE_BETWEEN_12_AND_16_WEEKS);
    }

    @Test
    void manageHearingHearingNonFastTrackHearingDatesNoWarningFdr() {
        caseData.setIssueDate(LocalDate.now());
        caseData.setFastTrackDecision(YesOrNo.NO);
        setUpManageHearingToAdd(caseData, LocalDate.now().plusWeeks(13));
        List<String> errors = service.validateManageHearingWarnings(caseData, HearingType.FDR);
        assertThat(errors).isEmpty();
    }

    @Test
    void manageHearingHearingNonFastTrackHearingDatesNoWarningFda() {
        caseData.setIssueDate(LocalDate.now());
        caseData.setFastTrackDecision(YesOrNo.NO);
        setUpManageHearingToAdd(caseData, LocalDate.now().plusWeeks(13));
        List<String> errors = service.validateManageHearingWarnings(caseData, HearingType.FDA);
        assertThat(errors).isEmpty();
    }

    @Test
    void manageHearingHearingDatesWarningOtherHearingType() {
        caseData.setIssueDate(LocalDate.now());
        caseData.setFastTrackDecision(YesOrNo.NO);
        setUpManageHearingToAdd(caseData, LocalDate.now().plusWeeks(13));
        List<String> errors = service.validateManageHearingWarnings(caseData, HearingType.DIR);
        assertThat(errors).isEmpty();
    }

    @Test
    void manageHearingHearingDatesWarningNoIssueDate() {
        caseData.setIssueDate(null);
        caseData.setFastTrackDecision(YesOrNo.NO);
        setUpManageHearingToAdd(caseData, LocalDate.now().plusWeeks(13));
        List<String> errors = service.validateManageHearingWarnings(caseData, HearingType.DIR);
        assertThat(errors).isEmpty();
    }

    @Test
    void validateGeneralApplicationDirectionsMandatoryParties_whenPartiesPresent_noErrors() {
        // arrange
        WorkingHearing workingHearing = WorkingHearing.builder().partiesOnCaseMultiSelectList(
            DynamicMultiSelectList.builder()
                .value(List.of(
                    DynamicMultiSelectListElement.builder().code(CaseRole.APP_SOLICITOR.getCcdCode()).build(),
                    DynamicMultiSelectListElement.builder().code(CaseRole.RESP_SOLICITOR.getCcdCode()).build()))
                .listItems(List.of()).build()
        ).build();

        caseData.getManageHearingsWrapper().setWorkingHearing(workingHearing);

        // act
        List<String> errors = service.validateGeneralApplicationDirectionsMandatoryParties(caseData);

        // assert no errors when applicant and respondent selected for hearing correspondence
        assertThat(errors).isEmpty();
    }

    @ParameterizedTest
    @MethodSource("provideWorkingHearingsThatFailGadPartyChecks")
    void validateGeneralApplicationDirectionsMandatoryParties_whenPartiesMissing_showErrors(WorkingHearing workingHearing) {
        // arrange
        caseData.getManageHearingsWrapper().setWorkingHearing(workingHearing);

        // act
        List<String> errors = service.validateGeneralApplicationDirectionsMandatoryParties(caseData);

        // assert errors when selected parties don't include applicant and respondent for hearing correspondence
        assertThat(errors).contains("Select Applicant and Respondent for \"Who should see this order?\"");
    }

    @Test
    void validateGeneralApplicationDirectionsNoticeSelection_whenYes_noErrors() {
        // arrange
        caseData.getManageHearingsWrapper().setWorkingHearing(
            WorkingHearing.builder().hearingNoticePrompt(YesOrNo.YES).build());

        // act
        List<String> errors = service.validateGeneralApplicationDirectionsNoticeSelection(caseData);

        // assert
        assertThat(errors).isEmpty();
    }

    @ParameterizedTest
    @ValueSource(booleans = {true, false})
    void validateGeneralApplicationDirectionsNoticeSelection_whenNotYes_showErrors(boolean validInput) {
        // arrange
        if (validInput) {
         caseData.getManageHearingsWrapper().setWorkingHearing(
            WorkingHearing.builder().hearingNoticePrompt(YesOrNo.NO).build());
        } else {
            caseData.getManageHearingsWrapper().setWorkingHearing(
                WorkingHearing.builder().hearingNoticePrompt(null).build());
        }

        // act
        List<String> errors = service.validateGeneralApplicationDirectionsNoticeSelection(caseData);

        // assert
        assertThat(errors).contains("Select \"Yes\" for \"Do you want to send a notice of hearing?\"");
    }

    @Test
    void validateGeneralApplicationDirectionsIntervenerParties_whenIntervenerCreatedGaButNotSelected_showErrors() {
        // arrange - label selected for a General Application created by an intervener
        String label = "General Application 1 - Received from - Intervener 1 - Created Date - 2025-09-04 - Hearing Required - Yes";
        caseData.getGeneralApplicationWrapper().setGeneralApplicationDirectionsList(
            DynamicList.builder()
                .value(DynamicListElement.builder().label(label).build()
                ).build()
        );

        // arrange - omit intervener parties from working hearing notice selection
        caseData.getManageHearingsWrapper()
            .setWorkingHearing(WorkingHearing.builder().partiesOnCaseMultiSelectList(
                DynamicMultiSelectList.builder()
                    .value(List.of(
                        DynamicMultiSelectListElement.builder().code(APP_SOLICITOR.getCcdCode()).build(),
                        DynamicMultiSelectListElement.builder().code(CaseRole.RESP_SOLICITOR.getCcdCode()).build()))
                    .listItems(List.of()).build()
            ).build());

        // Act
        List<String> warnings = service.validateGeneralApplicationDirectionsIntervenerParties(caseData);

        // Assert warnings.  GA created by intervener, but no intervener party selected for correspondence.
        assertThat(warnings).containsExactly(
            "An Intervener created this general application. " +
                "Consider if an Intervener should be selected in \"Who should see this order?\"");
    }

    /*
     * Any intervener party selected will satisfy the validation.  Manage Interveners can modify the list of interveners
     * on the case after the GA is created, so don't validate against a specific intervener.
     */
    @ParameterizedTest
    @EnumSource(value = CaseRole.class, names = {
        "INTVR_SOLICITOR_1", "INTVR_SOLICITOR_2", "INTVR_SOLICITOR_3", "INTVR_SOLICITOR_4"
    })
    void validateGeneralApplicationDirectionsIntervenerParties_whenIntervenerCreatedGaAndSelected_NoErrors(CaseRole intervenerCaseRole) {
        // arrange - label selected for a General Application created by an intervener
        String label = "General Application 1 - Received from - Intervener 2 - Created Date - 2025-09-04 - Hearing Required - Yes";
        caseData.getGeneralApplicationWrapper().setGeneralApplicationDirectionsList(
            DynamicList.builder()
                .value(DynamicListElement.builder().label(label).build()
                ).build()
        );

        // arrange - add parameterised intervener party to working hearing
        caseData.getManageHearingsWrapper()
            .setWorkingHearing(
                WorkingHearing.builder().partiesOnCaseMultiSelectList(
                    DynamicMultiSelectList.builder()
                        .value(List.of(
                            DynamicMultiSelectListElement.builder().code(intervenerCaseRole.getCcdCode()).build()))
                        .listItems(List.of()).build()
                ).build());

        // Act
        List<String> warnings = service.validateGeneralApplicationDirectionsIntervenerParties(caseData);

        // Assert that no warnings.  GA created by intervener, and one of the parameterised intervener parties selected.
        assertThat(warnings).isEmpty();
    }

    @Test
    void validateGeneralApplicationDirectionsIntervenerParties_whenApplicantCreatedGa_NoErrors() {
        // arrange - label selected for a General Application created by an applicant
        String label = "General Application 1 - Received from - Applicant - Created Date - 2025-09-04 - Hearing Required - Yes";
        caseData.getGeneralApplicationWrapper().setGeneralApplicationDirectionsList(
            DynamicList.builder()
                .value(DynamicListElement.builder().label(label).build()
                ).build()
        );

        // arrange - omit intervener parties from working hearing notice selection
        caseData.getManageHearingsWrapper()
            .setWorkingHearing(WorkingHearing.builder().partiesOnCaseMultiSelectList(
                DynamicMultiSelectList.builder()
                    .value(List.of(
                        DynamicMultiSelectListElement.builder().code(APP_SOLICITOR.getCcdCode()).build(),
                        DynamicMultiSelectListElement.builder().code(CaseRole.RESP_SOLICITOR.getCcdCode()).build()))
                    .listItems(List.of()).build()
            ).build());

        // Act
        List<String> warnings = service.validateGeneralApplicationDirectionsIntervenerParties(caseData);

        // Assert that no warnings.  GA created by applicant, so not selecting intervener party for correspondence OK.
        assertThat(warnings).isEmpty();
    }

    private List<String> doTestWarnings() {
        return service.validateHearingWarnings(caseDetails);
    }

    private List<String> doTestErrors() {
        return service.validateHearingErrors(caseDetails);
    }

    private FinremCaseDetails getCaseDetails() {
        return FinremCaseDetails.builder().id(Long.parseLong(TestConstants.CASE_ID)).data(FinremCaseData.builder().build()).build();
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
                getDynamicElementList(INTVR_SOLICITOR_1.getCcdCode()),
                getDynamicElementList(INTVR_SOLICITOR_2.getCcdCode()),
                getDynamicElementList(INTVR_SOLICITOR_3.getCcdCode()),
                getDynamicElementList(INTVR_SOLICITOR_4.getCcdCode())));

        List<DynamicMultiSelectListElement> selectedParties = new ArrayList<>(
            List.of(getDynamicElementList(INTVR_SOLICITOR_1.getCcdCode()),
                getDynamicElementList(INTVR_SOLICITOR_2.getCcdCode()),
                getDynamicElementList(INTVR_SOLICITOR_3.getCcdCode()),
                getDynamicElementList(INTVR_SOLICITOR_4.getCcdCode())));
        return DynamicMultiSelectList.builder()
            .value(selectedParties)
            .listItems(activeParties)
            .build();
    }

    private DynamicMultiSelectListElement getDynamicElementList(String role) {
        return DynamicMultiSelectListElement.builder()
            .code(role)
            .label(role)
            .build();
    }

    private void setUpManageHearingToAdd(FinremCaseData caseData, LocalDate hearingDate) {
        caseData.getManageHearingsWrapper().setWorkingHearing(WorkingHearing
            .builder()
            .hearingDate(hearingDate)
            .build());
    }

    private static Stream<Arguments>provideWorkingHearingsThatFailGadPartyChecks() {
        return Stream.of(
            // Respondent not selected
            Arguments.of(WorkingHearing.builder().partiesOnCaseMultiSelectList(
                DynamicMultiSelectList.builder()
                    .value(List.of(
                        DynamicMultiSelectListElement.builder().code(CaseRole.APP_SOLICITOR.getCcdCode()).build()))
                    .listItems(List.of()).build()
            ).build()),
            // Applicant not selected
            Arguments.of(WorkingHearing.builder().partiesOnCaseMultiSelectList(
                DynamicMultiSelectList.builder()
                    .value(List.of(
                        DynamicMultiSelectListElement.builder().code(CaseRole.RESP_SOLICITOR.getCcdCode()).build()))
                    .listItems(List.of()).build()
            ).build()),
            // No Parties selected
            Arguments.of(WorkingHearing.builder().build())
        );
    }
}
