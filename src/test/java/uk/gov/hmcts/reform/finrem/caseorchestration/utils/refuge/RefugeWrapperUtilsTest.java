package uk.gov.hmcts.reform.finrem.caseorchestration.utils.refuge;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import uk.gov.hmcts.reform.finrem.caseorchestration.FinremCaseDetailsBuilderFactory;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.YesOrNo;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseType.CONTESTED;

class RefugeWrapperUtilsTest {

    private FinremCaseDetails caseDetails;

    @BeforeEach
    void setUp() {
        caseDetails = FinremCaseDetailsBuilderFactory.from(123L, CONTESTED).build();
    }

    @Test
    void givenCase_whenApplicantRefugeQuestionAnswered_thenApplicantRefugeTabUpdated() {

        // imitate user answering YES to applicant in refuge question on create case journey.
        caseDetails.getData().getRefugeWrapper().setApplicantInRefugeQuestion(YesOrNo.YES);
        assertEquals(YesOrNo.YES, caseDetails.getData().getRefugeWrapper().getApplicantInRefugeQuestion());

        RefugeWrapperUtils.updateApplicantInRefugeTab(caseDetails);

        // Assert handler updated applicantInRefugeTab from applicantInRefugeQuestion, and latter then cleared.
        assertEquals(YesOrNo.YES, caseDetails.getData().getRefugeWrapper().getApplicantInRefugeTab());
        assertNull(caseDetails.getData().getRefugeWrapper().getApplicantInRefugeQuestion());
    }

    @Test
    void givenCase_whenApplicantRefugeQuestionUnanswered_thenApplicantRefugeTabUnchanged() {

        // imitate user not answering applicant in refuge question on create case journey.
        assertNull(caseDetails.getData().getRefugeWrapper().getApplicantInRefugeQuestion());

        RefugeWrapperUtils.updateApplicantInRefugeTab(caseDetails);

        // Assert handler didn't update applicantInRefugeTab from applicantInRefugeQuestion, which remains null.
        assertNull(caseDetails.getData().getRefugeWrapper().getApplicantInRefugeTab());
        assertNull(caseDetails.getData().getRefugeWrapper().getApplicantInRefugeQuestion());
    }

    @Test
    void givenCase_whenApplicantInRefugeTabHasData_thenPopulateApplicantInRefugeQuestion() {

        // imitate user answering YES to applicant in refuge question on create case journey.
        caseDetails.getData().getRefugeWrapper().setApplicantInRefugeTab(YesOrNo.YES);
        assertEquals(YesOrNo.YES, caseDetails.getData().getRefugeWrapper().getApplicantInRefugeTab());

        RefugeWrapperUtils.populateApplicantInRefugeQuestion(caseDetails);

        // Assert handler updated applicantInRefugeTab from applicantInRefugeQuestion, and latter then cleared.
        assertEquals(YesOrNo.YES, caseDetails.getData().getRefugeWrapper().getApplicantInRefugeQuestion());
    }

    @Test
    void givenCase_whenApplicantInRefugeTabHasNoData_thenApplicantInRefugeQuestionStaysNull() {

        // imitate user answering YES to applicant in refuge question on create case journey.
        assertNull(caseDetails.getData().getRefugeWrapper().getApplicantInRefugeTab());

        RefugeWrapperUtils.populateApplicantInRefugeQuestion(caseDetails);

        // Assert handler updated applicantInRefugeTab from applicantInRefugeQuestion, and latter then cleared.
        assertNull(caseDetails.getData().getRefugeWrapper().getApplicantInRefugeQuestion());
    }

    @Test
    void givenCase_whenRespondentInRefugeTabHasData_thenPopulateRespondentInRefugeQuestion() {

        // imitate user answering YES to respondent in refuge question on create case journey.
        caseDetails.getData().getRefugeWrapper().setRespondentInRefugeTab(YesOrNo.YES);
        assertEquals(YesOrNo.YES, caseDetails.getData().getRefugeWrapper().getRespondentInRefugeTab());

        RefugeWrapperUtils.populateRespondentInRefugeQuestion(caseDetails);

        // Assert handler updated respondentInRefugeTab from respondentInRefugeQuestion, and latter then cleared.
        assertEquals(YesOrNo.YES, caseDetails.getData().getRefugeWrapper().getRespondentInRefugeQuestion());
    }

    @Test
    void givenCase_whenRespondentInRefugeTabHasNoData_thenRespondentInRefugeQuestionStaysNull() {

        // imitate user answering YES to respondent in refuge question on create case journey.
        assertNull(caseDetails.getData().getRefugeWrapper().getRespondentInRefugeTab());

        RefugeWrapperUtils.populateRespondentInRefugeQuestion(caseDetails);

        // Assert handler updated respondentInRefugeTab from respondentInRefugeQuestion, and latter then cleared.
        assertNull(caseDetails.getData().getRefugeWrapper().getRespondentInRefugeQuestion());
    }
}
