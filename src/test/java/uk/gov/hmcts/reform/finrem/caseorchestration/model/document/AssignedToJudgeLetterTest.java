package uk.gov.hmcts.reform.finrem.caseorchestration.model.document;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class AssignedToJudgeLetterTest {

    @Test
    public void checkAllStatusValues() {
        AssignedToJudgeLetter assignedToJudgeLetter =
            AssignedToJudgeLetter.builder()
                .caseNumber("caseNumber")
                .reference("reference")
                .letterDate("letterDate")
                .applicantName("applicantName")
                .respondentName("respondentName")
                .build();
        assertEquals("caseNumber", assignedToJudgeLetter.getCaseNumber());
        assertEquals("reference", assignedToJudgeLetter.getReference());
        assertEquals("letterDate", assignedToJudgeLetter.getLetterDate());
        assertEquals("applicantName", assignedToJudgeLetter.getApplicantName());
        assertEquals("respondentName", assignedToJudgeLetter.getRespondentName());
    }
}
