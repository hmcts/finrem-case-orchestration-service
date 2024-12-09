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
    void givenCase_whenRespondentRefugeQuestionAnswered_thenRespondentRefugeTabUpdated() {

        // imitate user answering YES to respondent in refuge question on create case journey.
        caseDetails.getData().getRefugeWrapper().setRespondentInRefugeQuestion(YesOrNo.YES);
        assertEquals(YesOrNo.YES, caseDetails.getData().getRefugeWrapper().getRespondentInRefugeQuestion());

        RefugeWrapperUtils.updateRespondentInRefugeTab(caseDetails);

        // Assert handler updated respondentInRefugeTab from respondentInRefugeQuestion, and latter then cleared.
        assertEquals(YesOrNo.YES, caseDetails.getData().getRefugeWrapper().getRespondentInRefugeTab());
        assertNull(caseDetails.getData().getRefugeWrapper().getRespondentInRefugeQuestion());
    }

    @Test
    void givenCase_whenRespondentRefugeQuestionUnanswered_thenRespondentInRefugeTabUnchanged() {

        // imitate user not answering Respondent in refuge question on create case journey.
        assertNull(caseDetails.getData().getRefugeWrapper().getRespondentInRefugeQuestion());

        RefugeWrapperUtils.updateRespondentInRefugeTab(caseDetails);

        // Assert handler didn't update respondentInRefugeTab from respondentInRefugeQuestion, which remains null.
        assertNull(caseDetails.getData().getRefugeWrapper().getRespondentInRefugeTab());
        assertNull(caseDetails.getData().getRefugeWrapper().getRespondentInRefugeQuestion());
    }
}
